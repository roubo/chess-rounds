package com.airoubo.chessrounds.entity;

import com.airoubo.chessrounds.enums.RatingType;

import jakarta.persistence.*;

/**
 * 评价实体类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "ratings", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_rating", columnNames = {"round_id", "from_user_id", "to_user_id"})
    },
    indexes = {
        @Index(name = "idx_round_id", columnList = "round_id"),
        @Index(name = "idx_from_user", columnList = "from_user_id"),
        @Index(name = "idx_to_user", columnList = "to_user_id"),
        @Index(name = "idx_rating_type", columnList = "rating_type")
    }
)
public class Rating extends BaseEntity {
    
    @Column(name = "round_id", nullable = false)
    private Long roundId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", insertable = false, updatable = false)
    private Round round;
    
    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", insertable = false, updatable = false)
    private User fromUser;
    
    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", insertable = false, updatable = false)
    private User toUser;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rating_type", nullable = false)
    private RatingType ratingType;
    
    @Column(name = "comment", length = 200)
    private String comment;
    
    // Getters and Setters
    public Long getRoundId() {
        return roundId;
    }
    
    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }
    
    public Round getRound() {
        return round;
    }
    
    public void setRound(Round round) {
        this.round = round;
    }
    
    public Long getFromUserId() {
        return fromUserId;
    }
    
    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }
    
    public User getFromUser() {
        return fromUser;
    }
    
    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }
    
    public Long getToUserId() {
        return toUserId;
    }
    
    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }
    
    public User getToUser() {
        return toUser;
    }
    
    public void setToUser(User toUser) {
        this.toUser = toUser;
    }
    
    public RatingType getRatingType() {
        return ratingType;
    }
    
    public void setRatingType(RatingType ratingType) {
        this.ratingType = ratingType;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
}