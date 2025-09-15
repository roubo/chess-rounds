package com.airoubo.chessrounds.service;

import com.airoubo.chessrounds.dto.statistics.AdminFinancialDetailResponse;
import com.airoubo.chessrounds.dto.statistics.AdminStatisticsOverviewResponse;
import com.airoubo.chessrounds.dto.statistics.AdminUserDetailResponse;
import com.airoubo.chessrounds.dto.statistics.RoundStatisticsDTO;

import java.util.Map;

/**
 * 管理员统计服务接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public interface AdminStatisticsService {
    
    /**
     * 获取管理员统计总览
     * 包含回合统计、用户统计和财务统计的综合信息
     * 
     * @return 统计总览数据
     */
    AdminStatisticsOverviewResponse getStatisticsOverview();
    
    /**
     * 获取回合状态统计
     * 按状态（WAITING、PLAYING、FINISHED）分组统计回合数量和ID列表
     * 
     * @return 回合状态统计数据，key为状态，value为统计信息
     */
    Map<String, RoundStatisticsDTO> getRoundStatistics();
    
    /**
     * 获取用户详细信息
     * 支持分页和排序
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param sortBy 排序字段（created_at, last_login_at, total_amount等）
     * @param sortDir 排序方向（asc, desc）
     * @return 用户详细信息列表
     */
    AdminUserDetailResponse getUserDetails(int page, int size, String sortBy, String sortDir);
    
    /**
     * 获取财务流水详细统计
     * 包含总流水、收支统计、平均值等财务指标
     * 
     * @return 财务详细统计数据
     */
    AdminFinancialDetailResponse getFinancialDetails();
    
    /**
     * 刷新统计缓存
     * 清除所有统计相关的缓存数据，强制重新计算
     */
    void refreshStatisticsCache();
    
    /**
     * 检查用户是否为管理员
     * 
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isAdmin(Long userId);
}