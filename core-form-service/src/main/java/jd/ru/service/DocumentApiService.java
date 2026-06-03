package jd.ru.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jd.ru.api.dto.CreateDocumentRequest;
import jd.ru.api.dto.CreateDocumentResponse;
import jd.ru.api.dto.DeleteEntryResponse;
import jd.ru.api.dto.DictionariesResponse;
import jd.ru.api.dto.ExportDocumentRequest;
import jd.ru.api.dto.GetDocumentResponse;
import jd.ru.api.dto.ListEntriesResponse;
import jd.ru.api.dto.TotalsResponse;
import jd.ru.api.dto.UpdateCommonInfoRequest;
import jd.ru.api.dto.UpsertEntryRequest;
import jd.ru.api.dto.UpsertEntryResponse;
import jd.ru.api.error.ApiException;
import jd.ru.domain.DictionaryType;
import jd.ru.domain.SectionKeys;
import jd.ru.domain.DocumentCommonInfoEntity;
import jd.ru.domain.DocumentEntity;
import jd.ru.domain.DocumentEntryEntity;
import jd.ru.domain.SectionDictionaryEntity;
import jd.ru.domain.ValidationStatus;
import jd.ru.repository.DictionaryValueRepository;
import jd.ru.repository.DocumentCommonInfoRepository;
import jd.ru.repository.DocumentEntryRepository;
import jd.ru.repository.DocumentRepository;
import jd.ru.repository.SectionDictionaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class DocumentApiService {

    private static final Pattern SUPPLIER_INN_PATTERN = Pattern.compile("^[0-9]{10}([0-9]{2})?$");

    private final DocumentRepository documentRepository;
    private final DocumentCommonInfoRepository commonInfoRepository;
    private final DocumentEntryRepository entryRepository;
    private final SectionDictionaryRepository sectionDictionaryRepository;
    private final DictionaryValueRepository dictionaryValueRepository;
    private final EntryRowNumberService entryRowNumberService;
    private final XlsxTemplateExportService xlsxTemplateExportService;
    private final ObjectMapper objectMapper;

    public DocumentApiService(DocumentRepository documentRepository,
                              DocumentCommonInfoRepository commonInfoRepository,
                              DocumentEntryRepository entryRepository,
                              SectionDictionaryRepository sectionDictionaryRepository,
                              DictionaryValueRepository dictionaryValueRepository,
                              EntryRowNumberService entryRowNumberService,
                              XlsxTemplateExportService xlsxTemplateExportService,
                              ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.commonInfoRepository = commonInfoRepository;
        this.entryRepository = entryRepository;
        this.sectionDictionaryRepository = sectionDictionaryRepository;
        this.dictionaryValueRepository = dictionaryValueRepository;
        this.entryRowNumberService = entryRowNumberService;
        this.xlsxTemplateExportService = xlsxTemplateExportService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CreateDocumentResponse createDocument(CreateDocumentRequest request) {
        DocumentEntity document = documentRepository.save(new DocumentEntity());

        DocumentCommonInfoEntity common = new DocumentCommonInfoEntity();
        common.setDocument(document);
        common.setTruName(request.common().truName());
        common.setTruCode("");
        common.setStage(request.common().stage());
        common.setReportYear(request.common().reportYear());
        common.setPlanYear(request.common().planYear());
        commonInfoRepository.save(common);

        return new CreateDocumentResponse(document.getId(), document.getStatus(), document.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public GetDocumentResponse getDocument(UUID documentId) {
        DocumentEntity document = getDocumentEntity(documentId);
        DocumentCommonInfoEntity commonInfo = commonInfoRepository.findById(documentId)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Common info not found for document"));
        int entriesCount = entryRepository.findForList(documentId, null, null, null).size();
        return new GetDocumentResponse(
                document.getId(),
                document.getStatus(),
                new GetDocumentResponse.CommonInfo(
                        commonInfo.getTruName(),
                        commonInfo.getTruCode(),
                        commonInfo.getStage(),
                        commonInfo.getReportYear(),
                        commonInfo.getPlanYear()
                ),
                entriesCount
        );
    }

    @Transactional
    public void updateCommonInfo(UUID documentId, UpdateCommonInfoRequest request) {
        DocumentCommonInfoEntity commonInfo = commonInfoRepository.findById(documentId)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Common info not found for document"));
        commonInfo.setTruName(request.truName());
        commonInfo.setTruCode("");
        commonInfo.setStage(request.stage());
        commonInfo.setReportYear(request.reportYear());
        commonInfo.setPlanYear(request.planYear());
    }

    @Transactional
    public UpsertEntryResponse addEntry(UUID documentId, UpsertEntryRequest request) {
        DocumentEntity document = getDocumentEntity(documentId);
        SectionDictionaryEntity section = getSection(request.sectionKey());
        validateCol15(request.fields());
        validateTwoDecimalFields(request.fields());
        int nextRowNo = entryRepository.findMaxRowNo(documentId, request.sectionKey()).orElse(0) + 1;

        DocumentEntryEntity entry = new DocumentEntryEntity();
        entry.setDocument(document);
        entry.setSection(section);
        entry.setRowNo(nextRowNo);
        entry.setFields(request.fields());
        entry.setComputed(compute(section, request.fields()));
        entry.setValidationStatus(ValidationStatus.VALID);
        entryRepository.save(entry);

        return toUpsertResponse(entry);
    }

    @Transactional
    public UpsertEntryResponse updateEntry(UUID documentId, UUID entryId, UpsertEntryRequest request) {
        getDocumentEntity(documentId);
        DocumentEntryEntity entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Entry not found"));
        if (!entry.getDocument().getId().equals(documentId)) {
            throw new ApiException("NOT_FOUND", "Entry not found in document");
        }
        if (!entry.getSection().getKey().equals(request.sectionKey())) {
            throw new ApiException("VALIDATION_ERROR", "Changing sectionKey is not allowed");
        }

        validateCol15(request.fields());
        validateTwoDecimalFields(request.fields());
        entry.setFields(request.fields());
        entry.setComputed(compute(entry.getSection(), request.fields()));
        entry.setValidationStatus(ValidationStatus.VALID);
        entryRepository.save(entry);
        return toUpsertResponse(entry);
    }

    @Transactional
    public DeleteEntryResponse deleteEntry(UUID documentId, UUID entryId) {
        getDocumentEntity(documentId);
        DocumentEntryEntity entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Entry not found"));
        if (!entry.getDocument().getId().equals(documentId)) {
            throw new ApiException("NOT_FOUND", "Entry not found in document");
        }
        String sectionKey = entry.getSection().getKey();
        entryRepository.delete(entry);
        entryRowNumberService.renumberRows(documentId, sectionKey);
        return new DeleteEntryResponse(true, true);
    }

    @Transactional(readOnly = true)
    public ListEntriesResponse listEntries(UUID documentId, String sectionKey, Integer derivedFormNo, String status) {
        getDocumentEntity(documentId);
        ValidationStatus validationStatus = parseStatus(status);
        List<DocumentEntryEntity> entries = entryRepository.findForList(documentId, sectionKey, derivedFormNo, validationStatus);
        List<ListEntriesResponse.Item> items = entries.stream()
                .map(entry -> new ListEntriesResponse.Item(
                        entry.getId(),
                        entry.getSection().getKey(),
                        entry.getSection().getFormNo(),
                        entry.getSection().getSectionNo(),
                        toRowNo(entry.getSection().getFormNo(), entry.getSection().getSectionNo(), entry.getRowNo()),
                        entry.getFields(),
                        entry.getComputed(),
                        entry.getValidationStatus().name().toLowerCase()
                ))
                .toList();
        return new ListEntriesResponse(items, items.size());
    }

    @Transactional(readOnly = true)
    public TotalsResponse getTotals(UUID documentId) {
        getDocumentEntity(documentId);
        List<DocumentEntryEntity> entries = entryRepository.findForList(documentId, null, null, null);
        TotalsAccumulator form4s1 = new TotalsAccumulator();
        TotalsAccumulator form4s2 = new TotalsAccumulator();
        TotalsAccumulator form5s1 = new TotalsAccumulator();
        TotalsAccumulator form6 = new TotalsAccumulator();

        for (DocumentEntryEntity entry : entries) {
            TotalsAccumulator target = switch (entry.getSection().getKey()) {
                case "raw_materials" -> form4s1;
                case "aux_materials" -> form4s2;
                case "purchased_semi" -> form5s1;
                case "components" -> form6;
                default -> null;
            };
            if (target != null) {
                target.add(readComputed(entry.getComputed(), "col11"), readComputed(entry.getComputed(), "col12"));
            }
        }

        return new TotalsResponse(
                new TotalsResponse.Form4Totals(
                        form4s1.toPair(),
                        form4s2.toPair(),
                        new TotalsResponse.PairTotal(form4s1.col11 + form4s2.col11, form4s1.col12 + form4s2.col12)
                ),
                new TotalsResponse.Form5Totals(form5s1.toPair()),
                new TotalsResponse.Form6Totals(form6.toPair())
        );
    }

    @Transactional(readOnly = true)
    public DictionariesResponse getDictionaries() {
        List<DictionariesResponse.SectionKeyItem> sectionKeys = sectionDictionaryRepository.findByActiveTrueOrderByFormNoAscSectionNoAsc()
                .stream()
                .filter(section -> !SectionKeys.isExcluded(section.getKey()))
                .map(section -> new DictionariesResponse.SectionKeyItem(
                        section.getKey(),
                        section.getLabel(),
                        section.getFormNo(),
                        section.getSectionNo()
                ))
                .toList();

        List<DictionariesResponse.DictionaryItem> col13 = dictionaryValueRepository
                .findByDictionaryTypeAndActiveTrueOrderByCodeAsc(DictionaryType.COL13_2)
                .stream()
                .map(v -> new DictionariesResponse.DictionaryItem(v.getCode(), v.getLabel()))
                .toList();

        List<DictionariesResponse.DictionaryItem> col5 = dictionaryValueRepository
                .findByDictionaryTypeAndActiveTrueOrderByCodeAsc(DictionaryType.COL5_2)
                .stream()
                .map(v -> new DictionariesResponse.DictionaryItem(v.getCode(), v.getLabel()))
                .toList();

        return new DictionariesResponse(sectionKeys, col13, col5);
    }

    @Transactional(readOnly = true)
    public byte[] exportDocument(UUID documentId, ExportDocumentRequest request) {
        if (!"xlsx".equalsIgnoreCase(request.format())) {
            throw new ApiException("VALIDATION_ERROR", "Only format=xlsx is supported");
        }
        if (!"fill_template".equalsIgnoreCase(request.mode())) {
            throw new ApiException("VALIDATION_ERROR", "Only mode=fill_template is supported");
        }

        getDocumentEntity(documentId);
        DocumentCommonInfoEntity commonInfo = commonInfoRepository.findById(documentId)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Common info not found for document"));
        List<DocumentEntryEntity> entries = entryRepository.findForList(documentId, null, null, null);
        return xlsxTemplateExportService.exportFilledTemplate(commonInfo, entries);
    }

    private DocumentEntity getDocumentEntity(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Document not found"));
    }

    private SectionDictionaryEntity getSection(String sectionKey) {
        if (SectionKeys.isExcluded(sectionKey)) {
            throw new ApiException("VALIDATION_ERROR", "Раздел «Возвратные отходы» недоступен для ввода");
        }
        return sectionDictionaryRepository.findByKeyAndActiveTrue(sectionKey)
                .orElseThrow(() -> new ApiException("VALIDATION_ERROR", "Unknown sectionKey"));
    }

    private void validateTwoDecimalFields(JsonNode fields) {
        if (fields == null || fields.isNull()) {
            return;
        }
        for (String fieldName : List.of("col9", "col10")) {
            JsonNode node = fields.get(fieldName);
            if (node == null || node.isNull()) {
                continue;
            }
            BigDecimal value = toBigDecimal(node, fieldName);
            if (value.scale() > 2) {
                throw new ApiException(
                        "VALIDATION_ERROR",
                        "Поле " + fieldName + ": допустимо не более 2 знаков после запятой",
                        List.of(new ApiException.Detail("fields." + fieldName, "decimal_scale"))
                );
            }
        }
    }

    private BigDecimal toBigDecimal(JsonNode node, String fieldName) {
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual()) {
            String text = node.asText("").trim().replace(',', '.');
            if (text.isEmpty()) {
                throw new ApiException("VALIDATION_ERROR", "Поле " + fieldName + " должно быть числом");
            }
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException ex) {
                throw new ApiException("VALIDATION_ERROR", "Поле " + fieldName + " должно быть числом");
            }
        }
        throw new ApiException("VALIDATION_ERROR", "Поле " + fieldName + " должно быть числом");
    }

    private static double roundTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void validateCol15(JsonNode fields) {
        if (fields == null || fields.isNull()) {
            return;
        }
        JsonNode node = fields.get("col15");
        if (node == null || node.isNull()) {
            return;
        }
        String text = node.isNumber() ? node.asText() : node.asText("");
        text = text.trim();
        if (text.isEmpty()) {
            throw new ApiException(
                    "VALIDATION_ERROR",
                    "Поле col15 (ИНН организации-поставщика): укажите 10 или 12 цифр либо не передавайте поле",
                    List.of(new ApiException.Detail("fields.col15", "inn_empty"))
            );
        }
        if (!SUPPLIER_INN_PATTERN.matcher(text).matches()) {
            throw new ApiException(
                    "VALIDATION_ERROR",
                    "Поле col15 (ИНН организации-поставщика): допустимы только 10 или 12 цифр (ЮЛ — 10, ИП — 12)",
                    List.of(new ApiException.Detail("fields.col15", "inn_format"))
            );
        }
    }

    private UpsertEntryResponse toUpsertResponse(DocumentEntryEntity entry) {
        return new UpsertEntryResponse(
                entry.getId(),
                entry.getSection().getFormNo(),
                entry.getSection().getSectionNo(),
                toRowNo(entry.getSection().getFormNo(), entry.getSection().getSectionNo(), entry.getRowNo()),
                entry.getComputed()
        );
    }

    private JsonNode compute(SectionDictionaryEntity section, JsonNode fields) {
        double col11 = roundTwoDecimals(readNumeric(fields, "col7") * readNumeric(fields, "col9"));
        double col12 = roundTwoDecimals(readNumeric(fields, "col8") * readNumeric(fields, "col10"));
        return objectMapper.createObjectNode().put("col11", col11).put("col12", col12);
    }

    private double readNumeric(JsonNode node, String fieldName) {
        if (node == null || node.get(fieldName) == null || node.get(fieldName).isNull()) {
            return 0d;
        }
        JsonNode value = node.get(fieldName);
        if (value.isNumber()) {
            return value.asDouble();
        }
        if (value.isTextual()) {
            try {
                return Double.parseDouble(value.asText().replace(",", "."));
            } catch (NumberFormatException ex) {
                throw new ApiException("VALIDATION_ERROR", "Поле " + fieldName + " должно быть числом");
            }
        }
        throw new ApiException("VALIDATION_ERROR", "Поле " + fieldName + " должно быть числом");
    }

    private double readComputed(JsonNode computed, String fieldName) {
        if (computed == null || computed.get(fieldName) == null || computed.get(fieldName).isNull()) {
            return 0d;
        }
        return computed.get(fieldName).asDouble(0d);
    }

    private String toRowNo(Integer formNo, Integer sectionNo, Integer rowNo) {
        if (formNo != null && formNo == 6) {
            return String.valueOf(rowNo);
        }
        return sectionNo + "." + rowNo;
    }

    private ValidationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ValidationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ApiException("VALIDATION_ERROR", "Unknown validation status: " + status);
        }
    }

    private static final class TotalsAccumulator {
        private double col11;
        private double col12;

        void add(double col11, double col12) {
            this.col11 += col11;
            this.col12 += col12;
        }

        TotalsResponse.PairTotal toPair() {
            return new TotalsResponse.PairTotal(col11, col12);
        }
    }
}
