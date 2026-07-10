package vn.edu.shiningenglish.shiningenglishapi.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AccessTokenRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}
