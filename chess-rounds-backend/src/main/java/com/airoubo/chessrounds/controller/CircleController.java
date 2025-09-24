package com.airoubo.chessrounds.controller;

import com.airoubo.chessrounds.dto.circle.*;
import com.airoubo.chessrounds.service.CircleService;
import com.airoubo.chessrounds.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 圈子控制器
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/circles")
@CrossOrigin(origins = "*")
public class CircleController {
    
    @Autowired
    private CircleService circleService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 创建圈子
     * 
     * @param request HTTP请求
     * @param createRequest 创建圈子请求
     * @return 圈子信息
     */
    @PostMapping
    public ResponseEntity<CircleInfoResponse> createCircle(
            HttpServletRequest request,
            @Valid @RequestBody CreateCircleRequest createRequest) {
        Long userId = getUserIdFromRequest(request);
        CircleInfoResponse response = circleService.createCircle(userId, createRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 加入圈子
     * 
     * @param request HTTP请求
     * @param joinRequest 加入圈子请求
     * @return 圈子信息
     */
    @PostMapping("/join")
    public ResponseEntity<CircleInfoResponse> joinCircle(
            HttpServletRequest request,
            @Valid @RequestBody JoinCircleRequest joinRequest) {
        Long userId = getUserIdFromRequest(request);
        CircleInfoResponse response = circleService.joinCircle(userId, joinRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 通过加入码加入圈子
     * 
     * @param request HTTP请求
     * @param joinRequest 通过加入码加入圈子请求
     * @return 圈子信息
     */
    @PostMapping("/join-by-code")
    public ResponseEntity<CircleInfoResponse> joinCircleByCode(
            HttpServletRequest request,
            @Valid @RequestBody JoinCircleByCodeRequest joinRequest) {
        Long userId = getUserIdFromRequest(request);
        CircleInfoResponse response = circleService.joinCircleByCode(userId, joinRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 退出圈子
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @return 成功响应
     */
    @DeleteMapping("/{circleId}/leave")
    public ResponseEntity<Void> leaveCircle(
            HttpServletRequest request,
            @PathVariable Long circleId) {
        Long userId = getUserIdFromRequest(request);
        circleService.leaveCircle(userId, circleId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取用户加入的圈子列表
     * 
     * @param request HTTP请求
     * @param pageable 分页参数
     * @return 圈子列表
     */
    @GetMapping("/my")
    public ResponseEntity<Page<CircleInfoResponse>> getUserCircles(
            HttpServletRequest request,
            Pageable pageable) {
        Long userId = getUserIdFromRequest(request);
        Page<CircleInfoResponse> circles = circleService.getUserCircles(userId, pageable);
        return ResponseEntity.ok(circles);
    }
    
    /**
     * 根据ID获取圈子信息
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @return 圈子信息
     */
    @GetMapping("/{circleId}")
    public ResponseEntity<CircleInfoResponse> getCircleInfo(
            HttpServletRequest request,
            @PathVariable Long circleId) {
        Long userId = getUserIdFromRequest(request);
        Optional<CircleInfoResponse> circle = circleService.getCircleInfo(circleId, userId);
        return circle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据代码获取圈子信息
     * 
     * @param request HTTP请求
     * @param circleCode 圈子代码
     * @return 圈子信息
     */
    @GetMapping("/code/{circleCode}")
    public ResponseEntity<CircleInfoResponse> getCircleInfoByCode(
            HttpServletRequest request,
            @PathVariable String circleCode) {
        Long userId = getUserIdFromRequest(request);
        Optional<CircleInfoResponse> circle = circleService.getCircleInfoByCode(circleCode, userId);
        return circle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新圈子信息
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @param updateRequest 更新请求
     * @return 更新后的圈子信息
     */
    @PutMapping("/{circleId}")
    public ResponseEntity<CircleInfoResponse> updateCircle(
            HttpServletRequest request,
            @PathVariable Long circleId,
            @Valid @RequestBody UpdateCircleRequest updateRequest) {
        Long userId = getUserIdFromRequest(request);
        CircleInfoResponse response = circleService.updateCircle(userId, circleId, updateRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 解散圈子
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @return 成功响应
     */
    @DeleteMapping("/{circleId}")
    public ResponseEntity<Void> dissolveCircle(
            HttpServletRequest request,
            @PathVariable Long circleId) {
        Long userId = getUserIdFromRequest(request);
        circleService.dissolveCircle(userId, circleId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取圈子成员列表
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @param pageable 分页参数
     * @return 成员列表
     */
    @GetMapping("/{circleId}/members")
    public ResponseEntity<Page<CircleMemberResponse>> getCircleMembers(
            HttpServletRequest request,
            @PathVariable Long circleId,
            Pageable pageable) {
        Long userId = getUserIdFromRequest(request);
        Page<CircleMemberResponse> members = circleService.getCircleMembers(circleId, userId, pageable);
        return ResponseEntity.ok(members);
    }
    
    /**
     * 移除圈子成员
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @param memberId 成员ID
     * @return 成功响应
     */
    @DeleteMapping("/{circleId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            HttpServletRequest request,
            @PathVariable Long circleId,
            @PathVariable Long memberId) {
        Long operatorId = getUserIdFromRequest(request);
        circleService.removeMember(operatorId, circleId, memberId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 设置成员管理员权限
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @param memberId 成员ID
     * @param isAdmin 是否为管理员
     * @return 成功响应
     */
    @PutMapping("/{circleId}/members/{memberId}/admin")
    public ResponseEntity<Void> setMemberAdmin(
            HttpServletRequest request,
            @PathVariable Long circleId,
            @PathVariable Long memberId,
            @RequestParam Boolean isAdmin) {
        Long operatorId = getUserIdFromRequest(request);
        circleService.setMemberAdmin(operatorId, circleId, memberId, isAdmin);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取圈子排行榜
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @param pageable 分页参数
     * @param sort 排序参数
     * @return 排行榜
     */
    @GetMapping("/{circleId}/leaderboard")
    public ResponseEntity<Page<CircleLeaderboardResponse>> getCircleLeaderboard(
            HttpServletRequest request,
            @PathVariable Long circleId,
            @RequestParam(required = false) String sort,
            Pageable pageable) {
        Long userId = getUserIdFromRequest(request);
        
        // 解析排序参数
        String sortBy = "score";
        String sortOrder = "desc";
        
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length >= 1) {
                sortBy = sortParams[0].trim();
            }
            if (sortParams.length >= 2) {
                sortOrder = sortParams[1].trim();
            }
        }
        
        Page<CircleLeaderboardResponse> leaderboard = circleService.getCircleLeaderboard(circleId, userId, pageable, sortBy, sortOrder);
        return ResponseEntity.ok(leaderboard);
    }
    
    /**
     * 刷新圈子排行榜
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @return 成功响应
     */
    @PostMapping("/{circleId}/leaderboard/refresh")
    public ResponseEntity<Void> refreshCircleLeaderboard(
            HttpServletRequest request,
            @PathVariable Long circleId) {
        Long userId = getUserIdFromRequest(request);
        circleService.refreshCircleLeaderboard(circleId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 搜索公开圈子
     * 
     * @param keyword 搜索关键字
     * @param pageable 分页参数
     * @return 圈子列表
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CircleInfoResponse>> searchPublicCircles(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<CircleInfoResponse> circles = circleService.searchPublicCircles(keyword, pageable);
        return ResponseEntity.ok(circles);
    }
    
    /**
     * 获取热门圈子
     * 
     * @param pageable 分页参数
     * @return 热门圈子列表
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<CircleInfoResponse>> getPopularCircles(Pageable pageable) {
        Page<CircleInfoResponse> circles = circleService.getPopularCircles(pageable);
        return ResponseEntity.ok(circles);
    }
    
    /**
     * 检查用户是否为圈子成员
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @return 是否为成员
     */
    @GetMapping("/{circleId}/membership")
    public ResponseEntity<Boolean> checkMembership(
            HttpServletRequest request,
            @PathVariable Long circleId) {
        Long userId = getUserIdFromRequest(request);
        boolean isMember = circleService.isMember(userId, circleId);
        return ResponseEntity.ok(isMember);
    }
    
    /**
     * 检查用户是否为圈子管理员
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @return 是否为管理员
     */
    @GetMapping("/{circleId}/admin")
    public ResponseEntity<Boolean> checkAdmin(
            HttpServletRequest request,
            @PathVariable Long circleId) {
        Long userId = getUserIdFromRequest(request);
        boolean isAdmin = circleService.isAdmin(userId, circleId);
        return ResponseEntity.ok(isAdmin);
    }
    
    /**
     * 检查用户是否为圈子创建者
     * 
     * @param request HTTP请求
     * @param circleId 圈子ID
     * @return 是否为创建者
     */
    @GetMapping("/{circleId}/creator")
    public ResponseEntity<Boolean> checkCreator(
            HttpServletRequest request,
            @PathVariable Long circleId) {
        Long userId = getUserIdFromRequest(request);
        boolean isCreator = circleService.isCreator(userId, circleId);
        return ResponseEntity.ok(isCreator);
    }
    
    /**
     * 从请求中获取用户ID
     * 
     * @param request HTTP请求
     * @return 用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未找到有效的认证令牌");
    }
}