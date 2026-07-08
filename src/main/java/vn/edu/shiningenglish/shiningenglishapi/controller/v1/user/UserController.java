package vn.edu.shiningenglish.shiningenglishapi.controller.v1.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.service.user.UserService;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(Authentication auth, HttpServletRequest request) {
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return unauthorized("Unauthenticated");
        }
        try {
            var body = new LinkedHashMap<String, Object>();
            var contentType = request.getContentType();

            if (contentType != null && contentType.startsWith("multipart/")) {
                for (var entry : request.getParameterMap().entrySet()) {
                    body.put(entry.getKey(), entry.getValue().length == 1 ? entry.getValue()[0] : entry.getValue());
                }
            } else {
                body.putAll(new ObjectMapper().readValue(request.getInputStream(), Map.class));
            }

            var updated = userService.updateProfile(user, body);
            return success("Updated", updated);
        } catch (Exception e) {
            return error("Update failed", 422);
        }
    }
}
