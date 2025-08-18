# 文件分享平台 📁

一个基于Spring Boot的临时文件分享服务，支持文件24小时自动删除。

## ✨ 特性

- 🚀 **即传即用** - 无需注册，拖拽上传
- ⏰ **临时存储** - 24小时自动删除
- 🔗 **安全分享** - 唯一链接，加密存储
- 📊 **实时统计** - 下载次数监控
- 📱 **响应式** - 现代化Web界面

## 🛠️ 技术栈

- **后端**: Spring Boot + JPA + H2
- **前端**: Vue.js 3 + Element Plus

## 🚀 快速开始

```bash
# 克隆项目
git clone https://github.com/ning-g-mo/file-sharing-platform.git
cd file-sharing-platform

# 运行应用
./mvnw spring-boot:run

# 访问应用
# http://localhost:8080/api
```

## 📖 使用说明

1. **上传**: 拖拽文件到上传区域（最大100MB）
2. **分享**: 复制生成的分享链接
3. **管理**: 查看、下载或删除已上传的文件

## ⚙️ 配置

主要配置在 `application.yml`：
- 端口: `server.port`
- 文件大小: `spring.servlet.multipart.max-file-size`
- 过期时间: `file.upload.expire-hours`

## 🔗 API接口

- `POST /api/files/upload` - 上传文件
- `GET /api/files/download/{fileId}` - 下载文件
- `DELETE /api/files/{fileId}` - 删除文件
- `GET /api/files/list` - 获取文件列表
- `GET /api/system/status` - 获取系统状态

## 📦 部署

```bash
# 打包
./mvnw clean package -DskipTests

# 运行
java -jar target/file-share-platform-1.0.0.jar
```

## 📄 许可证

本项目采用 [MIT 许可证](LICENSE)。

## 👨‍💻 作者

**柠枺 (ning-g-mo)**

- GitHub: [@ning-g-mo](https://github.com/ning-g-mo)
- 项目地址: [file-sharing-platform](https://github.com/ning-g-mo/file-sharing-platform)

---

⭐ 如果这个项目对你有帮助，请给个Star支持一下！