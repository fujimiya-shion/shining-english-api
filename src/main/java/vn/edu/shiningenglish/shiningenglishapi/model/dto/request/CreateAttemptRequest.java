package vn.edu.shiningenglish.shiningenglishapi.model.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateAttemptRequest(
    @NotNull Double scorePercent,
    @NotNull Boolean passed,
    LocalDateTime submittedAt
) {}
