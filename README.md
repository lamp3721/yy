# 一言 (Yiyan) 桌面版

一款简洁、优雅的桌面"一言"应用，将诗词、名言或富含哲理的短句展示在您的桌面上，为您在繁忙的工作中带来片刻的宁静与思考。

![应用截图](https://raw.githubusercontent.com/i-trader/yiyan-desktop/master/docs/screenshot.png)

## ✨ 主要功能

- **随机数据源**: 每次都从配置的API列表中随机选择一个进行尝试，保持未知的新鲜感。
- **动态展示**: 在桌面上以无边框、背景透明的悬浮窗形式展示文本。
- **自定义字体**: 使用内置的艺术字体，提供更佳的视觉效果。
- **智能调度**: 以随机的时间间隔自动刷新"一言"，避免单调。
- **高可配置性**:
    - 所有API端点均在外置的 `api-list.json` 文件中配置，无需修改代码或核心配置文件即可增删API。
    - 支持 **JSON** 和 **纯文本** 两种API响应格式。
    - 为JSON格式提供了灵活的字段映射规则（支持JSON Pointer）。
- **健壮的API管理**:
    - **API自检**: 应用启动后自动检查所有API的可用性。
    - **熔断机制**: 当某个API连续请求失败时会自动熔断，在一段时间内不再调用，保证应用的稳定性。
    - **网络冷却**: 当检测到网络连接问题时，会暂停所有请求一段时间，避免频繁无效的尝试。
- **便捷的操作**:
    - **窗口拖动**: 可按住鼠标左键上下拖动窗口。
    - **右键菜单**: 提供刷新、复制、显示/隐藏作者、退出等快捷操作。

## 🛠️ 技术栈

- **核心框架**: Spring Boot 3
- **界面**: Java Swing (AWT)
- **HTTP客户端**: OkHttp
- **异步与调度**: Spring Scheduling, Resilience4j (熔断器)
- **构建工具**: Apache Maven

## 📁 项目结构

项目采用分层架构，将核心业务逻辑与外部依赖（如 UI、API 请求）分离，使得代码更易于维护和扩展。

```
├── pom.xml               # Maven 项目配置
├── api-list.json         # 【重要】外部API端点配置文件
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
│   │       └── fonts/            # 存放自定义字体
│   └── test
└── yiyan_log.txt             # 成功获取到的一言日志
```

- **`core` (核心层)**: 定义了项目的核心业务对象（如 `Sentence`）和业务规则，不依赖任何外部框架。
- **`application` (应用层)**: 封装和实现了所有的用例，协调 `core` 层和 `infrastructure` 层。
- **`infrastructure` (基础设施层)**: 负责所有与外部世界的交互，例如调用第三方API、实现UI界面、读取配置等。
- **`Launcher`**: 程序的入口，负责启动整个 Spring Boot 应用。

## ⚙️ 系统配置

应用的大部分配置都在 `src/main/resources/application.yml` 文件中，但最重要的API列表在外部的 `api-list.json` 文件中。

### API 端点配置 (`api-list.json`)

这是配置所有API端点的核心文件。您可以直接修改它来增删API。

#### 1. JSON 类型解析 (`type: "json"`)

这是默认的解析方式，适用于返回 JSON 格式数据的 API。

- **`mappings`**: 定义 JSON 字段到程序内部字段（`text` 和 `author`）的映射规则。
    - 对于简单结构，直接写字段名，如 `hitokoto`。
    - 对于嵌套结构，使用 [JSON Pointer](https://datatracker.ietf.org/doc/html/rfc6901) 标准，以 `/` 作为路径分隔符，如 `/data/content`。
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

`api-list.json` 中的配置应为:
```json
[
  {
    "name": "example-api",
    "url": "https://api.example.com/yiyan",
    "parser": {
      "type": "json",
      "mappings": {
        "text": "/data/content",
        "author": "/data/origin/author"
      }
    }
  }
]
```

#### 2. 纯文本类型解析 (`type: "plain_text"`)

适用于直接返回一句话文本的 API。

- **`type`**: 必须明确设置为 `plain_text`。

**示例**:

假设 API 直接返回字符串 `面朝大海，春暖花开`。

`api-list.json` 中的配置应为:
```json
[
  {
    "name": "example-plain-text-api",
    "url": "https://api.example.com/text",
    "parser": {
      "type": "plain_text"
    }
  }
]
```

### 其他配置 (`application.yml`)

您可以在 `application.yml` 中调整其他行为：

- **`yiyan.api-list-path`**: 指定 `api-list.json` 的加载路径，默认为 `classpath:api-list.json`。
- **`yiyan.max-text-length`**: "一言"的最大长度限制，超过则丢弃。
- **`scheduler.min-delay-seconds`**: 自动刷新的最小间隔（秒）。
- **`scheduler.max-delay-seconds`**: 自动刷新的最大间隔（秒）。

## 🚀 如何添加新的 API

1.  打开根目录下的 `api-list.json` 文件。
2.  在JSON数组中，仿照现有格式添加一个新的JSON对象。
3.  填写 `name` (自定义、唯一) 和 `url` (API请求地址)。
4.  根据API的返回格式，配置 `parser`。
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