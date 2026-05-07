package app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record TaskDto(@NotEmpty @NotBlank @Size(min = 3, max = 25) String title) {}
