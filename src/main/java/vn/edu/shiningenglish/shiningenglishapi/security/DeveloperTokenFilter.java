package vn.edu.shiningenglish.shiningenglishapi.security;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.shiningenglish.shiningenglishapi.repository.DeveloperRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.PersonalAccessTokenRepository;

@Component
public class DeveloperTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DeveloperTokenFilter.class);
    private final PersonalAccessTokenRepository tokenRepository;
    private final DeveloperRepository developerRepository;

    public DeveloperTokenFilter(PersonalAccessTokenRepository tokenRepository, DeveloperRepository developerRepository) {
        this.tokenRepository = tokenRepository;
        this.developerRepository = developerRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Don't overwrite existing user authentication
        var existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.getPrincipal() instanceof vn.edu.shiningenglish.shiningenglishapi.model.entity.User) {
            chain.doFilter(request, response);
            return;
        }

        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

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
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Developer token authentication failed: {}", e.getMessage());
        }
        chain.doFilter(request, response);
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
