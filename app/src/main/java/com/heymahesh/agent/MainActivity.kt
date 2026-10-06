package com.heymahesh.agent

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heymahesh.agent.core.VoiceAgentController
import com.heymahesh.agent.core.accessibility.ScreenPilotAccessibilityService
import com.heymahesh.agent.core.audio.WakeWordForegroundService
import com.heymahesh.agent.core.state.AssistantState
import com.heymahesh.agent.ui.theme.*

class MainActivity : ComponentActivity() {

    private lateinit var voiceAgentController: VoiceAgentController

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions update
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        voiceAgentController = VoiceAgentController(this)
        requestAppPermissions()

        setContent {
            MaheshTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by voiceAgentController.agentState.collectAsState()
                    MaheshMainScreen(
                        state = state,
                        onActivateVoice = {
                            voiceAgentController.startVoiceInteraction()
                        },
                        onStopVoice = {
                            voiceAgentController.stop()
                        },
                        onOpenAccessibilitySettings = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        onOpenOverlaySettings = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                )
                                startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }
}

@Composable
fun MaheshMainScreen(
    state: AssistantState,
    onActivateVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit
) {
    val isListening = state is AssistantState.Listening
    val isThinking = state is AssistantState.Thinking
    val isDisambiguating = state is AssistantState.Disambiguating
    val isExecuting = state is AssistantState.Executing
    val isSpeaking = state is AssistantState.Speaking

    val statusText = when (state) {
        is AssistantState.Listening -> "Listening... (Say your command)"
        is AssistantState.Thinking -> "Thinking: '${state.rawQuery}'"
        is AssistantState.Disambiguating -> state.question
        is AssistantState.MissingSlot -> state.question
        is AssistantState.Executing -> state.statusMessage
        is AssistantState.Speaking -> state.spokenText
        is AssistantState.Error -> "Error: ${state.errorMessage}"
        else -> "Say 'Hey Mahesh' or tap the button below"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Brand Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚡ mahesh-",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = ElectricBlue
            )
        }
        Text(
            text = "The Autonomous Voice AI for Android",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Central Holographic Glowing Orb Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = if (isListening) listOf(ElectricBlue, NeonCyan, Color.Transparent)
                                else if (isExecuting) listOf(AccentGreen, Color.Transparent)
                                else listOf(CyberPurple, Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                        .clickable {
                            if (isListening) onStopVoice() else onActivateVoice()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Hearing else Icons.Default.Mic,
                        contentDescription = "Voice Trigger",
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (isListening) "LISTENING" else if (isExecuting) "EXECUTING" else "STANDBY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isListening) ElectricBlue else if (isExecuting) AccentGreen else TextSecondary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = statusText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Power Settings & Automation Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Superpowers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingStatusRow(
                    title = "Screen Pilot (WhatsApp Auto-Send)",
                    isActive = ScreenPilotAccessibilityService.isRunning,
                    onClick = onOpenAccessibilitySettings
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingStatusRow(
                    title = "Floating Holographic HUD",
                    isActive = true,
                    onClick = onOpenOverlaySettings
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Example Prompts
        Text(
            text = "Try Saying:",
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        PromptCard(icon = Icons.Default.Phone, text = "Hey Mahesh, call Ramalingam on speaker")
        Spacer(modifier = Modifier.height(8.dp))
        PromptCard(icon = Icons.Default.Send, text = "Hey Mahesh, tell Rahul on WhatsApp we're at canteen")
        Spacer(modifier = Modifier.height(8.dp))
        PromptCard(icon = Icons.Default.CameraAlt, text = "Hey Mahesh, take a group photo in 3 seconds")
        Spacer(modifier = Modifier.height(8.dp))
        PromptCard(icon = Icons.Default.FlashlightOn, text = "Hey Mahesh, turn on flashlight")

        Spacer(modifier = Modifier.height(32.dp))

        // Activation Action Button
        Button(
            onClick = {
                if (isListening) onStopVoice() else onActivateVoice()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) AccentRed else CyberPurple
            )
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.GraphicEq,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isListening) "Stop Listening" else "Activate Mahesh Voice",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingStatusRow(title: String, isActive: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 13.sp, color = TextSecondary)
        Text(
            text = if (isActive) "ACTIVE" else "ENABLE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) AccentGreen else ElectricBlue
        )
    }
}

@Composable
fun PromptCard(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, color = TextPrimary, fontSize = 13.sp)
        }
    }
}
