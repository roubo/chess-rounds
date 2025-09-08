package com.airoubo.chessrounds.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 用户登录请求DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class UserLoginRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 微信授权码
     */
    @JsonProperty("code")
    private String code;
    
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
    
    public UserLoginRequest() {
    }
    
    // Getter and Setter methods
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
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
}