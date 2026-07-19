package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GiggzViewModel
import com.example.ui.RecruitCampaign
import com.example.ui.components.showSafeToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Theme Colors matching Giggz Brand
val RecruitDarkBg @Composable get() = MaterialTheme.colorScheme.background
val GiggzGreen @Composable get() = MaterialTheme.colorScheme.primary
val GiggzMint @Composable get() = MaterialTheme.colorScheme.secondary
val GiggzGold = Color(0xFFFBBF24)
val GiggzSlate @Composable get() = MaterialTheme.colorScheme.surfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiggzRecruitSection(viewModel: GiggzViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    val walletBalance by viewModel.recruiterWalletBalance.collectAsStateWithLifecycle()
    val isSubscribed by viewModel.isRecruiterSubscribed.collectAsStateWithLifecycle()
    val campaigns by viewModel.recruitmentCampaigns.collectAsStateWithLifecycle()

    var showCampaignForm by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedOptionType by remember { mutableStateOf("Student") } // "Student", "Worker", "Bulk"
    
    // Pricing packages & Campaign payment states
    var activePaymentPackage by remember { mutableStateOf<String?>(null) } // "Single", "Monthly", "Yearly"
    var pendingCampaignToPublish by remember { mutableStateOf<RecruitCampaign?>(null) }

    // Active Dashboard tab
    var activeDashboardTab by remember { mutableStateOf("overview") } // "overview", "applicants"
    var selectedCampaignForDetails by remember { mutableStateOf<RecruitCampaign?>(null) }

    // Toast & Dialog States
    var showSuccessUpgradeDialog by remember { mutableStateOf(false) }
    var showSuccessCampaignDialog by remember { mutableStateOf(false) }

    val bgModifier = Modifier.background(MaterialTheme.colorScheme.background)

    val textColor = MaterialTheme.colorScheme.onBackground
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = if (isDark) Color(0xFF1F2937) else Color.White // match other cards in light mode

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(bgModifier)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }



        // 2. Recruitment Options (Selectable Cards)
        item {
            Column {
                Text(
                    text = "Select Recruitment Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecruitmentTypeCard(
                        title = "Students",
                        description = "Hire from colleges for internships/gigs",
                        icon = Icons.Filled.School,
                        isSelected = selectedOptionType == "Student",
                        onClick = { selectedOptionType = "Student" },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    RecruitmentTypeCard(
                        title = "Workers",
                        description = "Find vetted professionals instantly",
                        icon = Icons.Filled.Engineering,
                        isSelected = selectedOptionType == "Worker",
                        onClick = { selectedOptionType = "Worker" },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    RecruitmentTypeCard(
                        title = "Bulk Staff",
                        description = "Recruit teams of 10 to 100+ at once",
                        icon = Icons.Filled.Groups,
                        isSelected = selectedOptionType == "Bulk",
                        onClick = { selectedOptionType = "Bulk" },
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }
            }
        }

        // Action Trigger Button to expand form or see details
        item {
            Button(
                onClick = {
                    val oneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000
                    val hasPostedToday = campaigns.any { it.createdAt > oneDayAgo }
                    if (isSubscribed || !hasPostedToday) {
                        showCampaignForm = true
                    } else {
                        context.showSafeToast("Limit reached: 1 post per day for free accounts. Please Upgrade to Premium for unlimited posts!")
                        showPaymentDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("start_recruitment_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.RocketLaunch, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Recruitment Campaign",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }

        // 3. Premium Features (locked/unlocked checklist)
        item {
            PremiumFeaturesChecklist(isDark = isDark, isUnlocked = isSubscribed)
        }

        // 6. Recruitment Dashboard for Campaigns
        item {
            Text(
                text = "Recruitment Campaign Dashboard",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (campaigns.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active recruitment campaigns yet. Launch one above!",
                            fontSize = 12.sp,
                            color = subTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(campaigns) { campaign ->
                CampaignDashboardItem(
                    campaign = campaign,
                    isDark = isDark,
                    onViewAnalytics = {
                        selectedCampaignForDetails = campaign
                        activeDashboardTab = "overview"
                    },
                    onManageApplicants = {
                        selectedCampaignForDetails = campaign
                        activeDashboardTab = "applicants"
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // 4. Create Campaign Form Dialog
    if (showCampaignForm) {
        CreateCampaignDialog(
            typePreset = selectedOptionType,
            isDark = isDark,
            onDismiss = { showCampaignForm = false },
            onPublishCampaign = { campaign ->
                showCampaignForm = false
                pendingCampaignToPublish = campaign
                // Before publishing, require payment!
                showPaymentDialog = true
            }
        )
    }

    // 5. Payment System Selector
    if (showPaymentDialog) {
        PaymentSystemDialog(
            isDark = isDark,
            walletBalance = walletBalance,
            pendingCampaign = pendingCampaignToPublish,
            onDismiss = { showPaymentDialog = false },
            onPaymentSuccess = { cost, isSubscriptionUpgrade ->
                viewModel.updateRecruiterWallet(-cost)
                if (isSubscriptionUpgrade) {
                    viewModel.setRecruiterSubscribed(true)
                    showSuccessUpgradeDialog = true
                } else {
                    pendingCampaignToPublish?.let {
                        viewModel.addRecruitmentCampaign(it)
                    }
                    showSuccessCampaignDialog = true
                }
                showPaymentDialog = false
                pendingCampaignToPublish = null
            }
        )
    }

    // Interactive Details & Analytics Dashboard
    if (selectedCampaignForDetails != null) {
        CampaignDetailsDashboardDialog(
            campaign = selectedCampaignForDetails!!,
            tab = activeDashboardTab,
            isDark = isDark,
            onDismiss = { selectedCampaignForDetails = null },
            onUpdateCampaignStats = { camp, apps, short, hired ->
                viewModel.updateCampaignStats(camp.id, apps, short, hired)
                // update local state too
                selectedCampaignForDetails = selectedCampaignForDetails?.copy(
                    applicantsCount = apps,
                    shortlistedCount = short,
                    hiredCount = hired
                )
            }
        )
    }

    // Success Upgrade Dialog
    if (showSuccessUpgradeDialog) {
        SuccessCelebrationDialog(
            title = "Upgrade Successful! 👑",
            message = "You have unlocked the Giggz Recruit Premium subscription. You now have unlimited access to all verified profiles, AI candidate matching, and smart analytics dashboards!",
            isDark = isDark,
            onDismiss = { showSuccessUpgradeDialog = false }
        )
    }

    // Success Campaign Dialog
    if (showSuccessCampaignDialog) {
        SuccessCelebrationDialog(
            title = "Campaign Live! 🚀",
            message = "Your premium recruitment campaign has been successfully registered and published on the Giggz network. Matchmakers are already searching for your candidates!",
            isDark = isDark,
            onDismiss = { showSuccessCampaignDialog = false }
        )
    }
}

// 1. Premium Header Section with Custom Interactive Animated Connecting Candidate Nodes
@Composable
fun PremiumRecruitHeader(isDark: Boolean) {
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563)
    val containerBg = if (isDark) Color(0xFF1F2937) else Color(0xFFFFFBEE) // Gold cream background for premium feel in light mode

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                spotColor = GiggzGold.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(GiggzGold, GiggzGreen)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gold Glowing Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = "Premium Icon",
                        tint = RecruitDarkBg,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GIGGZ RECRUIT PREMIUM",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RecruitDarkBg,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title (20% smaller than standard 24sp/22sp titles -> 17.sp)
            Text(
                text = "Recruit Smarter with Giggz",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color(0xFF111827),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "Find skilled workers, students, and teams faster.",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = subTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Recruiter-Candidate Connecting Nodes Illustration
            RecruitmentIllustrationCanvas(isDark = isDark)
        }
    }
}

// Custom Vector Animation in Canvas representing a recruiter connecting with multiple students/workers
@Composable
fun RecruitmentIllustrationCanvas(isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "IllustrationNetwork")

    // Node animations
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotate"
    )

    val signalProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ),
        label = "Signal"
    )

    val lineColor = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    val canvasGreen = GiggzGreen
    val canvasMint = GiggzMint

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(if (isDark) Color(0xFF111827) else Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
            .border(1.dp, if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val outerRadius = 90f // Radius of candidates orbit

            // Draw connecting lines
            val candidateCount = 5
            for (i in 0 until candidateCount) {
                val angleRad = Math.toRadians((360.0 / candidateCount) * i + rotateAngle).toFloat()
                val targetX = cx + outerRadius * cos(angleRad)
                val targetY = cy + outerRadius * sin(angleRad)

                // Main Connection Line
                drawLine(
                    color = lineColor,
                    start = Offset(cx, cy),
                    end = Offset(targetX, targetY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Glowing pulse signal dot moving from Recruiter to Candidate
                val dotX = cx + (outerRadius * signalProgress) * cos(angleRad)
                val dotY = cy + (outerRadius * signalProgress) * sin(angleRad)
                drawCircle(
                    color = canvasGreen,
                    radius = 5f,
                    center = Offset(dotX, dotY)
                )

                // Star highlight or gold glitter glow at candidate node
                drawCircle(
                    color = GiggzGold.copy(alpha = 0.2f),
                    radius = 24f * pulseRatio,
                    center = Offset(targetX, targetY)
                )
            }

            // Draw Central Recruiter Node
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GiggzGold, Color(0xFFD97706)),
                    center = Offset(cx, cy),
                    radius = 22f
                ),
                radius = 20f * pulseRatio,
                center = Offset(cx, cy)
            )

            // Central Recruiter Crown Icon Represented by Gold Dot inside
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = Offset(cx, cy)
            )

            // Draw Candidates Orbiting
            for (i in 0 until candidateCount) {
                val angleRad = Math.toRadians((360.0 / candidateCount) * i + rotateAngle).toFloat()
                val targetX = cx + outerRadius * cos(angleRad)
                val targetY = cy + outerRadius * sin(angleRad)

                // Alternating Mint Green & Soft Blue candidates
                val color = if (i % 2 == 0) canvasMint else Color(0xFF3B82F6)
                drawCircle(
                    color = color,
                    radius = 12f,
                    center = Offset(targetX, targetY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(targetX, targetY)
                )
            }
        }

        // Overlay small informative text tags next to orbits
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                "Recruiter (You)",
                color = GiggzGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 28.dp)
            )
            Text(
                "Students",
                color = GiggzMint,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 16.dp, y = (-26).dp)
            )
            Text(
                "Workers",
                color = Color(0xFF3B82F6),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-16).dp, y = (26).dp)
            )
        }
    }
}

