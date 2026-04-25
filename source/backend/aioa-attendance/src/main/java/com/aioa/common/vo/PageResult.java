package com.aioa.common.vo;

import lombok.Data;

import java.util.List;

/**
 * Page result wrapper
 */
@Data
public class PageResult<T> {
    
    private List<T> records;
    private long total;
    private int pageNum;
    private int pageSize;
    private int totalPages;
    
    public PageResult() {
    }
    
    public PageResult(List<T> records, long total) {
        this.records = records;
        this.total = total;
    }
    
    public PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
    
    public static <T> PageResult<T> success(List<T> records, long total) {
        return new PageResult<>(records, total);
    }
    
    public static <T> PageResult<T> success(List<T> records, long total, int pageNum, int pageSize) {
        return new PageResult<>(records, total, pageNum, pageSize);
    }
}