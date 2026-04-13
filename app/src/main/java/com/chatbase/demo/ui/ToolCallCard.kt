package com.chatbase.demo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatbase.sdk.model.UiMessage
import com.chatbase.sdk.model.UiMessageContent
import com.chatbase.demo.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ToolCallCard(message: UiMessage) {
    val toolCall = message.content as UiMessageContent.ToolCall
    var inputExpanded by remember { mutableStateOf(true) }
    var outputExpanded by remember { mutableStateOf(false) }

    // Execution timer
    var elapsedMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(toolCall.isExecuting) {
        if (toolCall.isExecuting) {
            val start = System.currentTimeMillis()
            while (true) {
                elapsedMs = System.currentTimeMillis() - start
                delay(100)
            }
        }
    }
    val elapsedText = if (toolCall.output != null || toolCall.isExecuting) {
        String.format("%.1fs", elapsedMs / 1000.0)
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkOutline)
    ) {
        Column {
            // Animated progress line along top edge when executing
            if (toolCall.isExecuting) {
                val transition = rememberInfiniteTransition(label = "progress")
                val progress by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "progressAnim"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(DarkOutline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = 0.3f)
                            .height(2.dp)
                            .padding(start = (progress * 300).dp)
                            .background(BluePrimary)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status dot or checkmark
                    if (toolCall.isExecuting) {
                        PulsingDot()
                    } else if (toolCall.output != null) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Complete",
                            modifier = Modifier.size(16.dp),
                            tint = Success
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = toolCall.toolName,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.weight(1f))
                    if (elapsedText != null) {
                        Text(
                            text = elapsedText,
                            fontFamily = FontFamily.Monospace,
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Input section
                val trimmedInput = toolCall.input.trim()
                if (trimmedInput.isNotBlank() && trimmedInput != "{}" && trimmedInput != "{ }") {
                    Spacer(Modifier.height(8.dp))
                    ExpandableJsonSection(
                        title = "INPUT",
                        expanded = inputExpanded,
                        onToggle = { inputExpanded = !inputExpanded },
                        content = toolCall.input
                    )
                }

                // Output section
                val output = toolCall.output
                if (output != null) {
                    Spacer(Modifier.height(4.dp))
                    ExpandableJsonSection(
                        title = "OUTPUT",
                        expanded = outputExpanded,
                        onToggle = { outputExpanded = !outputExpanded },
                        content = output
                    )
                }
            }
        }
    }
}

@Composable
private fun PulsingDot() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(BluePrimary.copy(alpha = alpha))
    )
}

@Composable
private fun ExpandableJsonSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: String
) {
    Row(
        modifier = Modifier.clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = TextTertiary,
            letterSpacing = 1.sp
        )
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (expanded) "Collapse" else "Expand",
            modifier = Modifier.size(16.dp),
            tint = TextTertiary
        )
    }
    AnimatedVisibility(visible = expanded) {
        Surface(
            color = DarkSurfaceVariant,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Text(
                text = highlightJson(content),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(8.dp),
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Simple regex-based JSON syntax highlighting.
 * Keys are blue, string values are green, other values are gray.
 */
private fun highlightJson(json: String) = buildAnnotatedString {
    // Try to pretty-print if it's compact JSON
    val formatted = try {
        val trimmed = json.trim()
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            formatJson(trimmed)
        } else trimmed
    } catch (_: Exception) { json }

    val keyPattern = Regex(""""([^"\\]*(\\.[^"\\]*)*)"\s*:""")
    val stringValuePattern = Regex(""":\s*"([^"\\]*(\\.[^"\\]*)*)"""")
    val numberPattern = Regex(""":\s*(-?\d+\.?\d*)""")

    var i = 0
    while (i < formatted.length) {
        val keyMatch = keyPattern.find(formatted, i)
        val stringMatch = stringValuePattern.find(formatted, i)
        val numberMatch = numberPattern.find(formatted, i)

        val nextMatch = listOfNotNull(keyMatch, stringMatch, numberMatch)
            .minByOrNull { it.range.first }

        if (nextMatch == null || nextMatch.range.first > i) {
            val end = nextMatch?.range?.first ?: formatted.length
            withStyle(SpanStyle(color = TextSecondary)) {
                append(formatted.substring(i, end))
            }
            if (nextMatch == null) break
            i = end
        }

        when (nextMatch) {
            keyMatch -> {
                val keyStart = nextMatch.range.first
                val colonEnd = nextMatch.range.last + 1
                val keyName = nextMatch.groupValues[1]

                withStyle(SpanStyle(color = SyntaxKey)) {
                    append("\"$keyName\"")
                }
                withStyle(SpanStyle(color = TextSecondary)) {
                    append(formatted.substring(keyStart + keyName.length + 2, colonEnd))
                }
                i = colonEnd
            }
            stringMatch -> {
                val colonPart = formatted.substring(nextMatch.range.first, formatted.indexOf('"', nextMatch.range.first + 1))
                withStyle(SpanStyle(color = TextSecondary)) {
                    append(colonPart)
                }
                val strVal = nextMatch.groupValues[1]
                withStyle(SpanStyle(color = SyntaxString)) {
                    append("\"$strVal\"")
                }
                i = nextMatch.range.last + 1
            }
            numberMatch -> {
                val colonPart = formatted.substring(nextMatch.range.first, formatted.indexOf(nextMatch.groupValues[1], nextMatch.range.first))
                withStyle(SpanStyle(color = TextSecondary)) {
                    append(colonPart)
                }
                withStyle(SpanStyle(color = Warning)) {
                    append(nextMatch.groupValues[1])
                }
                i = nextMatch.range.last + 1
            }
        }
    }
}

/** Simple JSON indentation — not a full parser, just adds newlines and indentation. */
private fun formatJson(json: String): String {
    val sb = StringBuilder()
    var indent = 0
    var inString = false
    var escape = false

    for (c in json) {
        when {
            escape -> { sb.append(c); escape = false }
            c == '\\' && inString -> { sb.append(c); escape = true }
            c == '"' -> { inString = !inString; sb.append(c) }
            inString -> sb.append(c)
            c == '{' || c == '[' -> {
                sb.append(c)
                indent += 2
                sb.append('\n').append(" ".repeat(indent))
            }
            c == '}' || c == ']' -> {
                indent -= 2
                sb.append('\n').append(" ".repeat(indent.coerceAtLeast(0))).append(c)
            }
            c == ',' -> {
                sb.append(c).append('\n').append(" ".repeat(indent))
            }
            c == ':' -> sb.append(": ")
            !c.isWhitespace() -> sb.append(c)
        }
    }
    return sb.toString()
}
