package jd.ru.repository;

import jd.ru.domain.DocumentTotalsSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentTotalsSnapshotRepository extends JpaRepository<DocumentTotalsSnapshotEntity, UUID> {
}
