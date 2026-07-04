package vn.edu.shiningenglish.shiningenglishapi.service.developer;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.shiningenglish.shiningenglishapi.model.entity.Developer;
import vn.edu.shiningenglish.shiningenglishapi.repository.DeveloperRepository;

import java.util.Optional;

@Service
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;

    public DeveloperService(DeveloperRepository developerRepository, PasswordEncoder passwordEncoder) {
        this.developerRepository = developerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Developer> login(String email, String password) {
        var developer = developerRepository.findByEmail(email);
        if (developer.isPresent() && passwordEncoder.matches(password, developer.get().getPassword())) {
            return developer;
        }
        return Optional.empty();
    }
}
