package com.example.project.util;

import com.example.project.entity.Department;
import com.example.project.entity.Project;
import com.example.project.entity.Project.ProjectStatus;
import com.example.project.entity.Task;
import com.example.project.entity.Task.TaskStatus;
import com.example.project.entity.User;
import com.example.project.entity.User.UserRole;
import com.example.project.dto.UserFilter;
import com.example.project.service.ProjectService;
import com.example.project.service.TaskService;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试数据生成器，用于向系统添加测试数据
 */
@Component
public class TestDataGenerator {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 生成所有测试数据
     */
    public void generateAllTestData() {
        // 测试数据生成代码已注释掉
        // generateDepartments();
        // generateUsers();
        // generateProjects();
        // generateTasks();
        System.out.println("测试数据生成已禁用");
    }

    /**
     * 生成部门测试数据
     */
    private void generateDepartments() {
        // 测试数据生成代码已注释掉
        // 由于没有DepartmentService，暂时不保存部门数据
        // 部门数据将在generateUsers方法中直接创建
    }

    /**
     * 生成用户测试数据
     */
    private void generateUsers() {
        // 测试数据生成代码已注释掉
        /*
        // 先检查用户是否已存在，如果不存在则创建
        UserFilter filter = new UserFilter();
        filter.setUsername("manager1");
        if (userService.findUsersByFilter(filter).isEmpty()) {
            // 创建项目经理
            User manager = new User();
            manager.setUsername("manager1");
            manager.setName("项目经理");
            manager.setPassword(passwordEncoder.encode("password123"));
            manager.setEmail("manager@example.com");
            manager.setPhone("13800138002");
            manager.setRole(UserRole.MANAGER);
            manager.setAvatar("https://example.com/avatars/manager1.png"); // 添加默认头像
            userService.createUser(manager);
        }

        filter.setUsername("developer1");
        if (userService.findUsersByFilter(filter).isEmpty()) {
            // 创建普通用户1 (MANAGER角色)
            User developer1 = new User();
            developer1.setUsername("developer1");
            developer1.setName("普通用户1");
            developer1.setPassword(passwordEncoder.encode("password123"));
            developer1.setEmail("developer1@example.com");
            developer1.setPhone("13800138003");
            developer1.setRole(UserRole.MANAGER);
            developer1.setAvatar("https://example.com/avatars/developer1.png"); // 添加默认头像
            userService.createUser(developer1);
        }
        */
    }

    /**
     * 生成项目测试数据
     */
    private void generateProjects() {
        // 测试数据生成代码已注释掉
        /*
        // 查找项目经理
        UserFilter filter = new UserFilter();
        filter.setUsername("manager1");
        List<User> managers = userService.findUsersByFilter(filter);
        if (managers.isEmpty()) {
            return; // 如果项目经理不存在，则不创建项目
        }
        User manager = managers.get(0);

        // 检查项目是否已存在，不存在则创建
        List<Project> existingProjects = projectService.getAllProjects();
        boolean project1Exists = existingProjects.stream().anyMatch(p -> "电商网站开发".equals(p.getName()));
        boolean project2Exists = existingProjects.stream().anyMatch(p -> "企业内部管理系统".equals(p.getName()));
        boolean project3Exists = existingProjects.stream().anyMatch(p -> "移动应用开发".equals(p.getName()));
        boolean project4Exists = existingProjects.stream().anyMatch(p -> "公司官网升级".equals(p.getName()));

        if (!project1Exists) {
            // 创建项目1
            Project project1 = new Project();
            project1.setName("电商网站开发");
            project1.setDescription("开发一个完整的电商网站系统");
            project1.setStartTime(LocalDateTime.now().minusMonths(2));
            project1.setEndTime(LocalDateTime.now().plusMonths(1));
            project1.setStatus(ProjectStatus.IN_PROGRESS);
            project1.setTotalProgress(60);
            project1.setManager(manager);
            projectService.createProject(project1);
        }

        if (!project2Exists) {
            // 创建项目2
            Project project2 = new Project();
            project2.setName("企业内部管理系统");
            project2.setDescription("开发企业内部管理系统");
            project2.setStartTime(LocalDateTime.now().minusMonths(1));
            project2.setEndTime(LocalDateTime.now().plusMonths(2));
            project2.setStatus(ProjectStatus.IN_PROGRESS);
            project2.setTotalProgress(30);
            project2.setManager(manager);
            projectService.createProject(project2);
        }

        if (!project3Exists) {
            // 创建项目3
            Project project3 = new Project();
            project3.setName("移动应用开发");
            project3.setDescription("开发一个移动应用");
            project3.setStartTime(LocalDateTime.now());
            project3.setEndTime(LocalDateTime.now().plusMonths(3));
            project3.setStatus(ProjectStatus.NOT_STARTED);
            project3.setTotalProgress(0);
            project3.setManager(manager);
            projectService.createProject(project3);
        }

        if (!project4Exists) {
            // 创建已完成的项目
            Project project4 = new Project();
            project4.setName("公司官网升级");
            project4.setDescription("升级公司官方网站");
            project4.setStartTime(LocalDateTime.now().minusMonths(4));
            project4.setEndTime(LocalDateTime.now().minusMonths(1));
            project4.setStatus(ProjectStatus.COMPLETED);
            project4.setTotalProgress(100);
            project4.setManager(manager);
            projectService.createProject(project4);
        }
        */
    }

