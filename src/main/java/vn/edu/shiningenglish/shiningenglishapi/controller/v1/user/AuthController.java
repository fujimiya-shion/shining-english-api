package vn.edu.shiningenglish.shiningenglishapi.controller.v1.user;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.enums.AuthenticatedBy;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.security.RecaptchaVerifier;
import vn.edu.shiningenglish.shiningenglishapi.service.star.StarService;
import vn.edu.shiningenglish.shiningenglishapi.service.user.UserService;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.DeviceInfo;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {

    private final UserService userService;
    private final RecaptchaVerifier recaptchaVerifier;
    private final StarService starService;

    public AuthController(UserService userService, RecaptchaVerifier recaptchaVerifier, StarService starService) {
        this.userService = userService;
        this.recaptchaVerifier = recaptchaVerifier;
        this.starService = starService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> body) {
        try {
            recaptchaVerifier.verifyOrFail(
                (String) body.get("recaptcha_token"),
                "register",
                (String) body.getOrDefault("ip_address", null)
            );
            var result = userService.register(
                (String) body.get("name"),
                (String) body.get("email"),
                (String) body.get("phone"),
                (String) body.get("password")
            );
            return created(result.toArray(), "Register successfully");
        } catch (IllegalArgumentException e) {
            return error(e.getMessage(), 422);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            var deviceData = (Map<String, Object>) body;
            var device = DeviceInfo.fromArray((Map<String, String>)(Map) body);
            var result = userService.login(
                (String) body.get("email"),
                (String) body.get("password"),
                device
            );
            return success("Login successfully", result.toArray());
        } catch (IllegalArgumentException e) {
            return error(e.getMessage(), 422);
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/third-party-login")
    public ResponseEntity<Map<String, Object>> thirdPartyLogin(@RequestBody Map<String, Object> body) {
        try {
            var device = DeviceInfo.fromArray((Map<String, String>)(Map) body);
            var provider = (String) body.get("provider");
            var accessToken = (String) body.get("access_token");
            var providerEnum = AuthenticatedBy.valueOf(provider);

            // Verify with Google
            String email;
            String name;
            String avatar = null;
            if ("google".equals(provider) && accessToken != null) {
                var restTemplate = new RestTemplate();
                var url = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken;
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                var googleData = (Map<String, Object>) response.getBody();
                email = (String) googleData.get("email");
                name = (String) googleData.getOrDefault("name",
                    (String) googleData.getOrDefault("given_name", email));
                avatar = (String) googleData.getOrDefault("picture", null);
            } else {
                email = (String) body.get("email");
                name = (String) body.getOrDefault("name", "User");
            }

            // Check if user already exists
            var existingUser = userService.findByEmail(email);
            if (existingUser.isEmpty()) {
                // Register new third-party user
                var registerResult = userService.register(name, email, null, null, providerEnum);
                if (!registerResult.isSuccessfully()) {
                    return error("Third party authentication failed", 422);
                }
                var createdUser = registerResult.getUser();
                createdUser.setEmailVerifiedAt(java.time.LocalDateTime.now());
                createdUser.setAvatar(avatar);
                userService.save(createdUser);
            } else {
                // Update avatar if provided
                if (avatar != null && existingUser.get().getAvatar() == null) {
                    existingUser.get().setAvatar(avatar);
                    userService.save(existingUser.get());
                }
            }

            var loginResult = userService.login(email, null, device);
            return success("Login by third-party successfully", loginResult.toArray());
        } catch (Exception e) {
            return error(e.getMessage(), 422);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return unauthorized("Unauthenticated");
        }
        var data = new java.util.LinkedHashMap<String, Object>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        data.put("city_id", user.getCityId());
        data.put("star_balance", starService.getBalance(user.getId()));
        return success(data);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("User-Authorization") String token) {
        var loggedOut = userService.logoutByToken(token);
        if (!loggedOut) return unauthorized("Unauthenticated");
        return success("Logged out");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, Object> body) {
        userService.sendPasswordResetLink((String) body.get("email"));
        return success("If your email exists, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, Object> body) {
        var reset = userService.resetPassword(
            (String) body.get("email"),
            (String) body.get("token"),
            (String) body.get("password")
        );
        if (!reset) return error("Invalid or expired reset token.", 422);
        return success("Password reset successfully.");
    }
}
