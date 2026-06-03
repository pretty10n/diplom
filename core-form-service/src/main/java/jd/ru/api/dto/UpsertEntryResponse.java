package jd.ru.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record UpsertEntryResponse(
        UUID entryId,
        Integer formNo,
        Integer sectionNo,
        String rowNo,
        JsonNode computed
) {
}
