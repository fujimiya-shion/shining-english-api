package vn.edu.shiningenglish.shiningenglishapi.model.entity;

import jakarta.persistence.*;
import vn.edu.shiningenglish.shiningenglishapi.enums.AuthenticatedBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private LocalDate birthday;

    private String avatar;

    @Column(name = "city_id")
    private Long cityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "authenticated_by", length = 50)
    private AuthenticatedBy authenticatedBy = AuthenticatedBy.local;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    private String password;

    @Column(name = "remember_token", length = 100)
    private String rememberToken;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public AuthenticatedBy getAuthenticatedBy() { return authenticatedBy; }
    public void setAuthenticatedBy(AuthenticatedBy authenticatedBy) { this.authenticatedBy = authenticatedBy; }
    public LocalDateTime getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRememberToken() { return rememberToken; }
    public void setRememberToken(String rememberToken) { this.rememberToken = rememberToken; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
