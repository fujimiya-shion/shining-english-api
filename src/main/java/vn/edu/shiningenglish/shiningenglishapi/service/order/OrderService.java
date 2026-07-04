package vn.edu.shiningenglish.shiningenglishapi.service.order;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.shiningenglish.shiningenglishapi.enums.OrderStatus;
import vn.edu.shiningenglish.shiningenglishapi.enums.PaymentMethod;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Order;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.OrderItem;
import vn.edu.shiningenglish.shiningenglishapi.repository.*;
import vn.edu.shiningenglish.shiningenglishapi.repository.order.*;
import vn.edu.shiningenglish.shiningenglishapi.repository.cart.*;
import vn.edu.shiningenglish.shiningenglishapi.repository.course.*;
import vn.edu.shiningenglish.shiningenglishapi.service.enrollment.EnrollmentService;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.CheckoutCustomerData;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.QueryOption;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartRepository cartRepository, CourseRepository courseRepository,
                        EnrollmentService enrollmentService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.courseRepository = courseRepository;
        this.enrollmentService = enrollmentService;
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
        return orderRepository.findByIdAndUserId(orderId, userId);
    }

    @Transactional
    public Map<String, Object> createFromCart(Long userId, PaymentMethod paymentMethod, CheckoutCustomerData customerData) {
        var items = cartRepository.findByUserId(userId);
        if (items.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        var total = items.stream()
            .mapToInt(item -> {
                var course = courseRepository.findById(item.getCourseId());
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
        order = orderRepository.save(order);

        for (var item : items) {
            var course = courseRepository.findById(item.getCourseId());
            var oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setCourseId(item.getCourseId());
            oi.setQuantity(item.getQuantity() != null ? item.getQuantity() : 1);
            oi.setPrice(course.map(c -> c.getPrice() != null ? c.getPrice() : 0).orElse(0));
            orderItemRepository.save(oi);

            enrollmentService.enroll(userId, item.getCourseId(), order.getId());
        }

        cartRepository.deleteByUserId(userId);

        var result = new LinkedHashMap<String, Object>();
        result.put("order", order);
        result.put("payment_action", Map.of("method", paymentMethod.name(), "url", ""));
        return result;
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
        order = orderRepository.save(order);

        var oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setCourseId(courseId);
        oi.setQuantity(quantity);
        oi.setPrice(course.getPrice() != null ? course.getPrice() : 0);
        orderItemRepository.save(oi);

        enrollmentService.enroll(userId, courseId, order.getId());

        var result = new LinkedHashMap<String, Object>();
        result.put("order", order);
        result.put("payment_action", Map.of("method", paymentMethod.name(), "url", ""));
        return result;
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
        order = orderRepository.save(order);

        var oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setCourseId(courseId);
        oi.setQuantity(1);
        oi.setPrice(0);
        orderItemRepository.save(oi);

        enrollmentService.enroll(userId, courseId, order.getId());
        return order;
    }

    @Transactional
    public boolean cancelByUserId(Long userId, Long orderId) {
        var order = orderRepository.findByIdAndUserId(orderId, userId);
        if (order.isEmpty()) return false;
        var o = order.get();
        o.setStatus(OrderStatus.cancelled);
        orderRepository.save(o);
        return true;
    }
}
