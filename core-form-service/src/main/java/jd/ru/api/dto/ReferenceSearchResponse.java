package jd.ru.api.dto;

import java.util.List;

public record ReferenceSearchResponse<T>(
        List<T> items
) {
}
