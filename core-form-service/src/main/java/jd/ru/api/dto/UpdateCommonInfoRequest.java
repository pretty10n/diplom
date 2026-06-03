package jd.ru.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCommonInfoRequest(
        @NotBlank String truName,
        @NotBlank String stage,
        @NotNull @Min(1000) @Max(9999) Integer reportYear,
        @NotNull @Min(1000) @Max(9999) Integer planYear
) {
}
