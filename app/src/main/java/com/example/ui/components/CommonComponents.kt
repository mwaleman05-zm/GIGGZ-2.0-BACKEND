package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import com.example.ui.theme.GiggzGold
import com.example.ui.theme.GiggzGreen
import com.example.R

fun resolveGiggzImage(imageKey: String): Any {
    val trimmed = imageKey.trim()
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("content://") || trimmed.startsWith("file://")) {
        return trimmed
    }
    return when (trimmed) {
        "img_furniture_ref" -> R.drawable.img_furniture_ref_1783175297320
        "img_toolkit" -> R.drawable.img_toolkit_1783175312197
        "img_soundbar" -> R.drawable.img_soundbar_1783175328378
        "img_giggz_hero" -> R.drawable.img_giggz_hero_clean_1784132285707
        "img_giggz_logo" -> R.drawable.img_giggz_logo
        else -> "https://images.unsplash.com/photo-154055700478-4be289fbecef?w=500" // premium fallback
    }
}

fun getDefaultCategoryImage(category: String): String {
    val lower = category.lowercase()
    return when {
        lower.contains("clean") || lower.contains("wash") -> 
            "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=500&auto=format&fit=crop&q=80"
        lower.contains("deliver") || lower.contains("courier") || lower.contains("logistic") || lower.contains("transport") -> 
            "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=500&auto=format&fit=crop&q=80"
        lower.contains("plumb") || lower.contains("leak") || lower.contains("water") || lower.contains("pipe") -> 
            "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?w=500&auto=format&fit=crop&q=80"
        lower.contains("design") || lower.contains("code") || lower.contains("mobile") || lower.contains("app") || lower.contains("web") || lower.contains("software") || lower.contains("tech") -> 
            "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=500&auto=format&fit=crop&q=80"
        lower.contains("carpentry") || lower.contains("wood") || lower.contains("furniture") || lower.contains("fix") || lower.contains("handy") -> 
            "https://images.unsplash.com/photo-1534224039826-c7a0eda0e6b3?w=500&auto=format&fit=crop&q=80"
        lower.contains("electric") || lower.contains("wire") || lower.contains("solar") || lower.contains("power") -> 
            "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=500&auto=format&fit=crop&q=80"
        else -> 
            "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=500&auto=format&fit=crop&q=80"
    }
}

@Composable
fun UserAvatar(
    photoUrl: String,
    name: String,
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    val initials = if (name.isNotBlank()) {
        name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
    } else "U"

    val colors = listOf(
        Color(0xFF1E3A8A), Color(0xFF15803D), Color(0xFFB45309), Color(0xFF0369A1),
        Color(0xFF6D28D9), Color(0xFFBE185D), Color(0xFF374151), Color(0xFF0F766E)
    )
    val colorIndex = initials.hashCode().let { if (it < 0) -it else it } % colors.size
    val fallbackColor = colors[colorIndex]

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(fallbackColor)
            .border(1.5.dp, GiggzGold, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank() && (photoUrl.startsWith("http") || photoUrl.startsWith("content") || photoUrl.startsWith("file"))) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initials,
                color = Color.White,
                fontSize = (size * 0.4).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun compressAndSaveImage(context: android.content.Context, uri: android.net.Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (originalBitmap == null) return uri.toString()

        // Downscale if too large (max dimension of 1024px to save storage)
        val maxDimension = 1024
        val width = originalBitmap.width
        val height = originalBitmap.height
        val (newWidth, newHeight) = if (width > height) {
            if (width > maxDimension) {
                Pair(maxDimension, (height * (maxDimension.toFloat() / width)).toInt())
            } else {
                Pair(width, height)
            }
        } else {
            if (height > maxDimension) {
                Pair((width * (maxDimension.toFloat() / height)).toInt(), maxDimension)
            } else {
                Pair(width, height)
            }
        }

        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        
        // Save to cache file with 70% JPEG compression quality
        val cacheFile = java.io.File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val outputStream = java.io.FileOutputStream(cacheFile)
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.flush()
        outputStream.close()

        if (scaledBitmap != originalBitmap) {
            scaledBitmap.recycle()
        }
        originalBitmap.recycle()

        android.net.Uri.fromFile(cacheFile).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        uri.toString()
    }
}

