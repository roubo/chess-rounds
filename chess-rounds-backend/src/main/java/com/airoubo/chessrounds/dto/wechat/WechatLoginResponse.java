package com.airoubo.chessrounds.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 微信登录响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class WechatLoginResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户唯一标识
     */
    @JsonProperty("openid")
    private String openId;
    
    /**
     * 会话密钥
     */
    @JsonProperty("session_key")
    private String sessionKey;
    
    /**
     * 用户在开放平台的唯一标识符
     */
    @JsonProperty("unionid")
    private String unionId;
    
    /**
     * 错误码
     */
    @JsonProperty("errcode")
    private Integer errCode;
    
    /**
     * 错误信息
     */
    @JsonProperty("errmsg")
    private String errMsg;
    
    public WechatLoginResponse() {
    }
    
    public String getOpenId() {
        return openId;
    }
    
    public void setOpenId(String openId) {
        this.openId = openId;
    }
    
    public String getSessionKey() {
        return sessionKey;
    }
    
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
    
    public String getUnionId() {
        return unionId;
    }
    
    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }
    
    public Integer getErrCode() {
        return errCode;
    }
    
    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }
    
    public String getErrMsg() {
        return errMsg;
    }
    
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return errCode == null || errCode == 0;
    }
}