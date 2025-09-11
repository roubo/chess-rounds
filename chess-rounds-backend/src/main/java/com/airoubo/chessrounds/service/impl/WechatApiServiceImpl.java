package com.airoubo.chessrounds.service.impl;

import com.airoubo.chessrounds.config.WechatConfig;
import com.airoubo.chessrounds.dto.wechat.WechatLoginResponse;
import com.airoubo.chessrounds.service.WechatApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信API服务实现类
 * 使用OkHttp客户端进行HTTP请求
 */
@Service
public class WechatApiServiceImpl implements WechatApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(WechatApiServiceImpl.class);
    
    @Autowired
    private WechatConfig wechatConfig;
    
    @Autowired
    private OkHttpClient okHttpClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 缓存access_token
    private String cachedAccessToken;
    private long tokenExpireTime = 0;
    
    @Override
    public WechatLoginResponse code2Session(String code) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(wechatConfig.getApiUrl() + "/sns/jscode2session")
                    .queryParam("appid", wechatConfig.getAppId())
                    .queryParam("secret", wechatConfig.getAppSecret())
                    .queryParam("js_code", code)
                    .queryParam("grant_type", "authorization_code")
                    .toUriString();
            
            logger.info("微信登录验证: {}", url.replaceAll("secret=[^&]*", "secret=***"));
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("微信登录验证失败，HTTP状态码: " + response.code());
                }
                
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, WechatLoginResponse.class);
            }
            
        } catch (Exception e) {
            logger.error("微信登录验证异常", e);
            throw new RuntimeException("微信登录验证失败", e);
        }
    }
    
    @Override
    public String getAccessToken() {
        try {
            // 检查缓存的token是否还有效（提前5分钟刷新）
            if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime - 300000) {
                return cachedAccessToken;
            }
            
            String url = UriComponentsBuilder
                    .fromHttpUrl(wechatConfig.getApiUrl() + "/cgi-bin/token")
                    .queryParam("grant_type", "client_credential")
                    .queryParam("appid", wechatConfig.getAppId())
                    .queryParam("secret", wechatConfig.getAppSecret())
                    .toUriString();
            
            logger.info("获取微信访问令牌: {}", url.replaceAll("secret=[^&]*", "secret=***"));
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("获取访问令牌失败，HTTP状态码: " + response.code());
                }
                
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                if (jsonNode.has("errcode")) {
                    logger.error("获取访问令牌失败: errcode={}, errmsg={}", 
                        jsonNode.get("errcode").asInt(), jsonNode.get("errmsg").asText());
                    throw new RuntimeException("获取访问令牌失败: " + jsonNode.get("errmsg").asText());
                }
                
                cachedAccessToken = jsonNode.get("access_token").asText();
                int expiresIn = jsonNode.get("expires_in").asInt();
                tokenExpireTime = System.currentTimeMillis() + expiresIn * 1000L;
                
                logger.info("获取访问令牌成功，有效期: {} 秒", expiresIn);
                return cachedAccessToken;
            }
            
        } catch (Exception e) {
            logger.error("获取微信访问令牌异常", e);
            throw new RuntimeException("获取微信访问令牌失败", e);
        }
    }
    
    @Override
    public byte[] generateMiniProgramCode(String scene, String page) {
        try {
            String accessToken = getAccessToken();
            logger.info("获取到access_token: {}", accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : "null");
            
            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("无法获取有效的access_token");
            }
            
            // 验证access_token是否有效
            if (!isAccessTokenValid(accessToken)) {
                logger.warn("当前access_token可能已失效，尝试重新获取");
                // 清除缓存的token，强制重新获取
                cachedAccessToken = null;
                tokenExpireTime = 0;
                accessToken = getAccessToken();
                logger.info("重新获取access_token: {}", accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : "null");
            }
            
            String url = wechatConfig.getApiUrl() + "/wxa/getwxacodeunlimit?access_token=" + accessToken;
            
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            
            // scene参数长度不能超过32个字符，如果scene包含"roundId="则提取数值部分
            String sceneValue = scene;
            if (scene.startsWith("roundId=")) {
                sceneValue = scene.substring(8); // 提取roundId=后面的值
            }
            
            // 设置小程序码参数
            params.put("scene", sceneValue);
            if (page != null && !page.isEmpty()) {
                params.put("page", page);
            }
            // 二维码样式参数
            params.put("width", 430);  // 二维码宽度，单位px，最大1280px，最小280px
            params.put("auto_color", false);  // 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
            params.put("check_path", false);  // 检查page是否存在，为true时page必须是已经发布的小程序存在的页面
            params.put("env_version", "release");  // 要打开的小程序版本。正式版为"release"，体验版为"trial"，开发版为"develop"
            
            // 设置线条颜色为深蓝色，提升视觉效果
            Map<String, Object> lineColor = new HashMap<>();
            lineColor.put("r", 93);   // RGB红色分量
            lineColor.put("g", 104);  // RGB绿色分量  
            lineColor.put("b", 138);  // RGB蓝色分量
            params.put("line_color", lineColor);
            
            // 构建JSON请求体
            String jsonBody = objectMapper.writeValueAsString(params);
            RequestBody requestBody = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            
            logger.info("生成小程序码: 原始scene={}, 处理后scene={}, page={}", scene, sceneValue, page);
            logger.info("Request URL: {}", url);
            logger.info("Request body: {}", params);
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                logger.info("Response status: {}", response.code());
                logger.info("Response headers: {}", response.headers());
                
                // 处理412 Precondition Failed错误
                if (response.code() == 412) {
                    logger.error("微信API返回412错误，可能是请求参数不符合要求");
                    if (response.body() != null) {
                        String errorBody = response.body().string();
                        logger.error("412错误响应内容: {}", errorBody);
                        throw new RuntimeException("微信API请求失败(412): " + errorBody);
                    } else {
                        throw new RuntimeException("微信API请求失败(412): 请求参数不符合微信API要求");
                    }
                }
                
                if (response.isSuccessful()) {
                    byte[] result = response.body().bytes();
                    if (result != null && result.length > 0) {
                        // 检查返回的是否是错误信息（JSON格式）
                        String contentType = response.header("Content-Type");
                        if (contentType != null && contentType.contains("application/json")) {
                            String errorMsg = new String(result);
                            logger.error("生成小程序码失败: {}", errorMsg);
                            throw new RuntimeException("生成小程序码失败: " + errorMsg);
                        }
                        logger.info("生成小程序码成功，大小: {} bytes", result.length);
                        return result;
                    }
                } else {
                    logger.error("生成小程序码失败，状态码: {}", response.code());
                    if (response.body() != null) {
                        String errorBody = response.body().string();
                        logger.error("错误响应内容: {}", errorBody);
                    }
                }
                
                throw new RuntimeException("生成小程序码失败，响应为空或状态码异常: " + response.code());
            }
            
        } catch (Exception e) {
            logger.error("生成小程序码异常", e);
            throw new RuntimeException("生成小程序码失败", e);
        }
    }
    
    /**
     * 验证access_token是否有效
     * 通过调用微信API的一个简单接口来验证token
     */
    private boolean isAccessTokenValid(String accessToken) {
        try {
            String testUrl = wechatConfig.getApiUrl() + "/cgi-bin/get_api_domain_ip?access_token=" + accessToken;
            
            Request request = new Request.Builder()
                    .url(testUrl)
                    .get()
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return false;
                }
                
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                // 如果没有errcode或errcode为0，说明token有效
                return !jsonNode.has("errcode") || jsonNode.get("errcode").asInt() == 0;
            }
        } catch (Exception e) {
            logger.warn("验证access_token时发生异常: {}", e.getMessage());
            return false;
        }
    }
}