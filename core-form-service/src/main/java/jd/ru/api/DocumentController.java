package jd.ru.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jd.ru.api.dto.CreateDocumentRequest;
import jd.ru.api.dto.CreateDocumentResponse;
import jd.ru.api.dto.DeleteEntryResponse;
import jd.ru.api.dto.DictionariesResponse;
import jd.ru.api.dto.ExportDocumentRequest;
import jd.ru.api.dto.ExportDocumentResponse;
import jd.ru.api.dto.GetDocumentResponse;
import jd.ru.api.dto.ListEntriesResponse;
import jd.ru.api.dto.TotalsResponse;
import jd.ru.api.dto.UpdateCommonInfoRequest;
import jd.ru.api.dto.UpsertEntryRequest;
import jd.ru.api.dto.UpsertEntryResponse;
import jd.ru.service.DocumentApiService;
import jd.ru.service.ExportFileStore;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Documents", description = "CRUD API для документа и строк форм 4/5/6")
public class DocumentController {

    private final DocumentApiService documentApiService;
    private final ExportFileStore exportFileStore;

    public DocumentController(DocumentApiService documentApiService, ExportFileStore exportFileStore) {
        this.documentApiService = documentApiService;
        this.exportFileStore = exportFileStore;
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать документ")
    @ApiResponse(responseCode = "201", description = "Документ создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    public CreateDocumentResponse createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        return documentApiService.createDocument(request);
    }

    @GetMapping("/documents/{documentId}")
    @Operation(summary = "Получить документ")
    @ApiResponse(responseCode = "200", description = "Документ найден")
    @ApiResponse(responseCode = "404", description = "Документ не найден")
    public GetDocumentResponse getDocument(@PathVariable UUID documentId) {
        return documentApiService.getDocument(documentId);
    }

    @PatchMapping("/documents/{documentId}/common")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Обновить общие данные документа")
    @ApiResponse(responseCode = "200", description = "Данные обновлены")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "404", description = "Документ не найден")
    public void updateCommonInfo(@PathVariable UUID documentId, @Valid @RequestBody UpdateCommonInfoRequest request) {
        documentApiService.updateCommonInfo(documentId, request);
    }

    @PostMapping("/documents/{documentId}/entries")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить строку")
    @ApiResponse(responseCode = "201", description = "Строка добавлена")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "404", description = "Документ не найден")
    public UpsertEntryResponse addEntry(@PathVariable UUID documentId, @Valid @RequestBody UpsertEntryRequest request) {
        return documentApiService.addEntry(documentId, request);
    }

    @PatchMapping("/documents/{documentId}/entries/{entryId}")
    @Operation(summary = "Обновить строку")
    @ApiResponse(responseCode = "200", description = "Строка обновлена")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "404", description = "Строка или документ не найдены")
    public UpsertEntryResponse updateEntry(@PathVariable UUID documentId,
                                           @PathVariable UUID entryId,
                                           @Valid @RequestBody UpsertEntryRequest request) {
        return documentApiService.updateEntry(documentId, entryId, request);
    }

    @DeleteMapping("/documents/{documentId}/entries/{entryId}")
    @Operation(summary = "Удалить строку")
    @ApiResponse(responseCode = "200", description = "Строка удалена")
    @ApiResponse(responseCode = "404", description = "Строка или документ не найдены")
    public DeleteEntryResponse deleteEntry(@PathVariable UUID documentId, @PathVariable UUID entryId) {
        return documentApiService.deleteEntry(documentId, entryId);
    }

    @GetMapping("/documents/{documentId}/entries")
    @Operation(summary = "Получить список строк")
    @ApiResponse(responseCode = "200", description = "Список получен")
    @ApiResponse(responseCode = "400", description = "Ошибка параметров фильтра")
    @ApiResponse(responseCode = "404", description = "Документ не найден")
    public ListEntriesResponse listEntries(@PathVariable UUID documentId,
                                           @RequestParam(required = false) String sectionKey,
                                           @RequestParam(required = false) Integer derivedFormNo,
                                           @RequestParam(required = false, name = "status") String validationStatus) {
        return documentApiService.listEntries(documentId, sectionKey, derivedFormNo, validationStatus);
    }

    @GetMapping("/documents/{documentId}/totals")
    @Operation(summary = "Получить итоги по формам 4/5/6")
    @ApiResponse(responseCode = "200", description = "Итоги получены")
    @ApiResponse(responseCode = "404", description = "Документ не найден")
    public TotalsResponse totals(@PathVariable UUID documentId) {
        return documentApiService.getTotals(documentId);
    }

    @GetMapping("/dictionaries")
    @Operation(summary = "Получить справочники")
    @ApiResponse(responseCode = "200", description = "Справочники получены")
    public DictionariesResponse dictionaries() {
        return documentApiService.getDictionaries();
    }

    @PostMapping("/documents/{documentId}/export")
    @Operation(summary = "Экспорт документа в XLSX по шаблону")
    @ApiResponse(responseCode = "200", description = "Файл экспорта сформирован")
    @ApiResponse(responseCode = "400", description = "Ошибка параметров экспорта или структуры шаблона")
    @ApiResponse(responseCode = "404", description = "Документ не найден")
    public ExportDocumentResponse export(@PathVariable UUID documentId, @Valid @RequestBody ExportDocumentRequest request) {
        byte[] payload = documentApiService.exportDocument(documentId, request);
        String fileName = request.fileName() == null || request.fileName().isBlank()
                ? "document-export.xlsx"
                : request.fileName();
        ExportFileStore.StoredRef storedRef = exportFileStore.save(payload, fileName);
        return new ExportDocumentResponse(
                "/api/v1/files/" + storedRef.fileId(),
                storedRef.expiresAt(),
                new ExportDocumentResponse.ExportMeta(true, List.of("Форма 4", "Форма 5", "Форма 6"))
        );
    }

    @GetMapping("/files/{fileId}")
    @Operation(summary = "Скачать ранее сформированный файл экспорта")
    @ApiResponse(responseCode = "200", description = "Файл найден")
    @ApiResponse(responseCode = "404", description = "Файл не найден или истек срок хранения")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) {
        ExportFileStore.StoredFile file = exportFileStore.get(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.payload());
    }
}
