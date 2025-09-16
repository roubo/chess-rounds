package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据OpenID查找用户
     * 
     * @param openid 微信OpenID
     * @return 用户信息
     */
    Optional<User> findByOpenid(String openid);
    
    /**
     * 根据UnionID查找用户
     * 
     * @param unionid 微信UnionID
     * @return 用户信息
     */
    Optional<User> findByUnionid(String unionid);
    
    /**
     * 根据昵称模糊查询用户
     * 
     * @param nickname 昵称关键词
     * @return 用户列表
     */
    List<User> findByNicknameContainingIgnoreCase(String nickname);
    
    /**
     * 根据状态查询用户
     * 
     * @param status 用户状态
     * @return 用户列表
     */
    List<User> findByStatus(Integer status);
    
    /**
     * 查询指定时间后登录的用户
     * 
     * @param lastLoginTime 最后登录时间
     * @return 用户列表
     */
    List<User> findByLastLoginAtAfter(LocalDateTime lastLoginTime);
    
    /**
     * 统计活跃用户数量（排除台板数据）
     * 
     * @param status 用户状态
     * @param lastLoginTime 最后登录时间
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status AND u.lastLoginAt >= :lastLoginTime " +
           "AND (u.nickname IS NULL OR NOT u.nickname LIKE '台板-%') " +
           "AND (u.openid IS NULL OR NOT u.openid LIKE 'table_%')")
    Long countActiveUsers(@Param("status") Integer status, @Param("lastLoginTime") LocalDateTime lastLoginTime);
    
    /**
     * 检查OpenID是否存在
     * 
     * @param openid 微信OpenID
     * @return 是否存在
     */
    boolean existsByOpenid(String openid);
    
    /**
     * 检查UnionID是否存在
     * 
     * @param unionid 微信UnionID
     * @return 是否存在
     */
    boolean existsByUnionid(String unionid);
}