package jd.ru.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ExportDocumentRequest(
        @NotBlank String format,
        @NotBlank String mode,
        String templateFileId,
        String fileName
) {
}
