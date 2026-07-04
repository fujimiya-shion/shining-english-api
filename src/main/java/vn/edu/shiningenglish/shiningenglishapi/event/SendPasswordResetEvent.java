package vn.edu.shiningenglish.shiningenglishapi.event;

public class SendPasswordResetEvent {
    private final String email;
    private final String token;

    public SendPasswordResetEvent(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public String getEmail() { return email; }
    public String getToken() { return token; }
}
