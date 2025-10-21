package com.example.project.service;

import com.example.project.dto.ProjectDTO;
import com.example.project.entity.Project;
import com.example.project.entity.Project.ProjectStatus;
import com.example.project.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// 添加新方法到接口
public interface ProjectService {
    // 基本CRUD操作
    Project createProject(Project project);

    Project updateProject(Project project);

    void deleteProject(String id);

    Project getProjectById(String id);

    // 此方法与下方重复，已移除
    List<Project> getAllProjects();

    // 分页查询
    Page<Project> findProjectsByPage(Pageable pageable);

    // 条件查询
    List<Project> findProjectsByName(String name);

    List<Project> findProjectsByStatus(Project.ProjectStatus status);

    // 修改所有使用Long类型ID的方法
    List<Project> findProjectsByDepartmentId(String departmentId);

    List<Project> findProjectsByManagerId(String managerId);

    List<Project> findProjectsByTimeRange(LocalDateTime start, LocalDateTime end);

    // 带筛选条件的分页查询
    Page<Project> findProjectsWithFilters(Pageable pageable, String status, String departmentId, String keyword);

    // 项目统计功能
    Integer calculateProjectProgress(String projectId);

    Map<String, Object> getProjectStatistics();

    Map<String, Object> getProjectDetailStatistics(String projectId);

    // 添加缺失的评估项目健康状态方法
    String evaluateProjectHealth(String projectId);

    // 添加缺失的导出项目数据方法
    byte[] exportProjectData(String projectId);

    // 修改方法签名以匹配实现
    List<Map<String, Object>> getProjectProgressTrend(String projectId, LocalDateTime start, LocalDateTime end);

    List<Map<String, Object>> getOverallProjectProgressTrend(LocalDateTime start, LocalDateTime end);

    // 新增综合统计接口
    Map<String, Object> getProjectComprehensiveStatistics();

    // 项目任务管理
    List<Task> getProjectTasks(String projectId);

    void updateProjectProgress(String projectId);

    // 新增从DTO创建和更新项目的方法
    Project createProjectFromDTO(ProjectDTO projectDTO);

    Project updateProjectFromDTO(ProjectDTO projectDTO);

    void updateProjectStatus(String id, ProjectStatus delayed);

    void checkAndUpdateDelayedProjects();
}