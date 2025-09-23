package com.airoubo.chessrounds.dto.circle;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.io.Serializable;

/**
 * 创建圈子请求DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class CreateCircleRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 圈子名称
     */
    @JsonProperty("name")
    @NotBlank(message = "圈子名称不能为空")
    @Size(max = 100, message = "圈子名称不能超过100个字符")
    private String name;
    
    /**
     * 圈子描述
     */
    @JsonProperty("description")
    @Size(max = 500, message = "圈子描述不能超过500个字符")
    private String description;
    
    /**
     * 最大成员数
     */
    @JsonProperty("max_members")
    @Min(value = 2, message = "最大成员数不能少于2人")
    @Max(value = 1000, message = "最大成员数不能超过1000人")
    private Integer maxMembers = 100;
    
    /**
     * 是否公开
     */
    @JsonProperty("is_public")
    private Boolean isPublic = true;
    
    /**
     * 圈子头像URL
     */
    @JsonProperty("avatar_url")
    @Size(max = 500, message = "头像URL不能超过500个字符")
    private String avatarUrl;
    
    public CreateCircleRequest() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getMaxMembers() {
        return maxMembers;
    }
    
    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}