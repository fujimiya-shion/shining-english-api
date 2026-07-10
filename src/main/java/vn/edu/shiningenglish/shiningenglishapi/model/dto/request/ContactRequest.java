package vn.edu.shiningenglish.shiningenglishapi.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank String message,
    String recaptchaToken,
    String ipAddress,
    String userAgent
) {}
