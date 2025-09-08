package com.airoubo.chessrounds.dto.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.airoubo.chessrounds.dto.user.UserInfoResponse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 记录信息响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class RecordInfoResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 记录ID
     */
    @JsonProperty("record_id")
    private Long recordId;
    
    /**
     * 回合ID
     */
    @JsonProperty("round_id")
    private Long roundId;
    
    /**
     * 记录序号
     */
    @JsonProperty("sequence_number")
    private Integer sequenceNumber;
    
    /**
     * 记录者信息
     */
    @JsonProperty("recorder")
    private UserInfoResponse recorder;
    
    /**
     * 记录类型
     */
    @JsonProperty("record_type")
    private String recordType;
    
    /**
     * 记录描述
     */
    @JsonProperty("description")
    private String description;
    
    /**
     * 总金额
     */
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    /**
     * 参与者数量
     */
    @JsonProperty("participant_count")
    private Integer participantCount;
    
    /**
     * 赢家数量
     */
    @JsonProperty("winner_count")
    private Integer winnerCount;
    
    /**
     * 备注
     */
    @JsonProperty("remarks")
    private String remarks;
    
    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 参与者记录详情
     */
    @JsonProperty("participant_details")
    private List<ParticipantRecordResponse> participantDetails;
    
    public RecordInfoResponse() {
    }
    
    // Getter and Setter methods
    public Long getRecordId() {
        return recordId;
    }
    
    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }
    
    public Long getRoundId() {
        return roundId;
    }
    
    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }
    
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public UserInfoResponse getRecorder() {
        return recorder;
    }
    
    public void setRecorder(UserInfoResponse recorder) {
        this.recorder = recorder;
    }
    
    public String getRecordType() {
        return recordType;
    }
    
    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Integer getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
    
    public Integer getWinnerCount() {
        return winnerCount;
    }
    
    public void setWinnerCount(Integer winnerCount) {
        this.winnerCount = winnerCount;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<ParticipantRecordResponse> getParticipantDetails() {
        return participantDetails;
    }
    
    public void setParticipantDetails(List<ParticipantRecordResponse> participantDetails) {
        this.participantDetails = participantDetails;
    }
    
    /**
     * 参与者记录响应内部类
     */
    public static class ParticipantRecordResponse implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 用户信息
         */
        @JsonProperty("user_info")
        private UserInfoResponse userInfo;
        
        /**
         * 金额变化
         */
        @JsonProperty("amount_change")
        private BigDecimal amountChange;
        
        /**
         * 变化后余额
         */
        @JsonProperty("balance_after")
        private BigDecimal balanceAfter;
        
        /**
         * 是否为赢家
         */
        @JsonProperty("is_winner")
        private Boolean isWinner;
        
        /**
         * 备注
         */
        @JsonProperty("remarks")
        private String remarks;
        
        public ParticipantRecordResponse() {
        }
        
        // Getter and Setter methods
        public UserInfoResponse getUserInfo() {
            return userInfo;
        }
        
        public void setUserInfo(UserInfoResponse userInfo) {
            this.userInfo = userInfo;
        }
        
        public BigDecimal getAmountChange() {
            return amountChange;
        }
        
        public void setAmountChange(BigDecimal amountChange) {
            this.amountChange = amountChange;
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
}