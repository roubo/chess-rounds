package com.airoubo.chessrounds.dto.record;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建记录请求DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class CreateRecordRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 回合ID
     */
    @JsonProperty("round_id")
    private Long roundId;
    
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
     * 参与者记录列表
     */
    @JsonProperty("participant_records")
    private List<ParticipantRecordRequest> participantRecords;
    
    /**
     * 备注
     */
    @JsonProperty("remarks")
    private String remarks;
    
    public CreateRecordRequest() {
    }
    
    // Getter and Setter methods
    public Long getRoundId() {
        return roundId;
    }
    
    public void setRoundId(Long roundId) {
        this.roundId = roundId;
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
    
    public List<ParticipantRecordRequest> getParticipantRecords() {
        return participantRecords;
    }
    
    public void setParticipantRecords(List<ParticipantRecordRequest> participantRecords) {
        this.participantRecords = participantRecords;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    /**
     * 参与者记录请求内部类
     */
    public static class ParticipantRecordRequest implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 用户ID
         */
        @JsonProperty("user_id")
        private Long userId;
        
        /**
         * 金额变化
         */
        @JsonProperty("amount_change")
        private BigDecimal amountChange;
        
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
        
        public ParticipantRecordRequest() {
        }
        
        // Getter and Setter methods
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public BigDecimal getAmountChange() {
            return amountChange;
        }
        
        public void setAmountChange(BigDecimal amountChange) {
            this.amountChange = amountChange;
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