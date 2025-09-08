package com.airoubo.chessrounds.service;

import com.airoubo.chessrounds.dto.record.CreateRecordRequest;
import com.airoubo.chessrounds.dto.record.RecordInfoResponse;
import com.airoubo.chessrounds.entity.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 记录服务接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public interface RecordService {
    
    /**
     * 创建记录
     * 
     * @param createRequest 创建请求
     * @param recorderId 记录者ID
     * @return 记录信息
     */
    RecordInfoResponse createRecord(CreateRecordRequest createRequest, Long recorderId);
    
    /**
     * 根据记录ID获取记录信息
     * 
     * @param recordId 记录ID
     * @return 记录信息
     */
    Optional<RecordInfoResponse> getRecordById(Long recordId);
    
    /**
     * 获取回合的所有记录
     * 
     * @param roundId 回合ID
     * @param pageable 分页参数
     * @return 记录列表
     */
    Page<RecordInfoResponse> getRoundRecords(Long roundId, Pageable pageable);
    
    /**
     * 获取用户的记录列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 记录列表
     */
    Page<RecordInfoResponse> getUserRecords(Long userId, Pageable pageable);
    
    /**
     * 获取用户在特定回合的记录
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 记录列表
     */
    Page<RecordInfoResponse> getUserRoundRecords(Long roundId, Long userId, Pageable pageable);
    
    /**
     * 根据记录类型获取记录
     * 
     * @param roundId 回合ID
     * @param recordType 记录类型
     * @param pageable 分页参数
     * @return 记录列表
     */
    Page<RecordInfoResponse> getRecordsByType(Long roundId, String recordType, Pageable pageable);
    
    /**
     * 获取时间范围内的记录
     * 
     * @param roundId 回合ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 记录列表
     */
    Page<RecordInfoResponse> getRecordsByTimeRange(Long roundId, LocalDateTime startTime, 
                                                   LocalDateTime endTime, Pageable pageable);
    
    /**
     * 更新记录
     * 
     * @param recordId 记录ID
     * @param updateRequest 更新请求
     * @param userId 操作用户ID（必须是记录者或回合创建者）
     * @return 更新后的记录信息
     */
    RecordInfoResponse updateRecord(Long recordId, CreateRecordRequest updateRequest, Long userId);
    
    /**
     * 删除记录
     * 
     * @param recordId 记录ID
     * @param userId 操作用户ID（必须是记录者或回合创建者）
     */
    void deleteRecord(Long recordId, Long userId);
    
    /**
     * 批量删除记录
     * 
     * @param recordIds 记录ID列表
     * @param userId 操作用户ID
     */
    void deleteRecords(List<Long> recordIds, Long userId);
    
    /**
     * 检查用户是否有权限操作记录
     * 
     * @param recordId 记录ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermission(Long recordId, Long userId);
    
    /**
     * 统计回合记录数量
     * 
     * @param roundId 回合ID
     * @param recordType 记录类型（可选）
     * @param userId 用户ID（可选）
     * @return 记录数量
     */
    Long countRoundRecords(Long roundId, String recordType, Long userId);
    
    /**
     * 统计用户记录数量
     * 
     * @param userId 用户ID
     * @param recordType 记录类型（可选）
     * @param since 时间范围（可选）
     * @return 记录数量
     */
    Long countUserRecords(Long userId, String recordType, LocalDateTime since);
    
    /**
     * 计算回合总金额
     * 
     * @param roundId 回合ID
     * @return 总金额
     */
    Long calculateRoundTotalAmount(Long roundId);
    
    /**
     * 计算用户在回合中的总金额
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 用户总金额
     */
    Long calculateUserRoundAmount(Long roundId, Long userId);
    
    /**
     * 获取回合最新记录
     * 
     * @param roundId 回合ID
     * @param limit 限制数量
     * @return 最新记录列表
     */
    List<RecordInfoResponse> getLatestRoundRecords(Long roundId, int limit);
    
    /**
     * 获取用户最新记录
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最新记录列表
     */
    List<RecordInfoResponse> getLatestUserRecords(Long userId, int limit);
    
    /**
     * 搜索记录（按备注模糊查询）
     * 
     * @param roundId 回合ID
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 记录列表
     */
    Page<RecordInfoResponse> searchRecords(Long roundId, String keyword, Pageable pageable);
    
    /**
     * 获取记录统计信息
     * 
     * @param roundId 回合ID
     * @param userId 用户ID（可选）
     * @return 统计信息
     */
    RecordStatistics getRecordStatistics(Long roundId, Long userId);
    
    /**
     * 获取记录实体（内部使用）
     * 
     * @param recordId 记录ID
     * @return 记录实体
     */
    Optional<Record> getRecordEntity(Long recordId);
    
    /**
     * 保存记录实体（内部使用）
     * 
     * @param record 记录实体
     * @return 保存后的记录实体
     */
    Record saveRecord(Record record);
    
    /**
     * 记录统计信息内部类
     */
    class RecordStatistics {
        private Long totalRecords;
        private Long totalAmount;
        private Long winAmount;
        private Long loseAmount;
        private Double avgAmount;
        private Long maxSingleWin;
        private Long maxSingleLose;
        
        public RecordStatistics() {
        }
        
        public RecordStatistics(Long totalRecords, Long totalAmount, Long winAmount, Long loseAmount) {
            this.totalRecords = totalRecords;
            this.totalAmount = totalAmount;
            this.winAmount = winAmount;
            this.loseAmount = loseAmount;
            this.avgAmount = totalRecords > 0 ? (double) totalAmount / totalRecords : 0.0;
        }
        
        // Getter and Setter methods
        public Long getTotalRecords() {
            return totalRecords;
        }
        
        public void setTotalRecords(Long totalRecords) {
            this.totalRecords = totalRecords;
        }
        
        public Long getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(Long totalAmount) {
            this.totalAmount = totalAmount;
        }
        
        public Long getWinAmount() {
            return winAmount;
        }
        
        public void setWinAmount(Long winAmount) {
            this.winAmount = winAmount;
        }
        
        public Long getLoseAmount() {
            return loseAmount;
        }
        
        public void setLoseAmount(Long loseAmount) {
            this.loseAmount = loseAmount;
        }
        
        public Double getAvgAmount() {
            return avgAmount;
        }
        
        public void setAvgAmount(Double avgAmount) {
            this.avgAmount = avgAmount;
        }
        
        public Long getMaxSingleWin() {
            return maxSingleWin;
        }
        
        public void setMaxSingleWin(Long maxSingleWin) {
            this.maxSingleWin = maxSingleWin;
        }
        
        public Long getMaxSingleLose() {
            return maxSingleLose;
        }
        
        public void setMaxSingleLose(Long maxSingleLose) {
            this.maxSingleLose = maxSingleLose;
        }
    }
}