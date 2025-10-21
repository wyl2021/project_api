@RestController
@RequestMapping("/users") // 去掉/api前缀，与ProjectController保持一致
public class UserController {
    // ... 现有代码 ...

    // 分页获取用户 - 修复路径配置
    @GetMapping("/page") // 只保留/page
    public ResponseEntity<?> getUsersByPage(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // ... 现有实现代码 ...
        return new ResponseEntity<>(new ApiResponse(true, "用户获取成功", pageUsers), HttpStatus.OK);
    }
    // ... 其他方法保持不变 ...
}