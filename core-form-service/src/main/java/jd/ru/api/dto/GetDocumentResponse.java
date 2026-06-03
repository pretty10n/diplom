package jd.ru.api.dto;

import java.util.UUID;

public record GetDocumentResponse(
        UUID id,
        String status,
        CommonInfo common,
        int entriesCount
) {
    public record CommonInfo(
            String truName,
            String truCode,
            String stage,
            Integer reportYear,
            Integer planYear
    ) {
    }
}
