package vn.edu.shiningenglish.shiningenglishapi.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String email,
    @NotBlank String password,
    @NotBlank String deviceIdentifier,
    String deviceName,
    String platform,
    String ipAddress,
    String userAgent
) {}
