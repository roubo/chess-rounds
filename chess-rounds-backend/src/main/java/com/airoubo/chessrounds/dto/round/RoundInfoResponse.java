package com.airoubo.chessrounds.dto.round;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.airoubo.chessrounds.dto.user.UserInfoResponse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 回合信息响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class RoundInfoResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 回合ID
     */
    @JsonProperty("round_id")
    private Long roundId;
    
    /**
     * 回合码
     */
    @JsonProperty("round_code")
    private String roundCode;
    
    /**
     * 麻将类型
     */
    @JsonProperty("game_type")
    private String gameType;
    
    /**
     * 创建者信息
     */
    @JsonProperty("creator")
    private UserInfoResponse creator;
    
    /**
     * 是否有台板
     */
    @JsonProperty("has_table")
    private Boolean hasTable;
    
    /**
     * 台板用户信息
     */
    @JsonProperty("table_user")
    private UserInfoResponse tableUser;
    
    /**
     * 回合状态
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * 最大参与人数
     */
    @JsonProperty("max_participants")
    private Integer maxParticipants;
    
    /**
     * 当前参与人数
     */
    @JsonProperty("current_participants")
    private Integer currentParticipants;
    
    /**
     * 底注金额
     */
    @JsonProperty("base_amount")
    private BigDecimal baseAmount;
    
    /**
     * 当前总金额
     */
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    /**
     * 是否公开
     */
    @JsonProperty("is_public")
    private Boolean isPublic;
    
    /**
     * 是否需要密码
     */
    @JsonProperty("has_password")
    private Boolean hasPassword;
    
    /**
     * 是否允许旁观
     */
    @JsonProperty("allow_spectator")
    private Boolean allowSpectator;
    
    /**
     * 当前旁观者数量
     */
    @JsonProperty("spectator_count")
    private Integer spectatorCount;
    
    /**
     * 开始时间
     */
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @JsonProperty("end_time")
    private LocalDateTime endTime;
    
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
     * 参与者列表
     */
    @JsonProperty("participants")
    private List<ParticipantInfoResponse> participants;
    
    /**
     * 是否可以加入
     */
    @JsonProperty("can_join")
    private Boolean canJoin;
    
    /**
     * 当前用户角色
     */
    @JsonProperty("current_user_role")
    private String currentUserRole;
    
    public RoundInfoResponse() {
    }
    
    // Getter and Setter methods
    public Long getRoundId() {
        return roundId;
    }
    
    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }
    
    public String getRoundCode() {
        return roundCode;
    }
    
    public void setRoundCode(String roundCode) {
        this.roundCode = roundCode;
    }
    
    public String getGameType() {
        return gameType;
    }
    
    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
    
    public Boolean getHasTable() {
        return hasTable;
    }
    
    public void setHasTable(Boolean hasTable) {
        this.hasTable = hasTable;
    }
    
    public UserInfoResponse getTableUser() {
        return tableUser;
    }
    
    public void setTableUser(UserInfoResponse tableUser) {
        this.tableUser = tableUser;
    }
    
    public UserInfoResponse getCreator() {
        return creator;
    }
    
    public void setCreator(UserInfoResponse creator) {
        this.creator = creator;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getMaxParticipants() {
        return maxParticipants;
    }
    
    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
    
    public Integer getCurrentParticipants() {
        return currentParticipants;
    }
    
    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }
    
    public BigDecimal getBaseAmount() {
        return baseAmount;
    }
    
    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Boolean getHasPassword() {
        return hasPassword;
    }
    
    public void setHasPassword(Boolean hasPassword) {
        this.hasPassword = hasPassword;
    }
    
    public Boolean getAllowSpectator() {
        return allowSpectator;
    }
    
    public void setAllowSpectator(Boolean allowSpectator) {
        this.allowSpectator = allowSpectator;
    }
    
    public Integer getSpectatorCount() {
        return spectatorCount;
    }
    
    public void setSpectatorCount(Integer spectatorCount) {
        this.spectatorCount = spectatorCount;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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
    
    public List<ParticipantInfoResponse> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<ParticipantInfoResponse> participants) {
        this.participants = participants;
    }
    
    public Boolean getCanJoin() {
        return canJoin;
    }
    
    public void setCanJoin(Boolean canJoin) {
        this.canJoin = canJoin;
    }
    
    public String getCurrentUserRole() {
        return currentUserRole;
    }
    
    public void setCurrentUserRole(String currentUserRole) {
        this.currentUserRole = currentUserRole;
    }
}