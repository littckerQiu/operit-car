package com.ai.assistance.operit.terminal.data

import com.ai.assistance.operit.terminal.TerminalSession

/**
 * 终端状态（车机版简化实现）
 * 兼容原始 API：包含 sessions 列表
 */
data class TerminalState(
    val isConnected: Boolean = true,
    val isInitializing: Boolean = false,
    val initializationProgress: Float = 1f,
    val activeSessionsCount: Int = 0,
    val environmentReady: Boolean = true,
    val sessions: List<TerminalSession> = emptyList()
)
