package com.example.project.service.impl;

import com.example.project.entity.ProgressLog;
import com.example.project.entity.Task;
import com.example.project.repository.ProgressLogRepository;
import com.example.project.repository.TaskRepository;
import com.example.project.service.ProjectService;
import com.example.project.service.TaskService;
import com.example.project.util.DateUtil;
import com.example.project.util.ExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProgressLogRepository progressLogRepository;

    @Autowired
    private ProjectService projectService;

    @Override
    public Task createTask(Task task) {
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());
        task.setProgress(0);
        task.setStatus(Task.TaskStatus.NOT_STARTED);
        Task savedTask = taskRepository.save(task);

        // 更新项目进度
        projectService.updateProjectProgress(savedTask.getProject().getId());

        return savedTask;
    }

    @Override
    public Task updateTask(Task task) {
        task.setUpdatedTime(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);

        // 更新项目进度
        projectService.updateProjectProgress(updatedTask.getProject().getId());

        return updatedTask;
    }

    @Override
    public void deleteTask(String id) {
        Task task = getTaskById(id);
        String projectId = task.getProject().getId();
        taskRepository.deleteById(id);

        // 更新项目进度
        projectService.updateProjectProgress(projectId);
    }

    @Override
    public Task getTaskById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到ID为" + id + "的任务"));
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Page<Task> findTasksByPage(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    @Override
    public List<Task> findTasksByProjectId(String projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @Override
    public List<Task> findTasksByAssigneeId(String assigneeId) {
        return taskRepository.findByAssigneeId(assigneeId);
    }

    @Override
    public List<Task> findTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    @Override
    public List<Task> findTasksByProgressBetween(Integer min, Integer max) {
        return taskRepository.findByProgressBetween(min, max);
    }

    @Override
    public List<Task> findOverdueTasks() {
        return taskRepository.findByEndTimeBefore(LocalDateTime.now());
    }

    @Override
    public List<Task> findTopLevelTasks(String projectId) {
        List<Task> topLevelTasks = taskRepository.findByParentTaskIsNull();
        return topLevelTasks.stream()
                .filter(task -> task.getProject().getId().equals(projectId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findSubTasks(String parentTaskId) {
        return taskRepository.findByParentTaskId(parentTaskId);
    }

    @Override
    public Task updateTaskProgress(String taskId, Integer progress, String changedBy, String reason) {
        Task task = getTaskById(taskId);
        Integer oldProgress = task.getProgress();

        // 更新任务进度
        task.setProgress(progress);
        task.setUpdatedTime(LocalDateTime.now());
        task.setUpdatedBy(changedBy);

        // 根据进度更新任务状态
        if (progress == 100) {
            task.setStatus(Task.TaskStatus.COMPLETED);
        } else if (progress > 0 && task.getStatus() == Task.TaskStatus.NOT_STARTED) {
            task.setStatus(Task.TaskStatus.IN_PROGRESS);
        }

        Task updatedTask = taskRepository.save(task);

        // 记录进度变更日志
        if (!Objects.equals(oldProgress, progress)) {
            ProgressLog log = new ProgressLog();
            log.setTask(updatedTask);
            log.setOldProgress(oldProgress);
            log.setNewProgress(progress);
            log.setChangedBy(changedBy);
            log.setChangeReason(reason);
            progressLogRepository.save(log);
        }

        // 更新项目进度
        projectService.updateProjectProgress(updatedTask.getProject().getId());

        return updatedTask;
    }

    @Override
    public Task updateTaskStatus(String taskId, Task.TaskStatus status) {
        Task task = getTaskById(taskId);
        task.setStatus(status);
        task.setUpdatedTime(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);

        // 如果状态变为完成，更新进度为100
        if (status == Task.TaskStatus.COMPLETED && task.getProgress() < 100) {
            updatedTask.setProgress(100);
            updatedTask = taskRepository.save(updatedTask);
        }

        // 更新项目进度
        projectService.updateProjectProgress(updatedTask.getProject().getId());

        return updatedTask;
    }

    @Override
    public Map<Task.TaskStatus, String> countTasksByStatus(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .collect(Collectors.groupingBy(
                        Task::getStatus,
                        Collectors.collectingAndThen(Collectors.counting(), String::valueOf)));
    }

    @Override
    public Double calculateAverageTaskProgress(String projectId) {
        return taskRepository.calculateAverageProgressByProject(projectId);
    }

    @Override
    public List<Task> getUpcomingTasks(String projectId, Integer days) {
        return taskRepository.findUpcomingTasksInProject(projectId, days);
    }

    @Override
    public Integer calculateTaskCompletionRate(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (tasks.isEmpty()) {
            return 0;
        }
        long completedTasks = tasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
                .count();
        return (int) Math.round(((double) completedTasks / tasks.size()) * 100);
    }

    @Override
    public List<Map<String, Object>> getGanttChartData(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream().map(task -> {
            Map<String, Object> ganttData = new HashMap<>();
            ganttData.put("id", task.getId());
            ganttData.put("name", task.getName());
            ganttData.put("start", task.getStartTime());
            ganttData.put("end", task.getEndTime());
            ganttData.put("progress", task.getProgress());
            ganttData.put("parent", task.getParentTask() != null ? task.getParentTask().getId() : null);
            ganttData.put("status", task.getStatus());
            return ganttData;
        }).collect(Collectors.toList());
    }

    @Autowired
    private ExportUtil exportUtil;

    @Override
    public byte[] exportTasksData(String projectId) {
        List<Task> tasks = findTasksByProjectId(projectId);

        // 准备导出数据
        List<Map<String, Object>> dataList = new ArrayList<>();

        // 添加任务数据
        for (Task task : tasks) {
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("任务名称", task.getName());
            taskData.put("任务描述", task.getDescription());
            taskData.put("所属项目", task.getProject().getName());
            taskData.put("负责人", task.getAssignee() != null ? task.getAssignee().getName() : "");
            taskData.put("状态", task.getStatus().name());
            taskData.put("进度", task.getProgress() + "%");
            taskData.put("开始时间",
                    task.getStartTime() != null
                            ? DateUtil.formatLocalDateTime(task.getStartTime(), DateUtil.DATE_TIME_FORMAT)
                            : "");
            taskData.put("结束时间",
                    task.getEndTime() != null
                            ? DateUtil.formatLocalDateTime(task.getEndTime(), DateUtil.DATE_TIME_FORMAT)
                            : "");
            taskData.put("预计工时", task.getEstimatedHours() != null ? task.getEstimatedHours() : "");
            taskData.put("实际工时", task.getActualHours() != null ? task.getActualHours() : "");
            taskData.put("父任务", task.getParentTask() != null ? task.getParentTask().getName() : "");
            taskData.put("创建时间", DateUtil.formatLocalDateTime(task.getCreatedTime(), DateUtil.DATE_TIME_FORMAT));
            taskData.put("更新时间", DateUtil.formatLocalDateTime(task.getUpdatedTime(), DateUtil.DATE_TIME_FORMAT));
            dataList.add(taskData);
        }

        // 准备表头
        List<String> headers = Arrays.asList("任务名称", "任务描述", "所属项目", "负责人", "状态", "进度",
                "开始时间", "结束时间", "预计工时", "实际工时", "父任务",
                "创建时间", "更新时间");

        // 导出Excel
        return exportUtil.exportToExcel(headers, dataList, "项目任务数据");
    }
}