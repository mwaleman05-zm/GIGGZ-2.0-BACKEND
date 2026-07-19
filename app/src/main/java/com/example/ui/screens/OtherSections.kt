package com.example.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import android.widget.Toast
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.ui.components.showSafeToast
import com.example.ui.components.ImageAttachmentPicker
import com.example.ui.components.JobDetailDialog
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.JobEntity
import com.example.data.MessageEntity
import com.example.data.UserEntity
import com.example.data.EventEntity
import com.example.ui.GiggzViewModel
import com.example.ui.components.CategoryBadge
import com.example.ui.components.RatingStars
import com.example.ui.components.GiggzReputationCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.GiggzGold
import com.example.ui.theme.GiggzGreen

// =============================================================================
// PIECE WORKS SECTION
// =============================================================================

@Composable
fun PieceWorksSection(
    viewModel: GiggzViewModel
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allApplications by viewModel.allApplications.collectAsStateWithLifecycle()
    val pieceWorks by viewModel.allPieceWorks.collectAsStateWithLifecycle()
    val savedJobIds by viewModel.savedJobIds.collectAsStateWithLifecycle()

    var selectedTimeRequired by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    var minPayInput by remember { mutableStateOf("") }
    var locationInput by remember(currentUser) { mutableStateOf(currentUser?.location ?: "") }

    var showFiltersSheet by remember { mutableStateOf(false) }
    var selectedJobForDetail by remember { mutableStateOf<JobEntity?>(null) }

    // Piece Works categories
    val categories = listOf(
        "All",
        "Carpentry",
        "Plumbing",
        "Electrical Wiring",
        "Electronics Repair",
        "Painting & Decoration",
        "General Cleaning",
        "Gardening & Landscaping",
        "Catering & Cooking",
        "Photography & Video",
        "Mobile App Development",
        "Web & Graphic Design",
        "Furniture Making",
        "Welding & Metalwork",
        "Automotive Mechanics",
        "Tailoring & Fashion Design",
        "Academic Tutoring",
        "Health & Wellness",
        "Real Estate Services",
        "Other Casual Gigs"
    )
    val timeRequiredOptions = listOf("All", "2 hours", "Same Day", "Weekend")

    // Filtered pieces
    val userLoc = currentUser?.location?.trim()?.lowercase() ?: ""
    val filteredPieceWorks = remember(pieceWorks, selectedCategory, selectedTimeRequired, locationInput, minPayInput, userLoc) {
        pieceWorks.filter { job ->
            job.status == "Active" &&
            (selectedCategory == "All" || job.category == selectedCategory) &&
            (selectedTimeRequired == "All" || job.timeRequired == selectedTimeRequired) &&
            (locationInput.isBlank() || job.location.lowercase().contains(locationInput.lowercase())) &&
            (minPayInput.isBlank() || job.budget >= (minPayInput.toDoubleOrNull() ?: 0.0))
        }.sortedWith(compareByDescending { job ->
            userLoc.isNotEmpty() && (
                job.location.lowercase().contains(userLoc) ||
                userLoc.contains(job.location.lowercase())
            )
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Filters Header Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Same-Day & Hourly Gigs (${filteredPieceWorks.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            OutlinedButton(
                onClick = { showFiltersSheet = !showFiltersSheet },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(34.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Filters", fontSize = 11.sp)
            }
        }

        // Inline filters drawers
        AnimatedVisibility(visible = showFiltersSheet) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Filter Casual Gigs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GiggzGreen)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Category selection
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Category", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(6.dp)).clickable { expanded = true }.padding(8.dp)) {
                                Text(selectedCategory, fontSize = 11.sp)
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expanded = false })
                                    }
                                }
                            }
                        }

                        // Time frame
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Time Required", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            var expandedTime by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(6.dp)).clickable { expandedTime = true }.padding(8.dp)) {
                                Text(selectedTimeRequired, fontSize = 11.sp)
                                DropdownMenu(expanded = expandedTime, onDismissRequest = { expandedTime = false }) {
                                    timeRequiredOptions.forEach { to ->
                                        DropdownMenuItem(text = { Text(to) }, onClick = { selectedTimeRequired = to; expandedTime = false })
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minPayInput,
                            onValueChange = { minPayInput = it },
                            label = { Text("Min Wage (K)") },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = TextStyle(fontSize = 12.sp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = { locationInput = it },
                            label = { Text("Area/Location") },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = TextStyle(fontSize = 12.sp),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            selectedCategory = "All"
                            selectedTimeRequired = "All"
                            minPayInput = ""
                            locationInput = ""
                            showFiltersSheet = false
                        }) {
                            Text("Reset", color = Color.Red, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Feed list
        if (filteredPieceWorks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No short-term casual gigs found.", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredPieceWorks) { gig ->
                    val isBookmarked = savedJobIds.contains(gig.id)
                    val hasApplied = currentUser?.let { worker ->
                        allApplications.any { it.jobId == gig.id && it.workerId == worker.id }
                    } ?: false
                    PieceGigItemCard(
                        gig = gig,
                        isBookmarked = isBookmarked,
                        hasApplied = hasApplied,
                        onBookmark = { viewModel.saveJob(gig.id) },
                        onApplyClick = {
                            viewModel.applyForJob(gig, "Applying for instant ${gig.timeRequired} contract.", "", "") {
                                context.showSafeToast("Instant Application Submitted!")
                            }
                        },
                        onProfileClick = {
                            viewModel.showUserProfile(gig.employerId)
                        },
                        onClick = {
                            selectedJobForDetail = gig
                        }
                    )
                }
            }
        }
    }

    if (selectedJobForDetail != null) {
        val detailJob = selectedJobForDetail!!
        val isBookmarked = savedJobIds.contains(detailJob.id)
        val hasApplied = currentUser?.let { worker ->
            allApplications.any { it.jobId == detailJob.id && it.workerId == worker.id }
        } ?: false
        val coroutineScope = rememberCoroutineScope()
        JobDetailDialog(
            job = detailJob,
            isBookmarked = isBookmarked,
            hasApplied = hasApplied,
            onDismissRequest = { selectedJobForDetail = null },
            onBookmark = { viewModel.saveJob(detailJob.id) },
            onApplyClick = {
                selectedJobForDetail = null
                viewModel.applyForJob(detailJob, "Applying for instant ${detailJob.timeRequired} contract.", "", "") {
                    context.showSafeToast("Instant Application Submitted!")
                }
            },
            onChatClick = {
                selectedJobForDetail = null
                coroutineScope.launch {
                    val employer = viewModel.allUsers.value.find { it.id == detailJob.employerId }
                    if (employer != null) {
                        viewModel.selectChatPartner(employer)
                    } else {
                        context.showSafeToast("Employer profile offline.")
                    }
                }
            },
            onProfileClick = {
                selectedJobForDetail = null
                viewModel.showUserProfile(detailJob.employerId)
            }
        )
    }
}

data class GigCategoryStyle(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val bgColor: Color,
    val iconTint: Color
)

@Composable
fun getGigCategoryStyle(category: String): GigCategoryStyle {
    val lower = category.lowercase()
    return when {
        lower.contains("clean") || lower.contains("wash") -> GigCategoryStyle(
            icon = Icons.Filled.Home,
            bgColor = Color(0xFF8E24AA), // Darker premium purple
            iconTint = Color(0xFFF3E5F5) // Soft contrast light purple
        )
        lower.contains("deliver") || lower.contains("courier") || lower.contains("logistic") || lower.contains("transport") -> GigCategoryStyle(
            icon = Icons.Filled.LocalShipping,
            bgColor = Color(0xFF2E7D32), // Darker premium green
            iconTint = Color(0xFFE8F5E9) // Soft contrast light green
        )
        lower.contains("plumb") || lower.contains("leak") || lower.contains("water") || lower.contains("pipe") -> GigCategoryStyle(
            icon = Icons.Filled.Build,
            bgColor = Color(0xFFD84315), // Darker premium orange-red
            iconTint = Color(0xFFFBE9E7) // Soft contrast light orange
        )
        lower.contains("design") || lower.contains("code") || lower.contains("mobile") || lower.contains("app") || lower.contains("web") || lower.contains("software") || lower.contains("tech") -> GigCategoryStyle(
            icon = Icons.Filled.Work,
            bgColor = Color(0xFF1565C0), // Darker premium blue
            iconTint = Color(0xFFE3F2FD) // Soft contrast light blue
        )
        lower.contains("carpentry") || lower.contains("wood") || lower.contains("furniture") || lower.contains("fix") || lower.contains("handy") -> GigCategoryStyle(
            icon = Icons.Filled.Build,
            bgColor = Color(0xFF4E342E), // Darker premium brown
            iconTint = Color(0xFFEFEBE9) // Soft contrast light brown
        )
        lower.contains("electric") || lower.contains("wire") || lower.contains("solar") || lower.contains("power") -> GigCategoryStyle(
            icon = Icons.Filled.Build,
            bgColor = Color(0xFFF9A825), // Darker premium golden yellow
            iconTint = Color(0xFFFFFDE7) // Soft contrast light yellow
        )
        else -> GigCategoryStyle(
            icon = Icons.Filled.Work,
            bgColor = Color(0xFF37474F), // Darker premium slate grey
            iconTint = Color(0xFFECEFF1) // Soft contrast light grey
        )
    }
}

