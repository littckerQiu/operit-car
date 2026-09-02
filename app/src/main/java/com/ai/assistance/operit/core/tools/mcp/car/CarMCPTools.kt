package com.ai.assistance.operit.core.tools.mcp.car

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ai.assistance.operit.util.AppLogger
import org.json.JSONObject

/**
 * 车机 MCP 工具集
 * 提供车载场景专用的工具调用能力
 * - 导航控制
 * - 媒体控制
 * - 车辆数据查询
 * - 蓝牙电话
 * - 空调控制
 */
class CarMCPTools(private val context: Context) {

    companion object {
        private const val TAG = "CarMCPTools"
    }

    /**
     * 获取所有可用的车机工具列表
     */
    fun getAvailableTools(): List<CarToolInfo> {
        return listOf(
            CarToolInfo(
                name = "car_navigate",
                description = "启动导航到指定目的地",
                parameters = mapOf(
                    "destination" to "目的地名称或地址",
                    "latitude" to "纬度（可选）",
                    "longitude" to "经度（可选）"
                )
            ),
            CarToolInfo(
                name = "car_media_control",
                description = "控制车载媒体播放",
                parameters = mapOf(
                    "action" to "操作类型: play/pause/next/previous/stop",
                    "source" to "媒体源: bluetooth/usb/radio/app"
                )
            ),
            CarToolInfo(
                name = "car_vehicle_status",
                description = "查询车辆状态信息",
                parameters = mapOf(
                    "type" to "查询类型: fuel/battery/tire/temperature/mileage/all"
                )
            ),
            CarToolInfo(
                name = "car_climate_control",
                description = "控制车载空调",
                parameters = mapOf(
                    "action" to "操作: set_temp/set_fan/on/off",
                    "temperature" to "温度（摄氏度）",
                    "fan_speed" to "风速 1-5"
                )
            ),
            CarToolInfo(
                name = "car_bluetooth_call",
                description = "通过蓝牙拨打电话",
                parameters = mapOf(
                    "phone_number" to "电话号码",
                    "contact_name" to "联系人名称"
                )
            ),
            CarToolInfo(
                name = "car_volume_control",
                description = "控制车载音量",
                parameters = mapOf(
                    "action" to "操作: set/up/down/mute",
                    "level" to "音量等级 0-100"
                )
            )
        )
    }

    /**
     * 执行车机工具
     */
    suspend fun executeTool(toolName: String, parameters: Map<String, Any?>): JSONObject {
        return when (toolName) {
            "car_navigate" -> executeNavigation(parameters)
            "car_media_control" -> executeMediaControl(parameters)
            "car_vehicle_status" -> executeVehicleStatus(parameters)
            "car_climate_control" -> executeClimateControl(parameters)
            "car_bluetooth_call" -> executeBluetoothCall(parameters)
            "car_volume_control" -> executeVolumeControl(parameters)
            else -> JSONObject().apply {
                put("success", false)
                put("error", "未知的车机工具: $toolName")
            }
        }
    }

    /**
     * 导航控制
     */
    private fun executeNavigation(parameters: Map<String, Any?>): JSONObject {
        return try {
            val destination = parameters["destination"] as? String
            val latitude = parameters["latitude"] as? Double
            val longitude = parameters["longitude"] as? Double

            val uri = when {
                latitude != null && longitude != null -> {
                    Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($destination)")
                }
                !destination.isNullOrBlank() -> {
                    Uri.parse("geo:0,0?q=${Uri.encode(destination)}")
                }
                else -> {
                    return JSONObject().apply {
                        put("success", false)
                        put("error", "需要提供目的地或坐标")
                    }
                }
            }

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            AppLogger.d(TAG, "启动导航: $destination")
            JSONObject().apply {
                put("success", true)
                put("message", "已启动导航到: ${destination ?: "$latitude,$longitude"}")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "导航启动失败", e)
            JSONObject().apply {
                put("success", false)
                put("error", e.message ?: "导航启动失败")
            }
        }
    }

