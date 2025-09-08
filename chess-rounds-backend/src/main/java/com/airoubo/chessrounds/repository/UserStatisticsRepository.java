package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.UserStatistics;
import com.airoubo.chessrounds.enums.StatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 用户统计Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {
    
    /**
     * 根据用户ID查询统计
     * 
     * @param userId 用户ID
     * @return 统计列表
     */
    List<UserStatistics> findByUserId(Long userId);
    
    /**
     * 根据用户ID和统计类型查询统计
     * 
     * @param userId 用户ID
     * @param statType 统计类型
     * @return 统计列表
     */
    List<UserStatistics> findByUserIdAndStatType(Long userId, StatType statType);
    
    /**
     * 根据用户ID、统计日期和统计类型查询统计
     * 
     * @param userId 用户ID
     * @param statDate 统计日期
     * @param statType 统计类型
     * @return 统计信息
     */
    Optional<UserStatistics> findByUserIdAndStatDateAndStatType(Long userId, LocalDate statDate, StatType statType);
    
    /**
     * 根据统计日期和统计类型查询统计
     * 
     * @param statDate 统计日期
     * @param statType 统计类型
     * @return 统计列表
     */
    List<UserStatistics> findByStatDateAndStatType(LocalDate statDate, StatType statType);
    
    /**
     * 查询用户指定时间范围内的统计
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 统计列表
     */
    List<UserStatistics> findByUserIdAndStatDateBetweenAndStatType(Long userId, LocalDate startDate, LocalDate endDate, StatType statType);
    
    /**
     * 查询用户最新的统计记录
     * 
     * @param userId 用户ID
     * @param statType 统计类型
     * @return 统计信息
     */
    @Query("SELECT us FROM UserStatistics us WHERE us.userId = :userId AND us.statType = :statType ORDER BY us.statDate DESC")
    List<UserStatistics> findLatestStatsByUserIdAndStatType(@Param("userId") Long userId, @Param("statType") StatType statType);
    
    /**
     * 查询用户指定日期的所有统计类型
     * 
     * @param userId 用户ID
     * @param statDate 统计日期
     * @return 统计列表
     */
    List<UserStatistics> findByUserIdAndStatDate(Long userId, LocalDate statDate);
    
    /**
     * 统计用户总回合数
     * 
     * @param userId 用户ID
     * @param statType 统计类型
     * @return 总回合数
     */
    @Query("SELECT SUM(us.totalRounds) FROM UserStatistics us WHERE us.userId = :userId AND us.statType = :statType")
    Long sumTotalRoundsByUserIdAndStatType(@Param("userId") Long userId, @Param("statType") StatType statType);
    
    /**
     * 统计用户总胜利回合数
     * 
     * @param userId 用户ID
     * @param statType 统计类型
     * @return 总胜利回合数
     */
    @Query("SELECT SUM(us.winRounds) FROM UserStatistics us WHERE us.userId = :userId AND us.statType = :statType")
    Long sumWinRoundsByUserIdAndStatType(@Param("userId") Long userId, @Param("statType") StatType statType);
    
    /**
     * 统计用户总金额
     * 
     * @param userId 用户ID
     * @param statType 统计类型
     * @return 总金额
     */
    @Query("SELECT SUM(us.totalAmount) FROM UserStatistics us WHERE us.userId = :userId AND us.statType = :statType")
    BigDecimal sumTotalAmountByUserIdAndStatType(@Param("userId") Long userId, @Param("statType") StatType statType);
    
    /**
     * 统计用户总胜利金额
     * 
     * @param userId 用户ID
     * @param statType 统计类型
     * @return 总胜利金额
     */
    @Query("SELECT SUM(us.winAmount) FROM UserStatistics us WHERE us.userId = :userId AND us.statType = :statType")
    BigDecimal sumWinAmountByUserIdAndStatType(@Param("userId") Long userId, @Param("statType") StatType statType);
    
    /**
     * 查询用户最大单次胜利
     * 
     * @param userId 用户ID
     * @return 最大单次胜利金额
     */
    @Query("SELECT MAX(us.maxSingleWin) FROM UserStatistics us WHERE us.userId = :userId")
    BigDecimal findMaxSingleWinByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户最大单次失败
     * 
     * @param userId 用户ID
     * @return 最大单次失败金额
     */
    @Query("SELECT MAX(us.maxSingleLose) FROM UserStatistics us WHERE us.userId = :userId")
    BigDecimal findMaxSingleLoseByUserId(@Param("userId") Long userId);
    
    /**
     * 检查统计是否存在
     * 
     * @param userId 用户ID
     * @param statDate 统计日期
     * @param statType 统计类型
     * @return 是否存在
     */
    boolean existsByUserIdAndStatDateAndStatType(Long userId, LocalDate statDate, StatType statType);
    
    /**
     * 查询排行榜（按总金额排序）
     * 
     * @param statDate 统计日期
     * @param statType 统计类型
     * @param limit 限制数量
     * @return 统计列表
     */
    @Query(value = "SELECT * FROM user_statistics WHERE stat_date = :statDate AND stat_type = :statType ORDER BY total_amount DESC LIMIT :limit", nativeQuery = true)
    List<UserStatistics> findTopUsersByTotalAmount(@Param("statDate") LocalDate statDate, @Param("statType") String statType, @Param("limit") int limit);
    
    /**
     * 查询排行榜（按胜率排序）
     * 
     * @param statDate 统计日期
     * @param statType 统计类型
     * @param minRounds 最少回合数
     * @param limit 限制数量
     * @return 统计列表
     */
    @Query(value = "SELECT * FROM user_statistics WHERE stat_date = :statDate AND stat_type = :statType AND total_rounds >= :minRounds ORDER BY (win_rounds * 1.0 / total_rounds) DESC LIMIT :limit", nativeQuery = true)
    List<UserStatistics> findTopUsersByWinRate(@Param("statDate") LocalDate statDate, @Param("statType") String statType, @Param("minRounds") int minRounds, @Param("limit") int limit);
}