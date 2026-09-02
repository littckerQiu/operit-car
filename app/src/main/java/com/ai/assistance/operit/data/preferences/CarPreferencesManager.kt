package com.ai.assistance.operit.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * CarPreferencesManager
 * 车机版本专属偏好设置管理器
 * 管理车载模式、主界面选择、驾驶安全、车载UI优化等设置
 */
class CarPreferencesManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: CarPreferencesManager? = null

        fun getInstance(context: Context): CarPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = CarPreferencesManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        // ===== 主界面模式 =====
        /** 主界面模式：standard=标准聊天界面, voice_floating=语音悬浮窗界面 */
        private val KEY_MAIN_INTERFACE_MODE = stringPreferencesKey("car_main_interface_mode")

        // ===== 车载模式 =====
        private val KEY_CAR_MODE_ENABLED = booleanPreferencesKey("car_mode_enabled")
        private val KEY_DRIVING_SAFETY_MODE = booleanPreferencesKey("driving_safety_mode")
        private val KEY_LARGE_FONT_MODE = booleanPreferencesKey("large_font_mode")
        private val KEY_LANDSCAPE_LOCK = booleanPreferencesKey("landscape_lock")

        // ===== 语音交互 =====
        private val KEY_AUTO_START_LISTENING = booleanPreferencesKey("auto_start_listening")
        private val KEY_WAKE_WORD_ENABLED = booleanPreferencesKey("car_wake_word_enabled")
        private val KEY_VOICE_FEEDBACK_ENABLED = booleanPreferencesKey("voice_feedback_enabled")
        private val KEY_BLUETOOTH_HANDSFREE = booleanPreferencesKey("bluetooth_handsfree")

        // ===== 性能优化 =====
        private val KEY_AGGRESSIVE_MEMORY_MANAGEMENT = booleanPreferencesKey("aggressive_memory_management")
        private val KEY_REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
        private val KEY_STARTUP_SPEEDUP = booleanPreferencesKey("startup_speedup")

        // ===== 车载功能 =====
        private val KEY_NAVIGATION_INTEGRATION = booleanPreferencesKey("navigation_integration")
        private val KEY_MEDIA_CONTROL_ENABLED = booleanPreferencesKey("media_control_enabled")
        private val KEY_VEHICLE_DATA_MCP = booleanPreferencesKey("vehicle_data_mcp")

        // ===== 显示优化 =====
        private val KEY_NIGHT_MODE_AUTO = booleanPreferencesKey("night_mode_auto")
        private val KEY_SCREEN_BRIGHTNESS_OPTIMIZE = booleanPreferencesKey("screen_brightness_optimize")
        private val KEY_TOUCH_TARGET_SIZE = intPreferencesKey("touch_target_size")
    }

    // ===== 主界面模式 =====
    enum class MainInterfaceMode(val value: String) {
        STANDARD("standard"),
        VOICE_FLOATING("voice_floating");

        companion object {
            fun fromValue(value: String?): MainInterfaceMode =
                values().find { it.value == value } ?: STANDARD
        }
    }

    val mainInterfaceMode: Flow<MainInterfaceMode> =
        context.carPreferencesDataStore.data.map { preferences ->
            MainInterfaceMode.fromValue(preferences[KEY_MAIN_INTERFACE_MODE])
        }

    // ===== 车载模式 =====
    val carModeEnabled: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_CAR_MODE_ENABLED] ?: true }

    val drivingSafetyMode: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_DRIVING_SAFETY_MODE] ?: true }

    val largeFontMode: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_LARGE_FONT_MODE] ?: true }

    val landscapeLock: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_LANDSCAPE_LOCK] ?: true }

    // ===== 语音交互 =====
    val autoStartListening: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_AUTO_START_LISTENING] ?: true }

    val wakeWordEnabled: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_WAKE_WORD_ENABLED] ?: true }

    val voiceFeedbackEnabled: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_VOICE_FEEDBACK_ENABLED] ?: true }

    val bluetoothHandsfree: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_BLUETOOTH_HANDSFREE] ?: true }

    // ===== 性能优化 =====
    val aggressiveMemoryManagement: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_AGGRESSIVE_MEMORY_MANAGEMENT] ?: true }

    val reduceAnimations: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_REDUCE_ANIMATIONS] ?: false }

    val startupSpeedup: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_STARTUP_SPEEDUP] ?: true }

    // ===== 车载功能 =====
    val navigationIntegration: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_NAVIGATION_INTEGRATION] ?: true }

    val mediaControlEnabled: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_MEDIA_CONTROL_ENABLED] ?: true }

    val vehicleDataMcp: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_VEHICLE_DATA_MCP] ?: true }

    // ===== 显示优化 =====
    val nightModeAuto: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_NIGHT_MODE_AUTO] ?: true }

    val screenBrightnessOptimize: Flow<Boolean> =
        context.carPreferencesDataStore.data.map { it[KEY_SCREEN_BRIGHTNESS_OPTIMIZE] ?: true }

    val touchTargetSize: Flow<Int> =
        context.carPreferencesDataStore.data.map { it[KEY_TOUCH_TARGET_SIZE] ?: 56 }

    // ===== 同步获取方法（用于启动时快速读取） =====
    fun getMainInterfaceModeSync(): MainInterfaceMode = runBlocking {
        mainInterfaceMode.first()
    }

    fun isCarModeEnabledSync(): Boolean = runBlocking { carModeEnabled.first() }
    fun isDrivingSafetyModeSync(): Boolean = runBlocking { drivingSafetyMode.first() }
    fun isLargeFontModeSync(): Boolean = runBlocking { largeFontMode.first() }
    fun isLandscapeLockSync(): Boolean = runBlocking { landscapeLock.first() }
    fun isAutoStartListeningSync(): Boolean = runBlocking { autoStartListening.first() }
    fun isReduceAnimationsSync(): Boolean = runBlocking { reduceAnimations.first() }
    fun isStartupSpeedupSync(): Boolean = runBlocking { startupSpeedup.first() }
    fun getTouchTargetSizeSync(): Int = runBlocking { touchTargetSize.first() }

    // ===== 保存方法 =====
    suspend fun saveMainInterfaceMode(mode: MainInterfaceMode) {
        context.carPreferencesDataStore.edit { it[KEY_MAIN_INTERFACE_MODE] = mode.value }
    }

    suspend fun saveCarSettings(
        carModeEnabled: Boolean? = null,
        drivingSafetyMode: Boolean? = null,
        largeFontMode: Boolean? = null,
        landscapeLock: Boolean? = null,
        autoStartListening: Boolean? = null,
        wakeWordEnabled: Boolean? = null,
        voiceFeedbackEnabled: Boolean? = null,
        bluetoothHandsfree: Boolean? = null,
        aggressiveMemoryManagement: Boolean? = null,
        reduceAnimations: Boolean? = null,
        startupSpeedup: Boolean? = null,
        navigationIntegration: Boolean? = null,
        mediaControlEnabled: Boolean? = null,
        vehicleDataMcp: Boolean? = null,
        nightModeAuto: Boolean? = null,
        screenBrightnessOptimize: Boolean? = null,
        touchTargetSize: Int? = null
    ) {
        context.carPreferencesDataStore.edit { preferences ->
            carModeEnabled?.let { preferences[KEY_CAR_MODE_ENABLED] = it }
            drivingSafetyMode?.let { preferences[KEY_DRIVING_SAFETY_MODE] = it }
            largeFontMode?.let { preferences[KEY_LARGE_FONT_MODE] = it }
            landscapeLock?.let { preferences[KEY_LANDSCAPE_LOCK] = it }
            autoStartListening?.let { preferences[KEY_AUTO_START_LISTENING] = it }
            wakeWordEnabled?.let { preferences[KEY_WAKE_WORD_ENABLED] = it }
            voiceFeedbackEnabled?.let { preferences[KEY_VOICE_FEEDBACK_ENABLED] = it }
            bluetoothHandsfree?.let { preferences[KEY_BLUETOOTH_HANDSFREE] = it }
            aggressiveMemoryManagement?.let { preferences[KEY_AGGRESSIVE_MEMORY_MANAGEMENT] = it }
            reduceAnimations?.let { preferences[KEY_REDUCE_ANIMATIONS] = it }
            startupSpeedup?.let { preferences[KEY_STARTUP_SPEEDUP] = it }
            navigationIntegration?.let { preferences[KEY_NAVIGATION_INTEGRATION] = it }
            mediaControlEnabled?.let { preferences[KEY_MEDIA_CONTROL_ENABLED] = it }
            vehicleDataMcp?.let { preferences[KEY_VEHICLE_DATA_MCP] = it }
            nightModeAuto?.let { preferences[KEY_NIGHT_MODE_AUTO] = it }
            screenBrightnessOptimize?.let { preferences[KEY_SCREEN_BRIGHTNESS_OPTIMIZE] = it }
            touchTargetSize?.let { preferences[KEY_TOUCH_TARGET_SIZE] = it.coerceIn(40, 80) }
        }
    }

    suspend fun resetCarSettings() {
        context.carPreferencesDataStore.edit { preferences ->
            preferences[KEY_MAIN_INTERFACE_MODE] = MainInterfaceMode.VOICE_FLOATING.value
            preferences[KEY_CAR_MODE_ENABLED] = true
            preferences[KEY_DRIVING_SAFETY_MODE] = true
            preferences[KEY_LARGE_FONT_MODE] = true
            preferences[KEY_LANDSCAPE_LOCK] = true
            preferences[KEY_AUTO_START_LISTENING] = true
            preferences[KEY_WAKE_WORD_ENABLED] = true
            preferences[KEY_VOICE_FEEDBACK_ENABLED] = true
            preferences[KEY_BLUETOOTH_HANDSFREE] = true
            preferences[KEY_AGGRESSIVE_MEMORY_MANAGEMENT] = true
            preferences[KEY_REDUCE_ANIMATIONS] = false
            preferences[KEY_STARTUP_SPEEDUP] = true
            preferences[KEY_NAVIGATION_INTEGRATION] = true
            preferences[KEY_MEDIA_CONTROL_ENABLED] = true
            preferences[KEY_VEHICLE_DATA_MCP] = true
            preferences[KEY_NIGHT_MODE_AUTO] = true
            preferences[KEY_SCREEN_BRIGHTNESS_OPTIMIZE] = true
            preferences[KEY_TOUCH_TARGET_SIZE] = 56
        }
    }
}

private val Context.carPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "car_preferences"
)
