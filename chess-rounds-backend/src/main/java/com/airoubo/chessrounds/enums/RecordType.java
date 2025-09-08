package com.airoubo.chessrounds.enums;

/**
 * 记录类型枚举
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public enum RecordType {
    
    /**
     * 胜利
     */
    WIN("win", "胜利"),
    
    /**
     * 失败
     */
    LOSE("lose", "失败"),
    
    /**
     * 平局
     */
    DRAW("draw", "平局"),
    
    /**
     * 特殊
     */
    SPECIAL("special", "特殊");
    
    private final String code;
    private final String description;
    
    RecordType(String code, String description) {
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
     * @param code 记录类型代码
     * @return 记录类型枚举
     */
    public static RecordType fromCode(String code) {
        for (RecordType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown record type code: " + code);
    }
}