package com.example.project.service;

import com.example.project.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface TaskService {

    Task createTask(Task task);

    Task updateTask(Task task);

    void deleteTask(String id);

    Task getTaskById(String id);

    List<Task> getAllTasks();

    Page<Task> findTasksByPage(Pageable pageable);

    List<Task> findTasksByProjectId(String projectId);

    List<Task> findTasksByAssigneeId(String assigneeId);

    List<Task> findTasksByStatus(Task.TaskStatus status);

    List<Task> findTasksByProgressBetween(Integer min, Integer max);

    List<Task> findOverdueTasks();

    List<Task> findTopLevelTasks(String projectId);

    List<Task> findSubTasks(String parentTaskId);

    Task updateTaskProgress(String taskId, Integer progress, String changedBy, String reason);

    Task updateTaskStatus(String taskId, Task.TaskStatus status);

    Map<Task.TaskStatus, String> countTasksByStatus(String projectId);

    Double calculateAverageTaskProgress(String projectId);

    List<Task> getUpcomingTasks(String projectId, Integer days);

    Integer calculateTaskCompletionRate(String projectId);

    List<Map<String, Object>> getGanttChartData(String projectId);

    byte[] exportTasksData(String projectId);
}