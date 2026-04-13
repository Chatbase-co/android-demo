package com.chatbase.demo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.chatbase.demo.ui.theme.*

@Composable
fun SettingsDialog(
    deviceId: String,
    isIdentified: Boolean,
    currentUserId: String?,
    onDismiss: () -> Unit,
    onIdentify: (String) -> Unit,
    onLogout: () -> Unit
) {
    var token by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text("Settings") },
        text = {
            Column {
                Text("Device ID", color = TextTertiary, fontFamily = FontFamily.Monospace)
                Text(
                    text = deviceId,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )

                Spacer(Modifier.height(12.dp))

                Text("Identity", color = TextTertiary, fontFamily = FontFamily.Monospace)
                if (isIdentified) {
                    Text(
                        text = "Identified as: ${currentUserId ?: "unknown"}",
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        border = BorderStroke(1.dp, DarkOutline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Text("Logout")
                    }
                } else {
                    Text("Not identified", color = TextTertiary)
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("JWT Token", color = TextTertiary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = BluePrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { if (token.isNotBlank()) onIdentify(token.trim()) },
                        enabled = token.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Identify")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = BluePrimary)
            }
        }
    )
}
