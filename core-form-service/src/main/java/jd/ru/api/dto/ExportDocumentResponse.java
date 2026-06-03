package jd.ru.api.dto;

import java.time.Instant;
import java.util.List;

public record ExportDocumentResponse(
        String downloadUrl,
        Instant expiresAt,
        ExportMeta exportMeta
) {
    public record ExportMeta(
            boolean templatePreserved,
            List<String> updatedSheets
    ) {
    }
}