@Composable
fun CasualGigAnimatedIllustration(category: String, modifier: Modifier = Modifier) {
    val lower = category.lowercase()
    val infiniteTransition = rememberInfiniteTransition(label = "IllustrationLoop")
    
    // Smooth loops
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    // Vibrant colors
    val purpleColor = Color(0xFF7E57C2)
    val mintColor = Color(0xFF10B981)
    val goldColor = Color(0xFFF59E0B)

    val (bgColor, drawBlock) = when {
        lower.contains("clean") || lower.contains("wash") -> {
            // Cleaning / Washing: bouncing soapy bubbles and shining sparkles
            val bubble1Y by infiniteTransition.animateFloat(
                initialValue = 48f,
                targetValue = 16f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "bubble1"
            )
            val bubble2Y by infiniteTransition.animateFloat(
                initialValue = 54f,
                targetValue = 20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "bubble2"
            )
            val starScale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "star"
            )

            Pair(Color(0xFFE0F7FA)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height
                val center = Offset(w / 2, h / 2)

                drawScope.drawCircle(
                    color = Color(0xFF26C6DA).copy(alpha = 0.15f),
                    radius = w * 0.28f,
                    center = center
                )
                
                drawScope.drawCircle(
                    color = Color(0xFF00ACC1).copy(alpha = 0.6f),
                    radius = 7f,
                    center = Offset(w * 0.35f, bubble1Y)
                )
                drawScope.drawCircle(
                    color = Color(0xFF00ACC1).copy(alpha = 0.75f),
                    radius = 10f,
                    center = Offset(w * 0.65f, bubble2Y)
                )

                drawScope.drawCircle(
                    color = Color(0xFF00ACC1),
                    radius = w * 0.25f,
                    center = center,
                    style = Stroke(width = 3.5f)
                )

                val starCenter1 = Offset(w * 0.72f, h * 0.28f)
                val starSize1 = 11f * starScale
                val starPath1 = Path().apply {
                    moveTo(starCenter1.x, starCenter1.y - starSize1)
                    quadraticTo(starCenter1.x, starCenter1.y, starCenter1.x + starSize1, starCenter1.y)
                    quadraticTo(starCenter1.x, starCenter1.y, starCenter1.x, starCenter1.y + starSize1)
                    quadraticTo(starCenter1.x, starCenter1.y, starCenter1.x - starSize1, starCenter1.y)
                    quadraticTo(starCenter1.x, starCenter1.y, starCenter1.x, starCenter1.y - starSize1)
                }
                drawScope.drawPath(path = starPath1, color = Color(0xFFFFD54F))
            }
        }
        lower.contains("deliver") || lower.contains("courier") || lower.contains("logistic") || lower.contains("transport") || lower.contains("drive") || lower.contains("road") || lower.contains("car") || lower.contains("truck") || lower.contains("move") || lower.contains("packing") -> {
            val boxBounce by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "boxBounce"
            )
            val lineShift by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 48f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "lineShift"
            )

            Pair(Color(0xFFE8F5E9)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height
                val roadY = h * 0.78f

                drawScope.drawLine(
                    color = mintColor.copy(alpha = 0.2f),
                    start = Offset(0f, roadY),
                    end = Offset(w, roadY),
                    strokeWidth = 3f
                )

                drawScope.drawLine(
                    color = mintColor.copy(alpha = 0.5f),
                    start = Offset((w * 0.1f - lineShift + w) % w, roadY + 4f),
                    end = Offset((w * 0.4f - lineShift + w) % w, roadY + 4f),
                    strokeWidth = 4f
                )

                val boxWidth = w * 0.38f
                val boxHeight = h * 0.35f
                val boxX = (w - boxWidth) / 2
                val boxY = (h - boxHeight) / 2 - 4f + boxBounce

                drawScope.drawRect(
                    color = Color(0xFFD7CCC8),
                    topLeft = Offset(boxX, boxY),
                    size = Size(boxWidth, boxHeight)
                )
                drawScope.drawRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(boxX, boxY),
                    size = Size(boxWidth, boxHeight),
                    style = Stroke(width = 3.5f)
                )
                drawScope.drawRect(
                    color = Color(0xFF81C784),
                    topLeft = Offset(boxX + boxWidth * 0.42f, boxY),
                    size = Size(boxWidth * 0.16f, boxHeight)
                )
            }
        }
        lower.contains("plumb") || lower.contains("leak") || lower.contains("water") || lower.contains("pipe") -> {
            val dripY by infiniteTransition.animateFloat(
                initialValue = 0.35f,
                targetValue = 0.82f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutLinearInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "dripY"
            )
            val waveRadius by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 22f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "wave"
            )

            Pair(Color(0xFFE3F2FD)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height

                val faucet = Path().apply {
                    moveTo(w * 0.15f, h * 0.3f)
                    lineTo(w * 0.52f, h * 0.3f)
                    quadraticTo(w * 0.62f, h * 0.3f, w * 0.62f, h * 0.4f)
                    lineTo(w * 0.62f, h * 0.46f)
                }
                drawScope.drawPath(
                    path = faucet,
                    color = Color(0xFF1E88E5),
                    style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                if (dripY < 0.81f) {
                    val dropPath = Path().apply {
                        val cx = w * 0.62f
                        val cy = h * dripY
                        moveTo(cx, cy - 6f)
                        quadraticTo(cx + 4f, cy + 2f, cx, cy + 6f)
                        quadraticTo(cx - 4f, cy + 2f, cx, cy - 6f)
                    }
                    drawScope.drawPath(dropPath, color = Color(0xFF29B6F6))
                }

                val fade = (1f - (waveRadius / 22f)).coerceIn(0f, 1f)
                drawScope.drawCircle(
                    color = Color(0xFF0288D1).copy(alpha = fade),
                    radius = waveRadius,
                    center = Offset(w * 0.62f, h * 0.82f),
                    style = Stroke(width = 2.5f)
                )
            }
        }
        lower.contains("garden") || lower.contains("grass") || lower.contains("plant") || lower.contains("flower") || lower.contains("lawn") -> {
            val plantSway by infiniteTransition.animateFloat(
                initialValue = -7f,
                targetValue = 7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "plantSway"
            )

            Pair(Color(0xFFE8F5E9)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height

                drawScope.withTransform({
                    rotate(rotation, pivot = Offset(w * 0.78f, h * 0.22f))
                }) {
                    for (i in 0 until 6) {
                        val rayAngle = i * 60f
                        rotate(rayAngle, pivot = Offset(w * 0.78f, h * 0.22f)) {
                            drawLine(
                                color = Color(0xFFFFB300).copy(alpha = 0.5f),
                                start = Offset(w * 0.78f, h * 0.12f),
                                end = Offset(w * 0.78f, h * 0.16f),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                drawScope.withTransform({
                    rotate(plantSway, pivot = Offset(w * 0.48f, h * 0.85f))
                }) {
                    drawScope.drawLine(
                        color = Color(0xFF8D6E63),
                        start = Offset(w * 0.25f, h * 0.85f),
                        end = Offset(w * 0.7f, h * 0.85f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    drawScope.drawLine(
                        color = Color(0xFF4CAF50),
                        start = Offset(w * 0.48f, h * 0.85f),
                        end = Offset(w * 0.48f, h * 0.44f),
                        strokeWidth = 4.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        lower.contains("tutor") || lower.contains("teach") || lower.contains("educat") || lower.contains("book") || lower.contains("class") -> {
            val pagesGlow by infiniteTransition.animateFloat(
                initialValue = 0.86f,
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1300, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "book"
            )

            Pair(Color(0xFFEDE7F6)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height

                val spine = Path().apply {
                    moveTo(w * 0.2f, h * 0.44f)
                    quadraticTo(w * 0.5f, h * 0.49f, w * 0.8f, h * 0.44f)
                    lineTo(w * 0.8f, h * 0.78f)
                    quadraticTo(w * 0.5f, h * 0.83f, w * 0.2f, h * 0.78f)
                    close()
                }
                drawScope.drawPath(spine, color = Color(0xFF5C6BC0))

                drawScope.withTransform({
                    scale(pagesGlow, 1.0f, pivot = Offset(w * 0.5f, h * 0.6f))
                }) {
                    val pL = Path().apply {
                        moveTo(w * 0.23f, h * 0.41f)
                        quadraticTo(w * 0.5f, h * 0.46f, w * 0.5f, h * 0.75f)
                        lineTo(w * 0.23f, h * 0.72f)
                        close()
                    }
                    val pR = Path().apply {
                        moveTo(w * 0.77f, h * 0.41f)
                        quadraticTo(w * 0.5f, h * 0.46f, w * 0.5f, h * 0.75f)
                        lineTo(w * 0.77f, h * 0.72f)
                        close()
                    }
                    drawPath(pL, color = Color.White)
                    drawPath(pR, color = Color.White)
                }
            }
        }
        lower.contains("photo") || lower.contains("camera") || lower.contains("video") || lower.contains("lens") -> {
            val flashScale by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "flashScale"
            )

            Pair(Color(0xFFECEFF1)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height
                val center = Offset(w / 2, h / 2)

                drawScope.drawRoundRect(
                    color = Color(0xFF263238),
                    topLeft = Offset(w * 0.22f, h * 0.35f),
                    size = Size(w * 0.56f, h * 0.4f),
                    cornerRadius = CornerRadius(10f)
                )

                drawScope.drawCircle(
                    color = Color(0xFFCFD8DC),
                    radius = w * 0.16f,
                    center = center,
                    style = Stroke(width = 4f)
                )

                drawScope.withTransform({
                    rotate(rotation, pivot = center)
                }) {
                    for (i in 0 until 5) {
                        val rotAngle = i * 72f
                        rotate(rotAngle, pivot = center) {
                            drawLine(
                                color = Color(0xFF80DEEA),
                                start = Offset(w * 0.5f, h * 0.44f),
                                end = Offset(w * 0.56f, h * 0.46f),
                                strokeWidth = 2.5f
                            )
                        }
                    }
                }

                val alphaVal = (1f - flashScale).coerceIn(0f, 1f)
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = alphaVal),
                    radius = w * 0.15f + (flashScale * 25f),
                    center = Offset(w * 0.7f, h * 0.33f),
                    style = Stroke(width = 2f)
                )
            }
        }
        lower.contains("electric") || lower.contains("wire") || lower.contains("solar") || lower.contains("power") || lower.contains("lightning") -> {
            val glowScale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glowScale"
            )

            Pair(Color(0xFFFFFDE7)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height

                drawScope.drawCircle(
                    color = Color(0xFFFFE082).copy(alpha = 0.2f * glowScale),
                    radius = w * 0.36f,
                    center = Offset(w * 0.5f, h * 0.44f)
                )

                drawScope.drawCircle(
                    color = Color(0xFFFFCA28),
                    radius = w * 0.16f,
                    center = Offset(w * 0.5f, h * 0.44f)
                )

                val bolt = Path().apply {
                    moveTo(w * 0.51f, h * 0.36f)
                    lineTo(w * 0.44f, h * 0.47f)
                    lineTo(w * 0.5f, h * 0.47f)
                    lineTo(w * 0.48f, h * 0.54f)
                    lineTo(w * 0.56f, h * 0.43f)
                    lineTo(w * 0.5f, h * 0.43f)
                    close()
                }
                drawScope.drawPath(bolt, color = Color.White)
            }
        }
        lower.contains("carpentry") || lower.contains("wood") || lower.contains("furniture") || lower.contains("fix") || lower.contains("handy") -> {
            val woodSawX by infiniteTransition.animateFloat(
                initialValue = -12f,
                targetValue = 12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "woodSawX"
            )

            Pair(Color(0xFFEFEBE9)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height

                drawScope.drawRoundRect(
                    color = Color(0xFF8D6E63),
                    topLeft = Offset(w * 0.22f, h * 0.54f),
                    size = Size(w * 0.56f, h * 0.16f),
                    cornerRadius = CornerRadius(4f)
                )

                val blade = Path().apply {
                    moveTo(w * 0.18f + woodSawX, h * 0.51f)
                    lineTo(w * 0.56f + woodSawX, h * 0.51f)
                    for (tx in (w * 0.56f + woodSawX).toInt() downTo (w * 0.18f + woodSawX).toInt() step 6) {
                        lineTo(tx.toFloat() - 3, h * 0.55f)
                        lineTo(tx.toFloat() - 6, h * 0.51f)
                    }
                    close()
                }
                drawScope.drawPath(blade, color = Color(0xFFCFD8DC))
            }
        }
        lower.contains("cook") || lower.contains("food") || lower.contains("chef") || lower.contains("bake") -> {
            val steamFloatY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1100, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "steamFloatY"
            )

            Pair(Color(0xFFFFEBEE)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height

                val v1 = Path().apply {
                    moveTo(w * 0.42f, h * 0.44f + steamFloatY)
                    quadraticTo(w * 0.39f, h * 0.38f + steamFloatY, w * 0.42f, h * 0.32f + steamFloatY)
                    quadraticTo(w * 0.45f, h * 0.26f + steamFloatY, w * 0.42f, h * 0.20f + steamFloatY)
                }
                drawScope.drawPath(v1, color = Color(0xFFEF9A9A), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

                drawScope.drawArc(
                    color = Color(0xFFE57373),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.28f, h * 0.45f),
                    size = Size(w * 0.44f, h * 0.28f)
                )
            }
        }
        lower.contains("design") || lower.contains("code") || lower.contains("mobile") || lower.contains("app") || lower.contains("web") || lower.contains("software") || lower.contains("tech") -> {
            val codeBlink by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "codeBlink"
            )

            Pair(Color(0xFFE1F5FE)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height

                drawScope.drawRoundRect(
                    color = Color(0xFF1A237E),
                    topLeft = Offset(w * 0.16f, h * 0.28f),
                    size = Size(w * 0.68f, h * 0.44f),
                    cornerRadius = CornerRadius(8f)
                )

                val brL = Path().apply {
                    moveTo(w * 0.38f, h * 0.38f)
                    lineTo(w * 0.28f, h * 0.5f)
                    lineTo(w * 0.38f, h * 0.62f)
                }
                drawScope.drawPath(
                    path = brL,
                    color = Color(0xFF00E5FF),
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                val brR = Path().apply {
                    moveTo(w * 0.62f, h * 0.38f)
                    lineTo(w * 0.72f, h * 0.5f)
                    lineTo(w * 0.62f, h * 0.62f)
                }
                drawScope.drawPath(
                    path = brR,
                    color = Color(0xFF00E5FF).copy(alpha = codeBlink),
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        else -> {
            Pair(Color(0xFFECEFF1)) { drawScope: DrawScope ->
                val w = drawScope.size.width
                val h = drawScope.size.height
                val center = Offset(w / 2, h / 2)

                drawScope.withTransform({
                    rotate(rotation, pivot = center)
                }) {
                    drawCircle(Color(0xFF90A4AE), radius = w * 0.18f, center = center)
                    for (i in 0 until 6) {
                        val gAngle = i * 60f
                        rotate(gAngle, pivot = center) {
                            drawRect(
                                color = Color(0xFF90A4AE),
                                topLeft = Offset(center.x - w * 0.04f, center.y - w * 0.24f),
                                size = Size(w * 0.08f, w * 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .size(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.linearGradient(
                        listOf(
                            purpleColor.copy(alpha = 0.6f),
                            mintColor.copy(alpha = 0.6f)
                        )
                    )
                ),
                RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            drawBlock(this)
        }
    }
}

@Composable
fun PieceGigItemCard(
    gig: JobEntity,
    isBookmarked: Boolean,
    hasApplied: Boolean = false,
    onBookmark: () -> Unit,
    onApplyClick: () -> Unit,
    onProfileClick: () -> Unit,
    onClick: () -> Unit
) {
    val style = getGigCategoryStyle(gig.category)
    val formattedName = remember(gig.employerName) {
        val parts = gig.employerName.split(" ")
        if (parts.size >= 2) {
            val secondPart = parts[1]
            val initial = if (secondPart.isNotEmpty()) "${secondPart.first().uppercase()}." else ""
            "${parts[0]} $initial"
        } else {
            gig.employerName
        }
    }
    val ratingVal = remember(gig.employerId) {
        val base = 4.5 + (gig.employerId % 5) * 0.1
        if (base > 5.0) 5.0 else base
    }
    val ratingStr = remember(ratingVal) { String.format("%.1f", ratingVal) }
    
    // Encouraging labels based on mock rating score to make it user/beginner friendly!
    val ratingLabel = remember(ratingVal) {
        when {
            ratingVal >= 4.9 -> "Elite Employer"
            ratingVal >= 4.7 -> "Highly Rated"
            else -> "Verified Employer"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF7E57C2).copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Core Details Section: Left side has the Animation, Right has the Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Left square container with colorful looping illustration
                CasualGigAnimatedIllustration(
                    category = gig.category,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Middle section representing job parameters next to animation
                Column(modifier = Modifier.weight(1f)) {
                    // Category Badge Row with friendly Ratings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Category Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(style.bgColor)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = gig.category,
                                color = style.iconTint,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Star badge representing verified rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating Badge",
                                tint = GiggzGold,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Title
                    Text(
                        text = gig.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.5.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Description (maximum 2 lines)
                    Text(
                        text = gig.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Location and Posted Time / Deadline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = gig.location,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Date Posted",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = gig.deadline,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Divider styled subtly to match light & dark theme elegantly
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Lower Action and Employer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Employer Info Section (Beginner friendly & clickable)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onProfileClick() }
                        .padding(horizontal = 3.dp, vertical = 1.0.dp)
                ) {
                    UserAvatar(photoUrl = gig.employerPhoto, name = gig.employerName, size = 24)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = formattedName,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Gig Owner",
                            fontSize = 7.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Wage & Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Budget/Wage pill
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        Text(
                            text = "K${gig.budget.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981) // Beautiful emerald Giggz Green wage
                        )
                        Text(
                            text = gig.payType,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    // Bookmark
                    IconButton(
                        onClick = onBookmark,
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) GiggzGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Apply Button: Styled with Giggz Green branding
                    Button(
                        onClick = onApplyClick,
                        enabled = !hasApplied,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GiggzGreen,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Text(
                            text = if (hasApplied) "Applied ✓" else "Apply Now",
                            fontSize = 9.5.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


// =============================================================================
// MESSAGES / CHAT CENTER
// =============================================================================

@Composable
fun MessagesCenterSection(
    viewModel: GiggzViewModel
) {
    val context = LocalContext.current
    val currentUser = viewModel.currentUser.collectAsStateWithLifecycle().value ?: return
    val chatPartner by viewModel.chatPartner.collectAsStateWithLifecycle()
    val chatPartners by viewModel.chatPartners.collectAsStateWithLifecycle()
    val allApplications by viewModel.allApplications.collectAsStateWithLifecycle()
    val allListings by viewModel.allListings.collectAsStateWithLifecycle()
    val allPieceWorks by viewModel.allPieceWorks.collectAsStateWithLifecycle()
    val currentThemeName by viewModel.currentTheme.collectAsStateWithLifecycle()
    val activeWallpaper by viewModel.chatWallpaper.collectAsStateWithLifecycle()
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    if (chatPartner != null) {
        // Individual premium chat window
        val partner = chatPartner!!
        val activeMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
        val isPartnerTyping by viewModel.isPartnerTyping.collectAsStateWithLifecycle()

        var currentText by remember { mutableStateOf("") }
        var replyToId by remember { mutableStateOf<Int?>(null) }
        var replyToText by remember { mutableStateOf<String?>(null) }
        var selectedChatJobDetail by remember { mutableStateOf<JobEntity?>(null) }

        // Popup and dialog states
        var showAttachmentDialog by remember { mutableStateOf(false) }
        var showEmojiPanel by remember { mutableStateOf(false) }
        var showVoiceRecorder by remember { mutableStateOf(false) }
        var isRecordingVoice by remember { mutableStateOf(false) }
        var voiceTimerSeconds by remember { mutableStateOf(0) }
        var showFullScreenImage by remember { mutableStateOf<String?>(null) }
        var showCallingDialog by remember { mutableStateOf<String?>(null) } // "Audio" or "Video"
        var showMenuDropdown by remember { mutableStateOf(false) }
        var showSearchDialog by remember { mutableStateOf(false) }
        var searchInChatQuery by remember { mutableStateOf("") }
        var messageToOptionMenu by remember { mutableStateOf<MessageEntity?>(null) }

        // Local state for media note playing
        var activeVoicePlayingId by remember { mutableStateOf<Int?>(null) }
        var voicePlayingProgress by remember { mutableStateOf(0f) }

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val listState = rememberLazyListState()

        LaunchedEffect(activeMessages.size, isPartnerTyping) {
            if (activeMessages.isNotEmpty() || isPartnerTyping) {
                listState.animateScrollToItem(activeMessages.size + (if (isPartnerTyping) 1 else 0))
            }
        }

        // Voice recorder simulator timer
        LaunchedEffect(isRecordingVoice) {
            if (isRecordingVoice) {
                voiceTimerSeconds = 0
                while (isRecordingVoice) {
                    delay(1000)
                    voiceTimerSeconds++
                }
            }
        }

        // Voice playback simulator timer
        LaunchedEffect(activeVoicePlayingId) {
            if (activeVoicePlayingId != null) {
                voicePlayingProgress = 0f
                while (activeVoicePlayingId != null && voicePlayingProgress < 1f) {
                    delay(100)
                    voicePlayingProgress += 0.05f
                }
                activeVoicePlayingId = null
                voicePlayingProgress = 0f
            }
        }

        BackHandler {
            viewModel.selectChatPartner(null)
        }

        Column(modifier = Modifier.fillMaxSize().background(if (isDarkMode) Color(0xFF121212) else Color(0xFFF3F4F6)).imePadding()) {
            // Header Bar
            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.selectChatPartner(null) 
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = if (isDarkMode) Color.White else GiggzGreen)
                    }
                },
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.showUserProfile(partner.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(photoUrl = partner.profilePhoto, name = partner.fullName, size = 38)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = partner.fullName,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color.Black,
                                fontSize = 14.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Online • Tap for profile",
                                    fontSize = 10.sp,
                                    color = if (isDarkMode) Color.LightGray else Color.Gray
                                )
                            }
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenuDropdown = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More Menu", tint = Color.Gray)
                        }

                        // Simplified dropdown menu linking view job, view profile, block user
                        DropdownMenu(
                            expanded = showMenuDropdown,
                            onDismissRequest = { showMenuDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View Job Post 📋", fontSize = 13.sp) },
                                onClick = {
                                    showMenuDropdown = false
                                    val relatedJob = allPieceWorks.find { job ->
                                        allApplications.any { app -> 
                                            app.jobId == job.id && 
                                            ((app.workerId == partner.id && app.employerId == currentUser.id) || 
                                             (app.workerId == currentUser.id && app.employerId == partner.id))
                                        }
                                    } ?: allPieceWorks.find { job ->
                                        job.employerId == partner.id
                                    } ?: allPieceWorks.find { job ->
                                        job.employerId == currentUser.id
                                    }
                                    
                                    if (relatedJob != null) {
                                        selectedChatJobDetail = relatedJob
                                    } else {
                                        context.showSafeToast("No related job post found.")
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("View Profile 👤", fontSize = 13.sp) },
                                onClick = {
                                    showMenuDropdown = false
                                    viewModel.showUserProfile(partner.id)
                                }
                            )

                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                            DropdownMenuItem(
                                text = { Text("Block User 🚫", fontSize = 13.sp, color = Color.Red, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenuDropdown = false
                                    viewModel.blockUser(partner.id) {
                                        context.showSafeToast("User blocked successfully.")
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White),
                modifier = Modifier.border(width = 0.5.dp, color = Color.LightGray.copy(alpha = 0.4f))
            )

            // Search Bar header if active
            if (showSearchDialog) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkMode) Color(0xFF1E1E1E) else Color.White)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchInChatQuery,
                        onValueChange = { searchInChatQuery = it },
                        placeholder = { Text("Search in conversation...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp),
                        trailingIcon = {
                            if (searchInChatQuery.isNotEmpty()) {
                                IconButton(onClick = { searchInChatQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )
                    TextButton(onClick = { 
                        showSearchDialog = false 
                        searchInChatQuery = ""
                    }) {
                        Text("Cancel", color = GiggzGreen, fontSize = 12.sp)
                    }
                }
            }

            // Message Bubble list
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp)) {
                // Vector Chat Wallpaper Backdrop
                val baseColor = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.fillMaxSize()) {
                    when (activeWallpaper) {
                        "Dot Grid" -> {
                            val dotColor = baseColor.copy(alpha = 0.08f)
                            val spacing = 20.dp.toPx()
                            val radius = 1.2.dp.toPx()
                            for (x in 0..size.width.toInt() step spacing.toInt()) {
                                    for (y in 0..size.height.toInt() step spacing.toInt()) {
                                        drawCircle(
                                            color = dotColor,
                                            radius = radius,
                                            center = Offset(x.toFloat(), y.toFloat())
                                        )
                                    }
                            }
                        }
                        "Lined Grid" -> {
                            val lineColor = baseColor.copy(alpha = 0.04f)
                            val spacing = 24.dp.toPx()
                            for (x in 0..size.width.toInt() step spacing.toInt()) {
                                drawLine(
                                    color = lineColor,
                                    start = Offset(x.toFloat(), 0f),
                                    end = Offset(x.toFloat(), size.height),
                                    strokeWidth = 0.8.dp.toPx()
                                )
                            }
                            for (y in 0..size.height.toInt() step spacing.toInt()) {
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, y.toFloat()),
                                    end = Offset(size.width, y.toFloat()),
                                    strokeWidth = 0.8.dp.toPx()
                                )
                            }
                        }
                        "Abstract Waves" -> {
                            val waveColor = baseColor.copy(alpha = 0.05f)
                            val path = Path().apply {
                                moveTo(0f, size.height * 0.85f)
                                cubicTo(
                                    size.width * 0.25f, size.height * 0.75f,
                                    size.width * 0.5f, size.height * 0.95f,
                                    size.width * 0.75f, size.height * 0.8f
                                )
                                cubicTo(
                                    size.width * 0.85f, size.height * 0.7f,
                                    size.width, size.height * 0.9f,
                                    size.width, size.height
                                )
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(path = path, color = waveColor)
                        }
                        else -> { /* Minimalist Clean - no drawing */ }
                    }
                }

                if (activeMessages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(GiggzGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("No messages here yet", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Send a message to propose a gig meeting, tool options, or negotiate rates instantly!", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 30.dp))
                    }
                } else {
                    val filteredMessages = remember(activeMessages, searchInChatQuery) {
                        if (searchInChatQuery.isBlank()) {
                            activeMessages
                        } else {
                            activeMessages.filter { it.messageText.lowercase().contains(searchInChatQuery.lowercase()) }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp)
                    ) {
                        items(filteredMessages) { msg ->
                            val isMe = msg.senderId == currentUser.id

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                // Message Context Action wrapper
                                Box(
                                    modifier = Modifier
                                        .clickable { 
                                            messageToOptionMenu = msg 
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .widthIn(max = 280.dp)
                                            .background(
                                                color = if (isMe) Color(0xFF10B981) else (if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFDCDFE4)),
                                                shape = RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                                )
                                            )
                                            .border(
                                                width = 0.5.dp, 
                                                color = if (isMe) Color(0xFF059669) else (if (isDarkMode) Color(0xFF4B5563) else Color(0xFFB0B6C0)),
                                                shape = RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                                )
                                            )
                                            .padding(8.dp)
                                    ) {
                                        // 1. Render Quoted reply header if exists
                                        if (msg.replyToId != null && msg.replyToText != null) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                                    .border(BorderStroke(0.5.dp, Color.LightGray), RoundedCornerShape(8.dp))
                                                    .padding(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(4.dp)
                                                        .fillMaxHeight()
                                                        .background(GiggzGreen, RoundedCornerShape(2.dp))
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text("Quoted message", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GiggzGreen)
                                                    Text(msg.replyToText!!, fontSize = 10.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }

                                        // 2. Render Media attachment components
                                        if (msg.mediaPath.isNotBlank()) {
                                            when (msg.mediaType) {
                                                "image" -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(130.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color.DarkGray)
                                                            .clickable { showFullScreenImage = msg.mediaPath }
                                                    ) {
                                                        // Fallback beautiful visual mockup
                                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                                            drawRect(color = Color(0xFF1E293B))
                                                            // Draw mini architectural mockup icon inside
                                                        }
                                                        Column(
                                                            modifier = Modifier
                                                                .align(Alignment.BottomCenter)
                                                                .fillMaxWidth()
                                                                .background(Color.Black.copy(alpha = 0.6f))
                                                                .padding(4.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Text("📷 Image Attachment", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                            Text("Tap to view full screen", fontSize = 8.sp, color = Color.LightGray)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }
                                                "pdf" -> {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.Black.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                                            .clickable { context.showSafeToast("Downloading ${msg.mediaPath}...") }
                                                            .padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Filled.Description, contentDescription = "PDF", tint = Color.Red, modifier = Modifier.size(28.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(msg.mediaPath, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                            Text("Document • Click to download", fontSize = 9.sp, color = Color.Gray)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }
                                                "location" -> {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                                            .border(BorderStroke(0.5.dp, Color(0xFFBFDBFE)), RoundedCornerShape(8.dp))
                                                            .clickable { context.showSafeToast("Opening coordinates in Google Maps...") }
                                                            .padding(8.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = Color.Red, modifier = Modifier.size(22.dp))
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("Gig Meeting Location", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E40AF))
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text("📍 ${msg.mediaPath}", fontSize = 11.sp, color = Color.DarkGray)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(54.dp)
                                                                .background(Color(0xFFDBEAFE), RoundedCornerShape(4.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("🗺️ TAP TO VIEW STREET MAP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }
                                                "voice" -> {
                                                    val isThisPlaying = activeVoicePlayingId == msg.id
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color.Black.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                                            .padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        IconButton(
                                                            onClick = { 
                                                                if (isThisPlaying) {
                                                                    activeVoicePlayingId = null
                                                                } else {
                                                                    activeVoicePlayingId = msg.id
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .background(GiggzGreen, CircleShape)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isThisPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, 
                                                                contentDescription = "Play Voice", 
                                                                tint = Color.White,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }

                                                        // Interactive voice progress bar
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("🎙️ Voice Note", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            LinearProgressIndicator(
                                                                progress = if (isThisPlaying) voicePlayingProgress else 0f,
                                                                color = GiggzGreen,
                                                                trackColor = Color.LightGray.copy(alpha = 0.5f),
                                                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                                            )
                                                        }
                                                        
                                                        Text(
                                                            text = if (isThisPlaying) "0:${String.format("%02d", (voicePlayingProgress * 14).toInt())}" else "0:14",
                                                            fontSize = 10.sp,
                                                            color = Color.Gray,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }
                                            }
                                        }

                                        // 3. Main Text Body
                                        Text(
                                            text = if (msg.isDeleted) "Deleted" else msg.messageText,
                                            fontSize = 13.sp,
                                            color = if (msg.isDeleted) {
                                                if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray
                                            } else {
                                                if (isMe) Color.White else (if (isDarkMode) Color.White else Color.Black)
                                            },
                                            style = if (msg.isDeleted) TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) else TextStyle()
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // 4. Timestamp & Checkmarks delivery status
                                        Row(
                                            modifier = Modifier.align(Alignment.End),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val sdf = remember { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()) }
                                            val timeString = remember(msg.timestamp) { sdf.format(java.util.Date(msg.timestamp)) }

                                            Text(
                                                text = timeString,
                                                fontSize = 8.sp,
                                                color = Color.Gray
                                            )
                                            if (isMe) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                val statusIcon = when (msg.deliveryStatus) {
                                                    "Sending" -> Icons.Filled.Timer
                                                    "Sent" -> Icons.Filled.Check
                                                    "Delivered" -> Icons.Filled.DoneAll
                                                    else -> Icons.Filled.DoneAll // "Read"
                                                }
                                                val statusTint = if (msg.deliveryStatus == "Read") GiggzGreen else Color.Gray

                                                Icon(
                                                    imageVector = statusIcon,
                                                    contentDescription = msg.deliveryStatus,
                                                    tint = statusTint,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 5. Reactions display sitting under the card
                                if (msg.reactions != null && msg.reactions!!.isNotBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .offset(y = (-6).dp, x = if (isMe) (-8).dp else 8.dp)
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .border(0.5.dp, Color.LightGray, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(msg.reactions!!, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Typing Status Bubble
                        if (isPartnerTyping) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .background(if (isDarkMode) Color(0xFF2C2C2C) else Color.White, RoundedCornerShape(16.dp))
                                            .border(0.5.dp, if (isDarkMode) Color(0xFF4B5563) else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("${partner.fullName} typing", fontSize = 11.sp, color = if (isDarkMode) Color.LightGray else Color.Gray, fontWeight = FontWeight.Bold)
                                            
                                            // Infinite bouncing dot animation mockup
                                            val infiniteTransition = rememberInfiniteTransition()
                                            val dotAlpha by infiniteTransition.animateFloat(
                                                initialValue = 0.2f,
                                                targetValue = 1.0f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(600, easing = LinearEasing),
                                                    repeatMode = RepeatMode.Reverse
                                                )
                                            )
                                            Text("...", fontSize = 11.sp, color = GiggzGreen.copy(alpha = dotAlpha), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reply Preview Bar if replying
            if (replyToId != null && replyToText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkMode) Color(0xFF1E1E1E) else Color.White)
                        .border(BorderStroke(0.5.dp, if (isDarkMode) Color(0xFF4B5563) else Color.LightGray))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Reply, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Replying to", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GiggzGreen)
                            Text(replyToText!!, fontSize = 11.sp, color = if (isDarkMode) Color.LightGray else Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    IconButton(onClick = { 
                        replyToId = null
                        replyToText = null
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = if (isDarkMode) Color.White else Color.Black, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Keyboard area input row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) Color(0xFF1E1E1E) else Color.White)
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Left attachment triggers
                    IconButton(onClick = { showAttachmentDialog = true }) {
                        Icon(Icons.Filled.AttachFile, contentDescription = "Attachments", tint = if (isDarkMode) Color.LightGray else Color.Gray)
                    }

                    IconButton(onClick = { showEmojiPanel = !showEmojiPanel }) {
                        Icon(Icons.Filled.Face, contentDescription = "Emojis", tint = if (showEmojiPanel) GiggzGreen else (if (isDarkMode) Color.LightGray else Color.Gray))
                    }

                    // Modern rounded input box (30% darker)
                    OutlinedTextField(
                        value = currentText,
                        onValueChange = { 
                            currentText = it 
                            if (showEmojiPanel) showEmojiPanel = false
                        },
                        placeholder = { Text("Write your message...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = TextStyle(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                            unfocusedTextColor = if (isDarkMode) Color.White else Color.Black,
                            focusedContainerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFE5E7EB),
                            unfocusedContainerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFE5E7EB),
                            focusedBorderColor = GiggzGreen,
                            unfocusedBorderColor = if (isDarkMode) Color(0xFF4B5563) else Color(0xFF9CA3AF)
                        ),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (currentText.isNotBlank()) {
                                    viewModel.sendMessage(currentText, replyToId = replyToId, replyToText = replyToText)
                                    currentText = ""
                                    replyToId = null
                                    replyToText = null
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            }
                        )
                    )

                    // Right action: Unified Send button (voice recorder/notes removed)
                    IconButton(
                        onClick = {
                            if (currentText.isNotBlank()) {
                                viewModel.sendMessage(currentText, replyToId = replyToId, replyToText = replyToText)
                                currentText = ""
                                replyToId = null
                                replyToText = null
                            }
                        },
                        enabled = currentText.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (currentText.isNotBlank()) GiggzGreen else Color(0xFFD1D5DB),
                            disabledContainerColor = Color(0xFFE5E7EB)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_uncommon_paper_jet), 
                            contentDescription = "Send", 
                            tint = if (currentText.isNotBlank()) Color.White else Color(0xFF9CA3AF), 
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Emoji panel section
                if (showEmojiPanel) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val emojiList = listOf("👍", "❤️", "😂", "😮", "😢", "🙏", "🙌", "🔥", "🛠️", "💼")
                        emojiList.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .clickable { currentText += emoji }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODERN DIALOGS & OVERLAYS
        // ==========================================

        // Attachment dialog selection menu
        if (showAttachmentDialog) {
            Dialog(onDismissRequest = { showAttachmentDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Share with ${partner.fullName}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GiggzGreen)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Image Attachment
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                showAttachmentDialog = false
                                viewModel.sendMessage("📷 Shared a site photo", "duplex_renovation_site.jpg", "image")
                                context.showSafeToast("Photo uploaded and shared live!")
                            }) {
                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFECFDF5), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Image, contentDescription = "Image", tint = GiggzGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Photo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Document Attachment
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                showAttachmentDialog = false
                                viewModel.sendMessage("📄 Shared specifications document", "Giggz_Contract_SOP_04.pdf", "pdf")
                                context.showSafeToast("Document shared live!")
                            }) {
                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFFEF3C7), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Description, contentDescription = "Document", tint = GiggzGold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Document", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Location Attachment
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                showAttachmentDialog = false
                                viewModel.sendMessage("📍 Shared meeting location", "Nairobi Central Square, block 4B", "location")
                                context.showSafeToast("Meeting map coordinate shared!")
                            }) {
                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFEFF6FF), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = Color.Blue)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Location", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        
                        TextButton(onClick = { showAttachmentDialog = false }) {
                            Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Voice recorder popup simulator
        if (showVoiceRecorder) {
            Dialog(onDismissRequest = { 
                showVoiceRecorder = false
                isRecordingVoice = false
            }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎙️ Giggz Audio Recorder", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GiggzGreen)
                        
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .background(if (isRecordingVoice) Color(0xFFFEE2E2) else Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic, 
                                contentDescription = "Mic", 
                                tint = if (isRecordingVoice) Color.Red else GiggzGreen,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        if (isRecordingVoice) {
                            Text("Recording Live... 0:${String.format("%02d", voiceTimerSeconds)}", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            // Animated recording waves
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                repeat(8) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(24.dp)
                                            .background(Color.Red, RoundedCornerShape(1.dp))
                                    )
                                }
                            }
                        } else {
                            Text("Tap Start to record instructions, contract bids, or message terms easily.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!isRecordingVoice) {
                                Button(
                                    onClick = { isRecordingVoice = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen)
                                ) {
                                    Text("Start Recording")
                                }
                            } else {
                                Button(
                                    onClick = { 
                                        isRecordingVoice = false
                                        showVoiceRecorder = false
                                        viewModel.sendMessage("🎙️ Voice Note (0:14)", "giggz_voice_note.aac", "voice")
                                        context.showSafeToast("Audio recording sent!")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Stop & Send")
                                }
                            }
                            
                            OutlinedButton(onClick = { 
                                showVoiceRecorder = false
                                isRecordingVoice = false
                            }) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }

        // WhatsApp Style Full-Screen Image modal viewer
        if (showFullScreenImage != null) {
            Dialog(
                onDismissRequest = { showFullScreenImage = null },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Project Site Attachment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { showFullScreenImage = null }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        // High fidelity visual container mockup
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(54.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Mock Giggz Image File", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(showFullScreenImage!!, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }

                        Button(
                            onClick = { 
                                showFullScreenImage = null
                                context.showSafeToast("Attachment saved to Giggz downloads!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen)
                        ) {
                            Text("Download to Gallery", color = Color.White)
                        }
                    }
                }
            }
        }

        // High fidelity future-ready Dialing audio/video call mockup screen
        if (showCallingDialog != null) {
            Dialog(
                onDismissRequest = { showCallingDialog = null },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 40.dp)
                        ) {
                            Text(showCallingDialog!!.uppercase(), color = GiggzGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            UserAvatar(photoUrl = partner.profilePhoto, name = partner.fullName, size = 96)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(partner.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ringing...", color = Color.LightGray, fontSize = 12.sp)
                        }

                        // Call action control row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mute mic button
                            IconButton(
                                onClick = { context.showSafeToast("Mic muted") },
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Filled.VolumeMute, contentDescription = "Mute", tint = Color.White)
                            }

                            // End call button
                            IconButton(
                                onClick = { showCallingDialog = null },
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.Red, CircleShape)
                            ) {
                                Icon(Icons.Filled.CallEnd, contentDescription = "End Call", tint = Color.White)
                            }

                            // Speaker button
                            IconButton(
                                onClick = { context.showSafeToast("Speaker mode activated") },
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = "Speaker", tint = Color.White)
                            }
                        }

                        Text("Giggz Secure End-to-End Encrypted Call", color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
        }

        // Long-press option sheet for individual messages
        if (messageToOptionMenu != null) {
            val targetMsg = messageToOptionMenu!!
            Dialog(onDismissRequest = { messageToOptionMenu = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Message Actions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GiggzGreen)
                        
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))

                        // Reaction floating list right inside options sheet
                        Text("Add Reaction:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val reactionsList = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")
                            reactionsList.forEach { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.addMessageReaction(targetMsg.id, emoji)
                                            messageToOptionMenu = null
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }

                        Divider(color = Color.LightGray.copy(alpha = 0.5f))

                        // Reply to message option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    replyToId = targetMsg.id
                                    replyToText = targetMsg.messageText
                                    messageToOptionMenu = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Reply, contentDescription = "Reply", tint = GiggzGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Reply to Message", fontSize = 13.sp)
                        }

                        // Copy to clipboard option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    messageToOptionMenu = null
                                    context.showSafeToast("Copied to clipboard!")
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Copy Message Text", fontSize = 13.sp)
                        }

                        // Delete option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.deleteMessage(targetMsg.id)
                                    messageToOptionMenu = null
                                    context.showSafeToast("Message deleted.")
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (targetMsg.senderId == currentUser.id) "Delete for Everyone" else "Delete Message",
                                fontSize = 13.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = { messageToOptionMenu = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Close", color = Color.Gray)
                        }
                    }
                }
            }
        }

        if (selectedChatJobDetail != null) {
            val detailJob = selectedChatJobDetail!!
            val savedJobIds by viewModel.savedJobIds.collectAsStateWithLifecycle()
            val isBookmarked = savedJobIds.contains(detailJob.id)
            val hasApplied = allApplications.any { it.jobId == detailJob.id && it.workerId == currentUser.id }
            
            JobDetailDialog(
                job = detailJob,
                isBookmarked = isBookmarked,
                hasApplied = hasApplied,
                onDismissRequest = { selectedChatJobDetail = null },
                onBookmark = { viewModel.saveJob(detailJob.id) },
                onApplyClick = {
                    selectedChatJobDetail = null
                    viewModel.applyForJob(detailJob, "Applying for instant ${detailJob.timeRequired} contract.", "", "") {
                        context.showSafeToast("Instant Application Submitted!")
                    }
                },
                onChatClick = {
                    selectedChatJobDetail = null
                },
                onProfileClick = {
                    selectedChatJobDetail = null
                    viewModel.showUserProfile(detailJob.employerId)
                }
            )
        }
    } else {
        // Partners thread list
        val isTabbed = currentUser.role == "Employer" || currentUser.role == "Worker"
        var selectedSideTab by remember { mutableStateOf("Workers") } // Default to Workers (Contracts) for everyone

        val filteredPartners = remember(chatPartners, selectedSideTab, isTabbed) {
            if (isTabbed) {
                chatPartners.filter { partner ->
                    if (selectedSideTab == "Workers") {
                        if (currentUser.role == "Employer") {
                            partner.role == "Worker"
                        } else {
                            partner.role == "Employer" || partner.role == "Admin"
                        }
                    } else {
                        if (currentUser.role == "Employer") {
                            partner.role == "Employer" || partner.role == "Admin"
                        } else {
                            partner.role == "Worker"
                        }
                    }
                }
            } else {
                chatPartners
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(10.dp))

            if (isTabbed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(if (isDarkMode) Color(0xFF2D2D2D) else Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    listOf("Workers", "Deals").forEach { tab ->
                        val isSelected = selectedSideTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) GiggzGreen else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedSideTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (tab == "Workers") Icons.Filled.Assignment else Icons.Filled.Storefront,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else (if (isDarkMode) Color.LightGray else Color.Gray),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (tab == "Workers") "Contracts" else "Deals",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else (if (isDarkMode) Color.LightGray else Color.Gray)
                                )
                            }
                        }
                    }
                }
            }

            if (filteredPartners.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isTabbed) {
                            if (selectedSideTab == "Workers") "No active contracts in your message threads yet."
                            else "No active deals in your message threads yet."
                        } else {
                            "No conversation history. Go to 'Dashboard' or 'Marketplace' to message sellers & employers."
                        },
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    items(filteredPartners) { partner ->
                        val lastMessage = remember(allMessages, partner.id) {
                            allMessages.firstOrNull { 
                                (it.senderId == currentUser.id && it.receiverId == partner.id) ||
                                (it.senderId == partner.id && it.receiverId == currentUser.id)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.selectChatPartner(partner) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.clickable { viewModel.showUserProfile(partner.id) }
                                ) {
                                    UserAvatar(photoUrl = partner.profilePhoto, name = partner.fullName, size = 44)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(partner.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Role: ${partner.role} | Availability: ${partner.availabilityStatus}", fontSize = 11.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (lastMessage != null) {
                                        Text(
                                            text = lastMessage.messageText,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = if (!lastMessage.isRead && lastMessage.receiverId == currentUser.id) FontWeight.Bold else FontWeight.Normal,
                                            color = if (!lastMessage.isRead && lastMessage.receiverId == currentUser.id) MaterialTheme.colorScheme.primary else Color.DarkGray
                                        )
                                    } else {
                                        Text("No messages yet.", fontSize = 11.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                }
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_uncommon_paper_jet), 
                                    contentDescription = null, 
                                    tint = GiggzGreen, 
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// =============================================================================
// PROFILE SECTION
// =============================================================================

@Composable
fun ProfileSection(
    viewModel: GiggzViewModel
) {
    val context = LocalContext.current
    val user = viewModel.currentUser.collectAsStateWithLifecycle().value ?: return

    var isEditing by remember { mutableStateOf(false) }
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var showHistoryPage by remember { mutableStateOf(false) }

    // Forms
    var editName by remember { mutableStateOf(user.fullName) }
    var editPhone by remember { mutableStateOf(user.phone) }
    var editLocation by remember { mutableStateOf(user.location) }
    var editSkills by remember { mutableStateOf(user.skills) }
    var editExperience by remember { mutableStateOf(user.experience.toString()) }
    var editBio by remember { mutableStateOf(user.bio) }
    var editAvailability by remember { mutableStateOf(user.availabilityStatus) }
    var editProfilePhoto by remember { mutableStateOf(user.profilePhoto) }

    if (showHistoryPage) {
        SimpleHistoryPage(viewModel = viewModel, onBack = { showHistoryPage = false })
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Profile Management", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Simple History Clock icon immediately before the dark mode toggle
                    IconButton(
                        onClick = { showHistoryPage = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "View Simple History",
                            tint = GiggzGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    val currentThemeName by viewModel.currentTheme.collectAsStateWithLifecycle()
                    val isLight = currentThemeName == "Light Mode"
                    IconButton(
                        onClick = {
                            viewModel.setTheme(if (isLight) "Dark Mode" else "Light Mode")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isLight) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                            contentDescription = "Toggle Theme",
                            tint = GiggzGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

        Spacer(modifier = Modifier.height(14.dp))

        // Avatar with Rating badge
        UserAvatar(photoUrl = user.profilePhoto, name = user.fullName, size = 80)
        Spacer(modifier = Modifier.height(8.dp))

        Text(user.fullName, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(user.role.uppercase(), fontWeight = FontWeight.Bold, fontSize = 10.sp, color = GiggzGreen)

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isEditing = !isEditing },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GiggzGreen.copy(alpha = 0.12f),
                    contentColor = GiggzGreen
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (isEditing) "Cancel Edit" else "Edit Profile",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { viewModel.showSettingsDialog.value = true }
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "System Settings",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        GiggzReputationCard(
            rating = user.rating,
            reviewsCount = user.reviewsCount,
            completedJobs = user.completedJobs
        )

        Divider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 16.dp))

        if (isEditing) {
            // Edit forms
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ImageAttachmentPicker(
                    imageUrl = editProfilePhoto,
                    onImageSelected = { editProfilePhoto = it },
                    label = "Change Profile Photo"
                )
                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Display Name") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone Number") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editLocation, onValueChange = { editLocation = it }, label = { Text("Location Address") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())

                if (user.role == "Worker") {
                    OutlinedTextField(value = editSkills, onValueChange = { editSkills = it }, label = { Text("Skills (comma-separated, enter 'all' to see all jobs)") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editExperience, onValueChange = { editExperience = it }, label = { Text("Years of Experience") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("Professional Biography") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(), maxLines = 4)
                }

                // Availability Selection
                Text("Availability Status:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Available", "Busy", "Offline").forEach { status ->
                        val isSelected = editAvailability == status
                        Button(
                            onClick = { editAvailability = status },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) GiggzGreen else Color.LightGray
                            ),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(status, fontSize = 11.sp, color = if (isSelected) Color.White else Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.updateProfile(
                            fullName = editName,
                            phone = editPhone,
                            location = editLocation,
                            skills = editSkills,
                            experience = editExperience.toIntOrNull() ?: 0,
                            bio = editBio,
                            availability = editAvailability,
                            profilePhoto = editProfilePhoto
                        )
                        isEditing = false
                        context.showSafeToast("Profile details updated in Room Database!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            }
        } else {
            // View details
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileDetailRow(label = "Email Address", value = user.email)
                ProfileDetailRow(label = "Phone Number", value = user.phone.ifBlank { "Not specified" })
                ProfileDetailRow(label = "Location", value = user.location.ifBlank { "Not specified" })
                ProfileDetailRow(label = "Availability", value = user.availabilityStatus, valueColor = if (user.availabilityStatus == "Available") GiggzGreen else Color.Red)

                if (user.role == "Worker") {
                    ProfileDetailRow(label = "Experience", value = "${user.experience} Years")
                    if (user.bio.isNotBlank()) {
                        ProfileDetailRow(label = "Biography", value = user.bio)
                    }

                    val skillsList = user.skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    if (skillsList.isNotEmpty()) {
                        Text("Skills & Categories tags", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            skillsList.forEach { skill ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(GiggzGreen.copy(alpha = 0.08f))
                                        .border(0.5.dp, GiggzGreen.copy(alpha = 0.3f), RoundedCornerShape(30.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(skill, color = GiggzGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }



                    if (user.cvPath.isNotBlank()) {
                        Text("Attached CV / Resume", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("📄 ${user.cvPath}", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                TextButton(onClick = { context.showSafeToast("Mock CV PDF downloaded to system storage.") }) {
                                    Text("Download", fontSize = 11.sp, color = GiggzGreen)
                                }
                              }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Improvement Suggestions Card
        var improvementText by remember { mutableStateOf("") }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF242424) else Color(0xFFF9FBF9)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (isDarkMode) Color(0xFF333333) else GiggzGreen.copy(alpha = 0.2f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Feedback,
                        contentDescription = "Suggestions Icon",
                        tint = GiggzGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Improvement Suggestions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                    )
                }

                Text(
                    text = "Help us make Giggz better! Write your feature requests, bugs, or improvements below. Your suggestion will be sent directly to the Admin Hub.",
                    fontSize = 11.sp,
                    color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = improvementText,
                    onValueChange = { improvementText = it },
                    placeholder = { Text("Write your suggestions here...", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GiggzGreen,
                        unfocusedBorderColor = if (isDarkMode) Color(0xFF444444) else Color.LightGray
                    ),
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        if (improvementText.isNotBlank()) {
                            viewModel.sendImprovementSuggestion(
                                senderName = user.fullName.ifBlank { "Anonymous" },
                                senderRole = user.role,
                                text = improvementText
                            )
                            improvementText = ""
                            context.showSafeToast("Suggestion sent to Admin's Improvement Hub! Thank you! ❤️")
                        } else {
                            context.showSafeToast("Please type something before sending.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GiggzGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_uncommon_paper_jet),
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Submit Suggestion",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Logout Session", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SimpleHistoryPage(
    viewModel: GiggzViewModel,
    onBack: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val deletedHistory by viewModel.deletedPostsHistory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Simple History", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (isDarkMode) Color.White else Color.Black)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = GiggzGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
                )
            )
        },
        containerColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF9FAFB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1C1C1E) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isDarkMode) Color(0xFF2C2C2E) else Color.LightGray.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Your Platform History Log",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = "This register archives and logs details of any job posts, contracts, or marketplace listings that were deleted, completed, or removed.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (deletedHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDarkMode) Color(0xFF2C2C2E) else Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.History, contentDescription = null, tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your simple history log is empty.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "When your posts are deleted or finished, their records will appear here.",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            deletedHistory.forEach { post ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = if (isDarkMode) Color(0xFF2C2C2E) else Color(0xFFF9FAFB),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            BorderStroke(
                                                0.5.dp,
                                                if (isDarkMode) Color(0xFF3C3C3E) else Color.LightGray.copy(alpha = 0.5f)
                                            ),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = post.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White else Color.Black,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(GiggzGreen.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = post.type,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GiggzGreen
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = post.description,
                                        fontSize = 11.sp,
                                        color = if (isDarkMode) Color.LightGray else Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Schedule,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Archived: " + java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(post.dateDeleted)),
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = "ID: #${post.originalId}",
                                            fontSize = 8.sp,
                                            color = Color.Gray
                                        )
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
fun ProfileDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun NotificationsFullScreen(
    viewModel: GiggzViewModel,
    onBack: () -> Unit
) {
    val notificationsList by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount = notificationsList.filter { !it.isRead }.size
    val context = androidx.compose.ui.platform.LocalContext.current
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var isDeleteMode by remember { mutableStateOf(false) }

    val filteredNotifications = if (showOnlyFavorites) {
        notificationsList.filter { it.isFavorite }
    } else {
        notificationsList
    }

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val appBarBg = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
    val appBarContentColor = if (isDarkMode) Color.White else Color.Black
    val appBarCloseColor = if (isDarkMode) Color.LightGray else Color.Gray

    Scaffold(
        topBar = {
            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications Hub", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = appBarContentColor)
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text("$unreadCount New", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back", tint = appBarContentColor)
                    }
                },
                actions = {
                    if (notificationsList.isNotEmpty()) {
                        if (!isDeleteMode) {
                            IconButton(onClick = {
                                isDeleteMode = true
                                selectedIds = emptySet()
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Mode", tint = appBarContentColor)
                            }
                        } else {
                            // Confirm delete of selected notifications
                            IconButton(onClick = {
                                if (selectedIds.isNotEmpty()) {
                                    viewModel.deleteNotifications(selectedIds.toList())
                                    context.showSafeToast("Deleted marked notifications")
                                    selectedIds = emptySet()
                                } else {
                                    context.showSafeToast("No notifications marked")
                                }
                                isDeleteMode = false
                            }) {
                                Icon(Icons.Filled.Check, contentDescription = "Confirm Delete", tint = appBarContentColor)
                            }
                            // Cancel delete mode
                            IconButton(onClick = {
                                isDeleteMode = false
                                selectedIds = emptySet()
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Cancel Delete Mode", tint = appBarCloseColor)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appBarBg)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {

            // Favorites Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                listOf("All Alerts", "Saved Favourites").forEach { tab ->
                    val isSelected = (tab == "All Alerts" && !showOnlyFavorites) || (tab == "Saved Favourites" && showOnlyFavorites)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) com.example.ui.theme.GiggzGreen else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { showOnlyFavorites = (tab == "Saved Favourites") }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (tab == "All Alerts") Icons.Filled.NotificationsActive else Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }

            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (showOnlyFavorites) Icons.Outlined.StarBorder else Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (showOnlyFavorites) "No saved notifications yet" else "No notifications yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = if (showOnlyFavorites) "Star any notification to keep track of it here!" else "Any system updates or alerts will appear here",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotifications) { notif ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.markNotificationAsRead(notif.id)
                                    if (notif.category == "message") {
                                        viewModel.setTab("messages")
                                        onBack()
                                    } else if (notif.category == "job") {
                                        viewModel.setTab("my_contracts")
                                        onBack()
                                    } else if (notif.category == "application") {
                                        viewModel.setTab("dashboard")
                                        onBack()
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else com.example.ui.theme.GiggzGreen.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (notif.isRead) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f) else com.example.ui.theme.GiggzGreen.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isDeleteMode) {
                                    val isSelected = selectedIds.contains(notif.id)
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedIds = if (checked == true) {
                                                selectedIds + notif.id
                                            } else {
                                                selectedIds - notif.id
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = com.example.ui.theme.GiggzGreen)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            color = if (notif.isRead) Color(0xFFF3F4F6) else com.example.ui.theme.GiggzGreen.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (notif.category) {
                                            "job" -> Icons.Filled.Work
                                            "message" -> Icons.Filled.Chat
                                            "application" -> Icons.Filled.Assignment
                                            else -> Icons.Filled.NotificationsActive
                                        },
                                        contentDescription = null,
                                        tint = if (notif.isRead) Color.Gray else com.example.ui.theme.GiggzGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (notif.isRead) Color.DarkGray else Color.Black
                                        )
                                        if (!notif.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(com.example.ui.theme.GiggzGreen, RoundedCornerShape(4.dp))
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        lineHeight = 16.sp
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.toggleNotificationFavorite(notif.id, !notif.isFavorite)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (notif.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (notif.isFavorite) com.example.ui.theme.GiggzGold else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
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
fun EventsSection(viewModel: GiggzViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Celebration,
                contentDescription = "Coming Soon",
                tint = com.example.ui.theme.GiggzGold,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Giggz Events & Celebration",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = com.example.ui.theme.GiggzGreen,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Coming Soon!",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We are currently cooking up some exciting new features to help you discover local gatherings, professional meetups, and community celebrations. Stay tuned!",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
    return

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val events by viewModel.allEvents.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var manualLocation by remember { mutableStateOf("San Francisco, CA") }
    var showLocationDialog by remember { mutableStateOf(false) }
    
    // Dialog states
    var showCreateEventDialog by remember { mutableStateOf(false) }
    var selectedEventForDetails by remember { mutableStateOf<com.example.data.EventEntity?>(null) }

    val isDark = isSystemInDarkTheme()
    val eventCardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val eventCardBorder = if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.4f)
    
    // Category list with emoji icons
    val categories = listOf(
        "All" to "🌐",
        "Music" to "🎶",
        "Education" to "🎓",
        "Community" to "🤝",
        "Sports" to "⚽",
        "Parties" to "🎉",
        "Church" to "⛪",
        "Business" to "💼"
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Title and Location Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Events Near You",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clickable { showLocationDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = manualLocation,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "• Change",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Promote button removed because we have a beautiful FAB at the bottom right!
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search local music, sports, meets...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal scrolling category tabs
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(categories.size) { index ->
                        val (catName, emoji) = categories[index]
                        val isSelected = selectedCategory == catName
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { selectedCategory = catName }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = catName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateEventDialog = true },
                containerColor = GiggzGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Post Event")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Post Event", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val filteredEvents = remember(events, searchQuery, selectedCategory) {
                events.filter { event ->
                    val matchesSearch = event.title.contains(searchQuery, ignoreCase = true) || 
                                        event.description.contains(searchQuery, ignoreCase = true) ||
                                        event.location.contains(searchQuery, ignoreCase = true)
                    
                    val matchesCategory = selectedCategory == "All" || event.category.equals(selectedCategory, ignoreCase = true)
                    matchesSearch && matchesCategory
                }
            }

            if (filteredEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No active events found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try adjusting your search query or post your own new event!",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                val recommendedEvents = remember(events) {
                    events.filter { it.isFeatured || it.isTrending || it.isVerified }.take(4)
                }
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Smart Recommendations Row
                    if (recommendedEvents.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == "All") {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Smart Recommendations",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Based on interests",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(recommendedEvents.size) { idx ->
                                        val recEvent = recommendedEvents[idx]
                                        Card(
                                            modifier = Modifier
                                                .width(260.dp)
                                                .clickable { selectedEventForDetails = recEvent },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = eventCardBg),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (recEvent.isFeatured) GiggzGold.copy(alpha = 0.3f) else eventCardBorder
                                            )
                                        ) {
                                            Column {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(110.dp)
                                                ) {
                                                    coil.compose.AsyncImage(
                                                        model = recEvent.imageUrl.ifBlank { "https://images.unsplash.com/photo-1511578314322-379afb476865?w=800&auto=format&fit=crop" },
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                    
                                                    // Category Badge
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .padding(8.dp)
                                                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = recEvent.category.uppercase(),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    }
                                                }
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(
                                                        text = recEvent.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        maxLines = 1,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "📅 ${recEvent.date}",
                                                        fontSize = 10.sp,
                                                        color = Color.Gray,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            }
                        }
                    }

                    // Feed Section Title
                    item {
                        Text(
                            text = "Explore Events",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(filteredEvents) { event ->
                        val savedList = remember(event.savedByUserIds) {
                            event.savedByUserIds.split(",")
                                .filter { it.isNotBlank() }
                                .map { it.trim() }
                        }
                        val savesCount = savedList.size
                        val currentUserIdStr = currentUser?.id?.toString() ?: ""
                        val isSaved = savedList.contains(currentUserIdStr)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedEventForDetails = event },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = eventCardBg),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (event.isFeatured) GiggzGold.copy(alpha = 0.3f) else eventCardBorder
                            )
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(165.dp)
                                ) {
                                    coil.compose.AsyncImage(
                                        model = event.imageUrl.ifBlank { "https://images.unsplash.com/photo-1511578314322-379afb476865?w=800&auto=format&fit=crop" },
                                        contentDescription = "Event banner",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    
                                    // Custom visual gradient overlay on dark mode
                                    if (MaterialTheme.colorScheme.surface != Color.White) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                                    )
                                                )
                                        )
                                    }

                                    // Featured/Trending Badges Top Left
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (event.isFeatured) {
                                            Box(
                                                modifier = Modifier
                                                    .background(GiggzGold, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("🟡 FEATURED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            }
                                        }
                                        if (event.isTrending) {
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("🌿 TRENDING", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            }
                                        }
                                    }

                                    // Save Button Top End
                                    IconButton(
                                        onClick = { viewModel.toggleSaveEvent(event.id) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(30.dp))
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Save",
                                            tint = if (isSaved) GiggzGold else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    // Category tag pill bottom-left
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = event.category.uppercase(),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = event.title,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (event.isVerified) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Verified Organizer",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Verified", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = event.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        lineHeight = 18.sp,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Location & Date
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = event.date, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = event.location, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Promoter info footer
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(6.dp)
                                    ) {
                                        UserAvatar(photoUrl = event.promoterPhoto, name = event.promoterName, size = 26)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(text = "Organizer", fontSize = 8.sp, color = Color.Gray)
                                            Text(text = event.promoterName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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

    // Change Location Dialog
    if (showLocationDialog) {
        Dialog(onDismissRequest = { showLocationDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Location", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    OutlinedTextField(
                        value = manualLocation,
                        onValueChange = { manualLocation = it },
                        label = { Text("City or Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Button(
                        onClick = {
                            manualLocation = "Detecting..."
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(1000)
                                manualLocation = "Chicago, IL 📍"
                                context.showSafeToast("Detected location successfully! (Mocked via GPS)")
                                showLocationDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📍 Auto-Detect My Location", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showLocationDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                                    onClick = { showLocationDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen)
                                ) {
                                    Text("Confirm", color = Color.White)
                                }
                    }
                }
            }
        }
    }

    // MULTI-STEP CREATE EVENT FLOW DIALOG - MINIMIZED TO SINGLE SCREEN QUICK FORM
    if (showCreateEventDialog) {
        var titleInput by remember { mutableStateOf("") }
        var descriptionInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("Community") }
        var dateInput by remember { mutableStateOf("July 15") }
        var timeInput by remember { mutableStateOf("7:00 PM") }
        var locationInput by remember { mutableStateOf(manualLocation) }
        var shortVideoUrl by remember { mutableStateOf("") }
        
        // Multi-photo and Social links states
        var photoUrls by remember { mutableStateOf(listOf("https://images.unsplash.com/photo-1511578314322-379afb476865?w=800")) }
        var instagramLink by remember { mutableStateOf("") }
        var facebookLink by remember { mutableStateOf("") }
        var twitterLink by remember { mutableStateOf("") }
        var tiktokLink by remember { mutableStateOf("") }
        
        var showAddPhotoDialog by remember { mutableStateOf(false) }
        var photoInputTemp by remember { mutableStateOf("") }
        
        var showSuccessAnimation by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { if (!showSuccessAnimation) showCreateEventDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                if (showSuccessAnimation) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Event Published! 🚀",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Your community event is now live and glowing on the Giggz marketplace.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Quick Post Event",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Fill out details below to post your event instantly.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Event Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = descriptionInput,
                            onValueChange = { descriptionInput = it },
                            label = { Text("Event Description") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = dateInput,
                                onValueChange = { dateInput = it },
                                label = { Text("Date") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = timeInput,
                                onValueChange = { timeInput = it },
                                label = { Text("Time") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = { locationInput = it },
                            label = { Text("Location Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Interactive photo urls max 4 section
                        Text("Event Photo Gallery (Max 4)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            photoUrls.forEachIndexed { idx, url ->
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                ) {
                                    coil.compose.AsyncImage(
                                        model = url,
                                        contentDescription = "Event Thumbnail",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(18.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(bottomStart = 8.dp))
                                            .clickable {
                                                photoUrls = photoUrls.toMutableList().apply { removeAt(idx) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            if (photoUrls.size < 4) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { showAddPhotoDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.AddAPhoto, contentDescription = "Add Photo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                        Text("Add", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Social Media Links
                        Text("Social Media Platform Links (Optional)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        OutlinedTextField(
                            value = instagramLink,
                            onValueChange = { instagramLink = it },
                            label = { Text("Instagram Link") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Text("📸", fontSize = 16.sp) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = facebookLink,
                            onValueChange = { facebookLink = it },
                            label = { Text("Facebook Page Link") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Text("👥", fontSize = 16.sp) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = twitterLink,
                            onValueChange = { twitterLink = it },
                            label = { Text("Twitter / X Link") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Text("🐦", fontSize = 16.sp) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tiktokLink,
                            onValueChange = { tiktokLink = it },
                            label = { Text("TikTok Link") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Text("🎵", fontSize = 16.sp) },
                            singleLine = true
                        )

                        Text("Select Category:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val dialogCats = listOf("Music", "Education", "Community", "Sports", "Parties")
                            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(dialogCats.size) { idx ->
                                    val cat = dialogCats[idx]
                                    val isSelected = categoryInput == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { categoryInput = cat }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(cat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showCreateEventDialog = false }) {
                                Text("Cancel", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (titleInput.isBlank() || descriptionInput.isBlank() || dateInput.isBlank() || locationInput.isBlank()) {
                                        context.showSafeToast("Please fill in all details!")
                                    } else {
                                        val finalCoverImage = photoUrls.firstOrNull { it.isNotBlank() } ?: "https://images.unsplash.com/photo-1511578314322-379afb476865?w=800"
                                        val finalGallery = photoUrls.filter { it.isNotBlank() }.joinToString(",")
                                        
                                        val socialLinksJoined = listOf(instagramLink, facebookLink, twitterLink, tiktokLink)
                                            .filter { it.isNotBlank() }
                                            .joinToString(",")
                                            
                                        viewModel.promoteEvent(
                                            title = titleInput,
                                            description = descriptionInput,
                                            date = "$dateInput at $timeInput",
                                            location = locationInput,
                                            imageUrl = finalCoverImage,
                                            category = categoryInput,
                                            isFeatured = true,
                                            isTrending = false,
                                            imageGallery = finalGallery,
                                            videoUrl = shortVideoUrl,
                                            socialLinks = socialLinksJoined
                                        ) {
                                            coroutineScope.launch {
                                                showSuccessAnimation = true
                                                kotlinx.coroutines.delay(1800)
                                                showCreateEventDialog = false
                                                showSuccessAnimation = false
                                                
                                                titleInput = ""
                                                descriptionInput = ""
                                                categoryInput = "Community"
                                                dateInput = "July 15"
                                                timeInput = "7:00 PM"
                                                locationInput = ""
                                                photoUrls = listOf("https://images.unsplash.com/photo-1511578314322-379afb476865?w=800")
                                                instagramLink = ""
                                                facebookLink = ""
                                                twitterLink = ""
                                                tiktokLink = ""
                                                shortVideoUrl = ""
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen)
                            ) {
                                Text("Publish Event 🚀", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        if (showAddPhotoDialog) {
            AlertDialog(
                onDismissRequest = { showAddPhotoDialog = false },
                title = { Text("Add Photo Link", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Paste or type a photo link/URL below:", fontSize = 12.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = photoInputTemp,
                            onValueChange = { photoInputTemp = it },
                            placeholder = { Text("https://example.com/photo.jpg", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (photoInputTemp.isNotBlank()) {
                                photoUrls = photoUrls + photoInputTemp.trim()
                                photoInputTemp = ""
                            }
                            showAddPhotoDialog = false
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPhotoDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    // PREMIUM EVENT DETAILS DIALOG
    selectedEventForDetails?.let { event ->
        val savedList = remember(event.savedByUserIds) {
            event.savedByUserIds.split(",")
                .filter { it.isNotBlank() }
                .map { it.trim() }
        }
        val isSaved = savedList.contains(currentUser?.id?.toString() ?: "")
        
        val goingList = remember(event.goingUserIds) {
            event.goingUserIds.split(",")
                .filter { it.isNotBlank() }
                .map { it.trim() }
        }
        val goingCount = goingList.size
        val isGoing = goingList.contains(currentUser?.id?.toString() ?: "")

        val interestedList = remember(event.interestedUserIds) {
            event.interestedUserIds.split(",")
                .filter { it.isNotBlank() }
                .map { it.trim() }
        }
        val interestedCount = interestedList.size
        val isInterested = interestedList.contains(currentUser?.id?.toString() ?: "")

        var showShareSheet by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { selectedEventForDetails = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(18.dp),
                color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else Color.White,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar with Back and Save
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedEventForDetails = null }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.toggleSaveEvent(event.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Save",
                                    tint = if (isSaved) GiggzGold else Color.Gray
                                )
                            }
                            IconButton(onClick = { showShareSheet = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Reply,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.graphicsLayer(scaleX = -1f)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Swipeable Image Gallery / Carousel
                        val imageList = remember(event.imageUrl, event.imageGallery) {
                            val list = mutableListOf<String>()
                            if (event.imageUrl.isNotBlank()) list.add(event.imageUrl)
                            if (event.imageGallery.isNotBlank()) {
                                list.addAll(event.imageGallery.split(",").filter { it.isNotBlank() }.map { it.trim() })
                            }
                            if (list.isEmpty()) {
                                list.add("https://images.unsplash.com/photo-1511578314322-379afb476865?w=800&auto=format&fit=crop")
                            }
                            list.distinct()
                        }
                        
                        Column(modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.foundation.lazy.LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(imageList.size) { index ->
                                    Box(modifier = Modifier.fillParentMaxWidth()) {
                                        coil.compose.AsyncImage(
                                            model = imageList[index],
                                            contentDescription = "Event Gallery Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${index + 1}/${imageList.size}",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                "Swipe horizontally to view other images",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                            )
                        }

                        // Title + Category tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = event.category,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Elite Badges
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (event.isFeatured) {
                                Box(
                                    modifier = Modifier
                                        .background(GiggzGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .border(0.5.dp, GiggzGold, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("🌟 Featured Spot", color = GiggzGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (event.isVerified) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .border(0.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("💎 Verified Organizer", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Date, Time, Location rows
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = event.date, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = event.location, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        // Attendance Counters
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$goingCount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "Going", fontSize = 11.sp, color = Color.Gray)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.3f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$interestedCount", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                                    Text(text = "Interested", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }

                        // Description
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Event Description", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = event.description,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                        // Organizer Profile
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Organizer Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                UserAvatar(photoUrl = event.promoterPhoto, name = event.promoterName, size = 42)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = event.promoterName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "Giggz Organizer • High Trust Score", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        // Social Media Links Display
                        if (event.socialLinks.isNotBlank()) {
                            val links = event.socialLinks.split(",").filter { it.isNotBlank() }
                            if (links.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Social Platform Links", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        links.forEach { rawUrl ->
                                            val url = rawUrl.trim()
                                            val (icon, label) = when {
                                                url.contains("instagram", ignoreCase = true) -> "📸" to "Instagram"
                                                url.contains("facebook", ignoreCase = true) -> "👥" to "Facebook"
                                                url.contains("twitter", ignoreCase = true) || url.contains("x.com", ignoreCase = true) -> "🐦" to "Twitter"
                                                url.contains("tiktok", ignoreCase = true) -> "🎵" to "TikTok"
                                                else -> "🔗" to "Link"
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                                                    .clickable {
                                                        try {
                                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(
                                                                if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                                            ))
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            context.showSafeToast("Cannot open link: $url")
                                                        }
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(icon, fontSize = 12.sp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Action buttons (Interested, Going, Share)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleInterestedEvent(event.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInterested) GiggzGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = if (isInterested) "🌿 Interested ✓" else "🌿 Interested",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isInterested) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { viewModel.toggleGoingEvent(event.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGoing) GiggzGreen else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = if (isGoing) "🌟 Going ✓" else "🌟 Going",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isGoing) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // SHARE SHEET DIALOG
        if (showShareSheet) {
            Dialog(onDismissRequest = { showShareSheet = false }) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Share Event", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                context.showSafeToast("Copied event link to clipboard! 📋")
                                showShareSheet = false
                            }) {
                                Icon(Icons.Filled.Launch, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                Text("Copy Link", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                context.showSafeToast("Opening WhatsApp share sheet...")
                                showShareSheet = false
                            }) {
                                Icon(Icons.Filled.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(36.dp))
                                Text("WhatsApp", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                context.showSafeToast("Sharing to social feeds...")
                                showShareSheet = false
                            }) {
                                Icon(Icons.Filled.People, contentDescription = null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(36.dp))
                                Text("Twitter", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showShareSheet = false }) {
                                Text("Cancel", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
