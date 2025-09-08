package com.airoubo.chessrounds.enums;

/**
 * 统计类型枚举
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public enum StatType {
    
    /**
     * 日统计
     */
    DAILY("daily", "日统计"),
    
    /**
     * 月统计
     */
    MONTHLY("monthly", "月统计"),
    
    /**
     * 年统计
     */
    YEARLY("yearly", "年统计");
    
    private final String code;
    private final String description;
    
    StatType(String code, String description) {
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
     * @param code 统计类型代码
     * @return 统计类型枚举
     */
    public static StatType fromCode(String code) {
        for (StatType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown stat type code: " + code);
    }
}