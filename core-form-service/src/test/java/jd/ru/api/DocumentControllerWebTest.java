package jd.ru.api;

import jd.ru.api.dto.ExportDocumentRequest;
import jd.ru.api.error.ApiException;
import jd.ru.service.DocumentApiService;
import jd.ru.service.ExportFileStore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentApiService documentApiService;

    @MockBean
    private ExportFileStore exportFileStore;

    @Test
    void exportReturnsDownloadUrlContract() throws Exception {
        UUID documentId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        Instant expiresAt = Instant.parse("2026-05-09T10:00:00Z");

        Mockito.when(documentApiService.exportDocument(eq(documentId), any(ExportDocumentRequest.class)))
                .thenReturn(new byte[]{1, 2, 3});
        Mockito.when(exportFileStore.save(any(byte[].class), eq("report.xlsx")))
                .thenReturn(new ExportFileStore.StoredRef(fileId, expiresAt));

        mockMvc.perform(post("/api/v1/documents/{documentId}/export", documentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "format": "xlsx",
                                  "mode": "fill_template",
                                  "fileName": "report.xlsx"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl").value("/api/v1/files/" + fileId))
                .andExpect(jsonPath("$.expiresAt").value(expiresAt.toString()))
                .andExpect(jsonPath("$.exportMeta.templatePreserved").value(true));
    }

    @Test
    void downloadReturnsStoredBinaryFile() throws Exception {
        UUID fileId = UUID.randomUUID();
        byte[] payload = new byte[]{10, 20, 30};
        Mockito.when(exportFileStore.get(fileId))
                .thenReturn(new ExportFileStore.StoredFile(payload, "report.xlsx", Instant.now().plusSeconds(300)));

        mockMvc.perform(get("/api/v1/files/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"report.xlsx\""))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(payload));
    }

    @Test
    void downloadReturnsNotFoundWhenFileMissing() throws Exception {
        UUID fileId = UUID.randomUUID();
        Mockito.when(exportFileStore.get(fileId))
                .thenThrow(new ApiException("NOT_FOUND", "Export file not found or expired"));

        mockMvc.perform(get("/api/v1/files/{fileId}", fileId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
