package jd.ru.repository;

import jd.ru.domain.DocumentEntryEntity;
import jd.ru.domain.ValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentEntryRepository extends JpaRepository<DocumentEntryEntity, UUID> {
    @Query("""
            select e from DocumentEntryEntity e
            where e.document.id = :documentId
              and e.section.key = :sectionKey
            order by e.rowNo asc
            """)
    List<DocumentEntryEntity> findByDocumentAndSectionOrdered(@Param("documentId") UUID documentId,
                                                              @Param("sectionKey") String sectionKey);

    @Query("""
            select max(e.rowNo) from DocumentEntryEntity e
            where e.document.id = :documentId
              and e.section.key = :sectionKey
            """)
    Optional<Integer> findMaxRowNo(@Param("documentId") UUID documentId, @Param("sectionKey") String sectionKey);

    @Query("""
            select e from DocumentEntryEntity e
            where e.document.id = :documentId
              and (:sectionKey is null or e.section.key = :sectionKey)
              and (:derivedFormNo is null or e.section.formNo = :derivedFormNo)
              and (:validationStatus is null or e.validationStatus = :validationStatus)
            order by e.section.formNo asc, e.section.sectionNo asc, e.rowNo asc
            """)
    List<DocumentEntryEntity> findForList(@Param("documentId") UUID documentId,
                                          @Param("sectionKey") String sectionKey,
                                          @Param("derivedFormNo") Integer derivedFormNo,
                                          @Param("validationStatus") ValidationStatus validationStatus);
}
