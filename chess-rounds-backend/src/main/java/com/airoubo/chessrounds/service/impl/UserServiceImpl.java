package com.airoubo.chessrounds.service.impl;

import com.airoubo.chessrounds.dto.user.UpdateProfileRequest;
import com.airoubo.chessrounds.dto.user.UserInfoResponse;
import com.airoubo.chessrounds.dto.user.UserLoginRequest;
import com.airoubo.chessrounds.dto.user.UserLoginResponse;
import com.airoubo.chessrounds.dto.wechat.WechatLoginResponse;
import com.airoubo.chessrounds.entity.User;
import com.airoubo.chessrounds.entity.ParticipantRecord;
import com.airoubo.chessrounds.enums.RoundStatus;
import com.airoubo.chessrounds.repository.UserRepository;
import com.airoubo.chessrounds.repository.ParticipantRecordRepository;
import com.airoubo.chessrounds.service.UserService;
import com.airoubo.chessrounds.service.WechatApiService;
import com.airoubo.chessrounds.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户服务实现类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WechatApiService wechatApiService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ParticipantRecordRepository participantRecordRepository;
    
    @Override
    public UserLoginResponse login(UserLoginRequest loginRequest) {
        try {
            logger.info("开始微信登录，code: {}", loginRequest.getCode());
            
            // 1. 通过微信code获取openid和session_key
            WechatLoginResponse wechatResponse = wechatApiService.code2Session(loginRequest.getCode());
            if (wechatResponse == null || !wechatResponse.isSuccess()) {
                throw new RuntimeException("微信登录失败: " + (wechatResponse != null ? wechatResponse.getErrMsg() : "未知错误"));
            }
            
            String openId = wechatResponse.getOpenId();
            String sessionKey = wechatResponse.getSessionKey();
            String unionId = wechatResponse.getUnionId();
            
            logger.info("微信登录成功，openId: {}", openId);
            
            // 2. 根据openid查找或创建用户
            Optional<User> existingUser = userRepository.findByOpenid(openId);
            
            User user;
            boolean isNewUser = false;
            
            if (existingUser.isPresent()) {
                user = existingUser.get();
                logger.info("找到已存在用户，userId: {}", user.getId());
                
                // 只更新会话信息，不覆盖用户的昵称和头像
                // 避免每次登录时用默认的微信信息覆盖用户自定义的信息
                user.setSessionKey(sessionKey);
                user.setLastLoginAt(LocalDateTime.now());
            } else {
                // 创建新用户
                isNewUser = true;
                user = new User();
                user.setOpenid(openId);
                user.setUnionid(unionId);
                user.setSessionKey(sessionKey);
                user.setNickname(loginRequest.getNickname() != null && !loginRequest.getNickname().isEmpty() ? loginRequest.getNickname() : "微信用户");
				user.setAvatarUrl(loginRequest.getAvatarUrl() != null && !loginRequest.getAvatarUrl().isEmpty() ? loginRequest.getAvatarUrl() : "/static/default-avatar.png");
                user.setGender(loginRequest.getGender());
                user.setCountry(loginRequest.getCountry());
                user.setProvince(loginRequest.getProvince());
                user.setCity(loginRequest.getCity());
                user.setLanguage(loginRequest.getLanguage());
                user.setStatus(1); // 1-正常
                user.setCreatedAt(LocalDateTime.now());
                user.setLastLoginAt(LocalDateTime.now());
                
                logger.info("创建新用户，openId: {}", openId);
            }
            
            user = userRepository.save(user);
            
            // 3. 生成访问令牌和刷新令牌
            String accessToken = generateAccessToken(user.getId());
            String refreshToken = generateRefreshToken(user.getId());
            
            // 4. 构建用户信息响应
            UserInfoResponse userInfo = convertToUserInfoResponse(user);
            
            UserLoginResponse response = new UserLoginResponse();
            response.setUserId(user.getId());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setUserInfo(userInfo);
            
            logger.info("用户登录成功，userId: {}, isNewUser: {}", user.getId(), isNewUser);
            return response;
            
        } catch (Exception e) {
            logger.error("用户登录失败", e);
            throw new RuntimeException("登录失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String refreshToken(String refreshToken) {
        try {
            // 1. 验证刷新令牌
            if (!jwtUtil.isTokenValid(refreshToken)) {
                throw new RuntimeException("刷新令牌无效或已过期");
            }
            
            // 2. 提取用户ID
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            
            // 3. 生成新的访问令牌
            return generateAccessToken(userId);
        } catch (Exception e) {
            logger.error("刷新令牌失败", e);
            throw new RuntimeException("刷新令牌失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void logout(Long userId) {
        // TODO: 实现登出逻辑
        // 1. 清除用户的令牌缓存
        // 2. 记录登出日志
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserInfoResponse> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::convertToUserInfoResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserInfoResponse> getUserByOpenid(String openid) {
        return userRepository.findByOpenid(openid)
                .map(this::convertToUserInfoResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserInfoResponse> getUserByUnionid(String unionid) {
        return userRepository.findByUnionid(unionid)
                .map(this::convertToUserInfoResponse);
    }
    
    @Override
    public UserInfoResponse updateUserInfo(Long userId, UserInfoResponse userInfo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新用户信息
        user.setNickname(userInfo.getNickname());
        user.setAvatarUrl(userInfo.getAvatarUrl());
        user.setGender(userInfo.getGender());
        user.setCity(userInfo.getCity());
        user.setProvince(userInfo.getProvince());
        user.setCountry(userInfo.getCountry());
        user.setUpdatedAt(LocalDateTime.now());
        
        user = userRepository.save(user);
        return convertToUserInfoResponse(user);
    }
    
    @Override
    public UserInfoResponse updateUserProfile(Long userId, UpdateProfileRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新用户资料
        if (updateRequest.getNickname() != null && !updateRequest.getNickname().trim().isEmpty()) {
            user.setNickname(updateRequest.getNickname());
        }
        if (updateRequest.getAvatarUrl() != null && !updateRequest.getAvatarUrl().trim().isEmpty()) {
            user.setAvatarUrl(updateRequest.getAvatarUrl());
        }
        if (updateRequest.getGender() != null) {
            user.setGender(updateRequest.getGender());
        }
        if (updateRequest.getCity() != null) {
            user.setCity(updateRequest.getCity());
        }
        if (updateRequest.getProvince() != null) {
            user.setProvince(updateRequest.getProvince());
        }
        if (updateRequest.getCountry() != null) {
            user.setCountry(updateRequest.getCountry());
        }
        user.setUpdatedAt(LocalDateTime.now());
        
        user = userRepository.save(user);
        logger.info("用户资料更新成功，userId: {}, nickname: {}", userId, user.getNickname());
        return convertToUserInfoResponse(user);
    }
    
    @Override
    public void updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setStatus(Integer.parseInt(status));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    @Override
    public void updateLastLoginTime(Long userId, LocalDateTime lastLoginTime) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setLastLoginAt(lastLoginTime);
        userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserInfoResponse> searchUsers(String keyword, Pageable pageable) {
        // 由于Repository中没有分页版本的方法，这里需要自定义查询
        // 暂时返回空页面，需要在Repository中添加相应方法
        return Page.empty(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserInfoResponse> getActiveUsers(LocalDateTime since, Pageable pageable) {
        // 由于Repository中没有分页版本的方法，这里需要自定义查询
        // 暂时返回空页面，需要在Repository中添加相应方法
        return Page.empty(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userStatistics", key = "#userId", unless = "#result.totalRounds == 0")
    public UserStatistics getUserStatistics(Long userId) {
        try {
            // 查询用户在已结束回合中的参与记录
            List<ParticipantRecord> participantRecords = participantRecordRepository.findByUserIdAndFinishedRounds(userId, RoundStatus.FINISHED);
            
            if (participantRecords.isEmpty()) {
                return new UserStatistics(0L, 0L, 0L, 0L, 0L, 0L);
            }
            
            // 计算统计数据
            long totalRounds = participantRecords.size();
            long winRounds = participantRecords.stream()
                    .mapToLong(record -> Boolean.TRUE.equals(record.getIsWinner()) ? 1L : 0L)
                    .sum();
            
            // 计算平场数（金额变化为0的记录）
            long drawRounds = participantRecords.stream()
                    .mapToLong(record -> record.getAmountChange().compareTo(BigDecimal.ZERO) == 0 ? 1L : 0L)
                    .sum();
            
            // 计算负场数（总回合数 - 胜场数 - 平场数）
            long loseRounds = totalRounds - winRounds - drawRounds;
            
            // 计算总金额变化（正数为盈利，负数为亏损），应用回合倍率
            BigDecimal totalAmountChange = participantRecords.stream()
                    .map(record -> {
                        BigDecimal amountChange = record.getAmountChange();
                        BigDecimal multiplier = record.getRound() != null && record.getRound().getMultiplier() != null 
                                ? record.getRound().getMultiplier() 
                                : BigDecimal.ONE;
                        return amountChange.multiply(multiplier);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 计算胜利金额（只统计正数的金额变化），应用回合倍率
            BigDecimal winAmountChange = participantRecords.stream()
                    .map(record -> {
                        BigDecimal amountChange = record.getAmountChange();
                        BigDecimal multiplier = record.getRound() != null && record.getRound().getMultiplier() != null 
                                ? record.getRound().getMultiplier() 
                                : BigDecimal.ONE;
                        return amountChange.multiply(multiplier);
                    })
                    .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 转换为Long类型（以分为单位）
            long totalAmount = totalAmountChange.multiply(new BigDecimal(100)).longValue();
            long winAmount = winAmountChange.multiply(new BigDecimal(100)).longValue();
            
            return new UserStatistics(totalRounds, winRounds, loseRounds, drawRounds, totalAmount, winAmount);
            
        } catch (Exception e) {
            logger.error("获取用户统计信息失败，userId: {}", userId, e);
            return new UserStatistics(0L, 0L, 0L, 0L, 0L, 0L);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByOpenid(String openid) {
        return userRepository.existsByOpenid(openid);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUnionid(String unionid) {
        return userRepository.existsByUnionid(unionid);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countActiveUsers(LocalDateTime since) {
        // 使用现有的countActiveUsers方法，传入正常状态
        return userRepository.countActiveUsers(1, since);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserEntity(Long userId) {
        return userRepository.findById(userId);
    }
    
    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * 将User实体转换为UserInfoResponse
     */
    private UserInfoResponse convertToUserInfoResponse(User user) {
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getId());
        response.setOpenid(user.getOpenid());
        response.setUnionid(user.getUnionid());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setGender(user.getGender());
        response.setCity(user.getCity());
        response.setProvince(user.getProvince());
        response.setCountry(user.getCountry());
        response.setStatus(user.getStatus().toString());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginTime(user.getLastLoginAt());
        return response;
    }
    
    /**
     * 生成访问令牌
     */
    private String generateAccessToken(Long userId) {
        // 获取用户的openid作为JWT token的subject
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在: " + userId);
        }
        String openid = userOpt.get().getOpenid();
        // 使用JwtUtil生成真正的JWT令牌，使用openid作为subject
        return jwtUtil.generateToken(openid, userId);
    }
    
    /**
     * 生成刷新令牌
     */
    private String generateRefreshToken(Long userId) {
        // 获取用户的openid作为JWT token的subject
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在: " + userId);
        }
        String openid = userOpt.get().getOpenid();
        // 使用JwtUtil生成JWT格式的刷新令牌（有效期更长），使用openid作为subject
        return jwtUtil.generateToken("refresh_" + openid, userId);
    }
    

}