// 2. Selectable Option Cards
@Composable
fun RecruitmentTypeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    val cardBg = if (isDark) Color(0xFF1F2937) else Color.White // match other cards in light mode
    val borderBrush = if (isSelected) {
        Brush.sweepGradient(listOf(GiggzGold, GiggzGreen, GiggzGold))
    } else {
        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }

    val glowModifier = if (isSelected) {
        Modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(12.dp),
            clip = false,
            spotColor = GiggzGreen.copy(alpha = 0.4f)
        )
    } else Modifier

    Card(
        modifier = modifier
            .then(glowModifier)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                if (isDark) Color(0xFF111827) else Color(0xFFECFDF5)
            } else cardBg
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) GiggzGreen else if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) GiggzGreen.copy(alpha = 0.15f)
                        else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) GiggzGreen else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subheadings are also 20% smaller (from 14sp to 11.sp)
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF111827),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = description,
                fontSize = 8.sp,
                color = Color.Gray,
                lineHeight = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 3. Premium Features Checklist
@Composable
fun PremiumFeaturesChecklist(isDark: Boolean, isUnlocked: Boolean) {
    val cardBg = if (isDark) Color(0xFF1F2937) else Color.White // match other cards in light mode
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Giggz Recruit Powerful Tools",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isUnlocked) GiggzGreen.copy(alpha = 0.15f)
                            else GiggzGold.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isUnlocked) "UNLOCKED" else "PREMIUM",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) GiggzGreen else GiggzGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val features = listOf(
                "Access larger candidate pools" to true,
                "AI-powered candidate matching" to true,
                "Send recruitment invitations to multiple people" to true,
                "Advanced filters (School, Experience, Rating)" to true,
                "View verified profiles & portfolios" to false, // highlights
                "Contact selected candidates directly" to false,
                "Recruitment analytics dashboard" to false,
                "Save favourite candidates" to false
            )

            // Grid Layout for checklist
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                features.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { feature ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isUnlocked) GiggzGreen.copy(alpha = 0.05f)
                                        else Color.Transparent
                                    )
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isUnlocked) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = if (isUnlocked) GiggzGreen else GiggzGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = feature.first,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isUnlocked) textColor else subTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (pair.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// 4. Create Campaign Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCampaignDialog(
    typePreset: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onPublishCampaign: (RecruitCampaign) -> Unit
) {
    var orgName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var peopleNeeded by remember { mutableStateOf(10f) }
    var jobType by remember { mutableStateOf(if (typePreset == "Student") "Internship" else "Part-time") }
    var paymentRange by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("August 1, 2026") }
    var location by remember { mutableStateOf("") }
    var selectedLogoPreset by remember { mutableStateOf("TechCorp") } // "TechCorp", "EventPro", "EduMatch"

    val dialogBg = if (isDark) Color(0xFF1F2937) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    // Preset autocomplete helper based on selection type
    LaunchedEffect(typePreset) {
        if (typePreset == "Student") {
            title = "Academic Research & Marketing Interns"
            description = "We are seeking college students to join our marketing team for the summer. You will conduct market surveys, design campaigns, and present analytics."
            paymentRange = "$500 / Month"
            requirements = "Current student or recent graduate in Business, Marketing, or Communication."
            location = "Hybrid (Lusaka)"
            selectedLogoPreset = "EduMatch"
        } else if (typePreset == "Worker") {
            title = "Experienced General Electricians"
            description = "Hiring certified electricians for industrial grid setup. Responsibilities include running conduits, wiring panels, and ensuring safety compliance."
            paymentRange = "$35 / Hour"
            requirements = "Minimum 3 years experience, valid license, own basic hand tools."
            location = "Industrial Area, Ndola"
            selectedLogoPreset = "TechCorp"
        } else {
            title = "Need 30 Catering & Hospitality Workers"
            description = "Looking for a team of 30 hostesses, stewards, and kitchen staff for a multi-day international wedding reception. Meals and transport provided."
            paymentRange = "$120 / Flat Day"
            requirements = "Polite manner, experience in hospitality, smart presentation."
            location = "Radisson Blu, Lusaka"
            selectedLogoPreset = "EventPro"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            border = BorderStroke(1.5.dp, GiggzGreen)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        // Header 20% smaller (from 18sp to 14.5.sp)
                        Text(
                            text = "New Recruiter Campaign",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = orgName,
                        onValueChange = { orgName = it },
                        label = { Text("Organisation/Company Name", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Vertex Solutions", fontSize = 11.sp) },
                        textStyle = TextStyle(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GiggzGreen)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Recruitment Title", fontSize = 11.sp) },
                        textStyle = TextStyle(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GiggzGreen)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Campaign Description", fontSize = 11.sp) },
                        textStyle = TextStyle(fontSize = 11.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GiggzGreen)
                    )

                    // Slider for Number of People Needed
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Candidates Required:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text("${peopleNeeded.toInt()} People", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = GiggzGreen)
                        }
                        Slider(
                            value = peopleNeeded,
                            onValueChange = { peopleNeeded = it },
                            valueRange = 1f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = GiggzGreen,
                                activeTrackColor = GiggzGreen
                            )
                        )
                    }

                    // Job Type Radio Presets
                    Text("Job Placement Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Internship", "Part-time", "Temporary", "Full-time").forEach { type ->
                            val isSelected = jobType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) GiggzGreen.copy(alpha = 0.15f)
                                        else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GiggzGreen else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { jobType = type }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(type, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (isSelected) GiggzGreen else Color.Gray)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = paymentRange,
                        onValueChange = { paymentRange = it },
                        label = { Text("Payment / Budget Range", fontSize = 11.sp) },
                        placeholder = { Text("e.g. $25/Hr or $500/Month", fontSize = 11.sp) },
                        textStyle = TextStyle(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GiggzGreen)
                    )

                    OutlinedTextField(
                        value = requirements,
                        onValueChange = { requirements = it },
                        label = { Text("Candidate Requirements & Skills", fontSize = 11.sp) },
                        textStyle = TextStyle(fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GiggzGreen)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location", fontSize = 11.sp) },
                            textStyle = TextStyle(fontSize = 12.sp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GiggzGreen)
                        )

                        OutlinedTextField(
                            value = deadline,
                            onValueChange = { deadline = it },
                            label = { Text("Application Deadline", fontSize = 11.sp) },
                            textStyle = TextStyle(fontSize = 12.sp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GiggzGreen)
                        )
                    }

                    // Logo presets
                    Text("Select Company Logo / Brand Avatar:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "TechCorp" to "💻 TechCorp",
                            "EventPro" to "🎉 EventPro",
                            "EduMatch" to "🎓 EduMatch"
                        ).forEach { preset ->
                            val isSelected = selectedLogoPreset == preset.first
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) GiggzGold.copy(alpha = 0.15f)
                                        else if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GiggzGold else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedLogoPreset = preset.first }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(preset.second, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) GiggzGold else Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            if (orgName.isBlank() || title.isBlank()) {
                                // Simple validation
                                orgName = if (orgName.isBlank()) "Default Org" else orgName
                                title = if (title.isBlank()) "Standard Recruitment" else title
                            }
                            val camp = RecruitCampaign(
                                id = "camp-${System.currentTimeMillis()}",
                                organisationName = orgName,
                                recruitmentTitle = title,
                                description = description,
                                peopleNeeded = peopleNeeded.toInt(),
                                jobType = jobType,
                                paymentRange = paymentRange,
                                requirements = requirements,
                                deadline = deadline,
                                location = location,
                                logoPreset = selectedLogoPreset,
                                applicantsCount = 0,
                                shortlistedCount = 0,
                                hiredCount = 0
                            )
                            onPublishCampaign(camp)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Publish Campaign", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// 5. Payment System Selector
