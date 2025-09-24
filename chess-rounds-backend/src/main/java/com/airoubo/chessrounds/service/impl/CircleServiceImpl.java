package com.airoubo.chessrounds.service.impl;

import com.airoubo.chessrounds.dto.circle.*;
import com.airoubo.chessrounds.entity.*;
import com.airoubo.chessrounds.enums.RoundStatus;
import com.airoubo.chessrounds.repository.*;
import com.airoubo.chessrounds.service.CircleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 圈子服务实现类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Service
@Transactional
public class CircleServiceImpl implements CircleService {
    
    private static final Logger logger = LoggerFactory.getLogger(CircleServiceImpl.class);
    
    @Autowired
    private CircleRepository circleRepository;
    
    @Autowired
    private CircleMemberRepository circleMemberRepository;
    
    @Autowired
    private CircleLeaderboardRepository circleLeaderboardRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ParticipantRecordRepository participantRecordRepository;
    
    @Override
    public CircleInfoResponse createCircle(Long userId, CreateCircleRequest request) {
        logger.info("用户 {} 创建圈子: {}", userId, request.getName());
        
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 生成唯一的圈子代码
        String circleCode = generateUniqueCircleCode();
        
        // 生成唯一的加入代码
        String joinCode = generateUniqueJoinCode();
        
        // 创建圈子
        Circle circle = new Circle();
        circle.setName(request.getName());
        circle.setCircleCode(circleCode);
        circle.setJoinCode(joinCode);
        circle.setDescription(request.getDescription());
        circle.setAvatarUrl(request.getAvatarUrl());
        circle.setCreatorId(userId);
        circle.setMaxMembers(request.getMaxMembers());
        circle.setCurrentMembers(1); // 创建者自动成为成员
        circle.setIsPublic(request.getIsPublic());
        circle.setStatus(1); // 1-正常
        circle.setLastActivityAt(LocalDateTime.now());
        
        circle = circleRepository.save(circle);
        
        // 创建者自动加入圈子
        CircleMember member = new CircleMember();
        member.setCircleId(circle.getId());
        member.setUserId(userId);
        member.setNickname(user.getNickname());
        member.setAvatarUrl(user.getAvatarUrl());
        member.setRole(2); // 2-创建者
        member.setStatus(1); // 1-正常状态
        member.setJoinedAt(LocalDateTime.now());
        
        circleMemberRepository.save(member);
        
        logger.info("圈子创建成功，ID: {}, 代码: {}", circle.getId(), circleCode);
        
        return convertToCircleInfoResponse(circle, userId);
    }
    
    @Override
    public CircleInfoResponse joinCircle(Long userId, JoinCircleRequest request) {
        logger.info("用户 {} 加入圈子: {}", userId, request.getCircleCode());
        
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 根据圈子代码查找圈子
        Circle circle = circleRepository.findByCircleCode(request.getCircleCode())
                .orElseThrow(() -> new RuntimeException("圈子不存在或代码错误"));
        
        // 检查圈子状态
        if (circle.getStatus() != 1) {
            throw new RuntimeException("圈子已被解散或暂停");
        }
        
        // 检查用户是否已经是成员
        if (circleMemberRepository.findByCircleIdAndUserId(circle.getId(), userId).isPresent()) {
            throw new RuntimeException("您已经是该圈子的成员");
        }
        
        // 检查圈子是否已满
        if (circle.getCurrentMembers() >= circle.getMaxMembers()) {
            throw new RuntimeException("圈子人数已满");
        }
        
        // 加入圈子
        CircleMember member = new CircleMember();
        member.setCircleId(circle.getId());
        member.setUserId(userId);
        member.setNickname(request.getNickname() != null ? request.getNickname() : user.getNickname());
        member.setAvatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : user.getAvatarUrl());
        member.setRole(0); // 0-普通成员
        member.setStatus(1); // 1-正常状态
        member.setJoinedAt(LocalDateTime.now());
        
        circleMemberRepository.save(member);
        
        // 更新圈子成员数量
        circle.setCurrentMembers(circle.getCurrentMembers() + 1);
        circle.setLastActivityAt(LocalDateTime.now());
        circleRepository.save(circle);
        
        // 为新用户创建排行榜记录
        updateUserLeaderboardData(circle.getId(), userId);
        
        logger.info("用户 {} 成功加入圈子 {}", userId, circle.getId());
        
