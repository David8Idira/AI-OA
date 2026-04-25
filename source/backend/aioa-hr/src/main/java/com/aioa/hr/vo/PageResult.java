package com.aioa.hr.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> {
    
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页
     */
    private Long current;
    
    /**
     * 页大小
     */
    private Long size;
    
    /**
     * 总页数
     */
    private Long pages;
    
    public PageResult() {
    }
    
    public PageResult(List<T> records, Long total, Long current, Long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = size == 0 ? 0 : (total + size - 1) / size;
    }
    
    public PageResult(List<T> records, Long total, Long current, Long size, Long pages) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = pages;
    }
}