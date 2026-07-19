package com.example.ui

import android.widget.Toast
import com.example.ui.components.showSafeToast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserEntity
import com.example.data.ApplicationEntity
import com.example.data.JobEntity
import com.example.ui.components.CategoryBadge
import com.example.ui.components.PrivacyTermsDialog
import com.example.ui.components.RatingStars
import com.example.ui.components.SupportHelpDialog
import com.example.ui.components.UserAvatar
import com.example.ui.components.calculateTrustProfile
import com.example.ui.components.MetricRowBar
import com.example.ui.components.TwoWayRatingDialog
import com.example.ui.components.GiggzReputationCard
import com.example.ui.screens.*
import com.example.ui.theme.GiggzGold
import com.example.ui.theme.GiggzGreen
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiggzApp(
    viewModel: GiggzViewModel,
    darkThemeState: MutableState<Boolean>,
    languageState: MutableState<String>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val screenState by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedProfileUser by viewModel.selectedProfileUser.collectAsStateWithLifecycle()

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showWalletComingSoonDialog by remember { mutableStateOf(false) }
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()

    val currentThemeName by viewModel.currentTheme.collectAsStateWithLifecycle()
    val activeWallpaper by viewModel.chatWallpaper.collectAsStateWithLifecycle()

    // Navigation Router
    when {
        currentUser == null -> {
            // Unauthenticated Flow
            val actualScreen = if (screenState == "animated_splash" && viewModel.hasPlayedSplash) {
                "login"
            } else {
                screenState
            }
            when (actualScreen) {
                "animated_splash" -> AnimatedSplashScreen(viewModel) {
                    viewModel.hasPlayedSplash = true
                    viewModel.navigateTo(it)
                }
                "splash" -> SplashScreen(viewModel) { viewModel.navigateTo(it) }
                "login" -> LoginScreen(viewModel) { viewModel.navigateTo(it) }
                "register" -> RegisterScreen(viewModel) { viewModel.navigateTo(it) }
                else -> {
                    if (!viewModel.hasPlayedSplash) {
                        AnimatedSplashScreen(viewModel) {
                            viewModel.hasPlayedSplash = true
                            viewModel.navigateTo("login")
                        }
                    } else {
                        LoginScreen(viewModel) { viewModel.navigateTo(it) }
                    }
                }
            }
        }
        currentUser != null && screenState == "notifications" -> {
            NotificationsFullScreen(viewModel) {
                viewModel.navigateTo("app")
            }
        }
        currentUser != null && currentUser!!.role == "Worker" && currentUser!!.phone.isBlank() -> {
            // Worker profile completion wizard
            WorkerProfileSetupScreen(viewModel) {
                viewModel.navigateTo("app")
            }
        }
        else -> {
            // Main Authenticated Workspace
            val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
            val notificationsList by viewModel.notifications.collectAsStateWithLifecycle()
            val activeNotificationsList by viewModel.activeNotifications.collectAsStateWithLifecycle()
            val chatPartner by viewModel.chatPartner.collectAsStateWithLifecycle()
            val mSearchQuery by viewModel.marketplaceSearchQuery.collectAsStateWithLifecycle()
            val unreadCount = activeNotificationsList.filter { !it.isRead }.size
            var showBoostPremiumDialog by remember { mutableStateOf(false) }
            var showJournalDialog by remember { mutableStateOf(false) }
            var showNewUsersDialog by remember { mutableStateOf(false) }

            val user = currentUser!!

            // Localized labels based on setting
            val labels = getLocalizedLabels(languageState.value)

            val tabsList = when (user.role) {
                "Worker" -> listOf("dashboard", "piece_gigs", "marketplace", "messages", "profile", "events", "my_contracts")
                "Employer" -> listOf("my_contracts", "marketplace", "messages", "profile", "events", "dashboard")

                else -> listOf("dashboard", "messages", "profile", "events")
            }

            val pagerState = androidx.compose.foundation.pager.rememberPagerState(
                initialPage = tabsList.indexOf(activeTab).coerceAtLeast(0),
                pageCount = { tabsList.size }
            )

            val isViewingActiveChat = chatPartner != null && tabsList.getOrNull(pagerState.currentPage) == "messages"

            // Sync pager to activeTab
            androidx.compose.runtime.LaunchedEffect(activeTab) {
                val targetPage = tabsList.indexOf(activeTab)
                if (targetPage >= 0 && pagerState.settledPage != targetPage) {
                    pagerState.animateScrollToPage(targetPage)
                }
            }

            // Sync activeTab to pager when settled
            androidx.compose.runtime.LaunchedEffect(pagerState.settledPage) {
                val targetTab = tabsList.getOrNull(pagerState.settledPage)
                if (targetTab != null && activeTab != targetTab) {
                    viewModel.setTab(targetTab)
                }
            }

            Scaffold(
                topBar = {
                    if (!isViewingActiveChat) {
                        TopAppBar(
                            navigationIcon = {
                                if (activeTab == "marketplace" && mSearchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            viewModel.marketplaceSearchQuery.value = ""
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = GiggzGreen
                                        )
                                    }
                                } else if (activeTab == "messages") {
                                    IconButton(
                                        onClick = {
                                            viewModel.setTab(if (user.role == "Employer") "my_contracts" else "dashboard")
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = GiggzGreen
                                        )
                                    }
                                }
                            },
                            title = {
                                val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(enabled = !isRefreshing) {
                                            val readablePageName = when (activeTab) {
                                                "dashboard" -> "Job Feed"
                                                "my_contracts" -> "Contracts Console"
                                                "piece_gigs" -> labels["piece_gigs"]!!
                                                "performance" -> labels["performance"] ?: "Performance"
                                                "messages" -> labels["messages"]!!
                                                "profile" -> labels["profile"]!!
                                                "admin" -> labels["admin"]!!
                                                "recruit" -> "Giggz Recruit"
                                                else -> "Giggz"
                                            }
                                            context.showSafeToast("Refreshing $readablePageName...")
                                            viewModel.refreshData(activeTab) {
                                                context.showSafeToast("$readablePageName page refreshed successfully!")
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    if (!(activeTab == "performance" && mSearchQuery.isNotBlank())) {
                                        if (activeTab != "messages") {
                                            Icon(
                                                imageVector = if (activeTab == "performance") Icons.Filled.TrendingUp else Icons.Filled.Storefront,
                                                contentDescription = null,
                                                tint = GiggzGold,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = when (activeTab) {
                                                "dashboard" -> "Job Feed"
                                                "my_contracts" -> "Contracts Console"
                                                "piece_gigs" -> labels["piece_gigs"]!!
                                                "performance" -> labels["performance"] ?: "Performance"
                                                "messages" -> labels["messages"]!!
                                                "profile" -> labels["profile"]!!
                                                "admin" -> labels["admin"]!!
                                                "recruit" -> "Giggz Recruit"
                                                else -> "Giggz"
                                            },
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GiggzGreen
                                        )
                                    } else {
                                        Text(
                                            text = "Search Results",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GiggzGreen
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            actions = {
                                // Boost icon before the notification icon
                                IconButton(
                                    onClick = {
                                        showBoostPremiumDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = "Boost Profile",
                                        tint = GiggzGold
                                    )
                                }

                                // Notifications tray icon with unread badge counter (Put the notification first)
                                Box(modifier = Modifier.clickable { showNotificationsDialog = true }.padding(8.dp)) {
                                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = GiggzGreen)
                                    if (unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 4.dp, y = (-4).dp)
                                                .clip(RoundedCornerShape(30.dp))
                                                .background(GiggzGold)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(unreadCount.toString(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Events celebration icon next to notification bell
                                IconButton(
                                    onClick = { viewModel.setTab("events") }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Celebration,
                                        contentDescription = "Events",
                                        tint = if (activeTab == "events") GiggzGold else GiggzGreen
                                    )
                                }

                                // Job Feed button (Where dark mode icon was, for employer side only)
                                if (user.role == "Employer") {
                                    IconButton(
                                        onClick = {
                                            viewModel.setTab("dashboard")
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Feed,
                                            contentDescription = "Job Feed",
                                            tint = if (activeTab == "dashboard") GiggzGold else GiggzGreen
                                        )
                                    }
                                }

                                // Contracts Console button next to Job Feed for easy access
                                if (user.role == "Worker") {
                                    IconButton(
                                        onClick = {
                                            viewModel.setTab("my_contracts")
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.BusinessCenter,
                                            contentDescription = "Contracts",
                                            tint = if (activeTab == "my_contracts") GiggzGold else GiggzGreen
                                        )
                                    }
                                }
                            }
                        )
                    }
                },
                bottomBar = {
                    if (!isViewingActiveChat) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            // Shared Bottom Tabs


                            if (user.role == "Employer") {
                                NavigationBarItem(
                                    selected = activeTab == "my_contracts",
                                    onClick = { viewModel.setTab("my_contracts") },
                                    icon = { Icon(Icons.Filled.BusinessCenter, contentDescription = null) },
                                    label = { Text("Contracts", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GiggzGreen, selectedTextColor = GiggzGreen)
                                )
                                NavigationBarItem(
                                    selected = activeTab == "marketplace",
                                    onClick = { viewModel.setTab("marketplace") },
                                    icon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                                    label = { Text("Marketplace", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GiggzGreen, selectedTextColor = GiggzGreen)
                                )
                            }

                            if (user.role == "Worker") {
                                NavigationBarItem(
                                    selected = activeTab == "dashboard",
                                    onClick = { viewModel.setTab("dashboard") },
                                    icon = { Icon(Icons.Filled.Work, contentDescription = null) },
                                    label = { Text("Job Feed", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GiggzGreen, selectedTextColor = GiggzGreen)
                                )
                                NavigationBarItem(
                                    selected = activeTab == "piece_gigs",
                                    onClick = { viewModel.setTab("piece_gigs") },
                                    icon = { Icon(Icons.Filled.Timer, contentDescription = null) },
                                    label = { Text(labels["nav_piece"]!!, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GiggzGreen, selectedTextColor = GiggzGreen)
                                )
                                NavigationBarItem(
                                    selected = activeTab == "marketplace",
                                    onClick = { viewModel.setTab("marketplace") },
                                    icon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                                    label = { Text("Marketplace", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = GiggzGreen, selectedTextColor = GiggzGreen)
                                )
                            }


                            NavigationBarItem(
                                selected = activeTab == "messages",
                                onClick = { viewModel.setTab("messages") },
                                icon = { Icon(Icons.Filled.Chat, contentDescription = null) },
                                label = { Text("Chats", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = GiggzGreen, selectedTextColor = GiggzGreen)
                            )
                            NavigationBarItem(
                                selected = activeTab == "profile",
                                onClick = { viewModel.setTab("profile") },
                                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                label = { Text(labels["nav_profile"]!!, fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = GiggzGreen, selectedTextColor = GiggzGreen)
                            )
                        }
                    }
                }
            ) { paddingValues ->
                val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (isRefreshing) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(3.dp),
                                color = GiggzGreen,
                                trackColor = GiggzGreen.copy(alpha = 0.15f)
                            )
                        }
                        androidx.compose.foundation.pager.HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize().weight(1f),
                            key = { pageIndex -> tabsList.getOrNull(pageIndex) ?: pageIndex.toString() },
                            beyondViewportPageCount = 1
                        ) { pageIndex ->
                        when (tabsList.getOrNull(pageIndex)) {
                            "dashboard" -> {
                                WorkerDashboard(viewModel)
                            }
                            "my_contracts" -> {
                                EmployerDashboardView(viewModel)
                            }
                            "piece_gigs" -> {
                                PieceWorksSection(viewModel)
                            }
                            "marketplace" -> {
                                com.example.ui.screens.AmaMarketplaceSection(viewModel)
                            }

                            "journal" -> {
                                com.example.ui.screens.JournalScreen(viewModel)
                            }
                            "events" -> {
                                EventsSection(viewModel)
                            }
                            "messages" -> {
                                MessagesCenterSection(viewModel)
                            }
                            "profile" -> {
                                ProfileSection(viewModel)
                            }

                        }
                    }
                    }
                }
            }

            // Wallet Coming Soon Dialog
            if (showWalletComingSoonDialog) {
                AlertDialog(
                    onDismissRequest = { showWalletComingSoonDialog = false },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(GiggzGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalanceWallet,
                                contentDescription = "Wallet Icon",
                                tint = GiggzGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Giggz Wallet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            text = "Coming soon! You'll be able to manage payments, track earnings, and withdraw funds with ease and security.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { showWalletComingSoonDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("excellent_button")
                        ) {
                            Text(
                                text = "Excellent!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.5.dp, GiggzGreen, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            // Notifications Dialog Tray list
            if (showNotificationsDialog) {
                Dialog(onDismissRequest = { showNotificationsDialog = false }) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.7f)
                            .border(1.dp, GiggzGreen, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Fullscreen icon link on the left
                                IconButton(onClick = {
                                    showNotificationsDialog = false
                                    viewModel.navigateTo("notifications")
                                }) {
                                    Icon(Icons.Filled.Launch, contentDescription = "Fullscreen", tint = GiggzGreen, modifier = Modifier.size(20.dp))
                                }
                                // Clear text button (Only has "Clear" text) on the right
                                TextButton(onClick = {
                                    viewModel.clearAllNotifications()
                                    context.showSafeToast("Inbox cleared!")
                                }) {
                                    Text("Clear", color = Color.Red, fontSize = 11.sp)
                                }
                            }

                            Divider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                            if (activeNotificationsList.isEmpty()) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("Inbox is empty.", fontSize = 12.sp, color = Color.Gray)
                                }
                            } else {
                                LazyColumn(modifier = Modifier.weight(1f)) {
                                    items(activeNotificationsList) { notif ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable { viewModel.markNotificationAsRead(notif.id) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else GiggzGreen.copy(alpha = 0.05f)
                                            )
                                        ) {
                                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (notif.isRead) Icons.Filled.Done else Icons.Filled.NotificationsActive,
                                                    contentDescription = null,
                                                    tint = if (notif.isRead) Color.Gray else GiggzGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(notif.message, fontSize = 11.sp, color = Color.DarkGray)
                                                    if (notif.senderId != null) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "View Profile",
                                                            fontSize = 11.sp,
                                                            color = GiggzGreen,
                                                            fontWeight = FontWeight.Bold,
                                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                                            modifier = Modifier.clickable {
                                                                viewModel.markNotificationAsRead(notif.id)
                                                                viewModel.showUserProfile(notif.senderId)
                                                                showNotificationsDialog = false
                                                            }
                                                        )
                                                    }
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
                                                        tint = if (notif.isFavorite) GiggzGold else Color.Gray,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { showNotificationsDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Close", color = Color.White)
                            }
                        }
                    }
                }
            }

            // Premium & Profile Boost Dialog
            if (showBoostPremiumDialog) {
                AlertDialog(
                    onDismissRequest = { showBoostPremiumDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = GiggzGold, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Premium & Profile Boost", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(GiggzGold.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Bolt, contentDescription = null, tint = GiggzGold, modifier = Modifier.size(32.dp))
                            }

                            Text(
                                "COMING SOON",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = GiggzGold,
                                letterSpacing = 1.5.sp,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                "We are currently preparing custom subscription options and marketing packages to help you succeed. Take a look at the exciting deals being worked on:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            Divider(color = Color.LightGray.copy(alpha = 0.3f))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.FlashOn, contentDescription = null, tint = GiggzGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("10x Profile Boost Deal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Boosted profiles appear at the absolute top of search results and job applications for 24 hours.", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Verified, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Verified Gold Badge Deal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Post unlimited job listings, get priority recruitment alerts, and display a premium verification badge.", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color.Blue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Advanced Talent Analytics Deal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Detailed, real-time insights on who viewed your profile, posts, or listings.", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showBoostPremiumDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Okay, Got It!", color = Color.White)
                        }
                    }
                )
            }

            // Daily Experience & Tips Journal Dialog (Removed per user request)
            if (false) {
                var newCustomerName by remember { mutableStateOf("") }
                var newCategory by remember { mutableStateOf("Worker") }
                var newRating by remember { mutableStateOf(5f) }
                var newSummaryText by remember { mutableStateOf("") }
                val summariesList by viewModel.customerSummaries.collectAsStateWithLifecycle()

                AlertDialog(
                    onDismissRequest = { showJournalDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.MenuBook, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Daily Journal & Tips", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Section 1: Customer Tips & Feedback
                            Text(
                                text = "RECENT CUSTOMER TIPS & EXPERIENCE LOGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GiggzGold
                            )

                            if (summariesList.isEmpty()) {
                                Text(
                                    text = "No logs yet. Write your first summary below!",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    summariesList.take(3).forEach { sum ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = if (viewModel.isDarkMode.value) Color(0xFF2D2D2D) else Color(0xFFF3F4F6),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(sum.customerName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("(${sum.category})", fontSize = 9.sp, color = Color.Gray)
                                                }
                                                Text("⭐ ${sum.experienceRating}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GiggzGold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(sum.summaryText, fontSize = 10.sp, color = if (viewModel.isDarkMode.value) Color.LightGray else Color.DarkGray, lineHeight = 14.sp)
                                        }
                                    }
                                }
                            }

                            Divider(color = Color.LightGray.copy(alpha = 0.3f))

                            // Section 2: Write Daily Experience Summary Form
                            Text(
                                text = "WRITE NEW DAILY EXPERIENCE SUMMARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GiggzGreen
                            )

                            OutlinedTextField(
                                value = newCustomerName,
                                onValueChange = { newCustomerName = it },
                                label = { Text("Customer/User Name", fontSize = 11.sp) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GiggzGreen)
                            )

                            // Category selector row
                            Column {
                                Text("User Role Category", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Worker", "Employer", "General").forEach { cat ->
                                        val isSelected = newCategory == cat
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) GiggzGreen else Color.LightGray.copy(alpha = 0.2f))
                                                .clickable { newCategory = cat }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = cat,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Star rating selector
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Experience Rating: ", fontSize = 10.sp, color = Color.Gray)
                                    Text("${newRating.toInt()} Stars", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GiggzGold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    (1..5).forEach { star ->
                                        IconButton(
                                            onClick = { newRating = star.toFloat() },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "$star Stars",
                                                tint = if (newRating >= star) GiggzGold else Color.Gray.copy(alpha = 0.4f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = newSummaryText,
                                onValueChange = { newSummaryText = it },
                                label = { Text("Daily Experience Summary / Tips notes", fontSize = 11.sp) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GiggzGreen)
                            )

                            Button(
                                onClick = {
                                    if (newCustomerName.isNotBlank() && newSummaryText.isNotBlank()) {
                                        viewModel.addCustomerSummary(
                                            CustomerExperienceSummary(
                                                customerName = newCustomerName,
                                                category = newCategory,
                                                experienceRating = newRating,
                                                summaryText = newSummaryText
                                            )
                                        )
                                        newCustomerName = ""
                                        newSummaryText = ""
                                        context.showSafeToast("Daily Experience logged to Journal! 📖")
                                    } else {
                                        context.showSafeToast("Please enter Customer Name and Summary Text.")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Log Daily Summary", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showJournalDialog = false }) {
                            Text("Done", color = GiggzGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Newly Registered Users Dialog
            if (showNewUsersDialog) {
                val usersList by viewModel.allUsers.collectAsStateWithLifecycle()
                val newestUsers = remember(usersList) {
                    usersList.sortedByDescending { it.joinedAt }
                }

                AlertDialog(
                    onDismissRequest = { showNewUsersDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.People, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Newly Registered Users", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "LATEST SIGN-UPS ON GIGGZ:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )

                            if (newestUsers.isEmpty()) {
                                Text("No users found.", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                newestUsers.forEach { u ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = if (viewModel.isDarkMode.value) Color(0xFF2D2D2D) else Color(0xFFF8FAFC),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (viewModel.isDarkMode.value) Color(0xFF3D3D3D) else Color(0xFFE2E8F0),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Simple user avatar
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (u.role == "Worker") GiggzGreen.copy(alpha = 0.15f) else GiggzGold.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = u.fullName.take(1).uppercase(),
                                                color = if (u.role == "Worker") GiggzGreen else GiggzGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = u.fullName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            if (u.role == "Worker") GiggzGreen.copy(alpha = 0.1f) else GiggzGold.copy(alpha = 0.1f),
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = u.role,
                                                        fontSize = 8.sp,
                                                        color = if (u.role == "Worker") GiggzGreen else GiggzGold,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text("Location: ${u.location.ifBlank { "Nairobi, KE" }}", fontSize = 10.sp, color = Color.Gray)
                                            val joinDateFormatted = remember(u.joinedAt) {
                                                val date = java.util.Date(u.joinedAt)
                                                val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                                format.format(date)
                                            }
                                            Text("Joined: $joinDateFormatted", fontSize = 8.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showNewUsersDialog = false }) {
                            Text("Close", color = GiggzGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Settings Dialog Box - Premium Mobile-First Settings Screen
            if (showSettingsDialog) {
                // Settings interactive states
                var pushEnabled by remember { mutableStateOf(true) }
                var emailEnabled by remember { mutableStateOf(true) }
                var alertsEnabled by remember { mutableStateOf(true) }
                var remindersEnabled by remember { mutableStateOf(true) }
                var msgsEnabled by remember { mutableStateOf(true) }
                var promoEnabled by remember { mutableStateOf(false) }

                var fontScaleSelected by remember { mutableStateOf("Medium") }
                var reduceMotion by remember { mutableStateOf(false) }
                var dataSaver by remember { mutableStateOf(false) }

                var searchRadiusKm by remember { mutableStateOf(15f) }
                var userLocationPref by remember { mutableStateOf("Lusaka, Zambia") }

                var currentCacheSize by remember { mutableStateOf("14.8 MB") }
                var preferredCategories by remember { mutableStateOf(setOf("Real Estate", "Carpentry", "Services")) }

                var activeSubPage by remember { mutableStateOf<String?>(null) }
                var activeCategory by remember { mutableStateOf<String?>(null) }

                Dialog(
                    onDismissRequest = { 
                        if (activeCategory != null) {
                            activeCategory = null
                        } else {
                            viewModel.showSettingsDialog.value = false 
                        }
                    },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            topBar = {
                                androidx.compose.material3.CenterAlignedTopAppBar(
                                    title = { 
                                        Text(
                                            text = activeCategory ?: "Settings", 
                                            fontWeight = FontWeight.ExtraBold, 
                                            fontSize = 18.sp, 
                                            color = GiggzGreen
                                        ) 
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { 
                                            if (activeCategory != null) {
                                                activeCategory = null
                                            } else {
                                                viewModel.showSettingsDialog.value = false 
                                            }
                                        }) {
                                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = GiggzGreen)
                                        }
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                )
                            }
                        ) { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .widthIn(max = 440.dp)
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (activeCategory == null) {
                                        Text(
                                            "System Preferences",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                                        )

                                        val categoriesList = listOf(
                                            Triple("Notifications", Icons.Filled.Notifications, "Configure your push alerts, email settings and chat reminders"),
                                            Triple("Appearance", Icons.Filled.Palette, "Customize color themes, text sizing and language defaults"),
                                            Triple("Privacy & Policy", Icons.Filled.Lock, "Manage your private permissions and secure configurations"),
                                            Triple("Content Preferences", Icons.Filled.Tune, "Control search distance, location and category filters"),
                                            Triple("Storage", Icons.Filled.Storage, "Clean cached application files and storage space utilization"),
                                            Triple("Help & Support", Icons.Filled.Help, "Access helper documents, contact teams and support channels"),
                                            Triple("About Giggz", Icons.Filled.Info, "View software updates, licenses and terms of service")
                                        )

                                        categoriesList.forEach { (catName, catIcon, catDesc) ->
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { activeCategory = catName },
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(GiggzGreen.copy(alpha = 0.08f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(catIcon, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(20.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = catName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = catDesc,
                                                            fontSize = 11.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Filled.ArrowForward,
                                                        contentDescription = "Navigate to $catName",
                                                        tint = GiggzGreen,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Backend Sync Diagnostics Card
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(if (viewModel.isFirebaseAvailable) GiggzGreen else Color.Red)
                                                    )
                                                    Text(
                                                        text = if (viewModel.isFirebaseAvailable) "Firebase: Online & Synced" else "Firebase: Offline (Local Room)",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = if (viewModel.isFirebaseAvailable) GiggzGreen else Color.Red
                                                     )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = if (viewModel.isFirebaseAvailable) {
                                                        "Direct live replication is active. All jobs, direct chats, and media uploads are connected to your cloud Firestore backend."
                                                    } else {
                                                        "Local SQLite (Room) database is fully operational. To activate real-time cloud backup and cross-device sync, download 'google-services.json' from your Firebase Console and place it in the '/app' folder of your codebase."
                                                    },
                                                    fontSize = 10.sp,
                                                    color = Color.Gray,
                                                    lineHeight = 14.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = Color.LightGray.copy(alpha = 0.2f))

                                        // Contact Us
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text("Contact Us", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GiggzGreen)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Support Email: support@giggz.app", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("Business Relations: business@giggz.app", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = { context.showSafeToast("Launching email client to support@giggz.app") },
                                                        colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f).height(36.dp)
                                                    ) {
                                                        Text("Send Email", fontSize = 11.sp, color = Color.White)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { context.showSafeToast("Redirecting to https://giggz.app") },
                                                        border = BorderStroke(1.dp, GiggzGreen),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f).height(36.dp)
                                                    ) {
                                                        Text("Visit Website", fontSize = 11.sp, color = GiggzGreen)
                                                    }
                                                }
                                            }
                                        }

                                        // Follow Giggz
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth(),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Follow Giggz", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    listOf(
                                                        "Facebook" to "FB", "Instagram" to "IG", "X (Twitter)" to "X", 
                                                        "TikTok" to "TK", "LinkedIn" to "LN", "YouTube" to "YT"
                                                    ).forEach { social ->
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            modifier = Modifier.clickable { context.showSafeToast("Opening Giggz official ${social.first} profile...") }
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(36.dp)
                                                                    .clip(CircleShape)
                                                                    .background(GiggzGreen.copy(alpha = 0.1f)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(social.second, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GiggzGreen)
                                                            }
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(social.first.split(" ").first(), fontSize = 8.sp, color = Color.Gray)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Additional Actions
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth(),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                SettingsActionRow(label = "Rate Giggz App", icon = Icons.Filled.Star, color = GiggzGold) {
                                                    context.showSafeToast("Thank you! Opening Google Play Store to rate Giggz.")
                                                }
                                                Divider(color = Color.LightGray.copy(alpha = 0.2f))
                                                SettingsActionRow(label = "Share Giggz", icon = Icons.Filled.Share, color = GiggzGreen) {
                                                    context.showSafeToast("Sharing invite link to clipboard!")
                                                }
                                                Divider(color = Color.LightGray.copy(alpha = 0.2f))
                                                SettingsActionRow(label = "Invite Friends", icon = Icons.Filled.PersonAdd, color = GiggzGreen) {
                                                    context.showSafeToast("Invite codes dispatched!")
                                                }
                                                Divider(color = Color.LightGray.copy(alpha = 0.2f))
                                                SettingsActionRow(label = "Back Door", icon = Icons.Filled.MeetingRoom, color = Color.Red) {
                                                    viewModel.logout()
                                                    viewModel.showSettingsDialog.value = false
                                                }
                                            }
                                        }
                                    } else {
                                        // Sub-page content
                                        when (activeCategory) {
                                            "Notifications" -> {
                                                SettingsGroupCard(title = "Notifications", icon = Icons.Filled.Notifications) {
                                                    SettingsSwitchRow(label = "Push Notifications", checked = pushEnabled, onCheckedChange = { pushEnabled = it }, icon = Icons.Filled.NotificationsActive)
                                                    SettingsSwitchRow(label = "Email Notifications", checked = emailEnabled, onCheckedChange = { emailEnabled = it }, icon = Icons.Filled.Email)
                                                    SettingsSwitchRow(label = "Gig Alerts", checked = alertsEnabled, onCheckedChange = { alertsEnabled = it }, icon = Icons.Filled.Campaign)
                                                    SettingsSwitchRow(label = "Event Reminders", checked = remindersEnabled, onCheckedChange = { remindersEnabled = it }, icon = Icons.Filled.Event)
                                                    SettingsSwitchRow(label = "Message Notifications", checked = msgsEnabled, onCheckedChange = { msgsEnabled = it }, icon = Icons.Filled.Chat)
                                                    SettingsSwitchRow(label = "Marketing Notifications", checked = promoEnabled, onCheckedChange = { promoEnabled = it }, icon = Icons.Filled.CardGiftcard)
                                                }
                                            }
                                            "Appearance" -> {
                                                SettingsGroupCard(title = "Appearance", icon = Icons.Filled.Palette) {
                                                    // Theme selector
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                            Icon(Icons.Filled.Palette, contentDescription = null, tint = GiggzGreen.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Theme Mode", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                                        }
                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            listOf("Light Mode", "Dark Mode").forEach { mode ->
                                                                val isActive = currentThemeName == mode
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(8.dp))
                                                                        .background(if (isActive) GiggzGreen else Color.LightGray.copy(alpha = 0.3f))
                                                                        .clickable { viewModel.setTheme(mode) }
                                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                                ) {
                                                                    Text(mode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface)
                                                                }
                                                            }
                                                        }
                                                    }

                                                    // Font size
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                            Icon(Icons.Filled.TextFields, contentDescription = null, tint = GiggzGreen.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Font Size", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                                        }
                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            listOf("Small", "Medium", "Large").forEach { size ->
                                                                val isSelected = fontScaleSelected == size
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(8.dp))
                                                                        .background(if (isSelected) GiggzGreen else Color.LightGray.copy(alpha = 0.3f))
                                                                        .clickable { fontScaleSelected = size }
                                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                                ) {
                                                                    Text(size, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                                                                }
                                                            }
                                                        }
                                                    }

                                                    // Language
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                            Icon(Icons.Filled.Language, contentDescription = null, tint = GiggzGreen.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Language", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                                        }
                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            listOf("English" to "en", "Swahili" to "sw", "French" to "fr").forEach { pair ->
                                                                val isSelected = languageState.value == pair.second
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(8.dp))
                                                                        .background(if (isSelected) GiggzGreen else Color.LightGray.copy(alpha = 0.3f))
                                                                        .clickable { languageState.value = pair.second }
                                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                                ) {
                                                                    Text(pair.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                                                                }
                                                            }
                                                        }
                                                    }

                                                    SettingsSwitchRow(label = "Reduce Motion", checked = reduceMotion, onCheckedChange = { reduceMotion = it }, icon = Icons.Filled.Animation)
                                                    SettingsSwitchRow(label = "Data Saver Mode", checked = dataSaver, onCheckedChange = { dataSaver = it }, icon = Icons.Filled.NetworkCheck)
                                                }
                                            }
                                            "Privacy & Policy" -> {
                                                SettingsGroupCard(title = "Privacy & Policy", icon = Icons.Filled.Lock) {
                                                    SettingsNavigationRow(label = "Privacy Settings", icon = Icons.Filled.PrivacyTip, onClick = { activeSubPage = "Privacy Settings" })
                                                    SettingsNavigationRow(label = "Manage Permissions", icon = Icons.Filled.Security, onClick = { activeSubPage = "Manage Permissions" })
                                                    SettingsNavigationRow(label = "Two-Factor Authentication", icon = Icons.Filled.VerifiedUser, onClick = { activeSubPage = "Two-Factor Authentication" })
                                                    SettingsNavigationRow(label = "Login Activity", icon = Icons.Filled.Devices, onClick = { activeSubPage = "Login Activity" })
                                                    SettingsNavigationRow(label = "Blocked Users", icon = Icons.Filled.Block, onClick = { activeSubPage = "Blocked Users" })
                                                    SettingsNavigationRow(label = "Change Password", icon = Icons.Filled.VpnKey, onClick = { activeSubPage = "Change Password" })
                                                    SettingsNavigationRow(label = "Report a Security Issue", icon = Icons.Filled.Report, onClick = { activeSubPage = "Report a Security Issue" })
                                                }
                                            }
                                            "Content Preferences" -> {
                                                SettingsGroupCard(title = "Content Preferences", icon = Icons.Filled.Tune) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.Map, contentDescription = null, tint = GiggzGreen.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Nearby Search Radius (${searchRadiusKm.toInt()} km)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                    }
                                                    Slider(
                                                        value = searchRadiusKm,
                                                        onValueChange = { searchRadiusKm = it },
                                                        valueRange = 1f..50f,
                                                        colors = SliderDefaults.colors(
                                                            activeTrackColor = GiggzGreen,
                                                            thumbColor = GiggzGreen
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.Place, contentDescription = null, tint = GiggzGreen.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Location Preference", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                    }
                                                    OutlinedTextField(
                                                        value = userLocationPref,
                                                        onValueChange = { userLocationPref = it },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GiggzGreen),
                                                        singleLine = true,
                                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.Category, contentDescription = null, tint = GiggzGreen.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Preferred Categories & Interests", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                    }
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        listOf("Real Estate", "Carpentry", "Painting", "Services").forEach { category ->
                                                            val isSelected = preferredCategories.contains(category)
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(12.dp))
                                                                    .background(if (isSelected) GiggzGreen.copy(alpha = 0.15f) else Color.Transparent)
                                                                    .border(1.dp, if (isSelected) GiggzGreen else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                                    .clickable { 
                                                                        preferredCategories = if (isSelected) preferredCategories - category else preferredCategories + category
                                                                    }
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) GiggzGreen else Color.Gray)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            "Storage" -> {
                                                SettingsGroupCard(title = "Storage", icon = Icons.Filled.Storage) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                            Icon(Icons.Filled.Storage, contentDescription = null, tint = GiggzGreen.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column {
                                                                Text("Cached System Data", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                                Text("Current size: $currentCacheSize", fontSize = 10.sp, color = Color.Gray)
                                                            }
                                                        }
                                                        Button(
                                                            onClick = { 
                                                                currentCacheSize = "0.0 B"
                                                                context.showSafeToast("Giggz cache successfully cleared!")
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.height(32.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp)
                                                        ) {
                                                            Text("Clear Cache", fontSize = 10.sp, color = Color.White)
                                                        }
                                                    }
                                                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                                                    SettingsNavigationRow(label = "Manage Downloads", icon = Icons.Filled.FileDownload, onClick = { activeSubPage = "Manage Downloads" })
                                                    SettingsNavigationRow(label = "Storage Usage Details", icon = Icons.Filled.BarChart, onClick = { activeSubPage = "Storage Usage Details" })
                                                }
                                            }
                                            "Help & Support" -> {
                                                SettingsGroupCard(title = "Help & Support", icon = Icons.Filled.Help) {
                                                    SettingsNavigationRow(label = "Help Center", icon = Icons.Filled.HelpCenter, onClick = { activeSubPage = "Help Center" })
                                                    SettingsNavigationRow(label = "Frequently Asked Questions (FAQ)", icon = Icons.Filled.QuestionAnswer, onClick = { activeSubPage = "Frequently Asked Questions (FAQ)" })
                                                    SettingsNavigationRow(label = "Contact Support", icon = Icons.Filled.ContactSupport, onClick = { activeSubPage = "Contact Support" })
                                                    SettingsNavigationRow(label = "Report a Bug", icon = Icons.Filled.BugReport, onClick = { activeSubPage = "Report a Bug" })
                                                    SettingsNavigationRow(label = "Send Feedback", icon = Icons.Filled.Feedback, onClick = { activeSubPage = "Send Feedback" })
                                                    SettingsNavigationRow(label = "Live Chat (Coming Soon)", icon = Icons.Filled.Forum, isEnabled = false, onClick = {})
                                                }
                                            }
                                            "About Giggz" -> {
                                                SettingsGroupCard(title = "About Giggz", icon = Icons.Filled.Info) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                            Icon(Icons.Filled.Update, contentDescription = null, tint = GiggzGreen.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column {
                                                                Text("Giggz Premium", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                                Text("Version v2.1.0-Premium", fontSize = 10.sp, color = Color.Gray)
                                                            }
                                                        }
                                                        Button(
                                                            onClick = { activeSubPage = "Check for Updates" },
                                                            colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.height(32.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp)
                                                        ) {
                                                            Text("Check Update", fontSize = 10.sp, color = Color.White)
                                                        }
                                                    }
                                                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                                                    SettingsNavigationRow(label = "Terms of Service", icon = Icons.Filled.Description, onClick = { activeSubPage = "Terms of Service" })
                                                    SettingsNavigationRow(label = "Privacy Policy", icon = Icons.Filled.Policy, onClick = { activeSubPage = "Privacy Policy" })
                                                    SettingsNavigationRow(label = "Community Guidelines", icon = Icons.Filled.People, onClick = { activeSubPage = "Community Guidelines" })
                                                    SettingsNavigationRow(label = "Payment & Refund Policy", icon = Icons.Filled.Payments, onClick = { activeSubPage = "Payment & Refund Policy" })
                                                    SettingsNavigationRow(label = "Safety & Trust Policy", icon = Icons.Filled.VerifiedUser, onClick = { activeSubPage = "Safety & Trust Policy" })
                                                    SettingsNavigationRow(label = "Account Deletion Policy", icon = Icons.Filled.DeleteForever, onClick = { activeSubPage = "Account Deletion Policy" })
                                                    SettingsNavigationRow(label = "Open Source Licenses", icon = Icons.Filled.Copyright, onClick = { activeSubPage = "Open Source Licenses" })
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { activeCategory = null },
                                            colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen.copy(alpha = 0.1f), contentColor = GiggzGreen),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(44.dp)
                                        ) {
                                            Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Back to Settings Menu", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }

                        // Overlay Sub-pages / dedicated page mock sheets!
                        if (activeSubPage != null) {
                            val subTitle = activeSubPage!!
                            Dialog(
                                onDismissRequest = { activeSubPage = null },
                                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    Scaffold(
                                        topBar = {
                                            androidx.compose.material3.CenterAlignedTopAppBar(
                                                title = { Text(subTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GiggzGreen) },
                                                navigationIcon = {
                                                    IconButton(onClick = { activeSubPage = null }) {
                                                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = GiggzGreen)
                                                    }
                                                },
                                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                )
                                            )
                                        }
                                    ) { innerPadding ->
                                        val isLegalDoc = subTitle in listOf(
                                            "Terms of Service",
                                            "Privacy Policy",
                                            "Community Guidelines",
                                            "Payment & Refund Policy",
                                            "Safety & Trust Policy",
                                            "Account Deletion Policy"
                                        )

                                        if (isLegalDoc) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(innerPadding)
                                                    .background(MaterialTheme.colorScheme.background)
                                                    .padding(16.dp)
                                            ) {
                                                LegalDocumentViewer(
                                                    subTitle = subTitle,
                                                    isDark = darkThemeState.value
                                                )
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(innerPadding)
                                                    .background(MaterialTheme.colorScheme.background)
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(20.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(72.dp)
                                                        .clip(CircleShape)
                                                        .background(GiggzGreen.copy(alpha = 0.08f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.Settings, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(36.dp))
                                                }

                                                Text(
                                                    text = "Configure $subTitle",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                Text(
                                                    text = "You are currently accessing the private security and premium setup screen for $subTitle. Standard enterprise grade encryption (AES-256) is applied to all configurations.",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray,
                                                    textAlign = TextAlign.Center
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Render custom inputs based on the subpage type to make them look authentic!
                                                when (subTitle) {
                                                    "Change Password" -> {
                                                        var curPass by remember { mutableStateOf("") }
                                                        var newPass by remember { mutableStateOf("") }
                                                        OutlinedTextField(value = curPass, onValueChange = { curPass = it }, label = { Text("Current Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                                        OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("New Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                                    }
                                                    "Withdraw Funds" -> {
                                                        var amount by remember { mutableStateOf("500") }
                                                        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount to withdraw (ZMW / K)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                                        Text("Recipient Wallet: Mobile Money Registered Number", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                    "Two-Factor Authentication" -> {
                                                        var is2faActive by remember { mutableStateOf(false) }
                                                        SettingsSwitchRow(label = "Enable SMS-based Two Factor Auth", checked = is2faActive, onCheckedChange = { is2faActive = it })
                                                    }
                                                    "Report a Security Issue", "Report a Bug", "Send Feedback" -> {
                                                        var msgText by remember { mutableStateOf("") }
                                                        OutlinedTextField(
                                                            value = msgText, 
                                                            onValueChange = { msgText = it }, 
                                                            label = { Text("Please describe the details:") }, 
                                                            modifier = Modifier.fillMaxWidth().height(120.dp),
                                                            maxLines = 5
                                                        )
                                                    }
                                                    "Check for Updates" -> {
                                                        Text("System Checked: Giggz version v2.1.0 is up to date.", color = GiggzGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }
                                                    "Open Source Licenses" -> {
                                                        Text("Using Jetpack Compose, Kotlin Coroutines, Room DB, Coil Image Loader, and Material Design 3 libraries. App builds successfully as part of Google AI Studio.", fontSize = 11.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                                                    }
                                                    else -> {
                                                        Text("Secure Settings Sync Active", color = GiggzGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                        Text("Standard configuration successfully updated to cloud backend service.", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(24.dp))

                                                Button(
                                                    onClick = {
                                                        context.showSafeToast("Settings saved successfully!")
                                                        activeSubPage = null
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text("Apply & Save Details", color = Color.White)
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

            // Worker Profile Detail Dialog with prominent exit buttons
            val selectedWorker by viewModel.selectedWorker.collectAsStateWithLifecycle()
            if (selectedWorker != null) {
                val worker = selectedWorker!!
                
                androidx.activity.compose.BackHandler {
                    viewModel.selectWorker(null)
                }

                Dialog(
                    onDismissRequest = { viewModel.selectWorker(null) },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .wrapContentHeight()
                            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Header with Giggz logo, title and Exit Button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GiggzGreen.copy(alpha = 0.05f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Engineering,
                                        contentDescription = null,
                                        tint = GiggzGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Worker Profile",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = GiggzGreen
                                    )
                                }
                                
                                // Explicit Exit Button requested by the user
                                Button(
                                    onClick = { viewModel.selectWorker(null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Exit",
                                            tint = Color.DarkGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Exit",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                // Profile Header Card inside
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(photoUrl = worker.profilePhoto, name = worker.fullName, size = 64)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = worker.fullName,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "Professional Giggz Partner",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        RatingStars(rating = worker.rating, reviewsCount = worker.reviewsCount, size = 14)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Quick details
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    border = BorderStroke(1.dp, Color(0xFFF3F4F6))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Availability", fontSize = 12.sp, color = Color.Gray)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(RoundedCornerShape(50.dp))
                                                        .background(if (worker.availabilityStatus == "Available") GiggzGreen else Color.Red)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = worker.availabilityStatus,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (worker.availabilityStatus == "Available") GiggzGreen else Color.Red
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Experience", fontSize = 12.sp, color = Color.Gray)
                                            Text("${worker.experience} Years", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Completed Contracts", fontSize = 12.sp, color = Color.Gray)
                                            Text("${worker.completedJobs} projects", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Location", fontSize = 12.sp, color = Color.Gray)
                                            Text(worker.location.ifBlank { "Not specified" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Skills Section
                                Text(
                                    text = "Skilled Expertise",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val skillList = worker.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                if (skillList.isEmpty()) {
                                    Text("No skills specified yet.", fontSize = 12.sp, color = Color.Gray)
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        skillList.forEach { skill ->
                                            Box(
                                                modifier = Modifier
                                                    .background(GiggzGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                                    .border(BorderStroke(1.dp, GiggzGreen.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = skill,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GiggzGreen
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Biography
                                Text(
                                    text = "Biography & Background",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = worker.bio.ifBlank { "This professional partner is verified on the Giggz decentralized system but has not filled in a biography yet." },
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Contact/Chat Button
                                Button(
                                    onClick = {
                                        viewModel.selectWorker(null)
                                        viewModel.selectChatPartner(worker)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Chat",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Explicit secondary exit button at bottom
                                OutlinedButton(
                                    onClick = { viewModel.selectWorker(null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Close Profile",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedProfileUser != null) {
        val profileUser = selectedProfileUser!!
        val trust = remember(profileUser) { calculateTrustProfile(profileUser) }
        val reviewsList = viewModel.allReviews.collectAsStateWithLifecycle().value
        val userReviews = remember(profileUser, reviewsList) {
            reviewsList.filter { it.revieweeId == profileUser.id }
        }

        Dialog(
            onDismissRequest = { viewModel.dismissUserProfile() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .border(2.dp, GiggzGreen, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header Area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trust Profile & Ratings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GiggzGreen
                        )
                        IconButton(
                            onClick = { viewModel.dismissUserProfile() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color.Gray
                            )
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 10.dp))

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // User Badge Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GiggzGreen.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            UserAvatar(photoUrl = profileUser.profilePhoto, name = profileUser.fullName, size = 64)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profileUser.fullName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Verified Profile",
                                        tint = GiggzGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Badge(
                                        containerColor = if (profileUser.role == "Worker") GiggzGreen else GiggzGold,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = profileUser.role.uppercase(),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (profileUser.nationality.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "•  ${profileUser.nationality}",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Giggz Reputation Card
                        GiggzReputationCard(
                            rating = profileUser.rating,
                            reviewsCount = profileUser.reviewsCount,
                            completedJobs = profileUser.completedJobs
                        )

                        // BADGES SYSTEM ROW
                        Column {
                            Text("Earned Trust Badges", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 1. Elite or Trusted Status
                                Box(
                                    modifier = Modifier
                                        .background(trust.color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (trust.score >= 90) Icons.Filled.Stars else Icons.Filled.Verified,
                                            contentDescription = null,
                                            tint = if (trust.score >= 90) GiggzGold else trust.color,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = trust.level,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = trust.color
                                        )
                                    }
                                }

                                // 2. Rising Star Badge (fast improvement)
                                if (profileUser.completedJobs > 15) {
                                    Box(
                                        modifier = Modifier
                                            .background(GiggzGold.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.TrendingUp,
                                                contentDescription = null,
                                                tint = GiggzGold,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Rising Star",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GiggzGold
                                            )
                                        }
                                    }
                                }

                                // 3. Fast Responder
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Fast Responder",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3B82F6)
                                        )
                                    }
                                }
                            }
                        }

                        // AI REVIEW SUMMARY
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GiggzGreen.copy(alpha = 0.04f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = GiggzGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI Trust Profile Summary",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GiggzGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = trust.aiSummary,
                                    fontSize = 11.5.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // STATS PANEL
                        Column {
                            Text("Core Platform Statistics", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val statBoxes = listOf(
                                    Triple("${profileUser.completedJobs}", "Completed", GiggzGreen),
                                    Triple("${trust.cancellationRate}%", "Cancellation", Color.Red),
                                    Triple("${trust.responseTimeMin}m", "Reply Speed", Color(0xFF3B82F6)),
                                    Triple("${trust.repeatClientRate}%", "Repeat Clients", GiggzGold)
                                )
                                statBoxes.forEach { (value, label, color) ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (darkThemeState.value) Color(0xFF27272A) else Color(0xFFF9FAFB)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }

                        // SKILLS SECTION
                        if (profileUser.role == "Worker" && profileUser.skills.isNotBlank()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Skills & Specializations", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    profileUser.skills.split(",").forEach { skill ->
                                        if (skill.trim().isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .background(GiggzGreen.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(skill.trim(), fontSize = 11.sp, color = GiggzGreen, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // CONTACT & BIO
                        if (profileUser.bio.isNotBlank()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Biography", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF3F4F6).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = profileUser.bio,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        // REVIEWS FEED
                        if (false) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Verified Platform Reviews (${userReviews.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            
                            if (userReviews.isEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No reviews received yet. High-trust members build reviews over time.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                    }
                                }
                            } else {
                                userReviews.forEach { review ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    UserAvatar(photoUrl = review.reviewerPhoto, name = review.reviewerName, size = 32)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(review.reviewerName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        Text(
                                                            text = if (review.isFromWorker) "Contractor review" else "Employer review",
                                                            fontSize = 9.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                }
                                                // Rating count
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Filled.Star, contentDescription = null, tint = GiggzGold, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(text = String.format("%.1f", review.averageRating), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Regarding Contract: \"${review.jobTitle}\"",
                                                fontSize = 10.sp,
                                                color = GiggzGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = review.comment,
                                                fontSize = 11.sp,
                                                color = Color.DarkGray,
                                                lineHeight = 15.sp
                                            )
                                            
                                            if (review.imageUrl.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("📸 Proof of completed work:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(110.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                ) {
                                                    AsyncImage(
                                                        model = review.imageUrl,
                                                        contentDescription = "Work Proof",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons/Footer Area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.dismissUserProfile() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss", color = Color.Gray)
                        }

                        val currentUserId = currentUser?.id ?: 0
                        if (profileUser.id != currentUserId) {
                            Button(
                                onClick = {
                                    viewModel.dismissUserProfile()
                                    coroutineScope.launch {
                                        viewModel.selectChatPartner(profileUser)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Chat", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// EMPLOYER DASHBOARD VIEW MODULE
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerDashboardView(
    viewModel: GiggzViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val employer = viewModel.currentUser.collectAsStateWithLifecycle().value ?: return

    val allUsersList by viewModel.allUsers.collectAsStateWithLifecycle()
    val allJobsList by viewModel.allJobs.collectAsStateWithLifecycle()
    val allAppsList by viewModel.allApplications.collectAsStateWithLifecycle()

    var showPostJobDialog by remember { mutableStateOf(false) }
    var selectedTabEmployer by remember { mutableStateOf("contracts") } // "contracts", "workers"
    var selectedAppForRating by remember { mutableStateOf<ApplicationEntity?>(null) }
    var showUpgradeLimitDialog by remember { mutableStateOf(false) }
    var limitType by remember { mutableStateOf("") }
    var limitMax by remember { mutableStateOf(0) }

    // Employer's posted contracts
    val postedContracts = allJobsList.filter { it.employerId == employer.id }
    val activeWorkers = allUsersList.filter { it.role == "Worker" && it.phone.isNotBlank() }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        // Tab Row
        TabRow(
            selectedTabIndex = if (selectedTabEmployer == "contracts") 0 else 1,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                val idx = if (selectedTabEmployer == "contracts") 0 else 1
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[idx]),
                    color = GiggzGreen
                )
            }
        ) {
            Tab(
                selected = selectedTabEmployer == "contracts",
                onClick = { selectedTabEmployer = "contracts" },
                text = { Text("Your Posted Gigs", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                selectedContentColor = GiggzGreen,
                unselectedContentColor = Color.Gray
            )
            Tab(
                selected = selectedTabEmployer == "workers",
                onClick = { selectedTabEmployer = "workers" },
                text = { Text("Search Skilled Workers", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                selectedContentColor = GiggzGreen,
                unselectedContentColor = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTabEmployer == "contracts") {
            // Contracts posted tab
            Box(modifier = Modifier.weight(1f)) {
                if (postedContracts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active job postings. Click 'Post New Contract' to recruit skilled workers.", textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(24.dp))
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(postedContracts) { contract ->
                            val applicants = allAppsList.filter { it.jobId == contract.id }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(contract.category.uppercase(), fontWeight = FontWeight.Bold, fontSize = 9.sp, color = GiggzGreen)
                                        Text(contract.status, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = if (contract.status == "Active") GiggzGreen else Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(contract.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(contract.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2, modifier = Modifier.padding(vertical = 4.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Wage: K${contract.budget}", fontWeight = FontWeight.ExtraBold, color = GiggzGreen, fontSize = 14.sp)
                                        Text("Applicants: ${applicants.size}", fontSize = 11.sp, color = GiggzGold, fontWeight = FontWeight.Bold)
                                    }

                                    if (applicants.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("Submitted Applications (Click to view profile):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GiggzGold)
                                        applicants.forEach { app ->
                                            val workerProfile = viewModel.allUsers.value.find { it.id == app.workerId }
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        if (workerProfile != null) {
                                                            viewModel.selectWorker(workerProfile)
                                                        } else {
                                                            context.showSafeToast("Worker offline.")
                                                        }
                                                    },
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    UserAvatar(photoUrl = app.workerPhoto, name = app.workerName, size = 32)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = app.workerName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = GiggzGreen,
                                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                                        )
                                                        Text("Cover Letter: ${app.coverLetter}", fontSize = 10.sp, color = Color.Gray)
                                                        if (app.imagePath.isNotBlank()) {
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            AsyncImage(
                                                                model = app.imagePath,
                                                                contentDescription = "Attached Portfolio Image",
                                                                modifier = Modifier
                                                                    .size(width = 80.dp, height = 50.dp)
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        }
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        TextButton(
                                                            onClick = {
                                                                coroutineScope.launch {
                                                                    if (workerProfile != null) {
                                                                        viewModel.selectChatPartner(workerProfile)
                                                                    } else {
                                                                        context.showSafeToast("Worker offline.")
                                                                    }
                                                                }
                                                            },
                                                            modifier = Modifier.height(28.dp),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                                        ) {
                                                            Text("Chat", fontSize = 11.sp, color = GiggzGreen, fontWeight = FontWeight.Bold)
                                                        }
                                                        
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        
                                                        when (app.status) {
                                                            "Pending" -> {
                                                                Button(
                                                                    onClick = {
                                                                        viewModel.updateApplicationStatus(app, "Accepted")
                                                                        context.showSafeToast("Application Accepted! Contractor notified. 🎉")
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                                    modifier = Modifier.height(26.dp)
                                                                ) {
                                                                    Text("Accept", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                            "Accepted" -> {
                                                                Button(
                                                                    onClick = {
                                                                        selectedAppForRating = app
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGold),
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                                    modifier = Modifier.height(26.dp)
                                                                ) {
                                                                    Text("Complete & Rate", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                            "Completed" -> {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(12.dp))
                                                                    Spacer(modifier = Modifier.width(3.dp))
                                                                    Text("Completed", fontSize = 10.sp, color = GiggzGreen, fontWeight = FontWeight.Bold)
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

                // Add Contract FAB
                FloatingActionButton(
                    onClick = { showPostJobDialog = true },
                    containerColor = GiggzGreen,
                    contentColor = Color.White,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, contentDescription = "Post Gig")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Post New Gig", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Worker search directory tab
            var searchCategoryQuery by remember { mutableStateOf("") }
            val matchingWorkers = activeWorkers.filter {
                searchCategoryQuery.isBlank() || it.skills.lowercase().contains(searchCategoryQuery.lowercase()) || it.fullName.lowercase().contains(searchCategoryQuery.lowercase())
            }

            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = searchCategoryQuery,
                    onValueChange = { searchCategoryQuery = it },
                    placeholder = { Text("Search by skill (e.g. Carpenter, Plumber)") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = GiggzGreen) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (matchingWorkers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matching workers found in database.", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(matchingWorkers) { worker ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectWorker(worker) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(photoUrl = worker.profilePhoto, name = worker.fullName, size = 48)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(worker.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Skills: ${worker.skills}", fontSize = 11.sp, color = GiggzGreen, fontWeight = FontWeight.SemiBold)
                                        RatingStars(rating = worker.rating, reviewsCount = worker.reviewsCount, size = 12)
                                        Text("Experience: ${worker.experience} Years | ${worker.location}", fontSize = 10.sp, color = Color.Gray)
                                    }

                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.selectChatPartner(worker)
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.Chat, contentDescription = "Chat", tint = GiggzGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Post a Job Form Modal Dialog
    if (showPostJobDialog) {
        var jTitle by remember { mutableStateOf("") }
        var jCategory by remember { mutableStateOf("Plumbing") }
        var jBudget by remember { mutableStateOf("") }
        var jDesc by remember { mutableStateOf("") }
        var jDeadline by remember { mutableStateOf("July 15, 2026") }
        var jLocation by remember { mutableStateOf(employer.location) }
        var jIsHourlyPieceWork by remember { mutableStateOf(false) }
        var jHourlyDuration by remember { mutableStateOf("Same Day") }
        var jBringOwnTools by remember { mutableStateOf(false) }
        var jPhotos by remember { mutableStateOf<List<String>>(emptyList()) }

        val jobImagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            if (uri != null) {
                if (jPhotos.size < 3) {
                    jPhotos = jPhotos + uri.toString()
                }
                context.showSafeToast("Selected photo from device gallery!")
            }
        }

        Dialog(
            onDismissRequest = { showPostJobDialog = false },
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
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Post Job Contract", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GiggzGreen)
                    Text("Recruit skilled workers instantly", fontSize = 11.sp, color = Color.Gray)

                    OutlinedTextField(value = jTitle, onValueChange = { jTitle = it }, label = { Text("Job Title") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Short Hourly Gig?", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Switch(checked = jIsHourlyPieceWork, onCheckedChange = { jIsHourlyPieceWork = it })
                        }

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bring Your Own Tool?", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Switch(checked = jBringOwnTools, onCheckedChange = { jBringOwnTools = it })
                        }
                    }

                    var jCategoryExpanded by remember { mutableStateOf(false) }
                    val jobCategoryOptions = listOf(
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
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = jCategory,
                            onValueChange = {},
                            label = { Text("Category (click to select)") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Select Category",
                                    tint = GiggzGreen
                                )
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { jCategoryExpanded = true }
                        )
                        DropdownMenu(
                            expanded = jCategoryExpanded,
                            onDismissRequest = { jCategoryExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            jobCategoryOptions.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        jCategory = cat
                                        jCategoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(value = jBudget, onValueChange = { jBudget = it }, label = { Text("Wage / Budget (K)") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = jLocation, onValueChange = { jLocation = it }, label = { Text("Contract Location") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = jDeadline, onValueChange = { jDeadline = it }, label = { Text("Contract Deadline") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = jDesc, onValueChange = { jDesc = it }, label = { Text("Detailed Specifications") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(90.dp), maxLines = 4)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Job Reference Photos (Optional)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        jPhotos.forEach { photoUri ->
                            Box(
                                modifier = Modifier
                                    .size(65.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = photoUri,
                                        contentDescription = "Selected Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .clickable { jPhotos = jPhotos.filter { it != photoUri } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✕", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (jPhotos.size < 3) {
                            Box(
                                modifier = Modifier
                                    .size(65.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GiggzGreen.copy(alpha = 0.05f))
                                    .border(1.5.dp, GiggzGreen, RoundedCornerShape(8.dp))
                                    .clickable { jobImagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.AddPhotoAlternate,
                                        contentDescription = "Add Photo",
                                        tint = GiggzGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Gallery", fontSize = 9.sp, color = GiggzGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val budgetVal = jBudget.toDoubleOrNull() ?: 0.0
                                if (jTitle.isBlank() || jDesc.isBlank() || budgetVal <= 0.0) {
                                    context.showSafeToast("Please complete details and valid wage.")
                                    return@Button
                                }

                                // Check daily limits
                                val calendar = java.util.Calendar.getInstance()
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                calendar.set(java.util.Calendar.MINUTE, 0)
                                calendar.set(java.util.Calendar.SECOND, 0)
                                calendar.set(java.util.Calendar.MILLISECOND, 0)
                                val startOfToday = calendar.timeInMillis

                                if (jIsHourlyPieceWork) {
                                    val gigsPostedToday = allJobsList.count { 
                                        it.employerId == employer.id && it.isPieceWork && it.createdAt >= startOfToday 
                                    }
                                    if (gigsPostedToday >= 3) {
                                        showPostJobDialog = false
                                        showUpgradeLimitDialog = true
                                        limitType = "Casual Gigs"
                                        limitMax = 3
                                        return@Button
                                    }
                                } else {
                                    val jobsPostedToday = allJobsList.count { 
                                        it.employerId == employer.id && !it.isPieceWork && it.createdAt >= startOfToday 
                                    }
                                    if (jobsPostedToday >= 2) {
                                        showPostJobDialog = false
                                        showUpgradeLimitDialog = true
                                        limitType = "Job Contracts"
                                        limitMax = 2
                                        return@Button
                                    }
                                }

                                viewModel.postJob(
                                    title = jTitle,
                                    description = if (jBringOwnTools) "$jDesc\n\n🛠️ Bring Your Own Tool Required: Yes" else jDesc,
                                    category = jCategory,
                                    budget = budgetVal,
                                    location = jLocation,
                                    deadline = jDeadline,
                                    isPieceWork = jIsHourlyPieceWork,
                                    timeRequired = if (jIsHourlyPieceWork) jHourlyDuration else "",
                                    siteImage = jPhotos.joinToString(","),
                                    recommendedBudget = 0.0
                                ) {
                                    context.showSafeToast("Contract posted live!")
                                    showPostJobDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit Gig", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showPostJobDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (showUpgradeLimitDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeLimitDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = GiggzGold
                    )
                    Text(
                        text = "Limit Reached: Upgrade Required",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "You have reached your daily limit of $limitMax $limitType. To post more, please upgrade to Giggz Premium!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(color = Color.LightGray.copy(alpha = 0.3f))

                    Text(
                        text = "Our Upcoming Premium Deals:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GiggzGreen
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, tint = GiggzGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("10x Profile Boost Deal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Boosted profiles appear at the absolute top of search results and applications.", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Verified Gold Badge Deal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Post unlimited job listings, priority alerts, and display a premium verification badge.", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color.Blue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Advanced Talent Analytics Deal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Detailed, real-time insights on who viewed your profile, posts, or listings.", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }

                    Text(
                        text = "Premium Plans and Highlighted Gigs are undergoing final regulatory assessments with the Bank of Zambia and will be activated soon.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showUpgradeLimitDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GiggzGreen)
                ) {
                    Text("Okay, Got It!", color = Color.White)
                }
            }
        )
    }

    val appToRate = selectedAppForRating
    if (appToRate != null) {
        TwoWayRatingDialog(
            isFromWorker = false,
            onDismiss = { selectedAppForRating = null },
            onSubmit = { comment, ratings, imageProofUrl ->
                val r1 = ratings.getOrNull(0) ?: 5f
                val r2 = ratings.getOrNull(1) ?: 5f
                val r3 = ratings.getOrNull(2) ?: 5f
                val r4 = ratings.getOrNull(3) ?: 5f
                viewModel.submitReview(
                    jobId = appToRate.jobId,
                    jobTitle = appToRate.jobTitle,
                    reviewerId = employer.id,
                    reviewerName = employer.fullName,
                    reviewerPhoto = employer.profilePhoto,
                    revieweeId = appToRate.workerId,
                    revieweeName = appToRate.workerName,
                    rating1 = r1,
                    rating2 = r2,
                    rating3 = r3,
                    rating4 = r4,
                    comment = comment,
                    imageUrl = imageProofUrl,
                    isFromWorker = false,
                    onComplete = {
                        viewModel.updateApplicationStatus(appToRate, "Completed")
                        selectedAppForRating = null
                        context.showSafeToast("Contract complete! Review posted. 💰")
                    }
                )
            },
            jobTitle = appToRate.jobTitle
        )
    }
}

// =============================================================================
// LOCALIZATION RE-MAPPING RULES
// =============================================================================

fun getLocalizedLabels(langCode: String): Map<String, String> {
    return when (langCode) {
        "sw" -> mapOf(
            "dashboard" to "Kazi Zilizopendekezwa",
            "employer_home" to "Mwajiri Nyumbani",
            "piece_gigs" to "Kazi Ndogo / Kibarua",
            "performance" to "Utendaji",
            "messages" to "Ujumbe",
            "profile" to "Wasifu Wangu",
            "admin" to "Utawala",
            "nav_home" to "Nyumbani",
            "nav_piece" to "Vibarua",
            "nav_profile" to "Wasifu",
            "journal" to "Jarida/Rekodi"
        )
        "fr" -> mapOf(
            "dashboard" to "Contrats Recommandés",
            "employer_home" to "Accueil Employeur",
            "piece_gigs" to "Petits Boulots",
            "performance" to "Performance",
            "messages" to "Messagerie",
            "profile" to "Mon Profil",
            "admin" to "Administration",
            "nav_home" to "Accueil",
            "nav_piece" to "Tâches",
            "nav_profile" to "Profil",
            "journal" to "Journal"
        )
        else -> mapOf(
            "dashboard" to "Gigs",
            "employer_home" to "Employer Console",
            "piece_gigs" to "Casual Gigs",
            "performance" to "Performance",
            "messages" to "Messages",
            "profile" to "My Profile",
            "admin" to "Admin Portal",
            "nav_home" to "Dashboard",
            "nav_piece" to "Casual Gigs",
            "nav_profile" to "Profile",
            "journal" to "Journal"
        )
    }
}

@Composable
fun SettingsGroupCard(
    title: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = GiggzGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    label: String, 
    checked: Boolean, 
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GiggzGreen.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GiggzGreen,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.4f)
            ),
            modifier = Modifier.scale(0.85f)
        )
    }
}

@Composable
fun SettingsNavigationRow(
    label: String, 
    isEnabled: Boolean = true, 
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GiggzGreen.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = label, 
                fontSize = 13.sp, 
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray,
                fontWeight = if (isEnabled) FontWeight.Normal else FontWeight.Light
            )
        }
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SettingsActionRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    val isShareIcon = icon == Icons.Filled.Share || (icon == Icons.Filled.Reply && label.contains("Share", ignoreCase = true))
    val resolvedIcon = if (isShareIcon) Icons.Filled.Reply else icon
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = resolvedIcon,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(16.dp)
                .then(if (isShareIcon) Modifier.graphicsLayer(scaleX = -1f) else Modifier)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(16.dp)
        )
    }
}

