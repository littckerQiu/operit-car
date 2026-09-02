package com.ai.assistance.operit.ui.features.settings.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.operit.R
import com.ai.assistance.operit.data.preferences.CarPreferencesManager
import kotlinx.coroutines.launch

/**
 * 车机设置界面
 * 专为车载场景优化的设置选项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val carPrefs = remember { CarPreferencesManager.getInstance(context) }

    // 收集所有设置状态
    val mainInterfaceMode by carPrefs.mainInterfaceMode.collectAsState(
        initial = CarPreferencesManager.MainInterfaceMode.VOICE_FLOATING
    )
    val carModeEnabled by carPrefs.carModeEnabled.collectAsState(initial = true)
    val drivingSafetyMode by carPrefs.drivingSafetyMode.collectAsState(initial = true)
    val largeFontMode by carPrefs.largeFontMode.collectAsState(initial = true)
    val landscapeLock by carPrefs.landscapeLock.collectAsState(initial = true)
    val autoStartListening by carPrefs.autoStartListening.collectAsState(initial = true)
    val wakeWordEnabled by carPrefs.wakeWordEnabled.collectAsState(initial = true)
    val voiceFeedbackEnabled by carPrefs.voiceFeedbackEnabled.collectAsState(initial = true)
    val bluetoothHandsfree by carPrefs.bluetoothHandsfree.collectAsState(initial = true)
    val aggressiveMemory by carPrefs.aggressiveMemoryManagement.collectAsState(initial = true)
    val reduceAnimations by carPrefs.reduceAnimations.collectAsState(initial = false)
    val startupSpeedup by carPrefs.startupSpeedup.collectAsState(initial = true)
    val navigationIntegration by carPrefs.navigationIntegration.collectAsState(initial = true)
    val mediaControlEnabled by carPrefs.mediaControlEnabled.collectAsState(initial = true)
    val vehicleDataMcp by carPrefs.vehicleDataMcp.collectAsState(initial = true)
    val nightModeAuto by carPrefs.nightModeAuto.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "车机设置",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ===== 主界面模式 =====
            CarSettingsSection(title = "主界面", icon = Icons.Default.PhoneAndroid) {
                CarSettingsSwitchItem(
                    title = "语音悬浮窗作为主界面",
                    subtitle = "启动后直接进入车载语音交互界面",
                    icon = Icons.Default.Mic,
                    checked = mainInterfaceMode == CarPreferencesManager.MainInterfaceMode.VOICE_FLOATING,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            val mode = if (enabled)
                                CarPreferencesManager.MainInterfaceMode.VOICE_FLOATING
                            else
                                CarPreferencesManager.MainInterfaceMode.STANDARD
                            carPrefs.saveMainInterfaceMode(mode)
                            Toast.makeText(
                                context,
                                if (enabled) "已设置语音悬浮窗为主界面，重启应用生效" else "已切换为标准主界面",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            // ===== 驾驶安全 =====
            CarSettingsSection(title = "驾驶安全", icon = Icons.Default.CarCrash) {
                CarSettingsSwitchItem(
                    title = "驾驶安全模式",
                    subtitle = "行驶中限制复杂操作，优先语音交互",
                    icon = Icons.Default.CarCrash,
                    checked = drivingSafetyMode,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(drivingSafetyMode = it) } }
                )
                CarSettingsSwitchItem(
                    title = "横屏锁定",
                    subtitle = "固定横屏显示，适配车机屏幕",
                    icon = Icons.Default.Navigation,
                    checked = landscapeLock,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(landscapeLock = it) } }
                )
            }

            // ===== 语音交互 =====
            CarSettingsSection(title = "语音交互", icon = Icons.Default.Mic) {
                CarSettingsSwitchItem(
                    title = "自动开始聆听",
                    subtitle = "AI回复后自动进入下一轮语音识别",
                    icon = Icons.Default.Mic,
                    checked = autoStartListening,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(autoStartListening = it) } }
                )
                CarSettingsSwitchItem(
                    title = "语音唤醒",
                    subtitle = "支持唤醒词启动语音助手",
                    icon = Icons.Default.Mic,
                    checked = wakeWordEnabled,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(wakeWordEnabled = it) } }
                )
                CarSettingsSwitchItem(
                    title = "语音反馈",
                    subtitle = "AI回复时自动语音播报",
                    icon = Icons.Default.VolumeUp,
                    checked = voiceFeedbackEnabled,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(voiceFeedbackEnabled = it) } }
                )
                CarSettingsSwitchItem(
                    title = "蓝牙免提",
                    subtitle = "通过车载蓝牙进行语音通话和播放",
                    icon = Icons.Default.PhoneAndroid,
                    checked = bluetoothHandsfree,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(bluetoothHandsfree = it) } }
                )
            }

            // ===== 显示优化 =====
            CarSettingsSection(title = "显示优化", icon = Icons.Default.Palette) {
                CarSettingsSwitchItem(
                    title = "大字体模式",
                    subtitle = "增大界面文字和按钮，便于驾驶中查看",
                    icon = Icons.Default.Palette,
                    checked = largeFontMode,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(largeFontMode = it) } }
                )
                CarSettingsSwitchItem(
                    title = "自动夜间模式",
                    subtitle = "根据环境光自动切换深色主题",
                    icon = Icons.Default.Palette,
                    checked = nightModeAuto,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(nightModeAuto = it) } }
                )
                CarSettingsSwitchItem(
                    title = "减少动画",
                    subtitle = "降低动画效果，提升响应速度和性能",
                    icon = Icons.Default.Speed,
                    checked = reduceAnimations,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(reduceAnimations = it) } }
                )
            }

            // ===== 性能优化 =====
            CarSettingsSection(title = "性能优化", icon = Icons.Default.Speed) {
                CarSettingsSwitchItem(
                    title = "激进内存管理",
                    subtitle = "更频繁地回收内存，适应车机有限资源",
                    icon = Icons.Default.Memory,
                    checked = aggressiveMemory,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(aggressiveMemoryManagement = it) } }
                )
                CarSettingsSwitchItem(
                    title = "启动加速",
                    subtitle = "延迟加载非必要组件，加快冷启动速度",
                    icon = Icons.Default.Speed,
                    checked = startupSpeedup,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(startupSpeedup = it) } }
                )
            }

            // ===== 车载功能 =====
            CarSettingsSection(title = "车载功能", icon = Icons.Default.DirectionsCar) {
                CarSettingsSwitchItem(
                    title = "导航集成",
                    subtitle = "支持通过语音控制导航应用",
                    icon = Icons.Default.Navigation,
                    checked = navigationIntegration,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(navigationIntegration = it) } }
                )
                CarSettingsSwitchItem(
                    title = "媒体控制",
                    subtitle = "支持语音控制音乐播放",
                    icon = Icons.Default.VolumeUp,
                    checked = mediaControlEnabled,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(mediaControlEnabled = it) } }
                )
                CarSettingsSwitchItem(
                    title = "车辆数据 MCP",
                    subtitle = "通过 MCP 协议访问车辆数据（车速、电量等）",
                    icon = Icons.Default.DirectionsCar,
                    checked = vehicleDataMcp,
                    onCheckedChange = { coroutineScope.launch { carPrefs.saveCarSettings(vehicleDataMcp = it) } }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 版本信息
            Text(
                text = "Operit Car Edition 车机定制版",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 车机设置分组
 */
@Composable
private fun CarSettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

/**
 * 车机设置开关项
 */
@Composable
private fun CarSettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
