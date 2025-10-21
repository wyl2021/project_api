# 项目进度统计系统

## 项目简介
这是一个基于Spring Boot的项目进度统计管理系统，提供完整的项目管理、任务追踪和进度统计功能。

## 技术栈
- Java 11
- Spring Boot 2.7.15
- Spring Data JPA
- MySQL/H2数据库
- Apache POI (Excel导出)
- Lombok
- MapStruct

## 系统功能

### 1. 项目管理
- 创建、查询、更新、删除项目
- 根据名称、状态、部门、负责人等条件筛选项目
- 项目进度自动计算和更新
- 项目健康状态评估

### 2. 任务管理
- 创建、查询、更新、删除任务
- 任务层级结构管理（父子任务）
- 任务进度跟踪和记录
- 任务状态管理

### 3. 进度统计
- 项目总进度自动计算
- 任务进度变更历史记录
- 项目统计数据可视化
- 甘特图数据支持

### 4. 导出功能
- 项目数据导出为Excel
- 任务数据导出为Excel

### 5. 定时任务
- 定期自动更新项目进度
- 关键项目进度监控

## 项目结构
```
src/main/java/com/example/project/
├── ProjectApplication.java       # 应用程序入口
├── config/                       # 配置类
│   └── AppConfig.java            # 应用配置
├── controller/                   # 控制器层
│   ├── ProjectController.java    # 项目相关API
│   └── TaskController.java       # 任务相关API
├── entity/                       # 实体类
│   ├── Project.java              # 项目实体
│   ├── Task.java                 # 任务实体
│   ├── User.java                 # 用户实体
│   ├── Department.java           # 部门实体
│   └── ProgressLog.java          # 进度日志实体
├── exception/                    # 异常处理
│   ├── GlobalExceptionHandler.java # 全局异常处理器
│   ├── ResourceNotFoundException.java # 资源未找到异常
│   ├── ValidationException.java  # 验证异常
│   └── ErrorDetails.java         # 错误详情类
├── repository/                   # 数据访问层
│   ├── ProjectRepository.java    # 项目Repository
│   ├── TaskRepository.java       # 任务Repository
│   ├── UserRepository.java       # 用户Repository
│   ├── DepartmentRepository.java # 部门Repository
│   └── ProgressLogRepository.java # 进度日志Repository
├── scheduler/                    # 定时任务
│   └── ProjectProgressScheduler.java # 项目进度定时更新
├── service/                      # 服务层
│   ├── ProjectService.java       # 项目Service接口
│   ├── TaskService.java          # 任务Service接口
│   └── impl/                     # 服务实现
│       ├── ProjectServiceImpl.java # 项目Service实现
│       └── TaskServiceImpl.java  # 任务Service实现
└── util/                         # 工具类
    ├── ApiResponse.java          # API响应封装
    ├── DateUtil.java             # 日期工具
    └── ExportUtil.java           # 导出工具
```

## 数据库配置
在application.yml中配置数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/project_progress?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## API接口说明

### 项目相关接口
- `POST /api/projects` - 创建新项目
- `GET /api/projects` - 获取所有项目
- `GET /api/projects/page` - 分页获取项目
- `GET /api/projects/{id}` - 根据ID获取项目详情
- `PUT /api/projects/{id}` - 更新项目信息
- `DELETE /api/projects/{id}` - 删除项目
- `GET /api/projects/search` - 搜索项目
- `GET /api/projects/{id}/statistics` - 获取项目统计信息
- `GET /api/projects/{id}/export` - 导出项目数据

### 任务相关接口
- `POST /api/tasks` - 创建新任务
- `GET /api/tasks` - 获取所有任务
- `GET /api/tasks/project/{projectId}` - 获取指定项目的所有任务
- `GET /api/tasks/{id}` - 根据ID获取任务详情
- `PUT /api/tasks/{id}` - 更新任务信息
- `DELETE /api/tasks/{id}` - 删除任务
- `POST /api/tasks/{id}/update-progress` - 更新任务进度
- `POST /api/tasks/{id}/update-status` - 更新任务状态
- `GET /api/tasks/project/{projectId}/gantt-data` - 获取甘特图数据
- `GET /api/tasks/project/{projectId}/export` - 导出任务数据

## 部署说明
1. 确保已安装JDK 11和Maven
2. 配置好MySQL数据库
3. 修改application.yml中的数据库连接信息
4. 运行命令 `mvn clean package` 打包项目
5. 使用 `java -jar project-progress-tracking-1.0.0.jar` 启动应用

## 开发说明
1. 克隆项目代码
2. 在IDE中导入为Maven项目
3. 确保IDE已安装Lombok插件
4. 配置数据库连接
5. 运行ProjectApplication类启动项目