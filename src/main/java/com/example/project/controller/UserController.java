package com.example.project.controller;

import com.example.project.dto.UserFilter;
import com.example.project.entity.User;
import com.example.project.exception.ValidationException;
import com.example.project.service.UserService;
import com.example.project.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.project.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    // 创建用户
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return new ResponseEntity<>(new ApiResponse(true, "用户创建成功", createdUser), HttpStatus.CREATED);
        } catch (ValidationException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), e.getErrors()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Log the exception details for debugging
            e.printStackTrace();
            return new ResponseEntity<>(new ApiResponse(false, "创建用户失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 获取用户列表 - 支持分页处理
    @GetMapping("/page")
    public ResponseEntity<?> getUsersByPage(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userService.findUsersByPage(pageable);

        // 构建包含分页信息的响应
        Map<String, Object> response = new HashMap<>();
        response.put("content", userPage.getContent());
        response.put("currentPage", userPage.getNumber());
        response.put("totalItems", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());

        return new ResponseEntity<ApiResponse>(new ApiResponse(true, "用户获取成功", response), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        // 如果提供了分页参数，使用分页查询
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userService.findUsersByPage(pageable);

            // 构建包含分页信息的响应
            Map<String, Object> response = new HashMap<>();
            response.put("content", userPage.getContent());
            response.put("currentPage", userPage.getNumber());
            response.put("totalItems", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());

            return new ResponseEntity<ApiResponse>(new ApiResponse(true, "用户获取成功", response), HttpStatus.OK);
        } else {
            // 否则返回所有用户（保持向后兼容）
            List<User> users = userService.findUsersByFilter(new UserFilter());
            return new ResponseEntity<>(new ApiResponse(true, "用户获取成功", users), HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return new ResponseEntity<>(new ApiResponse(true, "用户获取成功", user), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody User user) { // Changed from Long to
                                                                                           // String
        user.setId(id);
        User updatedUser = userService.updateUser(user);
        return new ResponseEntity<>(new ApiResponse(true, "用户更新成功", updatedUser), HttpStatus.OK);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<?> getUsersByDepartmentId(@PathVariable String departmentId) { // Changed from Long to String
        UserFilter filter = new UserFilter();
        filter.setDepartmentId(departmentId);
        List<User> users = userService.findUsersByFilter(filter);
        return new ResponseEntity<>(new ApiResponse(true, "用户获取成功", users), HttpStatus.OK);
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(new ApiResponse(true, "用户删除成功", null), HttpStatus.OK);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        UserFilter filter = new UserFilter();
        filter.setUsername(username);
        List<User> users = userService.findUsersByFilter(filter);
        if (users.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "用户不存在", null), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new ApiResponse(true, "用户获取成功", users.get(0)), HttpStatus.OK);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            UserFilter filter = new UserFilter();
            filter.setRole(userRole);
            List<User> users = userService.findUsersByFilter(filter);
            return new ResponseEntity<>(new ApiResponse(true, "用户获取成功", users), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse(false, "无效的角色", null), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/departments-with-users")
    public ResponseEntity<?> getDepartmentsWithUsers() {
        Map<String, List<User>> departmentsWithUsers = userService.getUsersByDepartments();
        return new ResponseEntity<>(new ApiResponse(true, "部门及员工信息获取成功", departmentsWithUsers), HttpStatus.OK);
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        // 这里应该从请求中获取当前登录用户的信息
        // 由于缺少上下文，这里仅作为示例
        // 实际实现时需要结合项目的认证机制
        return new ResponseEntity<>(new ApiResponse(true, "当前用户信息获取成功", null), HttpStatus.OK);
    }

    /**
     * 上传临时头像（为新增用户准备）
     * 路径：/api/users/avatar
     * 等同于 /api/users//avatar (userId为空)
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadTemporaryAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            // 直接调用uploadAvatar方法，传入null作为id参数
            // 这样可以重用相同的临时头像上传逻辑
            return uploadAvatar(null, file, request);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ApiResponse(false, "临时头像上传失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 修改后的头像上传接口，支持userId不传值时新建上传头像
    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable(required = false) String id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            // 保存新头像
            String fileName = FileUploadUtil.saveFile(file, uploadDir);
            
            // 构建头像访问URL
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            // 确保accessUrl和fileName之间有斜杠
            String avatarUrl = baseUrl + accessUrl + (accessUrl.endsWith("/") ? "" : "/") + fileName;
            
            // 创建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("avatarFileName", fileName);
            responseData.put("avatarUrl", avatarUrl);
            
            // 如果提供了有效的用户ID（不是null且不是字符串"null"），则关联到现有用户
            if (id != null && !id.isEmpty() && !"null".equals(id)) {
                User user = userService.getUserById(id);
                if (user == null) {
                    return new ResponseEntity<>(new ApiResponse(false, "用户不存在", null), HttpStatus.NOT_FOUND);
                }

                // 删除旧头像（如果存在且不是默认头像）
                if (user.getAvatar() != null && !user.getAvatar().equals(FileUploadUtil.DEFAULT_AVATAR_URL)) {
                    // 提取文件名进行删除
                    String oldAvatarName = user.getAvatar().substring(user.getAvatar().lastIndexOf("/") + 1);
                    String oldAvatarPath = uploadDir + File.separator + oldAvatarName;
                    FileUploadUtil.deleteFile(oldAvatarPath);
                }

                // 只存储文件名而非完整URL，减少数据库存储压力
                user.setAvatar(fileName);
                User updatedUser = userService.updateUser(user);
                responseData.put("user", updatedUser);
                return new ResponseEntity<>(new ApiResponse(true, "头像上传成功并关联到用户", responseData), HttpStatus.OK);
            } else {
                // 如果没有提供用户ID，则返回临时头像信息
                // 前端可以在创建用户时使用这个临时头像
                return new ResponseEntity<>(new ApiResponse(true, "临时头像上传成功", responseData), HttpStatus.CREATED);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>(new ApiResponse(false, "文件上传失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, "头像更新失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 统一用户筛选接口
     * 支持多条件组合筛选，替代多个独立的筛选方法
     */
    @PostMapping("/filter")
    public ResponseEntity<?> findUsersByFilter(@RequestBody UserFilter filter) {
        try {
            List<User> users = userService.findUsersByFilter(filter);
            return new ResponseEntity<>(new ApiResponse(true, "用户筛选成功", users), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ApiResponse(false, "用户筛选失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取默认头像
     */
    @GetMapping("/default-avatar")
    public ResponseEntity<?> getDefaultAvatar() {
        return new ResponseEntity<>(new ApiResponse(true, "获取默认头像成功",
                FileUploadUtil.DEFAULT_AVATAR_URL), HttpStatus.OK);
    }

    /**
     * 简易重置密码接口
     * 根据用户名直接重置密码，无需验证链接或token验证
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            // 获取请求参数
            String username = request.get("username");
            String newPassword = request.get("newPassword");

            // 验证参数
            if (username == null || username.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse(false, "用户名和新密码不能为空", null), HttpStatus.BAD_REQUEST);
            }

            // 查找用户
            // 使用统一的筛选方法
            UserFilter filter = new UserFilter();
            filter.setUsername(username);
            List<User> users = userService.findUsersByFilter(filter);
            User user = users.isEmpty() ? null : users.get(0);
            if (user == null) {
                return new ResponseEntity<>(new ApiResponse(false, "用户不存在", null), HttpStatus.NOT_FOUND);
            }

            // 重置密码
            user.setPassword(newPassword); // updateUser方法会自动加密密码
            User updatedUser = userService.updateUser(user);

            return new ResponseEntity<>(new ApiResponse(true, "密码重置成功", updatedUser), HttpStatus.OK);
        } catch (ValidationException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), e.getErrors()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ApiResponse(false, "密码重置失败: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
