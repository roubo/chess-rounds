package com.airoubo.chessrounds.enums;

/**
 * 关系类型枚举
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public enum RelationshipType {
    
    /**
     * 合伙人
     */
    PARTNER("partner", "合伙人"),
    
    /**
     * 对手
     */
    OPPONENT("opponent", "对手");
    
    private final String code;
    private final String description;
    
    RelationshipType(String code, String description) {
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
     * @param code 关系类型代码
     * @return 关系类型枚举
     */
    public static RelationshipType fromCode(String code) {
        for (RelationshipType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown relationship type code: " + code);
    }
}