        return convertToCircleInfoResponse(circle, userId);
    }
    
    @Override
    public CircleInfoResponse joinCircleByCode(Long userId, JoinCircleByCodeRequest request) {
        logger.info("用户 {} 通过加入码加入圈子: {}", userId, request.getJoinCode());
        
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 根据加入码查找圈子
        Circle circle = circleRepository.findByJoinCode(request.getJoinCode())
                .orElseThrow(() -> new RuntimeException("加入码不存在或已失效"));
        
        // 检查圈子状态
        if (circle.getStatus() != 1) {
            throw new RuntimeException("圈子已被解散或暂停");
        }
        
        // 检查用户是否已经是成员
        if (circleMemberRepository.findByCircleIdAndUserId(circle.getId(), userId).isPresent()) {
            throw new RuntimeException("您已经是该圈子的成员");
        }
        
        // 检查圈子是否已满
        if (circle.getCurrentMembers() >= circle.getMaxMembers()) {
            throw new RuntimeException("圈子人数已满");
        }
        
        // 加入圈子
        CircleMember member = new CircleMember();
        member.setCircleId(circle.getId());
        member.setUserId(userId);
        member.setNickname(request.getNickname() != null ? request.getNickname() : user.getNickname());
        member.setAvatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : user.getAvatarUrl());
        member.setRole(0); // 0-普通成员
        member.setStatus(1); // 1-正常状态
        member.setJoinedAt(LocalDateTime.now());
        
        circleMemberRepository.save(member);
        
        // 更新圈子成员数量
        circle.setCurrentMembers(circle.getCurrentMembers() + 1);
        circle.setLastActivityAt(LocalDateTime.now());
        circleRepository.save(circle);
        
        // 为新用户创建排行榜记录
        updateUserLeaderboardData(circle.getId(), userId);
        
        logger.info("用户 {} 成功通过加入码加入圈子 {}", userId, circle.getId());
        
        return convertToCircleInfoResponse(circle, userId);
    }
    
    @Override
    public void leaveCircle(Long userId, Long circleId) {
        logger.info("用户 {} 退出圈子: {}", userId, circleId);
        
        // 检查用户是否是圈子成员
        CircleMember member = circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                .orElseThrow(() -> new RuntimeException("您不是该圈子的成员"));
        
        // 检查是否是创建者
        if (member.getRole() == 2) {
            throw new RuntimeException("圈子创建者不能退出圈子，请先转让圈子或解散圈子");
        }
        
        // 删除成员记录
        circleMemberRepository.delete(member);
        
        // 更新圈子成员数量
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new RuntimeException("圈子不存在"));
        
        circle.setCurrentMembers(circle.getCurrentMembers() - 1);
        circle.setLastActivityAt(LocalDateTime.now());
        circleRepository.save(circle);
        
        logger.info("用户 {} 成功退出圈子 {}", userId, circleId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CircleInfoResponse> getUserCircles(Long userId, Pageable pageable) {
        // 简化实现，直接查询所有圈子成员记录
        List<CircleMember> members = circleMemberRepository.findAll().stream()
                .filter(member -> member.getUserId().equals(userId) && member.getStatus() == 1)
                .collect(Collectors.toList());
        
        List<CircleInfoResponse> circles = members.stream()
                .map(member -> {
                    Circle circle = circleRepository.findById(member.getCircleId()).orElse(null);
                    return circle != null ? convertToCircleInfoResponse(circle, userId) : null;
                })
                .filter(circle -> circle != null)
                .collect(Collectors.toList());
        
        return new PageImpl<>(circles, pageable, circles.size());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CircleInfoResponse> getCircleInfo(Long circleId, Long userId) {
        return circleRepository.findById(circleId)
                .map(circle -> convertToCircleInfoResponse(circle, userId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CircleInfoResponse> getCircleInfoByCode(String circleCode, Long userId) {
        return circleRepository.findByCircleCode(circleCode)
                .map(circle -> convertToCircleInfoResponse(circle, userId));
    }
    
    @Override
    public CircleInfoResponse updateCircle(Long userId, Long circleId, UpdateCircleRequest request) {
        logger.info("用户 {} 更新圈子 {}", userId, circleId);
        
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new RuntimeException("圈子不存在"));
        
        // 检查权限（只有创建者和管理员可以更新）
        if (!isCreator(userId, circleId) && !isAdmin(userId, circleId)) {
            throw new RuntimeException("没有权限更新圈子信息");
        }
        
        // 更新圈子信息
        if (request.getName() != null) {
            circle.setName(request.getName());
        }
        if (request.getDescription() != null) {
            circle.setDescription(request.getDescription());
        }
        if (request.getAvatarUrl() != null) {
            circle.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getMaxMembers() != null) {
            if (request.getMaxMembers() < circle.getCurrentMembers()) {
                throw new RuntimeException("最大成员数不能小于当前成员数");
            }
            circle.setMaxMembers(request.getMaxMembers());
        }
        if (request.getIsPublic() != null) {
            circle.setIsPublic(request.getIsPublic());
        }
        
        circle = circleRepository.save(circle);
        
        logger.info("圈子 {} 更新成功", circleId);
        
        return convertToCircleInfoResponse(circle, userId);
    }
    
    @Override
    public void dissolveCircle(Long userId, Long circleId) {
        logger.info("用户 {} 解散圈子 {}", userId, circleId);
        
        // 检查权限（只有创建者可以解散）
        if (!isCreator(userId, circleId)) {
            throw new RuntimeException("只有圈子创建者可以解散圈子");
        }
        
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new RuntimeException("圈子不存在"));
        
        // 更新圈子状态为已解散
        circle.setStatus(0); // 0-禁用
        circleRepository.save(circle);
        
        // 删除所有成员记录
        List<CircleMember> members = circleMemberRepository.findAll().stream()
                .filter(member -> member.getCircleId().equals(circleId))
                .collect(Collectors.toList());
        circleMemberRepository.deleteAll(members);
        
        logger.info("圈子 {} 解散成功", circleId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CircleMemberResponse> getCircleMembers(Long circleId, Long userId, Pageable pageable) {
        // 检查用户是否是圈子成员
        if (!isMember(userId, circleId)) {
            throw new RuntimeException("您不是该圈子的成员");
        }
        
        List<CircleMember> members = circleMemberRepository.findAll().stream()
                .filter(member -> member.getCircleId().equals(circleId) && member.getStatus() == 1)
                .collect(Collectors.toList());
        
        List<CircleMemberResponse> memberResponses = members.stream()
                .map(this::convertToCircleMemberResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(memberResponses, pageable, memberResponses.size());
    }
    
    @Override
    public void removeMember(Long operatorId, Long circleId, Long memberId) {
        logger.info("用户 {} 移除圈子 {} 的成员 {}", operatorId, circleId, memberId);
        
        // 检查操作者权限
        if (!isCreator(operatorId, circleId) && !isAdmin(operatorId, circleId)) {
            throw new RuntimeException("没有权限移除成员");
        }
        
        // 检查被移除的成员
        CircleMember member = circleMemberRepository.findByCircleIdAndUserId(circleId, memberId)
                .orElseThrow(() -> new RuntimeException("该用户不是圈子成员"));
        
        // 不能移除创建者
        if (member.getRole() == 2) {
            throw new RuntimeException("不能移除圈子创建者");
        }
        
        // 管理员不能移除其他管理员（除非是创建者操作）
        if (member.getRole() == 1 && !isCreator(operatorId, circleId)) {
            throw new RuntimeException("管理员不能移除其他管理员");
        }
        
        // 删除成员记录
        circleMemberRepository.delete(member);
        
        // 更新圈子成员数量
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new RuntimeException("圈子不存在"));
        
        circle.setCurrentMembers(circle.getCurrentMembers() - 1);
        circle.setLastActivityAt(LocalDateTime.now());
        circleRepository.save(circle);
        
        logger.info("成功移除圈子 {} 的成员 {}", circleId, memberId);
    }
    
    @Override
    public void setMemberAdmin(Long operatorId, Long circleId, Long memberId, Boolean isAdmin) {
        logger.info("用户 {} 设置圈子 {} 的成员 {} 管理员权限: {}", operatorId, circleId, memberId, isAdmin);
        
        // 检查操作者权限（只有创建者可以设置管理员）
        if (!isCreator(operatorId, circleId)) {
            throw new RuntimeException("只有圈子创建者可以设置管理员");
        }
        
        CircleMember member = circleMemberRepository.findByCircleIdAndUserId(circleId, memberId)
                .orElseThrow(() -> new RuntimeException("该用户不是圈子成员"));
        
        // 不能修改创建者的角色
        if (member.getRole() == 2) {
            throw new RuntimeException("不能修改创建者的角色");
        }
        
        // 设置角色
        member.setRole(isAdmin ? 1 : 0); // 1-管理员，0-普通成员
        circleMemberRepository.save(member);
        
        logger.info("成功设置圈子 {} 的成员 {} 管理员权限: {}", circleId, memberId, isAdmin);
    }
    
    @Override
    @Transactional
    public Page<CircleLeaderboardResponse> getCircleLeaderboard(Long circleId, Long userId, Pageable pageable, String sortBy, String sortOrder) {
        // 检查用户是否是圈子成员
        if (!isMember(userId, circleId)) {
            throw new RuntimeException("您不是该圈子的成员");
        }
        
        // 根据排序参数选择查询方法
        Page<CircleLeaderboard> leaderboardPage;
        
        // 默认排序参数
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "score";
        }
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "desc";
        }
        
        // 根据排序字段和方向选择对应的查询方法
        if ("score".equalsIgnoreCase(sortBy)) {
            if ("asc".equalsIgnoreCase(sortOrder)) {
                leaderboardPage = circleLeaderboardRepository.findByCircleIdOrderByScoreAsc(circleId, pageable);
            } else {
                leaderboardPage = circleLeaderboardRepository.findByCircleIdOrderByScore(circleId, pageable);
            }
        } else if ("winRate".equalsIgnoreCase(sortBy)) {
            if ("asc".equalsIgnoreCase(sortOrder)) {
                leaderboardPage = circleLeaderboardRepository.findByCircleIdOrderByWinRateAsc(circleId, pageable);
            } else {
                leaderboardPage = circleLeaderboardRepository.findByCircleIdOrderByWinRate(circleId, pageable);
            }
        } else {
            // 默认按积分降序排序
            leaderboardPage = circleLeaderboardRepository.findByCircleIdOrderByScore(circleId, pageable);
        }
        
        // 如果排行榜为空，尝试初始化排行榜数据
        if (leaderboardPage.isEmpty()) {
            initializeCircleLeaderboard(circleId);
            // 重新查询
            if ("score".equalsIgnoreCase(sortBy)) {
                if ("asc".equalsIgnoreCase(sortOrder)) {
                    leaderboardPage = circleLeaderboardRepository.findByCircleIdOrderByScoreAsc(circleId, pageable);
                } else {
                    leaderboardPage = circleLeaderboardRepository.findByCircleIdOrderByScore(circleId, pageable);
                }
            } else if ("winRate".equalsIgnoreCase(sortBy)) {
                if ("asc".equalsIgnoreCase(sortOrder)) {
                    leaderboardPage = circleLeaderboardRepository.findByCircleIdOrderByWinRateAsc(circleId, pageable);
                } else {
                    leaderboardPage = circleLeaderboardRepository.findByCircleIdOrderByWinRate(circleId, pageable);
                }
            } else {
                leaderboardPage = circleLeaderboardRepository.findByCircleIdOrderByScore(circleId, pageable);
            }
        }
        
        // 转换为响应对象
        List<CircleLeaderboardResponse> responses = leaderboardPage.getContent().stream()
                .map(this::convertToCircleLeaderboardResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, leaderboardPage.getTotalElements());
    }
    
    @Override
    public void refreshCircleLeaderboard(Long circleId, Long userId) {
        logger.info("用户 {} 刷新圈子 {} 的排行榜", userId, circleId);
        
        // 检查用户权限
        if (!isCreator(userId, circleId) && !isAdmin(userId, circleId)) {
            throw new RuntimeException("没有权限刷新排行榜");
        }
        
        // 初始化或更新排行榜数据
        initializeCircleLeaderboard(circleId);
        
        logger.info("圈子 {} 排行榜刷新完成", circleId);
    }
    
    @Override
    @Transactional
    public void updateUserLeaderboardData(Long circleId, Long userId) {
        logger.info("更新用户 {} 在圈子 {} 中的排行榜数据", userId, circleId);
        
        // 首先检查用户是否是圈子成员
        if (!isMember(userId, circleId)) {
            logger.warn("用户 {} 不是圈子 {} 的成员，跳过排行榜更新", userId, circleId);
            return;
        }
        
        // 查找或创建排行榜记录
        Optional<CircleLeaderboard> leaderboardOpt = circleLeaderboardRepository.findByCircleIdAndUserId(circleId, userId);
        CircleLeaderboard leaderboard;
        
        if (leaderboardOpt.isPresent()) {
            leaderboard = leaderboardOpt.get();
        } else {
            // 创建新的排行榜记录
            leaderboard = new CircleLeaderboard();
            leaderboard.setCircleId(circleId);
            leaderboard.setUserId(userId);
            leaderboard.setScore(new java.math.BigDecimal("1000")); // 初始积分
            leaderboard.setSeason("2024");
        }
        
        // 由于回合和圈子没有直接关联，我们统计用户的所有游戏数据
        // 这里可以考虑两种方案：
        // 1. 统计用户所有的游戏数据（当前采用）
        // 2. 只统计圈子成员之间的对局数据（需要额外的逻辑）
        List<ParticipantRecord> userRecords = participantRecordRepository.findByUserIdAndFinishedRounds(userId, RoundStatus.FINISHED);
        
        if (!userRecords.isEmpty()) {
            // 按回合分组统计 - 统计回合数而不是局数
            Map<Long, List<ParticipantRecord>> recordsByRound = userRecords.stream()
                .collect(Collectors.groupingBy(record -> record.getRound().getId()));
            
            int totalRounds = recordsByRound.size(); // 改为回合数
            int winRounds = 0;
            int loseRounds = 0;
            int drawRounds = 0;
            
            // 计算总金额变化（每个有效回合的筹码数*倍率之和）
            BigDecimal totalAmountChange = BigDecimal.ZERO;
            
            // 统计每个回合的胜负情况和金额
            for (List<ParticipantRecord> roundRecords : recordsByRound.values()) {
                // 计算该回合的总金额变化
                BigDecimal roundAmountChange = roundRecords.stream()
                    .map(ParticipantRecord::getAmountChange)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // 应用回合倍率
                BigDecimal multiplier = roundRecords.get(0).getRound() != null && roundRecords.get(0).getRound().getMultiplier() != null 
                        ? roundRecords.get(0).getRound().getMultiplier() 
                        : BigDecimal.ONE;
                BigDecimal roundAmountWithMultiplier = roundAmountChange.multiply(multiplier);
                totalAmountChange = totalAmountChange.add(roundAmountWithMultiplier);
                
                // 判断该回合的胜负情况
                if (roundAmountChange.compareTo(BigDecimal.ZERO) > 0) {
                    winRounds++;
                } else if (roundAmountChange.compareTo(BigDecimal.ZERO) == 0) {
                    drawRounds++;
                } else {
                    loseRounds++;
                }
            }
            
            // 更新排行榜数据 - 使用回合数
            leaderboard.setTotalGames(totalRounds); // 实际存储的是回合数
            leaderboard.setWins(winRounds);
            leaderboard.setLosses(loseRounds);
            leaderboard.setDraws(drawRounds);
            
            // 计算胜率 - 按回合胜负计算并保留两位小数的百分比
            if (totalRounds > 0) {
                double winRatePercent = (double) winRounds / totalRounds * 100;
                BigDecimal winRate = BigDecimal.valueOf(winRatePercent).setScale(2, RoundingMode.HALF_UP);
                leaderboard.setWinRate(winRate);
            } else {
                leaderboard.setWinRate(BigDecimal.ZERO);
            }
            
            // 设置总金额（直接存储，不转换为分）
            leaderboard.setScore(totalAmountChange); // 使用score字段存储总金额
            
            // 更新最后游戏时间
            LocalDateTime lastGameTime = userRecords.stream()
                .map(record -> record.getRound().getEndTime())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            leaderboard.setLastGameAt(lastGameTime);
        } else {
            // 没有游戏记录，设置为初始值
            leaderboard.setTotalGames(0);
            leaderboard.setWins(0);
            leaderboard.setLosses(0);
            leaderboard.setDraws(0);
            leaderboard.setWinRate(BigDecimal.ZERO);
            leaderboard.setScore(BigDecimal.ZERO); // 初始金额为0
            leaderboard.setLastGameAt(null);
        }
        
        // 保存排行榜记录
        circleLeaderboardRepository.save(leaderboard);
        
        // 更新排名
        circleLeaderboardRepository.updateRankings(circleId);
        
        logger.info("用户 {} 在圈子 {} 中的排行榜数据更新完成，总局数: {}, 胜局: {}", userId, circleId, leaderboard.getTotalGames(), leaderboard.getWins());
    }
    
    /**
     * 初始化圈子排行榜数据
     * 
     * @param circleId 圈子ID
     */
    @Transactional
    private void initializeCircleLeaderboard(Long circleId) {
        logger.info("开始初始化圈子 {} 的排行榜数据", circleId);
        
        // 获取圈子所有成员（包括创建者）
        List<CircleMember> members = circleMemberRepository.findByCircleIdAndStatus(circleId, 1, Pageable.unpaged()).getContent();
        
        for (CircleMember member : members) {
            // 检查是否已存在排行榜记录
            Optional<CircleLeaderboard> existingRecord = circleLeaderboardRepository.findByCircleIdAndUserId(circleId, member.getUserId());
            
            if (existingRecord.isEmpty()) {
                logger.info("为用户 {} 创建排行榜记录，计算真实游戏数据", member.getUserId());
                
                // 创建新的排行榜记录并计算真实数据
                CircleLeaderboard leaderboard = new CircleLeaderboard();
                leaderboard.setCircleId(circleId);
                leaderboard.setUserId(member.getUserId());
                leaderboard.setSeason("2024");
                
                // 获取用户的所有已完成回合的参与记录
                 List<ParticipantRecord> userRecords = participantRecordRepository.findByUserIdAndFinishedRounds(member.getUserId(), RoundStatus.FINISHED);
                
                if (!userRecords.isEmpty()) {
                    // 按回合分组统计胜负情况
                    Map<Long, List<ParticipantRecord>> recordsByRound = userRecords.stream()
                            .collect(Collectors.groupingBy(record -> record.getRound().getId()));
                    
                    int totalRounds = recordsByRound.size(); // 统计回合数而不是局数
                    int winRounds = 0;
                    int loseRounds = 0;
                    int drawRounds = 0;
                    BigDecimal totalAmountWithMultiplier = BigDecimal.ZERO; // 金额*倍率之和
                    
                    // 连胜相关统计
                    int currentConsecutiveWins = 0;
                    int maxConsecutiveWins = 0;
                    
                    for (Map.Entry<Long, List<ParticipantRecord>> entry : recordsByRound.entrySet()) {
                        List<ParticipantRecord> roundRecords = entry.getValue();
                        
                        // 计算该回合的总金额变化
                        BigDecimal roundAmountChange = roundRecords.stream()
                                .filter(record -> record.getAmountChange() != null)
                                .map(ParticipantRecord::getAmountChange)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        // 获取回合倍率
                        BigDecimal multiplier = roundRecords.get(0).getRound().getMultiplier();
                        if (multiplier == null) {
                            multiplier = BigDecimal.ONE;
                        }
                        
                        // 计算金额*倍率
                        BigDecimal amountWithMultiplier = roundAmountChange.multiply(multiplier);
                        totalAmountWithMultiplier = totalAmountWithMultiplier.add(amountWithMultiplier);
                        
                        // 判断该回合的胜负（基于总金额变化）
                        if (roundAmountChange.compareTo(BigDecimal.ZERO) > 0) {
                            winRounds++;
                            currentConsecutiveWins++;
                            maxConsecutiveWins = Math.max(maxConsecutiveWins, currentConsecutiveWins);
                        } else if (roundAmountChange.compareTo(BigDecimal.ZERO) < 0) {
                            loseRounds++;
                            currentConsecutiveWins = 0;
                        } else {
                            drawRounds++;
                            currentConsecutiveWins = 0;
                        }
                    }
                    
                    // 设置统计数据（使用回合数）
                    leaderboard.setTotalGames(totalRounds);
                    leaderboard.setWins(winRounds);
                    leaderboard.setLosses(loseRounds);
                    leaderboard.setDraws(drawRounds);
                    
                    // 设置金额统计（每个有效回合的筹码数*倍率之和）
                    leaderboard.setScore(totalAmountWithMultiplier);
                    
                    // 计算胜率（按回合胜负计算，保留两位小数的百分比）
                    if (totalRounds > 0) {
                        BigDecimal winRate = new BigDecimal(winRounds)
                                .divide(new BigDecimal(totalRounds), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, RoundingMode.HALF_UP);
                        leaderboard.setWinRate(winRate);
                    } else {
                        leaderboard.setWinRate(BigDecimal.ZERO);
                    }
                    
                    // 设置连胜数据
                    leaderboard.setConsecutiveWins(currentConsecutiveWins);
                    leaderboard.setMaxConsecutiveWins(maxConsecutiveWins);
                    
                    // 更新最后游戏时间
                    LocalDateTime lastGameTime = userRecords.stream()
                        .map(record -> record.getRound().getEndTime())
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
                    leaderboard.setLastGameAt(lastGameTime);
                    
                    logger.info("用户 {} 的游戏数据：总回合数={}, 胜回合={}, 负回合={}, 平回合={}, 金额={}, 胜率={}%", 
                            member.getUserId(), totalRounds, winRounds, loseRounds, drawRounds, totalAmountWithMultiplier, leaderboard.getWinRate());
                } else {
                    // 没有游戏记录，设置为初始值
                    leaderboard.setScore(BigDecimal.ZERO); // 初始金额为0
                    leaderboard.setTotalGames(0);
                    leaderboard.setWins(0);
                    leaderboard.setLosses(0);
                    leaderboard.setDraws(0);
                    leaderboard.setWinRate(BigDecimal.ZERO);
                    leaderboard.setConsecutiveWins(0);
                    leaderboard.setMaxConsecutiveWins(0);
                    leaderboard.setLastGameAt(null);
                    
                    logger.info("用户 {} 没有游戏记录，使用初始值", member.getUserId());
                }
                
                circleLeaderboardRepository.save(leaderboard);
            } else {
                logger.info("用户 {} 的排行榜记录已存在，跳过初始化", member.getUserId());
            }
        }
        
        // 更新排名
        circleLeaderboardRepository.updateRankings(circleId);
        
        logger.info("圈子 {} 的排行榜数据初始化完成", circleId);
    }
    
    /**
     * 转换CircleLeaderboard为CircleLeaderboardResponse
     * 
     * @param leaderboard 排行榜实体
     * @return 排行榜响应对象
     */
    private CircleLeaderboardResponse convertToCircleLeaderboardResponse(CircleLeaderboard leaderboard) {
        CircleLeaderboardResponse response = new CircleLeaderboardResponse();
        
        // 获取用户信息
        Optional<User> userOpt = userRepository.findById(leaderboard.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            response.setUserId(user.getId());
            response.setNickname(user.getNickname());
            response.setAvatarUrl(user.getAvatarUrl());
        }
        
        // 设置排行榜数据
        response.setRank(leaderboard.getRanking());
        response.setScore(leaderboard.getScore().intValue()); // 转换为Integer
        response.setTotalGames(leaderboard.getTotalGames());
        response.setWins(leaderboard.getWins());
        response.setLosses(leaderboard.getLosses());
        response.setDraws(leaderboard.getDraws());
        
        // 计算胜率
        if (leaderboard.getTotalGames() > 0) {
            double winRate = (double) leaderboard.getWins() / leaderboard.getTotalGames();
            response.setWinRate(winRate);
        } else {
            response.setWinRate(0.0);
        }
        
        response.setLastUpdated(leaderboard.getUpdatedAt());
        response.setRankChange(0); // 暂时设为0，后续可实现排名变化逻辑
        response.setIsCurrentUser(false); // 暂时设为false，后续可根据当前用户判断
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CircleInfoResponse> searchPublicCircles(String keyword, Pageable pageable) {
        List<Circle> circles = circleRepository.findAll().stream()
                .filter(circle -> circle.getIsPublic() && circle.getStatus() == 1)
                .filter(circle -> keyword == null || circle.getName().contains(keyword))
                .collect(Collectors.toList());
        
        List<CircleInfoResponse> circleResponses = circles.stream()
                .map(circle -> convertToCircleInfoResponse(circle, null))
                .collect(Collectors.toList());
        
        return new PageImpl<>(circleResponses, pageable, circleResponses.size());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CircleInfoResponse> getPopularCircles(Pageable pageable) {
        List<Circle> circles = circleRepository.findAll().stream()
                .filter(circle -> circle.getIsPublic() && circle.getStatus() == 1)
                .sorted((c1, c2) -> c2.getCurrentMembers().compareTo(c1.getCurrentMembers()))
                .collect(Collectors.toList());
        
        List<CircleInfoResponse> circleResponses = circles.stream()
                .map(circle -> convertToCircleInfoResponse(circle, null))
                .collect(Collectors.toList());
        
        return new PageImpl<>(circleResponses, pageable, circleResponses.size());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isMember(Long userId, Long circleId) {
        return circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                .map(member -> member.getStatus() == 1)
                .orElse(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(Long userId, Long circleId) {
        return circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                .map(member -> member.getRole() == 1 && member.getStatus() == 1)
                .orElse(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isCreator(Long userId, Long circleId) {
        return circleMemberRepository.findByCircleIdAndUserId(circleId, userId)
                .map(member -> member.getRole() == 2 && member.getStatus() == 1)
                .orElse(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Circle> getCircleEntity(Long circleId) {
        return circleRepository.findById(circleId);
    }
    
    @Override
    public Circle saveCircle(Circle circle) {
        return circleRepository.save(circle);
    }
    
    /**
     * 生成唯一的圈子代码
     */
    private String generateUniqueCircleCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (circleRepository.findByCircleCode(code).isPresent());
        return code;
    }
    
    /**
     * 生成唯一的加入代码
     */
    private String generateUniqueJoinCode() {
        String code;
        do {
            code = generateRandomJoinCode();
        } while (circleRepository.findByJoinCode(code).isPresent());
        return code;
    }
    
    /**
     * 生成随机代码
     */
    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    /**
     * 生成随机加入代码（5位数字）
     */
    private String generateRandomJoinCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 转换为圈子信息响应DTO
     */
    private CircleInfoResponse convertToCircleInfoResponse(Circle circle, Long userId) {
        CircleInfoResponse response = new CircleInfoResponse();
        response.setCircleId(circle.getId());
        response.setName(circle.getName());
        response.setCircleCode(circle.getCircleCode());
        response.setDescription(circle.getDescription());
        response.setAvatarUrl(circle.getAvatarUrl());
        response.setCreatorId(circle.getCreatorId());
        response.setMemberCount(circle.getCurrentMembers());
        response.setMaxMembers(circle.getMaxMembers());
        response.setIsPublic(circle.getIsPublic());
        response.setStatus(circle.getStatus().toString());
        response.setLastActivityTime(circle.getLastActivityAt());
        response.setCreatedAt(circle.getCreatedAt());
        
        // 设置创建者昵称
        userRepository.findById(circle.getCreatorId())
                .ifPresent(creator -> response.setCreatorNickname(creator.getNickname()));
        
        // 设置当前用户相关信息
        if (userId != null) {
            circleMemberRepository.findByCircleIdAndUserId(circle.getId(), userId)
                    .ifPresentOrElse(
                            member -> {
                                String role = member.getRole() == 2 ? "CREATOR" : 
                                             member.getRole() == 1 ? "ADMIN" : "MEMBER";
                                response.setUserRole(role);
                                response.setIsMember(member.getStatus() == 1);
                            },
                            () -> {
                                response.setUserRole(null);
                                response.setIsMember(false);
                            }
                    );
        } else {
            response.setUserRole(null);
            response.setIsMember(false);
        }
        
        // 设置加入码
        response.setJoinCode(circle.getJoinCode());
        
        return response;
    }
    
    /**
     * 转换为圈子成员响应DTO
     */
    private CircleMemberResponse convertToCircleMemberResponse(CircleMember member) {
        CircleMemberResponse response = new CircleMemberResponse();
        response.setUserId(member.getUserId());
        response.setCircleNickname(member.getNickname());
        response.setCircleAvatarUrl(member.getAvatarUrl());
        String role = member.getRole() == 2 ? "CREATOR" : 
                     member.getRole() == 1 ? "ADMIN" : "MEMBER";
        response.setRole(role);
        response.setJoinedAt(member.getJoinedAt());
        
        // 设置用户基本信息
        userRepository.findById(member.getUserId())
                .ifPresent(user -> {
                    response.setNickname(user.getNickname());
                    response.setAvatarUrl(user.getAvatarUrl());
                    // 简单的在线状态判断（最近5分钟有活动）
                    response.setIsOnline(user.getLastLoginAt() != null && 
                            user.getLastLoginAt().isAfter(LocalDateTime.now().minusMinutes(5)));
                });
        
        return response;
    }
}