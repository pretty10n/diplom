package jd.ru.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExportTemplateStructureTest {

    private static InputStream openTemplate() throws java.io.IOException {
        InputStream fromClasspath = ExportTemplateStructureTest.class.getClassLoader().getResourceAsStream("export-template.xlsx");
        if (fromClasspath != null) {
            return fromClasspath;
        }
        Path fallback = Path.of("../templates/export-template.xlsx").toAbsolutePath().normalize();
        if (Files.exists(fallback)) {
            return Files.newInputStream(fallback);
        }
        return null;
    }

    @Test
    void exportTemplate_hasExpectedSheetsAndAnchorCells() throws Exception {
        try (InputStream in = openTemplate()) {
            Assumptions.assumeTrue(in != null, "Добавьте export-template.xlsx в src/main/resources или templates/");
            try (Workbook wb = new XSSFWorkbook(in)) {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                names.add(wb.getSheetName(i));
            }

            Sheet form4 = wb.getSheet("Форма 4");
            if (form4 == null) {
                form4 = wb.getSheet("Ф.4");
            }
            assertNotNull(form4, "Нужен лист «Форма 4» или «Ф.4». Листы: " + names);

            Sheet form5 = wb.getSheet("Форма 5");
            if (form5 == null) {
                form5 = wb.getSheet("Ф.5");
            }
            assertNotNull(form5, "Нужен лист «Форма 5» или «Ф.5». Листы: " + names);

            Sheet form6 = wb.getSheet("Форма 6");
            if (form6 == null) {
                form6 = wb.getSheet("Ф.6");
            }
            assertNotNull(form6, "Нужен лист «Форма 6» или «Ф.6». Листы: " + names);

            assertCellPresent(form4, 4, 23, "Ф.4/Форма 4: наименование и шифр ТРУ (X5)");
            assertRowPresent(form4, 6, "Ф.4: строка этапа (B7)");
            assertRowPresent(form4, 8, "Ф.4: строка годов (AC9/AS9)");
            assertRowHasCells(form4, 15, 0, 2, "Ф.4: первая строка данных раздела 1 (строка 16)");
            assertRowHasCells(form4, 20, 0, 2, "Ф.4: первая строка данных раздела 2 под «Вспомогательные материалы» (строка 21)");
            assertRowHasCells(form4, 26, 0, 2, "Ф.4: первая строка данных раздела 3 (строка 27)");

            assertCellPresent(form5, 4, 19, "Ф.5: наименование и шифр ТРУ (T5)");
            assertRowPresent(form5, 6, "Ф.5: строка этапа (B7)");
            assertRowPresent(form5, 8, "Ф.5: строка годов (Y9/AQ9)");
            assertRowHasCells(form5, 15, 0, 2, "Ф.5: первая строка данных");

            assertCellPresent(form6, 4, 17, "Ф.6: наименование и шифр ТРУ (R5)");
            assertRowPresent(form6, 6, "Ф.6: строка этапа (B7)");
            assertRowPresent(form6, 8, "Ф.6: строка годов (X9/AQ9)");
            assertRowHasCells(form6, 14, 0, 2, "Ф.6: первая строка данных (строка 15)");
            }
        }
    }

    private static void assertCellPresent(Sheet sheet, int row0, int col0, String what) {
        Row row = sheet.getRow(row0);
        assertNotNull(row, what + " — нет строки Excel " + (row0 + 1));
        Cell cell = row.getCell(col0);
        assertNotNull(cell, what + " — нет ячейки " + row0 + "," + col0);
    }

    private static void assertRowPresent(Sheet sheet, int row0, String what) {
        assertNotNull(sheet.getRow(row0), what + " — нет строки Excel " + (row0 + 1));
    }

    private static void assertRowHasCells(Sheet sheet, int row0, int colA, int colB, String what) {
        Row row = sheet.getRow(row0);
        assertNotNull(row, what + " — нет строки " + (row0 + 1));
        assertNotNull(row.getCell(colA), what + " — нет col1 (A)");
        assertNotNull(row.getCell(colB), what + " — нет col2 (B)");
    }
}
