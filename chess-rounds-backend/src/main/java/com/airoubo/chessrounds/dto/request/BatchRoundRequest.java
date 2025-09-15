package com.airoubo.chessrounds.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 批量查询回合请求DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class BatchRoundRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 回合ID列表
     */
    @JsonProperty("ids")
    private List<Long> ids;
    
    public BatchRoundRequest() {}
    
    public List<Long> getIds() {
        return ids;
    }
    
    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
    
    @Override
    public String toString() {
        return "BatchRoundRequest{" +
                "ids=" + ids +
                '}';
    }
}