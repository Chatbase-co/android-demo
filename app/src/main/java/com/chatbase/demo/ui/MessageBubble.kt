package com.chatbase.demo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.chatbase.sdk.model.Role
import com.chatbase.sdk.model.UiMessage
import com.chatbase.sdk.model.UiMessageContent
import com.chatbase.demo.ui.theme.*

@Composable
fun MessageBubble(
    message: UiMessage,
    onRetry: (String?) -> Unit
) {
    val isUser = message.role == Role.USER
    val text = (message.content as UiMessageContent.Text).text

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (isUser) 12.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 12.dp
                ),
                color = when {
                    message.isError -> ErrorContainer
                    isUser -> BluePrimary
                    else -> DarkSurfaceVariant
                },
                border = when {
                    message.isError -> BorderStroke(1.dp, Error)
                    isUser -> null
                    else -> BorderStroke(1.dp, DarkOutline)
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = text,
                        color = when {
                            message.isError -> Error
                            isUser -> TextPrimary
                            else -> TextPrimary
                        },
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (message.isStreaming) {
                        BlinkingCursor()
                    }
                }
            }

            AnimatedVisibility(visible = !isUser && !message.isStreaming && message.messageId != null) {
                IconButton(
                    onClick = { onRetry(message.messageId) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(16.dp),
                        tint = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun BlinkingCursor() {
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )
    Text(
        text = "|",
        modifier = Modifier
            .padding(start = 2.dp)
            .alpha(alpha),
        color = BluePrimary
    )
}
