package jd.ru.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpsertReferenceSupplierRequest(
        @NotBlank @Size(max = 512) String name,
        @Pattern(regexp = "^$|^[0-9]{10}([0-9]{2})?$", message = "ИНН: допустимы только 10 или 12 цифр") String inn
) {
}
