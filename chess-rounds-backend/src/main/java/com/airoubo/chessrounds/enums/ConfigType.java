package com.airoubo.chessrounds.enums;

/**
 * 配置类型枚举
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public enum ConfigType {
    
    /**
     * 字符串类型
     */
    STRING("string", "字符串"),
    
    /**
     * 数字类型
     */
    NUMBER("number", "数字"),
    
    /**
     * 布尔类型
     */
    BOOLEAN("boolean", "布尔"),
    
    /**
     * JSON类型
     */
    JSON("json", "JSON");
    
    private final String code;
    private final String description;
    
    ConfigType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举
     * 
     * @param code 配置类型代码
     * @return 配置类型枚举
     */
    public static ConfigType fromCode(String code) {
        for (ConfigType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown config type code: " + code);
    }
}