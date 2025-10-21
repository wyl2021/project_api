package com.example.project.repository;

import com.example.project.dto.UserFilter;
import com.example.project.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    // 统一筛选方法 - 基于UserFilter构建动态查询
    default List<User> findByFilter(UserFilter filter) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 根据部门ID筛选
            if (StringUtils.hasText(filter.getDepartmentId())) {
                predicates.add(criteriaBuilder.equal(root.get("department").get("id"), filter.getDepartmentId()));
            }
            
            // 根据用户名筛选（模糊匹配）
            if (StringUtils.hasText(filter.getUsername())) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + filter.getUsername() + "%"));
            }
            
            // 根据邮箱筛选
            if (StringUtils.hasText(filter.getEmail())) {
                predicates.add(criteriaBuilder.equal(root.get("email"), filter.getEmail()));
            }
            
            // 根据手机号筛选
            if (StringUtils.hasText(filter.getPhone())) {
                predicates.add(criteriaBuilder.equal(root.get("phone"), filter.getPhone()));
            }
            
            // 根据角色筛选
            if (filter.getRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), filter.getRole()));
            }
            
            // 根据活跃状态筛选
            if (filter.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filter.getIsActive()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return findAll(spec);
    }
    // 根据用户名查询用户
    User findByUsername(String username);

    // 根据部门ID查询用户
    List<User> findByDepartmentId(String departmentId);

    // 根据角色查询用户
    List<User> findByRole(User.UserRole role);

    // 查询活跃用户
    List<User> findByIsActiveTrue();

    // 根据邮箱查询用户
    User findByEmail(String email);

    // 根据手机号查询用户
    User findByPhone(String phone);

    // 根据用户名或邮箱查询用户
    List<User> findByUsernameContainingOrEmailContaining(String username, String email);

    Long countActiveUsersByDepartmentId(String departmentId);
}