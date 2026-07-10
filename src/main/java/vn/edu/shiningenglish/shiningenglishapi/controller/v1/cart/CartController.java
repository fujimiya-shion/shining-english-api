package vn.edu.shiningenglish.shiningenglishapi.controller.v1.cart;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.dto.request.AddToCartRequest;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.service.cart.CartService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController extends BaseController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> items(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return success(cartService.itemsByUserId(user.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> store(Authentication auth, @Valid @RequestBody AddToCartRequest request) {
        var user = (User) auth.getPrincipal();
        try {
            cartService.addCourse(user.getId(), request.courseId(),
                request.quantity() != null ? request.quantity() : 1);
        } catch (RuntimeException e) {
            return error(e.getMessage(), 422);
        }
        var data = Map.of("course_id", request.courseId(), "enrolled", false, "pending_access", false, "in_cart", true);
        return created(data, "Course added to cart");
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> count(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return success(cartService.countByUserId(user.getId()));
    }

    @DeleteMapping("/items/{courseId}")
    public ResponseEntity<Map<String, Object>> removeItem(Authentication auth, @PathVariable Long courseId) {
        var user = (User) auth.getPrincipal();
        var removed = cartService.removeByCourseId(user.getId(), courseId);
        if (!removed) return notfound("Cart item not found");
        return deleted("Course removed from cart");
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clear(Authentication auth) {
        var user = (User) auth.getPrincipal();
        cartService.clearByUserId(user.getId());
        return deleted("Cart cleared");
    }
}
