package jd.ru.api.dto;

import java.util.List;

public record DictionariesResponse(
        List<SectionKeyItem> sectionKeys,
        List<DictionaryItem> col13_2Values,
        List<DictionaryItem> col5_2Values
) {
    public record SectionKeyItem(
            String key,
            String label,
            Integer formNo,
            Integer sectionNo
    ) {
    }

    public record DictionaryItem(
            String code,
            String label
    ) {
    }
}
