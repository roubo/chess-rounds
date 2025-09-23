package com.airoubo.chessrounds.dto.circle;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 通过加入码加入圈子请求DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class JoinCircleByCodeRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 加入码
     */
    @JsonProperty("joinCode")
    @NotBlank(message = "加入码不能为空")
    @Size(min = 5, max = 5, message = "加入码必须是5位字符")
    private String joinCode;
    
    /**
     * 在圈子中的昵称
     */
    @JsonProperty("nickname")
    @Size(max = 100, message = "昵称不能超过100个字符")
    private String nickname;
    
    /**
     * 在圈子中的头像URL
     */
    @JsonProperty("avatar_url")
    @Size(max = 500, message = "头像URL不能超过500个字符")
    private String avatarUrl;
    
    public JoinCircleByCodeRequest() {
    }
    
    public String getJoinCode() {
        return joinCode;
    }
    
    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
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
}