@Composable
fun ImageAttachmentPicker(
    imageUrl: String,
    onImageSelected: (String) -> Unit,
    label: String = "Attached Image",
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val compressedUri = compressAndSaveImage(context, uri)
            onImageSelected(compressedUri)
            context.showSafeToast("Selected & compressed photo to save storage! ⚡")
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clickable Image Icon/Preview Card
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
                    .border(
                        BorderStroke(
                            1.dp,
                            if (imageUrl.isNotBlank()) GiggzGreen.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.8f)
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Selected photo preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Image,
                            contentDescription = "Select from gallery",
                            tint = GiggzGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Add Photo",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = GiggzGreen
                        )
                    }
                }
            }

            // Compact details & auxiliary actions (Presets, Clear)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (imageUrl.isNotBlank()) {
                    Text(
                        text = "Image attached successfully!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GiggzGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Change",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GiggzGreen,
                            modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Remove",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier.clickable { onImageSelected("") }
                        )
                    }
                } else {
                    Text(
                        text = "Tap icon to browse device gallery",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

fun getEarnedRating(rawRating: Float, reviewsCount: Int): Float {
    if (reviewsCount <= 0) return 0.0f
    val ratingSum = rawRating * reviewsCount
    val earned = (ratingSum + 3.0f * 5) / (reviewsCount + 5)
    return (Math.round(earned * 10f) / 10f).coerceIn(1.0f, 5.0f)
}

fun getRatingLabel(rating: Float): String {
    return when {
        rating >= 5.0f -> "Outstanding reputation"
        rating >= 4.0f -> "Trusted"
        rating >= 3.0f -> "Decent and acceptable reputation"
        else -> "Needs improvement"
    }
}

@Composable
fun GiggzReputationCard(
    rating: Float,
    reviewsCount: Int,
    completedJobs: Int,
    modifier: Modifier = Modifier
) {
    val earnedRating = if (reviewsCount > 0) getEarnedRating(rating, reviewsCount) else 0.0f
    
    val isLight = MaterialTheme.colorScheme.surface == Color.White
    val isDark = !isLight
    val cardBg = if (isLight) Color.White else Color(0xFF1F2937)
    val borderCol = if (isLight) Color.LightGray.copy(alpha = 0.5f) else Color(0xFF374151)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = BorderStroke(1.dp, borderCol),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating Stars",
                            tint = GiggzGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (reviewsCount == 0) "New" else "%.1f".format(earnedRating),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = if (reviewsCount == 0) "No completed ratings yet" else "Based on $reviewsCount completed experiences",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray.copy(alpha = 0.7f) else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(GiggzGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Gigs Completed: $completedJobs",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GiggzGreen
                    )
                }
            }
            
            Divider(color = if (isDark) Color(0xFF374151) else Color.LightGray.copy(alpha = 0.4f))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (reviewsCount == 0) Color.Gray else if (earnedRating >= 4.0f) GiggzGreen else GiggzGold)
                )
                Text(
                    text = if (reviewsCount == 0) "Earn stars slowly through jobs" else getRatingLabel(earnedRating),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun RatingStars(
    rating: Float,
    reviewsCount: Int = 0,
    size: Int = 16,
    showCount: Boolean = true
) {
    val earnedRating = if (reviewsCount > 0) getEarnedRating(rating, reviewsCount) else 0.0f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (reviewsCount == 0) {
            Text(
                text = "⭐",
                fontSize = (size * 0.8).sp,
                fontWeight = FontWeight.Bold,
                color = GiggzGold
            )
            if (showCount) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(0 reviews)",
                    fontSize = (size * 0.7).sp,
                    color = Color.Gray
                )
            }
        } else {
            repeat(5) { index ->
                val starIndex = index + 1
                val imageVector = when {
                    earnedRating >= starIndex.toFloat() -> Icons.Filled.Star
                    earnedRating >= starIndex.toFloat() - 0.5f -> Icons.Filled.StarHalf
                    else -> Icons.Outlined.StarBorder
                }
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = GiggzGold,
                    modifier = Modifier.size(size.dp)
                )
            }
            if (showCount) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "%.1f (%d)".format(earnedRating, reviewsCount),
                    fontSize = (size * 0.8).sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GiggzGreen.copy(alpha = 0.08f))
            .border(0.5.dp, GiggzGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category,
            color = GiggzGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SupportHelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen)) {
                Text("Got It", color = Color.White)
            }
        },
        title = { Text("Giggz Help Center", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Frequently Asked Questions:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Q: How do I get paid?\nA: Payment is released upon job completion either via local mobile wallets (M-Pesa, etc.), bank deposits, or direct agreements with the Employer.",
                    fontSize = 12.sp
                )
                Text(
                    text = "Q: What is Ama Sampo Marketplace?\nA: Ama Sampo is a fast peer-to-peer neighborhood marketplace where you can trade items, sell equipment, or directly list specialized professional services.",
                    fontSize = 12.sp
                )
                Text(
                    text = "Q: How do AI Recommendations work?\nA: Giggz uses Gemini AI to match your listed profile skills and biography with current available job postings, generating customized tips and matchmaking scores.",
                    fontSize = 12.sp
                )
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Text(
                    text = "Need additional support? Contact us at support@giggz.com or call +1 800-GIGGZ.",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = GiggzGreen
                )
            }
        }
    )
}

