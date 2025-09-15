package com.airoubo.chessrounds.service.impl;

import com.airoubo.chessrounds.dto.round.CreateRoundRequest;
import com.airoubo.chessrounds.dto.round.RoundInfoResponse;
import com.airoubo.chessrounds.dto.round.ParticipantInfoResponse;
import com.airoubo.chessrounds.dto.user.UserInfoResponse;
import com.airoubo.chessrounds.entity.Round;
import com.airoubo.chessrounds.entity.Participant;
import com.airoubo.chessrounds.entity.User;
import com.airoubo.chessrounds.enums.RoundStatus;
import com.airoubo.chessrounds.enums.ParticipantRole;
import com.airoubo.chessrounds.repository.RoundRepository;
import com.airoubo.chessrounds.repository.ParticipantRepository;
import com.airoubo.chessrounds.service.RoundService;
import com.airoubo.chessrounds.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ArrayList;

/**
 * 回合服务实现类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Service
@Transactional
public class RoundServiceImpl implements RoundService {
    
    @Autowired
    private RoundRepository roundRepository;
    
    @Autowired
    private ParticipantRepository participantRepository;
    
    @Autowired
    private UserService userService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public RoundInfoResponse createRound(CreateRoundRequest createRequest, Long creatorId) {
        // 创建回合实体
        Round round = new Round();
        round.setGameType(createRequest.getGameType());
        round.setMultiplier(createRequest.getBaseAmount());
        round.setMaxParticipants(createRequest.getMaxParticipants());
        round.setHasTable(createRequest.getHasTable());
        // 不再使用传入的tableUserId，台板用户在startRound时自动创建
        round.setTableUserId(null);
        round.setCreatorId(creatorId);
        round.setRoundCode(generateUniqueRoundCode());
        round.setStatus(RoundStatus.WAITING); // 等待中
        round.setCreatedAt(LocalDateTime.now());
        
        round = roundRepository.save(round);
        
        return convertToRoundInfoResponse(round);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<RoundInfoResponse> getRoundById(Long roundId) {
        return roundRepository.findById(roundId)
                .map(this::convertToRoundInfoResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<RoundInfoResponse> getRoundByCode(String roundCode) {
        return roundRepository.findByRoundCode(roundCode)
                .map(this::convertToRoundInfoResponse);
    }
    
    @Override
    public ParticipantInfoResponse joinRound(Long roundId, Long userId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("回合不存在"));
        
        // 检查回合状态
        if (!RoundStatus.WAITING.equals(round.getStatus())) {
            throw new RuntimeException("回合已开始或已结束，无法加入");
        }
        
        // 检查是否已经是参与者
        if (participantRepository.existsByRoundIdAndUserId(roundId, userId)) {
            throw new RuntimeException("您已经是该回合的参与者");
        }
        
        // 检查参与者数量限制
        Long currentParticipants = participantRepository.countByRoundIdAndIsActive(roundId, true);
        if (currentParticipants >= round.getMaxParticipants()) {
            throw new RuntimeException("回合人数已满，无法加入");
        }
        
        // 创建参与者记录
        Participant participant = new Participant();
        participant.setRoundId(roundId);
        participant.setUserId(userId);
        participant.setRole(ParticipantRole.PLAYER);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setIsActive(true);
        
        // 分配座位号（简单递增分配）
        int maxSeatNumber = participantRepository.findByRoundId(roundId).stream()
                .mapToInt(p -> p.getSeatNumber() != null ? p.getSeatNumber() : 0)
                .max().orElse(0);
        participant.setSeatNumber(maxSeatNumber + 1);
        
        participant = participantRepository.save(participant);
        
        // 构建参与者信息响应
        ParticipantInfoResponse response = new ParticipantInfoResponse();
        response.setParticipantId(participant.getId());
        response.setRole(participant.getRole().name());
        response.setJoinedAt(participant.getJoinedAt());
        response.setSeatNumber(participant.getSeatNumber());
        
        // 获取用户信息
        Optional<UserInfoResponse> userInfo = userService.getUserById(userId);
        if (userInfo.isPresent()) {
            response.setUserInfo(userInfo.get());
        }
        
        return response;
    }
    
    @Override
    public ParticipantInfoResponse joinRoundByCode(String roundCode, Long userId) {
        Round round = roundRepository.findByRoundCode(roundCode)
                .orElseThrow(() -> new RuntimeException("回合码无效"));
        
        return joinRound(round.getId(), userId);
    }
    
    @Override
    public void leaveRound(Long roundId, Long userId) {
        // 检查回合是否存在
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("回合不存在"));
        
        // 检查是否是参与者
        if (!isParticipant(roundId, userId)) {
            throw new RuntimeException("您不是该回合的参与者");
        }
        
        // 检查是否是创建者
        if (isCreator(roundId, userId)) {
            throw new RuntimeException("创建者不能离开回合");
        }
        
        // TODO: 删除参与者记录
    }
    
    @Override
    public void startRound(Long roundId, Long userId, Boolean hasTable, Long tableUserId, Double baseAmount) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("回合不存在"));
        
        // 检查权限
        if (!isCreator(roundId, userId)) {
            throw new RuntimeException("只有创建者可以开始回合");
        }
        
        // 检查状态
        if (!RoundStatus.WAITING.equals(round.getStatus())) {
            throw new RuntimeException("回合状态不正确");
        }
        
        // 更新台板信息
        if (hasTable != null) {
            round.setHasTable(hasTable);
            round.setTableUserId(tableUserId);
        }
        
        // 更新倍率值
        if (baseAmount != null) {
            round.setMultiplier(BigDecimal.valueOf(baseAmount));
        }
        
        // 如果有台板，创建专门的台板用户并添加为参与者
        if (round.getHasTable()) {
            // 为每个回合创建专门的台板用户
            User tableUser = new User();
            tableUser.setNickname("台板-" + round.getRoundCode());
            tableUser.setAvatarUrl("");
            tableUser.setOpenid("table_" + round.getRoundCode() + "_" + System.currentTimeMillis());
            tableUser.setUnionid(null);
            tableUser.setStatus(1); // 1-正常状态
            tableUser.setCreatedAt(LocalDateTime.now());
            tableUser.setUpdatedAt(LocalDateTime.now());
            
            // 保存台板用户
            User savedTableUser = userService.saveUser(tableUser);
            
            // 更新回合的台板用户ID
            round.setTableUserId(savedTableUser.getId());
            
            // 创建台板参与者记录
            Participant tableParticipant = new Participant();
            tableParticipant.setRoundId(roundId);
            tableParticipant.setUserId(savedTableUser.getId());
            tableParticipant.setRole(ParticipantRole.TABLE);
            tableParticipant.setJoinedAt(LocalDateTime.now());
            tableParticipant.setIsActive(true);
            participantRepository.save(tableParticipant);
        }
        
        // 更新状态
        round.setStatus(RoundStatus.PLAYING);
        round.setStartTime(LocalDateTime.now());
        roundRepository.save(round);
    }
    
    @Override
    public void endRound(Long roundId, Long userId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("回合不存在"));
        
        // 检查权限 - 创建者或参与者都可以收盘
        if (!isCreator(roundId, userId) && !isParticipant(roundId, userId)) {
            throw new RuntimeException("只有创建者或参与者可以结束回合");
        }
        
        // 检查状态
        if (!RoundStatus.PLAYING.equals(round.getStatus())) {
            throw new RuntimeException("回合状态不正确");
        }
        
        // 更新状态
        round.setStatus(RoundStatus.FINISHED);
        round.setEndTime(LocalDateTime.now());
        roundRepository.save(round);
    }
    
    @Override
    public void pauseRound(Long roundId, Long userId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("回合不存在"));
        
        // 检查权限
        if (!isCreator(roundId, userId)) {
            throw new RuntimeException("只有创建者可以暂停回合");
        }
        
        // 检查状态
        if (!RoundStatus.PLAYING.equals(round.getStatus())) {
            throw new RuntimeException("只有进行中的回合可以暂停");
        }
        
        // 更新状态
        round.setStatus(RoundStatus.WAITING);
        roundRepository.save(round);
    }
    
    @Override
    public void resumeRound(Long roundId, Long userId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("回合不存在"));
        
        // 检查权限
        if (!isCreator(roundId, userId)) {
            throw new RuntimeException("只有创建者可以恢复回合");
        }
        
        // 检查状态
        if (!RoundStatus.WAITING.equals(round.getStatus())) {
            throw new RuntimeException("只有暂停的回合可以恢复");
        }
        
        // 更新状态
        round.setStatus(RoundStatus.PLAYING);
        roundRepository.save(round);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParticipantInfoResponse> getRoundParticipants(Long roundId) {
        // 获取PLAYER和TABLE角色的参与者，不包含旁观者
        List<Participant> players = participantRepository.findByRoundIdAndRoleAndIsActive(roundId, ParticipantRole.PLAYER, true);
        List<Participant> tables = participantRepository.findByRoundIdAndRoleAndIsActive(roundId, ParticipantRole.TABLE, true);
        
        // 合并两个列表
        List<Participant> participants = new ArrayList<>();
        participants.addAll(players);
        participants.addAll(tables);
        
        List<ParticipantInfoResponse> responses = new ArrayList<>();
        
        for (Participant participant : participants) {
            ParticipantInfoResponse response = new ParticipantInfoResponse();
            response.setParticipantId(participant.getId());
            response.setRole(participant.getRole().name());
            response.setJoinedAt(participant.getJoinedAt());
            response.setSeatNumber(participant.getSeatNumber());
            
            // 计算参与者累计金额
            response.setTotalAmount(calculateParticipantTotalAmount(roundId, participant.getUserId()));
            
            // 获取用户信息
            Optional<UserInfoResponse> userInfo = userService.getUserById(participant.getUserId());
            if (userInfo.isPresent()) {
                response.setUserInfo(userInfo.get());
            }
            
            responses.add(response);
        }
        
        return responses;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RoundInfoResponse> getUserRounds(Long userId, Pageable pageable) {
        // 直接查询用户参与的回合，按创建时间倒序排列
        return roundRepository.findRoundsByUserId(userId, pageable)
                .map(round -> convertToRoundInfoResponseWithUserRole(round, userId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RoundInfoResponse> getCreatedRounds(Long creatorId, Pageable pageable) {
        return roundRepository.findByCreatorId(creatorId, pageable)
                .map(this::convertToRoundInfoResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RoundInfoResponse> searchRounds(String keyword, Pageable pageable) {
        // TODO: 实现标题搜索功能
        return Page.empty(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RoundInfoResponse> getRoundsByIds(List<Long> roundIds, Pageable pageable) {
        if (roundIds == null || roundIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        return roundRepository.findByIdIn(roundIds, pageable)
                .map(this::convertToRoundInfoResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RoundInfoResponse> getActiveRounds(Pageable pageable) {
        return roundRepository.findByStatus(RoundStatus.PLAYING, pageable)
                .map(this::convertToRoundInfoResponse);
    }
    
    @Override
    public RoundInfoResponse updateRound(Long roundId, CreateRoundRequest updateRequest, Long userId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("回合不存在"));
        
        // 检查权限
        if (!isCreator(roundId, userId)) {
            throw new RuntimeException("只有创建者可以修改回合");
        }
        
        // 检查状态
        if ("FINISHED".equals(round.getStatus())) {
            throw new RuntimeException("已结束的回合不能修改");
        }
        
        // 更新回合信息
        round.setGameType(updateRequest.getGameType());
        round.setMaxParticipants(updateRequest.getMaxParticipants());
        round.setMultiplier(updateRequest.getBaseAmount());
        round.setHasTable(updateRequest.getHasTable());
        round.setTableUserId(updateRequest.getTableUserId());
        round.setUpdatedAt(LocalDateTime.now());
        
        round = roundRepository.save(round);
        return convertToRoundInfoResponse(round);
    }
    
    @Override
    public void deleteRound(Long roundId, Long userId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("回合不存在"));
        
        // 检查权限
        if (!isCreator(roundId, userId)) {
            throw new RuntimeException("只有创建者可以删除回合");
        }
        
        // 检查状态 - 只允许删除等待中的回合
        if (!RoundStatus.WAITING.equals(round.getStatus())) {
            throw new RuntimeException("只能删除等待中的回合");
        }
        
        // 删除相关的所有记录
        // 1. 删除参与者记录
        participantRepository.deleteByRoundId(roundId);
        
        // 2. 删除游戏记录（如果有的话）
        // 注意：需要先删除子记录，再删除主记录
        try {
            // 删除参与者记录表
            entityManager.createQuery("DELETE FROM ParticipantRecord pr WHERE pr.roundId = :roundId")
                    .setParameter("roundId", roundId)
                    .executeUpdate();
            
            // 删除游戏记录表
            entityManager.createQuery("DELETE FROM Record r WHERE r.roundId = :roundId")
                    .setParameter("roundId", roundId)
                    .executeUpdate();
            
            // 删除评分记录
            entityManager.createQuery("DELETE FROM Rating rt WHERE rt.roundId = :roundId")
                    .setParameter("roundId", roundId)
                    .executeUpdate();
        } catch (Exception e) {
            // 如果删除相关记录失败，记录日志但继续删除回合
            System.err.println("删除回合相关记录时出错: " + e.getMessage());
        }
        
        // 3. 删除回合记录
         roundRepository.delete(round);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isParticipant(Long roundId, Long userId) {
        return participantRepository.existsByRoundIdAndUserId(roundId, userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isCreator(Long roundId, Long userId) {
        return roundRepository.findById(roundId)
                .map(round -> round.getCreatorId().equals(userId))
                .orElse(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoundCode(String roundCode) {
        return roundRepository.existsByRoundCode(roundCode);
    }
    
    @Override
    public String generateUniqueRoundCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (existsByRoundCode(code));
        return code;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countRounds(Long creatorId, String status, LocalDateTime since) {
        // TODO: 实现复杂的统计查询
        if (creatorId != null) {
            return roundRepository.countByCreatorId(creatorId);
        } else {
            return roundRepository.count();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Round> getRoundEntity(Long roundId) {
        return roundRepository.findById(roundId);
    }
    
    @Override
    public Round saveRound(Round round) {
        return roundRepository.save(round);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Object> getParticipantEntity(Long roundId, Long userId) {
        // TODO: 实现获取参与者实体
        return Optional.empty();
    }
    
    @Override
    public Object saveParticipant(Object participant) {
        // TODO: 实现保存参与者实体
        return participant;
    }
    
    /**
     * 将Round实体转换为RoundInfoResponse
     */
    private RoundInfoResponse convertToRoundInfoResponse(Round round) {
        return convertToRoundInfoResponseWithUserRole(round, null);
    }
    
    /**
     * 将Round实体转换为RoundInfoResponse，并设置当前用户角色
     */
    private RoundInfoResponse convertToRoundInfoResponseWithUserRole(Round round, Long userId) {
        RoundInfoResponse response = new RoundInfoResponse();
        response.setRoundId(round.getId());
        response.setRoundCode(round.getRoundCode());
        response.setGameType(round.getGameType());
        response.setMaxParticipants(round.getMaxParticipants());
        response.setBaseAmount(round.getMultiplier());
        response.setHasTable(round.getHasTable());
        response.setStatus(round.getStatus().getCode());
        response.setCreatedAt(round.getCreatedAt());
        response.setStartTime(round.getStartTime());
        response.setEndTime(round.getEndTime());
        response.setTotalAmount(round.getTotalAmount());
        
        // 获取创建者信息
        Optional<UserInfoResponse> creatorInfo = userService.getUserById(round.getCreatorId());
        if (creatorInfo.isPresent()) {
            response.setCreator(creatorInfo.get());
        }
        
        // 获取台板用户信息
        if (round.getTableUserId() != null) {
            Optional<UserInfoResponse> tableUserInfo = userService.getUserById(round.getTableUserId());
            if (tableUserInfo.isPresent()) {
                response.setTableUser(tableUserInfo.get());
            }
        }
        
        // 设置当前用户角色
        if (userId != null) {
            if (round.getCreatorId().equals(userId)) {
                response.setCurrentUserRole("creator");
            } else {
                // 查询用户是否为参与者
                Optional<Participant> participant = participantRepository.findByRoundIdAndUserId(round.getId(), userId);
                if (participant.isPresent()) {
                    if (participant.get().getRole() == ParticipantRole.SPECTATOR) {
                        response.setCurrentUserRole("spectator");
                    } else {
                        response.setCurrentUserRole("participant");
                    }
                }
            }
        }
        
        // 设置参与者列表
        List<ParticipantInfoResponse> participants = getRoundParticipants(round.getId());
        response.setParticipants(participants);

        // 设置旁观者列表
        List<ParticipantInfoResponse> spectators = getSpectators(round.getId());
        response.setSpectators(spectators);

        return response;
    }
    
    /**
     * 计算参与者在指定回合中的累计金额
     */
    private Double calculateParticipantTotalAmount(Long roundId, Long userId) {
        String sql = "SELECT COALESCE(SUM(pr.amount_change), 0) " +
                    "FROM participant_records pr " +
                    "WHERE pr.round_id = :roundId AND pr.user_id = :userId";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("roundId", roundId);
        query.setParameter("userId", userId);
        
        Object result = query.getSingleResult();
        if (result instanceof BigDecimal) {
            return ((BigDecimal) result).doubleValue();
        }
        return 0.0;
    }
    
    @Override
    public void joinSpectator(Long roundId, Long userId) {
        // 检查回合是否存在
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("回合不存在"));
        
        // 检查用户是否已经是参与者
        if (participantRepository.existsByRoundIdAndUserId(roundId, userId)) {
            throw new RuntimeException("用户已经是参与者，不能加入旁观");
        }
        
        // 检查用户是否已经是旁观者
        Optional<Participant> existingSpectator = participantRepository
                .findByRoundIdAndUserIdAndRole(roundId, userId, ParticipantRole.SPECTATOR);
        if (existingSpectator.isPresent() && existingSpectator.get().getIsActive()) {
            throw new RuntimeException("用户已经是旁观者");
        }
        
        // 创建旁观者记录
        Participant spectator = new Participant();
        spectator.setRoundId(roundId);
        spectator.setUserId(userId);
        spectator.setRole(ParticipantRole.SPECTATOR);
        spectator.setJoinedAt(LocalDateTime.now());
        spectator.setIsActive(true);
        
        participantRepository.save(spectator);
    }
    
    @Override
    public void leaveSpectator(Long roundId, Long userId) {
        // 查找旁观者记录
        Optional<Participant> spectator = participantRepository
                .findByRoundIdAndUserIdAndRole(roundId, userId, ParticipantRole.SPECTATOR);
        
        if (!spectator.isPresent() || !spectator.get().getIsActive()) {
            throw new RuntimeException("用户不是该回合的旁观者");
        }
        
        // 设置为非活跃状态
        Participant spectatorEntity = spectator.get();
        spectatorEntity.setIsActive(false);
        participantRepository.save(spectatorEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParticipantInfoResponse> getSpectators(Long roundId) {
        // 获取活跃的旁观者列表
        List<Participant> spectators = participantRepository
                .findByRoundIdAndRoleAndIsActive(roundId, ParticipantRole.SPECTATOR, true);
        
        List<ParticipantInfoResponse> responses = new ArrayList<>();
        for (Participant spectator : spectators) {
            ParticipantInfoResponse response = new ParticipantInfoResponse();
            response.setParticipantId(spectator.getId());
            response.setRole(spectator.getRole().name());
            response.setJoinedAt(spectator.getJoinedAt());
            
            // 获取用户信息
            Optional<UserInfoResponse> userInfo = userService.getUserById(spectator.getUserId());
            if (userInfo.isPresent()) {
                response.setUserInfo(userInfo.get());
            }
            
            responses.add(response);
        }
        
        return responses;
    }
    
    /**
     * 生成随机回合码
     */
    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}