# Chatbase Android SDK Demo

A demo Android app showcasing the [Chatbase Android SDK](https://github.com/nichochar/chatbase-android). Built with Jetpack Compose and Material Design 3, it demonstrates real-time chat with AI agents, conversation history, tool calling, and user identification.

<p align="center">
  <img src="demo.gif" alt="Demo" width="320" />
</p>

## Prerequisites

- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 11+** (bundled with Android Studio)

## Quick Start

1. **Clone the repo:**
   ```bash
   git clone https://github.com/nichochar/android-demo.git
   ```

2. **Open in Android Studio** — open the `android-demo` folder as a project. The Chatbase SDK is pulled automatically from Maven Central.

3. **Set your Agent ID** — open `app/src/main/java/com/chatbase/demo/AppNavigation.kt` and replace the demo agent ID:
   ```kotlin
   private const val DEMO_AGENT_ID = "your-agent-id-here"
   ```

4. **Set your API base URL** — open `app/src/main/java/com/chatbase/demo/viewmodel/AppViewModel.kt` and update the base URL:
   ```kotlin
   this.baseUrl = "https://www.chatbase.co"
   ```

5. **Run the app** — select an emulator or connected device and click **Run** (or press `Shift+F10`).

## What the Demo Shows

| Feature | Description |
|---|---|
| **Chat** | Send messages and receive streaming AI responses |
| **Conversations** | List, create, and resume conversations with paginated history |
| **Tool Calling** | A `get_spell_damage` tool that pops a picker dialog, demonstrating interactive tool execution |
| **User Identification** | JWT-based user identification via the Settings dialog |
| **Typing Indicator** | Animated indicator while the agent is responding |

## Project Structure

```
app/src/main/java/com/chatbase/demo/
├── MainActivity.kt              # Entry point
├── ChatbaseApp.kt               # Application class
├── AppNavigation.kt             # Navigation graph & auto-connect
├── viewmodel/
│   ├── AppViewModel.kt          # SDK client lifecycle, tool registration
│   └── ChatViewModel.kt         # Conversation state & message handling
└── ui/
    ├── SetupScreen.kt           # Agent ID input
    ├── ConversationsScreen.kt   # Conversation list
    ├── ChatScreen.kt            # Chat interface
    ├── SettingsDialog.kt        # Device ID, JWT identification
    ├── SpellPickerDialog.kt     # Tool calling demo UI
    ├── MessageBubble.kt         # Message display
    ├── MessageInput.kt          # Text input
    ├── ToolCallCard.kt          # Tool execution card
    └── TypingIndicator.kt       # Typing animation
```

## Build Details

| Property | Value |
|---|---|
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 |
| Kotlin | 2.2.20 |
| Compose BOM | 2025.01.01 |
| Java target | 11 |
