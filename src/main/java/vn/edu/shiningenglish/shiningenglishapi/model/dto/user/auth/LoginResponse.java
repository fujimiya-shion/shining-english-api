package vn.edu.shiningenglish.shiningenglishapi.model.dto.user.auth;

import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;

import java.util.LinkedHashMap;
import java.util.Map;

public class LoginResponse {
    private final String token;
    private final User user;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public Map<String, Object> toArray() {
        var map = new LinkedHashMap<String, Object>();
        map.put("token", token);
        map.put("user", user);
        return map;
    }

    public String getToken() { return token; }
    public User getUser() { return user; }
}
