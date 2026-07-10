package vn.edu.shiningenglish.shiningenglishapi.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ThirdPartyLoginRequest(
    @NotBlank String provider,
    String accessToken,
    String email,
    String name,
    @NotBlank String deviceIdentifier,
    String deviceName,
    String platform,
    String ipAddress,
    String userAgent
) {}
