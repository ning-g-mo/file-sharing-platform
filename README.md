# 文件分享平台 📁

一个基于Spring Boot的安全、快速、临时文件分享服务平台。

## 🌟 项目特色

- **无需注册** - 即传即用，无需复杂的注册流程
- **临时存储** - 文件24小时后自动删除，保护用户隐私
- **安全可靠** - 文件加密存储，生成唯一分享链接
- **多格式支持** - 支持各种文件格式上传
- **实时统计** - 提供下载次数和系统状态监控
- **现代化UI** - 响应式设计，支持拖拽上传

## 🚀 功能特性

### 核心功能
- ✅ 文件上传（支持最大100MB）
- ✅ 文件下载和分享链接生成
- ✅ 文件自动过期删除（24小时）
- ✅ 下载统计和访问记录
- ✅ 系统状态监控
- ✅ 文件管理（查看、删除）

### 技术特性
- 🔒 文件安全存储
- 📊 实时数据统计
- 🎨 现代化Web界面
- 📱 响应式设计
- ⚡ 高性能文件处理

## 🛠️ 技术栈

### 后端
- **Spring Boot 2.7.0** - 主框架
- **Spring Web** - Web服务
- **Spring Data JPA** - 数据持久化
- **H2 Database** - 内存数据库
- **Maven** - 项目管理

### 前端
- **Vue.js 3** - 前端框架
- **Element Plus** - UI组件库
- **Axios** - HTTP客户端
- **现代CSS** - 样式设计

## 📦 快速开始

### 环境要求
- Java 8 或更高版本
- Maven 3.6 或更高版本

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/filecopy.git
   cd filecopy
   ```

2. **编译项目**
   ```bash
   ./mvnw clean compile
   ```

3. **运行应用**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **访问应用**
   
   打开浏览器访问：`http://localhost:8080/api`

### Windows用户
如果使用Windows系统，请使用：
```cmd
mvnw.cmd clean compile
mvnw.cmd spring-boot:run
```

## 🎯 使用说明

### 上传文件
1. 访问主页面
2. 点击上传区域或拖拽文件到上传区域
3. 选择要上传的文件（最大100MB）
4. 等待上传完成

### 分享文件
1. 上传完成后，点击"复制链接"按钮
2. 分享链接给其他用户
3. 其他用户通过链接可直接下载文件

### 管理文件
- **查看文件**：在"我的文件"区域查看已上传的文件
- **下载文件**：点击"下载"按钮
- **删除文件**：点击"删除"按钮（需确认）
- **查看统计**：查看文件下载次数和系统统计

## 🔧 配置说明

### 应用配置
主要配置文件：`src/main/resources/application.yml`

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

file:
  upload:
    path: ./uploads
    max-size: 104857600  # 100MB
    expire-hours: 24
```

### 自定义配置
- **端口修改**：修改`server.port`
- **上传路径**：修改`file.upload.path`
- **文件大小限制**：修改`file.upload.max-size`
- **过期时间**：修改`file.upload.expire-hours`

## 📁 项目结构

```
filecopy/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── cn/lemwood/fileshare/
│   │   │       ├── FileShareApplication.java     # 主启动类
│   │   │       ├── config/                      # 配置类
│   │   │       ├── controller/                  # 控制器
│   │   │       ├── entity/                      # 实体类
│   │   │       ├── repository/                  # 数据访问层
│   │   │       ├── service/                     # 业务逻辑层
│   │   │       └── task/                        # 定时任务
│   │   └── resources/
│   │       ├── static/                          # 静态资源
│   │       ├── templates/                       # 模板文件
│   │       └── application.yml                  # 配置文件
│   └── test/                                    # 测试代码
├── uploads/                                     # 文件上传目录
├── .github/workflows/                           # GitHub Actions
├── pom.xml                                      # Maven配置
└── README.md                                    # 项目说明
```

## 🔗 API接口

### 文件操作
- `POST /api/files/upload` - 上传文件
- `GET /api/files/download/{fileId}` - 下载文件
- `DELETE /api/files/{fileId}` - 删除文件
- `GET /api/files/list` - 获取文件列表

### 系统监控
- `GET /api/system/status` - 获取系统状态
- `POST /api/system/cleanup` - 清理过期文件
- `POST /api/system/optimize` - 优化存储空间

## 🚀 部署说明

### 生产环境部署

1. **打包应用**
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **运行JAR包**
   ```bash
   java -jar target/file-share-platform-1.0.0.jar
   ```

3. **使用Docker（可选）**
   ```dockerfile
   FROM openjdk:8-jre-slim
   COPY target/file-share-platform-1.0.0.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "/app.jar"]
   ```

### 环境变量配置
```bash
export SERVER_PORT=8080
export FILE_UPLOAD_PATH=/app/uploads
export FILE_MAX_SIZE=104857600
export FILE_EXPIRE_HOURS=24
```

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📝 更新日志

### v1.0.0 (2025-08-18)
- ✨ 初始版本发布
- ✅ 基础文件上传下载功能
- ✅ 文件自动过期删除
- ✅ 现代化Web界面
- ✅ 系统监控和统计
- ✅ GitHub Actions CI/CD

## ❓ 常见问题

**Q: 文件上传失败怎么办？**
A: 请检查文件大小是否超过100MB限制，确保网络连接正常。

**Q: 如何修改文件过期时间？**
A: 修改`application.yml`中的`file.upload.expire-hours`配置。

**Q: 可以部署到云服务器吗？**
A: 可以，请确保服务器有足够的存储空间和Java运行环境。

**Q: 如何备份上传的文件？**
A: 文件存储在`uploads`目录中，可以定期备份该目录。

## 📄 许可证

本项目采用 [MIT 许可证](LICENSE) - 查看 LICENSE 文件了解详情。

## 📞 联系方式

- 项目地址：[GitHub Repository](https://github.com/yourusername/filecopy)
- 问题反馈：[Issues](https://github.com/yourusername/filecopy/issues)
- 邮箱：your.email@example.com

---

⭐ 如果这个项目对你有帮助，请给个Star支持一下！

**Made with ❤️ by [Your Name]**