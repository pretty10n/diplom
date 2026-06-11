package jd.ru.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jd.ru.api.dto.ReferenceMaterialDto;
import jd.ru.api.dto.ReferenceSearchResponse;
import jd.ru.api.dto.ReferenceSupplierDto;
import jd.ru.api.dto.UpsertReferenceMaterialRequest;
import jd.ru.api.dto.UpsertReferenceSupplierRequest;
import jd.ru.service.ReferenceDataService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reference-data")
@Tag(name = "Reference Data", description = "Справочники поставщиков и материалов")
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping("/suppliers/search")
    @Operation(summary = "Поиск поставщиков для автозаполнения")
    public ReferenceSearchResponse<ReferenceSupplierDto> searchSuppliers(
            @RequestParam String q,
            @RequestParam(required = false) Integer limit
    ) {
        return referenceDataService.searchSuppliers(q, limit);
    }

    @GetMapping("/suppliers")
    @Operation(summary = "Список всех поставщиков")
    public ReferenceSearchResponse<ReferenceSupplierDto> listSuppliers() {
        return referenceDataService.listSuppliers();
    }

    @PostMapping("/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить поставщика")
    @ApiResponse(responseCode = "201", description = "Поставщик создан")
    public ReferenceSupplierDto createSupplier(@Valid @RequestBody UpsertReferenceSupplierRequest request) {
        return referenceDataService.createSupplier(request);
    }

    @PatchMapping("/suppliers/{id}")
    @Operation(summary = "Обновить поставщика")
    public ReferenceSupplierDto updateSupplier(@PathVariable UUID id,
                                               @Valid @RequestBody UpsertReferenceSupplierRequest request) {
        return referenceDataService.updateSupplier(id, request);
    }

    @DeleteMapping("/suppliers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить поставщика")
    public void deleteSupplier(@PathVariable UUID id) {
        referenceDataService.deleteSupplier(id);
    }

    @GetMapping("/materials/search")
    @Operation(summary = "Поиск материалов для автозаполнения")
    public ReferenceSearchResponse<ReferenceMaterialDto> searchMaterials(
            @RequestParam String q,
            @RequestParam(required = false) Integer limit
    ) {
        return referenceDataService.searchMaterials(q, limit);
    }

    @GetMapping("/materials")
    @Operation(summary = "Список всех материалов")
    public ReferenceSearchResponse<ReferenceMaterialDto> listMaterials() {
        return referenceDataService.listMaterials();
    }

    @PostMapping("/materials")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить материал")
    @ApiResponse(responseCode = "201", description = "Материал создан")
    public ReferenceMaterialDto createMaterial(@Valid @RequestBody UpsertReferenceMaterialRequest request) {
        return referenceDataService.createMaterial(request);
    }

    @PatchMapping("/materials/{id}")
    @Operation(summary = "Обновить материал")
    public ReferenceMaterialDto updateMaterial(@PathVariable UUID id,
                                               @Valid @RequestBody UpsertReferenceMaterialRequest request) {
        return referenceDataService.updateMaterial(id, request);
    }

    @DeleteMapping("/materials/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить материал")
    public void deleteMaterial(@PathVariable UUID id) {
        referenceDataService.deleteMaterial(id);
    }
}
