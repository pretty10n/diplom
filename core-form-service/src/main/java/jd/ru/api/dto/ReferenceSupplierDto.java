package jd.ru.api.dto;

import java.util.UUID;

public record ReferenceSupplierDto(
        UUID id,
        String name,
        String inn
) {
}
