package com.airoubo.chessrounds.dto.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 回合统计DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class RoundStatisticsDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 回合数量
     */
    @JsonProperty("count")
    private Integer count;
    
    /**
     * 回合ID列表
     */
    @JsonProperty("round_ids")
    private List<Long> roundIds;
    
    public RoundStatisticsDTO() {
    }
    
    public RoundStatisticsDTO(Integer count, List<Long> roundIds) {
        this.count = count;
        this.roundIds = roundIds;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public List<Long> getRoundIds() {
        return roundIds;
    }
    
    public void setRoundIds(List<Long> roundIds) {
        this.roundIds = roundIds;
    }
}