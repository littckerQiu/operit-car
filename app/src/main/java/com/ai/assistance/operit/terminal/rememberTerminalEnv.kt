package com.ai.assistance.operit.terminal

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 终端环境（车机版简化实现）
 */
data class TerminalEnv(
    val context: Context,
    val terminalManager: TerminalManager,
    val forceShowSetup: Boolean = false
)

val LocalTerminalEnv = staticCompositionLocalOf<TerminalEnv?> { null }

/**
 * 记住终端环境
 * 兼容原始 API：支持 terminalManager 和 forceShowSetup 参数
 */
@Composable
fun rememberTerminalEnv(
    terminalManager: TerminalManager? = null,
    forceShowSetup: Boolean = false
): TerminalEnv {
    val context = androidx.compose.ui.platform.LocalContext.current
    val manager = terminalManager ?: TerminalManager.getInstance(context)
    return remember(context, manager, forceShowSetup) {
        TerminalEnv(
            context = context.applicationContext,
            terminalManager = manager,
            forceShowSetup = forceShowSetup
        )
    }
}
