# 数据库配置文件说明

## 概述
此目录包含文件分享平台的外部数据库配置文件，允许用户在不重新编译JAR文件的情况下修改数据库配置。

## 配置文件
- `database.yml` - 主数据库配置文件

## 配置优先级
系统按以下优先级加载配置：
1. **外部配置文件** (`./config/database.yml`) - 优先级最高
2. **内置配置文件** (`classpath:database.yml`) - 备用配置

## 支持的数据库类型
- **SQLite** - 轻量级文件数据库，无需额外安装
- **MySQL** - 企业级关系型数据库

## 环境配置
系统支持以下环境配置：

### SQLite 配置
- `default` - 默认SQLite配置
- `development` - 开发环境SQLite配置
- `test` - 测试环境SQLite配置
- `production` - 生产环境SQLite配置

### MySQL 配置
- `development-mysql` - 开发环境MySQL配置
- `test-mysql` - 测试环境MySQL配置
- `production-mysql` - 生产环境MySQL配置

## 配置示例

### SQLite 配置示例
```yaml
database:
  default:
    type: sqlite
    url: jdbc:sqlite:./data/fileshare.db
    driver-class-name: org.sqlite.JDBC
```

### MySQL 配置示例
```yaml
database:
  development-mysql:
    type: mysql
    url: jdbc:mysql://localhost:3306/fileshare_dev?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your_password
```

## 使用方法

### 1. 修改配置
直接编辑 `config/database.yml` 文件，修改相应的数据库配置。

### 2. 重启应用
修改配置后，重启应用程序以使配置生效：
```bash
java -jar file-share-platform-1.0.0.jar
```

### 3. 验证配置
访问以���API端点验证配置是否生效：
- `http://localhost:8080/api/database/current` - 查看当前数据库配置
- `http://localhost:8080/api/database/status` - 检查数据库连接状态

## 注意事项

### SQLite
- 数据库文件会自动创建在指定路径
- 确保应用程序对数据库文件目录有读写权限
- 适合小型应用和开发环境

### MySQL
- 需要预先创建数据库
- 确保MySQL服务正在运行
- 配置正确的用户名和密码
- 适合生产环境和大型应用

## 故障排除

### 配置文件未生效
1. 检查配置文件路径是否正确 (`./config/database.yml`)
2. 检查YAML语法是否正确
3. 重启应用程序

### 数据库连接失败
1. 检查数据库服务是否运行
2. 验证连接参数（URL、用户名、密码）
3. 检查网络连接和防火墙设置

### SQLite文件权限问题
1. 确保应用程序对数据目录有写权限
2. 检查磁盘空间是否充足

## 技术支持
如需技术支持，请访问：
- API帮助：`http://localhost:8080/api/database/help`
- 项目文档：查看项目README.md文件