@Composable
fun PaymentSystemDialog(
    isDark: Boolean,
    walletBalance: Double,
    pendingCampaign: RecruitCampaign?,
    onDismiss: () -> Unit,
    onPaymentSuccess: (cost: Double, isSubscriptionUpgrade: Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedPaymentMethod by remember { mutableStateOf("wallet") } // "wallet", "mpesa", "card"
    var selectedPackageCost by remember { mutableStateOf(49.0) } // Default single campaign cost
    var selectedPackageName by remember { mutableStateOf("Single recruitment campaign") }
    var isSubscriptionUpgrade by remember { mutableStateOf(false) }

    LaunchedEffect(pendingCampaign) {
        if (pendingCampaign == null) {
            // Unlocked subscription selector default
            selectedPackageCost = 199.0
            selectedPackageName = "Monthly recruiter subscription"
            isSubscriptionUpgrade = true
        } else {
            selectedPackageCost = 49.0
            selectedPackageName = "Single recruitment campaign"
            isSubscriptionUpgrade = false
        }
    }

    val dialogBg = if (isDark) Color(0xFF1F2937) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val cardBg = if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            border = BorderStroke(1.5.dp, GiggzGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = GiggzGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Checkout Secure Payment",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Divider(color = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB))

                // Product details
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GiggzGold.copy(alpha = 0.12f))
                        .border(1.dp, GiggzGold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PREMIUM SERVICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GiggzGold)
                            Text(selectedPackageName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                        Text("$${selectedPackageCost.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = GiggzGold)
                    }
                }

                // If no pending campaign, show dynamic subscription options
                if (pendingCampaign == null) {
                    Text("Select Plan Tier:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            Triple("Monthly recruiter subscription", 199.0, "Best for scaling companies"),
                            Triple("Organisation VIP Package", 499.0, "Best for enterprise bulk hires")
                        ).forEach { plan ->
                            val isSelected = selectedPackageCost == plan.second
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPackageCost = plan.second
                                        selectedPackageName = plan.first
                                        isSubscriptionUpgrade = true
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) GiggzGold.copy(alpha = 0.08f) else cardBg
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) GiggzGold else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(plan.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                                        Text(plan.third, fontSize = 8.5.sp, color = Color.Gray)
                                    }
                                    Text("$${plan.second.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GiggzGold)
                                }
                            }
                        }
                    }
                }

                // Selectable payment methods
                Text("Select Mobile Payment Mode:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Giggz Wallet Option
                    PaymentMethodRow(
                        title = "Giggz Secure Wallet",
                        subtitle = "Instant deduction • Balance: $${String.format("%.2f", walletBalance)}",
                        icon = Icons.Filled.AccountBalanceWallet,
                        isSelected = selectedPaymentMethod == "wallet",
                        onSelect = { selectedPaymentMethod = "wallet" },
                        isDark = isDark,
                        badgeText = if (walletBalance < selectedPackageCost) "INSUFFICIENT FUNDS" else null
                    )

                    // Mobile money M-Pesa / MTN
                    PaymentMethodRow(
                        title = "Mobile Wallet (M-Pesa / MTN)",
                        subtitle = "Pay securely via your register mobile number",
                        icon = Icons.Filled.PhoneAndroid,
                        isSelected = selectedPaymentMethod == "mpesa",
                        onSelect = { selectedPaymentMethod = "mpesa" },
                        isDark = isDark
                    )

                    // Card Payment
                    PaymentMethodRow(
                        title = "Credit or Debit Card",
                        subtitle = "Visa, Mastercard, or local bank card",
                        icon = Icons.Filled.CreditCard,
                        isSelected = selectedPaymentMethod == "card",
                        onSelect = { selectedPaymentMethod = "card" },
                        isDark = isDark
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Button
                val hasEnoughFunds = selectedPaymentMethod != "wallet" || walletBalance >= selectedPackageCost
                Button(
                    onClick = {
                        if (!hasEnoughFunds) {
                            context.showSafeToast("Insufficient wallet funds. Please Top Up or select Mobile Money!")
                        } else {
                            // payment simulation
                            onPaymentSuccess(selectedPackageCost, isSubscriptionUpgrade)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (hasEnoughFunds) GiggzGold.copy(alpha = 0.75f) else Color.Gray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (hasEnoughFunds) "Authorize Pay $${selectedPackageCost.toInt()}" else "Authorize (Insufficient Balance)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = RecruitDarkBg
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isDark: Boolean,
    badgeText: String? = null
) {
    val cardBg = if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GiggzGreen.copy(alpha = 0.08f) else cardBg
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) GiggzGreen else if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) GiggzGreen else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(subtitle, fontSize = 8.sp, color = Color.Gray)
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Red.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(badgeText, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        }
                    }
                }
            }
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = GiggzGreen)
            )
        }
    }
}

