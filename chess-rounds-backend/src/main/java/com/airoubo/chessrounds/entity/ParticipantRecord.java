package com.airoubo.chessrounds.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 参与者记录实体类 - 存储每个参与者在每局游戏中的得分情况
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "participant_records", indexes = {
    @Index(name = "idx_record_id", columnList = "record_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_round_id", columnList = "round_id")
})
public class ParticipantRecord extends BaseEntity {
    
    @Column(name = "record_id", nullable = false)
    private Long recordId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", insertable = false, updatable = false)
    private Record record;
    
    @Column(name = "round_id", nullable = false)
    private Long roundId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", insertable = false, updatable = false)
    private Round round;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(name = "amount_change", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountChange;
    
    @Column(name = "balance_before", precision = 15, scale = 2)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;
    
    @Column(name = "is_winner")
    private Boolean isWinner = false;
    
    @Column(name = "remarks", length = 500)
    private String remarks;
    
    public ParticipantRecord() {
    }
    
    // Getter and Setter methods
    public Long getRecordId() {
        return recordId;
    }
    
    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }
    
    public Record getRecord() {
        return record;
    }
    
    public void setRecord(Record record) {
        this.record = record;
    }
    
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public BigDecimal getAmountChange() {
        return amountChange;
    }
    
    public void setAmountChange(BigDecimal amountChange) {
        this.amountChange = amountChange;
    }
    
    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }
    
    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }
    
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    public Boolean getIsWinner() {
        return isWinner;
    }
    
    public void setIsWinner(Boolean isWinner) {
        this.isWinner = isWinner;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}