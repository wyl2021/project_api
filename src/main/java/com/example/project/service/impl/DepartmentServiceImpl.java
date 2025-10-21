package com.example.project.service.impl;

import com.example.project.entity.Department;
import com.example.project.entity.User;
import com.example.project.repository.DepartmentRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.DepartmentService;

import org.springframework.util.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    // 添加缺失的分页查询方法
    @Override
    public Page<Department> findDepartmentsByPage(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }

    // 您现有的其他方法...
    @Override
    public Department createDepartment(Department department, String createdBy) {
        // 设置创建信息
        department.setCreatedBy(createdBy);
        department.setCreatedTime(LocalDateTime.now());
        department.setUpdatedBy(createdBy);
        department.setUpdatedTime(LocalDateTime.now());

        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(String id, Department department, String updatedBy) {
        // 检查部门是否存在
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在，ID: " + id));

        // 更新部门信息
        existingDepartment.setName(department.getName());
        existingDepartment.setDescription(department.getDescription());
        // 添加managerId更新
        if (department.getManagerId() != null) {
            // 验证managerId对应的用户是否存在且活跃
            User manager = userRepository.findById(department.getManagerId())
                    .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + department.getManagerId()));

            if (manager.getIsActive() == null || !manager.getIsActive()) {
                throw new RuntimeException("用户不活跃，无法设置为部门管理员，ID: " + department.getManagerId());
            }

            existingDepartment.setManagerId(department.getManagerId());
        }
        existingDepartment.setUpdatedBy(updatedBy);
        existingDepartment.setUpdatedTime(LocalDateTime.now());

        return departmentRepository.save(existingDepartment);
    }

    @Override
    public Department setDepartmentManager(String departmentId, String managerId, String updatedBy) {
        // 检查部门是否存在
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("部门不存在，ID: " + departmentId));
    
        // 检查管理员是否存在
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + managerId));
    
        // 检查用户是否活跃
        if (manager.getIsActive() == null || !manager.getIsActive()) {
            throw new RuntimeException("用户不活跃，无法设置为部门管理员，ID: " + managerId);
        }
    
        // 设置部门管理员
        department.setManagerId(managerId);
        
        // 自动将部门经理添加为部门成员（如果尚未添加）
        if (manager.getDepartment() == null || !manager.getDepartment().getId().equals(departmentId)) {
            manager.setDepartment(department);
            userRepository.save(manager);
        }
        
        department.setUpdatedBy(updatedBy);
        department.setUpdatedTime(LocalDateTime.now());
    
        return departmentRepository.save(department);
    }

    @Override
    public Department removeDepartmentManager(String departmentId, String updatedBy) {
        // 检查部门是否存在
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("部门不存在，ID: " + departmentId));

        // 移除部门管理员
        department.setManagerId(null);
        department.setUpdatedBy(updatedBy);
        department.setUpdatedTime(LocalDateTime.now());

        return departmentRepository.save(department);
    }

    @Override
    public void deleteDepartment(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteDepartment'");
    }

    @Override
    public Department getDepartmentById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDepartmentById'");
    }

    @Override
    public List<Department> getAllDepartments() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllDepartments'");
    }

    @Override
    public Department findDepartmentByName(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findDepartmentByName'");
    }

    @Override
    public boolean existsById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'existsById'");
    }

    @Override
    public Long getEmployeeCountByDepartmentId(String departmentId) {
        if (departmentId == null || departmentId.trim().isEmpty()) {
            throw new RuntimeException("部门ID不能为空");
        }
        // 使用 UserRepository 统计该部门的员工数量
        return userRepository.countActiveUsersByDepartmentId(departmentId);
    }

    @Override
    public int getProjectCountByDepartmentId(String departmentId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProjectCountByDepartmentId'");
    }

    // 您可能还需要实现其他缺失的接口方法
    // 例如：
    // @Override
    // public Department getDepartmentById(String id) { ... }
    //
    // @Override
    // public List<Department> getAllDepartments() { ... }
    //
    // @Override
    // public void deleteDepartment(String id) { ... }
}