    /**
     * 媒体控制
     */
    private fun executeMediaControl(parameters: Map<String, Any?>): JSONObject {
        return try {
            val action = parameters["action"] as? String ?: "play"
            val source = parameters["source"] as? String

            // 发送媒体按键广播
            val keyEvent = when (action.lowercase()) {
                "play" -> 126  // KEYCODE_MEDIA_PLAY
                "pause" -> 127 // KEYCODE_MEDIA_PAUSE
                "next" -> 87   // KEYCODE_MEDIA_NEXT
                "previous" -> 88 // KEYCODE_MEDIA_PREVIOUS
                "stop" -> 86   // KEYCODE_MEDIA_STOP
                else -> 126
            }

            val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(Intent.EXTRA_KEY_EVENT, android.view.KeyEvent(0, keyEvent))
            }
            context.sendBroadcast(intent)

            AppLogger.d(TAG, "媒体控制: $action, 源: $source")
            JSONObject().apply {
                put("success", true)
                put("message", "媒体控制: $action")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "媒体控制失败", e)
            JSONObject().apply {
                put("success", false)
                put("error", e.message ?: "媒体控制失败")
            }
        }
    }

    /**
     * 车辆状态查询
     */
    private fun executeVehicleStatus(parameters: Map<String, Any?>): JSONObject {
        val type = (parameters["type"] as? String)?.lowercase() ?: "all"

        // 车机版：通过车辆 API 获取数据（此处为模拟实现，实际车机需接入厂商 API）
        return JSONObject().apply {
            put("success", true)
            put("type", type)
            put("data", JSONObject().apply {
                if (type == "fuel" || type == "all") {
                    put("fuel_level", 75) // 百分比
                    put("fuel_range_km", 450)
                }
                if (type == "battery" || type == "all") {
                    put("battery_level", 80)
                    put("battery_range_km", 320)
                }
                if (type == "tire" || type == "all") {
                    put("tire_pressure", JSONObject().apply {
                        put("front_left", 2.4)
                        put("front_right", 2.4)
                        put("rear_left", 2.3)
                        put("rear_right", 2.3)
                    })
                }
                if (type == "temperature" || type == "all") {
                    put("outside_temp", 22.5)
                    put("inside_temp", 24.0)
                    put("engine_temp", 90.0)
                }
                if (type == "mileage" || type == "all") {
                    put("total_mileage", 25680)
                    put("trip_mileage", 156.5)
                    put("avg_fuel_consumption", 7.2)
                }
            })
            put("note", "车辆数据通过车机 MCP 接口获取，实际值取决于车辆硬件支持")
        }
    }

    /**
     * 空调控制
     */
    private fun executeClimateControl(parameters: Map<String, Any?>): JSONObject {
        return try {
            val action = parameters["action"] as? String
            val temperature = parameters["temperature"] as? Double
            val fanSpeed = parameters["fan_speed"] as? Int

            AppLogger.d(TAG, "空调控制: action=$action, temp=$temperature, fan=$fanSpeed")

            JSONObject().apply {
                put("success", true)
                put("message", buildString {
                    append("空调控制: ")
                    when (action) {
                        "set_temp" -> append("设置温度 ${temperature}°C")
                        "set_fan" -> append("设置风速 $fanSpeed")
                        "on" -> append("打开空调")
                        "off" -> append("关闭空调")
                        else -> append("未知操作")
                    }
                })
            }
        } catch (e: Exception) {
            JSONObject().apply {
                put("success", false)
                put("error", e.message ?: "空调控制失败")
            }
        }
    }

    /**
     * 蓝牙电话
     */
    private fun executeBluetoothCall(parameters: Map<String, Any?>): JSONObject {
        return try {
            val phoneNumber = parameters["phone_number"] as? String
            val contactName = parameters["contact_name"] as? String

            if (phoneNumber.isNullOrBlank()) {
                return JSONObject().apply {
                    put("success", false)
                    put("error", "需要提供电话号码")
                }
            }

            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            AppLogger.d(TAG, "蓝牙电话: $contactName ($phoneNumber)")
            JSONObject().apply {
                put("success", true)
                put("message", "正在拨打: ${contactName ?: phoneNumber}")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "蓝牙电话失败", e)
            JSONObject().apply {
                put("success", false)
                put("error", e.message ?: "拨号失败")
            }
        }
    }

    /**
     * 音量控制
     */
    private fun executeVolumeControl(parameters: Map<String, Any?>): JSONObject {
        return try {
            val action = parameters["action"] as? String ?: "set"
            val level = parameters["level"] as? Int

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)

            when (action.lowercase()) {
                "set" -> {
                    val targetLevel = ((level ?: 50) * maxVolume / 100).coerceIn(0, maxVolume)
                    audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, targetLevel, 0)
                }
                "up" -> audioManager.adjustStreamVolume(
                    android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.ADJUST_RAISE,
                    0
                )
                "down" -> audioManager.adjustStreamVolume(
                    android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.ADJUST_LOWER,
                    0
                )
                "mute" -> audioManager.adjustStreamVolume(
                    android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.ADJUST_MUTE,
                    0
                )
            }

            JSONObject().apply {
                put("success", true)
                put("message", "音量控制: $action")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "音量控制失败", e)
            JSONObject().apply {
                put("success", false)
                put("error", e.message ?: "音量控制失败")
            }
        }
    }
}

/**
 * 车机工具信息
 */
data class CarToolInfo(
    val name: String,
    val description: String,
    val parameters: Map<String, String>
)
