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
    val properties: Map<String, Any> = emptyMap()
) {
    fun getIndex(): String? = index
    fun getType(): String? = type
    fun getCodec(): String? = codec
    fun getWidth(): String? = width
    fun getHeight(): String? = height
    fun getStringProperty(key: String): String? = properties[key] as? String
}

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
    val streams: List<StreamInformation> = emptyList(),
    val properties: Map<String, Any> = emptyMap()
) {
    fun getDuration(): Long = duration
    fun getSize(): Long = size
    fun getBitrate(): Long = bitrate
    fun getFormat(): String = format
    fun getFilePath(): String = filePath
    fun getStreams(): List<StreamInformation> = streams
    fun getStringProperty(key: String): String? = properties[key] as? String
    fun getLongProperty(key: String): Long? = properties[key] as? Long
}
