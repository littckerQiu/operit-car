package com.arthenica.ffmpegkit

import android.util.Log

/**
 * FFmpegKit stub（车机版）
 * 车机版未集成 FFmpeg，所有操作返回失败
 */
object FFmpegKit {
    private const val TAG = "FFmpegKitStub"

    fun execute(command: String): FFmpegSession {
        Log.w(TAG, "FFmpeg not available in car edition, command: $command")
        return FFmpegSession(
            command = command,
            returnCode = ReturnCode.CANCEL,
            output = "FFmpeg not available",
            state = SessionState.FAILED
        )
    }

    fun executeAsync(command: String, callback: (FFmpegSession) -> Unit): FFmpegSession {
        val session = execute(command)
        callback(session)
        return session
    }

    fun cancel() {
        // no-op
    }

    fun cancel(sessionId: Long) {
        // no-op
    }
}
