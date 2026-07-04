package vn.edu.shiningenglish.shiningenglishapi.model.dto.user.auth;

import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;

import java.util.LinkedHashMap;
import java.util.Map;

public class RegisterResponse {
    private final User user;

    public RegisterResponse(User user) {
        this.user = user;
    }

    public boolean isSuccessfully() {
        return user != null;
    }

    public Map<String, Object> toArray() {
        var map = new LinkedHashMap<String, Object>();
        map.put("user", user);
        map.put("email_verification_sent", true);
        return map;
    }

    public User getUser() { return user; }
}
