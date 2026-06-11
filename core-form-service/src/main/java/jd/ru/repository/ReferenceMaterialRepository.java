package jd.ru.repository;

import jd.ru.domain.ReferenceMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReferenceMaterialRepository extends JpaRepository<ReferenceMaterialEntity, UUID> {

    @Query(value = """
            select *
            from reference_material
            where lower(name) like lower(concat('%', :query, '%'))
            order by name
            limit :limit
            """, nativeQuery = true)
    List<ReferenceMaterialEntity> search(@Param("query") String query, @Param("limit") int limit);

    @Query(value = """
            select *
            from reference_material
            where lower(trim(name)) = lower(trim(:name))
            limit 1
            """, nativeQuery = true)
    Optional<ReferenceMaterialEntity> findByNormalizedName(@Param("name") String name);

    List<ReferenceMaterialEntity> findAllByOrderByNameAsc();
}
