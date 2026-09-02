package com.ai.assistance.operit.terminal.provider.type

/**
 * 隐藏命令执行结果（车机版简化实现）
 * 兼容原始 API：包含 State 枚举
 */
data class HiddenExecResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val isTimedOut: Boolean = false,
    val state: State = if (exitCode == 0 && !isTimedOut) State.SUCCESS else if (isTimedOut) State.TIMEOUT else State.FAILURE
) {
    enum class State {
        SUCCESS,
        FAILURE,
        TIMEOUT,
        UNKNOWN
    }

    val isSuccess: Boolean get() = state == State.SUCCESS

    /** 兼容原始 API：isOk 是 isSuccess 的别名 */
    val isOk: Boolean get() = isSuccess

    /** 合并输出（stdout + stderr） */
    val output: String
        get() = if (stderr.isBlank()) stdout else "$stdout\n$stderr".trim()

    /** 原始输出预览（前500字符） */
    val rawOutputPreview: String
        get() = output.take(500)

    /** 错误输出 */
    val error: String
        get() = stderr
}
