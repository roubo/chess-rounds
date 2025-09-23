package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.CircleLeaderboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 圈子排行榜Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface CircleLeaderboardRepository extends JpaRepository<CircleLeaderboard, Long> {
    
    /**
     * 根据圈子ID和用户ID查找排行榜记录
     * 
     * @param circleId 圈子ID
     * @param userId 用户ID
     * @return 排行榜记录
     */
    Optional<CircleLeaderboard> findByCircleIdAndUserId(Long circleId, Long userId);
    
    /**
     * 根据圈子ID查找排行榜（按积分排序）
     * 
     * @param circleId 圈子ID
     * @param pageable 分页参数
     * @return 排行榜分页列表
     */
    @Query("SELECT cl FROM CircleLeaderboard cl WHERE cl.circleId = :circleId ORDER BY cl.score DESC, cl.winRate DESC, cl.totalGames DESC")
    Page<CircleLeaderboard> findByCircleIdOrderByScore(@Param("circleId") Long circleId, Pageable pageable);
    
    /**
     * 根据圈子ID和赛季查找排行榜
     * 
     * @param circleId 圈子ID
     * @param season 赛季
     * @param pageable 分页参数
     * @return 排行榜分页列表
     */
    @Query("SELECT cl FROM CircleLeaderboard cl WHERE cl.circleId = :circleId AND cl.season = :season ORDER BY cl.score DESC, cl.winRate DESC, cl.totalGames DESC")
    Page<CircleLeaderboard> findByCircleIdAndSeasonOrderByScore(@Param("circleId") Long circleId, @Param("season") String season, Pageable pageable);
    
    /**
     * 根据用户ID查找所有圈子的排行榜记录
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 排行榜分页列表
     */
    Page<CircleLeaderboard> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 查找圈子前N名
     * 
     * @param circleId 圈子ID
     * @param topN 前N名
     * @return 排行榜列表
     */
    @Query("SELECT cl FROM CircleLeaderboard cl WHERE cl.circleId = :circleId ORDER BY cl.score DESC, cl.winRate DESC, cl.totalGames DESC LIMIT :topN")
    List<CircleLeaderboard> findTopNByCircleId(@Param("circleId") Long circleId, @Param("topN") Integer topN);
    
    /**
     * 查找用户在圈子中的排名
     * 
     * @param circleId 圈子ID
     * @param userId 用户ID
     * @return 排名
     */
    @Query("SELECT COUNT(cl) + 1 FROM CircleLeaderboard cl WHERE cl.circleId = :circleId AND " +
           "(cl.score > (SELECT cl2.score FROM CircleLeaderboard cl2 WHERE cl2.circleId = :circleId AND cl2.userId = :userId) OR " +
           "(cl.score = (SELECT cl2.score FROM CircleLeaderboard cl2 WHERE cl2.circleId = :circleId AND cl2.userId = :userId) AND " +
           "cl.winRate > (SELECT cl2.winRate FROM CircleLeaderboard cl2 WHERE cl2.circleId = :circleId AND cl2.userId = :userId)))")
    Integer findUserRankingInCircle(@Param("circleId") Long circleId, @Param("userId") Long userId);
    
    /**
     * 统计圈子参与排行榜的人数
     * 
     * @param circleId 圈子ID
     * @return 参与人数
     */
    Long countByCircleId(Long circleId);
    
    /**
     * 查找积分大于指定值的记录
     * 
     * @param circleId 圈子ID
     * @param minScore 最小积分
     * @param pageable 分页参数
     * @return 排行榜分页列表
     */
    Page<CircleLeaderboard> findByCircleIdAndScoreGreaterThanEqual(Long circleId, BigDecimal minScore, Pageable pageable);
    
    /**
     * 查找胜率大于指定值的记录
     * 
     * @param circleId 圈子ID
     * @param minWinRate 最小胜率
     * @param pageable 分页参数
     * @return 排行榜分页列表
     */
    Page<CircleLeaderboard> findByCircleIdAndWinRateGreaterThanEqual(Long circleId, BigDecimal minWinRate, Pageable pageable);
    
    /**
     * 查找最近有对局的用户
     * 
     * @param circleId 圈子ID
     * @param afterTime 时间点
     * @param pageable 分页参数
     * @return 排行榜分页列表
     */
    @Query("SELECT cl FROM CircleLeaderboard cl WHERE cl.circleId = :circleId AND cl.lastGameAt >= :afterTime ORDER BY cl.lastGameAt DESC")
    Page<CircleLeaderboard> findRecentlyActiveUsers(@Param("circleId") Long circleId, @Param("afterTime") LocalDateTime afterTime, Pageable pageable);
    
    /**
     * 查找连胜王
     * 
     * @param circleId 圈子ID
     * @param pageable 分页参数
     * @return 排行榜分页列表
     */
    @Query("SELECT cl FROM CircleLeaderboard cl WHERE cl.circleId = :circleId ORDER BY cl.consecutiveWins DESC, cl.maxConsecutiveWins DESC")
    Page<CircleLeaderboard> findByConsecutiveWins(@Param("circleId") Long circleId, Pageable pageable);
    
    /**
     * 更新排行榜排名
     * 
     * @param circleId 圈子ID
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE circle_leaderboards cl1 " +
           "JOIN (SELECT id, " +
           "      (SELECT COUNT(*) + 1 FROM circle_leaderboards cl3 " +
           "       WHERE cl3.circle_id = :circleId " +
           "       AND (cl3.score > cl2.score OR (cl3.score = cl2.score AND cl3.win_rate > cl2.win_rate))) as new_ranking " +
           "      FROM circle_leaderboards cl2 " +
           "      WHERE cl2.circle_id = :circleId) as rankings " +
           "ON cl1.id = rankings.id " +
           "SET cl1.ranking = rankings.new_ranking " +
           "WHERE cl1.circle_id = :circleId", nativeQuery = true)
    void updateRankings(@Param("circleId") Long circleId);
    
    /**
     * 删除圈子所有排行榜记录
     * 
     * @param circleId 圈子ID
     */
    void deleteByCircleId(Long circleId);
    
    /**
     * 根据赛季删除排行榜记录
     * 
     * @param circleId 圈子ID
     * @param season 赛季
     */
    void deleteByCircleIdAndSeason(Long circleId, String season);
}