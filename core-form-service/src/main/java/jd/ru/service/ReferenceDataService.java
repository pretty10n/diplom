package jd.ru.service;

import com.fasterxml.jackson.databind.JsonNode;
import jd.ru.api.dto.ReferenceMaterialDto;
import jd.ru.api.dto.ReferenceSearchResponse;
import jd.ru.api.dto.ReferenceSupplierDto;
import jd.ru.api.dto.UpsertReferenceMaterialRequest;
import jd.ru.api.dto.UpsertReferenceSupplierRequest;
import jd.ru.api.error.ApiException;
import jd.ru.domain.ReferenceMaterialEntity;
import jd.ru.domain.ReferenceSupplierEntity;
import jd.ru.repository.ReferenceMaterialRepository;
import jd.ru.repository.ReferenceSupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ReferenceDataService {

    private static final Pattern SUPPLIER_INN_PATTERN = Pattern.compile("^[0-9]{10}([0-9]{2})?$");
    private static final int SEARCH_LIMIT_MAX = 50;
    private static final int SEARCH_LIMIT_DEFAULT = 10;

    private final ReferenceSupplierRepository supplierRepository;
    private final ReferenceMaterialRepository materialRepository;

    public ReferenceDataService(ReferenceSupplierRepository supplierRepository,
                                ReferenceMaterialRepository materialRepository) {
        this.supplierRepository = supplierRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional(readOnly = true)
    public ReferenceSearchResponse<ReferenceSupplierDto> searchSuppliers(String query, Integer limit) {
        if (query == null || query.isBlank()) {
            return new ReferenceSearchResponse<>(List.of());
        }
        int effectiveLimit = normalizeLimit(limit);
        List<ReferenceSupplierDto> items = supplierRepository.search(query.trim(), effectiveLimit)
                .stream()
                .map(this::toSupplierDto)
                .toList();
        return new ReferenceSearchResponse<>(items);
    }

    @Transactional(readOnly = true)
    public ReferenceSearchResponse<ReferenceSupplierDto> listSuppliers() {
        List<ReferenceSupplierDto> items = supplierRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toSupplierDto)
                .toList();
        return new ReferenceSearchResponse<>(items);
    }

    @Transactional
    public ReferenceSupplierDto createSupplier(UpsertReferenceSupplierRequest request) {
        String name = normalizeText(request.name());
        String inn = normalizeInn(request.inn());
        validateSupplierInn(inn);
        ensureSupplierUnique(name, inn, null);
        ReferenceSupplierEntity entity = new ReferenceSupplierEntity();
        entity.setName(name);
        entity.setInn(inn);
        return toSupplierDto(supplierRepository.save(entity));
    }

    @Transactional
    public ReferenceSupplierDto updateSupplier(UUID id, UpsertReferenceSupplierRequest request) {
        ReferenceSupplierEntity entity = supplierRepository.findById(id)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Поставщик не найден"));
        String name = normalizeText(request.name());
        String inn = normalizeInn(request.inn());
        validateSupplierInn(inn);
        ensureSupplierUnique(name, inn, id);
        entity.setName(name);
        entity.setInn(inn);
        return toSupplierDto(entity);
    }

    @Transactional
    public void deleteSupplier(UUID id) {
        if (!supplierRepository.existsById(id)) {
            throw new ApiException("NOT_FOUND", "Поставщик не найден");
        }
        supplierRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ReferenceSearchResponse<ReferenceMaterialDto> searchMaterials(String query, Integer limit) {
        if (query == null || query.isBlank()) {
            return new ReferenceSearchResponse<>(List.of());
        }
        int effectiveLimit = normalizeLimit(limit);
        List<ReferenceMaterialDto> items = materialRepository.search(query.trim(), effectiveLimit)
                .stream()
                .map(this::toMaterialDto)
                .toList();
        return new ReferenceSearchResponse<>(items);
    }

    @Transactional(readOnly = true)
    public ReferenceSearchResponse<ReferenceMaterialDto> listMaterials() {
        List<ReferenceMaterialDto> items = materialRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toMaterialDto)
                .toList();
        return new ReferenceSearchResponse<>(items);
    }

    @Transactional
    public ReferenceMaterialDto createMaterial(UpsertReferenceMaterialRequest request) {
        String name = normalizeText(request.name());
        ensureMaterialUnique(name, null);
        ReferenceMaterialEntity entity = new ReferenceMaterialEntity();
        entity.setName(name);
        entity.setOkpdCode(normalizeOptionalText(request.okpdCode()));
        entity.setEkpsCode(normalizeOptionalText(request.ekpsCode()));
        entity.setFnn(normalizeOptionalText(request.fnn()));
        return toMaterialDto(materialRepository.save(entity));
    }

    @Transactional
    public ReferenceMaterialDto updateMaterial(UUID id, UpsertReferenceMaterialRequest request) {
        ReferenceMaterialEntity entity = materialRepository.findById(id)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Материал не найден"));
        String name = normalizeText(request.name());
        ensureMaterialUnique(name, id);
        entity.setName(name);
        entity.setOkpdCode(normalizeOptionalText(request.okpdCode()));
        entity.setEkpsCode(normalizeOptionalText(request.ekpsCode()));
        entity.setFnn(normalizeOptionalText(request.fnn()));
        return toMaterialDto(entity);
    }

    @Transactional
    public void deleteMaterial(UUID id) {
        if (!materialRepository.existsById(id)) {
            throw new ApiException("NOT_FOUND", "Материал не найден");
        }
        materialRepository.deleteById(id);
    }

    @Transactional
    public void syncFromEntryFields(JsonNode fields) {
        if (fields == null || fields.isNull()) {
            return;
        }
        syncSupplierFromFields(fields);
        syncMaterialFromFields(fields);
    }

    private void syncSupplierFromFields(JsonNode fields) {
        String name = readText(fields, "col14");
        String inn = normalizeInn(readText(fields, "col15"));
        if (name == null) {
            return;
        }
        if (inn != null) {
            validateSupplierInn(inn);
            ReferenceSupplierEntity byInn = supplierRepository.findByInn(inn).orElse(null);
            if (byInn != null) {
                byInn.setName(name);
                return;
            }
        }
        ReferenceSupplierEntity byName = supplierRepository.findByNameWithoutInn(name).orElse(null);
        if (byName != null) {
            byName.setName(name);
            if (inn != null && supplierRepository.findByInn(inn).isEmpty()) {
                byName.setInn(inn);
            }
            return;
        }
        ReferenceSupplierEntity entity = new ReferenceSupplierEntity();
        entity.setName(name);
        entity.setInn(inn);
        supplierRepository.save(entity);
    }

    private void syncMaterialFromFields(JsonNode fields) {
        String name = readText(fields, "col2");
        if (name == null) {
            return;
        }
        String okpdCode = normalizeOptionalText(readText(fields, "col3"));
        String ekpsCode = normalizeOptionalText(readText(fields, "col4"));
        String fnn = normalizeOptionalText(readText(fields, "col5"));

        ReferenceMaterialEntity entity = materialRepository.findByNormalizedName(name).orElse(null);
        if (entity == null) {
            entity = new ReferenceMaterialEntity();
            entity.setName(name);
            entity.setOkpdCode(okpdCode);
            entity.setEkpsCode(ekpsCode);
            entity.setFnn(fnn);
            materialRepository.save(entity);
            return;
        }
        if (okpdCode != null) {
            entity.setOkpdCode(okpdCode);
        }
        if (ekpsCode != null) {
            entity.setEkpsCode(ekpsCode);
        }
        if (fnn != null) {
            entity.setFnn(fnn);
        }
    }

    private void ensureSupplierUnique(String name, String inn, UUID excludeId) {
        if (inn != null) {
            supplierRepository.findByInn(inn).ifPresent(existing -> {
                if (excludeId == null || !existing.getId().equals(excludeId)) {
                    throw new ApiException("CONFLICT", "Поставщик с таким ИНН уже существует");
                }
            });
            return;
        }
        supplierRepository.findByNameWithoutInn(name).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new ApiException("CONFLICT", "Поставщик с таким наименованием уже существует");
            }
        });
    }

    private void ensureMaterialUnique(String name, UUID excludeId) {
        materialRepository.findByNormalizedName(name).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new ApiException("CONFLICT", "Материал с таким наименованием уже существует");
            }
        });
    }

    private void validateSupplierInn(String inn) {
        if (inn == null) {
            return;
        }
        if (!SUPPLIER_INN_PATTERN.matcher(inn).matches()) {
            throw new ApiException("VALIDATION_ERROR", "ИНН: допустимы только 10 или 12 цифр");
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return SEARCH_LIMIT_DEFAULT;
        }
        return Math.min(limit, SEARCH_LIMIT_MAX);
    }

    private String readText(JsonNode fields, String fieldName) {
        JsonNode node = fields.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        String text = node.isNumber() ? node.asText() : node.asText("");
        text = text.trim();
        if (text.isEmpty() || "-".equals(text)) {
            return null;
        }
        return text;
    }

    private String normalizeText(String value) {
        if (value == null) {
            throw new ApiException("VALIDATION_ERROR", "Наименование обязательно");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ApiException("VALIDATION_ERROR", "Наименование обязательно");
        }
        return trimmed;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "-".equals(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private String normalizeInn(String value) {
        String normalized = normalizeOptionalText(value);
        return normalized == null ? null : normalized;
    }

    private ReferenceSupplierDto toSupplierDto(ReferenceSupplierEntity entity) {
        return new ReferenceSupplierDto(entity.getId(), entity.getName(), entity.getInn());
    }

    private ReferenceMaterialDto toMaterialDto(ReferenceMaterialEntity entity) {
        return new ReferenceMaterialDto(
                entity.getId(),
                entity.getName(),
                entity.getOkpdCode(),
                entity.getEkpsCode(),
                entity.getFnn()
        );
    }
}
