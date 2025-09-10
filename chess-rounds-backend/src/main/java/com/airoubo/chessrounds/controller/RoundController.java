package com.airoubo.chessrounds.controller;

import com.airoubo.chessrounds.dto.round.CreateRoundRequest;
import com.airoubo.chessrounds.dto.round.ParticipantInfoResponse;
import com.airoubo.chessrounds.dto.round.RoundInfoResponse;
import com.airoubo.chessrounds.service.RoundService;
import com.airoubo.chessrounds.service.WechatApiService;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 回合控制器
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/rounds")
@CrossOrigin(origins = "*")
public class RoundController {
    
    @Autowired
    private RoundService roundService;
    
    @Autowired
    private WechatApiService wechatApiService;
    
    /**
     * 创建回合
     * 
     * @param createRequest 创建请求
     * @param creatorId 创建者ID
     * @return 回合信息
     */
    @PostMapping
    public ResponseEntity<RoundInfoResponse> createRound(
            @RequestBody CreateRoundRequest createRequest,
            @RequestHeader("user-id") Long creatorId) {
        RoundInfoResponse response = roundService.createRound(createRequest, creatorId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取回合信息
     * 
     * @param roundId 回合ID
     * @return 回合信息
     */
    @GetMapping("/{roundId}")
    public ResponseEntity<RoundInfoResponse> getRoundById(@PathVariable Long roundId) {
        Optional<RoundInfoResponse> round = roundService.getRoundById(roundId);
        return round.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 通过回合码获取回合信息
     * 
     * @param roundCode 回合码
     * @return 回合信息
     */
    @GetMapping("/code/{roundCode}")
    public ResponseEntity<RoundInfoResponse> getRoundByCode(@PathVariable String roundCode) {
        Optional<RoundInfoResponse> response = roundService.getRoundByCode(roundCode);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    

    /**
     * 加入回合
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/{roundId}/join")
    public ResponseEntity<Void> joinRound(
            @PathVariable Long roundId,
            @RequestHeader("user-id") Long userId) {
        roundService.joinRound(roundId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 离开回合
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/{roundId}/leave")
    public ResponseEntity<Void> leaveRound(
            @PathVariable Long roundId,
            @RequestHeader("user-id") Long userId) {
        roundService.leaveRound(roundId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 开始回合
     * 
     * @param roundId 回合ID
     * @param requestBody 请求体，包含台板信息
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/{roundId}/start")
    public ResponseEntity<Void> startRound(
            @PathVariable Long roundId,
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("user-id") Long userId) {
        Boolean hasTable = (Boolean) requestBody.get("hasTable");
        // tableUserId不再需要，台板用户由后端自动创建
        roundService.startRound(roundId, userId, hasTable, null);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 结束回合
     * 
     * @param roundId 回合ID
     * @param userId 用户ID（必须是创建者）
     * @return 操作结果
     */
    @PostMapping("/{roundId}/end")
    public ResponseEntity<Void> endRound(
            @PathVariable Long roundId,
            @RequestHeader("user-id") Long userId) {
        roundService.endRound(roundId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 暂停回合
     * 
     * @param roundId 回合ID
     * @param userId 用户ID（必须是创建者）
     * @return 操作结果
     */
    @PutMapping("/{roundId}/pause")
    public ResponseEntity<Void> pauseRound(
            @PathVariable Long roundId,
            @RequestHeader("user-id") Long userId) {
        roundService.pauseRound(roundId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 恢复回合
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 响应
     */
    @PostMapping("/{roundId}/resume")
    public ResponseEntity<Void> resumeRound(
            @PathVariable Long roundId,
            @RequestHeader("user-id") Long userId) {
        roundService.resumeRound(roundId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 删除回合
     * 
     * @param roundId 回合ID
     * @param userId 用户ID
     * @return 响应
     */
    @DeleteMapping("/{roundId}")
    public ResponseEntity<Void> deleteRound(
            @PathVariable Long roundId,
            @RequestHeader("user-id") Long userId) {
        roundService.deleteRound(roundId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取回合参与者列表
     * 
     * @param roundId 回合ID
     * @return 参与者列表
     */
    @GetMapping("/{roundId}/participants")
    public ResponseEntity<List<ParticipantInfoResponse>> getRoundParticipants(@PathVariable Long roundId) {
        List<ParticipantInfoResponse> participants = roundService.getRoundParticipants(roundId);
        return ResponseEntity.ok(participants);
    }
    
    /**
     * 获取用户参与的回合列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 回合列表
     */
    @GetMapping("/my")
    public ResponseEntity<Page<RoundInfoResponse>> getUserRounds(
            @RequestHeader("user-id") Long userId,
            Pageable pageable) {
        Page<RoundInfoResponse> rounds = roundService.getUserRounds(userId, pageable);
        return ResponseEntity.ok(rounds);
    }
    
    /**
     * 获取用户创建的回合列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 回合列表
     */
    @GetMapping("/created")
    public ResponseEntity<Page<RoundInfoResponse>> getUserCreatedRounds(
            @RequestHeader("user-id") Long userId,
            Pageable pageable) {
        Page<RoundInfoResponse> rounds = roundService.getCreatedRounds(userId, pageable);
        return ResponseEntity.ok(rounds);
    }
    

    

    
    /**
     * 搜索回合（按标题）
     * 
     * @param title 标题关键字
     * @param pageable 分页参数
     * @return 回合列表
     */
    @GetMapping("/search")
    public ResponseEntity<Page<RoundInfoResponse>> searchRounds(
            @RequestParam String title,
            Pageable pageable) {
        Page<RoundInfoResponse> rounds = roundService.searchRounds(title, pageable);
        return ResponseEntity.ok(rounds);
    }
    
    /**
     * 生成回合小程序码
     * 
     * @param roundId 回合ID
     * @return 小程序码图片
     */
    @GetMapping("/{roundId}/miniprogram-code")
    public ResponseEntity<byte[]> generateMiniProgramCode(@PathVariable Long roundId) {
        try {
            // 验证回合是否存在
            Optional<RoundInfoResponse> round = roundService.getRoundById(roundId);
            if (!round.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            // 生成小程序码
            String scene = "roundId=" + roundId;
            String page = "pages/round-detail/round-detail";
            byte[] codeImage = wechatApiService.generateMiniProgramCode(scene, page);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(codeImage.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(codeImage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    

}