package com.example.project.repository;

import com.example.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String>, JpaSpecificationExecutor<Task> {
    // 根据项目ID查询任务
    List<Task> findByProjectId(String projectId);

    // 根据负责人ID查询任务
    List<Task> findByAssigneeId(String assigneeId);

    // 根据状态查询任务
    List<Task> findByStatus(Task.TaskStatus status);

    // 查询进度在指定范围内的任务
    List<Task> findByProgressBetween(Integer min, Integer max);

    // 查询截止日期在指定时间之前的任务
    List<Task> findByEndTimeBefore(LocalDateTime endTime);

    // 查询父任务ID为null的任务（即顶级任务）
    List<Task> findByParentTaskIsNull();

    // 根据父任务ID查询子任务
    List<Task> findByParentTaskId(String parentTaskId);

    // 自定义查询：计算项目的平均任务进度
    @Query("SELECT AVG(t.progress) FROM Task t WHERE t.project.id = :projectId")
    Double calculateAverageProgressByProject(String projectId);

    // 自定义查询：统计项目中各状态的任务数量
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> countTasksByStatusInProject(String projectId);

    // 自定义查询：查询项目中即将到期的任务
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.endTime BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + :days * 24 * 60 * 60 * 1000")
    List<Task> findUpcomingTasksInProject(String projectId, Integer days);
}