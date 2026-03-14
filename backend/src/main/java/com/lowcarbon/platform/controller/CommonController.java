package com.lowcarbon.platform.controller;

import com.lowcarbon.platform.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private final Path uploadDir;

    public CommonController(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping("/upload-image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("\u8bf7\u9009\u62e9\u8981\u4e0a\u4f20\u7684\u56fe\u7247");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new ApiException("\u56fe\u7247\u5927\u5c0f\u4e0d\u80fd\u8d85\u8fc7 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException("\u4ec5\u652f\u6301\u56fe\u7247\u6587\u4ef6\u4e0a\u4f20");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExt = (extension == null || extension.isBlank()) ? "jpg" : extension.toLowerCase();
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + "." + safeExt;

        try {
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ApiException("\u56fe\u7247\u4e0a\u4f20\u5931\u8d25\uff1a" + ex.getMessage());
        }

        String url = "/uploads/" + fileName;

        Map<String, Object> data = new HashMap<>();
        data.put("fileName", fileName);
        data.put("url", url);
        return data;
    }
}
