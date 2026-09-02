package com.arthenica.ffmpegkit

/**
 * FFmpegSession stub（车机版）
 */
class FFmpegSession(
    val command: String = "",
    val returnCode: ReturnCode = ReturnCode.CANCEL,
    val output: String = "FFmpeg not available in car edition",
    val state: SessionState = SessionState.FAILED
)

enum class SessionState {
    CREATED,
    RUNNING,
    FAILED,
    COMPLETED
}
