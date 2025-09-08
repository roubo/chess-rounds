package com.airoubo.chessrounds.entity;

import com.airoubo.chessrounds.enums.ConfigType;

import jakarta.persistence.*;

/**
 * 系统配置实体类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "system_configs", indexes = {
    @Index(name = "idx_config_key", columnList = "config_key"),
    @Index(name = "idx_is_active", columnList = "is_active")
})
public class SystemConfig extends BaseEntity {
    
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;
    
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "config_type")
    private ConfigType configType = ConfigType.STRING;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Getters and Setters
    public String getConfigKey() {
        return configKey;
    }
    
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
    
    public String getConfigValue() {
        return configValue;
    }
    
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
    
    public ConfigType getConfigType() {
        return configType;
    }
    
    public void setConfigType(ConfigType configType) {
        this.configType = configType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}