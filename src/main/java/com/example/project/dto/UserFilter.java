package com.example.project.dto;

import com.example.project.entity.User;
import java.util.List;

/**
 * 用户筛选条件类
 * 用于统一用户查询参数，替代多个独立的查询方法
 */
public class UserFilter {
    private String departmentId;
    private String username;
    private String email;
    private String phone;
    private User.UserRole role;
    private Boolean isActive;
    private List<User> users; // 存储筛选结果的用户列表
    
    // getter和setter方法
    public String getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
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
    
    public User.UserRole getRole() {
        return role;
    }
    
    public void setRole(User.UserRole role) {
        this.role = role;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }
}