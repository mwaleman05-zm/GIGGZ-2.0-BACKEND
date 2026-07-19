package com.example.ui.screens

import android.widget.Toast
import com.example.ui.components.showSafeToast
import com.example.ui.components.ImageAttachmentPicker
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Reply
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.People
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ListingEntity
import com.example.ui.GiggzViewModel
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import com.example.ui.components.RatingStars
import com.example.ui.components.UserAvatar
import com.example.ui.components.TwoWayRatingDialog
import com.example.data.UserEntity
import com.example.ui.theme.GiggzGold
import com.example.ui.theme.GiggzGreen

@Composable
fun AmaMarketplaceSection(
    viewModel: GiggzViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listingsList by viewModel.allListings.collectAsStateWithLifecycle()
    val savedListingIds by viewModel.savedListingIds.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser = viewModel.currentUser.collectAsStateWithLifecycle().value ?: return

    var selectedListingForDetail by remember { mutableStateOf<ListingEntity?>(null) }
    var selectedBuyerForRating by remember { mutableStateOf<UserEntity?>(null) }
    var showPostForm by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val activeSearchQuery by viewModel.marketplaceSearchQuery.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var selectedCategoryTab by remember { mutableStateOf("All") }

    LaunchedEffect(activeSearchQuery) {
        if (activeSearchQuery.isEmpty()) {
            searchQuery = ""
        }
    }

    val categories = listOf(
        "All", "Book/Rent", "Real Estate", "Events", "Electronics", "Furniture", "Fashion", "Phones", "Vehicles", "Home Items", 
        "Construction", "Food", "Services", "Painting", "Barber Shop", "Saloon & Hairdressing", "Other"
    )

    // Filtered items based on search query and category
    val filteredListings = listingsList.filter {
        (selectedCategoryTab == "All" || it.category == selectedCategoryTab) &&
        (activeSearchQuery.isBlank() ||
         it.title.contains(activeSearchQuery, ignoreCase = true) ||
         it.description.contains(activeSearchQuery, ignoreCase = true) ||
         it.category.contains(activeSearchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it 
                    viewModel.marketplaceSearchQuery.value = it
                },
                placeholder = { Text("Search products, services...", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GiggzGreen,
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        // Horizontal scroll category filter chips
        if (activeSearchQuery.isBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryTab == cat
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) GiggzGreen else if (isDarkMode) Color(0xFFE5E7EB) else Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GiggzGreen else if (isDarkMode) Color(0xFFD1D5DB) else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { 
                                if (cat == "Events") {
                                    viewModel.setTab("events")
                                } else {
                                    selectedCategoryTab = cat 
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else if (isDarkMode) Color(0xFF374151) else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        // Grid View of Listings
        if (filteredListings.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("No matching items listed in the marketplace.", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredListings) { item ->
                        val isSaved = savedListingIds.contains(item.id)
                        ListingGridCard(
                            item = item,
                            isSaved = isSaved,
                            onSave = { viewModel.saveListing(item.id) },
                            onClick = { selectedListingForDetail = item },
                            onSellerClick = { viewModel.showUserProfile(item.sellerId) }
                        )
                    }
                }

                // Floating Action Button to Sell / Post Service
                if (activeSearchQuery.isBlank()) {
                    FloatingActionButton(
                        onClick = { showPostForm = true },
                        containerColor = GiggzGreen,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = "Post Item")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("List Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Listing Detail Dialog
    if (selectedListingForDetail != null) {
        val target = selectedListingForDetail!!
        var showReportForm by remember { mutableStateOf(false) }
        var showExpressInterestDialog by remember { mutableStateOf(false) }
        var showBookRentDialog by remember { mutableStateOf(false) }
        var bookDuration by remember { mutableStateOf(1) }
        var bookDurationType by remember { mutableStateOf("Days") }
        var bookMessageText by remember { mutableStateOf("") }
        var interestMessageText by remember { mutableStateOf("Hi! I am very interested in your item: ${target.title}. Is it still available?") }
        var selectedImageIndex by remember { mutableStateOf(0) }

        // Find the live seller's profile so ratings stay completely reactive
        val liveSellerProfile = allUsers.find { it.id == target.sellerId }
        val liveRating = liveSellerProfile?.rating ?: 4.8f
        val liveReviewsCount = liveSellerProfile?.reviewsCount ?: 1

        Dialog(
            onDismissRequest = { selectedListingForDetail = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.95f)
                    .border(1.5.dp, GiggzGreen, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Storefront, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(target.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GiggzGreen)
                        }
                        IconButton(onClick = { selectedListingForDetail = null }) {
                            Text("✕", fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val allImages = remember(target.imageUrls) {
                        target.imageUrls.split(",").filter { it.isNotBlank() }
                    }

                    if (allImages.isNotEmpty()) {
                        val selectedImageUrl = allImages.getOrNull(selectedImageIndex) ?: allImages.first()
                        AsyncImage(
                            model = com.example.ui.components.resolveGiggzImage(selectedImageUrl),
                            contentDescription = "Listing item picture",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (allImages.size > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                allImages.forEachIndexed { idx, url ->
                                    val isSelected = idx == selectedImageIndex
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) GiggzGreen else Color.LightGray,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectedImageIndex = idx }
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Thumbnail ${idx + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Text(target.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("K${target.price}", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = GiggzGreen, modifier = Modifier.padding(vertical = 4.dp))

                    // Product Extra specifications (The requested product details!)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GiggzGreen.copy(alpha = 0.04f)),
                        border = BorderStroke(1.dp, GiggzGreen.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Trade Specifications", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GiggzGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Listing Status", fontSize = 10.sp, color = Color.Gray)
                                    Text(
                                        text = target.status,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (target.status == "Available") GiggzGreen else Color.Red
                                    )
                                }
                                Column {
                                    Text("Trade Type", fontSize = 10.sp, color = Color.Gray)
                                    Text(
                                        text = if (target.isOfferService) "Service Offer" else "Product Trade",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GiggzGold
                                    )
                                }
                                Column {
                                    Text("Location Center", fontSize = 10.sp, color = Color.Gray)
                                    Text(
                                        text = "Ama Sampo Hub",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Condition", fontSize = 10.sp, color = Color.Gray)
                                    Text(
                                        text = if (target.isOfferService) "N/A (Service)" else if (target.condition.isNotBlank()) target.condition else "Second Hand",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Trader Avatar Section
                    Text("Meet the Trader", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isDarkMode) Color(0xFF27272A) else Color(0xFFF9FAFB),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isDarkMode) Color(0xFF3F3F46) else Color(0xFFF3F4F6)
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.showUserProfile(target.sellerId) }
                            .padding(12.dp)
                    ) {
                        UserAvatar(photoUrl = target.sellerPhoto, name = target.sellerName, size = 48)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(target.sellerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Trader Class: ${target.sellerRole} • ", fontSize = 11.sp, color = Color.Gray)
                                RatingStars(rating = liveRating, reviewsCount = liveReviewsCount, size = 12)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Product Description", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(target.description, fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.padding(vertical = 4.dp))

                    val isMyListing = target.sellerId == currentUser.id

                    if (!isMyListing) {
                        Divider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))

                        val hasExpressedInterest = remember(target.interestedUserIds, currentUser.id) {
                            target.interestedUserIds.split(",").map { it.trim() }.contains(currentUser.id.toString())
                        }

                        // Trader Interactive Harder Rating Section with Interaction Requirements & Anti-Spam protection!
                        val allReviewsList by viewModel.allReviews.collectAsStateWithLifecycle()
                        val alreadyRated = remember(allReviewsList, target.id, currentUser.id) {
                            allReviewsList.any { it.reviewerId == currentUser.id && it.jobId == target.id }
                        }

                        if (alreadyRated) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = GiggzGreen.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, GiggzGreen.copy(alpha = 0.2f))
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GiggzGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("✅ You have submitted verified feedback for this marketplace transaction.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GiggzGreen)
                                }
                            }
                        } else if (!hasExpressedInterest) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Flag, contentDescription = null, tint = Color.Red)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("📩 Interaction Required: To rate this trader, you must first express interest by messaging them about this item.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Verified Trader Review", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Giggz ratings are built on actual trades. Select a star rating, leave detailed feedback, and confirm your transaction below:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                                    var selectedRating by remember { mutableStateOf(5) }
                                    var writtenReview by remember { mutableStateOf("") }
                                    var verificationAnswer by remember { mutableStateOf("") }
                                    
                                    val num1 = remember { (3..8).random() }
                                    val num2 = remember { (2..7).random() }
                                    val correctSum = num1 + num2
                                    
                                    var isSubmittingRating by remember { mutableStateOf(false) }

                                    // 1. Simple Star Rating selection
                                    Column(modifier = Modifier.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("How was your experience trading with ${target.sellerName}?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            (1..5).forEach { star ->
                                                IconButton(onClick = { selectedRating = star }, modifier = Modifier.size(36.dp)) {
                                                    Icon(
                                                        imageVector = if (star <= selectedRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                        contentDescription = null,
                                                        tint = if (star <= selectedRating) GiggzGold else Color.Gray.copy(alpha = 0.3f),
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
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

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // 2. Written review
                                    Text("Written Feedback Details (min 15 chars):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    OutlinedTextField(
                                        value = writtenReview,
                                        onValueChange = { writtenReview = it },
                                        placeholder = { Text("Describe the exchange details, trade promptness, or items...", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                        singleLine = false,
                                        maxLines = 3
                                    )
                                    Text("Length: ${writtenReview.length} / 15 minimum characters", fontSize = 10.sp, color = if (writtenReview.length >= 15) MaterialTheme.colorScheme.primary else Color.Red)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // 3. Math puzzle verification
                                    Text("Human Security Check:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text("Security Question: what is $num1 + $num2 = ?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = verificationAnswer,
                                            onValueChange = { verificationAnswer = it },
                                            placeholder = { Text("Sum", fontSize = 11.sp) },
                                            modifier = Modifier.width(80.dp),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    val isFormValid = selectedRating > 0 && 
                                                     writtenReview.length >= 15 && 
                                                     verificationAnswer.trim() == correctSum.toString()

                                    Button(
                                        onClick = {
                                            isSubmittingRating = true
                                            val rVal = selectedRating.toFloat()
                                            viewModel.submitReview(
                                                jobId = target.id,
                                                jobTitle = "Marketplace Trade: ${target.title}",
                                                reviewerId = currentUser.id,
                                                reviewerName = currentUser.fullName,
                                                reviewerPhoto = currentUser.profilePhoto,
                                                revieweeId = target.sellerId,
                                                revieweeName = target.sellerName,
                                                rating1 = rVal,
                                                rating2 = rVal,
                                                rating3 = rVal,
                                                rating4 = rVal,
                                                comment = writtenReview,
                                                imageUrl = target.imageUrls.split(",").firstOrNull { it.isNotBlank() } ?: "",
                                                isFromWorker = false,
                                                onComplete = {
                                                    isSubmittingRating = false
                                                    context.showSafeToast("Trade review submitted successfully! 🎉")
                                                    writtenReview = ""
                                                    verificationAnswer = ""
                                                }
                                            )
                                        },
                                        enabled = isFormValid && !isSubmittingRating,
                                        colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        if (isSubmittingRating) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                        } else {
                                            Text("Submit Verified Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isMyListing) {
                        Text(
                            text = "Interested Buyers",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GiggzGreen,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )

                        val interestedUsers = remember(target.interestedUserIds, allUsers) {
                            target.interestedUserIds.split(",")
                                .filter { it.isNotBlank() }
                                .mapNotNull { idStr ->
                                    val id = idStr.trim().toIntOrNull()
                                    allUsers.find { it.id == id }
                                }
                        }

                        if (interestedUsers.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Filled.People, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No one has expressed interest yet.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Interested buyers will show up here as soon as they message you about this item.",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(bottom = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                interestedUsers.forEach { interestedUser ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(18.dp))
                                                        .background(GiggzGreen.copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = interestedUser.fullName.take(1).uppercase(),
                                                        fontWeight = FontWeight.Bold,
                                                        color = GiggzGreen,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = interestedUser.fullName,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Black
                                                    )
                                                    Text(
                                                        text = interestedUser.role,
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.selectChatPartner(interestedUser)
                                                        selectedListingForDetail = null
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(Icons.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Chat Back", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = {
                                                        selectedBuyerForRating = interestedUser
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGold),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Rate Buyer", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Mark as Sold or Delete for seller
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (target.status != "Sold") {
                                Button(
                                    onClick = {
                                        viewModel.markListingAsSold(target.id)
                                        selectedListingForDetail = null
                                        context.showSafeToast("Listing marked as Sold!")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGold),
                                    modifier = Modifier.weight(1f).height(42.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Mark as Sold", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    viewModel.deleteListing(target.id)
                                    selectedListingForDetail = null
                                    context.showSafeToast("Listing deleted.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Delete Listing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        val hasExpressedInterest = remember(target.interestedUserIds, currentUser.id) {
                            target.interestedUserIds.split(",").map { it.trim() }.contains(currentUser.id.toString())
                        }

                        if (hasExpressedInterest) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = GiggzGreen.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, GiggzGreen.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "You have expressed interest in this item. The seller has been notified of your interest.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GiggzGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!hasExpressedInterest) {
                                Button(
                                    onClick = {
                                        if (target.category == "Book/Rent") {
                                            showBookRentDialog = true
                                        } else if (target.isOfferService) {
                                            val sellerProfile = allUsers.find { it.id == target.sellerId }
                                            if (sellerProfile != null) {
                                                viewModel.expressInterestInListing(target.id, "Hi, I am interested in your service: ${target.title}!")
                                                viewModel.selectChatPartner(sellerProfile)
                                                selectedListingForDetail = null
                                            } else {
                                                context.showSafeToast("Seller currently offline.")
                                            }
                                        } else {
                                            showExpressInterestDialog = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                    modifier = Modifier.weight(1.5f).height(46.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (target.category == "Book/Rent") Icons.Filled.CalendarToday else Icons.Filled.Chat,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (target.category == "Book/Rent") "Book/Rent Now" else "Express Interest",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val sellerProfile = allUsers.find { it.id == target.sellerId }
                                            if (sellerProfile != null) {
                                                viewModel.selectChatPartner(sellerProfile)
                                                selectedListingForDetail = null
                                            } else {
                                                context.showSafeToast("Seller currently offline.")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                    modifier = Modifier.weight(1.5f).height(46.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Chat, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Chat Back", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    context.showSafeToast("Link copied! Share Giggz Listing.")
                                },
                                modifier = Modifier.weight(0.7f).height(46.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Reply,
                                    contentDescription = "Share",
                                    modifier = Modifier.graphicsLayer(scaleX = -1f)
                                )
                            }

                            OutlinedButton(
                                onClick = { showReportForm = true },
                                modifier = Modifier.weight(0.7f).height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(Icons.Filled.Flag, contentDescription = "Report")
                            }
                        }
                    }
                }
            }
        }

        // Sub-dialog: Report form
        if (showReportForm) {
            var subject by remember { mutableStateOf("") }
            var reason by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showReportForm = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (subject.isBlank() || reason.isBlank()) {
                                context.showSafeToast("Please complete subject and reason fields.")
                                return@Button
                            }
                            viewModel.raiseReport(
                                reportedType = "listing",
                                reportedId = target.id,
                                subject = subject,
                                description = reason
                            ) {
                                context.showSafeToast("Thank you. Listing reported to administrators for immediate review.", Toast.LENGTH_LONG)
                                showReportForm = false
                                selectedListingForDetail = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Send Report", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportForm = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                title = { Text("Report Listing", color = Color.Red) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Please specify why you are flagging: '${target.title}'", fontSize = 11.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Issue (e.g. Counterfeit / Fake)") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Detailed Reason") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            maxLines = 3
                        )
                    }
                }
            )
        }

        // Sub-dialog: Express Interest and Message first
        if (showExpressInterestDialog) {
            AlertDialog(
                onDismissRequest = { showExpressInterestDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (interestMessageText.isBlank()) {
                                context.showSafeToast("Please enter a message to the seller.")
                                return@Button
                            }
                            viewModel.expressInterestInListing(target.id, interestMessageText)
                            context.showSafeToast("Message sent! You have expressed interest.")
                            showExpressInterestDialog = false
                            // Page stays intact as requested (selectedListingForDetail is not cleared)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen)
                    ) {
                        Text("Send Message", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExpressInterestDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                title = { Text("Express Interest", color = GiggzGreen, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "To express interest in '${target.title}', write a message to the seller. Your interest is recorded when you message first.",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        OutlinedTextField(
                            value = interestMessageText,
                            onValueChange = { interestMessageText = it },
                            label = { Text("Your Message") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GiggzGreen,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                }
            )
        }

        // Sub-dialog: Book / Rent configuration
        if (showBookRentDialog) {
            AlertDialog(
                onDismissRequest = { showBookRentDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            val msg = if (bookMessageText.isBlank()) "No additional notes." else bookMessageText
                            val fullMsg = "Hi! I would like to Book/Rent your item '${target.title}' for $bookDuration $bookDurationType. Estimated Total: K${(target.price * bookDuration).toInt()}. Additional Notes: $msg"
                            
                            viewModel.expressInterestInListing(target.id, fullMsg)
                            
                            val sellerProfile = allUsers.find { it.id == target.sellerId }
                            if (sellerProfile != null) {
                                viewModel.selectChatPartner(sellerProfile)
                            }
                            
                            context.showSafeToast("Booking request sent and conversation initiated!", Toast.LENGTH_LONG)
                            showBookRentDialog = false
                            // Page stays intact as requested (selectedListingForDetail is not cleared)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen)
                    ) {
                        Text("Confirm Booking", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBookRentDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Book/Rent Listing", color = GiggzGreen, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "You are booking/renting: ${target.title}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Base Rate: K${target.price.toInt()} per unit",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        
                        // Duration Stepper Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Duration:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { if (bookDuration > 1) bookDuration-- },
                                    modifier = Modifier.size(32.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                ) {
                                    Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                
                                Text(
                                    text = "$bookDuration",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                
                                IconButton(
                                    onClick = { bookDuration++ },
                                    modifier = Modifier.size(32.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                ) {
                                    Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                        
                        // Duration Type Selector Row (Days or Weeks)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Unit Type:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                                    .padding(2.dp)
                            ) {
                                listOf("Days", "Weeks").forEach { type ->
                                    val isSelected = bookDurationType == type
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isSelected) GiggzGreen else Color.Transparent,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { bookDurationType = type }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = type,
                                            color = if (isSelected) Color.White else Color.Gray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        
                        // Dynamically calculated cost
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GiggzGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Estimated Cost:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GiggzGreen)
                            Text(
                                text = "K${(target.price * bookDuration).toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = GiggzGreen
                            )
                        }
                        
                        OutlinedTextField(
                            value = bookMessageText,
                            onValueChange = { bookMessageText = it },
                            label = { Text("Special Requests / Delivery Notes") },
                            placeholder = { Text("Write delivery times, size needs, etc.") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GiggzGreen,
                                unfocusedBorderColor = Color.LightGray
                             )
                        )
                    }
                }
            )
        }
    }

    if (selectedBuyerForRating != null && selectedListingForDetail != null) {
        val buyerToRate = selectedBuyerForRating!!
        val targetListing = selectedListingForDetail!!
        TwoWayRatingDialog(
            isFromWorker = false,
            onDismiss = { selectedBuyerForRating = null },
            onSubmit = { comment, ratings, imageProofUrl ->
                val rVal = ratings.getOrNull(0) ?: 5f
                viewModel.submitReview(
                    jobId = targetListing.id,
                    jobTitle = "Marketplace Trade: ${targetListing.title}",
                    reviewerId = currentUser.id,
                    reviewerName = currentUser.fullName,
                    reviewerPhoto = currentUser.profilePhoto,
                    revieweeId = buyerToRate.id,
                    revieweeName = buyerToRate.fullName,
                    rating1 = rVal,
                    rating2 = rVal,
                    rating3 = rVal,
                    rating4 = rVal,
                    comment = comment,
                    imageUrl = imageProofUrl,
                    isFromWorker = false,
                    onComplete = {
                        selectedBuyerForRating = null
                        context.showSafeToast("Buyer rated successfully! 🎉")
                    }
                )
            },
            jobTitle = "Marketplace Listing: ${targetListing.title}"
        )
    }

    // Sell Post Listing Dialog Form
    if (showPostForm) {
        var pTitle by remember { mutableStateOf("") }
        var pPrice by remember { mutableStateOf("") }
        var pDesc by remember { mutableStateOf("") }
        var pCategory by remember { mutableStateOf("Electronics") }
        var isService by remember { mutableStateOf(false) }
        var pCondition by remember { mutableStateOf("Brand New") }
        var pPhotos by remember { mutableStateOf(listOf<String>()) }
        var showAddPhotoDialog by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { showPostForm = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.95f)
                    .border(1.5.dp, GiggzGreen, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("List Product or Service", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GiggzGreen)
                    Text("Ama Sampo Neighborhood marketplace", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Offering a Service?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isService,
                            onCheckedChange = {
                                isService = it
                                pCategory = if (it) "Painting" else "Electronics"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = pTitle,
                        onValueChange = { pTitle = it },
                        label = { Text("Title") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = pPrice,
                        onValueChange = { pPrice = it },
                        label = { Text("Price (K)") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simplistic dropdown trigger
                    Text("Select Category:", fontSize = 11.sp, color = Color.Gray)
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).clickable { expanded = true }.padding(12.dp)) {
                        Text(pCategory, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            val formCats = if (isService) {
                                listOf(
                                    "Painting",
                                    "Barber Shop",
                                    "Saloon & Hairdressing",
                                    "Cleaning & Laundry",
                                    "Plumbing",
                                    "Electrical Works",
                                    "Carpentry",
                                    "Gardening & Landscaping",
                                    "Catering & Cooking",
                                    "Photography & Video",
                                    "Tutoring & Lessons",
                                    "Tailoring & Fashion Design",
                                    "Other Services"
                                )
                            } else {
                                categories.filter { it != "All" && it != "Painting" && it != "Barber Shop" && it != "Saloon & Hairdressing" }
                            }
                            formCats.forEach { fc ->
                                DropdownMenuItem(
                                    text = { Text(fc) },
                                    onClick = {
                                        pCategory = fc
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = pDesc,
                        onValueChange = { pDesc = it },
                        label = { Text("Detailed Description") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        maxLines = 4
                    )

                    if (!isService) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Item Condition:", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Brand New", "Slightly Used", "Second Hand").forEach { cond ->
                                val isSelected = pCondition == cond
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (isSelected) GiggzGreen else Color.White,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) GiggzGreen else Color.LightGray,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { pCondition = cond }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cond,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Product Photos (Max 5):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display existing photos
                        pPhotos.forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Photo ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Clear/Delete button
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .background(Color.Red, RoundedCornerShape(bottomStart = 8.dp))
                                        .clickable {
                                            pPhotos = pPhotos.filterIndexed { i, _ -> i != index }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✕", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Add Photo Button (if less than 3)
                        if (pPhotos.size < 3) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GiggzGreen.copy(alpha = 0.05f))
                                    .border(1.5.dp, GiggzGreen, RoundedCornerShape(8.dp))
                                    .clickable { showAddPhotoDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text("+", color = GiggzGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("${pPhotos.size}/3", fontSize = 9.sp, color = GiggzGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val priceVal = pPrice.toDoubleOrNull() ?: 0.0
                                if (pTitle.isBlank() || pDesc.isBlank() || priceVal <= 0) {
                                    context.showSafeToast("Please complete listing details and valid price.")
                                    return@Button
                                }
                                viewModel.postListing(
                                    title = pTitle,
                                    description = pDesc,
                                    price = priceVal,
                                    category = pCategory,
                                    imageUrls = pPhotos.joinToString(","),
                                    isOfferService = isService,
                                    condition = if (isService) "" else pCondition
                                ) {
                                    context.showSafeToast("Listing uploaded!")
                                    showPostForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Post Listing", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showPostForm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }

        if (showAddPhotoDialog) {
            val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
            ) { uri: android.net.Uri? ->
                if (uri != null) {
                    if (pPhotos.size < 3) {
                        pPhotos = pPhotos + uri.toString()
                    }
                    showAddPhotoDialog = false
                    context.showSafeToast("Selected photo from device gallery!")
                }
            }

            AlertDialog(
                onDismissRequest = { showAddPhotoDialog = false },
                title = { Text("Add Photo (Max 3)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select a photo source to add to your product listing.", fontSize = 12.sp, color = Color.Gray)
                        
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Open Device Gallery", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddPhotoDialog = false }) {
                        Text("Cancel", color = GiggzGreen)
                    }
                }
            )
        }
    }
}

@Composable
fun ListingGridCard(
    item: ListingEntity,
    isSaved: Boolean,
    onSave: () -> Unit,
    onClick: () -> Unit,
    onSellerClick: () -> Unit
) {
    val firstImage = remember(item.imageUrls) {
        item.imageUrls.split(",").firstOrNull { it.isNotBlank() } ?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            if (firstImage.isNotBlank()) {
                // Header Mock Image Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(GiggzGreen.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = com.example.ui.components.resolveGiggzImage(firstImage),
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient dark overlay on images
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x33121417), // rgba(18, 20, 23, 0.2) top
                                            Color(0x99121417)  // rgba(18, 20, 23, 0.6) bottom
                                        )
                                    )
                                )
                        )
                    }

                    // Service tag
                    if (item.isOfferService) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("SERVICE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (item.condition.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(6.dp)
                                .background(GiggzGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(item.condition.uppercase(), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Bookmark icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(30.dp))
                            .clickable { onSave() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Save Item",
                            tint = if (isSaved) Color.Red else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                // When there is no photo, render tags and bookmark at the top in a compact row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 10.dp, end = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (item.isOfferService) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("SERVICE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (item.condition.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .background(GiggzGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(item.condition.uppercase(), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(30.dp))
                            .clickable { onSave() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Save Item",
                            tint = if (isSaved) Color.Red else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Info panel
            Column(modifier = Modifier.padding(10.dp)) {
                Text(item.title, maxLines = 1, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Seller: ${item.sellerName}",
                        maxLines = 1,
                        fontSize = 11.sp,
                        color = GiggzGreen,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        modifier = Modifier.clickable { onSellerClick() }
                    )
                }

                if (firstImage.isBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        fontSize = 11.sp,
                        maxLines = 3,
                        color = Color.Gray,
                        lineHeight = 14.sp,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("K${item.price}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = GiggzGreen)
                    if (item.status == "Sold") {
                        Text("SOLD", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    } else {
                        Text(item.category, fontSize = 8.sp, color = GiggzGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
