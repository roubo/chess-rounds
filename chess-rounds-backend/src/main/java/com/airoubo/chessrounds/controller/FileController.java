package com.airoubo.chessrounds.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    // 允许的图片格式
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    
    // 最大文件大小 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    /**
     * 上传头像图片
     */
    @PostMapping("/upload/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                result.put("success", false);
                result.put("message", "文件大小不能超过5MB");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isValidImageFile(originalFilename)) {
                result.put("success", false);
                result.put("message", "只支持jpg、jpeg、png、gif、webp格式的图片");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 创建上传目录
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fullUploadPath = uploadPath + "/avatars/" + dateFolder;
            File uploadDir = new File(fullUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 生成唯一文件名
            String fileExtension = getFileExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + fileExtension;
            
            // 保存文件
            Path filePath = Paths.get(fullUploadPath, fileName);
            Files.copy(file.getInputStream(), filePath);
            
            // 生成访问URL
            String fileUrl = "/static/avatars/" + dateFolder + "/" + fileName;
            
            result.put("success", true);
            result.put("message", "上传成功");
            result.put("url", fileUrl);
            result.put("filename", fileName);
            
            logger.info("头像上传成功: {}", fileUrl);
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 获取头像文件
     */
    @GetMapping("/avatars/**")
    public ResponseEntity<byte[]> getAvatar(@RequestParam String path) {
        try {
            // 从请求路径中提取文件路径
            String filePath = uploadPath + "/avatars/" + path;
            Path file = Paths.get(filePath);
            
            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(file);
            String contentType = Files.probeContentType(file);
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                    .body(fileContent);
                    
        } catch (IOException e) {
            logger.error("读取文件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 验证是否为有效的图片文件
     */
    private boolean isValidImageFile(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        for (String extension : ALLOWED_EXTENSIONS) {
            if (lowerCaseFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }
}