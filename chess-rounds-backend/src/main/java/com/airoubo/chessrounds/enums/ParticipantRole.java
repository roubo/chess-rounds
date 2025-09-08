package com.airoubo.chessrounds.enums;

/**
 * 参与者角色枚举
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public enum ParticipantRole {
    
    /**
     * 参与者
     */
    PLAYER("player", "参与者"),
    
    /**
     * 旁观者
     */
    SPECTATOR("spectator", "旁观者"),
    
    /**
     * 台板
     */
    TABLE("table", "台板");
    
    private final String code;
    private final String description;
    
    ParticipantRole(String code, String description) {
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
     * @param code 角色代码
     * @return 参与者角色枚举
     */
    public static ParticipantRole fromCode(String code) {
        for (ParticipantRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown participant role code: " + code);
    }
}