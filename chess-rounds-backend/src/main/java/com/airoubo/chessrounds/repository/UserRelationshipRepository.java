package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.UserRelationship;
import com.airoubo.chessrounds.enums.RelationshipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户关系Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface UserRelationshipRepository extends JpaRepository<UserRelationship, Long> {
    
    /**
     * 根据用户ID查询关系
     * 
     * @param userId 用户ID
     * @return 关系列表
     */
    List<UserRelationship> findByUserId(Long userId);
    
    /**
     * 根据用户ID和关系类型查询关系
     * 
     * @param userId 用户ID
     * @param relationshipType 关系类型
     * @return 关系列表
     */
    List<UserRelationship> findByUserIdAndRelationshipType(Long userId, RelationshipType relationshipType);
    
    /**
     * 根据用户ID、目标用户ID和关系类型查询关系
     * 
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @param relationshipType 关系类型
     * @return 关系信息
     */
    Optional<UserRelationship> findByUserIdAndTargetUserIdAndRelationshipType(Long userId, Long targetUserId, RelationshipType relationshipType);
    
    /**
     * 根据目标用户ID查询关系
     * 
     * @param targetUserId 目标用户ID
     * @return 关系列表
     */
    List<UserRelationship> findByTargetUserId(Long targetUserId);
    
    /**
     * 查询用户的合伙人关系（分页）
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 关系分页列表
     */
    Page<UserRelationship> findByUserIdAndRelationshipType(Long userId, RelationshipType relationshipType, Pageable pageable);
    
    /**
     * 查询用户最常合作的伙伴
     * 
     * @param userId 用户ID
     * @param relationshipType 关系类型
     * @param limit 限制数量
     * @return 关系列表
     */
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.userId = :userId AND ur.relationshipType = :relationshipType ORDER BY ur.totalRounds DESC")
    List<UserRelationship> findTopPartnersByUserId(@Param("userId") Long userId, @Param("relationshipType") RelationshipType relationshipType, Pageable pageable);
    
    /**
     * 查询用户胜率最高的关系
     * 
     * @param userId 用户ID
     * @param relationshipType 关系类型
     * @param minRounds 最少回合数
     * @return 关系列表
     */
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.userId = :userId AND ur.relationshipType = :relationshipType AND ur.totalRounds >= :minRounds ORDER BY (ur.winRounds * 1.0 / ur.totalRounds) DESC")
    List<UserRelationship> findHighestWinRateRelationships(@Param("userId") Long userId, @Param("relationshipType") RelationshipType relationshipType, @Param("minRounds") Integer minRounds);
    
    /**
     * 统计用户的关系数量
     * 
     * @param userId 用户ID
     * @param relationshipType 关系类型
     * @return 关系数量
     */
    Long countByUserIdAndRelationshipType(Long userId, RelationshipType relationshipType);
    
    /**
     * 统计用户总游戏回合数
     * 
     * @param userId 用户ID
     * @return 总回合数
     */
    @Query("SELECT SUM(ur.totalRounds) FROM UserRelationship ur WHERE ur.userId = :userId")
    Long sumTotalRoundsByUserId(@Param("userId") Long userId);
    
    /**
     * 统计用户总胜利回合数
     * 
     * @param userId 用户ID
     * @return 总胜利回合数
     */
    @Query("SELECT SUM(ur.winRounds) FROM UserRelationship ur WHERE ur.userId = :userId")
    Long sumWinRoundsByUserId(@Param("userId") Long userId);
    
    /**
     * 统计用户总金额
     * 
     * @param userId 用户ID
     * @return 总金额
     */
    @Query("SELECT SUM(ur.totalAmount) FROM UserRelationship ur WHERE ur.userId = :userId")
    BigDecimal sumTotalAmountByUserId(@Param("userId") Long userId);
    
    /**
     * 统计用户总胜利金额
     * 
     * @param userId 用户ID
     * @return 总胜利金额
     */
    @Query("SELECT SUM(ur.winAmount) FROM UserRelationship ur WHERE ur.userId = :userId")
    BigDecimal sumWinAmountByUserId(@Param("userId") Long userId);
    
    /**
     * 检查关系是否存在
     * 
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @param relationshipType 关系类型
     * @return 是否存在
     */
    boolean existsByUserIdAndTargetUserIdAndRelationshipType(Long userId, Long targetUserId, RelationshipType relationshipType);
    
    /**
     * 查询互相关系（双向关系）
     * 
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param relationshipType 关系类型
     * @return 关系列表
     */
    @Query("SELECT ur FROM UserRelationship ur WHERE ((ur.userId = :userId1 AND ur.targetUserId = :userId2) OR (ur.userId = :userId2 AND ur.targetUserId = :userId1)) AND ur.relationshipType = :relationshipType")
    List<UserRelationship> findMutualRelationships(@Param("userId1") Long userId1, @Param("userId2") Long userId2, @Param("relationshipType") RelationshipType relationshipType);
}