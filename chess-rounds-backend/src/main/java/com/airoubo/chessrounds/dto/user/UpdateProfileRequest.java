package com.airoubo.chessrounds.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 更新用户资料请求DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class UpdateProfileRequest {
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像URL
     */
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    
    /**
     * 用户性别（0-未知，1-男，2-女）
     */
    private Integer gender;
    
    /**
     * 用户所在城市
     */
    private String city;
    
    /**
     * 用户所在省份
     */
    private String province;
    
    /**
     * 用户所在国家
     */
    private String country;
    
    // 构造函数
    public UpdateProfileRequest() {
    }
    
    public UpdateProfileRequest(String nickname, String avatarUrl) {
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }
    
    // Getter and Setter methods
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
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    @Override
    public String toString() {
        return "UpdateProfileRequest{" +
                "nickname='" + nickname + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", gender='" + gender + '\'' +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}