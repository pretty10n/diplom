package jd.ru.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateDocumentResponse(
        UUID id,
        String status,
        OffsetDateTime createdAt
) {
}
