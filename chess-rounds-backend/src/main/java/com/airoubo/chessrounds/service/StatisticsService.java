package com.airoubo.chessrounds.service;

import com.airoubo.chessrounds.dto.statistics.UserStatisticsResponse;
import com.airoubo.chessrounds.entity.UserStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 统计服务接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public interface StatisticsService {
    
    /**
     * 获取用户统计信息
     * 
     * @param userId 用户ID
     * @param statisticsType 统计类型
     * @param statisticsDate 统计日期
     * @return 统计信息
     */
    Optional<UserStatisticsResponse> getUserStatistics(Long userId, String statisticsType, LocalDate statisticsDate);
    
    /**
     * 获取用户最新统计信息
     * 
     * @param userId 用户ID
     * @param statisticsType 统计类型
     * @return 最新统计信息
     */
    Optional<UserStatisticsResponse> getLatestUserStatistics(Long userId, String statisticsType);
    
    /**
     * 获取用户统计历史
     * 
     * @param userId 用户ID
     * @param statisticsType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 统计历史列表
     */
    Page<UserStatisticsResponse> getUserStatisticsHistory(Long userId, String statisticsType, 
                                                         LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * 更新用户统计信息
     * 
     * @param userId 用户ID
     * @param statisticsType 统计类型
     * @param statisticsDate 统计日期
     */
    void updateUserStatistics(Long userId, String statisticsType, LocalDate statisticsDate);
    
    /**
     * 批量更新用户统计信息
     * 
     * @param userIds 用户ID列表
     * @param statisticsType 统计类型
     * @param statisticsDate 统计日期
     */
    void batchUpdateUserStatistics(List<Long> userIds, String statisticsType, LocalDate statisticsDate);
    
    /**
     * 重新计算用户统计信息
     * 
     * @param userId 用户ID
     * @param statisticsType 统计类型
     * @param statisticsDate 统计日期
     */
    void recalculateUserStatistics(Long userId, String statisticsType, LocalDate statisticsDate);
    
    /**
     * 获取排行榜
     * 
     * @param statisticsType 统计类型
     * @param rankType 排名类型（total_rounds, win_rate, total_amount等）
     * @param statisticsDate 统计日期（可选）
     * @param pageable 分页参数
     * @return 排行榜列表
     */
    Page<UserStatisticsResponse> getLeaderboard(String statisticsType, String rankType, 
                                               LocalDate statisticsDate, Pageable pageable);
    
    /**
     * 获取用户排名
     * 
     * @param userId 用户ID
     * @param statisticsType 统计类型
     * @param rankType 排名类型
     * @param statisticsDate 统计日期（可选）
     * @return 用户排名（从1开始）
     */
    Long getUserRank(Long userId, String statisticsType, String rankType, LocalDate statisticsDate);
    
    /**
     * 获取统计概览
     * 
     * @param userId 用户ID（可选，为空则获取全局统计）
     * @param statisticsType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计概览
     */
    StatisticsOverview getStatisticsOverview(Long userId, String statisticsType, 
                                           LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取趋势数据
     * 
     * @param userId 用户ID
     * @param statisticsType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据（日期 -> 统计值）
     */
    Map<LocalDate, UserStatisticsResponse> getTrendData(Long userId, String statisticsType, 
                                                       LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取对比数据
     * 
     * @param userIds 用户ID列表
     * @param statisticsType 统计类型
     * @param statisticsDate 统计日期
     * @return 对比数据
     */
    List<UserStatisticsResponse> getComparisonData(List<Long> userIds, String statisticsType, LocalDate statisticsDate);
    
    /**
     * 删除过期统计数据
     * 
     * @param beforeDate 删除此日期之前的数据
     * @return 删除的记录数
     */
    Long deleteExpiredStatistics(LocalDate beforeDate);
    
    /**
     * 统计活跃用户
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 活跃用户数量
     */
    Long countActiveUsers(LocalDate startDate, LocalDate endDate);
    
    /**
     * 统计总回合数
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总回合数
     */
    Long countTotalRounds(LocalDate startDate, LocalDate endDate);
    
    /**
     * 统计总金额
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总金额
     */
    Long sumTotalAmount(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取热门统计类型
     * 
     * @param limit 限制数量
     * @return 热门统计类型列表
     */
    List<String> getPopularStatisticsTypes(int limit);
    
    /**
     * 检查统计数据是否存在
     * 
     * @param userId 用户ID
     * @param statisticsType 统计类型
     * @param statisticsDate 统计日期
     * @return 是否存在
     */
    boolean existsStatistics(Long userId, String statisticsType, LocalDate statisticsDate);
    
    /**
     * 获取统计实体（内部使用）
     * 
     * @param userId 用户ID
     * @param statisticsType 统计类型
     * @param statisticsDate 统计日期
     * @return 统计实体
     */
    Optional<UserStatistics> getStatisticsEntity(Long userId, String statisticsType, LocalDate statisticsDate);
    
    /**
     * 保存统计实体（内部使用）
     * 
     * @param statistics 统计实体
     * @return 保存后的统计实体
     */
    UserStatistics saveStatistics(UserStatistics statistics);
    
    /**
     * 统计概览内部类
     */
    class StatisticsOverview {
        private Long totalUsers;
        private Long totalRounds;
        private Long totalAmount;
        private Double avgWinRate;
        private Long maxSingleWin;
        private Long maxSingleLose;
        private Long activeUsers;
        private LocalDateTime lastUpdated;
        
        public StatisticsOverview() {
        }
        
        public StatisticsOverview(Long totalUsers, Long totalRounds, Long totalAmount, Double avgWinRate) {
            this.totalUsers = totalUsers;
            this.totalRounds = totalRounds;
            this.totalAmount = totalAmount;
            this.avgWinRate = avgWinRate;
            this.lastUpdated = LocalDateTime.now();
        }
        
        // Getter and Setter methods
        public Long getTotalUsers() {
            return totalUsers;
        }
        
        public void setTotalUsers(Long totalUsers) {
            this.totalUsers = totalUsers;
        }
        
        public Long getTotalRounds() {
            return totalRounds;
        }
        
        public void setTotalRounds(Long totalRounds) {
            this.totalRounds = totalRounds;
        }
        
        public Long getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(Long totalAmount) {
            this.totalAmount = totalAmount;
        }
        
        public Double getAvgWinRate() {
            return avgWinRate;
        }
        
        public void setAvgWinRate(Double avgWinRate) {
            this.avgWinRate = avgWinRate;
        }
        
        public Long getMaxSingleWin() {
            return maxSingleWin;
        }
        
        public void setMaxSingleWin(Long maxSingleWin) {
            this.maxSingleWin = maxSingleWin;
        }
        
        public Long getMaxSingleLose() {
            return maxSingleLose;
        }
        
        public void setMaxSingleLose(Long maxSingleLose) {
            this.maxSingleLose = maxSingleLose;
        }
        
        public Long getActiveUsers() {
            return activeUsers;
        }
        
        public void setActiveUsers(Long activeUsers) {
            this.activeUsers = activeUsers;
        }
        
        public LocalDateTime getLastUpdated() {
            return lastUpdated;
        }
        
        public void setLastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    }
}