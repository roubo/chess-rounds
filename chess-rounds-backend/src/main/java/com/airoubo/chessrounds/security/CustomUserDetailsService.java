package com.airoubo.chessrounds.security;

import com.airoubo.chessrounds.entity.User;
import com.airoubo.chessrounds.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 自定义用户详情服务
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByOpenid(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        return new CustomUserPrincipal(user);
    }
    
    /**
     * 自定义用户主体类
     */
    public static class CustomUserPrincipal implements UserDetails {
        
        private User user;
        
        public CustomUserPrincipal(User user) {
            this.user = user;
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();
            // 根据用户状态设置权限
            if (user.getStatus() == 1) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            return authorities;
        }
        
        @Override
        public String getPassword() {
            return user.getOpenid(); // 使用openid作为密码
        }
        
        @Override
        public String getUsername() {
            return user.getOpenid(); // 使用openid作为用户名
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return user.getStatus() == 1; // 状态为1表示账户未锁定
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return user.getStatus() == 1; // 状态为1表示账户启用
        }
        
        public User getUser() {
            return user;
        }
        
        public Long getUserId() {
            return user.getId(); // 使用继承自BaseEntity的getId方法
        }
    }
}