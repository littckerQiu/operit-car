package com.ai.assistance.operit.terminal.view.domain.ansi

/**
 * 终端字符（车机版简化实现）
 */
data class TerminalChar(
    val char: Char = ' ',
    val foregroundColor: Int = 0,
    val backgroundColor: Int = 0,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false
)
