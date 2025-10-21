package com.example.project.controller;

import com.example.project.dto.ProjectDTO;
import com.example.project.entity.Project;
import com.example.project.service.ProjectService;
import com.example.project.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 修改类级别RequestMapping，移除重复的/api前缀
@RestController
@RequestMapping("/projects") // 只需配置/projects，配合全局的/api上下文路径
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    // 创建项目 - 使用DTO
    @PostMapping
    public ResponseEntity<ApiResponse<Project>> createProject(@RequestBody ProjectDTO projectDTO) {
        try {
            Project project = projectService.createProjectFromDTO(projectDTO);
            return new ResponseEntity<>(
                    ApiResponse.success("项目创建成功", project),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("项目创建失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新项目 - 使用DTO
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> updateProject(@PathVariable String id,
            @RequestBody ProjectDTO projectDTO) {
        try {
            projectDTO.setId(id);
            Project project = projectService.updateProjectFromDTO(projectDTO);
            return new ResponseEntity<>(
                    ApiResponse.success("项目更新成功", project),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("更新项目失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 获取所有项目（带分页和筛选）
    // 修改获取所有项目的方法，添加自动检查并更新延期状态的逻辑
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String keyword) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            // 先批量检查并更新所有延迟的项目
            projectService.checkAndUpdateDelayedProjects();

            Page<Project> projects = projectService.findProjectsWithFilters(pageable, status, departmentId, keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("content", projects.getContent());
            response.put("currentPage", projects.getNumber());
            response.put("totalItems", projects.getTotalElements());
            response.put("totalPages", projects.getTotalPages());
            response.put("pageSize", projects.getSize());

            return new ResponseEntity<>(
                    ApiResponse.success("项目获取成功", response),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<ApiResponse<Map<String, Object>>>(
                    ApiResponse.error("获取项目列表失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根据ID获取项目
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable String id) {
        try {
            Project project = projectService.getProjectById(id);
            return new ResponseEntity<>(
                    ApiResponse.success("项目获取成功", project),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("获取项目失败: " + e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }

    // 删除项目
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable String id) {
        try {
            projectService.deleteProject(id);
            return new ResponseEntity<>(
                    ApiResponse.success("项目删除成功"),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("删除项目失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根据名称搜索项目
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Project>>> searchProjectsByName(@RequestParam String name) {
        try {
            List<Project> projects = projectService.findProjectsByName(name);
            return new ResponseEntity<>(
                    ApiResponse.success("搜索成功", projects),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("搜索失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根据状态获取项目
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByStatus(@PathVariable String status) {
        try {
            Project.ProjectStatus projectStatus = Project.ProjectStatus.valueOf(status.toUpperCase());
            List<Project> projects = projectService.findProjectsByStatus(projectStatus);
            return new ResponseEntity<>(
                    ApiResponse.success("项目获取成功", projects),
                    HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    ApiResponse.error("无效的状态: " + status),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("获取项目失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根据部门ID获取项目
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByDepartment(@PathVariable String departmentId) {
        try {
            List<Project> projects = projectService.findProjectsByDepartmentId(departmentId);
            return new ResponseEntity<>(
                    ApiResponse.success("项目获取成功", projects),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("获取项目失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根据负责人ID获取项目
    @GetMapping("/manager/{managerId}")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByManager(@PathVariable String managerId) {
        try {
            List<Project> projects = projectService.findProjectsByManagerId(managerId);
            return new ResponseEntity<>(
                    ApiResponse.success("项目获取成功", projects),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("获取项目失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 获取项目综合统计信息
    @GetMapping("/statistics/comprehensive")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectComprehensiveStatistics() {
        try {
            Map<String, Object> statistics = projectService.getProjectComprehensiveStatistics();
            return new ResponseEntity<>(
                    ApiResponse.success("统计信息获取成功", statistics),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("获取统计信息失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 获取项目详细统计信息
    @GetMapping("/{id}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectDetailStatistics(@PathVariable String id) {
        try {
            Map<String, Object> statistics = projectService.getProjectDetailStatistics(id);
            return new ResponseEntity<>(
                    ApiResponse.success("项目统计信息获取成功", statistics),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("获取项目统计信息失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 获取项目进度趋势
    @GetMapping("/{id}/progress-trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProjectProgressTrend(
            @PathVariable String id,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);

            List<Map<String, Object>> trendData = projectService.getProjectProgressTrend(id, start, end);
            return new ResponseEntity<>(
                    ApiResponse.success("进度趋势获取成功", trendData),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("获取进度趋势失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新项目进度
    @PostMapping("/{id}/update-progress")
    public ResponseEntity<ApiResponse<Void>> updateProjectProgress(@PathVariable String id) {
        try {
            projectService.updateProjectProgress(id);
            return new ResponseEntity<>(
                    ApiResponse.success("项目进度更新成功"),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("更新项目进度失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 评估项目健康状态
    @GetMapping("/{id}/health")
    public ResponseEntity<ApiResponse<String>> evaluateProjectHealth(@PathVariable String id) {
        try {
            String healthStatus = projectService.evaluateProjectHealth(id);
            return new ResponseEntity<>(
                    ApiResponse.success("健康状态评估成功", healthStatus),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("评估健康状态失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 导出项目数据
    @GetMapping("/{id}/export")
    public ResponseEntity<?> exportProjectData(@PathVariable String id) {
        try {
            byte[] data = projectService.exportProjectData(id);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=project_" + id + ".xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(data);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error("导出项目数据失败: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 辅助方法：解析日期时间
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e1) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (Exception e2) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    return LocalDateTime.parse(dateTimeStr + " 00:00:00",
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e3) {
                    throw new IllegalArgumentException("无效的日期格式: " + dateTimeStr);
                }
            }
        }
    }
}