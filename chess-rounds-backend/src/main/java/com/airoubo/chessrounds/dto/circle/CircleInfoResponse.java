package com.airoubo.chessrounds.dto.circle;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 圈子信息响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class CircleInfoResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 圈子ID
     */
    @JsonProperty("circle_id")
    private Long circleId;
    
    /**
     * 圈子名称
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * 圈子代码
     */
    @JsonProperty("circle_code")
    private String circleCode;
    
    /**
     * 圈子描述
     */
    @JsonProperty("description")
    private String description;
    
    /**
     * 圈子头像URL
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    /**
     * 创建者ID
     */
    @JsonProperty("creator_id")
    private Long creatorId;
    
    /**
     * 创建者昵称
     */
    @JsonProperty("creator_nickname")
    private String creatorNickname;
    
    /**
     * 当前成员数
     */
    @JsonProperty("member_count")
    private Integer memberCount;
    
    /**
     * 最大成员数
     */
    @JsonProperty("max_members")
    private Integer maxMembers;
    
    /**
     * 是否公开
     */
    @JsonProperty("is_public")
    private Boolean isPublic;
    
    /**
     * 圈子状态
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * 最后活动时间
     */
    @JsonProperty("last_activity_time")
    private LocalDateTime lastActivityTime;
    
    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 当前用户在圈子中的角色
     */
    @JsonProperty("user_role")
    private String userRole;
    
    /**
     * 当前用户是否已加入
     */
    @JsonProperty("is_member")
    private Boolean isMember;
    
    /**
     * 加入码
     */
    @JsonProperty("join_code")
    private String joinCode;
    
    public CircleInfoResponse() {
    }
    
    public Long getCircleId() {
        return circleId;
    }
    
    public void setCircleId(Long circleId) {
        this.circleId = circleId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCircleCode() {
        return circleCode;
    }
    
    public void setCircleCode(String circleCode) {
        this.circleCode = circleCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getCreatorNickname() {
        return creatorNickname;
    }
    
    public void setCreatorNickname(String creatorNickname) {
        this.creatorNickname = creatorNickname;
    }
    
    public Integer getMemberCount() {
        return memberCount;
    }
    
    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }
    
    public Integer getMaxMembers() {
        return maxMembers;
    }
    
    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }
    
    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    
    public Boolean getIsMember() {
        return isMember;
    }
    
    public void setIsMember(Boolean isMember) {
        this.isMember = isMember;
    }
    
    public String getJoinCode() {
        return joinCode;
    }
    
    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }
}