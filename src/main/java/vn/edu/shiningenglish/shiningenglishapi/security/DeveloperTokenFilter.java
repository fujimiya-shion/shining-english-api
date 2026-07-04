package vn.edu.shiningenglish.shiningenglishapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Developer;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.User;
import vn.edu.shiningenglish.shiningenglishapi.repository.DeveloperRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.PersonalAccessTokenRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.user.UserRepository;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Component
public class DeveloperTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DeveloperTokenFilter.class);
    private final PersonalAccessTokenRepository tokenRepository;
    private final DeveloperRepository developerRepository;
    private final UserRepository userRepository;

    public DeveloperTokenFilter(PersonalAccessTokenRepository tokenRepository,
                                DeveloperRepository developerRepository,
                                UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.developerRepository = developerRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // CORS preflight - skip auth
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        var path = request.getRequestURI();

        // Public routes - skip auth check
        if (path.equals("/up") || path.startsWith("/api/v1/payments/") || path.equals("/api/v1/access-token")) {
            chain.doFilter(request, response);
            return;
        }

        // Non-API routes - skip
        if (!path.startsWith("/api/v1/")) {
            chain.doFilter(request, response);
            return;
        }

        // Don't overwrite existing user authentication
        var existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.getPrincipal() instanceof User) {
            chain.doFilter(request, response);
            return;
        }

        // Try developer token (Authorization: Bearer)
        var authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.isBlank()) {
            var tokenValue = authHeader.trim();
            if (tokenValue.startsWith("Bearer ")) {
                tokenValue = tokenValue.substring(7);
            }
            try {
                var hashedToken = sha256(tokenValue);
                var optToken = tokenRepository.findByToken(hashedToken);
                if (optToken.isPresent()) {
                    var token = optToken.get();
                    if ("developer_access_token".equals(token.getName())) {
                        var optDev = developerRepository.findById(token.getTokenableId());
                        if (optDev.isPresent()) {
                            var auth = new UsernamePasswordAuthenticationToken(
                                optDev.get(), null, java.util.Collections.emptyList());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            token.setLastUsedAt(LocalDateTime.now());
                            tokenRepository.save(token);
                            chain.doFilter(request, response);
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Developer token authentication failed: {}", e.getMessage());
            }
        }

        // Fallback: try user token (User-Authorization)
        var userTokenHeader = request.getHeader("User-Authorization");
        if (userTokenHeader != null && !userTokenHeader.isBlank()) {
            try {
                var hashedToken = sha256(userTokenHeader.trim());
                var optToken = tokenRepository.findByToken(hashedToken);
                if (optToken.isPresent()) {
                    var token = optToken.get();
                    if ("user_auth_token".equals(token.getName())) {
                        var optUser = userRepository.findById(token.getTokenableId());
                        if (optUser.isPresent()) {
                            var auth = new UsernamePasswordAuthenticationToken(
                                optUser.get(), null, java.util.Collections.emptyList());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            token.setLastUsedAt(LocalDateTime.now());
                            tokenRepository.save(token);
                            chain.doFilter(request, response);
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("User token authentication failed: {}", e.getMessage());
            }
        }

        // No valid token found → 401 Unauthorized
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"message\":\"Access Token is not set or invalid\",\"status\":false,\"status_code\":401}");
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
