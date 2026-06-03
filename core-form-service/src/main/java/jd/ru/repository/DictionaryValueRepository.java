package jd.ru.repository;

import jd.ru.domain.DictionaryType;
import jd.ru.domain.DictionaryValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DictionaryValueRepository extends JpaRepository<DictionaryValueEntity, Long> {
    List<DictionaryValueEntity> findByDictionaryTypeAndActiveTrueOrderByCodeAsc(DictionaryType dictionaryType);
}
