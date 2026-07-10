package vn.edu.shiningenglish.shiningenglishapi.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddToCartRequest(
    @NotNull @Positive Long courseId,
    @Positive Integer quantity
) {}
