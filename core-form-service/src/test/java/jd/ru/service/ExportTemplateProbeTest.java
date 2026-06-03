package jd.ru.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Диагностика: временно снять @Disabled и выполнить
 * mvn test -Dtest=ExportTemplateProbeTest
 */
class ExportTemplateProbeTest {

    private static InputStream openTemplate() throws IOException {
        InputStream fromClasspath = ExportTemplateProbeTest.class.getClassLoader().getResourceAsStream("export-template.xlsx");
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
    @Disabled("Диагностика шаблона при необходимости")
    void dumpFormLayouts() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        DataFormatter fmt = new DataFormatter();
        try (InputStream in = openTemplate()) {
            if (in == null) {
                return;
            }
            try (XSSFWorkbook wb = new XSSFWorkbook(in)) {
                for (int si = 0; si < wb.getNumberOfSheets(); si++) {
                    Sheet sheet = wb.getSheetAt(si);
                    out.println("=== " + sheet.getSheetName() + " ===");
                    for (int r = 10; r <= 25; r++) {
                        Row row = sheet.getRow(r);
                        if (row == null) {
                            out.println("Row " + (r + 1) + ": (empty)");
                            continue;
                        }
                        int last = row.getLastCellNum();
                        for (int c = 0; c <= Math.min(last + 5, 52); c++) {
                            Cell cell = row.getCell(c);
                            if (cell == null) {
                                continue;
                            }
                            String v = fmt.formatCellValue(cell).trim();
                            if (v.isEmpty()) {
                                continue;
                            }
                            String col = CellReference.convertNumToColString(c);
                            out.println("  R" + (r + 1) + col + " (idx " + c + ") = [" + v + "]");
                        }
                    }
                    out.println();
                }
            }
        }
        out.flush();
        String outText = sw.toString();
        System.out.println(outText);
        Files.writeString(Path.of("target/template-probe-dump.txt"), outText);
    }
}
