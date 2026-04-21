package com.aioa.report.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_definition")
public class ReportDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "report_key", unique = true, nullable = false)
    private String reportKey;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sql_template", columnDefinition = "TEXT")
    private String sqlTemplate;

    @Column(name = "chart_config", columnDefinition = "TEXT")
    private String chartConfig;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getReportKey() { return reportKey; }
    public void setReportKey(String reportKey) { this.reportKey = reportKey; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSqlTemplate() { return sqlTemplate; }
    public void setSqlTemplate(String sqlTemplate) { this.sqlTemplate = sqlTemplate; }
    public String getChartConfig() { return chartConfig; }
    public void setChartConfig(String chartConfig) { this.chartConfig = chartConfig; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
