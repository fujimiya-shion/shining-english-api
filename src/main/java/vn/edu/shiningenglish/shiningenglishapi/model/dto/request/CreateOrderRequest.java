package vn.edu.shiningenglish.shiningenglishapi.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
    @NotBlank String type,
    String paymentMethod,
    Long courseId,
    Integer quantity,
    String buyerName,
    String buyerEmail,
    String buyerPhone
) {}
