package com.airoubo.chessrounds.enums;

/**
 * 评价类型枚举
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public enum RatingType {
    
    /**
     * 赞
     */
    LIKE("like", "赞"),
    
    /**
     * 贬
     */
    DISLIKE("dislike", "贬");
    
    private final String code;
    private final String description;
    
    RatingType(String code, String description) {
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
     * @param code 评价类型代码
     * @return 评价类型枚举
     */
    public static RatingType fromCode(String code) {
        for (RatingType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown rating type code: " + code);
    }
}