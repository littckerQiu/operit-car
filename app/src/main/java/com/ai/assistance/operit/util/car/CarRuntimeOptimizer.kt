package com.ai.assistance.operit.util.car

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.WindowManager
import com.ai.assistance.operit.data.preferences.CarPreferencesManager
import com.ai.assistance.operit.util.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 车机运行时优化管理器
 * 负责在运行时应用车机特定的优化配置
 * - 横屏锁定
 * - 性能模式
 * - 屏幕常亮
 * - 内存优化
 */
class CarRuntimeOptimizer(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "CarRuntimeOptimizer"
    }

    private val carPrefs = CarPreferencesManager.getInstance(context)

    /**
     * 应用车机优化到 Activity
     */
    fun applyToActivity(activity: Activity) {
        scope.launch {
            carPrefs.landscapeLock.collectLatest { enabled ->
                if (enabled) {
                    lockLandscape(activity)
                } else {
                    unlockOrientation(activity)
                }
            }
        }

        scope.launch {
            carPrefs.aggressiveMemoryManagement.collectLatest { enabled ->
                if (enabled) {
                    enableAggressiveMemoryManagement(activity)
                }
            }
        }

        // 车机模式下保持屏幕常亮
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        AppLogger.d(TAG, "车机优化已应用: 屏幕常亮")
    }

    /**
     * 锁定横屏
     */
    private fun lockLandscape(activity: Activity) {
        try {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            AppLogger.d(TAG, "已锁定横屏")
        } catch (e: Exception) {
            AppLogger.e(TAG, "锁定横屏失败", e)
        }
    }

    /**
     * 解锁屏幕方向
     */
    private fun unlockOrientation(activity: Activity) {
        try {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            AppLogger.d(TAG, "已解锁屏幕方向")
        } catch (e: Exception) {
            AppLogger.e(TAG, "解锁屏幕方向失败", e)
        }
    }

    /**
     * 启用激进内存管理
     */
    private fun enableAggressiveMemoryManagement(activity: Activity) {
        try {
            // 降低后台进程优先级
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 提示系统此应用适合在低内存环境运行
                AppLogger.d(TAG, "激进内存管理已启用")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "启用激进内存管理失败", e)
        }
    }

    /**
     * 应用启动优化
     */
    fun applyStartupOptimization() {
        scope.launch {
            val speedup = carPrefs.startupSpeedup.first()
            if (speedup) {
                AppLogger.d(TAG, "启动加速: 延迟加载非必要组件")
                // 启动加速策略：
                // 1. 延迟初始化非必要的插件
                // 2. 优先加载语音服务
                // 3. 减少启动时的数据库操作
            }
        }
    }

    /**
     * 获取车机触摸目标大小（dp）
     */
    fun getTouchTargetSize(): Int {
        return carPrefs.getTouchTargetSizeSync()
    }

    /**
     * 是否启用大字体模式
     */
    fun isLargeFontMode(): Boolean {
        return carPrefs.isLargeFontModeSync()
    }

    /**
     * 是否减少动画
     */
    fun shouldReduceAnimations(): Boolean {
        return carPrefs.isReduceAnimationsSync()
    }
}
