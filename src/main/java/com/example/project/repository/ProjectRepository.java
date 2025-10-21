package com.example.project.repository;

import com.example.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String>, JpaSpecificationExecutor<Project> {
    // 根据名称模糊查询项目
    List<Project> findByNameContaining(String name);

    // 根据状态查询项目
    List<Project> findByStatus(Project.ProjectStatus status);

    // 根据部门ID查询项目
    List<Project> findByDepartmentId(String departmentId);

    // 根据负责人ID查询项目
    List<Project> findByManagerId(String managerId);

    // 查询指定时间范围内开始的项目
    List<Project> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // 查询指定时间范围内结束的项目
    List<Project> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);

    // 查询进度低于指定值的项目
    List<Project> findByTotalProgressLessThan(Integer progress);

    // 查询进度高于指定值的项目
    List<Project> findByTotalProgressGreaterThan(Integer progress);

    // 自定义查询：获取项目总数和平均进度
    @Query("SELECT new map(COUNT(p) as totalProjects, AVG(p.totalProgress) as avgProgress) FROM Project p")
    Object getProjectStatistics();

    // 按状态分组统计项目数量
    @Query("SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> countProjectsByStatus();

    // 计算按时完成率：已完成且结束时间不晚于计划结束时间的项目比例
    @Query("SELECT COUNT(p) as totalCompletedProjects, " +
            "SUM(CASE WHEN p.endTime <= p.originalEndTime THEN 1 ELSE 0 END) as onTimeCompletedProjects " +
            "FROM Project p " +
            "WHERE p.status = 'COMPLETED'")
    Object[] calculateOnTimeCompletionRate();

    @Query("SELECT p FROM Project p WHERE p.endTime < :now AND p.status != 'COMPLETED'")
    List<Project> findDelayedProjects(@Param("now") LocalDateTime now);
}