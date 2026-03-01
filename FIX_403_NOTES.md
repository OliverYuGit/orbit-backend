# 403 问题修复说明

## 问题分析

前端登录后访问所有接口都返回 403 Forbidden。

## 根本原因

1. **CORS 配置问题**：使用了 `setAllowedOriginPatterns` 而不是 `setAllowedOrigins`，导致精确的 origin 匹配失败
2. **CORS headers 限制过严**：只允许特定的 headers，而前端可能发送其他 headers
3. **缺少调试日志**：无法快速定位问题

## 修复内容

### 1. SecurityConfig.java
- 将 `setAllowedOriginPatterns` 改为 `setAllowedOrigins`
- 将 `setAllowedHeaders` 从指定列表改为 `"*"`（允许所有 headers）
- 添加 `setMaxAge(3600L)` 以缓存 CORS 预检请求

### 2. JwtAuthenticationFilter.java
- 添加 `@Slf4j` 注解
- 添加详细的 DEBUG 日志，记录：
  - 请求 URI 和 Authorization header 状态
  - Token 验证结果
  - User ID 和 token version
  - Token version 不匹配的警告
  - 认证设置成功的确认

### 3. application.yml
- 添加日志级别配置：
  ```yaml
  logging:
    level:
      com.orbit.mission.auth: DEBUG
      org.springframework.security: DEBUG
  ```

## 部署步骤

1. 拉取最新代码：
   ```bash
   cd /path/to/orbit-backend
   git pull
   ```

2. 重新构建：
   ```bash
   ./mvnw clean package -DskipTests
   ```

3. 重启服务：
   ```bash
   docker-compose down
   docker-compose up -d --build
   ```

4. 查看日志验证：
   ```bash
   docker logs orbit-mission --tail 100 -f
   ```

## 测试

使用提供的测试脚本：
```bash
./test-auth.sh
```

或手动测试：
```bash
# 1. 登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. 使用返回的 token 访问受保护接口
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 预期结果

- 登录成功返回 token
- 使用 token 访问 `/api/v1/auth/me` 返回用户信息（200 OK）
- 日志中可以看到详细的认证流程

## 回滚方案

如果修复后仍有问题，可以回滚到上一个版本：
```bash
git revert HEAD
git push
```

## 后续优化建议

1. 在生产环境中将日志级别改回 INFO
2. 考虑添加更详细的错误响应，帮助前端调试
3. 添加 token 刷新机制的测试
