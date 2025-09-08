package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.Rating;
import com.airoubo.chessrounds.enums.RatingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评价Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    /**
     * 根据回合ID查询评价
     * 
     * @param roundId 回合ID
     * @return 评价列表
     */
    List<Rating> findByRoundId(Long roundId);
    
    /**
     * 根据评价者ID查询评价
     * 
     * @param fromUserId 评价者ID
     * @param pageable 分页参数
     * @return 评价分页列表
     */
    Page<Rating> findByFromUserId(Long fromUserId, Pageable pageable);
    
    /**
     * 根据被评价者ID查询评价
     * 
     * @param toUserId 被评价者ID
     * @param pageable 分页参数
     * @return 评价分页列表
     */
    Page<Rating> findByToUserId(Long toUserId, Pageable pageable);
    
    /**
     * 根据回合ID、评价者ID和被评价者ID查询评价
     * 
     * @param roundId 回合ID
     * @param fromUserId 评价者ID
     * @param toUserId 被评价者ID
     * @return 评价信息
     */
    Optional<Rating> findByRoundIdAndFromUserIdAndToUserId(Long roundId, Long fromUserId, Long toUserId);
    
    /**
     * 根据回合ID和评价类型查询评价
     * 
     * @param roundId 回合ID
     * @param ratingType 评价类型
     * @return 评价列表
     */
    List<Rating> findByRoundIdAndRatingType(Long roundId, RatingType ratingType);
    
    /**
     * 根据被评价者ID和评价类型查询评价
     * 
     * @param toUserId 被评价者ID
     * @param ratingType 评价类型
     * @return 评价列表
     */
    List<Rating> findByToUserIdAndRatingType(Long toUserId, RatingType ratingType);
    
    /**
     * 统计回合评价数量
     * 
     * @param roundId 回合ID
     * @return 评价数量
     */
    Long countByRoundId(Long roundId);
    
    /**
     * 统计用户收到的评价数量
     * 
     * @param toUserId 被评价者ID
     * @return 评价数量
     */
    Long countByToUserId(Long toUserId);
    
    /**
     * 统计用户收到的指定类型评价数量
     * 
     * @param toUserId 被评价者ID
     * @param ratingType 评价类型
     * @return 评价数量
     */
    Long countByToUserIdAndRatingType(Long toUserId, RatingType ratingType);
    
    /**
     * 统计用户给出的评价数量
     * 
     * @param fromUserId 评价者ID
     * @return 评价数量
     */
    Long countByFromUserId(Long fromUserId);
    
    /**
     * 检查评价是否存在
     * 
     * @param roundId 回合ID
     * @param fromUserId 评价者ID
     * @param toUserId 被评价者ID
     * @return 是否存在
     */
    boolean existsByRoundIdAndFromUserIdAndToUserId(Long roundId, Long fromUserId, Long toUserId);
    
    /**
     * 查询用户在回合中给出的评价
     * 
     * @param roundId 回合ID
     * @param fromUserId 评价者ID
     * @return 评价列表
     */
    List<Rating> findByRoundIdAndFromUserId(Long roundId, Long fromUserId);
    
    /**
     * 查询用户在回合中收到的评价
     * 
     * @param roundId 回合ID
     * @param toUserId 被评价者ID
     * @return 评价列表
     */
    List<Rating> findByRoundIdAndToUserId(Long roundId, Long toUserId);
    
    /**
     * 查询用户收到的赞评价统计
     * 
     * @param toUserId 被评价者ID
     * @return 赞评价数量
     */
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.toUserId = :toUserId AND r.ratingType = 'LIKE'")
    Long countLikesByToUserId(@Param("toUserId") Long toUserId);
    
    /**
     * 查询用户收到的贬评价统计
     * 
     * @param toUserId 被评价者ID
     * @return 贬评价数量
     */
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.toUserId = :toUserId AND r.ratingType = 'DISLIKE'")
    Long countDislikesByToUserId(@Param("toUserId") Long toUserId);
    
    /**
     * 删除回合的所有评价
     * 
     * @param roundId 回合ID
     */
    void deleteByRoundId(Long roundId);
}