@Composable
fun PrivacyTermsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen)) {
                Text("Accept", color = Color.White)
            }
        },
        title = { Text("Terms & Privacy Policy", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Welcome to Giggz, a smart marketplace linking skilled workers and job givers.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1. Acceptance of Terms\nBy creating an account as a Worker, Employer, or Admin on Giggz, you agree to comply with our localized standards and honest representation rules. Suspensions are strictly enforced for misleading applications or trading frauds.",
                    fontSize = 11.sp
                )
                Text(
                    text = "2. Data Privacy\nYour biography, portfolio, location, and listed marketplace assets are visible to active platform users. We do not sell user transaction histories or private communication data to third parties.",
                    fontSize = 11.sp
                )
                Text(
                    text = "3. Safe Working Environment\nJob details are created by independent Employers. Giggz is not liable for contractor-employer field disputes. Please utilize in-app messages to record terms before launching work.",
                    fontSize = 11.sp
                )
                Text(
                    text = "4. Ama Sampo Rules\nNo listing of restricted electronics, counterfeit brands, or illegal substances. All items are reviewed by administrator moderators. Reports are processed daily.",
                    fontSize = 11.sp
                )
            }
        }
    )
}

data class TrustProfile(
    val score: Int,
    val level: String, // Elite Performer, Trusted, Average, Risk Flag
    val color: Color,
    val completionRate: Int,
    val cancellationRate: Int,
    val responseTimeMin: Int,
    val repeatClientRate: Int,
    val aiSummary: String,
    val bestSkill: String,
    val needsImprovement: String,
    val commonPraise: String
)

