package vn.edu.shiningenglish.shiningenglishapi.controller.v1.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.service.user.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(Authentication auth, @RequestBody Map<String, Object> body) {
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return unauthorized("Unauthenticated");
        }
        try {
            var updated = userService.updateProfile(user, body);
            return success("Updated", updated);
        } catch (Exception e) {
            return error("Update failed", 422);
        }
    }
}
