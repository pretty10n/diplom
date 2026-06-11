package jd.ru.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertReferenceMaterialRequest(
        @NotBlank @Size(max = 512) String name,
        @Size(max = 128) String okpdCode,
        @Size(max = 128) String ekpsCode,
        @Size(max = 128) String fnn
) {
}
