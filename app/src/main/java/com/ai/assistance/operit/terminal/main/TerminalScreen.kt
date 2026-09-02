package com.ai.assistance.operit.terminal.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.operit.terminal.CommandExecutionEvent
import com.ai.assistance.operit.terminal.TerminalEnv
import com.ai.assistance.operit.terminal.TerminalManager
import kotlinx.coroutines.flow.collectLatest

/**
 * 终端屏幕（车机版简化实现）
 * 使用 Android 原生 shell，不依赖 Linux 环境
 * 兼容原始 API 参数
 */
@Composable
fun TerminalScreen(
    env: TerminalEnv? = null,
    useLocalImeHandling: Boolean = true,
    checkUpdatesOnEnter: Boolean = true,
    modifier: Modifier = Modifier,
    sessionId: String? = null
) {
    val context = LocalContext.current
    val terminalManager = remember(env) {
        env?.terminalManager ?: TerminalManager.getInstance(context)
    }
    var output by remember { mutableStateOf("车机版终端\n使用 Android 原生 shell\n（原 Linux 环境已移除）\n\n$ ") }

    LaunchedEffect(terminalManager) {
        terminalManager.commandExecutionEvents.collectLatest { event ->
            if (event.outputChunk.isNotEmpty()) {
                output += event.outputChunk
                if (event.isCompleted) {
                    output += "\n$ "
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = output,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
