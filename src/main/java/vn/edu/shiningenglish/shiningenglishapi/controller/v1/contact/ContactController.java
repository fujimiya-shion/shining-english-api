package vn.edu.shiningenglish.shiningenglishapi.controller.v1.contact;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.shiningenglish.shiningenglishapi.common.BaseController;
import vn.edu.shiningenglish.shiningenglishapi.model.dto.request.ContactRequest;
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
    public ResponseEntity<Map<String, Object>> store(@Valid @RequestBody ContactRequest request) {
        recaptchaVerifier.verifyOrFail(
            request.recaptchaToken(),
            "contact",
            request.ipAddress()
        );

        var contact = new Contact();
        contact.setName(request.name());
        contact.setEmail(request.email());
        contact.setMessage(request.message());
        contact.setIpAddress(request.ipAddress());
        contact.setUserAgent(request.userAgent());
        contactRepository.save(contact);

        return created(null, "Contact submitted successfully.");
    }
}
