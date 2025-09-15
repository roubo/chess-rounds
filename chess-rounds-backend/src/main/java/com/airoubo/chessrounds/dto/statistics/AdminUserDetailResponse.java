package com.airoubo.chessrounds.dto.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 管理员用户详细信息响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class AdminUserDetailResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户列表
     */
    @JsonProperty("users")
    private List<UserDetailDTO> users;
    
    /**
     * 总用户数
     */
    @JsonProperty("total_count")
    private Integer totalCount;
    
    /**
     * 当前页码
     */
    @JsonProperty("current_page")
    private Integer currentPage;
    
    /**
     * 每页大小
     */
    @JsonProperty("page_size")
    private Integer pageSize;
    
    /**
     * 总页数
     */
    @JsonProperty("total_pages")
    private Integer totalPages;
    
    /**
     * 查询时间戳
     */
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    public AdminUserDetailResponse() {
    }
    
    public AdminUserDetailResponse(List<UserDetailDTO> users, Integer totalCount,
                                 Integer currentPage, Integer pageSize,
                                 Integer totalPages, Instant timestamp) {
        this.users = users;
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.timestamp = timestamp;
    }
    
    public List<UserDetailDTO> getUsers() {
        return users;
    }
    
    public void setUsers(List<UserDetailDTO> users) {
        this.users = users;
    }
    
    public Integer getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    public Integer getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}