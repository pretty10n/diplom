package jd.ru.repository;

import jd.ru.domain.SectionDictionaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionDictionaryRepository extends JpaRepository<SectionDictionaryEntity, String> {
    Optional<SectionDictionaryEntity> findByKeyAndActiveTrue(String key);

    List<SectionDictionaryEntity> findByActiveTrueOrderByFormNoAscSectionNoAsc();
}
