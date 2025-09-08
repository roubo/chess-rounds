package com.airoubo.chessrounds.service.impl;

import com.airoubo.chessrounds.dto.record.CreateRecordRequest;
import com.airoubo.chessrounds.dto.record.RecordInfoResponse;
import com.airoubo.chessrounds.dto.user.UserInfoResponse;
import com.airoubo.chessrounds.entity.Record;
import com.airoubo.chessrounds.enums.RecordType;
import com.airoubo.chessrounds.repository.RecordRepository;
import com.airoubo.chessrounds.service.RecordService;
import com.airoubo.chessrounds.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 记录服务实现类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Service
@Transactional
public class RecordServiceImpl implements RecordService {
    
    @Autowired
    private RecordRepository recordRepository;
    
    @Autowired
    private UserService userService;
    
    @Override
    public RecordInfoResponse createRecord(CreateRecordRequest createRequest, Long recorderId) {
        // 创建记录实体
        Record record = new Record();
        record.setRoundId(createRequest.getRoundId());
        record.setRecorderId(recorderId);
        record.setRecordType(RecordType.valueOf(createRequest.getRecordType()));
        record.setAmount(createRequest.getTotalAmount());
        record.setDescription(createRequest.getDescription());
        record.setSequenceNumber(1); // TODO: 实现序列号生成逻辑
        record.setCreatedAt(LocalDateTime.now());
        
        record = recordRepository.save(record);
        
        return convertToRecordInfoResponse(record);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<RecordInfoResponse> getRecordById(Long recordId) {
        return recordRepository.findById(recordId)
                .map(this::convertToRecordInfoResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RecordInfoResponse> getRoundRecords(Long roundId, Pageable pageable) {
        // 暂时返回空页面，需要Repository支持分页查询
        return Page.empty(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RecordInfoResponse> getUserRecords(Long userId, Pageable pageable) {
        return recordRepository.findByRecorderId(userId, pageable)
                .map(this::convertToRecordInfoResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RecordInfoResponse> getUserRoundRecords(Long roundId, Long userId, Pageable pageable) {
        // 使用现有方法获取记录列表，然后转换为分页
        List<Record> records = recordRepository.findRecordsByRoundIdAndUserId(roundId, userId);
        List<RecordInfoResponse> responses = records.stream()
                .map(this::convertToRecordInfoResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, responses.size());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RecordInfoResponse> getRecordsByType(Long roundId, String recordType, Pageable pageable) {
        RecordType type = RecordType.valueOf(recordType);
        List<Record> records = recordRepository.findByRoundIdAndRecordType(roundId, type);
        List<RecordInfoResponse> responses = records.stream()
                .map(this::convertToRecordInfoResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, responses.size());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RecordInfoResponse> getRecordsByTimeRange(Long roundId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return recordRepository.findByCreatedAtBetween(startTime, endTime, pageable)
                .map(this::convertToRecordInfoResponse);
    }
    
    @Override
    public RecordInfoResponse updateRecord(Long recordId, CreateRecordRequest updateRequest, Long userId) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        
        // 检查权限
        if (!hasPermission(recordId, userId)) {
            throw new RuntimeException("无权限修改此记录");
        }
        
        // 更新记录信息
        if (updateRequest.getTotalAmount() != null) {
            record.setAmount(updateRequest.getTotalAmount());
        }
        if (updateRequest.getDescription() != null) {
            record.setDescription(updateRequest.getDescription());
        }
        record.setUpdatedAt(LocalDateTime.now());
        
        record = recordRepository.save(record);
        return convertToRecordInfoResponse(record);
    }
    
    @Override
    public void deleteRecord(Long recordId, Long userId) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        
        // 检查权限
        if (!hasPermission(recordId, userId)) {
            throw new RuntimeException("无权限删除此记录");
        }
        
        recordRepository.delete(record);
    }
    
    @Override
    public void deleteRecords(List<Long> recordIds, Long userId) {
        for (Long recordId : recordIds) {
            deleteRecord(recordId, userId);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long recordId, Long userId) {
        return recordRepository.findById(recordId)
                .map(record -> record.getRecorderId().equals(userId))
                .orElse(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RecordInfoResponse> searchRecords(Long roundId, String keyword, Pageable pageable) {
        // 暂时返回空页面，需要Repository支持描述搜索
        return Page.empty(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countRoundRecords(Long roundId, String recordType, Long userId) {
        if (userId != null) {
            List<Record> records = recordRepository.findRecordsByRoundIdAndUserId(roundId, userId);
            return (long) records.size();
        } else {
            return recordRepository.countByRoundId(roundId);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countUserRecords(Long userId, String recordType, LocalDateTime since) {
        return recordRepository.countByRecorderId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long calculateRoundTotalAmount(Long roundId) {
        BigDecimal total = recordRepository.sumAmountByRoundId(roundId);
        return total != null ? total.longValue() : 0L;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long calculateUserRoundAmount(Long roundId, Long userId) {
        List<Record> records = recordRepository.findRecordsByRoundIdAndUserId(roundId, userId);
        return records.stream()
                .map(Record::getAmount)
                .mapToLong(BigDecimal::longValue)
                .sum();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RecordInfoResponse> getLatestRoundRecords(Long roundId, int limit) {
        List<Record> records = recordRepository.findByRoundIdOrderBySequenceNumber(roundId);
        return records.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(limit)
                .map(this::convertToRecordInfoResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RecordInfoResponse> getLatestUserRecords(Long userId, int limit) {
        Page<Record> records = recordRepository.findByRecorderId(userId, PageRequest.of(0, limit));
        return records.getContent().stream()
                .map(this::convertToRecordInfoResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public RecordStatistics getRecordStatistics(Long roundId, Long userId) {
        List<Record> records;
        if (userId != null) {
            records = recordRepository.findRecordsByRoundIdAndUserId(roundId, userId);
        } else {
            records = recordRepository.findByRoundId(roundId);
        }
        
        long totalRecords = records.size();
        long totalAmount = records.stream()
                .map(Record::getAmount)
                .mapToLong(BigDecimal::longValue)
                .sum();
        long winAmount = records.stream()
                .filter(r -> r.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(Record::getAmount)
                .mapToLong(BigDecimal::longValue)
                .sum();
        long loseAmount = records.stream()
                .filter(r -> r.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .map(Record::getAmount)
                .mapToLong(BigDecimal::longValue)
                .sum();
        
        return new RecordStatistics(totalRecords, totalAmount, winAmount, Math.abs(loseAmount));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Record> getRecordEntity(Long recordId) {
        return recordRepository.findById(recordId);
    }
    
    @Override
    public Record saveRecord(Record record) {
        return recordRepository.save(record);
    }
    
    /**
     * 将Record实体转换为RecordInfoResponse
     */
    private RecordInfoResponse convertToRecordInfoResponse(Record record) {
        RecordInfoResponse response = new RecordInfoResponse();
        response.setRecordId(record.getId());
        response.setRoundId(record.getRoundId());
        response.setSequenceNumber(record.getSequenceNumber());
        response.setRecordType(record.getRecordType().name());
        response.setDescription(record.getDescription());
        response.setTotalAmount(record.getAmount());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        
        // 获取用户信息
        Optional<UserInfoResponse> userInfo = userService.getUserById(record.getRecorderId());
        if (userInfo.isPresent()) {
            response.setRecorder(userInfo.get());
        }
        
        return response;
    }
}