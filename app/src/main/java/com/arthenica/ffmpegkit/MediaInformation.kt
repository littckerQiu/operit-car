package com.arthenica.ffmpegkit

/**
 * 流信息
 */
class StreamInformation(
    val index: String? = null,
    val type: String? = null,
    val codec: String? = null,
    val width: String? = null,
    val height: String? = null,
    val bitrate: String? = null,
    val sampleRate: String? = null,
    val channels: String? = null,
    val allProperties: Map<String, Any>? = null,
    val properties: Map<String, Any> = emptyMap()
) {
    fun getStringProperty(key: String): String? = properties[key] as? String
}

/**
 * MediaInformation stub（车机版）
 */
class MediaInformation(
    val filePath: String = "",
    val duration: String? = null,
    val startTime: String? = null,
    val size: Long = 0,
    val bitrate: String? = null,
    val format: String? = null,
    val streams: List<StreamInformation> = emptyList(),
    val properties: Map<String, Any> = emptyMap()
) {
    fun getStringProperty(key: String): String? = properties[key] as? String
    fun getLongProperty(key: String): Long? = properties[key] as? Long
}
