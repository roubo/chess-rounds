package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.Participant;
import com.airoubo.chessrounds.enums.ParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 参与者Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    
    /**
     * 根据回合ID查询参与者
     * 
     * @param roundId 回合ID
     * @return 参与者列表
     */
    List<Participant> findByRoundId(Long roundId);
    
    /**
     * 根据用户ID查询参与者
     * 
     * @param userId 用户ID
     * @return 参与者列表
     */
    List<Participant> findByUserId(Long userId);
    
    /**
     * 根据回合ID和用户ID查询参与者
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 参与者信息
     */
    Optional<Participant> findByRoundIdAndUserId(Long roundId, Long userId);
    
    /**
     * 根据回合ID、用户ID和角色查询参与者
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @param role 角色
     * @return 参与者信息
     */
    Optional<Participant> findByRoundIdAndUserIdAndRole(Long roundId, Long userId, ParticipantRole role);
    
    /**
     * 根据回合ID和角色查询参与者
     * 
     * @param roundId 回合ID
     * @param role 参与者角色
     * @return 参与者列表
     */
    List<Participant> findByRoundIdAndRole(Long roundId, ParticipantRole role);
    
    /**
     * 根据回合ID和活跃状态查询参与者
     * 
     * @param roundId 回合ID
     * @param isActive 是否活跃
     * @return 参与者列表
     */
    List<Participant> findByRoundIdAndIsActive(Long roundId, Boolean isActive);
    
    /**
     * 根据回合ID、角色和活跃状态查询参与者
     * 
     * @param roundId 回合ID
     * @param role 参与者角色
     * @param isActive 是否活跃
     * @return 参与者列表
     */
    List<Participant> findByRoundIdAndRoleAndIsActive(Long roundId, ParticipantRole role, Boolean isActive);
    
    /**
     * 统计回合参与者数量
     * 
     * @param roundId 回合ID
     * @return 参与者数量
     */
    Long countByRoundId(Long roundId);
    
    /**
     * 统计回合活跃参与者数量
     * 
     * @param roundId 回合ID
     * @param isActive 是否活跃
     * @return 参与者数量
     */
    Long countByRoundIdAndIsActive(Long roundId, Boolean isActive);
    
    /**
     * 统计回合指定角色参与者数量
     * 
     * @param roundId 回合ID
     * @param role 参与者角色
     * @return 参与者数量
     */
    Long countByRoundIdAndRole(Long roundId, ParticipantRole role);
    
    /**
     * 根据用户ID统计参与的回合数量
     * 
     * @param userId 用户ID
     * @return 参与的回合数量
     */
    Long countByUserId(Long userId);
    
    /**
     * 根据用户ID查询参与的回合ID列表
     * 
     * @param userId 用户ID
     * @return 回合ID列表
     */
    @Query("SELECT DISTINCT p.roundId FROM Participant p WHERE p.userId = :userId AND p.isActive = true")
    List<Long> findRoundIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 检查用户是否已参与回合
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 是否已参与
     */
    boolean existsByRoundIdAndUserId(Long roundId, Long userId);
    
    /**
     * 查询回合中的玩家参与者（按座位号排序）
     * 
     * @param roundId 回合ID
     * @return 参与者列表
     */
    @Query("SELECT p FROM Participant p WHERE p.roundId = :roundId AND p.role = 'PLAYER' AND p.isActive = true ORDER BY p.seatNumber")
    List<Participant> findActivePlayersByRoundId(@Param("roundId") Long roundId);
    
    /**
     * 查询回合中的旁观者
     * 
     * @param roundId 回合ID
     * @return 参与者列表
     */
    @Query("SELECT p FROM Participant p WHERE p.roundId = :roundId AND p.role = 'SPECTATOR' AND p.isActive = true ORDER BY p.joinedAt")
    List<Participant> findActiveSpectatorsByRoundId(@Param("roundId") Long roundId);
    
    /**
     * 删除回合的所有参与者
     * 
     * @param roundId 回合ID
     */
    void deleteByRoundId(Long roundId);
}