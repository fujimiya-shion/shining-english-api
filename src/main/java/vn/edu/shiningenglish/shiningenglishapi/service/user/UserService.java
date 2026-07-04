package vn.edu.shiningenglish.shiningenglishapi.service.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.shiningenglish.shiningenglishapi.enums.AuthenticatedBy;
import vn.edu.shiningenglish.shiningenglishapi.event.SendPasswordResetEvent;
import vn.edu.shiningenglish.shiningenglishapi.event.UserRegisteredEvent;
import vn.edu.shiningenglish.shiningenglishapi.model.dto.user.auth.LoginResponse;
import vn.edu.shiningenglish.shiningenglishapi.model.dto.user.auth.RegisterResponse;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.PasswordResetToken;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.PersonalAccessToken;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.UserDevice;
import vn.edu.shiningenglish.shiningenglishapi.repository.*;
import vn.edu.shiningenglish.shiningenglishapi.repository.user.*;
import vn.edu.shiningenglish.shiningenglishapi.valueobject.DeviceInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PersonalAccessTokenRepository tokenRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository,
                       PersonalAccessTokenRepository tokenRepository,
                       UserDeviceRepository userDeviceRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.userDeviceRepository = userDeviceRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RegisterResponse register(String name, String email, String phone, String password) {
        return register(name, email, phone, password, AuthenticatedBy.local);
    }

    @Transactional
    public RegisterResponse register(String name, String email, String phone, String password, AuthenticatedBy authenticatedBy) {
        log.info("User registration started: email={}", email);
        var payload = new User();
        payload.setName(name);
        payload.setEmail(email);
        payload.setPhone(phone);
        if (password != null && !password.isBlank()) {
            payload.setPassword(passwordEncoder.encode(password));
        }
        if (authenticatedBy != AuthenticatedBy.local) {
            payload.setAuthenticatedBy(authenticatedBy);
        }

        var created = userRepository.save(payload);
        eventPublisher.publishEvent(new UserRegisteredEvent(created.getId(), created.getEmail(), created.getName(), authenticatedBy));

        log.info("User registered successfully: id={}, email={}", created.getId(), created.getEmail());
        return new RegisterResponse(created);
    }

    @Transactional
    public LoginResponse login(String email, String password, DeviceInfo device) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (password != null && (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword()))) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (user.getEmailVerifiedAt() == null) {
            throw new IllegalArgumentException("Email is not verified.");
        }

        var plainToken = java.util.UUID.randomUUID().toString().replace("-", "") 
            + java.util.UUID.randomUUID().toString().replace("-", "");
        var hashedToken = sha256(plainToken);

        var tokenRecord = new PersonalAccessToken();
        tokenRecord.setTokenableType("App\\Models\\User");
        tokenRecord.setTokenableId(user.getId());
        tokenRecord.setName("user_auth_token");
        tokenRecord.setToken(hashedToken);
        tokenRepository.save(tokenRecord);

        createUserDevice(user.getId(), tokenRecord.getId(), device);

        return new LoginResponse(plainToken, user);
    }

    @Transactional
    public boolean logoutByToken(String token) {
        var hashedToken = sha256(token);
        var optToken = tokenRepository.findByToken(hashedToken);
        if (optToken.isEmpty()) return false;

        var accessToken = optToken.get();
        // Mark device logged out
        userDeviceRepository.findAll().stream()
            .filter(d -> d.getPersonalAccessTokenId() != null && d.getPersonalAccessTokenId().equals(accessToken.getId()))
            .findFirst().ifPresent(d -> {
                d.setLoggedOutAt(LocalDateTime.now());
                userDeviceRepository.save(d);
            });

        tokenRepository.delete(accessToken);
        return true;
    }

    @Transactional
    public void sendPasswordResetLink(String email) {
        var optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }
        var resetToken = java.util.UUID.randomUUID().toString().replace("-", "");
        var user = optUser.get();

        var entity = new PasswordResetToken();
        entity.setEmail(user.getEmail());
        entity.setToken(resetToken);
        entity.setCreatedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(entity);

        eventPublisher.publishEvent(new SendPasswordResetEvent(user.getEmail(), resetToken));
    }

    @Transactional
    public boolean resetPassword(String email, String token, String password) {
        var optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) return false;

        var optReset = passwordResetTokenRepository.findByEmail(email);
        if (optReset.isEmpty()) return false;

        var resetRecord = optReset.get();
        if (!resetRecord.getToken().equals(token)) return false;

        // Check token not expired (24 hours)
        if (resetRecord.getCreatedAt() != null &&
            resetRecord.getCreatedAt().plusHours(24).isBefore(LocalDateTime.now())) {
            return false;
        }

        var user = optUser.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setRememberToken(java.util.UUID.randomUUID().toString());
        userRepository.save(user);

        // Delete all tokens for this user (like Laravel's $user->tokens()->delete())
        tokenRepository.findAll().stream()
            .filter(t -> t.getTokenableId().equals(user.getId())
                && "App\\Models\\User".equals(t.getTokenableType()))
            .forEach(tokenRepository::delete);

        passwordResetTokenRepository.delete(resetRecord);

        return true;
    }

    @Transactional
    public User updateProfile(User user, java.util.Map<String, Object> data) {
        if (data.containsKey("name")) user.setName((String) data.get("name"));
        if (data.containsKey("nickname")) user.setNickname((String) data.get("nickname"));
        if (data.containsKey("phone")) user.setPhone((String) data.get("phone"));
        if (data.containsKey("birthday")) user.setBirthday(java.time.LocalDate.parse((String) data.get("birthday")));
        if (data.containsKey("city_id")) user.setCityId(data.get("city_id") instanceof Number n ? n.longValue() : Long.parseLong(data.get("city_id").toString()));
        if (data.containsKey("avatar")) user.setAvatar((String) data.get("avatar"));
        return userRepository.save(user);
    }

    public java.util.Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    private void createUserDevice(Long userId, Long tokenId, DeviceInfo device) {
        var ud = new UserDevice();
        ud.setUserId(userId);
        ud.setPersonalAccessTokenId(tokenId);
        ud.setDeviceIdentifier(device.getIdentifier());
        ud.setDeviceName(device.getName());
        ud.setPlatform(device.getPlatform());
        ud.setIpAddress(device.getIpAddress());
        ud.setUserAgent(device.getUserAgent());
        ud.setLoggedInAt(LocalDateTime.now());
        ud.setLastSeenAt(LocalDateTime.now());
        userDeviceRepository.save(ud);
    }

    private String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest((value + (char) 0).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
