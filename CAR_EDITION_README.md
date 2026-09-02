# Operit 车机版 (Car Edition)

基于 Operit 开源项目定制的车机专用 AI 助手版本。

## 主要改动

### 1. 移除内置 Linux 环境
- 移除了 `:terminal` 模块（OperitTerminalCore 子模块）
- 移除了完整的 Ubuntu/Linux 容器环境
- 使用 Android 原生 shell (`sh -c`) 替代，支持基本命令执行
- 创建了轻量级 terminal stub 包，确保 API 兼容性
- 显著减小 APK 体积和内存占用

### 2. 语音悬浮窗作为主界面
- 新增车机专属语音主界面 (`CarHomeScreen`)
- 大字体、大按钮设计，适合驾驶中操作
- 语音优先交互，支持自动连续对话
- 在设置中可切换"语音悬浮窗主界面"或"标准聊天界面"

### 3. 保留 MCP 功能
- 完整保留 MCP (Model Context Protocol) 支持
- 新增车机专用 MCP 工具集 (`CarMCPTools`)
  - `car_navigate` - 导航控制
  - `car_media_control` - 媒体播放控制
  - `car_vehicle_status` - 车辆状态查询
  - `car_climate_control` - 空调控制
  - `car_bluetooth_call` - 蓝牙电话
  - `car_volume_control` - 音量控制

### 4. 深度车机优化
- **UI 优化**: 大字体模式、大触摸目标、横屏适配
- **交互优化**: 语音优先、驾驶安全模式、减少分心操作
- **性能优化**: 启动加速、激进内存管理、可选减少动画
- **车载特性**: 屏幕常亮、蓝牙免提、导航集成、媒体控制
- **设置优化**: 独立的车机设置页面，所有车载选项集中管理

## 项目结构

### 新增文件
```
app/src/main/java/com/ai/assistance/operit/
├── data/preferences/
│   └── CarPreferencesManager.kt          # 车机偏好设置管理器
├── ui/features/car/
│   └── CarHomeScreen.kt                  # 车机语音主界面
├── ui/features/settings/screens/
│   └── CarSettingsScreen.kt              # 车机设置页面
├── core/tools/mcp/car/
│   └── CarMCPTools.kt                    # 车机 MCP 工具集
├── util/car/
│   └── CarRuntimeOptimizer.kt            # 车机运行时优化器
└── terminal/                             # terminal stub（替代原模块）
    ├── TerminalManager.kt
    ├── rememberTerminalEnv.kt
    ├── data/TerminalState.kt
    ├── main/TerminalScreen.kt
    ├── provider/type/HiddenExecResult.kt
    ├── provider/filesystem/FileSystemProvider.kt
    ├── utils/SSHFileConnectionManager.kt
    └── view/domain/ansi/TerminalChar.kt
```

### 修改文件
- `settings.gradle.kts` - 移除 `:terminal` 模块
- `app/build.gradle.kts` - 移除 terminal 依赖
- `app/src/main/AndroidManifest.xml` - 添加车机特性、优化配置
- `app/src/main/java/.../ui/main/MainActivity.kt` - 车机启动逻辑
- `app/src/main/java/.../ui/main/screens/OperitScreens.kt` - 注册车机界面
- `app/src/main/java/.../ui/main/screens/ScreenRouteRegistry.kt` - 路由注册
- `app/src/main/java/.../ui/features/settings/screens/SettingsScreen.kt` - 车机设置入口
- `app/src/main/res/values/strings.xml` - 应用名称、字符串资源
- `.gitmodules` - 移除 terminal 子模块

## 构建说明

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17
- Android SDK 34
- Gradle 8.x

### 构建步骤
```bash
# 克隆项目（无需初始化 terminal 子模块）
git clone <repository-url>
cd operit-car

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK（需要签名配置）
./gradlew assembleRelease
```

### 注意事项
1. 本版本移除了内置 Linux 环境，依赖完整 Linux 环境的工具包可能无法正常工作
2. 基本 shell 命令仍可通过 Android 原生 shell 执行
3. MCP 服务器如果依赖 Linux 环境，需要自行适配
4. 车机 MCP 工具中的车辆数据需要接入车机厂商 API 才能获取真实数据

## 默认配置

车机版默认启用以下设置：
- 语音悬浮窗作为主界面
- 驾驶安全模式
- 大字体模式
- 横屏锁定
- 自动开始聆听
- 语音反馈
- 蓝牙免提
- 激进内存管理
- 启动加速
- 屏幕常亮

可在"设置 → 车机设置"中调整所有选项。

## 版本信息
- 基于: Operit (main branch)
- 版本: Car Edition 1.0
- 构建日期: 2026-09-02
