package com.example.project.service;

import com.example.project.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DepartmentService {
    // 创建部门
    Department createDepartment(Department department, String createdBy);

    // 更新部门
    // Department updateDepartment(String id, Department department, String
    // updatedBy);

    // 删除部门
    void deleteDepartment(String id);

    // 根据ID获取部门
    Department getDepartmentById(String id);

    // 获取所有部门
    List<Department> getAllDepartments();

    // 分页查询部门
    Page<Department> findDepartmentsByPage(Pageable pageable);

    // 根据名称查询部门
    Department findDepartmentByName(String name);

    // 检查部门是否存在
    boolean existsById(String id);

    // 获取部门员工数量
    Long getEmployeeCountByDepartmentId(String departmentId);

    // 获取部门项目数量
    int getProjectCountByDepartmentId(String departmentId);

    // 设置部门管理员

    // 修改后
    // Department setDepartmentManager(String departmentId, String managerId, String
    // updatedBy);

    // 移除部门管理员
    // Department removeDepartmentManager(String departmentId, String updatedBy);

    Department removeDepartmentManager(String departmentId, String updatedBy);

    Department setDepartmentManager(String departmentId, String managerId, String updatedBy);

    Department updateDepartment(String id, Department department, String updatedBy);

    // int getEmployeeCountByDepartmentId(String departmentId);
}