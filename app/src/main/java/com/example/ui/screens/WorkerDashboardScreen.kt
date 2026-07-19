package com.example.ui.screens

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Work
import android.widget.Toast
import com.example.ui.components.showSafeToast
import com.example.ui.components.ImageAttachmentPicker
import com.example.ui.components.resolveGiggzImage
import com.example.ui.components.JobDetailDialog
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
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
import com.example.ui.GiggzViewModel
import com.example.ui.components.CategoryBadge
import com.example.ui.components.UserAvatar
import com.example.ui.components.TwoWayRatingDialog
import com.example.ui.theme.GiggzGold
import com.example.ui.theme.GiggzGreen

@Composable
fun WorkerDashboard(
    viewModel: GiggzViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val worker = viewModel.currentUser.collectAsStateWithLifecycle().value ?: return
    val activeSubTab by viewModel.workerSubTab.collectAsStateWithLifecycle()
    val allJobsList by viewModel.allJobs.collectAsStateWithLifecycle()
    val allAppsList by viewModel.allApplications.collectAsStateWithLifecycle()
    val savedJobIds by viewModel.savedJobIds.collectAsStateWithLifecycle()

    var showApplyDialog by remember { mutableStateOf(false) }
    var selectedJobForApply by remember { mutableStateOf<JobEntity?>(null) }
    var selectedJobForDetail by remember { mutableStateOf<JobEntity?>(null) }
    var selectedAppForRating by remember { mutableStateOf<com.example.data.ApplicationEntity?>(null) }

    var attachedPdf by remember { mutableStateOf("") }

    val pdfPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            var displayName = "selected_cv.pdf"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                val path = uri.path
                if (path != null) {
                    val cut = path.lastIndexOf('/')
                    if (cut != -1) {
                        displayName = path.substring(cut + 1)
                    }
                }
            }
            if (!displayName.lowercase().endsWith(".pdf")) {
                displayName = "$displayName.pdf"
            }
            attachedPdf = displayName
            context.showSafeToast("Loaded CV PDF: $displayName")
        }
    }

    // Recommendation logic: match skills keywords and location
    val workerSkills = remember(worker.skills) {
        worker.skills.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
    }
    val workerLocation = remember(worker.location) {
        worker.location.trim().lowercase()
    }

    val isAllCategory = remember(workerSkills) {
        workerSkills.any { it == "all" }
    }

    val sortedJobs = remember(allJobsList, workerSkills, workerLocation) {
        allJobsList.filter { job -> job.status == "Active" && !job.isPieceWork }.map { job ->
            val matchesSkills = workerSkills.filter { skill ->
                job.category.lowercase().contains(skill) ||
                job.title.lowercase().contains(skill) ||
                job.description.lowercase().contains(skill)
            }
            val matchesLocation = workerLocation.isNotEmpty() && (
                job.location.lowercase().contains(workerLocation) ||
                workerLocation.contains(job.location.lowercase())
            )
            // Bring all active Gigs/Contracts to everyone within the area!
            // Give highest priority/score to location match, then skills.
            val matchScore = (if (matchesLocation) 100 else 0) + (if (matchesSkills.isNotEmpty()) 10 else 0)
            Pair(job, matchScore)
        }.sortedByDescending { it.second }
    }

    val finalJobsFeed = sortedJobs.map { it.first }
    val appliedJobs = allAppsList.filter { it.workerId == worker.id }
    val savedJobs = allJobsList.filter { savedJobIds.contains(it.id) }
    
    val events by viewModel.allEvents.collectAsStateWithLifecycle()
    val savedEvents = remember(events, worker) {
        events.filter { event ->
            val savedList = event.savedByUserIds.split(",")
                .filter { it.isNotBlank() }
                .map { it.trim() }
            savedList.contains(worker.id.toString())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().padding(horizontal = 16.dp)
    ) {
        // Sub Navigation Tabs
        TabRow(
            selectedTabIndex = when (activeSubTab) {
                "recommended" -> 0
                "applied" -> 1
                else -> 2
            },
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                val activeIndex = when (activeSubTab) {
                    "recommended" -> 0
                    "applied" -> 1
                    else -> 2
                }
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeIndex]),
                    color = GiggzGreen
                )
            }
        ) {
            Tab(
                selected = activeSubTab == "recommended",
                onClick = { viewModel.workerSubTab.value = "recommended" },
                text = { Text("Recommended", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                selectedContentColor = GiggzGreen,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Tab(
                selected = activeSubTab == "applied",
                onClick = { viewModel.workerSubTab.value = "applied" },
                text = { Text("Applied (${appliedJobs.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                selectedContentColor = GiggzGreen,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Tab(
                selected = activeSubTab == "saved",
                onClick = { viewModel.workerSubTab.value = "saved" },
                text = { Text("Saved (${savedJobs.size + savedEvents.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                selectedContentColor = GiggzGreen,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content Feed
        val currentFeed = when (activeSubTab) {
            "recommended" -> finalJobsFeed
            "applied" -> emptyList() // Render separately
            else -> savedJobs
        }

        if (activeSubTab == "applied") {
            if (appliedJobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No submitted applications yet.", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(appliedJobs) { app ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(app.jobTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    if (app.status == "Completed") {
                                        Button(
                                            onClick = { selectedAppForRating = app },
                                            colors = ButtonDefaults.buttonColors(containerColor = GiggzGold),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Rate Employer", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.TaskAlt, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Status: ${app.status}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GiggzGreen)
                                }
                                Text("Submitted Cover Letter: ${app.coverLetter}", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == "saved") {
            if (savedJobs.isEmpty() && savedEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No saved items yet.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Save job contracts or community events to track them here.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    if (savedJobs.isNotEmpty()) {
                        item {
                            Text(
                                "Saved Gigs & Contracts (${savedJobs.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GiggzGreen,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(savedJobs) { job ->
                            val hasApplied = allAppsList.any { it.jobId == job.id && it.workerId == worker.id }
                            JobCardItem(
                                job = job,
                                isBookmarked = true,
                                recommendationReason = null,
                                hasApplied = hasApplied,
                                onBookmark = { viewModel.saveJob(job.id) },
                                onApplyClick = {
                                    selectedJobForApply = job
                                    showApplyDialog = true
                                },
                                onChatClick = {
                                    coroutineScope.launch {
                                        val employer = viewModel.allUsers.value.find { it.id == job.employerId }
                                        if (employer != null) {
                                            viewModel.selectChatPartner(employer)
                                        } else {
                                            context.showSafeToast("Employer profile offline.")
                                        }
                                    }
                                },
                                onProfileClick = {
                                    viewModel.showUserProfile(job.employerId)
                                },
                                onClick = {
                                    selectedJobForDetail = job
                                }
                            )
                        }
                    }

                    if (savedEvents.isNotEmpty()) {
                        item {
                            Text(
                                "Saved Community Events (${savedEvents.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GiggzGreen,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                        }
                        items(savedEvents) { event ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail banner
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        val finalImgUrl = event.imageUrl.ifBlank {
                                            "https://images.unsplash.com/photo-1511578314322-379afb476865?w=200&auto=format&fit=crop"
                                        }
                                        coil.compose.AsyncImage(
                                            model = finalImgUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${event.date} • ${event.location}", fontSize = 11.sp, color = Color.Gray)
                                        // Importance Badge based on saves
                                        val saves = event.savedByUserIds.split(",").filter { it.isNotBlank() }.size
                                        val impText = when {
                                            saves == 0 -> "Local Interest"
                                            saves == 1 -> "Growing Interest"
                                            saves == 2 -> "Trending Local Hub"
                                            else -> "Featured Mega Event"
                                        }
                                        Text("Importance: $impText", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleSaveEvent(event.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Filled.Star, contentDescription = "Unsave", tint = GiggzGold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (finalJobsFeed.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No jobs found matching this section.", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(finalJobsFeed) { job ->
                        val isBookmarked = savedJobIds.contains(job.id)

                        val matchedSkills = remember(workerSkills, job) {
                            workerSkills.filter { skill ->
                                job.category.lowercase().contains(skill) ||
                                job.title.lowercase().contains(skill) ||
                                job.description.lowercase().contains(skill)
                            }
                        }
                        val matchesLocation = remember(workerLocation, job.location) {
                            workerLocation.isNotEmpty() && job.location.lowercase().contains(workerLocation)
                        }

                        val recommendationReason = when {
                            matchesLocation ->
                                "Location Match: Jobs near ${job.location}"
                            else -> null
                        }

                        val hasApplied = allAppsList.any { it.jobId == job.id && it.workerId == worker.id }
                        JobCardItem(
                            job = job,
                            isBookmarked = isBookmarked,
                            recommendationReason = recommendationReason,
                            hasApplied = hasApplied,
                            onBookmark = { viewModel.saveJob(job.id) },
                            onApplyClick = {
                                selectedJobForApply = job
                                showApplyDialog = true
                            },
                            onChatClick = {
                                coroutineScope.launch {
                                    val employer = viewModel.allUsers.value.find { it.id == job.employerId }
                                    if (employer != null) {
                                        viewModel.selectChatPartner(employer)
                                    } else {
                                        context.showSafeToast("Employer profile offline.")
                                    }
                                }
                            },
                            onProfileClick = {
                                viewModel.showUserProfile(job.employerId)
                            },
                            onClick = {
                                selectedJobForDetail = job
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog for Job Applications
    if (showApplyDialog && selectedJobForApply != null) {
        val targetJob = selectedJobForApply!!
        var coverLetter by remember { mutableStateOf("") }
        var attachedImage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { 
                attachedPdf = ""
                showApplyDialog = false 
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (coverLetter.isBlank()) {
                            context.showSafeToast("Please write a cover letter.")
                            return@Button
                        }
                        viewModel.applyForJob(
                            job = targetJob,
                            coverLetter = coverLetter,
                            pdfPath = attachedPdf,
                            imagePath = attachedImage
                        ) {
                            context.showSafeToast("Application submitted successfully!")
                            attachedPdf = ""
                            showApplyDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen)
                ) {
                    Text("Submit Application", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    attachedPdf = ""
                    showApplyDialog = false 
                }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = { Text("Apply for Contract", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Job: ${targetJob.title} (${targetJob.employerName})",
                        fontWeight = FontWeight.SemiBold,
                        color = GiggzGreen,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = coverLetter,
                        onValueChange = { coverLetter = it },
                        label = { Text("Cover Letter / Introduction") },
                        placeholder = { Text("Explain why you are the perfect worker for this contract.") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        maxLines = 4
                    )

                    Text("Attachments & CV Documentation:", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

                    if (attachedPdf.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GiggzGreen.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GiggzGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("📄", fontSize = 18.sp)
                                    Column {
                                        Text(
                                            text = attachedPdf,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.Black,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Ready to upload",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { attachedPdf = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear CV",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { 
                                    try {
                                        pdfPickerLauncher.launch("application/pdf") 
                                    } catch (e: Exception) {
                                        context.showSafeToast("Could not open file manager")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                modifier = Modifier.weight(1.2f).height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("📁", fontSize = 14.sp)
                                    Text("Select PDF from Phone", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { attachedPdf = "${worker.fullName.replace(" ", "_")}_Resume.pdf" },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GiggzGreen),
                                border = BorderStroke(1.dp, GiggzGreen),
                                modifier = Modifier.weight(0.8f).height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Use Mock CV", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        )
    }

    val appToRate = selectedAppForRating
    if (appToRate != null) {
        val employerProfile = viewModel.allUsers.collectAsStateWithLifecycle().value.find { it.id == appToRate.employerId }
        val employerName = employerProfile?.fullName ?: "Employer"
        TwoWayRatingDialog(
            isFromWorker = true,
            onDismiss = { selectedAppForRating = null },
            onSubmit = { comment, ratings, imageProofUrl ->
                val r1 = ratings.getOrNull(0) ?: 5f
                val r2 = ratings.getOrNull(1) ?: 5f
                val r3 = ratings.getOrNull(2) ?: 5f
                val r4 = ratings.getOrNull(3) ?: 5f
                viewModel.submitReview(
                    jobId = appToRate.jobId,
                    jobTitle = appToRate.jobTitle,
                    reviewerId = worker.id,
                    reviewerName = worker.fullName,
                    reviewerPhoto = worker.profilePhoto,
                    revieweeId = appToRate.employerId,
                    revieweeName = employerName,
                    rating1 = r1,
                    rating2 = r2,
                    rating3 = r3,
                    rating4 = r4,
                    comment = comment,
                    imageUrl = imageProofUrl,
                    isFromWorker = true,
                    onComplete = {
                        selectedAppForRating = null
                        context.showSafeToast("Employer rated successfully! Trust profile updated. 💎")
                    }
                )
            },
            jobTitle = appToRate.jobTitle
        )
    }

    if (selectedJobForDetail != null) {
        val detailJob = selectedJobForDetail!!
        val isBookmarked = savedJobIds.contains(detailJob.id)
        val hasApplied = allAppsList.any { it.jobId == detailJob.id && it.workerId == worker.id }
        JobDetailDialog(
            job = detailJob,
            isBookmarked = isBookmarked,
            hasApplied = hasApplied,
            onDismissRequest = { selectedJobForDetail = null },
            onBookmark = { viewModel.saveJob(detailJob.id) },
            onApplyClick = {
                selectedJobForDetail = null
                selectedJobForApply = detailJob
                showApplyDialog = true
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

data class CategoryStyle(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val bgColor: Color,
    val iconTint: Color
)

@Composable
fun getCategoryStyle(category: String): CategoryStyle {
    val lower = category.lowercase()
    return when {
        lower.contains("clean") || lower.contains("wash") -> CategoryStyle(
            icon = Icons.Filled.Home,
            bgColor = Color(0xFF8E24AA), // Darker premium purple
            iconTint = Color(0xFFF3E5F5) // Soft contrast light purple
        )
        lower.contains("deliver") || lower.contains("courier") || lower.contains("logistic") || lower.contains("transport") -> CategoryStyle(
            icon = Icons.Filled.LocalShipping,
            bgColor = Color(0xFF2E7D32), // Darker premium green
            iconTint = Color(0xFFE8F5E9) // Soft contrast light green
        )
        lower.contains("plumb") || lower.contains("leak") || lower.contains("water") || lower.contains("pipe") -> CategoryStyle(
            icon = Icons.Filled.Build,
            bgColor = Color(0xFFD84315), // Darker premium orange-red
            iconTint = Color(0xFFFBE9E7) // Soft contrast light orange
        )
        lower.contains("design") || lower.contains("code") || lower.contains("mobile") || lower.contains("app") || lower.contains("web") || lower.contains("software") || lower.contains("tech") -> CategoryStyle(
            icon = Icons.Filled.Work,
            bgColor = Color(0xFF1565C0), // Darker premium blue
            iconTint = Color(0xFFE3F2FD) // Soft contrast light blue
        )
        lower.contains("carpentry") || lower.contains("wood") || lower.contains("furniture") || lower.contains("fix") || lower.contains("handy") -> CategoryStyle(
            icon = Icons.Filled.Build,
            bgColor = Color(0xFF4E342E), // Darker premium brown
            iconTint = Color(0xFFEFEBE9) // Soft contrast light brown
        )
        lower.contains("electric") || lower.contains("wire") || lower.contains("solar") || lower.contains("power") -> CategoryStyle(
            icon = Icons.Filled.Build,
            bgColor = Color(0xFFF9A825), // Darker premium golden yellow
            iconTint = Color(0xFFFFFDE7) // Soft contrast light yellow
        )
        else -> CategoryStyle(
            icon = Icons.Filled.Work,
            bgColor = Color(0xFF37474F), // Darker premium slate grey
            iconTint = Color(0xFFECEFF1) // Soft contrast light grey
        )
    }
}

@Composable
fun JobCardItem(
    job: JobEntity,
    isBookmarked: Boolean,
    recommendationReason: String? = null,
    hasApplied: Boolean = false,
    onBookmark: () -> Unit,
    onApplyClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit,
    onClick: () -> Unit
) {
    val style = getCategoryStyle(job.category)
    val formattedName = remember(job.employerName) {
        val parts = job.employerName.split(" ")
        if (parts.size >= 2) {
            val secondPart = parts[1]
            val initial = if (secondPart.isNotEmpty()) "${secondPart.first().uppercase()}." else ""
            "${parts[0]} $initial"
        } else {
            job.employerName
        }
    }
    val rating = remember(job.employerId) {
        val base = 4.5 + (job.employerId % 5) * 0.1
        String.format("%.1f", if (base > 5.0) 5.0 else base)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (recommendationReason != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .background(GiggzGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, GiggzGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = GiggzGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = recommendationReason,
                        color = GiggzGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Middle Column: Category, Title, Description, Footer details
                Column(modifier = Modifier.weight(1f)) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(style.bgColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = job.category,
                            color = style.iconTint,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = job.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = job.description,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = job.location,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = job.deadline,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right Column: Price & Owner details
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    // Price
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "K${job.budget.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GiggzGreen
                        )
                        Text(
                            text = "Fixed Price",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // User Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onProfileClick() }
                    ) {
                        UserAvatar(photoUrl = job.employerPhoto, name = job.employerName, size = 28)
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = formattedName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Rating",
                                    tint = GiggzGold,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(1.dp))
                                Text(
                                    text = rating,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            // Site Image: Display custom photo only if present
            if (job.images.isNotBlank()) {
                val imageSource = resolveGiggzImage(job.images.split(",").firstOrNull { it.isNotBlank() } ?: "")
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = imageSource,
                    contentDescription = "Site Picture",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Slim Action Row at the Bottom for native interactions
            Divider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = onBookmark, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) GiggzGold else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onChatClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = "Chat",
                            tint = GiggzGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Button(
                    onClick = onApplyClick,
                    enabled = !hasApplied,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GiggzGreen,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = if (hasApplied) "Applied ✓" else "Apply", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
