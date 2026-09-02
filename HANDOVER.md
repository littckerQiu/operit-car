# Operit 车机版 - 项目交接文档

**交接日期**: 2026-09-02
**项目状态**: 代码定制完成，GitHub Actions 云编译调试中（尚未成功构建 APK）

---

## 一、项目概述

基于开源项目 Operit (operitai) 定制的车机版本，核心目标：
1. ✅ 移除内置 Linux 环境，精简体积
2. ✅ 设置内可选择将语音悬浮窗页面设为主界面
3. ✅ 保留 MCP 用于车机工具调用
4. ✅ 深度车机优化（横屏、大字体、驾驶安全、性能优化）

---

## 二、GitHub 仓库信息

- **仓库地址**: https://github.com/littckerQiu/operit-car
- **分支**: main
- **GitHub Token**: `[GITHUB_TOKEN-已移除]`（拥有所有权限，建议后续轮换）
- **本地路径**: `/home/user/.super_doubao/super-doubao-runtime/workspace/operit-car`

---

## 三、已完成的定制工作

### 3.1 新增文件

| 文件 | 功能 |
|------|------|
| `app/.../data/preferences/CarPreferencesManager.kt` | 车机偏好管理器，含主界面模式、驾驶安全、大字体、横屏锁定等17项设置 |
| `app/.../ui/features/car/CarHomeScreen.kt` | 车机语音主界面，大按钮大字体、音量可视化、消息气泡、底部控制区 |
| `app/.../ui/features/settings/screens/CarSettingsScreen.kt` | 独立车机设置页，6大分组 |
| `app/.../core/tools/mcp/car/CarMCPTools.kt` | 6个车机MCP工具：导航/媒体/车况/空调/蓝牙电话/音量 |
| `app/.../util/car/CarRuntimeOptimizer.kt` | 车机运行时优化：横屏锁定、屏幕常亮、内存管理、启动加速 |
| `app/.../terminal/` (8个stub文件) | 替代原 terminal 模块的 stub 实现，使用 Android 原生 sh -c |
| `app/.../com/arthenica/ffmpegkit/` (6个stub文件) | 替代 FFmpegKit 依赖的 stub 实现 |
| `.github/workflows/android-build.yml` | GitHub Actions 云编译工作流 |
| `CAR_EDITION_README.md` | 车机版说明文档 |

### 3.2 修改的核心文件

| 文件 | 修改内容 |
|------|----------|
| `MainActivity.kt` | 初始化 CarPreferencesManager 和 CarRuntimeOptimizer，根据主界面模式决定启动页面 |
| `OperitScreens.kt` | 注册 CarHome 和 CarSettings 两个 Screen |
| `SettingsScreen.kt` | 添加车机设置入口 |
| `AndroidManifest.xml` | 车机特性声明、configChanges 扩展 |
| `strings.xml` | 应用名改为"Operit 车机版" |
| `settings.gradle.kts` | 移除 :terminal 模块 |
| `app/build.gradle.kts` | 移除 terminal 依赖和 FFmpegKit Maven 依赖 |
| `.gitmodules` | 移除 terminal 子模块引用 |
| `MarkdownCodeTypeface.kt` | 移除对 terminal 模块字体资源的引用 |
| `CanvasCodeEditorView.kt` | 移除对 terminal 模块字体资源的引用 |

### 3.3 移除的内容

- ✅ `:terminal` 模块（内置 Linux 环境）
- ✅ `.gitmodules` 中的 terminal 子模块
- ✅ FFmpegKit 官方 Maven 依赖（改用本地 stub）

---

## 四、技术栈

- **Gradle**: 8.13
- **AGP**: 8.13.2
- **Kotlin**: 2.2.21
- **compileSdk**: 36
- **minSdk**: 26
- **targetSdk**: 34
- **NDK**: 27.0.12077973
- **JDK**: 21（Temurin）
- **Rust**: stable + aarch64-linux-android target（用于构建 liboperit_ripgrep.so）

---

## 五、GitHub Actions 云编译状态

### 工作流配置
- 文件：`.github/workflows/android-build.yml`
- 触发：push 到 main 分支
- 步骤：
  1. 检出代码
  2. 设置 JDK 21
  3. 设置 Rust + aarch64-linux-android target
  4. 安装 NDK
  5. 构建原生库 `liboperit_ripgrep.so`（Rust 项目在 `tools/native_ripgrep/`）
  6. 复制 .so 到 `app/src/main/jniLibs/arm64-v8a/`
  7. Gradle 构建 Debug APK
  8. 上传 APK 产物

### 构建历史（共11次）

| 次数 | 提交 | 状态 | 失败原因 |
|------|------|------|----------|
| #1-#2 | - | failure | 原工作流 terminal 子模块初始化 |
| #3 | - | failure | 缺少 liboperit_ripgrep.so |
| #4 | - | failure | FFmpegKit Maven 依赖无法解析 |
| #5 | - | failure | JDK 17 下 backdrop 库 bad class file |
| #6-#11 | 多次修复 | failure | Kotlin 编译错误（stub 类 API 不完整） |

### 当前最新构建
- **Run ID**: 33604795581
- **提交**: 8246d96
- **状态**: failure
- **URL**: https://github.com/littckerQiu/operit-car/actions/runs/33604795581

---

## 六、当前未解决的编译问题

