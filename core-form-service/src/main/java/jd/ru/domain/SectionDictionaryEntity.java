package jd.ru.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dictionary_section_key")
public class SectionDictionaryEntity {

    @Id
    @Column(length = 64)
    private String key;

    @Column(nullable = false, length = 512)
    private String label;

    @Column(nullable = false)
    private Integer formNo;

    @Column(nullable = false)
    private Integer sectionNo;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private Boolean active;

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public Integer getFormNo() {
        return formNo;
    }

    public Integer getSectionNo() {
        return sectionNo;
    }

    public Integer getVersion() {
        return version;
    }

    public Boolean getActive() {
        return active;
    }
}
