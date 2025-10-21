package com.example.project.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectDTO {
    private String id;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer totalProgress;
    private String managerId;
    private String departmentId;
    
    // 团队成员ID列表
    private List<String> teamMemberIds;
}