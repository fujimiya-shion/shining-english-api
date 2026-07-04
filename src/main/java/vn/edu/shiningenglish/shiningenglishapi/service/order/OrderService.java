package vn.edu.shiningenglish.shiningenglishapi.service.order;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import vn.edu.shiningenglish.shiningenglishapi.enums.OrderStatus;
import vn.edu.shiningenglish.shiningenglishapi.enums.PaymentMethod;
import vn.edu.shiningenglish.shiningenglishapi.model.dto.transaction.checkout.CheckoutOrderResponse;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.OrderItem;
import vn.edu.shiningenglish.shiningenglishapi.repository.*;
import vn.edu.shiningenglish.shiningenglishapi.repository.order.*;
import vn.edu.shiningenglish.shiningenglishapi.repository.cart.*;
import vn.edu.shiningenglish.shiningenglishapi.repository.course.*;
import vn.edu.shiningenglish.shiningenglishapi.service.enrollment.EnrollmentService;
import vn.edu.shiningenglish.shiningenglishapi.service.order.strategy.PaymentStrategyFactory;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.CheckoutCustomerData;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.QueryOption;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    private final PaymentStrategyFactory paymentStrategyFactory;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartRepository cartRepository, CourseRepository courseRepository,
                        EnrollmentService enrollmentService,
                        PaymentStrategyFactory paymentStrategyFactory) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.courseRepository = courseRepository;
        this.enrollmentService = enrollmentService;
        this.paymentStrategyFactory = paymentStrategyFactory;
    }

    public Page<Order> listByUserId(Long userId, QueryOption options) {
        var pageable = org.springframework.data.domain.PageRequest.of(
            options.getPage() != null ? options.getPage() - 1 : 0,
            options.getPerPage(),
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        );
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public java.util.Optional<Order> detailByUserId(Long userId, Long orderId) {
        var opt = orderRepository.findByIdAndUserId(orderId, userId);
        opt.ifPresent(order -> {
            var strategy = paymentStrategyFactory.make(order.getPaymentMethod());
            var refreshed = strategy.refresh(order);
            order.setStatus(refreshed.getStatus());
            order.setPaymentMetadata(refreshed.getPaymentMetadata());
            order.setPaymentReference(refreshed.getPaymentReference());
            order.setPaymentCheckoutUrl(refreshed.getPaymentCheckoutUrl());
            order.setPaidAt(refreshed.getPaidAt());
        });
        return opt;
    }

    @Transactional
    public Map<String, Object> createFromCart(Long userId, PaymentMethod paymentMethod, CheckoutCustomerData customerData) {
        var items = cartRepository.findByUserId(userId);
        if (items.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        var courseIds = new ArrayList<Long>();
        var total = items.stream()
            .mapToInt(item -> {
                var course = courseRepository.findById(item.getCourseId());
                courseIds.add(item.getCourseId());
                return course.map(c -> (c.getPrice() != null ? c.getPrice() : 0) * (item.getQuantity() != null ? item.getQuantity() : 1)).orElse(0);
            })
            .sum();

        var initialStatus = total <= 0 ? OrderStatus.paid : OrderStatus.pending;
        var order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus(initialStatus);
        order.setPaymentMethod(paymentMethod);
        order.setPlacedAt(LocalDateTime.now());
        if (initialStatus == OrderStatus.paid) {
            order.setPaidAt(LocalDateTime.now());
        }
        var savedOrderId = order.getId();
        order = orderRepository.save(order);

        for (var item : items) {
            var course = courseRepository.findById(item.getCourseId());
            var oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setCourseId(item.getCourseId());
            oi.setQuantity(item.getQuantity() != null ? item.getQuantity() : 1);
            oi.setPrice(course.map(c -> c.getPrice() != null ? c.getPrice() : 0).orElse(0));
            orderItemRepository.save(oi);
        }

        cartRepository.deleteByUserId(userId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (Long courseId : courseIds) {
                    enrollmentService.enroll(userId, courseId, savedOrderId);
                }
            }
        });

        return finalizeCheckout(order, customerData);
    }

    @Transactional
    public Map<String, Object> createBuyNow(Long userId, Long courseId, int quantity, PaymentMethod paymentMethod, CheckoutCustomerData customerData) {
        var course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        var total = (course.getPrice() != null ? course.getPrice() : 0) * quantity;
        var initialStatus = total <= 0 ? OrderStatus.paid : OrderStatus.pending;

        var order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus(initialStatus);
        order.setPaymentMethod(paymentMethod);
        order.setPlacedAt(LocalDateTime.now());
        if (initialStatus == OrderStatus.paid) {
            order.setPaidAt(LocalDateTime.now());
        }
        var savedOrderId = order.getId();
        order = orderRepository.save(order);

        var oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setCourseId(courseId);
        oi.setQuantity(quantity);
        oi.setPrice(course.getPrice() != null ? course.getPrice() : 0);
        orderItemRepository.save(oi);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                enrollmentService.enroll(userId, courseId, savedOrderId);
            }
        });

        return finalizeCheckout(order, customerData);
    }

    private Map<String, Object> finalizeCheckout(Order order, CheckoutCustomerData customerData) {
        var strategy = paymentStrategyFactory.make(order.getPaymentMethod());
        var customerMap = new LinkedHashMap<String, Object>();
        customerMap.put("buyer_name", customerData.getFullName());
        customerMap.put("buyer_email", customerData.getEmail());
        customerMap.put("buyer_phone", customerData.getPhone());
        customerMap.put("fullName", customerData.getFullName());
        customerMap.put("email", customerData.getEmail());
        customerMap.put("phone", customerData.getPhone());
        var result = strategy.initialize(order, customerMap);
        return new CheckoutOrderResponse(order, result.toCheckoutAction()).toArray();
    }

    @Transactional
    public Order createWithStarPayment(Long userId, Long courseId) {
        var course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.getAllowStarPayment() == null || !course.getAllowStarPayment()) {
            throw new RuntimeException("Course does not support star payment");
        }

        var order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(0);
        order.setStatus(OrderStatus.paid);
        order.setPaymentMethod(PaymentMethod.star);
        order.setPlacedAt(LocalDateTime.now());
        order.setPaidAt(LocalDateTime.now());
        var savedOrderId = order.getId();
        order = orderRepository.save(order);

        var oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setCourseId(courseId);
        oi.setQuantity(1);
        oi.setPrice(0);
        orderItemRepository.save(oi);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                enrollmentService.enroll(userId, courseId, savedOrderId);
            }
        });

        return order;
    }

    @Transactional
    public boolean cancelByUserId(Long userId, Long orderId) {
        var order = orderRepository.findByIdAndUserId(orderId, userId);
        if (order.isEmpty()) return false;
        var o = order.get();
        var strategy = paymentStrategyFactory.make(o.getPaymentMethod());
        strategy.cancel(o, "Cancelled by user.");
        o.setStatus(OrderStatus.cancelled);
        orderRepository.save(o);
        return true;
    }
}
