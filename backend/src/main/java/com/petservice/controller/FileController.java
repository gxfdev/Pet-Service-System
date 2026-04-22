package com.petservice.controller;

import com.petservice.common.Result;
import com.petservice.entity.User;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private UserService userService;

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file, Authentication auth) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        try {
            // 确保上传目录存在
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存文件
            Path filePath = dirPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            // 返回访问 URL
            String url = "/api/file/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication auth) {
        Result<String> uploadResult = uploadFile(file, auth);
        if (uploadResult.getCode() == 200 && uploadResult.getData() != null) {
            // 自动更新用户头像
            User user = (User) auth.getPrincipal();
            user.setAvatar(uploadResult.getData());
            userService.updateById(user);
        }
        return uploadResult;
    }
}
