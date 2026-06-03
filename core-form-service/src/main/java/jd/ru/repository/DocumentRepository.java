package jd.ru.repository;

import jd.ru.domain.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {
}
