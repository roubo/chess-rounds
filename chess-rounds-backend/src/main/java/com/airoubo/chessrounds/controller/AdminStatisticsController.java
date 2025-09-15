package com.airoubo.chessrounds.controller;

import com.airoubo.chessrounds.dto.statistics.AdminFinancialDetailResponse;
import com.airoubo.chessrounds.dto.statistics.AdminStatisticsOverviewResponse;
import com.airoubo.chessrounds.dto.statistics.AdminUserDetailResponse;
import com.airoubo.chessrounds.dto.statistics.RoundStatisticsDTO;
import com.airoubo.chessrounds.security.AdminOnly;
import com.airoubo.chessrounds.service.AdminStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员统计数据控制器
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/admin/statistics")
@CrossOrigin(origins = "*")
public class AdminStatisticsController {
    
    @Autowired
    private AdminStatisticsService adminStatisticsService;
    
    /**
     * 获取管理员统计总览
     * 
     * @return 统计总览数据
     */
    @GetMapping("/overview")
    @AdminOnly
    public ResponseEntity<AdminStatisticsOverviewResponse> getStatisticsOverview() {
        try {
            AdminStatisticsOverviewResponse overview = adminStatisticsService.getStatisticsOverview();
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取回合状态统计
     * 
     * @return 回合状态统计数据
     */
    @GetMapping("/rounds")
    @AdminOnly
    public ResponseEntity<Map<String, RoundStatisticsDTO>> getRoundStatistics() {
        try {
            Map<String, RoundStatisticsDTO> roundStats = adminStatisticsService.getRoundStatistics();
            return ResponseEntity.ok(roundStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取用户详细信息
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param sortBy 排序字段（created_at, last_login_at, total_amount等）
     * @param sortDir 排序方向（asc, desc）
     * @return 用户详细信息列表
     */
    @GetMapping("/users")
    @AdminOnly
    public ResponseEntity<AdminUserDetailResponse> getUserDetails(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            AdminUserDetailResponse userDetails = adminStatisticsService.getUserDetails(page, size, sortBy, sortDir);
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取财务流水详细统计
     * 
     * @return 财务详细统计数据
     */
    @GetMapping("/financial")
    @AdminOnly
    public ResponseEntity<AdminFinancialDetailResponse> getFinancialDetails() {
        try {
            AdminFinancialDetailResponse financialDetails = adminStatisticsService.getFinancialDetails();
            return ResponseEntity.ok(financialDetails);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 刷新统计缓存
     * 
     * @return 操作结果
     */
    @PostMapping("/refresh-cache")
    @AdminOnly
    public ResponseEntity<String> refreshStatisticsCache() {
        try {
            adminStatisticsService.refreshStatisticsCache();
            return ResponseEntity.ok("统计缓存已刷新");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("刷新缓存失败: " + e.getMessage());
        }
    }
}