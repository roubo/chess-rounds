package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.Circle;
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
 * 圈子Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface CircleRepository extends JpaRepository<Circle, Long> {
    
    /**
     * 根据圈子代码查找圈子
     * 
     * @param circleCode 圈子代码
     * @return 圈子信息
     */
    Optional<Circle> findByCircleCode(String circleCode);
    
    /**
     * 根据加入代码查找圈子
     * 
     * @param joinCode 加入代码
     * @return 圈子信息
     */
    Optional<Circle> findByJoinCode(String joinCode);
    
    /**
     * 根据创建者ID查找圈子
     * 
     * @param creatorId 创建者ID
     * @return 圈子列表
     */
    List<Circle> findByCreatorId(Long creatorId);
    
    /**
     * 根据状态查询圈子
     * 
     * @param status 圈子状态
     * @param pageable 分页参数
     * @return 圈子分页列表
     */
    Page<Circle> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据名称模糊查询圈子
     * 
     * @param name 圈子名称关键词
     * @param status 圈子状态
     * @param pageable 分页参数
     * @return 圈子分页列表
     */
    Page<Circle> findByNameContainingIgnoreCaseAndStatus(String name, Integer status, Pageable pageable);
    
    /**
     * 查询公开的圈子
     * 
     * @param isPublic 是否公开
     * @param status 圈子状态
     * @param pageable 分页参数
     * @return 圈子分页列表
     */
    Page<Circle> findByIsPublicAndStatus(Boolean isPublic, Integer status, Pageable pageable);
    
    /**
     * 根据最后活动时间查询活跃圈子
     * 
     * @param lastActivityTime 最后活动时间
     * @param status 圈子状态
     * @param pageable 分页参数
     * @return 圈子分页列表
     */
    Page<Circle> findByLastActivityAtAfterAndStatus(LocalDateTime lastActivityTime, Integer status, Pageable pageable);
    
    /**
     * 检查圈子代码是否存在
     * 
     * @param circleCode 圈子代码
     * @return 是否存在
     */
    boolean existsByCircleCode(String circleCode);
    
    /**
     * 统计用户创建的圈子数量
     * 
     * @param creatorId 创建者ID
     * @param status 圈子状态
     * @return 圈子数量
     */
    Long countByCreatorIdAndStatus(Long creatorId, Integer status);
    
    /**
     * 查询热门圈子（按成员数排序）
     * 
     * @param status 圈子状态
     * @param pageable 分页参数
     * @return 圈子分页列表
     */
    @Query("SELECT c FROM Circle c WHERE c.status = :status ORDER BY c.currentMembers DESC, c.lastActivityAt DESC")
    Page<Circle> findPopularCircles(@Param("status") Integer status, Pageable pageable);
    
    /**
     * 查询最新圈子
     * 
     * @param status 圈子状态
     * @param pageable 分页参数
     * @return 圈子分页列表
     */
    @Query("SELECT c FROM Circle c WHERE c.status = :status ORDER BY c.createdAt DESC")
    Page<Circle> findLatestCircles(@Param("status") Integer status, Pageable pageable);
    
    /**
     * 更新圈子成员数量
     * 
     * @param circleId 圈子ID
     * @param memberCount 成员数量
     */
    @Query("UPDATE Circle c SET c.currentMembers = :memberCount WHERE c.id = :circleId")
    void updateMemberCount(@Param("circleId") Long circleId, @Param("memberCount") Integer memberCount);
    
    /**
     * 更新圈子最后活动时间
     * 
     * @param circleId 圈子ID
     * @param lastActivityAt 最后活动时间
     */
    @Query("UPDATE Circle c SET c.lastActivityAt = :lastActivityAt WHERE c.id = :circleId")
    void updateLastActivityAt(@Param("circleId") Long circleId, @Param("lastActivityAt") LocalDateTime lastActivityAt);
}