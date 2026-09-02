package com.arthenica.ffmpegkit

/**
 * MediaInformation stub（车机版）
 */
class MediaInformation(
    val filePath: String = "",
    val duration: Long = 0,
    val startTime: Long = 0,
    val size: Long = 0,
    val bitrate: Long = 0,
    val format: String = "",
    val properties: Map<String, Any> = emptyMap()
) {
    fun getDuration(): Long = duration
    fun getSize(): Long = size
    fun getBitrate(): Long = bitrate
    fun getFormat(): String = format
    fun getFilePath(): String = filePath
    fun getStringProperty(key: String): String? = properties[key] as? String
    fun getLongProperty(key: String): Long? = properties[key] as? Long
}
