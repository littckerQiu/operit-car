package com.ai.assistance.operit.terminal.utils

import android.content.Context
import com.ai.assistance.operit.terminal.provider.filesystem.FileSystemProvider
import com.ai.assistance.operit.util.AppLogger

/**
 * SSH 文件连接管理器（车机版简化实现）
 * 车机版不支持 SSH 连接，返回本地文件系统提供者
 */
class SSHFileConnectionManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SSHFileConnectionManager? = null
        private const val TAG = "SSHFileConnectionManager"

        fun getInstance(context: Context): SSHFileConnectionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SSHFileConnectionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val localFileSystemProvider = FileSystemProvider(context)

    fun isAvailable(): Boolean = false

    fun isConnected(): Boolean = false

    fun connect(host: String, port: Int, username: String, password: String): Boolean {
        AppLogger.w(TAG, "车机版不支持 SSH 连接，使用本地文件系统")
        return false
    }

    fun disconnect() {
        // 空实现
    }

    /**
     * 获取文件系统提供者
     * 车机版始终返回本地文件系统提供者
     */
    fun getFileSystemProvider(): FileSystemProvider {
        return localFileSystemProvider
    }
}
