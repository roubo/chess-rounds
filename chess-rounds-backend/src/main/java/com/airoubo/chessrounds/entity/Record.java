package com.airoubo.chessrounds.entity;

import com.airoubo.chessrounds.enums.RecordType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 记录实体类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "records", indexes = {
    @Index(name = "idx_round_id", columnList = "round_id"),
    @Index(name = "idx_recorder_id", columnList = "recorder_id"),
    @Index(name = "idx_record_type", columnList = "record_type"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_sequence", columnList = "round_id, sequence_number")
})
public class Record extends BaseEntity {
    
    @Column(name = "round_id", nullable = false)
    private Long roundId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", insertable = false, updatable = false)
    private Round round;
    
    @Column(name = "recorder_id", nullable = false)
    private Long recorderId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorder_id", insertable = false, updatable = false)
    private User recorder;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private RecordType recordType = RecordType.WIN;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "participants", columnDefinition = "json")
    private List<Long> participants;
    
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;
    
    @Column(name = "ai_comment", columnDefinition = "TEXT")
    private String aiComment;
    
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
    
    public Long getRecorderId() {
        return recorderId;
    }
    
    public void setRecorderId(Long recorderId) {
        this.recorderId = recorderId;
    }
    
    public User getRecorder() {
        return recorder;
    }
    
    public void setRecorder(User recorder) {
        this.recorder = recorder;
    }
    
    public RecordType getRecordType() {
        return recordType;
    }
    
    public void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<Long> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<Long> participants) {
        this.participants = participants;
    }
    
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public String getAiComment() {
        return aiComment;
    }
    
    public void setAiComment(String aiComment) {
        this.aiComment = aiComment;
    }
}