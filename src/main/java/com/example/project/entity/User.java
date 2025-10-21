package com.example.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 替换@Data注解，排除关联字段
@Data
@EqualsAndHashCode(exclude = { "managedProjects", "projects", "assigneeTasks" })
@ToString(exclude = { "managedProjects", "projects", "assigneeTasks" })
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "manager")
    // 删除字段级别的exclude注解，只使用类级别的配置
    @JsonIgnore
    private List<Project> managedProjects;

    // 在User类的projects字段上，优化@JsonIgnoreProperties配置
    @ManyToMany(mappedBy = "teamMembers")
    @JsonIgnore // 直接忽略projects字段
    private Set<Project> projects = new HashSet<>();

    // 为assigneeTasks字段添加忽略配置
    @OneToMany(mappedBy = "assignee")
    @JsonIgnore // 直接忽略assigneeTasks字段
    private List<Task> assigneeTasks = new ArrayList<>();

    // 删除这个不存在的映射关系
    // @OneToMany(mappedBy = "creator")
    // @JsonIgnoreProperties(value = { "creator", "project", "assignee" },
    // allowSetters = true)
    // private List<Task> createdTasks = new ArrayList<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    // 添加avatar字段，增加长度以支持更长的存储路径
    @Column(length = 1024)
    private String avatar;

    public enum UserRole {
        ADMIN, MANAGER
    }

}