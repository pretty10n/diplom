package jd.ru.domain;

import java.util.Set;

public final class SectionKeys {

    public static final Set<String> EXCLUDED = Set.of("return_waste_f4", "return_waste_f5");

    private SectionKeys() {
    }

    public static boolean isExcluded(String sectionKey) {
        return sectionKey != null && EXCLUDED.contains(sectionKey);
    }
}
