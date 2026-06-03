package jd.ru.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "document_common_info")
public class DocumentCommonInfoEntity {

    @Id
    private UUID documentId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

    @Column(nullable = false, length = 512)
    private String truName;

    @Column(nullable = false, length = 128)
    private String truCode;

    @Column(nullable = false, length = 128)
    private String stage;

    @Column(nullable = false)
    private Integer reportYear;

    @Column(nullable = false)
    private Integer planYear;

    public UUID getDocumentId() {
        return documentId;
    }

    public DocumentEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentEntity document) {
        this.document = document;
    }

    public String getTruName() {
        return truName;
    }

    public void setTruName(String truName) {
        this.truName = truName;
    }

    public String getTruCode() {
        return truCode;
    }

    public void setTruCode(String truCode) {
        this.truCode = truCode;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Integer getReportYear() {
        return reportYear;
    }

    public void setReportYear(Integer reportYear) {
        this.reportYear = reportYear;
    }

    public Integer getPlanYear() {
        return planYear;
    }

    public void setPlanYear(Integer planYear) {
        this.planYear = planYear;
    }
}
