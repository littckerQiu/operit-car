package com.ai.assistance.operit.terminal.provider.filesystem

import android.content.Context
import com.ai.assistance.operit.util.AppLogger
import java.io.File
import java.io.RandomAccessFile

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
                    lastModified = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified())),
                    permissions = getFilePermissions(file)
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

    /**
     * 读取文件样本（前 sampleSize 字节）
     */
    fun readFileSample(path: String, sampleSize: Int): ByteArray? {
        return try {
            val file = File(path)
            if (!file.exists() || !file.isFile) return null
            RandomAccessFile(file, "r").use { raf ->
                val size = minOf(sampleSize.toLong(), raf.length()).toInt()
                val buffer = ByteArray(size)
                raf.readFully(buffer)
                buffer
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "读取文件样本失败", e)
            null
        }
    }

    /**
     * 读取文件（限制最大字节数）
     */
    fun readFileWithLimit(path: String, maxBytes: Int): String? {
        return try {
            val file = File(path)
            if (!file.exists() || !file.isFile) return null
            RandomAccessFile(file, "r").use { raf ->
                val size = minOf(maxBytes.toLong(), raf.length()).toInt()
                val buffer = ByteArray(size)
                raf.readFully(buffer)
                String(buffer, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "读取文件（限制大小）失败", e)
            null
        }
    }

    /**
     * 获取文件行数
     */
    fun getLineCount(path: String): Int {
        return try {
            val file = File(path)
            if (!file.exists() || !file.isFile) return 0
            var count = 0
            file.useLines { count = it.count() }
            count
        } catch (e: Exception) {
            AppLogger.e(TAG, "获取文件行数失败", e)
            0
        }
    }

    /**
     * 读取指定行范围的文件内容
     */
    fun readFileLines(path: String, startLine: Int, endLine: Int): String? {
        return try {
            val file = File(path)
            if (!file.exists() || !file.isFile) return null
            val lines = file.readLines()
            val from = startLine.coerceAtLeast(0)
            val to = endLine.coerceAtMost(lines.size)
            if (from >= to) return ""
            lines.subList(from, to).joinToString("\n")
        } catch (e: Exception) {
            AppLogger.e(TAG, "读取文件行失败", e)
            null
        }
    }

    fun writeFile(path: String, content: String): FileOperationResult {
        return writeFile(path, content, false)
    }

    /**
     * 写入文件（支持追加模式）
     */
    fun writeFile(path: String, content: String, append: Boolean): FileOperationResult {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            if (append) {
                file.appendText(content)
            } else {
                file.writeText(content)
            }
            FileOperationResult(success = true, message = "File written successfully")
        } catch (e: Exception) {
            AppLogger.e(TAG, "写入文件失败", e)
            FileOperationResult(success = false, message = e.message ?: "Write failed")
        }
    }

    fun writeFileBytes(path: String, bytes: ByteArray): FileOperationResult {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)
            FileOperationResult(success = true, message = "File written successfully")
        } catch (e: Exception) {
            AppLogger.e(TAG, "写入文件字节失败", e)
            FileOperationResult(success = false, message = e.message ?: "Write failed")
        }
    }

    fun createDirectory(path: String, createParents: Boolean = true): FileOperationResult {
        return try {
            val dir = File(path)
            val result = if (createParents) dir.mkdirs() else dir.mkdir()
            if (result) {
                FileOperationResult(success = true, message = "Directory created successfully")
            } else {
                FileOperationResult(success = false, message = "Directory creation failed")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "创建目录失败", e)
            FileOperationResult(success = false, message = e.message ?: "Create directory failed")
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

    /**
     * 删除文件或目录
     */
    fun delete(path: String, recursive: Boolean = true): FileOperationResult {
        return try {
            val file = File(path)
            val result = if (recursive) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
            if (result) {
                FileOperationResult(success = true, message = "File deleted successfully")
            } else {
                FileOperationResult(success = false, message = "Delete failed")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "删除失败", e)
            FileOperationResult(success = false, message = e.message ?: "Delete failed")
        }
    }

    /**
     * 移动文件或目录
     */
    fun move(sourcePath: String, destPath: String): FileOperationResult {
        return try {
            val source = File(sourcePath)
            val dest = File(destPath)
            dest.parentFile?.mkdirs()
            val result = source.renameTo(dest)
            if (result) {
                FileOperationResult(success = true, message = "File moved successfully")
            } else {
                FileOperationResult(success = false, message = "Move failed")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "移动文件失败", e)
            FileOperationResult(success = false, message = e.message ?: "Move failed")
        }
    }

    /**
     * 复制文件或目录
     */
    fun copy(sourcePath: String, destPath: String, recursive: Boolean = true): FileOperationResult {
        return try {
            val source = File(sourcePath)
            val dest = File(destPath)
            if (!source.exists()) {
                return FileOperationResult(success = false, message = "Source not found")
            }
            dest.parentFile?.mkdirs()
            if (source.isDirectory && recursive) {
                source.copyRecursively(dest, overwrite = true)
            } else {
                source.copyTo(dest, overwrite = true)
            }
            FileOperationResult(success = true, message = "File copied successfully")
        } catch (e: Exception) {
            AppLogger.e(TAG, "复制文件失败", e)
            FileOperationResult(success = false, message = e.message ?: "Copy failed")
        }
    }

    fun fileExists(path: String): Boolean = exists(path)

    fun getBaseDirectory(): String = baseDir.absolutePath

    fun getAbsolutePath(path: String): String {
        return File(path).absolutePath
    }

    /**
     * 搜索文件
     */
    fun findFiles(basePath: String, pattern: String, maxDepth: Int = -1, caseInsensitive: Boolean = false): List<String> {
        return try {
            val baseDir = File(basePath)
            if (!baseDir.exists() || !baseDir.isDirectory) return emptyList()

            val regex = if (caseInsensitive) {
                Regex(pattern, RegexOption.IGNORE_CASE)
            } else {
                Regex(pattern)
            }

            val results = mutableListOf<String>()
            baseDir.walkTopDown().maxDepth(if (maxDepth < 0) Int.MAX_VALUE else maxDepth).forEach { file ->
                if (file.isFile && regex.containsMatchIn(file.name)) {
                    results.add(file.absolutePath)
                }
            }
            results
        } catch (e: Exception) {
            AppLogger.e(TAG, "搜索文件失败", e)
            emptyList()
        }
    }

    /**
     * 获取文件信息
     */
    fun getFileInfo(path: String): FileInfo? {
        return try {
            val file = File(path)
            if (!file.exists()) return null
            FileInfo(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = file.length(),
                lastModified = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified())),
                permissions = getFilePermissions(file)
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "获取文件信息失败", e)
            null
        }
    }

    /**
     * 获取文件权限字符串（简化实现）
     */
    private fun getFilePermissions(file: File): String {
        return buildString {
            append(if (file.canRead()) 'r' else '-')
            append(if (file.canWrite()) 'w' else '-')
            append(if (file.canExecute()) 'x' else '-')
        }
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
    val lastModified: String,
    val permissions: String = "rw-"
)

/**
 * 文件操作结果
 */
data class FileOperationResult(
    val success: Boolean,
    val message: String
)