// 6. Campaign Item Renderer in Recruiter Dashboard
@Composable
fun CampaignDashboardItem(
    campaign: RecruitCampaign,
    isDark: Boolean,
    onViewAnalytics: () -> Unit,
    onManageApplicants: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1F2937) else Color.White // match other cards in light mode
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563)

    // Preset Logo resolution
    val logoEmoji = when (campaign.logoPreset) {
        "TechCorp" -> "💻"
        "EventPro" -> "🎉"
        "EduMatch" -> "🎓"
        else -> "🏢"
    }

    val logoColor = when (campaign.logoPreset) {
        "TechCorp" -> Color(0xFF3B82F6)
        "EventPro" -> Color(0xFFEC4899)
        "EduMatch" -> Color(0xFF10B981)
        else -> GiggzGold
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(logoColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(logoEmoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        // Subheadings are 20% smaller (from 14sp to 11.sp)
                        Text(
                            text = campaign.recruitmentTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${campaign.organisationName} • ${campaign.location}",
                            fontSize = 8.5.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GiggzGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = GiggzGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = campaign.description,
                fontSize = 10.sp,
                color = subTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF111827) else Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CampaignMetric(label = "People Needed", value = "${campaign.peopleNeeded}", color = textColor)
                CampaignMetric(label = "Applicants", value = "${campaign.applicantsCount}", color = GiggzMint)
                CampaignMetric(label = "Shortlisted", value = "${campaign.shortlistedCount}", color = GiggzGold)
                CampaignMetric(label = "Hired", value = "${campaign.hiredCount}", color = GiggzGreen)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CTA Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewAnalytics,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.BarChart, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Campaign Performance", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                Button(
                    onClick = onManageApplicants,
                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.People, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Review Applicants", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CampaignMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 7.5.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
    }
}

// 7. Interactive Campaign Details & Analytics Dashboard Composable Dialog
@Composable
fun CampaignDetailsDashboardDialog(
    campaign: RecruitCampaign,
    tab: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onUpdateCampaignStats: (RecruitCampaign, applicants: Int, shortlisted: Int, hired: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val dialogBg = if (isDark) Color(0xFF1F2937) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)
    val cardBg = if (isDark) Color(0xFF111827) else Color(0xFFF9FAFB)

    var currentTab by remember { mutableStateOf(tab) } // "overview", "applicants"

    // Simulate dummy applicants
    val initialApplicants = remember(campaign.id) {
        listOf(
            Triple("Mulenga Mwansa", "UNZA Student • Computer Science", "Applied 1d ago"),
            Triple("Chipo Sampa", "Certified Hospitality Hostess • 2 yrs exp", "Applied 2d ago"),
            Triple("Bwalya Tembo", "Event Steward • 1 yr exp", "Applied 2d ago"),
            Triple("Natasha Phiri", "UX Design Student • Evelyn Hone", "Applied 3d ago"),
            Triple("Kabwe Chola", "Freelance Logistics Coordinator", "Applied 4d ago")
        )
    }

    var applicantListState by remember { mutableStateOf(initialApplicants) }
    var shortlistedState by remember { mutableStateOf(campaign.shortlistedCount) }
    var hiredState by remember { mutableStateOf(campaign.hiredCount) }
    var applicantsCountState by remember { mutableStateOf(campaign.applicantsCount) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            border = BorderStroke(1.5.dp, GiggzGreen)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = campaign.organisationName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GiggzGold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Title (20% smaller from 18sp to 14.sp)
                Text(
                    text = campaign.recruitmentTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Segmented Control Tab Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardBg)
                        .padding(3.dp)
                ) {
                    listOf("overview" to "Campaign Analytics", "applicants" to "Review Candidates").forEach { item ->
                        val isSelected = currentTab == item.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) GiggzGreen else Color.Transparent)
                                .clickable { currentTab = item.first }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.second,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB))

                // Body Section depending on tab selected
                Box(modifier = Modifier.weight(1f)) {
                    if (currentTab == "overview") {
                        // Analytics with Custom Compose Native Performance Bar Charts
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text("Campaign Funnel Analytics", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)

                            // Native bar progress illustration representing campaign funnel
                            FunnelBarChartRow(label = "Impressions (Views)", value = "1,420 views", progress = 1.0f, color = Color(0xFF3B82F6))
                            FunnelBarChartRow(label = "Applications Received", value = "$applicantsCountState users", progress = (applicantsCountState / 100f).coerceAtLeast(0.15f), color = GiggzMint)
                            FunnelBarChartRow(label = "Shortlisted Match", value = "$shortlistedState candidates", progress = (shortlistedState / 30f).coerceAtLeast(0.08f), color = GiggzGold)
                            FunnelBarChartRow(label = "Offers accepted & hired", value = "$hiredState workers", progress = (hiredState / 20f).coerceAtLeast(0.04f), color = GiggzGreen)

                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB))

                            // Interactive Smart Recommendation Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GiggzGreen.copy(alpha = 0.08f))
                                    .border(1.dp, GiggzGreen.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = "AI Recommendation", tint = GiggzGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("AI Recruiter Recommendation", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GiggzGreen)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Based on current market trends, your pay rate of ${campaign.paymentRange} is highly competitive! 84% of premium workers matched with your requirements are available for immediate onboarding.",
                                            fontSize = 8.5.sp,
                                            color = textColor,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Review candidates applicant list
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Candidates for review (${applicantListState.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                                Text("Click card to shortlist/hire", fontSize = 8.5.sp, color = Color.Gray)
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(applicantListState) { applicant ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = cardBg)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .clip(CircleShape)
                                                        .background(GiggzGreen.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(applicant.first.split(" ").map { it.take(1) }.joinToString(""), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GiggzGreen)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(applicant.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                                                    Text(applicant.second, fontSize = 8.sp, color = Color.Gray)
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                // Shortlist Button
                                                IconButton(
                                                    onClick = {
                                                        shortlistedState++
                                                        applicantListState = applicantListState.filter { it.first != applicant.first }
                                                        onUpdateCampaignStats(campaign, applicantsCountState, shortlistedState, hiredState)
                                                    },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(GiggzGold.copy(alpha = 0.15f))
                                                ) {
                                                    Icon(Icons.Filled.Star, contentDescription = "Shortlist", tint = GiggzGold, modifier = Modifier.size(14.dp))
                                                }

                                                // Hire Button
                                                IconButton(
                                                    onClick = {
                                                        hiredState++
                                                        applicantListState = applicantListState.filter { it.first != applicant.first }
                                                        onUpdateCampaignStats(campaign, applicantsCountState, shortlistedState, hiredState)
                                                    },
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(GiggzGreen.copy(alpha = 0.15f))
                                                ) {
                                                    Icon(Icons.Filled.Check, contentDescription = "Hire", tint = GiggzGreen, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FunnelBarChartRow(label: String, value: String, progress: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(value, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Progress bar representing relative volume
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

// Success Celebration Dialog overlay (Renders when upgrades or campaigns are paid)
@Composable
fun SuccessCelebrationDialog(
    title: String,
    message: String,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val dialogBg = if (isDark) Color(0xFF1F2937) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF111827)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            border = BorderStroke(2.dp, GiggzGreen)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(GiggzGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Success", tint = GiggzGreen, modifier = Modifier.size(40.dp))
                }

                // Headings are 20% smaller (from 20sp/22sp to 16.sp)
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = message,
                    fontSize = 11.5.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Superb!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
