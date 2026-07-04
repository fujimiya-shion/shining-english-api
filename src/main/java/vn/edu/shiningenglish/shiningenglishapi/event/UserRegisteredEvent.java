package vn.edu.shiningenglish.shiningenglishapi.event;

import vn.edu.shiningenglish.shiningenglishapi.enums.AuthenticatedBy;

public class UserRegisteredEvent {
    private final Long userId;
    private final String email;
    private final String name;
    private final AuthenticatedBy authenticatedBy;

    public UserRegisteredEvent(Long userId, String email, String name, AuthenticatedBy authenticatedBy) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.authenticatedBy = authenticatedBy;
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public AuthenticatedBy getAuthenticatedBy() { return authenticatedBy; }
}
