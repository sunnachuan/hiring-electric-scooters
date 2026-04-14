package com.scooter.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Slf4j
public class FileUploadController {
    
    private final String UPLOAD_DIR = "uploads/damage-images/";
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件不能为空");
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !(contentType.startsWith("image/jpeg") || contentType.startsWith("image/png"))) {
                return ResponseEntity.badRequest().body("只支持JPG和PNG格式的图片");
            }
            
            // 检查文件大小（最大2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("文件大小不能超过2MB");
            }
            
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // 保存文件
            Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件URL
            String fileUrl = "/uploads/damage-images/" + uniqueFileName;
            
            log.info("文件上传成功: {}", fileUrl);
            
            return ResponseEntity.ok(new UploadResponse(fileUrl, originalFilename, file.getSize()));
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError().body("文件上传失败");
        }
    }
    
    // 上传响应类
    public static class UploadResponse {
        private String url;
        private String filename;
        private long size;
        
        public UploadResponse(String url, String filename, long size) {
            this.url = url;
            this.filename = filename;
            this.size = size;
        }
        
        // getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
    }
}