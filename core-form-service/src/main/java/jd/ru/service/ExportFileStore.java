package jd.ru.service;

import jd.ru.api.error.ApiException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExportFileStore {

    private static final Duration TTL = Duration.ofMinutes(15);
    private final Map<UUID, StoredFile> files = new ConcurrentHashMap<>();

    public StoredRef save(byte[] payload, String fileName) {
        cleanupExpired();
        UUID fileId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(TTL);
        files.put(fileId, new StoredFile(payload, fileName, expiresAt));
        return new StoredRef(fileId, expiresAt);
    }

    public StoredFile get(UUID fileId) {
        cleanupExpired();
        StoredFile file = files.get(fileId);
        if (file == null || file.expiresAt().isBefore(Instant.now())) {
            files.remove(fileId);
            throw new ApiException("NOT_FOUND", "Export file not found or expired");
        }
        return file;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        files.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    public record StoredRef(UUID fileId, Instant expiresAt) {
    }

    public record StoredFile(byte[] payload, String fileName, Instant expiresAt) {
    }
}
