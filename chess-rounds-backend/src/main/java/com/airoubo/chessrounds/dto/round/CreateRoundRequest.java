package com.airoubo.chessrounds.dto.round;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建回合请求DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class CreateRoundRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 麻将类型
     */
    @JsonProperty("game_type")
    private String gameType;
    
    /**
     * 最大参与人数
     */
    @JsonProperty("max_participants")
    private Integer maxParticipants = 4;
    
    /**
     * 底注金额
     */
    @JsonProperty("base_amount")
    private BigDecimal baseAmount;
    
    /**
     * 是否有台板
     */
    @JsonProperty("has_table")
    private Boolean hasTable = false;
    
    /**
     * 台板用户ID
     */
    @JsonProperty("table_user_id")
    private Long tableUserId;
    
    /**
     * 是否公开
     */
    @JsonProperty("is_public")
    private Boolean isPublic = true;
    
    /**
     * 回合密码（私有回合）
     */
    @JsonProperty("password")
    private String password;
    
    /**
     * 是否允许旁观
     */
    @JsonProperty("allow_spectator")
    private Boolean allowSpectator = true;
    
    /**
     * 自动开始时间（分钟）
     */
    @JsonProperty("auto_start_minutes")
    private Integer autoStartMinutes;
    
    public CreateRoundRequest() {
    }
    
    // Getter and Setter methods
    public String getGameType() {
        return gameType;
    }
    
    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
    
    public Integer getMaxParticipants() {
        return maxParticipants;
    }
    
    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
    
    public BigDecimal getBaseAmount() {
        return baseAmount;
    }
    
    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Boolean getHasTable() {
        return hasTable;
    }
    
    public void setHasTable(Boolean hasTable) {
        this.hasTable = hasTable;
    }
    
    public Long getTableUserId() {
        return tableUserId;
    }
    
    public void setTableUserId(Long tableUserId) {
        this.tableUserId = tableUserId;
    }
    
    public Boolean getAllowSpectator() {
        return allowSpectator;
    }
    
    public void setAllowSpectator(Boolean allowSpectator) {
        this.allowSpectator = allowSpectator;
    }
    
    public Integer getAutoStartMinutes() {
        return autoStartMinutes;
    }
    
    public void setAutoStartMinutes(Integer autoStartMinutes) {
        this.autoStartMinutes = autoStartMinutes;
    }
}