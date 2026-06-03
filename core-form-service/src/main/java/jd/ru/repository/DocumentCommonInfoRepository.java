package jd.ru.repository;

import jd.ru.domain.DocumentCommonInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentCommonInfoRepository extends JpaRepository<DocumentCommonInfoEntity, UUID> {
}
