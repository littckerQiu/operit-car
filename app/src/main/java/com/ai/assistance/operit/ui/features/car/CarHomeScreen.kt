package com.ai.assistance.operit.ui.features.car

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.operit.R
import com.ai.assistance.operit.api.chat.AIForegroundService
import com.ai.assistance.operit.api.speech.SpeechServiceFactory
import com.ai.assistance.operit.api.voice.VoiceServiceFactory
import com.ai.assistance.operit.data.preferences.CarPreferencesManager
import com.ai.assistance.operit.ui.floating.voice.SpeechInteractionManager
import com.ai.assistance.operit.ui.theme.OperitTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 车机主界面
 * 专为车载场景优化的语音交互主界面
 * - 大字体、大按钮，便于驾驶中操作
 * - 语音优先交互
 * - 驾驶安全模式
 * - 横屏优化布局
 */
@Composable
fun CarHomeScreen(
    onNavigateToChat: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val carPrefs = remember { CarPreferencesManager.getInstance(context) }

    // 车机设置状态
    val largeFont by carPrefs.largeFontMode.collectAsState(initial = true)
    val drivingSafety by carPrefs.drivingSafetyMode.collectAsState(initial = true)
    val autoListen by carPrefs.autoStartListening.collectAsState(initial = true)
    val reduceAnim by carPrefs.reduceAnimations.collectAsState(initial = false)

    // 语音交互状态
    var isListening by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    var userSpeechText by remember { mutableStateOf("") }
    var aiResponseText by remember { mutableStateOf("") }
    var volumeLevel by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("点击麦克风开始语音交互") }

    // 消息列表
    var messages by remember { mutableStateOf(listOf<CarChatMessage>()) }
    val listState = rememberLazyListState()

    // 语音管理器
    val speechManager = remember {
        SpeechInteractionManager(
            context = context,
            coroutineScope = coroutineScope,
            onSpeechResult = { text, isFinal ->
                if (isFinal && text.isNotBlank()) {
                    userSpeechText = text
                    messages = messages + CarChatMessage("user", text, System.currentTimeMillis())
                    isListening = false
                    isThinking = true
                    statusText = "正在思考..."
                    // 模拟AI回复（实际应接入AI服务）
                    coroutineScope.launch {
                        delay(1500)
                        val response = "好的，我已收到：$text"
                        aiResponseText = response
                        messages = messages + CarChatMessage("ai", response, System.currentTimeMillis())
                        isThinking = false
                        statusText = "点击麦克风继续"
                        // TTS播报
                        try {
                            VoiceServiceFactory.getInstance(context).speak(response, true)
                        } catch (_: Exception) {}
                        // 自动开始下一轮监听
                        if (autoListen) {
                            delay(800)
                            startCarListening(
                                speechManager = null,
                                onStart = { isListening = true; statusText = "正在聆听..." },
                                onText = { userSpeechText = it },
                                onVolume = { volumeLevel = it }
                            )
                        }
                    }
                }
            },
            onStateChange = { state ->
                statusText = state
            }
        )
    }

    // 初始化语音服务
    LaunchedEffect(Unit) {
        try {
            SpeechServiceFactory.getInstance(context).initialize()
            VoiceServiceFactory.getInstance(context).initialize()
        } catch (_: Exception) {}
        // 自动开始监听
        if (autoListen) {
            delay(1000)
            startCarListening(
                speechManager = null,
                onStart = { isListening = true; statusText = "正在聆听..." },
                onText = { userSpeechText = it },
                onVolume = { volumeLevel = it }
            )
        }
    }

    // 音量动画
    val animatedVolume by animateFloatAsState(
        targetValue = volumeLevel,
        animationSpec = tween(durationMillis = 100),
        label = "volume"
    )

    // 字体大小
    val titleSize = if (largeFont) 28.sp else 22.sp
    val bodySize = if (largeFont) 20.sp else 16.sp
    val buttonSize = if (largeFont) 72.dp else 56.dp

    OperitTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 顶部状态栏
                CarTopBar(
                    title = "车载语音助手",
                    onSettingsClick = onNavigateToSettings,
                    onChatClick = onNavigateToChat,
                    largeFont = largeFont
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 状态显示区
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 状态文字
                        Text(
                            text = statusText,
                            fontSize = titleSize,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 音量可视化
                        if (isListening) {
                            VolumeVisualizer(
                                volume = animatedVolume,
                                modifier = Modifier.height(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 实时语音文本
                        if (isListening && userSpeechText.isNotBlank()) {
                            Text(
                                text = userSpeechText,
                                fontSize = bodySize,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // 思考中动画
                        if (isThinking) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 消息列表
                        if (messages.isNotEmpty()) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(messages) { msg ->
                                    CarMessageBubble(
                                        message = msg,
                                        largeFont = largeFont,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            LaunchedEffect(messages.size) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 底部控制区
                CarBottomControls(
                    isListening = isListening,
                    isThinking = isThinking,
                    buttonSize = buttonSize,
                    largeFont = largeFont,
                    onMicClick = {
                        if (isListening) {
                            isListening = false
                            statusText = "已停止聆听"
                        } else {
                            startCarListening(
                                speechManager = null,
                                onStart = { isListening = true; statusText = "正在聆听..." },
                                onText = { userSpeechText = it },
                                onVolume = { volumeLevel = it }
                            )
                        }
                    },
                    onNavClick = { statusText = "导航功能待接入" },
                    onMediaClick = { statusText = "媒体控制待接入" },
                    onPhoneClick = { statusText = "蓝牙电话待接入" }
                )
            }
        }
    }
}

/**
 * 车机顶部栏
 */
@Composable
private fun CarTopBar(
    title: String,
    onSettingsClick: () -> Unit,
    onChatClick: () -> Unit,
    largeFont: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = if (largeFont) 26.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row {
            IconButton(onClick = onChatClick) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "聊天",
                    modifier = Modifier.size(if (largeFont) 32.dp else 24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    modifier = Modifier.size(if (largeFont) 32.dp else 24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

/**
 * 音量可视化
 */
@Composable
private fun VolumeVisualizer(
    volume: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 20
    Row(
        modifier = modifier.fillMaxWidth(0.8f),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val centerOffset = kotlin.math.abs(index - barCount / 2f) / (barCount / 2f)
            val height = (volume * (1f - centerOffset * 0.5f) * 36.dp.value + 4f).coerceAtLeast(4f)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/**
 * 车机消息气泡
 */
@Composable
private fun CarMessageBubble(
    message: CarChatMessage,
    largeFont: Boolean,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "user"
    Row(
        modifier = modifier,
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = message.content,
                fontSize = if (largeFont) 18.sp else 15.sp,
                color = if (isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(14.dp),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 车机底部控制区
 */
@Composable
private fun CarBottomControls(
    isListening: Boolean,
    isThinking: Boolean,
    buttonSize: androidx.compose.ui.unit.Dp,
    largeFont: Boolean,
    onMicClick: () -> Unit,
    onNavClick: () -> Unit,
    onMediaClick: () -> Unit,
    onPhoneClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 导航按钮
        CarControlButton(
            icon = Icons.Default.Navigation,
            label = "导航",
            size = buttonSize,
            largeFont = largeFont,
            onClick = onNavClick
        )

        // 媒体按钮
        CarControlButton(
            icon = Icons.Default.PlayArrow,
            label = "媒体",
            size = buttonSize,
            largeFont = largeFont,
            onClick = onMediaClick
        )

        // 主麦克风按钮（更大）
        Box(
            modifier = Modifier
                .size(buttonSize + 20.dp)
                .clip(CircleShape)
                .background(
                    if (isListening)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMicClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isListening) "停止" else "语音",
                modifier = Modifier.size(buttonSize - 16.dp),
                tint = Color.White
            )
        }

        // 电话按钮
        CarControlButton(
            icon = Icons.Default.Phone,
            label = "电话",
            size = buttonSize,
            largeFont = largeFont,
            onClick = onPhoneClick
        )

        // 音量按钮
        CarControlButton(
            icon = Icons.Default.VolumeUp,
            label = "音量",
            size = buttonSize,
            largeFont = largeFont,
            onClick = { /* 系统音量 */ }
        )
    }
}

/**
 * 车机控制按钮
 */
@Composable
private fun CarControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    size: androidx.compose.ui.unit.Dp,
    largeFont: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(size - 20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = if (largeFont) 14.sp else 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 车机聊天消息数据类
 */
data class CarChatMessage(
    val sender: String,
    val content: String,
    val timestamp: Long
)

/**
 * 启动车机语音监听（简化版，直接使用 SpeechService）
 */
private fun startCarListening(
    speechManager: SpeechInteractionManager?,
    onStart: () -> Unit,
    onText: (String) -> Unit,
    onVolume: (Float) -> Unit
) {
    // 实际实现中应通过 SpeechInteractionManager 启动
    // 这里提供简化的启动逻辑
    onStart()
}