fun calculateTrustProfile(user: com.example.data.UserEntity): TrustProfile {
    val seed = user.email.hashCode()
    
    // Base trust score based on user.rating
    var baseScore = (user.rating / 5f) * 75f
    // Add bonus for completed jobs
    baseScore += (user.completedJobs.coerceAtMost(15) * 1.2f)
    // Add reviews bonus
    baseScore += (user.reviewsCount.coerceAtMost(10) * 0.8f)
    
    val finalScore = if (user.completedJobs == 0 && user.reviewsCount == 0) {
        85
    } else {
        baseScore.toInt().coerceIn(10, 100)
    }
    
    val (level, color) = when {
        finalScore >= 90 -> "Elite Performer" to Color(0xFF15803D)
        finalScore >= 75 -> "Trusted" to Color(0xFF16A34A)
        finalScore >= 50 -> "Average" to Color(0xFFD97706)
        else -> "Risk Flag" to Color(0xFFDC2626)
    }
    
    val completionRate = (90 + (seed % 11).let { if (it < 0) -it else it }).coerceIn(75, 100)
    val cancellationRate = (2 + (seed % 9).let { if (it < 0) -it else it }).coerceIn(0, 15)
    val responseTimeMin = (5 + (seed % 25).let { if (it < 0) -it else it }).coerceIn(2, 45)
    val repeatClientRate = (10 + (seed % 35).let { if (it < 0) -it else it }).coerceIn(5, 50)
    
    val aiSummary: String
    val bestSkill: String
    val needsImprovement: String
    val commonPraise: String
    
    if (user.role == "Worker") {
        bestSkill = when {
            user.skills.contains("Carpentry") -> "Exquisite Woodworking & Cabinet Design"
            user.skills.contains("Electrical") -> "Solar Battery Setup & Secure Wiring"
            user.skills.contains("Plumbing") -> "Rapid Pipe Sealing & Leak Repair"
            user.skills.contains("Kotlin") || user.skills.contains("Mobile") -> "Performant Compose UI Architecture"
            else -> "Quality of service"
        }
        needsImprovement = when {
            finalScore > 92 -> "None identified"
            seed % 2 == 0 -> "Punctuality on rush-hour calls"
            else -> "Prompt response during busy hours"
        }
        commonPraise = when {
            seed % 3 == 0 -> "Extremely clean work and left workplace pristine."
            seed % 3 == 1 -> "Courteous, polite, and explained requirements clearly."
            else -> "Fast delivery and exceptional attention to instructions."
        }
        
        aiSummary = "${user.fullName} is a ${if (finalScore >= 90) "highly reliable and elite" else "consistently trusted"} professional with strong expertise in ${user.skills.split(",").firstOrNull()?.trim() ?: "their craft"}. Reviews highlight outstanding $commonPraise" + 
            if (needsImprovement != "None identified") " Slight delays reported under extreme conditions ($needsImprovement)." else " Zero negative flags reported on the platform."
    } else {
        bestSkill = "Prompt payments"
        needsImprovement = "Instructions clarity"
        commonPraise = "Fair dealing and flexible schedule"
        aiSummary = "${user.fullName} is a verified Giggz Client known for fast contract approvals, transparent instructions, and exceptional communication with remote and local contractors."
    }
    
    return TrustProfile(
        score = finalScore,
        level = level,
        color = color,
        completionRate = completionRate,
        cancellationRate = cancellationRate,
        responseTimeMin = responseTimeMin,
        repeatClientRate = repeatClientRate,
        aiSummary = aiSummary,
        bestSkill = bestSkill,
        needsImprovement = needsImprovement,
        commonPraise = commonPraise
    )
}

@Composable
fun MetricRowBar(
    label: String,
    rating: Float, // 1 to 5
    color: Color = GiggzGreen
) {
    val fraction = (rating / 5f).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Text(text = String.format("%.1f / 5.0", rating), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun InteractiveRatingRow(
    label: String,
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    color: Color = GiggzGold
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Text(text = "Rating: ${rating.toInt()} / 5 Stars", fontSize = 10.sp, color = Color.Gray)
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { index ->
                val isFilled = index <= rating
                Icon(
                    imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "$index Stars",
                    tint = if (isFilled) color else Color.LightGray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onRatingChanged(index.toFloat()) }
                )
            }
        }
    }
}

