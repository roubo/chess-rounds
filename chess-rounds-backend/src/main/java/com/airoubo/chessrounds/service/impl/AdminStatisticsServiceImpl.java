package com.airoubo.chessrounds.service.impl;

import com.airoubo.chessrounds.dto.statistics.*;
import com.airoubo.chessrounds.entity.ParticipantRecord;
import com.airoubo.chessrounds.entity.Round;
import com.airoubo.chessrounds.entity.User;
import com.airoubo.chessrounds.enums.RoundStatus;
import com.airoubo.chessrounds.repository.ParticipantRecordRepository;
import com.airoubo.chessrounds.repository.RoundRepository;
import com.airoubo.chessrounds.repository.UserRepository;
import com.airoubo.chessrounds.service.AdminStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员统计服务实现类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Service
public class AdminStatisticsServiceImpl implements AdminStatisticsService {
    

    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoundRepository roundRepository;
    
    @Autowired
    private ParticipantRecordRepository participantRecordRepository;
    
    /**
     * 管理员用户ID（硬编码为1）
     */
    private static final Long ADMIN_USER_ID = 1L;
    
    @Override
    @Cacheable(value = "admin_statistics_overview", key = "'overview'")
    public AdminStatisticsOverviewResponse getStatisticsOverview() {
        // 获取回合统计
        Map<String, RoundStatisticsDTO> roundStats = getRoundStatistics();
        
        // 使用简化的查询方法
        UserStatisticsDTO userStats = calculateUserStatistics();
        FinancialStatisticsDTO financialStats = calculateFinancialStatistics();
        
        return new AdminStatisticsOverviewResponse(
            roundStats,
            userStats,
            financialStats,
            Instant.now()
        );
    }
    
    @Override
    @Cacheable(value = "admin_round_statistics", key = "'rounds'")
    public Map<String, RoundStatisticsDTO> getRoundStatistics() {
        Map<String, RoundStatisticsDTO> result = new HashMap<>();
        
        // 统计各状态的回合
        for (RoundStatus status : RoundStatus.values()) {
            List<Round> rounds = roundRepository.findAll().stream()
                .filter(round -> round.getStatus() == status)
                .collect(Collectors.toList());
            
            List<Long> roundIds = rounds.stream()
                .map(Round::getId)
                .collect(Collectors.toList());
            
            result.put(status.name(), new RoundStatisticsDTO(rounds.size(), roundIds));
        }
        
        return result;
    }
    
    @Override
    @Cacheable(value = "admin_user_details", key = "#page + '_' + #size + '_' + #sortBy + '_' + #sortDir")
    public AdminUserDetailResponse getUserDetails(int page, int size, String sortBy, String sortDir) {
        // 构建排序
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, convertSortField(sortBy));
        
        // 构建分页
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        // 直接查询非台板用户（在数据库层面过滤）
        Page<User> userPage = userRepository.findNonTableUsers(pageable);
        
        // 转换为DTO
        List<UserDetailDTO> userDetails = userPage.getContent().stream()
            .map(this::convertToUserDetailDTO)
            .collect(Collectors.toList());
        
        // 获取非台板用户总数
        long totalRealUsers = userRepository.countNonTableUsers();
        
