package jd.ru.api.dto;

import java.util.UUID;

public record ReferenceMaterialDto(
        UUID id,
        String name,
        String okpdCode,
        String ekpsCode,
        String fnn
) {
}
