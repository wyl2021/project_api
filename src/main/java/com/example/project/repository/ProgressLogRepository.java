package com.example.project.repository;

import com.example.project.entity.ProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressLogRepository extends JpaRepository<ProgressLog, Long> {
    // 根据任务ID查询进度日志
    List<ProgressLog> findByTaskId(Long taskId);

    // 根据更改人ID查询进度日志
    List<ProgressLog> findByChangedBy(Long changedBy);

    // 根据任务ID和时间倒序查询最新的10条日志
    List<ProgressLog> findTop10ByTaskIdOrderByChangedTimeDesc(Long taskId);
}