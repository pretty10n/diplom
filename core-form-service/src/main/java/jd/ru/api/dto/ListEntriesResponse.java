package jd.ru.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record ListEntriesResponse(
        List<Item> items,
        int total
) {
    public record Item(
            UUID entryId,
            String sectionKey,
            Integer formNo,
            Integer sectionNo,
            String rowNo,
            JsonNode fields,
            JsonNode computed,
            String validationStatus
    ) {
    }
}
