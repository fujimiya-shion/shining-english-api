package vn.edu.shiningenglish.shiningenglishapi.service.cart;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Cart;
import vn.edu.shiningenglish.shiningenglishapi.repository.cart.CartRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.course.CourseRepository;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CourseRepository courseRepository;

    public CartService(CartRepository cartRepository, CourseRepository courseRepository) {
        this.cartRepository = cartRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Cart addCourse(Long userId, Long courseId, int quantity) {
        var course = courseRepository.findById(courseId);
        if (course.isEmpty()) {
            throw new RuntimeException("Course not found");
        }
        var existing = cartRepository.findByUserIdAndCourseId(userId, courseId);
        if (existing.isPresent()) {
            var cart = existing.get();
            cart.setQuantity(cart.getQuantity() + quantity);
            return cartRepository.save(cart);
        }
        var cart = new Cart();
        cart.setUserId(userId);
        cart.setCourseId(courseId);
        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    public List<Cart> itemsByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public java.util.Map<String, Object> countByUserId(Long userId) {
        var items = cartRepository.findByUserId(userId);
        var itemCount = items.size();
        var totalQty = items.stream().mapToInt(c -> c.getQuantity() != null ? c.getQuantity() : 0).sum();
        return java.util.Map.of("items", itemCount, "quantity", totalQty);
    }

    @Transactional
    public void clearByUserId(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    public boolean hasCourse(Long userId, Long courseId) {
        return cartRepository.existsByUserIdAndCourseId(userId, courseId);
    }
}
