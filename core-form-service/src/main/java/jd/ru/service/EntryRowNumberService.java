package jd.ru.service;

import jd.ru.domain.DocumentEntryEntity;
import jd.ru.repository.DocumentEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EntryRowNumberService {

    private final DocumentEntryRepository documentEntryRepository;

    public EntryRowNumberService(DocumentEntryRepository documentEntryRepository) {
        this.documentEntryRepository = documentEntryRepository;
    }

    @Transactional
    public void renumberRows(UUID documentId, String sectionKey) {
        List<DocumentEntryEntity> entries = documentEntryRepository.findByDocumentAndSectionOrdered(documentId, sectionKey);
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRowNo(i + 1);
        }
    }
}
