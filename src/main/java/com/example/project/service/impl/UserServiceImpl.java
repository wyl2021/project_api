package com.example.project.service.impl;

// 在导入部分添加
import com.example.project.dto.UserFilter;
import com.example.project.util.FileUploadUtil;
import com.example.project.entity.User;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.exception.ValidationException;
import com.example.project.repository.UserRepository;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import javax.persistence.criteria.Predicate;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private Map<String, String> tokenCache = new HashMap<>();

    @Override
    public List<User> findUsersByFilter(UserFilter filter) {
        // 默认只查询活跃用户，除非明确指定了isActive条件
        if (filter.getIsActive() == null) {
            filter.setIsActive(true);
        }
        // 调用Repository层的统一筛选方法
        List<User> users = userRepository.findByFilter(filter);
        // 将结果设置到filter对象的users字段中
        filter.setUsers(users);
        return users;
    }

    @Override
    public User createUser(User user) {
        validateUser(user);

        // 检查用户名是否已存在
        UserFilter filter = new UserFilter();
        filter.setUsername(user.getUsername());
        List<User> existingUsers = findUsersByFilter(filter);
        if (!existingUsers.isEmpty()) {
            throw new ValidationException("用户名已存在");
        }

        // 设置默认头像（如果没有设置）
        if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
            user.setAvatar(FileUploadUtil.DEFAULT_AVATAR_URL);
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置创建和更新时间
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public User registerUser(User user, String password) {
        // 复制密码
        user.setPassword(password);

        // 确保设置默认头像
        if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
            user.setAvatar(FileUploadUtil.DEFAULT_AVATAR_URL);
        }

        return createUser(user);
    }

    @Override
    public User updateUser(User user) {
        User existingUser = getUserById(user.getId());
        validateUser(user);

        // 检查用户名是否已被其他用户使用
        if (!existingUser.getUsername().equals(user.getUsername())) {
            UserFilter filter = new UserFilter();
            filter.setUsername(user.getUsername());
            List<User> usersWithSameUsername = findUsersByFilter(filter);
            // 排除当前正在更新的用户自己
            boolean usernameExists = usersWithSameUsername.stream()
                    .anyMatch(u -> !u.getId().equals(user.getId()));
            if (usernameExists) {
                throw new ValidationException("用户名已存在");
            }
            existingUser.setUsername(user.getUsername());
        }

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setDepartment(user.getDepartment());
        existingUser.setRole(user.getRole());
        existingUser.setIsActive(user.getIsActive());
        existingUser.setAvatar(user.getAvatar());

        // 只有当明确提供了新密码且与现有密码不同时才更新密码
        // 避免在只更新头像等其他字段时错误地修改密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // 检查是否为加密密码（如果是加密密码，长度通常大于60）
            if (user.getPassword().length() < 60) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                // 如果已经是加密密码格式，直接使用
                existingUser.setPassword(user.getPassword());
            }
        }

        existingUser.setUpdatedTime(LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(String id) {
        User user = getUserById(id);
        // 使用软删除替代物理删除，避免外键约束错误
        user.setIsActive(false);
        user.setUpdatedTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到ID为" + id + "的用户"));
    }

    @Override
    public Page<User> findUsersByPage(Pageable pageable) {
        // 使用Specification确保只查询活跃用户
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("isActive"), true);
        };
        return userRepository.findAll(spec, pageable);
    }

    @Override
    public Map<String, Object> loginUser(String username, String password, boolean isEncrypted) {
        // 使用统一的筛选方法
        UserFilter filter = new UserFilter();
        filter.setUsername(username);
        List<User> users = findUsersByFilter(filter);
        User user = users.isEmpty() ? null : users.get(0);

        if (user == null) {
            throw new ValidationException("用户名不存在");
        }

        if (!user.getIsActive()) {
            throw new ValidationException("用户已被禁用");
        }

        if (isEncrypted) {
            if (!user.getPassword().equals(password)) {
                throw new ValidationException("密码错误");
            }
        } else {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new ValidationException("密码错误");
            }
        }

        String token = generateToken();
        tokenCache.put(token, user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);

        return result;
    }

    // @Override
    // public Map<String, Object> loginUser(String username, String password) {
    // return loginUser(username, password, false);
    // }

    @Override
    public void logoutUser(String token) {
        if (tokenCache.containsKey(token)) {
            tokenCache.remove(token);
        }
    }

    // @
    // public List<User> findActiveUsers() {
    // // 复用统一的筛选方法
    // UserFilter filter = new UserFilter();
    // filter.setIsActive(true);
    // return findUsersByFilter(filter);
    // }

    @Override
    public User validateToken(String token) {
        if (token == null || !tokenCache.containsKey(token)) {
            throw new ValidationException("无效或过期的令牌");
        }

        String userId = tokenCache.get(token);
        return getUserById(userId);
    }

    // 辅助方法
    private void validateUser(User user) {
        List<String> errors = new ArrayList<>();

        if (!StringUtils.hasText(user.getUsername())) {
            errors.add("用户名不能为空");
        }

        if (!StringUtils.hasText(user.getName())) {
            errors.add("姓名不能为空");
        }

        if (!StringUtils.hasText(user.getEmail())) {
            errors.add("邮箱不能为空");
        } else if (!user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errors.add("邮箱格式不正确");
        }

        if (user.getPhone() != null && !user.getPhone().isEmpty() &&
                !user.getPhone().matches("^\\+?[1-9]\\d{1,14}$")) {
            errors.add("手机号格式不正确");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("用户验证失败", errors);
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public Map<String, List<User>> getUsersByDepartments() {
        List<User> allUsers = userRepository.findAll();
        Map<String, List<User>> usersByDepartment = new HashMap<>();

        for (User user : allUsers) {
            if (user.getDepartment() != null) {
                String departmentName = user.getDepartment().getName();
                usersByDepartment.computeIfAbsent(departmentName, k -> new ArrayList<>()).add(user);
            } else {
                // 处理没有部门的用户
                usersByDepartment.computeIfAbsent("无部门", k -> new ArrayList<>()).add(user);
            }
        }

        return usersByDepartment;
    }
}