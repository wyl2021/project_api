package com.example.project.controller;

import com.example.project.entity.Department;
import com.example.project.entity.User;
import com.example.project.service.DepartmentService;
import com.example.project.service.UserService;
import com.example.project.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserService userService;

    // 创建部门
    @PostMapping
    public ResponseEntity<?> createDepartment(
            @RequestBody Department department,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证请求数据
            if (department.getName() == null || department.getName().trim().isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "部门名称不能为空", null),
                        HttpStatus.BAD_REQUEST);
            }

            // 验证用户身份
            User currentUser = validateUserIdentity(token);
            if (currentUser == null) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "未授权访问", null),
                        HttpStatus.UNAUTHORIZED);
            }

            // 检查权限 - 只有ADMIN可以创建部门
            if (!User.UserRole.ADMIN.equals(currentUser.getRole())) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "权限不足，只有管理员可以创建部门", null),
                        HttpStatus.FORBIDDEN);
            }

            // 创建部门
            Department createdDepartment = departmentService.createDepartment(department,
                    currentUser.getId().toString());
            return new ResponseEntity<>(
                    new ApiResponse(true, "部门创建成功", createdDepartment),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "创建部门失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 获取部门列表
    @GetMapping
    public ResponseEntity<?> getDepartmentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());
            Page<Department> departments = departmentService.findDepartmentsByPage(pageable);

            // 处理部门列表，添加manager name和部门人数
            List<Map<String, Object>> departmentListWithExtraInfo = new ArrayList<>();
            for (Department dept : departments.getContent()) {
                Map<String, Object> deptMap = new HashMap<>();
                // 添加部门基本信息
                deptMap.put("id", dept.getId());
                deptMap.put("name", dept.getName());
                deptMap.put("description", dept.getDescription());
                deptMap.put("managerId", dept.getManagerId());
                deptMap.put("createdBy", dept.getCreatedBy());
                deptMap.put("createdTime", dept.getCreatedTime());
                deptMap.put("updatedBy", dept.getUpdatedBy());
                deptMap.put("updatedTime", dept.getUpdatedTime());

                // 添加部门经理姓名
                if (dept.getManagerId() != null) {
                    try {
                        User manager = userService.getUserById(dept.getManagerId());
                        if (manager != null) {
                            deptMap.put("managerName", manager.getName());
                        }
                    } catch (Exception e) {
                        deptMap.put("managerName", null);
                    }
                } else {
                    deptMap.put("managerName", null);
                }

                // 添加部门人数
                Long employeeCount = departmentService.getEmployeeCountByDepartmentId(dept.getId().toString());
                deptMap.put("employeeCount", employeeCount);

                departmentListWithExtraInfo.add(deptMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("departments", departmentListWithExtraInfo);
            response.put("currentPage", departments.getNumber());
            response.put("totalItems", departments.getTotalElements());
            response.put("totalPages", departments.getTotalPages());

            return new ResponseEntity<>(
                    new ApiResponse(true, "部门列表获取成功", response),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "获取部门列表失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 获取部门详情
    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartmentDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证用户身份
            User currentUser = validateUserIdentity(token);
            if (currentUser == null) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "未授权访问", null),
                        HttpStatus.UNAUTHORIZED);
            }

            Department department = departmentService.getDepartmentById(id.toString());

            // 如果不是管理员且用户不在该部门，则无权查看详情
            if (!User.UserRole.ADMIN.equals(currentUser.getRole()) &&
                    (currentUser.getDepartment() == null || !currentUser.getDepartment().getId().equals(id))) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "权限不足，您只能查看自己所在部门的详情", null),
                        HttpStatus.FORBIDDEN);
            }

            // 获取部门统计信息
            Map<String, Object> departmentInfo = new HashMap<>();
            departmentInfo.put("department", department);
            departmentInfo.put("employeeCount", departmentService.getEmployeeCountByDepartmentId(id.toString()));
            departmentInfo.put("projectCount", departmentService.getProjectCountByDepartmentId(id.toString()));

            return new ResponseEntity<>(
                    new ApiResponse(true, "部门详情获取成功", departmentInfo),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "获取部门详情失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新部门
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(
            @PathVariable String id,
            @RequestBody Department department,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证请求数据
            if (department.getName() == null || department.getName().trim().isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "部门名称不能为空", null),
                        HttpStatus.BAD_REQUEST);
            }

            // 验证用户身份
            User currentUser = validateUserIdentity(token);
            if (currentUser == null) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "未授权访问", null),
                        HttpStatus.UNAUTHORIZED);
            }

            // 检查权限 - 只有ADMIN可以更新部门
            if (!User.UserRole.ADMIN.equals(currentUser.getRole())) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "权限不足，只有管理员可以更新部门", null),
                        HttpStatus.FORBIDDEN);
            }

            Department updatedDepartment = departmentService.updateDepartment(id, department,
                    currentUser.getId().toString());
            return new ResponseEntity<>(
                    new ApiResponse(true, "部门更新成功", updatedDepartment),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "更新部门失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 删除部门
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证用户身份
            User currentUser = validateUserIdentity(token);
            if (currentUser == null) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "未授权访问", null),
                        HttpStatus.UNAUTHORIZED);
            }

            // 检查权限 - 只有ADMIN可以删除部门
            if (!User.UserRole.ADMIN.equals(currentUser.getRole())) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "权限不足，只有管理员可以删除部门", null),
                        HttpStatus.FORBIDDEN);
            }

            // 检查部门是否有员工
            Long employeeCount = departmentService.getEmployeeCountByDepartmentId(id.toString());
            if (employeeCount > 0) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "该部门下有员工，无法删除", null),
                        HttpStatus.BAD_REQUEST);
            }

            // 检查部门是否有项目
            int projectCount = departmentService.getProjectCountByDepartmentId(id.toString());
            if (projectCount > 0) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "该部门下有项目，无法删除", null),
                        HttpStatus.BAD_REQUEST);
            }

            departmentService.deleteDepartment(id.toString());
            return new ResponseEntity<>(
                    new ApiResponse(true, "部门删除成功", null),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "删除部门失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 获取所有部门（不分页）
    @GetMapping("/all")
    public ResponseEntity<?> getAllDepartments() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            return new ResponseEntity<>(
                    new ApiResponse(true, "部门列表获取成功", departments),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "获取部门列表失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 设置部门管理员
    @PutMapping("/{id}/manager")
    public ResponseEntity<?> setDepartmentManager(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证用户身份
            User currentUser = validateUserIdentity(token);
            if (currentUser == null) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "未授权访问", null),
                        HttpStatus.UNAUTHORIZED);
            }

            // 检查权限 - 只有ADMIN可以设置部门管理员
            if (!User.UserRole.ADMIN.equals(currentUser.getRole())) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "权限不足，只有管理员可以设置部门管理员", null),
                        HttpStatus.FORBIDDEN);
            }

            // 验证请求数据
            String managerId = requestBody.get("managerId");
            if (managerId == null || managerId.trim().isEmpty()) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "管理员ID不能为空", null),
                        HttpStatus.BAD_REQUEST);
            }

            // 设置部门管理员
            Department updatedDepartment = departmentService.setDepartmentManager(id.toString(), managerId,
                    currentUser.getId().toString());
            return new ResponseEntity<>(
                    new ApiResponse(true, "部门管理员设置成功", updatedDepartment),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "设置部门管理员失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 移除部门管理员
    @DeleteMapping("/{id}/manager")
    public ResponseEntity<?> removeDepartmentManager(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证用户身份
            User currentUser = validateUserIdentity(token);
            if (currentUser == null) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "未授权访问", null),
                        HttpStatus.UNAUTHORIZED);
            }

            // 检查权限 - 只有ADMIN可以移除部门管理员
            if (!User.UserRole.ADMIN.equals(currentUser.getRole())) {
                return new ResponseEntity<>(
                        new ApiResponse(false, "权限不足，只有管理员可以移除部门管理员", null),
                        HttpStatus.FORBIDDEN);
            }

            // 移除部门管理员
            Department updatedDepartment = departmentService.removeDepartmentManager(id.toString(),
                    currentUser.getId().toString());
            return new ResponseEntity<>(
                    new ApiResponse(true, "部门管理员移除成功", updatedDepartment),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(false, "移除部门管理员失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 验证用户身份的辅助方法
    private User validateUserIdentity(String token) {
        try {
            // 移除Bearer前缀
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return userService.validateToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}