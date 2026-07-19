package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GiggzViewModel
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(
    viewModel: GiggzViewModel,
    onNavigate: (String) -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme() || isDarkMode
    
    // Animation triggers
    var startAnimation by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }
    
    // Smooth splash entry transition
    val entryProgress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "entry_progress"
    )
    
    // Smooth splash exit transitions (fade out + scaling)
    val exitAlpha by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "exit_alpha"
    )
    
    val exitScale by animateFloatAsState(
        targetValue = if (isExiting) 0.94f else 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "exit_scale"
    )
    
    // Subtle infinite rotation for the glow background
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val glowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_rotation"
    )
    
    // Pulse animation for the glow backdrop
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = SineIntensityEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    LaunchedEffect(Unit) {
        // Trigger the spring animations
        startAnimation = true
        
        // Let the splash screen linger beautifully
        delay(2200)
        
        // Initiate smooth exit animations
        isExiting = true
        
        // Wait for 500ms exit transition to finish fully
        delay(500)
        
        // Transition to Login
        onNavigate("login")
    }

    // Giggz theme colors for the splash screen
    val primaryColor = if (isDark) Color(0xFF10B981) else Color(0xFF059669) // Mint Green vs Giggz Emerald
    val secondaryColor = Color(0xFF34D399) // Soft light mint
    val goldColor = Color(0xFFFBBF24) // Gold Accent
    val backgroundColor = if (isDark) Color(0xFF121417) else Color.White
    val containerColor = if (isDark) Color(0xFF1E2228) else Color.White
    val subtextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(exitAlpha)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // 1. Ambient Golden-Green Glow Backdrop
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(entryProgress * glowScale * exitScale)
                .alpha(entryProgress * (if (isDark) 0.35f else 0.45f))
                .graphicsLayer { rotationZ = glowRotation }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.4f),
                            secondaryColor.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 2. Main Container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Smoothly animated logo card using the perfectly circular container
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(entryProgress * exitScale)
                    .alpha(entryProgress)
                    .clip(CircleShape)
                    .background(containerColor)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            colors = if (isDark) {
                                listOf(primaryColor, goldColor)
                            } else {
                                listOf(primaryColor, secondaryColor)
                            }
                        ),
                        shape = CircleShape
                    )
                    .padding(28.dp), // perfectly balanced padding so logo is centered and does not touch the edges
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.giggz_logo_refined_with_z_1783929293429),
                    contentDescription = "Giggz Logo Animation",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Smoothly animated Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer {
                        translationY = (35 * (1f - entryProgress * exitScale))
                        alpha = entryProgress
                    }
            ) {
                Text(
                    text = "Giggz",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Smart Job Marketplace",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subtextColor,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// Custom Easing to make pulse feel organic
private val SineIntensityEasing = Easing { fraction ->
    val t = fraction.toDouble()
    kotlin.math.sin(t * kotlin.math.PI / 2.0).toFloat()
}