### 问题根源
移除 `:terminal` 模块和 FFmpegKit 依赖后，使用了本地 stub 类替代，但 stub 类的 API 不完整，导致 `LinuxFileSystemTools.kt` 和 `StandardFFmpegTool.kt` 编译失败。

### 已修复的 stub 方法（FileSystemProvider）
- ✅ `permissions` 属性（FileInfo）
- ✅ `readFileSample(path, sampleSize)`
- ✅ `readFileWithLimit(path, maxBytes)`
- ✅ `getLineCount(path)`
- ✅ `readFileLines(path, startLine, endLine)`
- ✅ `writeFile(path, content, append)` → 返回 `FileOperationResult`
- ✅ `writeFileBytes(path, bytes)` → 返回 `FileOperationResult`
- ✅ `createDirectory(path, createParents)` → 返回 `FileOperationResult`
- ✅ `delete(path, recursive)` → 返回 `FileOperationResult`
- ✅ `move(sourcePath, destPath)` → 返回 `FileOperationResult`
- ✅ `copy(sourcePath, destPath, recursive)` → 返回 `FileOperationResult`
- ✅ `findFiles(basePath, pattern, maxDepth, caseInsensitive)`
- ✅ `getFileInfo(path)` → 返回 `FileInfo?`
- ✅ `FileInfo.lastModified` 类型改为 String

### 已修复的 stub 类（FFmpegKit）
- ✅ `MediaInformation.streams` 属性
- ✅ `StreamInformation` 类（index/type/codec/width/height 等）
- ✅ `StreamInformation.allProperties` 属性

### 可能仍存在的问题（未验证）
1. **MediaInformation.duration/bitrate 类型**：代码中 `mediaInfo.duration ?: "0"` 当作 String 使用，但 stub 中定义为 Long，可能需要改为 String
2. **其他文件中的 terminal/FFmpegKit 引用**：可能还有其他 Kotlin 文件引用了未实现的 stub 方法
3. **StandardFileSystemTools.kt**：该文件也使用 FileSystemProvider，可能有类似问题（文件被识别为二进制，需用 `grep -a` 检查）

### 调试建议
1. 每次构建失败后，用以下命令获取具体错误：
   ```bash
   GITHUB_TOKEN="[GITHUB_TOKEN-已移除]"
   JOB_ID=<从构建页面获取>
   curl -s -L -H "Authorization: token $GITHUB_TOKEN" \
     https://api.github.com/repos/littckerQiu/operit-car/actions/jobs/$JOB_ID/logs | grep "e: "
   ```
2. 根据错误信息补全对应 stub 类的方法
3. 提交推送后等待约20-25分钟构建完成

---

## 七、关键文件路径

### Stub 类（需要重点维护）
- **FileSystemProvider**: `app/src/main/java/com/ai/assistance/operit/terminal/provider/filesystem/FileSystemProvider.kt`
- **TerminalManager**: `app/src/main/java/com/ai/assistance/operit/terminal/TerminalManager.kt`
- **FFmpegKit 包**: `app/src/main/java/com/arthenica/ffmpegkit/`

### 车机定制代码
- **CarPreferencesManager**: `app/src/main/java/com/ai/assistance/operit/data/preferences/CarPreferencesManager.kt`
- **CarHomeScreen**: `app/src/main/java/com/ai/assistance/operit/ui/features/car/CarHomeScreen.kt`
- **CarSettingsScreen**: `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/CarSettingsScreen.kt`
- **CarMCPTools**: `app/src/main/java/com/ai/assistance/operit/core/tools/mcp/car/CarMCPTools.kt`
- **CarRuntimeOptimizer**: `app/src/main/java/com/ai/assistance/operit/util/car/CarRuntimeOptimizer.kt`

### 构建配置
- **工作流**: `.github/workflows/android-build.yml`
- **原生库**: `tools/native_ripgrep/`（Rust 项目）

---

## 八、后续接手建议

### 方案 A：继续补全 stub（推荐）
继续根据编译错误补全 FileSystemProvider 和 FFmpegKit stub 类的方法，直到编译通过。
- 优点：保持项目结构不变，改动最小
- 缺点：可能需要多轮迭代（每次构建20+分钟）

### 方案 B：本地编译调试
在本地配置 Android SDK + NDK + Rust 环境，直接运行 `./gradlew assembleDebug`，可以快速看到所有编译错误并一次性修复。
- 优点：调试效率高，不用等 GitHub Actions
- 缺点：需要配置本地环境

### 方案 C：恢复 terminal 模块
如果 stub 补全遇到太多问题，可以考虑恢复 `:terminal` 模块，但这会增加应用体积，违背"移除内置 Linux 环境"的需求。

---

## 九、安全提醒

1. **GitHub Token 已暴露在对话中**，建议接手后立即轮换：
   - 进入 GitHub → Settings → Developer settings → Personal access tokens
   - 删除旧 token，生成新 token
   - 更新仓库的 Actions secrets（如果使用了 secrets）

2. 当前工作流中没有使用 secrets，token 是通过 git remote URL 嵌入的，本地 `.git/config` 中可能包含 token，注意保护。

---

## 十、联系方式

如有疑问，可查看项目提交历史了解每次修改的具体内容：
```bash
cd /home/user/.super_doubao/super-doubao-runtime/workspace/operit-car
git log --oneline -20
```
