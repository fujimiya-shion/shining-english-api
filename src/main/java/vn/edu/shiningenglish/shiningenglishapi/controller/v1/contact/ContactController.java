package vn.edu.shiningenglish.shiningenglishapi.controller.v1.contact;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Contact;
import vn.edu.shiningenglish.shiningenglishapi.repository.ContactRepository;
import vn.edu.shiningenglish.shiningenglishapi.security.RecaptchaVerifier;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactController extends BaseController {

    private final ContactRepository contactRepository;
    private final RecaptchaVerifier recaptchaVerifier;

    public ContactController(ContactRepository contactRepository, RecaptchaVerifier recaptchaVerifier) {
        this.contactRepository = contactRepository;
        this.recaptchaVerifier = recaptchaVerifier;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> store(@RequestBody Map<String, Object> body) {
        try {
            recaptchaVerifier.verifyOrFail(
                (String) body.get("recaptcha_token"),
                "contact",
                (String) body.getOrDefault("ip_address", null)
            );
        } catch (IllegalArgumentException e) {
            return error(e.getMessage(), 422);
        }

        var contact = new Contact();
        contact.setName((String) body.get("name"));
        contact.setEmail((String) body.get("email"));
        contact.setMessage((String) body.get("message"));
        contact.setIpAddress((String) body.getOrDefault("ip_address", null));
        contact.setUserAgent((String) body.getOrDefault("user_agent", null));
        contactRepository.save(contact);

        return created(null, "Contact submitted successfully.");
    }
}