    /**
     * 生成任务测试数据
     */
    private void generateTasks() {
        // 测试数据生成代码已注释掉
        /*
        // 查找用户
        UserFilter filter = new UserFilter();
        filter.setUsername("developer1");
        List<User> developers1 = userService.findUsersByFilter(filter);
        if (developers1.isEmpty()) {
            return; // 如果必要的用户不存在，则不创建任务
        }
        User developer1 = developers1.get(0);
        
        // 只使用存在的用户创建任务
        if (developer1 == null) {
            return; // 如果必要的用户不存在，则不创建任务
        }

        // 查找项目
        List<Project> projects = projectService.getAllProjects();
        Project project1 = projects.stream().filter(p -> p.getName().equals("电商网站开发")).findFirst().orElse(null);
        Project project2 = projects.stream().filter(p -> p.getName().equals("企业内部管理系统")).findFirst().orElse(null);
        Project project3 = projects.stream().filter(p -> p.getName().equals("移动应用开发")).findFirst().orElse(null);

        // 检查任务是否已存在
        List<Task> existingTasks = taskService.getAllTasks();

        // 为项目1创建任务
        if (project1 != null) {
            // 前端开发任务
            if (existingTasks.stream()
                    .noneMatch(t -> "前端页面开发".equals(t.getName()) && t.getProject().equals(project1))) {
                Task frontendTask = new Task();
                frontendTask.setName("前端页面开发");
                frontendTask.setDescription("开发电商网站的前端页面");
                frontendTask.setProject(project1);
                frontendTask.setAssignee(developer1);
                frontendTask.setStatus(TaskStatus.IN_PROGRESS);
                frontendTask.setProgress(70);
                frontendTask.setStartTime(project1.getStartTime());
                frontendTask.setEndTime(project1.getEndTime().minusWeeks(2));
                frontendTask.setEstimatedHours(200);
                frontendTask.setActualHours(140);
                taskService.createTask(frontendTask);
            }

            // 后端开发任务
            if (existingTasks.stream()
                    .noneMatch(t -> "后端接口开发".equals(t.getName()) && t.getProject().equals(project1))) {
                Task backendTask = new Task();
                backendTask.setName("后端接口开发");
                backendTask.setDescription("开发电商网站的后端API接口");
                backendTask.setProject(project1);
                backendTask.setAssignee(developer1);
                backendTask.setStatus(TaskStatus.IN_PROGRESS);
                backendTask.setProgress(60);
                backendTask.setStartTime(project1.getStartTime());
                backendTask.setEndTime(project1.getEndTime().minusWeeks(3));
                backendTask.setEstimatedHours(250);
                backendTask.setActualHours(150);
                taskService.createTask(backendTask);
            }
        }

        // 为项目2创建任务
        if (project2 != null) {
            if (existingTasks.stream()
                    .noneMatch(t -> "需求分析与规划".equals(t.getName()) && t.getProject().equals(project2))) {
                Task planningTask = new Task();
                planningTask.setName("需求分析与规划");
                planningTask.setDescription("分析企业内部管理系统的需求并制定计划");
                planningTask.setProject(project2);
                planningTask.setAssignee(developer1);
                planningTask.setStatus(TaskStatus.COMPLETED);
                planningTask.setProgress(100);
                planningTask.setStartTime(project2.getStartTime());
                planningTask.setEndTime(project2.getStartTime().plusWeeks(2));
                planningTask.setEstimatedHours(60);
                planningTask.setActualHours(60);
                taskService.createTask(planningTask);
            }

            if (existingTasks.stream()
                    .noneMatch(t -> "核心功能开发".equals(t.getName()) && t.getProject().equals(project2))) {
                Task devTask = new Task();
                devTask.setName("核心功能开发");
                devTask.setDescription("开发企业内部管理系统的核心功能");
                devTask.setProject(project2);
                devTask.setAssignee(developer1);
                devTask.setStatus(TaskStatus.IN_PROGRESS);
                devTask.setProgress(40);
                devTask.setStartTime(project2.getStartTime().plusWeeks(2));
                devTask.setEndTime(project2.getEndTime().minusWeeks(2));
                devTask.setEstimatedHours(300);
                devTask.setActualHours(120);
                taskService.createTask(devTask);
            }
        }

        // 为项目3创建任务
        if (project3 != null) {
            if (existingTasks.stream().noneMatch(t -> "技术调研".equals(t.getName()) && t.getProject().equals(project3))) {
                Task researchTask = new Task();
                researchTask.setName("技术调研");
                researchTask.setDescription("调研移动应用开发的相关技术");
                researchTask.setProject(project3);
                researchTask.setAssignee(developer1);
                researchTask.setStatus(TaskStatus.IN_PROGRESS);
                researchTask.setProgress(30);
                researchTask.setStartTime(project3.getStartTime());
                researchTask.setEndTime(project3.getStartTime().plusWeeks(2));
                researchTask.setEstimatedHours(40);
                researchTask.setActualHours(12);
                taskService.createTask(researchTask);
            }
        }
        */
    }

    /**
     * 主方法，用于直接运行生成测试数据
     */
    public static void main(String[] args) {
        // 这个方法在实际Spring Boot应用启动时会通过Spring容器调用
        System.out.println("测试数据生成器已创建，请通过Spring Boot应用调用generateAllTestData()方法生成测试数据。");
    }
}