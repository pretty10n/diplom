package jd.ru.api.dto;

public record DeleteEntryResponse(
        boolean deleted,
        boolean renumbered
) {
}
