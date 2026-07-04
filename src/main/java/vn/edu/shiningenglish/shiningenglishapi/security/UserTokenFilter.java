package vn.edu.shiningenglish.shiningenglishapi.security;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

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
import vn.edu.shiningenglish.shiningenglishapi.repository.PersonalAccessTokenRepository;
import vn.edu.shiningenglish.shiningenglishapi.repository.user.UserRepository;

@Component
public class UserTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserTokenFilter.class);
    private final PersonalAccessTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public UserTokenFilter(PersonalAccessTokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var tokenHeader = request.getHeader("User-Authorization");
        if (tokenHeader != null && !tokenHeader.isBlank()) {
            try {
                var hashedToken = sha256(tokenHeader.trim());
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
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("User token authentication failed: {}", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }

    private String sha256(String value) throws NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance("SHA-256");
        var hash = digest.digest((value + (char) 0) .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
