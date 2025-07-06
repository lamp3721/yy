# 一言 (Yiyan) 桌面版

一款简洁、优雅的桌面"一言"应用，将诗词、名言、或富含哲理的短句展示在您的桌面上，为您在繁忙的工作中带来片刻的宁静与思考。

## ✨ 主要功能

- **多数据源**: 自动从多个在线 API 获取"一言"内容，确保内容的丰富性和多样性。
- **动态展示**: 在桌面上以无边框、背景透明的悬浮窗形式展示文本。
- **自定义字体**: 使用内置的艺术字体，提供更佳的视觉效果。
- **智能调度**: 以随机的时间间隔自动刷新"一言"，保持新鲜感。
- **高可配置性**:
    - 所有 API 端点均在 `application.yml` 中配置，方便增删改查。
    - 支持 JSON 和纯文本两种 API 响应格式。
    - 为 JSON 格式提供了灵活的字段映射规则。
- **健壮的 API 管理**: 内置 API 熔断机制，当某个 API 连续请求失败时会自动禁用，一段时间后恢复，保证应用的稳定性。

## 📁 项目结构

项目采用分层架构，将核心业务逻辑与外部依赖（如 UI、API 请求）分离，使得代码更易于维护和扩展。

```
├── pom.xml               # Maven 项目配置
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── yiyan
│   │   │           ├── application       # 应用服务层 (Use Cases)
│   │   │           ├── core              # 核心领域层 (Domain)
│   │   │           ├── infrastructure    # 基础设施层 (Adapters)
│   │   │           └── Launcher.java     # Spring Boot 启动类
│   │   └── resources
│   │       ├── application.yml   # 应用核心配置文件
│   │       ├── fonts/            # 存放自定义字体
│   │       └── 一言.txt          # 本地"一言"备份文件
│   └── test
│       └── java
└── logs/                     # 存放应用日志
```

- **`core` (核心层)**: 定义了项目的核心业务对象（如 `Sentence`）和业务规则，不依赖任何外部框架。
- **`application` (应用层)**: 封装和实现了所有的用例，协调 `core` 层和 `infrastructure` 层。
- **`infrastructure` (基础设施层)**: 负责所有与外部世界的交互，例如：
    - 调用第三方 API (`adapter/api`)
    - 实现 UI 界面 (`ui`)
    - 实现持久化 (`adapter/persistence`)
    - 读取配置 (`config`)
- **`Launcher`**: 程序的入口，负责启动整个 Spring Boot 应用。

## ⚙️ 系统配置

应用的所有配置都在 `src/main/resources/application.yml` 文件中。

### API 端点配置与响应解析规则

您可以在 `yiyan.endpoints` 列表下管理所有 API 端点。

#### 1. JSON 类型解析 (`type: "json"`)

这是默认的解析方式，适用于返回 JSON 格式数据的 API。

- **`mappings`**: 定义 JSON 字段到程序内部字段（`text` 和 `author`）的映射规则。
    - 对于简单结构，直接写字段名，如 `hitokoto`。
    - 对于嵌套结构，使用 `/` 作为路径分隔符，如 `/data/content`。
    - 对于数组，使用索引，如 `/0/zw`。

**示例**:

假设 API 返回:
```json
{
  "data": {
    "content": "若知是梦何须醒，不比真如一相会。",
    "origin": {
      "author": "原神",
      "title": "《神里绫华》"
    }
  }
}
```

配置应为:
```yaml
- name: "example.api"
  url: "https://api.example.com/yiyan"
  parser:
    type: "json" # 可省略
    mappings:
      text: "/data/content"
      author: "/data/origin/author"
```

#### 2. 纯文本类型解析 (`type: "plain_text"`)

适用于直接返回一句话文本的 API。

- **`type`**: 必须明确设置为 `plain_text`。

**示例**:

假设 API 直接返回字符串 `面朝大海，春暖花开`。

配置应为:
```yaml
- name: "example.plain.text.api"
  url: "https://api.example.com/text"
  parser:
    type: "plain_text"
```

## 🚀 如何添加新的 API

1.  打开 `src/main/resources/application.yml` 文件。
2.  在 `yiyan.endpoints` 列表下，仿照现有格式添加一个新的条目。
3.  填写 `name` (自定义、唯一) 和 `url` (API 请求地址)。
4.  根据 API 的返回格式，配置 `parser`：
    - 如果是 JSON，设置 `mappings`。
    - 如果是纯文本，设置 `type: "plain_text"`。
5.  保存文件并重新启动应用即可。

## 📦 构建与运行

本项目使用 Apache Maven 构建。

1.  **构建项目**

    在项目根目录下打开终端，运行以下命令：
    ```bash
    mvn clean package
    ```
    这将在 `target/` 目录下生成一个名为 `yiyan-desktop-1.0.0.jar` 的可执行文件。

2.  **运行应用**

    使用 Java 运行生成的 JAR 文件。请确保您的系统已安装 Java 17 或更高版本，并处于图形用户界面环境中。
    ```bash
    java -jar target/yiyan-desktop-1.0.0.jar
    ```

    应用启动后，您将在桌面上看到"一言"悬浮窗。 