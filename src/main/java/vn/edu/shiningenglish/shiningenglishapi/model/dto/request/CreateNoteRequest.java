package vn.edu.shiningenglish.shiningenglishapi.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateNoteRequest(
    @NotBlank String content
) {}
