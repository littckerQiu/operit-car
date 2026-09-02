package com.ai.assistance.operit.terminal.provider.filesystem

import android.content.Context
import com.ai.assistance.operit.util.AppLogger
import java.io.File

/**
 * 文件系统提供者（车机版简化实现）
 * 使用 Android 本地文件系统，不依赖 Linux 环境
 * 兼容原始 API
 */
class FileSystemProvider(private val context: Context) {

    companion object {
        private const val TAG = "FileSystemProvider"
    }

    private val baseDir = context.filesDir

    fun exists(path: String): Boolean {
        return try {
            File(path).exists()
        } catch (e: Exception) {
            false
        }
    }

    fun isDirectory(path: String): Boolean {
        return try {
            File(path).isDirectory
        } catch (e: Exception) {
            false
        }
    }

    fun isFile(path: String): Boolean {
        return try {
            File(path).isFile
        } catch (e: Exception) {
            false
        }
    }

    fun getFileSize(path: String): Long {
        return try {
            File(path).length()
        } catch (e: Exception) {
            0L
        }
    }

    fun listFiles(path: String): List<File> {
        return try {
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "列出文件失败", e)
            emptyList()
        }
    }

    fun listDirectory(path: String): List<FileInfo>? {
        return try {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) {
                return null
            }
            dir.listFiles()?.map { file ->
                FileInfo(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    size = file.length(),
                    lastModified = file.lastModified()
                )
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "列出目录失败", e)
            null
        }
    }

    fun readFile(path: String): String? {
        return try {
            File(path).takeIf { it.exists() && it.isFile }?.readText()
        } catch (e: Exception) {
            AppLogger.e(TAG, "读取文件失败", e)
            null
        }
    }

    fun readFileBytes(path: String): ByteArray? {
        return try {
            File(path).takeIf { it.exists() && it.isFile }?.readBytes()
        } catch (e: Exception) {
            AppLogger.e(TAG, "读取文件字节失败", e)
            null
        }
    }

    fun writeFile(path: String, content: String): Boolean {
        return try {
            File(path).writeText(content)
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "写入文件失败", e)
            false
        }
    }

    fun writeFileBytes(path: String, bytes: ByteArray): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "写入文件字节失败", e)
            false
        }
    }

    fun createDirectory(path: String, createParents: Boolean = true): Boolean {
        return try {
            val dir = File(path)
            if (createParents) dir.mkdirs() else dir.mkdir()
        } catch (e: Exception) {
            AppLogger.e(TAG, "创建目录失败", e)
            false
        }
    }

    fun deleteFile(path: String): Boolean {
        return try {
            File(path).deleteRecursively()
        } catch (e: Exception) {
            AppLogger.e(TAG, "删除文件失败", e)
            false
        }
    }

    fun fileExists(path: String): Boolean = exists(path)

    fun getBaseDirectory(): String = baseDir.absolutePath

    fun getAbsolutePath(path: String): String {
        return File(path).absolutePath
    }
}

/**
 * 文件信息
 */
data class FileInfo(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)
