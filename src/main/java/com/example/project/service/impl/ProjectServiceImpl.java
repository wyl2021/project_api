package com.example.project.service.impl;

import com.example.project.entity.Project;
import com.example.project.entity.Task;
import com.example.project.repository.ProjectRepository;
import com.example.project.repository.TaskRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.ProjectService;
import com.example.project.service.UserService;
import com.example.project.util.DateUtil;
import com.example.project.util.ExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.project.entity.User;
import com.example.project.entity.Department;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.example.project.repository.DepartmentRepository;

// 在类顶部添加
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 添加import
import com.example.project.dto.ProjectDTO;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // Add DepartmentRepository to handle department relationships properly
    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void checkAndUpdateDelayedProjects() {
        LocalDateTime now = LocalDateTime.now();
        List<Project> delayedProjects = projectRepository.findDelayedProjects(now);

        for (Project project : delayedProjects) {
            project.setStatus(Project.ProjectStatus.DELAYED);
            projectRepository.save(project);
        }
    }

    @Override
    public void updateProjectStatus(String projectId, Project.ProjectStatus status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("找不到ID为" + projectId + "的项目"));
        project.setStatus(status);
        projectRepository.save(project);
    }

    @Override
    public Project createProject(Project project) {
        project.setCreatedTime(LocalDateTime.now());
        project.setUpdatedTime(LocalDateTime.now());
        project.setTotalProgress(0);

        // 设置原始结束时间
        if (project.getEndTime() != null) {
            project.setOriginalEndTime(project.getEndTime());
        }

        // Properly handle manager relationship
        if (project.getManager() != null) {
            if (project.getManager().getId() != null) {
                // Fetch the managed User entity from the database
                User managedManager = userRepository.findById(project.getManager().getId())
                        .orElseThrow(() -> new RuntimeException(
                                "Manager user not found with ID: " + project.getManager().getId()));
                // Set the managed User entity to the project
                project.setManager(managedManager);
            } else {
                // If no ID is provided, set manager to null to avoid transient instance issues
                project.setManager(null);
            }
        }

        // Properly handle department relationship
        if (project.getDepartment() != null) {
            if (project.getDepartment().getId() != null) {
                // Fetch the managed Department entity from the database
                Department managedDepartment = departmentRepository.findById(project.getDepartment().getId())
                        .orElseThrow(() -> new RuntimeException(
                                "Department not found with ID: " + project.getDepartment().getId()));
                // Set the managed Department entity to the project
                project.setDepartment(managedDepartment);
            } else {
                // If no ID is provided, set department to null
                project.setDepartment(null);
            }
        }

        return projectRepository.save(project);
    }

    @Override
    public Project updateProject(Project project) {
        project.setUpdatedTime(LocalDateTime.now());
        // 保留原始结束时间，不随更新而改变
        Project existingProject = getProjectById(project.getId());

        if (existingProject.getOriginalEndTime() != null) {
            project.setOriginalEndTime(existingProject.getOriginalEndTime());
        }

        // Properly handle manager relationship during update
        if (project.getManager() != null && project.getManager().getId() != null) {
            // Fetch the managed User entity from the database
            User managedManager = userRepository.findById(project.getManager().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Manager user not found with ID: " + project.getManager().getId()));
            // Set the managed User entity to the project
            project.setManager(managedManager);
        }

        return projectRepository.save(project);
    }

    @Override
    public void deleteProject(String id) {
        projectRepository.deleteById(id);
    }

    @Override
    public Project getProjectById(String id) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User currentUser;
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            currentUser = (User) authentication.getPrincipal();
        } else {
            currentUser = null;
        }

        // 先找到项目
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到ID为" + id + "的项目"));

        // 权限控制
        // 修改权限检查逻辑，确保不会触发循环引用
        if (currentUser != null && !User.UserRole.ADMIN.equals(currentUser.getRole())) {
            boolean isManager = currentUser.getId().equals(project.getManager().getId());

            // 使用安全的方式检查用户是否是项目成员或任务执行人
            boolean hasAccess = isManager;

            // 检查任务权限的方式保持不变
            if (!hasAccess && project.getTasks() != null) {
                hasAccess = project.getTasks().stream()
                        .anyMatch(task -> task.getAssignee() != null
                                && currentUser.getId().equals(task.getAssignee().getId()));
            }

            if (!hasAccess) {
                throw new RuntimeException("您无权访问此项目信息");
            }
        }

        return project;
    }

    // 修复findProjectsWithFilters方法中的部门ID类型
    @Override
    public Page<Project> findProjectsWithFilters(Pageable pageable, String status, String departmentId,
            String keyword) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User currentUser;
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            currentUser = (User) authentication.getPrincipal();
        } else {
            currentUser = null;
        }

        // 构建动态查询条件
        Specification<Project> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 根据状态筛选
            if (StringUtils.hasText(status)) {
                try {
                    Project.ProjectStatus projectStatus = Project.ProjectStatus.valueOf(status.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), projectStatus));
                } catch (IllegalArgumentException e) {
                    // 状态参数无效，忽略该筛选条件
                }
            }

            // 根据部门ID筛选
            if (departmentId != null) {
                // 在构建查询条件时使用departmentId作为String类型
                if (StringUtils.hasText(departmentId)) {
                    predicates.add(criteriaBuilder.equal(root.get("department").get("id"), departmentId));
                }
            }

            // 根据关键词筛选（名称或描述包含关键词）
            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern);
                Predicate descLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern);
                predicates.add(criteriaBuilder.or(nameLike, descLike));
            }

            // 根据当前用户过滤项目
            if (currentUser != null) {
                // 如果是管理员角色，可以查看所有项目
                if (!User.UserRole.ADMIN.equals(currentUser.getRole())) {
                    // 非管理员可以查看自己负责的项目或参与的项目
                    Join<Project, Task> taskJoin = root.join("tasks", JoinType.LEFT);
                    Predicate isManager = criteriaBuilder.equal(root.get("manager").get("id"), currentUser.getId());
                    Predicate isTeamMember = criteriaBuilder.equal(taskJoin.get("assignee").get("id"),
                            currentUser.getId());

                    // 使用DISTINCT确保不返回重复的项目
                    query.distinct(true);

                    predicates.add(criteriaBuilder.or(isManager, isTeamMember));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return projectRepository.findAll(spec, pageable);
    }

    // 修改findProjectsByPage方法，添加权限控制
    @Override
    public Page<Project> findProjectsByPage(Pageable pageable) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User currentUser;
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            currentUser = (User) authentication.getPrincipal();
        } else {
            currentUser = null;
        }

        // 管理员可以查看所有项目，普通用户只能查看自己负责或参与的项目
        if (currentUser != null && !User.UserRole.ADMIN.equals(currentUser.getRole())) {
            Specification<Project> spec = (root, query, criteriaBuilder) -> {
                Join<Project, Task> taskJoin = root.join("tasks", JoinType.LEFT);
                Predicate isManager = criteriaBuilder.equal(root.get("manager").get("id"), currentUser.getId());
                Predicate isTeamMember = criteriaBuilder.equal(taskJoin.get("assignee").get("id"), currentUser.getId());

                query.distinct(true);

                return criteriaBuilder.or(isManager, isTeamMember);
            };

            return projectRepository.findAll(spec, pageable);
        }

        return projectRepository.findAll(pageable);
    }

    // 修改getAllProjects方法，添加权限控制
    @Override
    public List<Project> getAllProjects() {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User currentUser;
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            currentUser = (User) authentication.getPrincipal();
        } else {
            currentUser = null;
        }

        // 管理员可以查看所有项目，普通用户只能查看自己负责或参与的项目
        if (currentUser != null && !User.UserRole.ADMIN.equals(currentUser.getRole())) {
            Specification<Project> spec = (root, query, criteriaBuilder) -> {
                Join<Project, Task> taskJoin = root.join("tasks", JoinType.LEFT);
                Predicate isManager = criteriaBuilder.equal(root.get("manager").get("id"), currentUser.getId());
                Predicate isTeamMember = criteriaBuilder.equal(taskJoin.get("assignee").get("id"), currentUser.getId());

                query.distinct(true);

                return criteriaBuilder.or(isManager, isTeamMember);
            };

            return projectRepository.findAll(spec);
        }

        return projectRepository.findAll();
    }

    @Override
    public List<Project> findProjectsByName(String name) {
        return projectRepository.findByNameContaining(name);
    }

    @Override
    public List<Project> findProjectsByStatus(Project.ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }

    @Override
    public List<Project> findProjectsByDepartmentId(String departmentId) {
        return projectRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<Project> findProjectsByManagerId(String managerId) {
        return projectRepository.findByManagerId(managerId);
    }

    @Override
    public List<Project> findProjectsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return projectRepository.findByStartTimeBetween(start, end);
    }

    @Override
    public Integer calculateProjectProgress(String projectId) {
        Double avgProgress = taskRepository.calculateAverageProgressByProject(projectId);
        return avgProgress != null ? avgProgress.intValue() : 0;
    }

    @Override
    public Map<String, Object> getProjectComprehensiveStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 默认返回空统计数据
        statistics.put("totalProjects", 0L);
        statistics.put("averageProgress", 0.0);
        statistics.put("inProgressProjects", 0L);
        statistics.put("completedProjects", 0L);
        statistics.put("delayedProjects", 0L);
        statistics.put("statusDistribution", new HashMap<>());
        statistics.put("onTimeCompletionRate", 0.0);
        statistics.put("averageCompletionRate", 0.0);

        try {
            // 从当前线程获取请求对象
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String authorization = request.getHeader("Authorization");

            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                // 使用UserService验证token并获取用户信息
                User currentUser = userService.validateToken(token);

                // 构建用户可见项目的查询条件
                Specification<Project> spec = (root, query, criteriaBuilder) -> {
                    if (User.UserRole.ADMIN.equals(currentUser.getRole())) {
                        // 管理员可以查看所有项目
                        return null;
                    } else {
                        // 普通用户只能查看自己负责或参与的项目
                        Join<Project, Task> taskJoin = root.join("tasks", JoinType.LEFT);
                        Predicate isManager = criteriaBuilder.equal(root.get("manager").get("id"), currentUser.getId());
                        Predicate isTeamMember = criteriaBuilder.equal(taskJoin.get("assignee").get("id"),
                                currentUser.getId());

                        query.distinct(true);
                        return criteriaBuilder.or(isManager, isTeamMember);
                    }
                };

                // 1. 获取当前用户可见的项目
                List<Project> userProjects = projectRepository.findAll(spec);

                // 2. 计算项目总数和平均进度
                long totalProjects = userProjects.size();
                double avgProgress = userProjects.stream()
                        .mapToInt(p -> p.getTotalProgress() != null ? p.getTotalProgress() : 0)
                        .average()
                        .orElse(0.0);

                statistics.put("totalProjects", totalProjects);
                statistics.put("averageProgress", Math.round(avgProgress * 100.0) / 100.0);
                statistics.put("averageCompletionRate", Math.round(avgProgress * 100.0) / 100.0);

                // 3. 按状态统计项目数量
                Map<String, Long> statusCountMap = userProjects.stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getStatus() != null ? p.getStatus().name() : "UNKNOWN",
                                Collectors.counting()));

                statistics.put("inProgressProjects", statusCountMap.getOrDefault("IN_PROGRESS", 0L));
                statistics.put("completedProjects", statusCountMap.getOrDefault("COMPLETED", 0L));
                statistics.put("delayedProjects", statusCountMap.getOrDefault("DELAYED", 0L));
                statistics.put("statusDistribution", statusCountMap);

                // 4. 计算按时完成率
                List<Project> completedProjects = userProjects.stream()
                        .filter(p -> Project.ProjectStatus.COMPLETED.equals(p.getStatus()))
                        .collect(Collectors.toList());

                long onTimeCompleted = completedProjects.stream()
                        .filter(p -> p.getEndTime() != null && p.getOriginalEndTime() != null &&
                                (p.getEndTime().isBefore(p.getOriginalEndTime())
                                        || p.getEndTime().isEqual(p.getOriginalEndTime())))
                        .count();

                if (completedProjects.size() > 0) {
                    double onTimeRate = (double) onTimeCompleted / completedProjects.size() * 100;
                    statistics.put("onTimeCompletionRate", Math.round(onTimeRate * 100.0) / 100.0);
                } else {
                    statistics.put("onTimeCompletionRate", 0.0);
                }
            }
        } catch (Exception e) {
            // 记录异常但不中断流程
            System.err.println("获取用户信息或计算统计数据时出错: " + e.getMessage());
        }

        return statistics;
    }

    @Override
    public Map<String, Object> getProjectDetailStatistics(String projectId) {
        Map<String, Object> statistics = new HashMap<>();
        Project project = getProjectById(projectId);

        // 项目基本信息
        statistics.put("projectName", project.getName());
        statistics.put("projectStatus", project.getStatus());
        statistics.put("currentProgress", project.getTotalProgress());

        // 任务统计
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        statistics.put("totalTasks", tasks.size());

        // 按状态统计任务数量
        Map<Task.TaskStatus, Long> taskStatusCount = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        statistics.put("taskStatusCount", taskStatusCount);

        // 计算平均任务进度
        Double avgTaskProgress = tasks.stream()
                .mapToInt(Task::getProgress)
                .average()
                .orElse(0.0);
        statistics.put("avgTaskProgress", Math.round(avgTaskProgress));

        // 计算逾期任务数量
        long overdueTasks = tasks.stream()
                .filter(task -> task.getEndTime() != null && task.getEndTime().isBefore(LocalDateTime.now())
                        && !task.getStatus().equals(Task.TaskStatus.COMPLETED))
                .count();
        statistics.put("overdueTasks", overdueTasks);

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getOverallProjectProgressTrend(LocalDateTime start, LocalDateTime end) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        // 这里我们模拟一些数据，实际实现中应该从数据库查询
        // 我们可以按天或按周生成数据点
        LocalDateTime current = start;
        while (current.isBefore(end) || current.isEqual(end)) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", current.format(DateTimeFormatter.ISO_LOCAL_DATE));

            // 生成模拟的平均进度数据（10-90之间的随机值）
            double avgProgress = 10 + Math.random() * 80;
            dataPoint.put("averageProgress", Math.round(avgProgress * 100.0) / 100.0);

            // 添加进行中和完成的项目数量统计
            dataPoint.put("inProgressCount", (long) (Math.random() * 5) + 1);
            dataPoint.put("completedCount", (long) (Math.random() * 3));

            trendData.add(dataPoint);

            // 按天递增
            current = current.plusDays(1);
        }

        return trendData;
    }

    @Override
    public List<Map<String, Object>> getProjectProgressTrend(String projectId, LocalDateTime start, LocalDateTime end) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        // 获取项目信息
        Project project = getProjectById(projectId);

        // 生成模拟的进度数据点
        LocalDateTime current = start;
        int startProgress = project.getTotalProgress() > 50 ? project.getTotalProgress() - 20 : 0;
        int endProgress = project.getTotalProgress();

        while (current.isBefore(end) || current.isEqual(end)) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", current.format(DateTimeFormatter.ISO_LOCAL_DATE));

            // 计算当前日期对应的进度（线性增长）
            long totalDays = start.until(end, ChronoUnit.DAYS);
            long currentDays = start.until(current, ChronoUnit.DAYS);
            double progress = startProgress + ((double) currentDays / totalDays) * (endProgress - startProgress);

            dataPoint.put("progress", Math.round(progress * 100.0) / 100.0);
            dataPoint.put("projectName", project.getName());

            trendData.add(dataPoint);

            // 按天递增
            current = current.plusDays(1);
        }

        return trendData;
    }

    @Override
    public List<Task> getProjectTasks(String projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @Override
    public void updateProjectProgress(String projectId) {
        Integer progress = calculateProjectProgress(projectId);
        Project project = getProjectById(projectId);
        project.setTotalProgress(progress);

        // 根据进度更新项目状态
        if (progress == 100) {
            project.setStatus(Project.ProjectStatus.COMPLETED);
        } else if (progress > 0) {
            project.setStatus(Project.ProjectStatus.IN_PROGRESS);
        }

        projectRepository.save(project);
    }

    @Override
    public String evaluateProjectHealth(String projectId) {
        Project project = getProjectById(projectId);

        // 修复：使用正确的类型转换
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        if (tasks.isEmpty()) {
            return "NO_TASKS";
        }

        // 计算逾期任务比例
        long overdueTasks = tasks.stream()
                .filter(task -> task.getEndTime() != null && task.getEndTime().isBefore(LocalDateTime.now())
                        && task.getStatus() != null && !task.getStatus().equals(Task.TaskStatus.COMPLETED))
                .count();

        double overdueRatio = (double) overdueTasks / tasks.size();

        // 获取项目进度
        Integer progress = project.getTotalProgress();
        if (progress == null) {
            progress = calculateProjectProgress(projectId);
        }

        // 评估健康状态
        if (overdueRatio > 0.3) {
            return "UNHEALTHY";
        } else if (overdueRatio > 0.1) {
            return "NEEDS_ATTENTION";
        } else if (progress < 50 && project.getEndTime() != null
                && project.getEndTime().minusDays(7).isBefore(LocalDateTime.now())) {
            return "AT_RISK";
        } else {
            return "HEALTHY";
        }
    }

    @Autowired
    private ExportUtil exportUtil;

    // 添加缺失的formatLocalDateTime方法
    private String formatLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public byte[] exportProjectData(String projectId) {
        Project project = getProjectById(projectId);
        List<Task> tasks = getProjectTasks(projectId);

        // 准备导出数据
        List<Map<String, Object>> dataList = new ArrayList<>();

        // 添加项目基本信息
        Map<String, Object> projectInfo = new HashMap<>();
        projectInfo.put("项目名称", project.getName());
        projectInfo.put("项目描述", project.getDescription());
        projectInfo.put("开始时间",
                project.getStartTime() != null
                        ? formatLocalDateTime(project.getStartTime())
                        : "");
        projectInfo.put("结束时间",
                project.getEndTime() != null
                        ? formatLocalDateTime(project.getEndTime())
                        : "");
        projectInfo.put("状态", project.getStatus() != null ? project.getStatus().name() : "");
        projectInfo.put("总进度", (project.getTotalProgress() != null ? project.getTotalProgress() : 0) + "%");
        projectInfo.put("负责人",
                project.getManager() != null && project.getManager().getId() != null ? project.getManager().getId()
                        : "");
        projectInfo.put("所属部门",
                project.getDepartment() != null && project.getDepartment().getId() != null
                        ? project.getDepartment().getId()
                        : "");
        dataList.add(projectInfo);

        // 添加空行分隔
        dataList.add(new HashMap<>());

        // 添加任务信息表头
        Map<String, Object> taskHeader = new HashMap<>();
        taskHeader.put("任务名称", "任务名称");
        taskHeader.put("任务描述", "任务描述");
        taskHeader.put("负责人", "负责人");
        taskHeader.put("状态", "状态");
        taskHeader.put("进度", "进度");
        taskHeader.put("开始时间", "开始时间");
        taskHeader.put("结束时间", "结束时间");
        taskHeader.put("预计工时", "预计工时");
        taskHeader.put("实际工时", "实际工时");
        dataList.add(taskHeader);

        // 添加任务数据
        for (Task task : tasks) {
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("任务名称", task.getName() != null ? task.getName() : "");
            taskData.put("任务描述", task.getDescription() != null ? task.getDescription() : "");
            taskData.put("负责人",
                    task.getAssignee() != null && task.getAssignee().getId() != null ? task.getAssignee().getId() : "");
            taskData.put("状态", task.getStatus() != null ? task.getStatus().name() : "");
            taskData.put("进度", (task.getProgress() != null ? task.getProgress() : 0) + "%");
            taskData.put("开始时间",
                    task.getStartTime() != null
                            ? formatLocalDateTime(task.getStartTime())
                            : "");
            taskData.put("结束时间",
                    task.getEndTime() != null
                            ? formatLocalDateTime(task.getEndTime())
                            : "");
            taskData.put("预计工时", task.getEstimatedHours() != null ? task.getEstimatedHours() : "");
            taskData.put("实际工时", task.getActualHours() != null ? task.getActualHours() : "");
            dataList.add(taskData);
        }

        // 准备表头
        List<String> headers = Arrays.asList("项目名称", "项目描述", "开始时间", "结束时间", "状态", "总进度", "负责人", "所属部门",
                "任务名称", "任务描述", "负责人", "状态", "进度", "开始时间", "结束时间", "预计工时", "实际工时");

        // 导出Excel - 需要确保 exportUtil 已注入
        if (exportUtil != null) {
            return exportUtil.exportToExcel(headers, dataList, "项目进度数据");
        } else {
            // 如果没有 exportUtil，返回空数组或抛出异常
            throw new RuntimeException("导出工具未初始化");
        }
    }

    @Override
    public Map<String, Object> getProjectStatistics() {
        Object result = projectRepository.getProjectStatistics();
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        // 如果结果不是Map，返回默认值
        Map<String, Object> defaultStats = new HashMap<>();
        defaultStats.put("totalProjects", 0L);
        defaultStats.put("avgProgress", 0.0);
        return defaultStats;
    }

    @Override
    public Project createProjectFromDTO(ProjectDTO projectDTO) {
        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setStartTime(projectDTO.getStartTime());
        project.setEndTime(projectDTO.getEndTime());
        project.setOriginalEndTime(projectDTO.getEndTime());

        if (projectDTO.getStatus() != null) {
            project.setStatus(Project.ProjectStatus.fromValue(projectDTO.getStatus()));
        }

        if (projectDTO.getTotalProgress() != null) {
            project.setTotalProgress(projectDTO.getTotalProgress());
        } else {
            project.setTotalProgress(0);
        }

        project.setCreatedTime(LocalDateTime.now());
        project.setUpdatedTime(LocalDateTime.now());

        // 设置项目经理
        if (projectDTO.getManagerId() != null) {
            User manager = userRepository.findById(projectDTO.getManagerId())
                    .orElseThrow(() -> new RuntimeException("项目经理不存在: " + projectDTO.getManagerId()));
            project.setManager(manager);
        }

        // 设置部门
        if (projectDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(projectDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("部门不存在: " + projectDTO.getDepartmentId()));
            project.setDepartment(department);
        }

        // 保存项目以获取ID
        project = projectRepository.save(project);

        // 设置团队成员
        setTeamMembers(project, projectDTO.getTeamMemberIds());

        return projectRepository.save(project);
    }

    // 修改updateProjectFromDTO方法
    @Override
    public Project updateProjectFromDTO(ProjectDTO projectDTO) {
        // 获取现有项目
        Project project = projectRepository.findById(projectDTO.getId())
                .orElseThrow(() -> new RuntimeException("项目不存在: " + projectDTO.getId()));

        // 更新基本信息
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setStartTime(projectDTO.getStartTime());
        project.setEndTime(projectDTO.getEndTime());

        if (projectDTO.getStatus() != null) {
            project.setStatus(Project.ProjectStatus.fromValue(projectDTO.getStatus()));
        }

        if (projectDTO.getTotalProgress() != null) {
            project.setTotalProgress(projectDTO.getTotalProgress());
        }

        project.setUpdatedTime(LocalDateTime.now());

        // 更新项目经理
        if (projectDTO.getManagerId() != null) {
            User manager = userRepository.findById(projectDTO.getManagerId())
                    .orElseThrow(() -> new RuntimeException("项目经理不存在: " + projectDTO.getManagerId()));
            project.setManager(manager);
        }

        // 更新部门
        if (projectDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(projectDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("部门不存在: " + projectDTO.getDepartmentId()));
            project.setDepartment(department);
        }

        // 只有当DTO中明确提供了teamMemberIds时才更新团队成员
        if (projectDTO.getTeamMemberIds() != null) {
            setTeamMembers(project, projectDTO.getTeamMemberIds());
        }

        // return projectRepository.save(project);
        project = projectRepository.save(project);

        // 重新加载项目，确保返回一个干净的实例，避免序列化问题
        return projectRepository.findById(project.getId())
                .orElseThrow(() -> new RuntimeException("项目更新后无法重新加载"));
    }

    // 修改setTeamMembers方法
    private void setTeamMembers(Project project, List<String> teamMemberIds) {
        // 确保集合不为null
        if (project.getTeamMembers() == null) {
            project.setTeamMembers(new ArrayList<>());
        }

        // 清空现有成员
        project.getTeamMembers().clear();

        // 只在有新成员ID时添加
        if (teamMemberIds != null && !teamMemberIds.isEmpty()) {
            List<User> members = userRepository.findAllById(teamMemberIds);
            if (!members.isEmpty()) {
                project.getTeamMembers().addAll(members);
            }
        }
        // 不要在这里保存，让调用方法统一处理
    }
}