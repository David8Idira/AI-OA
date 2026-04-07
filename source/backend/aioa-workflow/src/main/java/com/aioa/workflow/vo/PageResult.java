package com.aioa.workflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Paginated result wrapper
 */
@Data
@Schema(name = "PageResult", description = "Paginated result")
public class PageResult<T> {

    @Schema(description = "Total records count")
    private Long total;

    @Schema(description = "Current page number")
    private Integer pageNum;

    @Schema(description = "Page size")
    private Integer pageSize;

    @Schema(description = "Total pages")
    private Integer totalPages;

    @Schema(description = "Has next page")
    private Boolean hasNext;

    @Schema(description = "Has previous page")
    private Boolean hasPrev;

    @Schema(description = "Result list")
    private List<T> records;

    public PageResult() {}

    public PageResult(Long total, Integer pageNum, Integer pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
        this.hasNext = pageNum < totalPages;
        this.hasPrev = pageNum > 1;
    }

    public static <T> PageResult<T> of(Long total, Integer pageNum, Integer pageSize, List<T> records) {
        return new PageResult<>(total, pageNum, pageSize, records);
    }
}
