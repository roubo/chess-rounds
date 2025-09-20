package com.airoubo.chessrounds.controller;

import com.airoubo.chessrounds.security.AdminOnly;
import com.airoubo.chessrounds.service.RoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员回合控制器
 * 提供管理员专用的回合管理接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/admin/rounds")
@CrossOrigin(origins = "*")
public class AdminRoundController {
    
    @Autowired
    private RoundService roundService;
    
    /**
     * 管理员删除回合
     * 管理员可以删除任何状态的回合，不受创建者权限限制
     * 
     * @param roundId 回合ID
     * @return 操作结果
     */
    @DeleteMapping("/{roundId}")
    @AdminOnly
    public ResponseEntity<Void> adminDeleteRound(@PathVariable Long roundId) {
        try {
            // 调用管理员专用的删除方法
            roundService.adminDeleteRound(roundId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}