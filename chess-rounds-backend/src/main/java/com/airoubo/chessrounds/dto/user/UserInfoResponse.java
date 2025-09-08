package com.airoubo.chessrounds.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class UserInfoResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Long userId;
    
    /**
     * 微信OpenID
     */
    @JsonProperty("openid")
    private String openid;
    
    /**
     * 微信UnionID
     */
    @JsonProperty("unionid")
    private String unionid;
    
    /**
     * 用户昵称
     */
    @JsonProperty("nickname")
    private String nickname;
    
    /**
     * 用户头像URL
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    /**
     * 用户性别（0-未知，1-男，2-女）
     */
    @JsonProperty("gender")
    private Integer gender;
    
    /**
     * 用户所在国家
     */
    @JsonProperty("country")
    private String country;
    
    /**
     * 用户所在省份
     */
    @JsonProperty("province")
    private String province;
    
    /**
     * 用户所在城市
     */
    @JsonProperty("city")
    private String city;
    
    /**
     * 用户语言
     */
    @JsonProperty("language")
    private String language;
    
    /**
     * 用户状态
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * 最后登录时间
     */
    @JsonProperty("last_login_time")
    private LocalDateTime lastLoginTime;
    
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
    
    public UserInfoResponse() {
    }
    
    // Getter and Setter methods
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getOpenid() {
        return openid;
    }
    
    public void setOpenid(String openid) {
        this.openid = openid;
    }
    
    public String getUnionid() {
        return unionid;
    }
    
    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public Integer getGender() {
        return gender;
    }
    
    public void setGender(Integer gender) {
        this.gender = gender;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
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
}