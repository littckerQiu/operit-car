package com.arthenica.ffmpegkit

/**
 * FFmpegKit ReturnCode stub（车机版）
 * 车机版未集成 FFmpeg，所有操作返回失败
 */
class ReturnCode(val value: Int) {
    companion object {
        val SUCCESS = ReturnCode(0)
        val CANCEL = ReturnCode(255)

        fun isSuccess(returnCode: ReturnCode?): Boolean {
            return returnCode?.value == 0
        }

        fun isCancel(returnCode: ReturnCode?): Boolean {
            return returnCode?.value == 255
        }
    }
}
