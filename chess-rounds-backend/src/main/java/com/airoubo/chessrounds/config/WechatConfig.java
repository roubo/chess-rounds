package com.airoubo.chessrounds.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "app.wechat")
public class WechatConfig {
    
    /**
     * 小程序AppID
     */
    private String appId;
    
    /**
     * 小程序AppSecret
     */
    private String appSecret;
    
    /**
     * 微信API基础URL
     */
    private String apiUrl = "https://api.weixin.qq.com";
    
    public String getAppId() {
        return appId;
    }
    
    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    public String getAppSecret() {
        return appSecret;
    }
    
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}