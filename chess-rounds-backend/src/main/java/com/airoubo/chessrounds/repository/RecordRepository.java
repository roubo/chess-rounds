package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.Record;
import com.airoubo.chessrounds.enums.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 记录Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    
    /**
     * 根据回合ID查询记录
     * 
     * @param roundId 回合ID
     * @return 记录列表
     */
    List<Record> findByRoundId(Long roundId);
    
    /**
     * 根据回合ID查询记录（按序号排序）
     * 
     * @param roundId 回合ID
     * @return 记录列表
     */
    List<Record> findByRoundIdOrderBySequenceNumber(Long roundId);
    
    /**
     * 根据记录者ID查询记录
     * 
     * @param recorderId 记录者ID
     * @param pageable 分页参数
     * @return 记录分页列表
     */
    Page<Record> findByRecorderId(Long recorderId, Pageable pageable);
    
    /**
     * 根据记录类型查询记录
     * 
     * @param recordType 记录类型
     * @param pageable 分页参数
     * @return 记录分页列表
     */
    Page<Record> findByRecordType(RecordType recordType, Pageable pageable);
    
    /**
     * 根据回合ID和记录类型查询记录
     * 
     * @param roundId 回合ID
     * @param recordType 记录类型
     * @return 记录列表
     */
    List<Record> findByRoundIdAndRecordType(Long roundId, RecordType recordType);
    
    /**
     * 查询指定时间范围内的记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 记录分页列表
     */
    Page<Record> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 统计回合记录数量
     * 
     * @param roundId 回合ID
     * @return 记录数量
     */
    Long countByRoundId(Long roundId);
    
    /**
     * 统计记录者的记录数量
     * 
     * @param recorderId 记录者ID
     * @return 记录数量
     */
    Long countByRecorderId(Long recorderId);
    
    /**
     * 统计回合总金额
     * 
     * @param roundId 回合ID
     * @return 总金额
     */
    @Query("SELECT SUM(r.amount) FROM Record r WHERE r.roundId = :roundId")
    BigDecimal sumAmountByRoundId(@Param("roundId") Long roundId);
    
    /**
     * 统计回合指定类型记录总金额
     * 
     * @param roundId 回合ID
     * @param recordType 记录类型
     * @return 总金额
     */
    @Query("SELECT SUM(r.amount) FROM Record r WHERE r.roundId = :roundId AND r.recordType = :recordType")
    BigDecimal sumAmountByRoundIdAndRecordType(@Param("roundId") Long roundId, @Param("recordType") RecordType recordType);
    
    /**
     * 查询用户参与的记录
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 记录分页列表
     */
    @Query(value = "SELECT * FROM records r WHERE JSON_CONTAINS(r.participants, CAST(:userId AS JSON), '$')", nativeQuery = true)
    Page<Record> findRecordsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 查询回合最大序号
     * 
     * @param roundId 回合ID
     * @return 最大序号
     */
    @Query("SELECT MAX(r.sequenceNumber) FROM Record r WHERE r.roundId = :roundId")
    Integer findMaxSequenceNumberByRoundId(@Param("roundId") Long roundId);
    
    /**
     * 查询用户在回合中的记录
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 记录列表
     */
    @Query(value = "SELECT * FROM records r WHERE r.round_id = :roundId AND JSON_CONTAINS(r.participants, CAST(:userId AS JSON), '$') ORDER BY r.sequence_number", nativeQuery = true)
    List<Record> findRecordsByRoundIdAndUserId(@Param("roundId") Long roundId, @Param("userId") Long userId);
    
    /**
     * 删除回合的所有记录
     * 
     * @param roundId 回合ID
     */
    void deleteByRoundId(Long roundId);
}