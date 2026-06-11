package jd.ru.repository;

import jd.ru.domain.ReferenceSupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReferenceSupplierRepository extends JpaRepository<ReferenceSupplierEntity, UUID> {

    @Query(value = """
            select *
            from reference_supplier
            where lower(name) like lower(concat('%', :query, '%'))
               or inn like concat('%', :query, '%')
            order by name
            limit :limit
            """, nativeQuery = true)
    List<ReferenceSupplierEntity> search(@Param("query") String query, @Param("limit") int limit);

    @Query(value = """
            select *
            from reference_supplier
            where inn = :inn
            limit 1
            """, nativeQuery = true)
    Optional<ReferenceSupplierEntity> findByInn(@Param("inn") String inn);

    @Query(value = """
            select *
            from reference_supplier
            where inn is null
              and lower(trim(name)) = lower(trim(:name))
            limit 1
            """, nativeQuery = true)
    Optional<ReferenceSupplierEntity> findByNameWithoutInn(@Param("name") String name);

    List<ReferenceSupplierEntity> findAllByOrderByNameAsc();
}
