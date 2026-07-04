package vn.edu.shiningenglish.shiningenglishapi.controller.v1.developer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.PersonalAccessToken;
import vn.edu.shiningenglish.shiningenglishapi.repository.PersonalAccessTokenRepository;
import vn.edu.shiningenglish.shiningenglishapi.service.developer.DeveloperService;

@RestController
@RequestMapping("/api/v1")
public class DeveloperController extends BaseController {

    private final DeveloperService developerService;
    private final PersonalAccessTokenRepository tokenRepository;

    public DeveloperController(DeveloperService developerService, PersonalAccessTokenRepository tokenRepository) {
        this.developerService = developerService;
        this.tokenRepository = tokenRepository;
    }

    @PostMapping("/access-token")
    public ResponseEntity<Map<String, Object>> accessToken(@RequestBody Map<String, Object> body) {
        var developer = developerService.login((String) body.get("email"), (String) body.get("password"));
        if (developer.isEmpty()) return unauthorized("Unauthorized");

        var plainToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        var hashedToken = sha256(plainToken);

        var tokenRecord = new PersonalAccessToken();
        tokenRecord.setTokenableType("App\\Models\\Developer");
        tokenRecord.setTokenableId(developer.get().getId());
        tokenRecord.setName("developer_access_token");
        tokenRecord.setToken(hashedToken);
        tokenRepository.save(tokenRecord);

        return success(Map.of("access_token", plainToken));
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
