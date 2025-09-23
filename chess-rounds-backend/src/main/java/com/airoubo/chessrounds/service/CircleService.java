package com.airoubo.chessrounds.service;

import com.airoubo.chessrounds.dto.circle.*;
import com.airoubo.chessrounds.entity.Circle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 圈子服务接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public interface CircleService {
    
    /**
     * 创建圈子
     * 
     * @param userId 创建者用户ID
     * @param request 创建圈子请求
     * @return 圈子信息响应
     */
    CircleInfoResponse createCircle(Long userId, CreateCircleRequest request);
    
    /**
     * 加入圈子
     * 
     * @param userId 用户ID
     * @param request 加入圈子请求
     * @return 圈子信息响应
     */
    CircleInfoResponse joinCircle(Long userId, JoinCircleRequest request);
    
    /**
     * 通过加入码加入圈子
     * 
     * @param userId 用户ID
     * @param request 通过加入码加入圈子请求
     * @return 圈子信息响应
     */
    CircleInfoResponse joinCircleByCode(Long userId, JoinCircleByCodeRequest request);
    
    /**
     * 退出圈子
     * 
     * @param userId 用户ID
     * @param circleId 圈子ID
     */
    void leaveCircle(Long userId, Long circleId);
    
    /**
     * 获取用户的圈子列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 圈子信息列表
     */
    Page<CircleInfoResponse> getUserCircles(Long userId, Pageable pageable);
    
    /**
     * 获取圈子详情
     * 
     * @param circleId 圈子ID
     * @param userId 当前用户ID（可选）
     * @return 圈子信息响应
     */
    Optional<CircleInfoResponse> getCircleInfo(Long circleId, Long userId);
    
    /**
     * 根据圈子代码获取圈子详情
     * 
     * @param circleCode 圈子代码
     * @param userId 当前用户ID（可选）
     * @return 圈子信息响应
     */
    Optional<CircleInfoResponse> getCircleInfoByCode(String circleCode, Long userId);
    
    /**
     * 更新圈子信息
     * 
     * @param userId 用户ID
     * @param circleId 圈子ID
     * @param request 更新圈子请求
     * @return 圈子信息响应
     */
    CircleInfoResponse updateCircle(Long userId, Long circleId, UpdateCircleRequest request);
    
    /**
     * 解散圈子
     * 
     * @param userId 用户ID
     * @param circleId 圈子ID
     */
    void dissolveCircle(Long userId, Long circleId);
    
    /**
     * 获取圈子成员列表
     * 
     * @param circleId 圈子ID
     * @param userId 当前用户ID
     * @param pageable 分页参数
     * @return 圈子成员列表
     */
    Page<CircleMemberResponse> getCircleMembers(Long circleId, Long userId, Pageable pageable);
    
    /**
     * 移除圈子成员
     * 
     * @param operatorId 操作者用户ID
     * @param circleId 圈子ID
     * @param memberId 被移除的成员ID
     */
    void removeMember(Long operatorId, Long circleId, Long memberId);
    
    /**
     * 设置圈子管理员
     * 
     * @param operatorId 操作者用户ID
     * @param circleId 圈子ID
     * @param memberId 成员ID
     * @param isAdmin 是否设为管理员
     */
    void setMemberAdmin(Long operatorId, Long circleId, Long memberId, Boolean isAdmin);
    
    /**
     * 获取圈子排行榜
     * 
     * @param circleId 圈子ID
     * @param userId 当前用户ID
     * @param pageable 分页参数
     * @return 排行榜列表
     */
    Page<CircleLeaderboardResponse> getCircleLeaderboard(Long circleId, Long userId, Pageable pageable);
    
    /**
     * 刷新圈子排行榜
     * 
     * @param circleId 圈子ID
     * @param userId 用户ID
     */
    void refreshCircleLeaderboard(Long circleId, Long userId);
    
    /**
     * 更新用户在圈子中的排行榜数据
     * 
     * @param circleId 圈子ID
     * @param userId 用户ID
     */
    void updateUserLeaderboardData(Long circleId, Long userId);
    
    /**
     * 搜索公开圈子
     * 
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 圈子信息列表
     */
    Page<CircleInfoResponse> searchPublicCircles(String keyword, Pageable pageable);
    
    /**
     * 获取热门圈子
     * 
     * @param pageable 分页参数
     * @return 圈子信息列表
     */
    Page<CircleInfoResponse> getPopularCircles(Pageable pageable);
    
    /**
     * 检查用户是否是圈子成员
     * 
     * @param userId 用户ID
     * @param circleId 圈子ID
     * @return 是否是成员
     */
    boolean isMember(Long userId, Long circleId);
    
    /**
     * 检查用户是否是圈子管理员
     * 
     * @param userId 用户ID
     * @param circleId 圈子ID
     * @return 是否是管理员
     */
    boolean isAdmin(Long userId, Long circleId);
    
    /**
     * 检查用户是否是圈子创建者
     * 
     * @param userId 用户ID
     * @param circleId 圈子ID
     * @return 是否是创建者
     */
    boolean isCreator(Long userId, Long circleId);
    
    /**
     * 获取圈子实体
     * 
     * @param circleId 圈子ID
     * @return 圈子实体
     */
    Optional<Circle> getCircleEntity(Long circleId);
    
    /**
     * 保存圈子实体
     * 
     * @param circle 圈子实体
     * @return 保存后的圈子实体
     */
    Circle saveCircle(Circle circle);
}