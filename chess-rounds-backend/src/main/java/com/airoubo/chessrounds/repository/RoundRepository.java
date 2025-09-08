package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.Round;
import com.airoubo.chessrounds.enums.RoundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 回合Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    
    /**
     * 根据回合码查找回合
     * 
     * @param roundCode 回合码
     * @return 回合信息
     */
    Optional<Round> findByRoundCode(String roundCode);
    
    /**
     * 根据创建者ID查询回合
     * 
     * @param creatorId 创建者ID
     * @param pageable 分页参数
     * @return 回合分页列表
     */
    Page<Round> findByCreatorId(Long creatorId, Pageable pageable);
    
    /**
     * 根据状态查询回合
     * 
     * @param status 回合状态
     * @param pageable 分页参数
     * @return 回合分页列表
     */
    Page<Round> findByStatus(RoundStatus status, Pageable pageable);
    
    /**
     * 根据创建者和状态查询回合
     * 
     * @param creatorId 创建者ID
     * @param status 回合状态
     * @return 回合列表
     */
    List<Round> findByCreatorIdAndStatus(Long creatorId, RoundStatus status);
    
    /**
     * 查询指定时间范围内的回合
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 回合分页列表
     */
    Page<Round> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 查询用户参与的回合
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 回合分页列表
     */
    @Query("SELECT r FROM Round r JOIN Participant p ON r.id = p.roundId WHERE p.userId = :userId ORDER BY r.createdAt DESC")
    Page<Round> findRoundsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 查询用户参与的指定状态回合
     * 
     * @param userId 用户ID
     * @param status 回合状态
     * @return 回合列表
     */
    @Query("SELECT r FROM Round r JOIN Participant p ON r.id = p.roundId WHERE p.userId = :userId AND r.status = :status")
    List<Round> findRoundsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") RoundStatus status);
    
    /**
     * 统计用户创建的回合数量
     * 
     * @param creatorId 创建者ID
     * @return 回合数量
     */
    Long countByCreatorId(Long creatorId);
    
    /**
     * 统计指定状态的回合数量
     * 
     * @param status 回合状态
     * @return 回合数量
     */
    Long countByStatus(RoundStatus status);
    
    /**
     * 检查回合码是否存在
     * 
     * @param roundCode 回合码
     * @return 是否存在
     */
    boolean existsByRoundCode(String roundCode);
    
    /**
     * 查询最近的活跃回合
     * 
     * @param status 回合状态
     * @param limit 限制数量
     * @return 回合列表
     */
    @Query(value = "SELECT * FROM rounds WHERE status = :status ORDER BY updated_at DESC LIMIT :limit", nativeQuery = true)
    List<Round> findRecentActiveRounds(@Param("status") String status, @Param("limit") int limit);
}