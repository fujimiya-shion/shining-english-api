package vn.edu.shiningenglish.shiningenglishapi.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank String phone,
    @NotBlank String password,
    String recaptchaToken,
    String ipAddress
) {}
