package com.airoubo.chessrounds.controller;

import com.airoubo.chessrounds.dto.record.CreateRecordRequest;
import com.airoubo.chessrounds.dto.record.RecordInfoResponse;
import com.airoubo.chessrounds.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 记录控制器
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/records")
@CrossOrigin(origins = "*")
public class RecordController {
    
    @Autowired
    private RecordService recordService;
    
    /**
     * 创建记录
     * 
     * @param createRequest 创建请求
     * @param userId 用户ID
     * @return 记录信息
     */
    @PostMapping
    public ResponseEntity<RecordInfoResponse> createRecord(
            @RequestBody CreateRecordRequest createRequest,
            @RequestHeader("user-id") Long userId) {
        RecordInfoResponse response = recordService.createRecord(createRequest, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取记录信息
     * 
     * @param recordId 记录ID
     * @return 记录信息
     */
    @GetMapping("/{recordId}")
    public ResponseEntity<RecordInfoResponse> getRecordById(@PathVariable Long recordId) {
        Optional<RecordInfoResponse> record = recordService.getRecordById(recordId);
        return record.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新记录
     * 
     * @param recordId 记录ID
     * @param createRequest 更新请求（复用创建请求）
     * @param userId 用户ID
     * @return 更新后的记录信息
     */
    @PutMapping("/{recordId}")
    public ResponseEntity<RecordInfoResponse> updateRecord(
            @PathVariable Long recordId,
            @RequestBody CreateRecordRequest createRequest,
            @RequestHeader("user-id") Long userId) {
        RecordInfoResponse response = recordService.updateRecord(recordId, createRequest, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除记录
     * 
     * @param recordId 记录ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteRecord(
            @PathVariable Long recordId,
            @RequestHeader("user-id") Long userId) {
        recordService.deleteRecord(recordId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 批量删除记录
     * 
     * @param recordIds 记录ID列表
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteRecords(
            @RequestParam List<Long> recordIds,
            @RequestHeader("user-id") Long userId) {
        recordService.deleteRecords(recordIds, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取回合记录列表
     * 
     * @param roundId 回合ID
     * @param pageable 分页参数
     * @return 记录列表
     */
    @GetMapping("/round/{roundId}")
    public ResponseEntity<Page<RecordInfoResponse>> getRoundRecords(
            @PathVariable Long roundId,
            Pageable pageable) {
        Page<RecordInfoResponse> records = recordService.getRoundRecords(roundId, pageable);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 获取用户在指定回合的记录列表
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 记录列表
     */
    @GetMapping("/round/{roundId}/user")
    public ResponseEntity<Page<RecordInfoResponse>> getUserRoundRecords(
            @PathVariable Long roundId,
            @RequestHeader("user-id") Long userId,
            Pageable pageable) {
        Page<RecordInfoResponse> records = recordService.getUserRoundRecords(roundId, userId, pageable);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 按类型获取记录列表
     * 
     * @param roundId 回合ID
     * @param recordType 记录类型
     * @param pageable 分页参数
     * @return 记录列表
     */
    @GetMapping("/round/{roundId}/type/{recordType}")
    public ResponseEntity<Page<RecordInfoResponse>> getRecordsByType(
            @PathVariable Long roundId,
            @PathVariable String recordType,
            Pageable pageable) {
        Page<RecordInfoResponse> records = recordService.getRecordsByType(roundId, recordType, pageable);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 统计回合记录数量
     * 
     * @param roundId 回合ID
     * @param recordType 记录类型（可选）
     * @param userId 用户ID（可选）
     * @return 记录数量
     */
    @GetMapping("/round/{roundId}/count")
    public ResponseEntity<Long> countRoundRecords(
            @PathVariable Long roundId,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) Long userId) {
        long count = recordService.countRoundRecords(roundId, recordType, userId);
        return ResponseEntity.ok(count);
    }
    

    
    /**
     * 计算回合总金额
     * 
     * @param roundId 回合ID
     * @return 总金额
     */
    @GetMapping("/round/{roundId}/total-amount")
    public ResponseEntity<Long> calculateRoundTotalAmount(@PathVariable Long roundId) {
        long totalAmount = recordService.calculateRoundTotalAmount(roundId);
        return ResponseEntity.ok(totalAmount);
    }
    
    /**
     * 计算用户在指定回合的总金额
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 总金额
     */
    @GetMapping("/round/{roundId}/user/total-amount")
    public ResponseEntity<Long> calculateUserRoundAmount(
            @PathVariable Long roundId,
            @RequestHeader("user-id") Long userId) {
        long totalAmount = recordService.calculateUserRoundAmount(roundId, userId);
        return ResponseEntity.ok(totalAmount);
    }
    
    /**
     * 获取最新的回合记录
     * 
     * @param roundId 回合ID
     * @param limit 限制数量
     * @return 记录列表
     */
    @GetMapping("/round/{roundId}/latest")
    public ResponseEntity<List<RecordInfoResponse>> getLatestRoundRecords(
            @PathVariable Long roundId,
            @RequestParam(defaultValue = "10") int limit) {
        List<RecordInfoResponse> records = recordService.getLatestRoundRecords(roundId, limit);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 获取用户最新记录
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 记录列表
     */
    @GetMapping("/user/latest")
    public ResponseEntity<List<RecordInfoResponse>> getLatestUserRecords(
            @RequestHeader("user-id") Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<RecordInfoResponse> records = recordService.getLatestUserRecords(userId, limit);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 搜索记录
     * 
     * @param roundId 回合ID（可选）
     * @param recordType 记录类型（可选）
     * @param pageable 分页参数
     * @return 记录列表
     */
    @GetMapping("/search")
    public ResponseEntity<Page<RecordInfoResponse>> searchRecords(
            @RequestParam(required = false) Long roundId,
            @RequestParam(required = false) String recordType,
            Pageable pageable) {
        Page<RecordInfoResponse> records = recordService.searchRecords(roundId, recordType, pageable);
        return ResponseEntity.ok(records);
    }
}