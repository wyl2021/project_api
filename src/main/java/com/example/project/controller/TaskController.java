package com.example.project.controller;

import com.example.project.entity.Task;
import com.example.project.service.TaskService;
import com.example.project.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // 创建任务
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return new ResponseEntity<>(new ApiResponse(true, "任务创建成功", createdTask), HttpStatus.CREATED);
    }

    // 获取所有任务
    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return new ResponseEntity<>(new ApiResponse(true, "任务获取成功", tasks), HttpStatus.OK);
    }

    // 分页获取任务
    @GetMapping("/page")
    public ResponseEntity<Page<Task>> getTasksByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskService.findTasksByPage(pageable);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // 根据ID获取任务
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        Task task = taskService.getTaskById(id);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    // 更新任务
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable String id, @RequestBody Task task) {
        task.setId(id);
        Task updatedTask = taskService.updateTask(task);
        return new ResponseEntity<>(new ApiResponse(true, "任务更新成功", updatedTask), HttpStatus.OK);
    }

    // 删除任务
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return new ResponseEntity<>(new ApiResponse(true, "任务删除成功", null), HttpStatus.OK);
    }

    // 根据项目ID获取任务
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTasksByProject(@PathVariable String projectId) {
        List<Task> tasks = taskService.findTasksByProjectId(projectId);
        return new ResponseEntity<>(new ApiResponse(true, "任务获取成功", tasks), HttpStatus.OK);
    }

    // 根据负责人ID获取任务
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<Task>> getTasksByAssignee(@PathVariable String assigneeId) {
        List<Task> tasks = taskService.findTasksByAssigneeId(assigneeId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // 根据状态获取任务
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        List<Task> tasks = taskService.findTasksByStatus(status);
        return new ResponseEntity<>(new ApiResponse(true, "任务获取成功", tasks), HttpStatus.OK);
    }

    // 获取逾期任务
    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        List<Task> tasks = taskService.findOverdueTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // 获取项目的顶级任务
    @GetMapping("/project/{projectId}/top-level")
    public ResponseEntity<List<Task>> getTopLevelTasks(@PathVariable String projectId) {
        List<Task> tasks = taskService.findTopLevelTasks(projectId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // 获取子任务
    @GetMapping("/{id}/sub-tasks")
    public ResponseEntity<List<Task>> getSubTasks(@PathVariable String id) {
        List<Task> tasks = taskService.findSubTasks(id);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // 更新任务进度
    @PostMapping("/{id}/update-progress")
    public ResponseEntity<?> updateTaskProgress(
            @PathVariable String id,
            @RequestParam Integer progress,
            @RequestParam String changedBy,
            @RequestParam(required = false) String reason) {
        // 验证进度值在0-100之间
        if (progress == null || progress < 0 || progress > 100) {
            return new ResponseEntity<>(new ApiResponse(false, "进度值必须在0-100之间", null), HttpStatus.BAD_REQUEST);
        }

        try {
            Task updatedTask = taskService.updateTaskProgress(id, progress, changedBy, reason);
            return new ResponseEntity<>(new ApiResponse(true, "任务进度更新成功", updatedTask), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, "任务进度更新失败：" + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新任务状态
    @PostMapping("/{id}/update-status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable String id,
            @RequestParam Task.TaskStatus status) {
        Task updatedTask = taskService.updateTaskStatus(id, status);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    // 获取项目中各状态任务数量统计
    @GetMapping("/project/{projectId}/status-count")
    public ResponseEntity<Map<Task.TaskStatus, String>> getTaskStatusCount(@PathVariable String projectId) {
        Map<Task.TaskStatus, String> countMap = taskService.countTasksByStatus(projectId);
        return new ResponseEntity<>(countMap, HttpStatus.OK);
    }

    // 获取项目平均任务进度
    @GetMapping("/project/{projectId}/average-progress")
    public ResponseEntity<Double> getAverageTaskProgress(@PathVariable String projectId) {
        Double avgProgress = taskService.calculateAverageTaskProgress(projectId);
        return new ResponseEntity<>(avgProgress, HttpStatus.OK);
    }

    // 获取即将到期的任务
    @GetMapping("/project/{projectId}/upcoming")
    public ResponseEntity<List<Task>> getUpcomingTasks(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "7") Integer days) {
        List<Task> tasks = taskService.getUpcomingTasks(projectId, days);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // 获取项目任务完成率
    @GetMapping("/project/{projectId}/completion-rate")
    public ResponseEntity<Integer> getTaskCompletionRate(@PathVariable String projectId) {
        Integer completionRate = taskService.calculateTaskCompletionRate(projectId);
        return new ResponseEntity<>(completionRate, HttpStatus.OK);
    }

    // 获取甘特图数据
    @GetMapping("/project/{projectId}/gantt-data")
    public ResponseEntity<List<Map<String, Object>>> getGanttChartData(@PathVariable String projectId) {
        List<Map<String, Object>> ganttData = taskService.getGanttChartData(projectId);
        return new ResponseEntity<>(ganttData, HttpStatus.OK);
    }

    // 导出任务数据
    @GetMapping("/project/{projectId}/export")
    public ResponseEntity<byte[]> exportTasksData(@PathVariable String projectId) {
        byte[] data = taskService.exportTasksData(projectId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=tasks_project_" + projectId + ".xlsx")
                .body(data);
    }
}