package com.airoubo.chessrounds.dto.circle;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 加入圈子请求DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class JoinCircleRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 圈子代码
     */
    @JsonProperty("circle_code")
    @NotBlank(message = "圈子代码不能为空")
    @Size(max = 20, message = "圈子代码不能超过20个字符")
    private String circleCode;
    
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
    
    public JoinCircleRequest() {
    }
    
    public String getCircleCode() {
        return circleCode;
    }
    
    public void setCircleCode(String circleCode) {
        this.circleCode = circleCode;
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