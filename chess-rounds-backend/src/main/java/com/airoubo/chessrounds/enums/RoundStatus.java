package com.airoubo.chessrounds.enums;

/**
 * 回合状态枚举
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public enum RoundStatus {
    
    /**
     * 等待中
     */
    WAITING("waiting", "等待中"),
    
    /**
     * 进行中
     */
    PLAYING("playing", "进行中"),
    
    /**
     * 已结束
     */
    FINISHED("finished", "已结束");
    
    private final String code;
    private final String description;
    
    RoundStatus(String code, String description) {
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
     * @param code 状态代码
     * @return 回合状态枚举
     */
    public static RoundStatus fromCode(String code) {
        for (RoundStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown round status code: " + code);
    }
}