package com.ai.assistance.operit.terminal

import android.content.Context
import com.ai.assistance.operit.util.AppLogger
import com.ai.assistance.operit.terminal.data.TerminalState
import com.ai.assistance.operit.terminal.provider.filesystem.FileSystemProvider
import com.ai.assistance.operit.terminal.provider.type.HiddenExecResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 车机版终端管理器（Stub）
 * 移除了内置 Linux 环境，使用 Android 原生 shell 执行基本命令
 */
class TerminalManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: TerminalManager? = null
        private const val TAG = "TerminalManager"

        fun getInstance(context: Context): TerminalManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TerminalManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 文件系统提供者
    private val fileSystemProvider = FileSystemProvider(context)

    fun getFileSystemProvider(): FileSystemProvider = fileSystemProvider

    // 状态流
    private val _commandExecutionEvents = MutableSharedFlow<CommandExecutionEvent>(extraBufferCapacity = 100)
    val commandExecutionEvents: SharedFlow<CommandExecutionEvent> = _commandExecutionEvents

    private val _directoryChangeEvents = MutableSharedFlow<SessionDirectoryEvent>(extraBufferCapacity = 10)
    val directoryChangeEvents: SharedFlow<SessionDirectoryEvent> = _directoryChangeEvents

    private val _terminalState = MutableStateFlow(TerminalState(sessions = emptyList()))
    val terminalState: StateFlow<TerminalState> = _terminalState

    // 会话管理
    private val _sessions = MutableStateFlow<List<TerminalSession>>(emptyList())
    val sessions: StateFlow<List<TerminalSession>> = _sessions

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId

    private val _currentDirectory = MutableStateFlow(context.filesDir.absolutePath)
    val currentDirectory: StateFlow<String> = _currentDirectory

    private val _isInteractiveMode = MutableStateFlow(false)
    val isInteractiveMode: StateFlow<Boolean> = _isInteractiveMode

    private val _interactivePrompt = MutableStateFlow("")
    val interactivePrompt: StateFlow<String> = _interactivePrompt

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen

    /**
     * 初始化环境 - 车机版使用 Android 原生 shell，无需初始化 Linux 环境
     */
    suspend fun initializeEnvironment(): Boolean {
        AppLogger.d(TAG, "车机版终端初始化（使用 Android 原生 shell）")
        return true
    }

    fun cleanup() {
        AppLogger.d(TAG, "终端管理器清理")
    }

    fun createNewSession(title: String? = null): TerminalSession {
        val sessionId = "session_${System.currentTimeMillis()}"
        val session = TerminalSession(
            id = sessionId,
            title = title ?: "终端",
            currentDirectory = context.filesDir.absolutePath
        )
        _sessions.value = _sessions.value + session
        _currentSessionId.value = sessionId
        // 同步更新 terminalState 中的 sessions
        _terminalState.value = _terminalState.value.copy(sessions = _sessions.value)
        AppLogger.d(TAG, "创建新会话: $sessionId")
        return session
    }

    fun switchToSession(sessionId: String) {
        _currentSessionId.value = sessionId
        val session = _sessions.value.find { it.id == sessionId }
        session?.let {
            _currentDirectory.value = it.currentDirectory
        }
    }

    fun closeSession(sessionId: String) {
        _sessions.value = _sessions.value.filterNot { it.id == sessionId }
        if (_currentSessionId.value == sessionId) {
            _currentSessionId.value = _sessions.value.firstOrNull()?.id
        }
        // 同步更新 terminalState 中的 sessions
        _terminalState.value = _terminalState.value.copy(sessions = _sessions.value)
    }

    /**
     * 发送命令到指定会话 - 使用 Android 原生 shell 执行
     */
    fun sendCommandToSession(sessionId: String, command: String, commandId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                // 发送开始事件
                _commandExecutionEvents.emit(
                    CommandExecutionEvent(
                        sessionId = sessionId,
                        commandId = commandId,
                        outputChunk = "",
                        isCompleted = false,
                        exitCode = null
                    )
                )

                // 使用 Android 原生 shell 执行命令
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val stdout = BufferedReader(InputStreamReader(process.inputStream))
                val stderr = BufferedReader(InputStreamReader(process.errorStream))

                val output = StringBuilder()
                var line: String?

                while (stdout.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                    _commandExecutionEvents.emit(
                        CommandExecutionEvent(
                            sessionId = sessionId,
                            commandId = commandId,
                            outputChunk = line + "\n",
                            isCompleted = false,
                            exitCode = null
                        )
                    )
                }

                while (stderr.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                    _commandExecutionEvents.emit(
                        CommandExecutionEvent(
                            sessionId = sessionId,
                            commandId = commandId,
                            outputChunk = line + "\n",
                            isCompleted = false,
                            exitCode = null
                        )
                    )
                }

                val exitCode = process.waitFor()

                // 发送完成事件
                _commandExecutionEvents.emit(
                    CommandExecutionEvent(
                        sessionId = sessionId,
                        commandId = commandId,
                        outputChunk = output.toString(),
                        isCompleted = true,
                        exitCode = exitCode
                    )
                )

                AppLogger.d(TAG, "命令执行完成，退出码: $exitCode")
            } catch (e: Exception) {
                AppLogger.e(TAG, "命令执行失败", e)
                _commandExecutionEvents.emit(
                    CommandExecutionEvent(
                        sessionId = sessionId,
                        commandId = commandId,
                        outputChunk = "错误: ${e.message}\n",
                        isCompleted = true,
                        exitCode = -1
                    )
                )
            }
        }
    }

    fun sendInput(input: String) {
        // 简化实现：忽略交互输入
        AppLogger.d(TAG, "发送输入（简化实现）: $input")
    }

    fun sendInterruptSignal() {
        AppLogger.d(TAG, "发送中断信号")
    }

    /**
     * 执行隐藏命令 - 使用 Android 原生 shell
     */
    suspend fun executeHiddenCommand(
        command: String,
        executorKey: String = "default",
        timeoutMs: Long = 120000L
    ): HiddenExecResult = withContext(Dispatchers.IO) {
        try {
            AppLogger.d(TAG, "执行隐藏命令: $command")
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val exitCode = process.waitFor()

            HiddenExecResult(
                exitCode = exitCode,
                stdout = stdout,
                stderr = stderr,
                isTimedOut = false
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "隐藏命令执行失败", e)
            HiddenExecResult(
                exitCode = -1,
                stdout = "",
                stderr = e.message ?: "Unknown error",
                isTimedOut = false
            )
        }
    }
}

/**
 * 终端会话数据类
 */
data class TerminalSession(
    val id: String,
    val title: String,
    val currentDirectory: String
)

/**
 * 命令执行事件
 */
data class CommandExecutionEvent(
    val sessionId: String,
    val commandId: String,
    val outputChunk: String,
    val isCompleted: Boolean,
    val exitCode: Int?
)

/**
 * 会话目录变更事件
 */
data class SessionDirectoryEvent(
    val sessionId: String,
    val directory: String
)
