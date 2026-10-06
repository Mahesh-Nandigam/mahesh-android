package com.heymahesh.agent.core.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heymahesh.agent.core.models.ContactEntity
import com.heymahesh.agent.core.state.AssistantState
import com.heymahesh.agent.ui.theme.*
import kotlinx.coroutines.flow.StateFlow

class FloatingHUDController(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null

    val canDrawOverlays: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true

    fun showOverlay(
        stateFlow: StateFlow<AssistantState>,
        onOptionSelected: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        if (composeView != null || !canDrawOverlays) return

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
        }

        composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                val state by stateFlow.collectAsState()
                MaheshOverlayContent(
                    state = state,
                    onOptionSelected = onOptionSelected,
                    onDismiss = {
                        hideOverlay()
                        onDismiss()
                    }
                )
            }
        }

        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            composeView = null
        }
    }

    fun hideOverlay() {
        composeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {}
            composeView = null
        }
    }
}

@Composable
fun MaheshOverlayContent(
    state: AssistantState,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = state !is AssistantState.Idle,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkCard.copy(alpha = 0.95f),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Indicator Pill
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                        .clickable { onDismiss() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (state) {
                    is AssistantState.Listening -> {
                        Text(
                            text = "⚡ Listening...",
                            color = ElectricBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    is AssistantState.Disambiguating -> {
                        Text(
                            text = state.question,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(state.candidates) { contact ->
                                SuggestionChip(
                                    onClick = { onOptionSelected(contact.name) },
                                    label = { Text("${contact.name} (${contact.tag})", color = TextPrimary) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = DarkSurface
                                    )
                                )
                            }
                        }
                    }

                    is AssistantState.MissingSlot -> {
                        Text(
                            text = state.question,
                            color = AccentAmber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    is AssistantState.Executing -> {
                        Text(
                            text = state.statusMessage,
                            color = AccentGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    is AssistantState.Speaking -> {
                        Text(
                            text = state.spokenText,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}
