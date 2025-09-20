package com.airoubo.chessrounds.service;

import com.airoubo.chessrounds.dto.round.CreateRoundRequest;
import com.airoubo.chessrounds.dto.round.RoundInfoResponse;
import com.airoubo.chessrounds.dto.round.ParticipantInfoResponse;
import com.airoubo.chessrounds.entity.Round;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 回合服务接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public interface RoundService {
    
    /**
     * 创建回合
     * 
     * @param createRequest 创建请求
     * @param creatorId 创建者ID
     * @return 回合信息
     */
    RoundInfoResponse createRound(CreateRoundRequest createRequest, Long creatorId);
    
    /**
     * 根据回合ID获取回合信息
     * 
     * @param roundId 回合ID
     * @return 回合信息
     */
    Optional<RoundInfoResponse> getRoundById(Long roundId);
    
    /**
     * 根据回合码获取回合信息
     * 
     * @param roundCode 回合码
     * @return 回合信息
     */
    Optional<RoundInfoResponse> getRoundByCode(String roundCode);
    
    /**
     * 加入回合
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 参与者信息
     */
    ParticipantInfoResponse joinRound(Long roundId, Long userId);
    
    /**
     * 通过回合码加入回合
     * 
     * @param roundCode 回合码
     * @param userId 用户ID
     * @return 参与者信息
     */
    ParticipantInfoResponse joinRoundByCode(String roundCode, Long userId);
    
    /**
     * 离开回合
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     */
    void leaveRound(Long roundId, Long userId);
    
    /**
     * 开始回合
     * 
     * @param roundId 回合ID
     * @param userId 操作用户ID（必须是创建者）
     * @param hasTable 是否有台板
     * @param tableUserId 台板用户ID（已废弃，系统会自动创建台板用户）
     * @param baseAmount 倍率值
     */
    void startRound(Long roundId, Long userId, Boolean hasTable, Long tableUserId, Double baseAmount);
    
    /**
     * 结束回合
     * 
     * @param roundId 回合ID
     * @param userId 操作用户ID（必须是创建者）
     */
    void endRound(Long roundId, Long userId);
    
    /**
     * 暂停回合
     * 
     * @param roundId 回合ID
     * @param userId 操作用户ID（必须是创建者）
     */
    void pauseRound(Long roundId, Long userId);
    
    /**
     * 恢复回合
     * 
     * @param roundId 回合ID
     * @param userId 操作用户ID（必须是创建者）
     */
    void resumeRound(Long roundId, Long userId);
    
    /**
     * 获取回合参与者列表
     * 
     * @param roundId 回合ID
     * @return 参与者列表
     */
    List<ParticipantInfoResponse> getRoundParticipants(Long roundId);
    
    /**
     * 加入旁观
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     */
    void joinSpectator(Long roundId, Long userId);
    
    /**
     * 退出旁观
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     */
    void leaveSpectator(Long roundId, Long userId);
    
    /**
     * 获取旁观者列表
     * 
     * @param roundId 回合ID
     * @return 旁观者列表
     */
    List<ParticipantInfoResponse> getSpectators(Long roundId);
    
    /**
     * 获取用户参与的回合列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 回合列表
     */
    Page<RoundInfoResponse> getUserRounds(Long userId, Pageable pageable);
    
    /**
     * 获取用户创建的回合列表
     * 
     * @param creatorId 创建者ID
     * @param pageable 分页参数
     * @return 回合列表
     */
    Page<RoundInfoResponse> getCreatedRounds(Long creatorId, Pageable pageable);
    
    /**
     * 搜索回合（按名称模糊查询）
     * 
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 回合列表
     */
    Page<RoundInfoResponse> searchRounds(String keyword, Pageable pageable);
    
    /**
     * 根据ID列表批量获取回合
     * 
     * @param roundIds 回合ID列表
     * @param pageable 分页参数
     * @return 回合列表
     */
    Page<RoundInfoResponse> getRoundsByIds(List<Long> roundIds, Pageable pageable);
    
    /**
     * 获取活跃回合列表
     * 
     * @param pageable 分页参数
     * @return 活跃回合列表
     */
    Page<RoundInfoResponse> getActiveRounds(Pageable pageable);
    
    /**
     * 更新回合信息
     * 
     * @param roundId 回合ID
     * @param updateRequest 更新请求
     * @param userId 操作用户ID（必须是创建者）
     * @return 更新后的回合信息
     */
    RoundInfoResponse updateRound(Long roundId, CreateRoundRequest updateRequest, Long userId);
    
    /**
     * 删除回合
     * 
     * @param roundId 回合ID
     * @param userId 操作用户ID（必须是创建者）
     */
    void deleteRound(Long roundId, Long userId);
    
    /**
     * 管理员删除回合
     * 管理员可以删除任何状态的回合，不受创建者权限限制
     * 
     * @param roundId 回合ID
     */
    void adminDeleteRound(Long roundId);
    
    /**
     * 检查用户是否是回合参与者
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 是否是参与者
     */
    boolean isParticipant(Long roundId, Long userId);
    
    /**
     * 检查用户是否是回合创建者
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 是否是创建者
     */
    boolean isCreator(Long roundId, Long userId);
    
    /**
     * 检查回合码是否存在
     * 
     * @param roundCode 回合码
     * @return 是否存在
     */
    boolean existsByRoundCode(String roundCode);
    
    /**
     * 生成唯一回合码
     * 
     * @return 回合码
     */
    String generateUniqueRoundCode();
    
    /**
     * 统计回合数量
     * 
     * @param creatorId 创建者ID（可选）
     * @param status 回合状态（可选）
     * @param since 时间范围（可选）
     * @return 回合数量
     */
    Long countRounds(Long creatorId, String status, LocalDateTime since);
    
    /**
     * 获取回合实体（内部使用）
     * 
     * @param roundId 回合ID
     * @return 回合实体
     */
    Optional<Round> getRoundEntity(Long roundId);
    
    /**
     * 保存回合实体（内部使用）
     * 
     * @param round 回合实体
     * @return 保存后的回合实体
     */
    Round saveRound(Round round);
    
    /**
     * 获取参与者实体（内部使用）
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 参与者实体
     */
    Optional<Object> getParticipantEntity(Long roundId, Long userId);
    
    /**
     * 保存参与者实体（内部使用）
     * 
     * @param participant 参与者实体
     * @return 保存后的参与者实体
     */
    Object saveParticipant(Object participant);
}