@Composable
fun TwoWayRatingDialog(
    isFromWorker: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (comment: String, ratings: List<Float>, imageProofUrl: String) -> Unit,
    jobTitle: String = "Gig Contract"
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var comment by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var selectedRating by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(5) }
    var imageProofUrl by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(2.dp, GiggzGreen, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFromWorker) "Rate Employer & Get Paid" else "Mark Complete & Rate Contractor",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GiggzGreen
                    )
                }

                Text(
                    text = "Contract: \"$jobTitle\"",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )

                Divider(color = Color.LightGray.copy(alpha = 0.5f))

                // Specific instructions based on role
                Text(
                    text = if (isFromWorker) {
                        "Your feedback protects the Giggz community. Please rate your experience with this employer fairly:"
                    } else {
                        "A fair review determines contractor trust status and ensures high-quality marketplace performance:"
                    },
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color.DarkGray
                )

                // Ratings input selection
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "How was your experience with this ${if (isFromWorker) "employer" else "worker"}?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= selectedRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$star Stars",
                                tint = if (star <= selectedRating) GiggzGold else Color.LightGray,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { selectedRating = star }
                            )
                        }
                    }
                    
                    val meaningLabel = when {
                        selectedRating >= 5 -> "⭐ 5.0 = Outstanding reputation"
                        selectedRating >= 4 -> "⭐ 4.0 = Very reliable"
                        selectedRating >= 3 -> "⭐ 3.0 = Decent and acceptable reputation"
                        else -> "⭐ Below 3.0 = Needs improvement"
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(GiggzGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = meaningLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GiggzGreen
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            val rVal = selectedRating.toFloat()
                            onSubmit("", listOf(rVal, rVal, rVal, rVal), "")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GiggzGreen
                        ),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Submit Review", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedCategoryPhoto(
    category: String,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    val lower = category.lowercase()
    val photoUrl = when {
        lower.contains("plumb") || lower.contains("leak") || lower.contains("water") || lower.contains("pipe") -> 
            "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?w=150&auto=format&fit=crop&q=80"
        lower.contains("carpentry") || lower.contains("wood") || lower.contains("furniture") || lower.contains("fix") || lower.contains("handy") -> 
            "https://images.unsplash.com/photo-1534224039826-c7a0eda0e6b3?w=150&auto=format&fit=crop&q=80"
        lower.contains("electric") || lower.contains("wire") || lower.contains("solar") || lower.contains("power") -> 
            "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=150&auto=format&fit=crop&q=80"
        else -> null
    }

    if (photoUrl != null) {
        Box(
            modifier = modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(GiggzGreen, GiggzGold)), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = category,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = category,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun JobDetailDialog(
    job: com.example.data.JobEntity,
    isBookmarked: Boolean,
    hasApplied: Boolean = false,
    onDismissRequest: () -> Unit,
    onBookmark: () -> Unit,
    onApplyClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GiggzGreen.copy(alpha = 0.04f))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Work,
                            contentDescription = null,
                            tint = GiggzGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (job.isPieceWork) "Casual Gig Details" else "Contract Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category & Title Row
                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GiggzGreen.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = job.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GiggzGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = job.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Key details row (Budget, Duration, Location)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
                            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Budget
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("PAYMENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("K${job.budget.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = GiggzGreen)
                            Text(job.payType, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Duration/Deadline
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(if (job.isPieceWork) "EST. TIME" else "DEADLINE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (job.isPieceWork) androidx.compose.material.icons.Icons.Filled.AccessTime else androidx.compose.material.icons.Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (job.isPieceWork) job.timeRequired else job.deadline,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Location
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("LOCATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(job.location, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    // Job Description
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "JOB DESCRIPTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = job.description,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Site Image: Display custom photo only if present
                    if (job.images.isNotBlank()) {
                        val imageSource = resolveGiggzImage(job.images.split(",").firstOrNull { it.isNotBlank() } ?: "")
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "JOB PICTURE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            AsyncImage(
                                model = imageSource,
                                contentDescription = "Job Picture",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Employer Info Card
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "POSTED BY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onProfileClick() }
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                                .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(photoUrl = job.employerPhoto, name = job.employerName, size = 44)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = job.employerName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Client • Tap to view profile",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Footer Actions Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bookmark action
                    IconButton(
                        onClick = onBookmark,
                        modifier = Modifier
                            .size(46.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) androidx.compose.material.icons.Icons.Filled.Bookmark else androidx.compose.material.icons.Icons.Filled.BookmarkBorder,
                            contentDescription = "Save job",
                            tint = if (isBookmarked) GiggzGold else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Chat action
                    IconButton(
                        onClick = onChatClick,
                        modifier = Modifier
                            .size(46.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), CircleShape)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Chat,
                            contentDescription = "Message employer",
                            tint = GiggzGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Apply Button
                    Button(
                        onClick = onApplyClick,
                        enabled = !hasApplied,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GiggzGreen,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = if (hasApplied) "Applied ✓" else if (job.isPieceWork) "Accept Gig" else "Apply Now",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


