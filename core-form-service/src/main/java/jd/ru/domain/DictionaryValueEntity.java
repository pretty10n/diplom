package jd.ru.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dictionary_value")
public class DictionaryValueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DictionaryType dictionaryType;

    @Column(nullable = false, length = 128)
    private String code;

    @Column(nullable = false, length = 512)
    private String label;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private Boolean active;

    public Long getId() {
        return id;
    }

    public DictionaryType getDictionaryType() {
        return dictionaryType;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public Integer getVersion() {
        return version;
    }

    public Boolean getActive() {
        return active;
    }
}
