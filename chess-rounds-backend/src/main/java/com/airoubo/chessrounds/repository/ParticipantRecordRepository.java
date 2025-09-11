package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.ParticipantRecord;
import com.airoubo.chessrounds.enums.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * 参与者记录数据访问层
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface ParticipantRecordRepository extends JpaRepository<ParticipantRecord, Long> {
    
    /**
     * 根据记录ID查询所有参与者记录
     * 
     * @param recordId 记录ID
     * @return 参与者记录列表
     */
    List<ParticipantRecord> findByRecordId(Long recordId);
    
    /**
     * 根据回合ID查询所有参与者记录
     * 
     * @param roundId 回合ID
     * @return 参与者记录列表
     */
    List<ParticipantRecord> findByRoundId(Long roundId);
    
    /**
     * 根据用户ID查询所有参与者记录
     * 
     * @param userId 用户ID
     * @return 参与者记录列表
     */
    List<ParticipantRecord> findByUserId(Long userId);
    
    /**
     * 根据用户ID查询已结束回合的参与者记录
     * 
     * @param userId 用户ID
     * @return 参与者记录列表
     */
    @Query("SELECT pr FROM ParticipantRecord pr JOIN FETCH pr.round r WHERE pr.userId = :userId AND r.status = :status")
    List<ParticipantRecord> findByUserIdAndFinishedRounds(@Param("userId") Long userId, @Param("status") RoundStatus status);
    
    /**
     * 根据回合ID和用户ID查询参与者记录
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 参与者记录列表
     */
    List<ParticipantRecord> findByRoundIdAndUserId(Long roundId, Long userId);
    
    /**
     * 根据记录ID和用户ID查询参与者记录
     * 
     * @param recordId 记录ID
     * @param userId 用户ID
     * @return 参与者记录
     */
    ParticipantRecord findByRecordIdAndUserId(Long recordId, Long userId);
    
    /**
     * 根据回合ID查询所有赢家记录
     * 
     * @param roundId 回合ID
     * @return 赢家记录列表
     */
    List<ParticipantRecord> findByRoundIdAndIsWinnerTrue(Long roundId);
    
    /**
     * 统计用户在回合中的胜利次数
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 胜利次数
     */
    @Query("SELECT COUNT(pr) FROM ParticipantRecord pr WHERE pr.roundId = :roundId AND pr.userId = :userId AND pr.isWinner = true")
    Long countWinsByRoundIdAndUserId(@Param("roundId") Long roundId, @Param("userId") Long userId);
    
    /**
     * 统计用户在回合中的总局数
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 总局数
     */
    Long countByRoundIdAndUserId(Long roundId, Long userId);
    
    /**
     * 删除指定记录的所有参与者记录
     * 
     * @param recordId 记录ID
     */
    void deleteByRecordId(Long recordId);
    
    /**
     * 删除指定回合的所有参与者记录
     * 
     * @param roundId 回合ID
     */
    void deleteByRoundId(Long roundId);
}