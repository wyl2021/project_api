package com.example.project.controller;

import com.example.project.entity.User;
import com.example.project.service.UserService;
import com.example.project.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.example.project.entity.User;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // 设置用户角色，如果请求中没有指定，则默认为MANAGER
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                user.setRole(User.UserRole.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // 如果角色不存在，则使用默认角色
                user.setRole(User.UserRole.MANAGER);
            }
        } else {
            user.setRole(User.UserRole.MANAGER); // 默认角色为普通用户
        }

        User registeredUser = userService.registerUser(user, request.getPassword());
        return new ResponseEntity<>(new ApiResponse(true, "用户注册成功", registeredUser), HttpStatus.CREATED);
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request) {
        Map<String, Object> loginResult = userService.loginUser(
                request.getUsername(),
                request.getPassword(),
                request.isEncrypted());
        return new ResponseEntity<>(new ApiResponse(true, "登录成功", loginResult), HttpStatus.OK);
    }

    // 用户登出
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        userService.logoutUser(token);
        return new ResponseEntity<>(new ApiResponse(true, "登出成功", null), HttpStatus.OK);
    }

    // 获取当前登录用户信息
    @GetMapping("/info")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        User user = userService.validateToken(token);
        return new ResponseEntity<>(new ApiResponse(true, "用户信息获取成功", user), HttpStatus.OK);
    }

    // 用户注册请求类
    static class UserRegistrationRequest {
        private String username;
        private String name;
        private String password;
        private String email;
        private String phone;
        private String role; // 可选字段，用于指定用户角色

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    // 用户登录请求类
    static class UserLoginRequest {
        private String username;
        private String password;
        private boolean encrypted = false; // 标识密码是否已加密

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isEncrypted() {
            return encrypted;
        }

        public void setEncrypted(boolean encrypted) {
            this.encrypted = encrypted;
        }
    }
}