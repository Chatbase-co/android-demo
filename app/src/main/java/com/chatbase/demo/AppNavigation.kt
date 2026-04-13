package com.chatbase.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chatbase.sdk.ConversationListState
import com.chatbase.demo.ui.ChatScreen
import com.chatbase.demo.ui.ConversationsScreen
import com.chatbase.demo.ui.SetupScreen
import com.chatbase.demo.ui.SettingsDialog
import com.chatbase.demo.ui.SpellPickerDialog
import com.chatbase.demo.viewmodel.AppViewModel
import com.chatbase.demo.viewmodel.ChatViewModel

private const val DEMO_AGENT_ID = "5QHA6VB-DIAbBhxwqxfdi"

@Composable
fun AppNavigation(appViewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val appState by appViewModel.state.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    // Auto-connect on launch
    LaunchedEffect(Unit) {
        if (!appState.isConnected) {
            appViewModel.connect(DEMO_AGENT_ID)
        }
    }

    if (showSettings) {
        SettingsDialog(
            deviceId = appState.deviceId,
            isIdentified = appState.isIdentified,
            currentUserId = appState.currentUserId,
            onDismiss = { showSettings = false },
            onIdentify = { token ->
                appViewModel.identify(token)
            },
            onLogout = {
                appViewModel.logout()
            }
        )
    }

    val spellPickerRequest by appViewModel.spellPickerRequest.collectAsStateWithLifecycle()
    if (spellPickerRequest != null) {
        SpellPickerDialog(
            spells = appViewModel.spellOptions,
            onSpellSelected = { spell -> appViewModel.onSpellPicked(spell) },
            onDismiss = { appViewModel.onSpellPickerDismissed() }
        )
    }

    NavHost(
        navController = navController,
        startDestination = "conversations"
    ) {
        composable("setup") {
            SetupScreen(
                error = appState.error,
                onConnect = { agentId ->
                    appViewModel.connect(agentId)
                    if (appViewModel.state.value.isConnected) {
                        navController.navigate("conversations") {
                            popUpTo("setup") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("conversations") {
            val conversationListState by (appViewModel.conversationList?.state
                ?: remember { kotlinx.coroutines.flow.MutableStateFlow(ConversationListState.State()) })
                .collectAsStateWithLifecycle()

            ConversationsScreen(
                appState = appState,
                conversationListState = conversationListState,
                onNewChat = {
                    appViewModel.newConversation()
                    navController.navigate("chat/new")
                },
                onOpenConversation = { conversationId ->
                    navController.navigate("chat/$conversationId")
                },
                onLoadConversations = { appViewModel.loadConversations() },
                onLoadMore = { appViewModel.loadMoreConversations() },
                onSettingsClick = { showSettings = true }
            )
        }

        composable(
            route = "chat/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId")
            val effectiveId = if (conversationId == "new") null else conversationId
            val client = appViewModel.client

            if (client != null) {
                val chatViewModel = remember(effectiveId) {
                    ChatViewModel(client, effectiveId)
                }
                ChatScreen(
                    viewModel = chatViewModel,
                    onBack = {
                        navController.popBackStack()
                        appViewModel.loadConversations()
                    }
                )
            }
        }
    }
}
