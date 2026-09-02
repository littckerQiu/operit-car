package com.arthenica.ffmpegkit

import android.util.Log

/**
 * FFprobeKit stub（车机版）
 */
object FFprobeKit {
    private const val TAG = "FFprobeKitStub"

    fun getMediaInformation(filePath: String): MediaInformationSession {
        Log.w(TAG, "FFprobe not available in car edition, file: $filePath")
        return MediaInformationSession(
            command = "-i $filePath",
            returnCode = ReturnCode.CANCEL,
            output = "FFprobe not available",
            mediaInformation = null
        )
    }

    fun execute(command: String): FFprobeSession {
        return FFprobeSession(
            command = command,
            returnCode = ReturnCode.CANCEL,
            output = "FFprobe not available"
        )
    }
}

/**
 * MediaInformationSession stub
 */
class MediaInformationSession(
    val command: String = "",
    val returnCode: ReturnCode = ReturnCode.CANCEL,
    val output: String = "",
    val mediaInformation: MediaInformation? = null
)

/**
 * FFprobeSession stub
 */
class FFprobeSession(
    val command: String = "",
    val returnCode: ReturnCode = ReturnCode.CANCEL,
    val output: String = ""
)
