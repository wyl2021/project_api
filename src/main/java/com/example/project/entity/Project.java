package com.example.project.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
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
@EqualsAndHashCode(exclude = { "teamMembers", "tasks", "manager", "department" })
@ToString(exclude = { "teamMembers", "tasks", "manager", "department" })
@Entity
@Table(name = "project")
public class Project {
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // 添加@JsonIgnore避免循环引用
    private List<Task> tasks = new ArrayList<>();
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "original_end_time")
    private LocalDateTime originalEndTime;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column(name = "total_progress")
    private Integer totalProgress = 0;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    // @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval =
    // true)
    // @JsonIgnore // 添加@JsonIgnore避免循环引用
    // private List<Task> tasks = new ArrayList<>();

    // 在Project类中添加团队成员字段
    // 在Project类的teamMembers字段上，优化@JsonIgnoreProperties配置
    // @ManyToMany(fetch = FetchType.EAGER)
    // @JoinTable(name = "user_project", joinColumns = @JoinColumn(name =
    // "project_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    // @JsonIgnoreProperties(value = {"projects", "tasks", "assigneeTasks"},
    // allowSetters = true)
    // 恢复并修改teamMembers字段，添加@JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_project", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnore // 忽略原始的teamMembers对象列表序列化
    private Set<User> teamMembers = new HashSet<>();

    // 添加JSON getter方法返回ID列表
    @JsonGetter("teamMembers")
    public List<String> getTeamMemberIds() {
        List<String> memberIds = new ArrayList<>();
        if (teamMembers != null) {
            for (User user : teamMembers) {
                memberIds.add(user.getId());
            }
        }
        return memberIds;
    }

    // 保留内部使用的getter
    public List<User> getTeamMembersInternal() {
        return teamMembers != null ? new ArrayList<>(teamMembers) : new ArrayList<>();
    }

    // 修改业务逻辑中使用getTeamMembers的地方，改为getTeamMembersInternal
    public void setTeamMembers(List<User> teamMembers) {
        this.teamMembers = new HashSet<>(teamMembers);
    }

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    public enum ProjectStatus {
        NOT_STARTED("未开始", "planned"),
        IN_PROGRESS("进行中", "in_progress"),
        COMPLETED("已完成", "completed"),
        DELAYED("已延迟", "delayed"),
        ON_HOLD("已暂停", "on_hold");

        private final String text; // 中文文本
        private final String value; // 英文值

        ProjectStatus(String text, String value) {
            this.text = text;
            this.value = value;
        }

        public String getValue() {
            return value; // 序列化时使用英文值
        }

        // 提供获取中文文本的方法
        @JsonValue
        public String getText() {
            return text;
        }

        // 自定义反序列化逻辑，支持中文和英文
        @JsonCreator
        public static ProjectStatus fromValue(String value) {
            for (ProjectStatus status : ProjectStatus.values()) {
                if (status.text.equalsIgnoreCase(value) || status.value.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid ProjectStatus value: " + value);
        }
    }
}