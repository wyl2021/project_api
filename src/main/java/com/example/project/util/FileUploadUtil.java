package com.example.project.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileUploadUtil {

    // 默认头像URL
    public static final String DEFAULT_AVATAR_URL = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fci.xiaohongshu.com%2Fc34b7b74-ba38-0456-982a-43c0f97522fe%3FimageView2%2F2%2Fw%2F1080%2Fformat%2Fjpg&refer=http%3A%2F%2Fci.xiaohongshu.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1763168553&t=eddfc58c26901fd446b900d0d234ac3d";

    @Value("${file.upload-dir:./uploads/avatars}")
    private String uploadDir;

    // 支持的图片格式
    private static final String[] ALLOWED_IMAGE_TYPES = { "image/jpeg", "image/png", "image/gif", "image/webp" };

    /**
     * 保存上传的文件
     * 
     * @param file      上传的文件
     * @param uploadDir 上传目录
     * @return 保存后的文件名
     * @throws IOException              文件保存异常
     * @throws IllegalArgumentException 无效的文件类型
     */
    public static String saveFile(MultipartFile file, String uploadDir) throws IOException, IllegalArgumentException {
        // 验证文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件为空");
        }

        // 验证文件类型
        if (!isAllowedImageType(file.getContentType())) {
            throw new IllegalArgumentException("不支持的文件类型，请上传JPG、PNG、GIF或WebP格式的图片");
        }

        // 验证文件大小（限制为5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }

        // 获取原始文件名的扩展名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);

        // 生成唯一的文件名
        String fileName = UUID.randomUUID().toString() + "." + extension;

        // 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    /**
     * 检查是否为允许的图片类型
     */
    private static boolean isAllowedImageType(String contentType) {
        if (contentType == null) {
            return false;
        }

        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 删除文件
     */
    public static void deleteFile(String filePath) {
        if (filePath != null && !filePath.isEmpty() && !filePath.equals(DEFAULT_AVATAR_URL)) {
            try {
                // 这里只删除本地文件，不处理远程URL
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                // 文件删除失败，记录日志但不抛出异常
                e.printStackTrace();
            }
        }
    }
}