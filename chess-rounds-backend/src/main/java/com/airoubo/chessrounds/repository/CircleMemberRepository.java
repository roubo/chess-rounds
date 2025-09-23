package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.CircleMember;
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
 * 圈子成员Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface CircleMemberRepository extends JpaRepository<CircleMember, Long> {
    
    /**
     * 根据圈子ID和用户ID查找成员
     * 
     * @param circleId 圈子ID
     * @param userId 用户ID
     * @return 成员信息
     */
    Optional<CircleMember> findByCircleIdAndUserId(Long circleId, Long userId);
    
    /**
     * 根据圈子ID查找所有成员
     * 
     * @param circleId 圈子ID
     * @param status 成员状态
     * @param pageable 分页参数
     * @return 成员分页列表
     */
    Page<CircleMember> findByCircleIdAndStatus(Long circleId, Integer status, Pageable pageable);
    
    /**
     * 根据用户ID查找所有加入的圈子
     * 
     * @param userId 用户ID
     * @param status 成员状态
     * @param pageable 分页参数
     * @return 成员分页列表
     */
    Page<CircleMember> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);
    
    /**
     * 根据圈子ID和角色查找成员
     * 
     * @param circleId 圈子ID
     * @param role 角色
     * @param status 成员状态
     * @return 成员列表
     */
    List<CircleMember> findByCircleIdAndRoleAndStatus(Long circleId, Integer role, Integer status);
    
    /**
     * 统计圈子成员数量
     * 
     * @param circleId 圈子ID
     * @param status 成员状态
     * @return 成员数量
     */
    Long countByCircleIdAndStatus(Long circleId, Integer status);
    
    /**
     * 统计用户加入的圈子数量
     * 
     * @param userId 用户ID
     * @param status 成员状态
     * @return 圈子数量
     */
    Long countByUserIdAndStatus(Long userId, Integer status);
    
    /**
     * 检查用户是否已加入圈子
     * 
     * @param circleId 圈子ID
     * @param userId 用户ID
     * @param status 成员状态
     * @return 是否已加入
     */
    boolean existsByCircleIdAndUserIdAndStatus(Long circleId, Long userId, Integer status);
    
    /**
     * 查找圈子管理员
     * 
     * @param circleId 圈子ID
     * @param status 成员状态
     * @return 管理员列表
     */
    @Query("SELECT cm FROM CircleMember cm WHERE cm.circleId = :circleId AND cm.role >= 1 AND cm.status = :status")
    List<CircleMember> findCircleAdmins(@Param("circleId") Long circleId, @Param("status") Integer status);
    
    /**
     * 查找圈子创建者
     * 
     * @param circleId 圈子ID
     * @param status 成员状态
     * @return 创建者信息
     */
    @Query("SELECT cm FROM CircleMember cm WHERE cm.circleId = :circleId AND cm.role = 2 AND cm.status = :status")
    Optional<CircleMember> findCircleCreator(@Param("circleId") Long circleId, @Param("status") Integer status);
    
    /**
     * 查找最近加入的成员
     * 
     * @param circleId 圈子ID
     * @param status 成员状态
     * @param pageable 分页参数
     * @return 成员分页列表
     */
    @Query("SELECT cm FROM CircleMember cm WHERE cm.circleId = :circleId AND cm.status = :status ORDER BY cm.joinedAt DESC")
    Page<CircleMember> findRecentMembers(@Param("circleId") Long circleId, @Param("status") Integer status, Pageable pageable);
    
    /**
     * 查找活跃成员（最近有活动的）
     * 
     * @param circleId 圈子ID
     * @param status 成员状态
     * @param afterTime 时间点
     * @param pageable 分页参数
     * @return 成员分页列表
     */
    @Query("SELECT cm FROM CircleMember cm WHERE cm.circleId = :circleId AND cm.status = :status AND cm.updatedAt >= :afterTime ORDER BY cm.updatedAt DESC")
    Page<CircleMember> findActiveMembers(@Param("circleId") Long circleId, @Param("status") Integer status, @Param("afterTime") LocalDateTime afterTime, Pageable pageable);
    
    /**
     * 根据昵称搜索圈子成员
     * 
     * @param circleId 圈子ID
     * @param nickname 昵称关键词
     * @param status 成员状态
     * @param pageable 分页参数
     * @return 成员分页列表
     */
    @Query("SELECT cm FROM CircleMember cm WHERE cm.circleId = :circleId AND cm.status = :status AND cm.nickname LIKE %:nickname%")
    Page<CircleMember> searchMembersByNickname(@Param("circleId") Long circleId, @Param("nickname") String nickname, @Param("status") Integer status, Pageable pageable);
    
    /**
     * 删除圈子所有成员（软删除）
     * 
     * @param circleId 圈子ID
     * @param leftAt 退出时间
     */
    @Query("UPDATE CircleMember cm SET cm.status = 0, cm.leftAt = :leftAt WHERE cm.circleId = :circleId AND cm.status = 1")
    void softDeleteAllMembers(@Param("circleId") Long circleId, @Param("leftAt") LocalDateTime leftAt);
}