package jd.ru.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reference_material")
public class ReferenceMaterialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 512)
    private String name;

    @Column(length = 128)
    private String okpdCode;

    @Column(length = 128)
    private String ekpsCode;

    @Column(length = 128)
    private String fnn;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOkpdCode() {
        return okpdCode;
    }

    public void setOkpdCode(String okpdCode) {
        this.okpdCode = okpdCode;
    }

    public String getEkpsCode() {
        return ekpsCode;
    }

    public void setEkpsCode(String ekpsCode) {
        this.ekpsCode = ekpsCode;
    }

    public String getFnn() {
        return fnn;
    }

    public void setFnn(String fnn) {
        this.fnn = fnn;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
