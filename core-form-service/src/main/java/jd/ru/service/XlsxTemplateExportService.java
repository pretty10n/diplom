package jd.ru.service;

import com.fasterxml.jackson.databind.JsonNode;
import jd.ru.api.error.ApiException;
import jd.ru.domain.DocumentCommonInfoEntity;
import jd.ru.domain.DocumentEntryEntity;
import jd.ru.domain.SectionKeys;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class XlsxTemplateExportService {

    private static final String SHEET_FORM_4_A = "Форма 4";
    private static final String SHEET_FORM_4_B = "Ф.4";
    private static final String SHEET_FORM_5_A = "Форма 5";
    private static final String SHEET_FORM_5_B = "Ф.5";
    private static final String SHEET_FORM_6_A = "Форма 6";
    private static final String SHEET_FORM_6_B = "Ф.6";

    private static final int FORM_4_SECTION_1_DATA_START_ROW = 15;
    private static final int FORM_4_SECTION_2_DATA_START_ROW = 20;
    private static final int FORM_4_SECTION_3_DATA_START_ROW = 26;
    private static final int FORM_4_SECTION_TEMPLATE_SLOTS = 3;
    private static final int FORM_4_SECTION_1_BOUNDARY_ROW = 18;
    private static final int FORM_4_SECTION_2_BOUNDARY_ROW = 23;
    private static final int FORM_4_SECTION_3_BOUNDARY_ROW = 30;
    private static final int FORM_4_SECTIONS_1_AND_2_GRAND_TOTAL_ROW = 13;
    private static final int FORM_5_SECTION_1_DATA_START_ROW = 15;
    private static final int FORM_5_SECTION_2_DATA_START_ROW = 20;
    private static final int FORM_5_SECTION_TEMPLATE_SLOTS = 3;
    private static final int FORM_5_SECTION_1_BOUNDARY_ROW = 18;
    private static final int FORM_5_SECTION_2_BOUNDARY_ROW = 23;
    private static final int FORM_6_ROW_START = 14;
    private static final int FORM_6_TEMPLATE_SLOTS = 3;
    private static final int FORM_6_BOUNDARY_ROW = 17;

    private static final List<SectionTotalBlock> FORM_4_TOTAL_BLOCKS = List.of(
            new SectionTotalBlock(1, FORM_4_SECTION_1_DATA_START_ROW, FORM_4_SECTION_TEMPLATE_SLOTS, FORM_4_SECTION_1_BOUNDARY_ROW),
            new SectionTotalBlock(2, FORM_4_SECTION_2_DATA_START_ROW, FORM_4_SECTION_TEMPLATE_SLOTS, FORM_4_SECTION_2_BOUNDARY_ROW)
    );

    private static final List<SectionTotalBlock> FORM_5_TOTAL_BLOCKS = List.of(
            new SectionTotalBlock(1, FORM_5_SECTION_1_DATA_START_ROW, FORM_5_SECTION_TEMPLATE_SLOTS, FORM_5_SECTION_1_BOUNDARY_ROW),
            new SectionTotalBlock(2, FORM_5_SECTION_2_DATA_START_ROW, FORM_5_SECTION_TEMPLATE_SLOTS, FORM_5_SECTION_2_BOUNDARY_ROW)
    );

    private static final List<SectionTotalBlock> FORM_6_TOTAL_BLOCKS = List.of(
            new SectionTotalBlock(1, FORM_6_ROW_START, FORM_6_TEMPLATE_SLOTS, FORM_6_BOUNDARY_ROW)
    );

    private static final CellCopyPolicy DATA_ROW_COPY_POLICY = createDataRowCopyPolicy();

    private static CellCopyPolicy createDataRowCopyPolicy() {
        CellCopyPolicy policy = new CellCopyPolicy();
        policy.setCopyCellValue(false);
        policy.setCopyCellStyle(true);
        policy.setCopyCellFormula(false);
        policy.setCopyHyperlink(false);
        policy.setMergeHyperlink(false);
        policy.setCondenseRows(false);
        return policy;
    }

    private static final FormHeaderLayout FORM_4_HEADER = new FormHeaderLayout(
            4, 23,
            6, 1,
            8, 28,
            8, 44
    );

    private static final FormHeaderLayout FORM_5_HEADER = new FormHeaderLayout(
            4, 19,
            6, 1,
            8, 24,
            8, 42
    );

    private static final FormHeaderLayout FORM_6_HEADER = new FormHeaderLayout(
            4, 17,
            6, 1,
            8, 23,
            8, 42
    );

    /**
     * Колонки по строке с номерами граф (Excel-строка 13, 0-based row 12) в templates/export-template.xlsx.
     */
    private static final Map<String, Integer> FORM_4_COLUMNS = freezeColumnMap(new LinkedHashMap<>() {
        {
            put("col1", 0);
            put("col2", 2);
            put("col3", 4);
            put("col4", 8);
            put("col5", 10);
            put("col6", 13);
            put("col7", 18);
            put("col8", 19);
            put("col9", 21);
            put("col10", 22);
            put("col11", 24);
            put("col12", 25);
            put("col13_1", 26);
            put("col13_2", 27);
            put("col14", 30);
            put("col15", 31);
        }
    });

    private static final Map<String, Integer> FORM_5_COLUMNS = freezeColumnMap(new LinkedHashMap<>() {
        {
            put("col1", 0);
            put("col2", 2);
            put("col3", 4);
            put("col4", 8);
            put("col5", 9);
            put("col6", 11);
            put("col7", 14);
            put("col8", 15);
            put("col9", 17);
            put("col10", 18);
            put("col11", 20);
            put("col12", 21);
            put("col13_1", 22);
            put("col13_2", 23);
            put("col14", 26);
            put("col15", 27);
        }
    });

    private static final Map<String, Integer> FORM_6_COLUMNS = freezeColumnMap(new LinkedHashMap<>() {
        {
            put("col1", 0);
            put("col2", 2);
            put("col3", 4);
            put("col4", 7);
            put("col5", 8);
            put("col6", 10);
            put("col7", 13);
            put("col8", 15);
            put("col9", 16);
            put("col10", 18);
            put("col11", 19);
            put("col12", 20);
            put("col13_1", 21);
            put("col13_2", 22);
            put("col14", 25);
            put("col15", 26);
        }
    });

    private static final List<String> WRAP_TEXT_FIELD_KEYS = List.of(
            "col2", "col3", "col4", "col5", "col6", "col13_1", "col13_2", "col14", "col15"
    );

    private static final double ROW_HEIGHT_LINE_SPACING = 1.15d;
    private static final float ROW_HEIGHT_PADDING_POINTS = 3f;

    private static final List<String> ENTRY_FIELD_ORDER = List.of(
            "col1", "col2", "col3", "col4", "col5", "col6",
            "col7", "col8", "col9", "col10", "col11", "col12",
            "col13_1", "col13_2", "col14", "col15"
    );

    private static Map<String, Integer> freezeColumnMap(LinkedHashMap<String, Integer> map) {
        return Map.copyOf(map);
    }

    private final Resource exportTemplate;

    public XlsxTemplateExportService(
            @Value("${app.export.template-path}") String templateLocation,
            ResourceLoader resourceLoader) {
        this.exportTemplate = resourceLoader.getResource(templateLocation);
    }

    public byte[] exportFilledTemplate(DocumentCommonInfoEntity commonInfo, List<DocumentEntryEntity> entries) {
        if (!exportTemplate.exists()) {
            throw new ApiException(
                    "VALIDATION_ERROR",
                    "Шаблон выгрузки не найден. Проверьте app.export.template-path: " + exportTemplate
            );
        }

        List<DocumentEntryEntity> exportEntries = entries.stream()
                .filter(entry -> !SectionKeys.isExcluded(entry.getSection().getKey()))
                .sorted(Comparator.comparing((DocumentEntryEntity e) -> e.getSection().getFormNo())
                        .thenComparing(e -> e.getSection().getSectionNo())
                        .thenComparing(DocumentEntryEntity::getRowNo))
                .toList();

        Set<Integer> usedFormNos = new HashSet<>();
        for (DocumentEntryEntity entry : exportEntries) {
            usedFormNos.add(entry.getSection().getFormNo());
        }
        if (usedFormNos.isEmpty()) {
            throw new ApiException("VALIDATION_ERROR", "Нет строк для экспорта: добавьте данные на шаге 2");
        }

        try (InputStream inputStream = exportTemplate.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Map<Integer, Sheet> filledSheetsByForm = validateAndResolveSheets(workbook, usedFormNos);

            if (usedFormNos.contains(4)) {
                Sheet form4 = filledSheetsByForm.get(4);
                fillCommonInfo(form4, sheetLabel(SHEET_FORM_4_A, form4), commonInfo, FORM_4_HEADER);
                ensureForm4Capacity(form4, countEntriesBySection(exportEntries, 4));
            }
            if (usedFormNos.contains(5)) {
                Sheet form5 = filledSheetsByForm.get(5);
                fillCommonInfo(form5, sheetLabel(SHEET_FORM_5_A, form5), commonInfo, FORM_5_HEADER);
                ensureForm5Capacity(form5, countEntriesBySection(exportEntries, 5));
            }
            if (usedFormNos.contains(6)) {
                Sheet form6 = filledSheetsByForm.get(6);
                fillCommonInfo(form6, sheetLabel(SHEET_FORM_6_A, form6), commonInfo, FORM_6_HEADER);
                ensureForm6Capacity(form6, countEntriesBySection(exportEntries, 6));
            }

            Map<Integer, Integer> form4SectionShifts = form4SectionShifts(countEntriesBySection(exportEntries, 4));
            Map<Integer, Integer> form5SectionShifts = form5SectionShifts(countEntriesBySection(exportEntries, 5));
            int form6Shift = form6Shift(countEntriesBySection(exportEntries, 6));

            int form4Section1Offset = 0;
            int form4Section2Offset = 0;
            int form4Section3Offset = 0;
            int form5Section1Offset = 0;
            int form5Section2Offset = 0;
            int form6Offset = 0;
            for (DocumentEntryEntity entry : exportEntries) {
                int formNo = entry.getSection().getFormNo();
                if (formNo == 4) {
                    Sheet form4 = filledSheetsByForm.get(4);
                    int sec = entry.getSection().getSectionNo();
                    int rowIndex;
                    if (sec == 2) {
                        rowIndex = FORM_4_SECTION_2_DATA_START_ROW
                                + form4SectionShifts.getOrDefault(2, 0)
                                + form4Section2Offset++;
                    } else if (sec == 3) {
                        rowIndex = FORM_4_SECTION_3_DATA_START_ROW
                                + form4SectionShifts.getOrDefault(3, 0)
                                + form4Section3Offset++;
                    } else {
                        rowIndex = FORM_4_SECTION_1_DATA_START_ROW
                                + form4SectionShifts.getOrDefault(1, 0)
                                + form4Section1Offset++;
                    }
                    fillEntryRow(workbook, form4, sheetLabel(SHEET_FORM_4_A, form4), rowIndex, FORM_4_COLUMNS, entry, formNo);
                } else if (formNo == 5) {
                    Sheet form5 = filledSheetsByForm.get(5);
                    int sec = entry.getSection().getSectionNo();
                    int rowIndex;
                    if (sec == 2) {
                        rowIndex = FORM_5_SECTION_2_DATA_START_ROW
                                + form5SectionShifts.getOrDefault(2, 0)
                                + form5Section2Offset++;
                    } else {
                        rowIndex = FORM_5_SECTION_1_DATA_START_ROW
                                + form5SectionShifts.getOrDefault(1, 0)
                                + form5Section1Offset++;
                    }
                    fillEntryRow(workbook, form5, sheetLabel(SHEET_FORM_5_A, form5), rowIndex, FORM_5_COLUMNS, entry, formNo);
                } else if (formNo == 6) {
                    Sheet form6 = filledSheetsByForm.get(6);
                    fillEntryRow(workbook, form6, sheetLabel(SHEET_FORM_6_A, form6),
                            FORM_6_ROW_START + form6Shift + form6Offset++, FORM_6_COLUMNS, entry, formNo);
                }
            }

            if (usedFormNos.contains(4)) {
                Map<Integer, Long> form4EntryCounts = countEntriesBySection(exportEntries, 4);
                applySectionTotals(
                        filledSheetsByForm.get(4),
                        workbook,
                        FORM_4_COLUMNS,
                        FORM_4_TOTAL_BLOCKS,
                        form4EntryCounts
                );
                applyForm4SectionsGrandTotal(filledSheetsByForm.get(4), workbook, form4EntryCounts);
            }
            if (usedFormNos.contains(5)) {
                applySectionTotals(
                        filledSheetsByForm.get(5),
                        workbook,
                        FORM_5_COLUMNS,
                        FORM_5_TOTAL_BLOCKS,
                        countEntriesBySection(exportEntries, 5)
                );
            }
            if (usedFormNos.contains(6)) {
                applySectionTotals(
                        filledSheetsByForm.get(6),
                        workbook,
                        FORM_6_COLUMNS,
                        FORM_6_TOTAL_BLOCKS,
                        countEntriesBySection(exportEntries, 6)
                );
            }

            pruneWorkbookSheets(workbook, usedFormNos, filledSheetsByForm);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new ApiException("INTERNAL_ERROR", "Failed to generate xlsx export");
        }
    }

    private Map<Integer, Sheet> validateAndResolveSheets(Workbook workbook, Set<Integer> usedFormNos) {
        List<ApiException.Detail> details = new java.util.ArrayList<>();
        Map<Integer, Sheet> sheetsByForm = new LinkedHashMap<>();

        if (usedFormNos.contains(4)) {
            Sheet form4 = resolveSheet(workbook, SHEET_FORM_4_A, SHEET_FORM_4_B);
            validateSheetPresence(form4, SHEET_FORM_4_A + "/" + SHEET_FORM_4_B, details);
            if (form4 != null) {
                String label = sheetLabel(SHEET_FORM_4_A, form4);
                validateCommonInfoCells(form4, label, FORM_4_HEADER, details);
                validateColumns(form4, label, FORM_4_SECTION_1_DATA_START_ROW, FORM_4_COLUMNS, details);
                validateColumns(form4, label, FORM_4_SECTION_2_DATA_START_ROW, FORM_4_COLUMNS, details);
                validateColumns(form4, label, FORM_4_SECTION_3_DATA_START_ROW, FORM_4_COLUMNS, details);
                sheetsByForm.put(4, form4);
            }
        }
        if (usedFormNos.contains(5)) {
            Sheet form5 = resolveSheet(workbook, SHEET_FORM_5_A, SHEET_FORM_5_B);
            validateSheetPresence(form5, SHEET_FORM_5_A + "/" + SHEET_FORM_5_B, details);
            if (form5 != null) {
                String label = sheetLabel(SHEET_FORM_5_A, form5);
                validateCommonInfoCells(form5, label, FORM_5_HEADER, details);
                validateColumns(form5, label, FORM_5_SECTION_1_DATA_START_ROW, FORM_5_COLUMNS, details);
                validateColumns(form5, label, FORM_5_SECTION_2_DATA_START_ROW, FORM_5_COLUMNS, details);
                sheetsByForm.put(5, form5);
            }
        }
        if (usedFormNos.contains(6)) {
            Sheet form6 = resolveSheet(workbook, SHEET_FORM_6_A, SHEET_FORM_6_B);
            validateSheetPresence(form6, SHEET_FORM_6_A + "/" + SHEET_FORM_6_B, details);
            if (form6 != null) {
                String label = sheetLabel(SHEET_FORM_6_A, form6);
                validateCommonInfoCells(form6, label, FORM_6_HEADER, details);
                validateColumns(form6, label, FORM_6_ROW_START, FORM_6_COLUMNS, details);
                sheetsByForm.put(6, form6);
            }
        }

        if (!details.isEmpty()) {
            throw new ApiException("VALIDATION_ERROR", "Template structure mismatch", details);
        }
        return sheetsByForm;
    }

    private void pruneWorkbookSheets(Workbook workbook, Set<Integer> usedFormNos, Map<Integer, Sheet> keptSheetsByForm) {
        for (int i = workbook.getNumberOfSheets() - 1; i >= 0; i--) {
            Sheet sheet = workbook.getSheetAt(i);
            Integer formNo = formNoFromSheetName(sheet.getSheetName());
            if (formNo == null) {
                workbook.removeSheetAt(i);
                continue;
            }
            if (!usedFormNos.contains(formNo)) {
                workbook.removeSheetAt(i);
                continue;
            }
            Sheet kept = keptSheetsByForm.get(formNo);
            if (kept != null && sheet != kept) {
                workbook.removeSheetAt(i);
            }
        }
    }

    private static Integer formNoFromSheetName(String sheetName) {
        if (SHEET_FORM_4_A.equals(sheetName) || SHEET_FORM_4_B.equals(sheetName)) {
            return 4;
        }
        if (SHEET_FORM_5_A.equals(sheetName) || SHEET_FORM_5_B.equals(sheetName)) {
            return 5;
        }
        if (SHEET_FORM_6_A.equals(sheetName) || SHEET_FORM_6_B.equals(sheetName)) {
            return 6;
        }
        return null;
    }

    private static Sheet resolveSheet(Workbook workbook, String primary, String alternate) {
        Sheet sheet = workbook.getSheet(primary);
        if (sheet == null) {
            sheet = workbook.getSheet(alternate);
        }
        return sheet;
    }

    private static String sheetLabel(String canonicalName, Sheet sheet) {
        return sheet != null ? sheet.getSheetName() : canonicalName;
    }

    private record SectionBlock(int dataStartRow, int templateSlots, int boundaryRow) {
    }

    private record SectionTotalBlock(int sectionNo, int dataStartRow, int templateSlots, int totalRow) {
    }

    private static Map<Integer, Long> countEntriesBySection(List<DocumentEntryEntity> entries, int formNo) {
        return entries.stream()
                .filter(entry -> entry.getSection().getFormNo() == formNo)
                .collect(Collectors.groupingBy(
                        entry -> entry.getSection().getSectionNo(),
                        Collectors.counting()
                ));
    }

    private void ensureForm4Capacity(Sheet sheet, Map<Integer, Long> entryCountBySection) {
        expandSectionBlocks(sheet, List.of(
                new SectionBlock(FORM_4_SECTION_1_DATA_START_ROW, FORM_4_SECTION_TEMPLATE_SLOTS, FORM_4_SECTION_1_BOUNDARY_ROW),
                new SectionBlock(FORM_4_SECTION_2_DATA_START_ROW, FORM_4_SECTION_TEMPLATE_SLOTS, FORM_4_SECTION_2_BOUNDARY_ROW),
                new SectionBlock(FORM_4_SECTION_3_DATA_START_ROW, FORM_4_SECTION_TEMPLATE_SLOTS, FORM_4_SECTION_3_BOUNDARY_ROW)
        ), entryCountBySection);
    }

    private void ensureForm5Capacity(Sheet sheet, Map<Integer, Long> entryCountBySection) {
        expandSectionBlocks(sheet, List.of(
                new SectionBlock(FORM_5_SECTION_1_DATA_START_ROW, FORM_5_SECTION_TEMPLATE_SLOTS, FORM_5_SECTION_1_BOUNDARY_ROW),
                new SectionBlock(FORM_5_SECTION_2_DATA_START_ROW, FORM_5_SECTION_TEMPLATE_SLOTS, FORM_5_SECTION_2_BOUNDARY_ROW)
        ), entryCountBySection);
    }

    private void ensureForm6Capacity(Sheet sheet, Map<Integer, Long> entryCountBySection) {
        long entryCount = entryCountBySection.getOrDefault(1, 0L);
        expandSectionBlocks(sheet, List.of(
                new SectionBlock(FORM_6_ROW_START, FORM_6_TEMPLATE_SLOTS, FORM_6_BOUNDARY_ROW)
        ), Map.of(1, entryCount));
    }

    private Map<Integer, Integer> form4SectionShifts(Map<Integer, Long> entryCountBySection) {
        return sectionShifts(List.of(
                new SectionBlock(FORM_4_SECTION_1_DATA_START_ROW, FORM_4_SECTION_TEMPLATE_SLOTS, FORM_4_SECTION_1_BOUNDARY_ROW),
                new SectionBlock(FORM_4_SECTION_2_DATA_START_ROW, FORM_4_SECTION_TEMPLATE_SLOTS, FORM_4_SECTION_2_BOUNDARY_ROW),
                new SectionBlock(FORM_4_SECTION_3_DATA_START_ROW, FORM_4_SECTION_TEMPLATE_SLOTS, FORM_4_SECTION_3_BOUNDARY_ROW)
        ), entryCountBySection);
    }

    private Map<Integer, Integer> form5SectionShifts(Map<Integer, Long> entryCountBySection) {
        return sectionShifts(List.of(
                new SectionBlock(FORM_5_SECTION_1_DATA_START_ROW, FORM_5_SECTION_TEMPLATE_SLOTS, FORM_5_SECTION_1_BOUNDARY_ROW),
                new SectionBlock(FORM_5_SECTION_2_DATA_START_ROW, FORM_5_SECTION_TEMPLATE_SLOTS, FORM_5_SECTION_2_BOUNDARY_ROW)
        ), entryCountBySection);
    }

    private int form6Shift(Map<Integer, Long> entryCountBySection) {
        Map<Integer, Integer> shifts = sectionShifts(List.of(
                new SectionBlock(FORM_6_ROW_START, FORM_6_TEMPLATE_SLOTS, FORM_6_BOUNDARY_ROW)
        ), entryCountBySection);
        return shifts.getOrDefault(1, 0);
    }

    private Map<Integer, Integer> sectionShifts(List<SectionBlock> blocks, Map<Integer, Long> entryCountBySection) {
        Map<Integer, Integer> shifts = new HashMap<>();
        int cumulativeShift = 0;
        for (int i = 0; i < blocks.size(); i++) {
            SectionBlock block = blocks.get(i);
            shifts.put(i + 1, cumulativeShift);
            long entryCount = entryCountBySection.getOrDefault(i + 1, 0L);
            cumulativeShift += extraRowsNeeded((int) entryCount, block.templateSlots());
        }
        return shifts;
    }

    private void expandSectionBlocks(Sheet sheet,
                                     List<SectionBlock> blocks,
                                     Map<Integer, Long> entryCountBySection) {
        int cumulativeShift = 0;
        for (int i = 0; i < blocks.size(); i++) {
            SectionBlock block = blocks.get(i);
            long entryCount = entryCountBySection.getOrDefault(i + 1, 0L);
            int extraRows = extraRowsNeeded((int) entryCount, block.templateSlots());
            if (extraRows <= 0) {
                continue;
            }
            int boundaryRow = block.boundaryRow() + cumulativeShift;
            int styleSourceRow = block.dataStartRow() + block.templateSlots() - 1 + cumulativeShift;
            insertExtraDataRows(sheet, boundaryRow, extraRows, styleSourceRow);
            cumulativeShift += extraRows;
        }
    }

    private static int extraRowsNeeded(int entryCount, int templateSlots) {
        return Math.max(0, entryCount - templateSlots);
    }

    private void insertExtraDataRows(Sheet sheet, int insertAtRow, int rowCount, int styleSourceRow) {
        int lastRow = sheet.getLastRowNum();
        if (insertAtRow <= lastRow) {
            sheet.shiftRows(insertAtRow, lastRow, rowCount, true, false);
        }
        if (!(sheet instanceof XSSFSheet xssfSheet)) {
            throw new ApiException("INTERNAL_ERROR", "Row expansion requires XSSF template");
        }
        for (int offset = 0; offset < rowCount; offset++) {
            xssfSheet.copyRows(styleSourceRow, styleSourceRow, insertAtRow + offset, DATA_ROW_COPY_POLICY);
        }
    }

    private void applySectionTotals(Sheet sheet,
                                    Workbook workbook,
                                    Map<String, Integer> columnMap,
                                    List<SectionTotalBlock> totalBlocks,
                                    Map<Integer, Long> entryCountBySection) {
        Integer costPlanColumn = columnMap.get("col11");
        Integer costFactColumn = columnMap.get("col12");
        if (costPlanColumn == null || costFactColumn == null) {
            return;
        }

        int cumulativeExtra = 0;
        for (SectionTotalBlock block : totalBlocks) {
            long entryCount = entryCountBySection.getOrDefault(block.sectionNo(), 0L);
            if (entryCount <= 0) {
                continue;
            }
            int extraRows = extraRowsNeeded((int) entryCount, block.templateSlots());
            int firstDataRow = block.dataStartRow() + cumulativeExtra;
            int lastDataRow = firstDataRow + (int) entryCount - 1;
            int totalRow = block.totalRow() + cumulativeExtra + extraRows;
            setSumFormula(sheet, workbook, totalRow, costPlanColumn, firstDataRow, lastDataRow);
            setSumFormula(sheet, workbook, totalRow, costFactColumn, firstDataRow, lastDataRow);
            cumulativeExtra += extraRows;
        }
    }

    private void applyForm4SectionsGrandTotal(Sheet sheet,
                                              Workbook workbook,
                                              Map<Integer, Long> entryCountBySection) {
        long section1Count = entryCountBySection.getOrDefault(1, 0L);
        long section2Count = entryCountBySection.getOrDefault(2, 0L);
        if (section1Count <= 0 && section2Count <= 0) {
            return;
        }

        int extraSection1 = extraRowsNeeded((int) section1Count, FORM_4_SECTION_TEMPLATE_SLOTS);
        int extraSection2 = extraRowsNeeded((int) section2Count, FORM_4_SECTION_TEMPLATE_SLOTS);
        int section1TotalRow = section1Count > 0
                ? FORM_4_SECTION_1_BOUNDARY_ROW + extraSection1
                : -1;
        int section2TotalRow = section2Count > 0
                ? FORM_4_SECTION_2_BOUNDARY_ROW + extraSection1 + extraSection2
                : -1;

        Row grandTotalRow = sheet.getRow(FORM_4_SECTIONS_1_AND_2_GRAND_TOTAL_ROW);
        if (grandTotalRow == null) {
            return;
        }

        setGrandTotalFormula(sheet, workbook, grandTotalRow, FORM_4_COLUMNS.get("col11"),
                section1TotalRow, section2TotalRow, section1Count > 0, section2Count > 0);
        setGrandTotalFormula(sheet, workbook, grandTotalRow, FORM_4_COLUMNS.get("col12"),
                section1TotalRow, section2TotalRow, section1Count > 0, section2Count > 0);
    }

    private void setGrandTotalFormula(Sheet sheet,
                                      Workbook workbook,
                                      Row grandTotalRow,
                                      Integer columnIndex,
                                      int section1TotalRow,
                                      int section2TotalRow,
                                      boolean includeSection1,
                                      boolean includeSection2) {
        if (columnIndex == null) {
            return;
        }
        Cell cell = obtainCell(grandTotalRow, columnIndex);
        String columnLetter = toColumnLetter(columnIndex);
        String formula;
        if (includeSection1 && includeSection2) {
            formula = columnLetter + (section1TotalRow + 1) + "+" + columnLetter + (section2TotalRow + 1);
        } else if (includeSection1) {
            formula = columnLetter + (section1TotalRow + 1);
        } else {
            formula = columnLetter + (section2TotalRow + 1);
        }
        cell.setCellFormula(formula);
        applyTwoDecimalCellStyle(workbook, cell);
    }

    private void setSumFormula(Sheet sheet,
                               Workbook workbook,
                               int totalRowIndex,
                               int columnIndex,
                               int firstDataRowIndex,
                               int lastDataRowIndex) {
        Row totalRow = sheet.getRow(totalRowIndex);
        if (totalRow == null) {
            return;
        }
        Cell cell = obtainCell(totalRow, columnIndex);
        String columnLetter = toColumnLetter(columnIndex);
        String formula = "SUM("
                + columnLetter + (firstDataRowIndex + 1)
                + ":"
                + columnLetter + (lastDataRowIndex + 1)
                + ")";
        cell.setCellFormula(formula);
        applyTwoDecimalCellStyle(workbook, cell);
    }

    private void fillCommonInfo(Sheet sheet,
                                String sheetName,
                                DocumentCommonInfoEntity commonInfo,
                                FormHeaderLayout layout) {
        setCellText(sheet, sheetName, layout.truRow(), layout.truCol(), formatTruHeaderLine(commonInfo));
        setCellText(sheet, sheetName, layout.stageRow(), layout.stageCol(), commonInfo.getStage());
        setCellNumeric(sheet, sheetName, layout.reportYearRow(), layout.reportYearCol(), commonInfo.getReportYear());
        setCellNumeric(sheet, sheetName, layout.planYearRow(), layout.planYearCol(), commonInfo.getPlanYear());
    }

    private void setCellText(Sheet sheet, String sheetName, int rowIndex, int columnIndex, String value) {
        Row row = requireRow(sheet, sheetName, rowIndex);
        obtainCell(row, columnIndex).setCellValue(value == null ? "" : value);
    }

    private void setCellNumeric(Sheet sheet, String sheetName, int rowIndex, int columnIndex, Integer value) {
        if (value == null) {
            return;
        }
        Row row = requireRow(sheet, sheetName, rowIndex);
        obtainCell(row, columnIndex).setCellValue(value.doubleValue());
    }

    private void validateCommonInfoCells(Sheet sheet,
                                       String sheetName,
                                       FormHeaderLayout layout,
                                       List<ApiException.Detail> details) {
        validateRow(sheet, sheetName, layout.truRow(), "header.truNameAndCode", details);
        validateRow(sheet, sheetName, layout.stageRow(), "header.stage", details);
        validateRow(sheet, sheetName, layout.reportYearRow(), "header.reportYear", details);
    }

    private void validateRow(Sheet sheet, String sheetName, int rowIndex, String reason, List<ApiException.Detail> details) {
        if (sheet.getRow(rowIndex) == null) {
            details.add(new ApiException.Detail(sheetName + "!" + (rowIndex + 1), reason));
        }
    }

    private record FormHeaderLayout(
            int truRow,
            int truCol,
            int stageRow,
            int stageCol,
            int reportYearRow,
            int reportYearCol,
            int planYearRow,
            int planYearCol
    ) {
    }

    private static String formatTruHeaderLine(DocumentCommonInfoEntity commonInfo) {
        String name = commonInfo.getTruName() == null ? "" : commonInfo.getTruName().trim();
        String code = commonInfo.getTruCode() == null ? "" : commonInfo.getTruCode().trim();
        if (name.isEmpty()) {
            return code;
        }
        if (code.isEmpty()) {
            return name;
        }
        if (name.equalsIgnoreCase(code)) {
            return name;
        }
        return name + " / " + code;
    }

    private void fillEntryRow(Workbook workbook,
                              Sheet sheet,
                              String sheetName,
                              int rowIndex,
                              Map<String, Integer> columnMap,
                              DocumentEntryEntity entry,
                              int formNo) {
        Row row = requireRow(sheet, sheetName, rowIndex);
        JsonNode fields = entry.getFields();
        String rowLabel = formNo == 6
                ? String.valueOf(entry.getRowNo())
                : entry.getSection().getSectionNo() + "." + entry.getRowNo();

        for (String key : ENTRY_FIELD_ORDER) {
            Integer columnIndex = columnMap.get(key);
            if (columnIndex == null) {
                continue;
            }
            JsonNode valueNode;
            if ("col1".equals(key)) {
                Cell cell = obtainCell(row, columnIndex);
                cell.setCellValue(rowLabel);
                continue;
            }
            valueNode = fields == null ? null : fields.get(key);
            if ("col11".equals(key) || "col12".equals(key)) {
                continue;
            }
            if (valueNode == null || valueNode.isNull()) {
                continue;
            }
            Cell cell = obtainCell(row, columnIndex);
            if (isTwoDecimalField(key)) {
                Double numeric = parseNumeric(valueNode);
                if (numeric != null) {
                    cell.setCellValue(roundTwoDecimals(numeric));
                    applyTwoDecimalCellStyle(workbook, cell);
                } else {
                    cell.setCellValue(valueNode.asText(""));
                }
                continue;
            }
            if (valueNode.isNumber()) {
                cell.setCellValue(valueNode.asDouble());
            } else {
                cell.setCellValue(valueNode.asText(""));
            }
            if (isWrapTextField(key)) {
                applyWrapTextCellStyle(workbook, cell, "col2".equals(key));
            }
        }

        applyCostFormula(workbook, row, rowIndex, columnMap, "col11", "col7", "col9");
        applyCostFormula(workbook, row, rowIndex, columnMap, "col12", "col8", "col10");
        adjustRowHeightForWrappedCells(workbook, sheet, row, columnMap);
    }

    private void applyCostFormula(Workbook workbook,
                                  Row row,
                                  int rowIndex,
                                  Map<String, Integer> columnMap,
                                  String costColumnKey,
                                  String normColumnKey,
                                  String priceColumnKey) {
        Integer costColumn = columnMap.get(costColumnKey);
        Integer normColumn = columnMap.get(normColumnKey);
        Integer priceColumn = columnMap.get(priceColumnKey);
        if (costColumn == null || normColumn == null || priceColumn == null) {
            return;
        }
        Cell cell = obtainCell(row, costColumn);
        int excelRow = rowIndex + 1;
        String formula = toColumnLetter(normColumn) + excelRow + "*" + toColumnLetter(priceColumn) + excelRow;
        cell.setCellFormula(formula);
        applyTwoDecimalCellStyle(workbook, cell);
    }

    private static Cell obtainCell(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        return cell != null ? cell : row.createCell(columnIndex);
    }

    private static boolean isTwoDecimalField(String key) {
        return "col9".equals(key) || "col10".equals(key);
    }

    private static Double parseNumeric(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asDouble();
        }
        if (node.isTextual()) {
            String text = node.asText("").trim().replace(',', '.');
            if (text.isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private static double roundTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static boolean isWrapTextField(String key) {
        return WRAP_TEXT_FIELD_KEYS.contains(key);
    }

    private void adjustRowHeightForWrappedCells(Workbook workbook,
                                                Sheet sheet,
                                                Row row,
                                                Map<String, Integer> columnMap) {
        float maxHeight = sheet.getDefaultRowHeightInPoints();
        for (String key : WRAP_TEXT_FIELD_KEYS) {
            Integer columnIndex = columnMap.get(key);
            if (columnIndex == null) {
                continue;
            }
            Cell cell = row.getCell(columnIndex);
            if (cell == null || !cell.getCellStyle().getWrapText()) {
                continue;
            }
            String text = getCellDisplayText(cell);
            if (text.isBlank()) {
                continue;
            }
            maxHeight = Math.max(maxHeight, calculateRequiredRowHeight(workbook, sheet, row.getRowNum(), columnIndex, text, cell));
        }
        if (maxHeight > row.getHeightInPoints()) {
            row.setHeightInPoints(maxHeight);
        }
        if (row instanceof XSSFRow xssfRow) {
            xssfRow.getCTRow().setCustomHeight(true);
        }
    }

    private static String getCellDisplayText(Cell cell) {
        if (cell.getCellType() == CellType.FORMULA) {
            return "";
        }
        return new DataFormatter().formatCellValue(cell).trim();
    }

    private static float calculateRequiredRowHeight(Workbook workbook,
                                                  Sheet sheet,
                                                  int rowIndex,
                                                  int columnIndex,
                                                  String text,
                                                  Cell cell) {
        int displayWidthUnits = getMergedColumnWidthUnits(sheet, rowIndex, columnIndex);
        int charsPerLine = Math.max(1, (int) Math.floor((displayWidthUnits / 256.0) * 0.85));
        int lines = 0;
        for (String paragraph : text.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                lines++;
            } else {
                lines += Math.max(1, (int) Math.ceil((double) paragraph.length() / charsPerLine));
            }
        }
        lines = Math.max(lines, 1);

        CellStyle style = cell.getCellStyle();
        Font font = workbook.getFontAt(style.getFontIndex());
        float lineHeight = font.getFontHeightInPoints() > 0
                ? font.getFontHeightInPoints()
                : sheet.getDefaultRowHeightInPoints();

        return (float) (lines * lineHeight * ROW_HEIGHT_LINE_SPACING + ROW_HEIGHT_PADDING_POINTS);
    }

    private static int getMergedColumnWidthUnits(Sheet sheet, int rowIndex, int columnIndex) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(rowIndex, columnIndex)) {
                int width = 0;
                for (int col = region.getFirstColumn(); col <= region.getLastColumn(); col++) {
                    width += sheet.getColumnWidth(col);
                }
                return Math.max(width, 256);
            }
        }
        return Math.max(sheet.getColumnWidth(columnIndex), 256);
    }

    private static void applyWrapTextCellStyle(Workbook workbook, Cell cell, boolean leftAlign) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(cell.getCellStyle());
        style.setWrapText(true);
        if (leftAlign) {
            style.setAlignment(HorizontalAlignment.LEFT);
        }
        cell.setCellStyle(style);
    }

    private static void applyTwoDecimalCellStyle(Workbook workbook, Cell cell) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(cell.getCellStyle());
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("0.00"));
        cell.setCellStyle(style);
    }

    private Row requireRow(Sheet sheet, String sheetName, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            throw new ApiException("VALIDATION_ERROR", "Template mismatch: row not found",
                    List.of(new ApiException.Detail(sheetName + "!" + (rowIndex + 1), "row_missing")));
        }
        return row;
    }

    private Cell requireCell(Row row, String sheetName, int rowIndex, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            throw new ApiException("VALIDATION_ERROR", "Template mismatch: cell not found",
                    List.of(new ApiException.Detail(sheetName + "!" + toA1(rowIndex, columnIndex), "cell_missing")));
        }
        return cell;
    }

    private void validateSheetPresence(Sheet sheet, String sheetName, List<ApiException.Detail> details) {
        if (sheet == null) {
            details.add(new ApiException.Detail(sheetName, "sheet_missing"));
        }
    }

    private void validateColumns(Sheet sheet,
                                 String sheetName,
                                 int rowIndex,
                                 Map<String, Integer> columns,
                                 List<ApiException.Detail> details) {
        validateCell(sheet, sheetName, rowIndex, columns.get("col1"), "fields.col1", details);
        validateCell(sheet, sheetName, rowIndex, columns.get("col2"), "fields.col2", details);
    }

    private void validateCell(Sheet sheet,
                              String sheetName,
                              int rowIndex,
                              int columnIndex,
                              String reason,
                              List<ApiException.Detail> details) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            details.add(new ApiException.Detail(sheetName + "!" + (rowIndex + 1), "row_missing"));
            return;
        }
        if (row.getCell(columnIndex) == null) {
            details.add(new ApiException.Detail(sheetName + "!" + toA1(rowIndex, columnIndex), reason));
        }
    }

    private String toA1(int rowIndex, int columnIndex) {
        return toColumnLetter(columnIndex) + (rowIndex + 1);
    }

    private String toColumnLetter(int columnIndex) {
        int col = columnIndex + 1;
        StringBuilder colName = new StringBuilder();
        while (col > 0) {
            int rem = (col - 1) % 26;
            colName.insert(0, (char) ('A' + rem));
            col = (col - 1) / 26;
        }
        return colName.toString();
    }
}
