package vn.edu.shiningenglish.shiningenglishapi.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "personal_access_tokens")
public class PersonalAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tokenable_type", nullable = false)
    private String tokenableType;

    @Column(name = "tokenable_id", nullable = false)
    private Long tokenableId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(columnDefinition = "TEXT")
    private String abilities;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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
    public String getTokenableType() { return tokenableType; }
    public void setTokenableType(String tokenableType) { this.tokenableType = tokenableType; }
    public Long getTokenableId() { return tokenableId; }
    public void setTokenableId(Long tokenableId) { this.tokenableId = tokenableId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getAbilities() { return abilities; }
    public void setAbilities(String abilities) { this.abilities = abilities; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
