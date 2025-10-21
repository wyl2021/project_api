package com.example.project.service;

import com.example.project.dto.UserFilter;
import com.example.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface UserService {
    // 基本CRUD操作
    /**
     * 创建用户
     * 
     * @param user 用户实体
     * @return 创建的用户实体
     */
    User createUser(User user);

    /**
     * 更新用户
     * 
     * @param user 用户实体
     * @return 更新的用户实体
     */
    User updateUser(User user);

    /**
     * 删除用户
     * 
     * @param id 用户ID
     */
    void deleteUser(String id); // Changed from Long to String

    /**
     * 根据ID获取用户
     * 
     * @param id 用户ID
     * @return 用户实体
     */
    User getUserById(String id); // Changed from Long to String

    /**
     * 根据筛选条件查询用户列表
     * 
     * @param filter 筛选条件对象
     * @return 用户实体列表
     */
    List<User> findUsersByFilter(UserFilter filter);



    /**
     * 分页查询用户
     * 
     * @param pageable 分页信息
     * @return 分页用户实体
     */
    Page<User> findUsersByPage(Pageable pageable);

    /**
     * 注册用户
     * 
     * @param user     用户实体
     * @param password 密码
     * @return 注册的用户实体
     */
    User registerUser(User user, String password);

    // Map<String, Object> loginUser(String username, String password);

    /**
     * 用户登录
     * 
     * @param username    用户名
     * @param password    密码
     * @param isEncrypted 是否加密密码
     * @return 登录结果，包含token和用户信息
     */
    Map<String, Object> loginUser(String username, String password, boolean isEncrypted); // 添加新的方法签名

    /**
     * 用户注销
     * 
     * @param token 登录令牌
     */
    void logoutUser(String token);

    /**
     * 验证登录令牌
     * 
     * @param token 登录令牌
     * @return 用户实体
     */
    User validateToken(String token);



    /**
     * 获取所有部门及部门下的员工信息
     * 
     * @return 部门及员工信息映射
     */
    Map<String, List<User>> getUsersByDepartments();

}