        return new AdminUserDetailResponse(
            userDetails,
            (int) totalRealUsers,
            page,
            size,
            (int) Math.ceil((double) totalRealUsers / size),
            Instant.now()
        );
    }
    
    @Override
    @Cacheable(value = "admin_financial_details", key = "'financial'")
    public AdminFinancialDetailResponse getFinancialDetails() {
        // 获取所有已结束回合的流水记录
        List<ParticipantRecord> finishedRecords = participantRecordRepository
            .findByUserIdAndFinishedRounds(null, RoundStatus.FINISHED)
            .stream()
            .filter(record -> record.getAmountChange() != null)
            .collect(Collectors.toList());
        
        // 计算总流水绝对值
        BigDecimal totalAmountAbs = finishedRecords.stream()
            .map(record -> record.getAmountChange().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算收入和支出
        BigDecimal totalIncome = finishedRecords.stream()
            .filter(record -> record.getAmountChange().compareTo(BigDecimal.ZERO) > 0)
            .map(ParticipantRecord::getAmountChange)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpense = finishedRecords.stream()
            .filter(record -> record.getAmountChange().compareTo(BigDecimal.ZERO) < 0)
            .map(record -> record.getAmountChange().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算净收益
        BigDecimal netProfit = totalIncome.subtract(totalExpense);
        
        // 获取已结束回合数量
        Long finishedRoundsCount = roundRepository.countByStatus(RoundStatus.FINISHED);
        
        // 计算平均每回合流水
        BigDecimal averagePerRound = finishedRoundsCount > 0 
            ? totalAmountAbs.divide(BigDecimal.valueOf(finishedRoundsCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // 计算最高和最低单回合流水
        Map<Long, BigDecimal> roundAmounts = finishedRecords.stream()
            .collect(Collectors.groupingBy(
                ParticipantRecord::getRoundId,
                Collectors.mapping(
                    record -> record.getAmountChange().abs(),
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
        
        BigDecimal maxRoundAmount = roundAmounts.values().stream()
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        BigDecimal minRoundAmount = roundAmounts.values().stream()
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        return new AdminFinancialDetailResponse(
            totalAmountAbs,
            totalIncome,
            totalExpense,
            netProfit,
            finishedRoundsCount.intValue(),
            averagePerRound,
            maxRoundAmount,
            minRoundAmount,
            Instant.now()
        );
    }
    
    @Override
    @CacheEvict(value = {"admin_statistics_overview", "admin_round_statistics", 
                        "admin_user_details", "admin_financial_details"}, allEntries = true)
    public void refreshStatisticsCache() {
        // 缓存已通过注解清除
    }
    
    @Override
    public boolean isAdmin(Long userId) {
        return ADMIN_USER_ID.equals(userId);
    }
    
    /**
     * 计算用户统计信息
     */
    private UserStatisticsDTO calculateUserStatistics() {
        List<User> allUsers = userRepository.findAll();
        
        // 过滤台板数据：排除nickname包含"台板-"或openid以"table_"开头的用户
        List<User> realUsers = allUsers.stream()
            .filter(user -> !isTableUser(user))
            .collect(Collectors.toList());
        
        int totalUsers = realUsers.size();
        
        // 计算活跃用户（最近30天有登录）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        long activeUsers = realUsers.stream()
            .filter(user -> user.getLastLoginAt() != null && user.getLastLoginAt().isAfter(thirtyDaysAgo))
            .count();
        
        int inactiveUsers = totalUsers - (int) activeUsers;
        
        // 计算今日新增用户
        LocalDateTime todayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        long newUsersToday = realUsers.stream()
            .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(todayStart))
            .count();
        
        // 计算本周新增用户
        LocalDateTime weekStart = LocalDateTime.now().minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        long newUsersThisWeek = realUsers.stream()
            .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(weekStart))
            .count();
        
        return new UserStatisticsDTO(
            totalUsers,
            (int) activeUsers,
            inactiveUsers,
            (int) newUsersToday,
            (int) newUsersThisWeek
        );
    }
    
    /**
     * 判断是否为台板用户
     * 
     * @param user 用户对象
     * @return 是否为台板用户
     */
    private boolean isTableUser(User user) {
        if (user == null) {
            return false;
        }
        
        // 检查nickname是否包含"台板-"
        if (user.getNickname() != null && user.getNickname().startsWith("台板-")) {
            return true;
        }
        
        // 检查openid是否以"table_"开头
        if (user.getOpenid() != null && user.getOpenid().startsWith("table_")) {
            return true;
        }
        
        return false;
    }

    /**
     * 计算财务统计数据
     */
    private FinancialStatisticsDTO calculateFinancialStatistics() {
        List<ParticipantRecord> allRecords = participantRecordRepository.findAll()
            .stream()
            .filter(record -> record.getAmountChange() != null)
            .collect(Collectors.toList());
        
        // 总流水
        BigDecimal totalAmount = allRecords.stream()
            .map(record -> record.getAmountChange().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 今日流水
        LocalDateTime todayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        BigDecimal todayAmount = allRecords.stream()
            .filter(record -> record.getCreatedAt() != null && 
                   record.getCreatedAt().isAfter(todayStart))
            .map(record -> record.getAmountChange().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 本周流水
        LocalDateTime weekStart = LocalDateTime.now().minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        BigDecimal thisWeekAmount = allRecords.stream()
            .filter(record -> record.getCreatedAt() != null && 
                   record.getCreatedAt().isAfter(weekStart))
            .map(record -> record.getAmountChange().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 本月流水
        LocalDateTime monthStart = LocalDateTime.now().minus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        BigDecimal thisMonthAmount = allRecords.stream()
            .filter(record -> record.getCreatedAt() != null && 
                   record.getCreatedAt().isAfter(monthStart))
            .map(record -> record.getAmountChange().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 平均每回合流水和最高单回合流水
        Long finishedRoundsCount = roundRepository.countByStatus(RoundStatus.FINISHED);
        BigDecimal averagePerRound = finishedRoundsCount > 0 
            ? totalAmount.divide(BigDecimal.valueOf(finishedRoundsCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        Map<Long, BigDecimal> roundAmounts = allRecords.stream()
            .collect(Collectors.groupingBy(
                ParticipantRecord::getRoundId,
                Collectors.mapping(
                    record -> record.getAmountChange().abs(),
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
        
        BigDecimal maxRoundAmount = roundAmounts.values().stream()
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        return new FinancialStatisticsDTO(
            totalAmount,
            todayAmount,
            thisWeekAmount,
            thisMonthAmount,
            averagePerRound,
            maxRoundAmount
        );
    }
    
    /**
     * 转换用户实体为DTO
     */
    private UserDetailDTO convertToUserDetailDTO(User user) {
        // 计算用户参与的回合数
        List<ParticipantRecord> userRecords = participantRecordRepository.findByUserId(user.getId());
        int roundsParticipated = (int) userRecords.stream()
            .map(ParticipantRecord::getRoundId)
            .distinct()
            .count();
        
        // 计算用户总流水（金额乘以倍率之和，保留正负号）
        // 使用自定义查询来确保Round实体被正确加载
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (ParticipantRecord record : userRecords) {
            if (record.getAmountChange() != null) {
                // 通过roundId查询Round实体来获取倍率
                Round round = roundRepository.findById(record.getRoundId()).orElse(null);
                if (round != null) {
                    BigDecimal multiplier = round.getMultiplier();
                    if (multiplier == null) {
                        multiplier = BigDecimal.ONE;
                    }
                    totalAmount = totalAmount.add(record.getAmountChange().multiply(multiplier));
                }
            }
        }
        
        // 判断是否活跃用户（最近30天有登录）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        boolean isActive = user.getLastLoginAt() != null && user.getLastLoginAt().isAfter(thirtyDaysAgo);
        
        return new UserDetailDTO(
            user.getId(),
            user.getOpenid(),
            user.getNickname(),
            user.getAvatarUrl(),
            user.getCreatedAt() != null ? user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null,
            user.getLastLoginAt() != null ? user.getLastLoginAt().atZone(ZoneId.systemDefault()).toInstant() : null,
            roundsParticipated,
            totalAmount,
            isActive
        );
    }
    
    /**
     * 转换排序字段
     */
    private String convertSortField(String sortBy) {
        switch (sortBy) {
            case "created_at":
                return "createdAt";
            case "last_login_at":
                return "lastLoginAt";
            case "total_amount":
                // 这个字段需要特殊处理，暂时使用createdAt
                return "createdAt";
            default:
                return "createdAt";
        }
    }
}