package com.airoubo.chessrounds.service;

import com.airoubo.chessrounds.dto.wechat.WechatLoginResponse;

/**
 * 微信API服务接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public interface WechatApiService {
    
    /**
     * 通过code获取微信用户信息
     * 
     * @param code 微信授权码
     * @return 微信登录响应
     */
    WechatLoginResponse code2Session(String code);
    
    /**
     * 获取微信访问令牌
     * 
     * @return 访问令牌
     */
    String getAccessToken();
    
    /**
     * 生成小程序码
     * 
     * @param scene 场景参数，最大32个可见字符
     * @param page 页面路径，不能带参数
     * @return 小程序码图片字节数组
     */
    byte[] generateMiniProgramCode(String scene, String page);
}