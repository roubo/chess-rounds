package com.airoubo.chessrounds.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 开始回合请求DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class StartRoundRequest {
    
    @JsonProperty("hasTable")
    private Boolean hasTable;
    
    @JsonProperty("tableUserId")
    private Long tableUserId;
    
    @JsonProperty("base_amount")
    private Double baseAmount;
    
    public StartRoundRequest() {}
    
    public Boolean getHasTable() {
        return hasTable;
    }
    
    public void setHasTable(Boolean hasTable) {
        this.hasTable = hasTable;
    }
    
    public Long getTableUserId() {
        return tableUserId;
    }
    
    public void setTableUserId(Long tableUserId) {
        this.tableUserId = tableUserId;
    }
    
    public Double getBaseAmount() {
        return baseAmount;
    }
    
    public void setBaseAmount(Double baseAmount) {
        this.baseAmount = baseAmount;
    }
    
    @Override
    public String toString() {
        return "StartRoundRequest{" +
                "hasTable=" + hasTable +
                ", tableUserId=" + tableUserId +
                ", baseAmount=" + baseAmount +
                '}';
    }
}