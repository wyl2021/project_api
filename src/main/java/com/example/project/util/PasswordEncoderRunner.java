package com.example.project.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密运行器，用于生成BCrypt加密后的密码
 */
public class PasswordEncoderRunner {
    
    public static void main(String[] args) {
        // 创建BCrypt密码编码器
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 要加密的密码
        String rawPassword = "1YHA8299D";
        
        // 生成加密后的密码
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // 输出加密后的密码
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("\n请使用以下SQL语句更新数据库中的admin用户密码：");
        System.out.println("UPDATE project_progress.user SET password = '" + encodedPassword + "' WHERE username = 'admin';");
    }
}