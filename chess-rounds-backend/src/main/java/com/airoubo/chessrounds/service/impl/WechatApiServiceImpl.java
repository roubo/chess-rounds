package com.airoubo.chessrounds.service.impl;

import com.airoubo.chessrounds.config.WechatConfig;
import com.airoubo.chessrounds.dto.wechat.WechatLoginResponse;
import com.airoubo.chessrounds.service.WechatApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信API服务实现类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Service
public class WechatApiServiceImpl implements WechatApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(WechatApiServiceImpl.class);
    
    @Autowired
    private WechatConfig wechatConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 缓存access_token，避免频繁请求
    private String cachedAccessToken;
    private long tokenExpireTime = 0;
    
    @Override
    public WechatLoginResponse code2Session(String code) {
        try {
            String url = String.format("%s/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wechatConfig.getApiUrl(),
                    wechatConfig.getAppId(),
                    wechatConfig.getAppSecret(),
                    code);
            
            logger.info("调用微信登录接口: {}", url.replaceAll("secret=[^&]*", "secret=***"));
            
            WechatLoginResponse response = restTemplate.getForObject(url, WechatLoginResponse.class);
            
            if (response != null && !response.isSuccess()) {
                logger.error("微信登录失败: errcode={}, errmsg={}", response.getErrCode(), response.getErrMsg());
                throw new RuntimeException("微信登录失败: " + response.getErrMsg());
            }
            
            logger.info("微信登录成功: openid={}", response != null ? response.getOpenId() : "null");
            return response;
            
        } catch (Exception e) {
            logger.error("调用微信登录接口异常", e);
            throw new RuntimeException("微信登录接口调用失败", e);
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
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);
            
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
            
        } catch (Exception e) {
            logger.error("获取微信访问令牌异常", e);
            throw new RuntimeException("获取微信访问令牌失败", e);
        }
    }
    
    @Override
    public byte[] generateMiniProgramCode(String scene, String page) {
        try {
            String accessToken = getAccessToken();
            String url = wechatConfig.getApiUrl() + "/wxa/getwxacodeunlimit?access_token=" + accessToken;
            
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("scene", scene);
            params.put("page", page);
            params.put("width", 430);
            params.put("auto_color", false);
            
            // 设置线条颜色
            Map<String, Integer> lineColor = new HashMap<>();
            lineColor.put("r", 93);
            lineColor.put("g", 104);
            lineColor.put("b", 138);
            params.put("line_color", lineColor);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
            
            logger.info("生成小程序码: scene={}, page={}", scene, page);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, byte[].class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                byte[] result = response.getBody();
                if (result != null && result.length > 0) {
                    // 检查返回的是否是错误信息（JSON格式）
                    String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                    if (contentType != null && contentType.contains("application/json")) {
                        String errorMsg = new String(result);
                        logger.error("生成小程序码失败: {}", errorMsg);
                        throw new RuntimeException("生成小程序码失败: " + errorMsg);
                    }
                    logger.info("生成小程序码成功，大小: {} bytes", result.length);
                    return result;
                }
            }
            
            throw new RuntimeException("生成小程序码失败，响应为空");
            
        } catch (Exception e) {
            logger.error("生成小程序码异常", e);
            throw new RuntimeException("生成小程序码失败", e);
        }
    }
}