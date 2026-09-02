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
}
