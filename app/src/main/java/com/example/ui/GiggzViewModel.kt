package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class GiggzViewModel(private val repository: GiggzRepository, private val context: android.content.Context) : ViewModel() {

    private val prefs = context.getSharedPreferences("giggz_prefs", android.content.Context.MODE_PRIVATE)
    private var savedEmailOnLaunch: String? = prefs.getString("logged_in_email", null)
    val lastRole = MutableStateFlow<String?>(prefs.getString("last_role", null))

    private val _isPartnerTyping = MutableStateFlow(false)
    val isPartnerTyping: StateFlow<Boolean> = _isPartnerTyping.asStateFlow()

    // =============================================================================
    // UI STATES
    // =============================================================================

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Screen navigation state (Simple, robust route string approach)
    private val _currentScreen = MutableStateFlow("animated_splash") // "animated_splash", "splash", "login", "register", "register_worker_profile", "main"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    var hasPlayedSplash = false

    // Bottom Navigation Bar Tab for Main Screen
    private val _activeTab = MutableStateFlow("dashboard") // "dashboard", "piece_works", "marketplace", "messages", "profile", "admin"
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    // Dashboard Sub-Tabs
    val workerSubTab = MutableStateFlow("recommended") // "recommended", "applied", "saved"
    val employerSubTab = MutableStateFlow("discover") // "discover", "post_job", "my_jobs"
    val adminSubTab = MutableStateFlow("stats") // "stats", "moderation", "announcements"

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val marketplaceSearchQuery = MutableStateFlow("")
    val filterCategory = MutableStateFlow("All")
    val filterLocation = MutableStateFlow("")
    val filterMinPay = MutableStateFlow("")
    val filterTimeRequired = MutableStateFlow("All") // For Piece Works

    // Selected items for detail screens
    private val _selectedWorker = MutableStateFlow<UserEntity?>(null)
    val selectedWorker: StateFlow<UserEntity?> = _selectedWorker.asStateFlow()

    private val _selectedJob = MutableStateFlow<JobEntity?>(null)
    val selectedJob: StateFlow<JobEntity?> = _selectedJob.asStateFlow()

    private val _selectedListing = MutableStateFlow<ListingEntity?>(null)
    val selectedListing: StateFlow<ListingEntity?> = _selectedListing.asStateFlow()

    private val _selectedProfileUser = MutableStateFlow<UserEntity?>(null)
    val selectedProfileUser: StateFlow<UserEntity?> = _selectedProfileUser.asStateFlow()

    fun showUserProfile(userId: Int) {
        viewModelScope.launch {
            val u = allUsers.value.find { it.id == userId }
            _selectedProfileUser.value = u
        }
    }

    fun showUserProfileByName(name: String) {
        viewModelScope.launch {
            val u = allUsers.value.find { it.fullName == name }
            _selectedProfileUser.value = u
        }
    }

    fun dismissUserProfile() {
        _selectedProfileUser.value = null
    }

    // Chat Conversation partner
    private val _chatPartner = MutableStateFlow<UserEntity?>(null)
    val chatPartner: StateFlow<UserEntity?> = _chatPartner.asStateFlow()

    // Database Flows
    val allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allWorkers = repository.allWorkers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allJobs = repository.allJobs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPieceWorks = repository.allPieceWorks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allApplications = repository.allApplications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allListings = repository.allListings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAnnouncements = repository.allAnnouncements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allReports = repository.allReports.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allEvents = repository.allEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allReviews = repository.allReviews.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDailyJobsHistory = repository.allDailyJobsHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _jobsDoneTodayCount = MutableStateFlow(5)
    val jobsDoneTodayCount = _jobsDoneTodayCount.asStateFlow()

    private val _cycleStartTime = MutableStateFlow(System.currentTimeMillis())
    val cycleStartTime = _cycleStartTime.asStateFlow()

    // Saved/Favourited items (Local memory list of IDs)
    private val _savedJobIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedJobIds: StateFlow<Set<Int>> = _savedJobIds.asStateFlow()

    private val _favouriteWorkerIds = MutableStateFlow<Set<Int>>(emptySet())
    val favouriteWorkerIds: StateFlow<Set<Int>> = _favouriteWorkerIds.asStateFlow()

    private val _savedListingIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedListingIds: StateFlow<Set<Int>> = _savedListingIds.asStateFlow()

    // AI Job Recommendations Output
    private val _aiRecommendationText = MutableStateFlow("")
    val aiRecommendationText: StateFlow<String> = _aiRecommendationText.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // Chat Message Flow for Active Chat Room
    val activeChatMessages: StateFlow<List<MessageEntity>> = combine(_currentUser, _chatPartner) { user, partner ->
        if (user != null && partner != null) {
            repository.getChatMessages(user.id, partner.id)
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications for Logged In User
    val notifications: StateFlow<List<NotificationEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getNotifications(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeNotifications: StateFlow<List<NotificationEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getActiveNotifications(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userJournals: StateFlow<List<JournalEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getJournals(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getUnreadNotificationsCount(user.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadMessagesCount: StateFlow<Int> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getUnreadMessagesCount(user.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allMessages: StateFlow<List<MessageEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getAllMessagesFlowForUser(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active chat partners
    val chatPartners: StateFlow<List<UserEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getChatPartners(user.id).map { ids ->
                ids.mapNotNull { repository.getUserById(it) }
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Language / Theme helper states
    val currentLanguage = MutableStateFlow("English") // "English", "Swahili", "French"
    val currentTheme = MutableStateFlow("Light Mode") // Default to Light Mode as requested
    val showSettingsDialog = MutableStateFlow(false)
    val chatWallpaper = MutableStateFlow("Dot Grid") // "Dot Grid", "Lined Grid", "Abstract Waves", "Minimalist Clean"
    val isDarkMode: StateFlow<Boolean> = currentTheme.map { it == "Dark Mode" || it == "Forest Mint" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setTheme(themeName: String) {
        currentTheme.value = themeName
    }

    val isFirebaseAvailable: Boolean
        get() = repository.isFirebaseAvailable()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshData(pageName: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.cycleMockData()
            delay(800)
            _isRefreshing.value = false
            onComplete()
        }
    }

    // =============================================================================
    // INITIALIZATION & MOCK LOADING
    // =============================================================================

    init {
        viewModelScope.launch {
            android.util.Log.d("GiggzViewModel", "Checking Firebase availability...")
            if (isFirebaseAvailable) {
                android.util.Log.d("GiggzViewModel", "Firebase is INITIALIZED and AVAILABLE. Ready for production backend sync!")
                try {
                    repository.firebaseService.observeAuthState().collect { firebaseUid ->
                        android.util.Log.d("GiggzViewModel", "Firebase Auth State Updated: UID=$firebaseUid")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("GiggzViewModel", "Failed to collect Firebase auth state: ${e.message}")
                }
            } else {
                android.util.Log.d("GiggzViewModel", "Firebase is NOT available (missing google-services.json). Running in SQLite Room local fallback mode.")
            }
        }
        viewModelScope.launch {
            repository.prepopulateMockData()
            // Clean up old notifications (older than 3 days) automatically to save database storage
            val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
            repository.deleteNotificationsOlderThan(threeDaysAgo)

            try {
                val currentHist = repository.allDailyJobsHistory.first()
                if (currentHist.isEmpty()) {
                    val now = System.currentTimeMillis()
                    val dayMs = 24 * 60 * 60 * 1000L
                    repository.insertDailyJobsHistory(DailyJobsHistoryEntity(dateString = "Mon", jobsCount = 4, timestamp = now - 6 * dayMs))
                    repository.insertDailyJobsHistory(DailyJobsHistoryEntity(dateString = "Tue", jobsCount = 7, timestamp = now - 5 * dayMs))
                    repository.insertDailyJobsHistory(DailyJobsHistoryEntity(dateString = "Wed", jobsCount = 3, timestamp = now - 4 * dayMs))
                    repository.insertDailyJobsHistory(DailyJobsHistoryEntity(dateString = "Thu", jobsCount = 9, timestamp = now - 3 * dayMs))
                    repository.insertDailyJobsHistory(DailyJobsHistoryEntity(dateString = "Fri", jobsCount = 5, timestamp = now - 2 * dayMs))
                    repository.insertDailyJobsHistory(DailyJobsHistoryEntity(dateString = "Sat", jobsCount = 12, timestamp = now - 1 * dayMs))
                }
            } catch (e: Exception) {
                // Safeguard against empty / uninitialized tables
            }

            // Periodic 24-hour cycle reset checker loop
            while (true) {
                try {
                    checkAndReset24HourCycle()
                } catch (e: Exception) {
                    // Avoid crashes during hot reload or db transitions
                }
                delay(30000) // check every 30 seconds
            }
        }
    }

    // =============================================================================
    // AUTHENTICATION LOGIC
    // =============================================================================

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user == null) {
                onResult(false, "No account registered with this email.")
            } else if (!user.isApproved) {
                onResult(false, "This account has been suspended by the platform administrator.")
            } else if (user.password != password) {
                onResult(false, "Incorrect password. Please try again.")
            } else {
                prefs.edit().putString("logged_in_email", user.email).apply()
                prefs.edit().putString("last_role", user.role).apply()
                savedEmailOnLaunch = user.email
                lastRole.value = user.role
                _currentUser.value = user
                _currentScreen.value = "main"
                // Default tab based on role
                _activeTab.value = when (user.role) {
                    "Admin" -> "admin"
                    "Employer" -> "my_contracts"
                    else -> "dashboard"
                }
                onResult(true, "Successfully logged in as ${user.fullName}.")
            }
        }
    }

    fun loginWithGoogle(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            // Mock successful Google Login using the primary worker
            val googleMockUser = repository.getUserByEmail("john.carpenter@giggz.com")
            if (googleMockUser != null) {
                prefs.edit().putString("logged_in_email", googleMockUser.email).apply()
                prefs.edit().putString("last_role", googleMockUser.role).apply()
                savedEmailOnLaunch = googleMockUser.email
                lastRole.value = googleMockUser.role
                _currentUser.value = googleMockUser
                _currentScreen.value = "main"
                _activeTab.value = if (googleMockUser.role == "Employer") "my_contracts" else "dashboard"
                onResult(true, "Successfully authenticated via Google as ${googleMockUser.fullName}.")
            } else {
                onResult(false, "Google Sign-In failed.")
            }
        }
    }

    fun register(email: String, password: String, fullName: String, role: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                onResult(false, "An account with this email already exists.")
                return@launch
            }

            val newUser = UserEntity(
                email = email,
                password = password,
                role = role,
                fullName = fullName,
                profilePhoto = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150" // Placeholder
            )

            val insertedId = repository.insertUser(newUser).toInt()
            val createdUser = repository.getUserById(insertedId)

            if (createdUser != null) {
                prefs.edit().putString("logged_in_email", createdUser.email).apply()
                prefs.edit().putString("last_role", createdUser.role).apply()
                savedEmailOnLaunch = createdUser.email
                lastRole.value = createdUser.role
                _currentUser.value = createdUser
            }

            if (role == "Worker") {
                // Workers go to additional profile wizard
                _currentScreen.value = "register_worker_profile"
            } else {
                _currentScreen.value = "main"
                _activeTab.value = if (role == "Employer") "my_contracts" else "dashboard"
            }
            onResult(true, "Registration successful!")
        }
    }

    fun completeWorkerProfile(
        nationality: String,
        location: String,
        phone: String,
        skills: String,
        experience: Int,
        bio: String,
        profilePhoto: String = "",
        onComplete: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(
                nationality = nationality,
                location = location,
                phone = phone,
                skills = skills,
                experience = experience,
                bio = bio,
                profilePhoto = if (profilePhoto.isNotBlank()) profilePhoto else user.profilePhoto,
                availabilityStatus = "Available"
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            _currentScreen.value = "main"
            _activeTab.value = if (updatedUser.role == "Employer") "my_contracts" else "dashboard"
            onComplete()
        }
    }

    fun logout() {
        prefs.edit().remove("logged_in_email").apply()
        savedEmailOnLaunch = null
        _currentUser.value = null
        _currentScreen.value = "splash"
        _activeTab.value = "dashboard"
        _chatPartner.value = null
        _selectedWorker.value = null
        _selectedJob.value = null
        _selectedListing.value = null
        _selectedProfileUser.value = null
        showSettingsDialog.value = false
    }

    // =============================================================================
    // PROFILE MANAGEMENT
    // =============================================================================

    fun updateProfile(
        fullName: String,
        phone: String,
        location: String,
        skills: String,
        experience: Int,
        bio: String,
        availability: String,
        profilePhoto: String? = null
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(
                fullName = fullName,
                phone = phone,
                location = location,
                skills = skills,
                experience = experience,
                bio = bio,
                availabilityStatus = availability,
                profilePhoto = profilePhoto ?: user.profilePhoto
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun rateTrader(traderId: Int, rating: Int, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val trader = repository.getUserById(traderId)
                if (trader != null) {
                    val newCount = trader.reviewsCount + 1
                    val newRating = ((trader.rating * trader.reviewsCount) + rating) / newCount
                    val formattedRating = String.format("%.1f", newRating).toFloat()
                    val updatedTrader = trader.copy(
                        rating = formattedRating,
                        reviewsCount = newCount
                    )
                    repository.updateUser(updatedTrader)
                    onComplete(true, "Successfully rated ${trader.fullName} with $rating stars!")
                } else {
                    onComplete(false, "Trader not found.")
                }
            } catch (e: Exception) {
                onComplete(false, "Failed to submit rating: ${e.message}")
            }
        }
    }

    // =============================================================================
    // JOB & PIECE WORK BOARD LOGIC
    // =============================================================================

    fun postJob(
        title: String,
        description: String,
        budget: Double,
        category: String,
        deadline: String,
        location: String,
        isPieceWork: Boolean = false,
        timeRequired: String = "",
        payType: String = "Flat",
        siteImage: String = "",
        recommendedBudget: Double = 0.0,
        onComplete: () -> Unit
    ) {
        val employer = _currentUser.value ?: return
        viewModelScope.launch {
            val newJob = JobEntity(
                title = title,
                description = description,
                budget = budget,
                category = category,
                deadline = deadline,
                location = location,
                employerId = employer.id,
                employerName = employer.fullName,
                employerPhoto = employer.profilePhoto,
                status = "Active",
                isPieceWork = isPieceWork,
                timeRequired = timeRequired,
                payType = payType,
                images = siteImage,
                recommendedBudget = recommendedBudget
            )
            repository.insertJob(newJob)

            // Notify relevant workers of new job category matches!
            val workersList = allWorkers.value
            for (worker in workersList) {
                if (worker.skills.lowercase().contains(category.lowercase()) ||
                    category.lowercase().contains(worker.skills.lowercase())) {
                    repository.insertNotification(
                        NotificationEntity(
                            userId = worker.id,
                            senderId = employer.id,
                            title = "New Job Matching Your Skills!",
                            message = "${employer.fullName} posted a job in '$category': '$title'. Budget: $$budget",
                            category = "job"
                        )
                    )
                }
            }
            onComplete()
        }
    }

    fun applyForJob(
        job: JobEntity,
        coverLetter: String,
        pdfPath: String = "",
        imagePath: String = "",
        onComplete: () -> Unit
    ) {
        val worker = _currentUser.value ?: return
        viewModelScope.launch {
            val app = ApplicationEntity(
                jobId = job.id,
                jobTitle = job.title,
                employerId = job.employerId,
                workerId = worker.id,
                workerName = worker.fullName,
                workerPhoto = worker.profilePhoto,
                coverLetter = coverLetter,
                pdfPath = pdfPath,
                imagePath = imagePath,
                status = "Pending"
            )
            repository.insertApplication(app)

            // Notify Employer
            repository.insertNotification(
                NotificationEntity(
                    userId = job.employerId,
                    senderId = worker.id,
                    title = "New Job Application!",
                    message = "${worker.fullName} applied for your job: '${job.title}'",
                    category = "job"
                )
            )
            onComplete()
        }
    }

    fun updateApplicationStatus(application: ApplicationEntity, newStatus: String) {
        viewModelScope.launch {
            val updatedApp = application.copy(status = newStatus)
            repository.updateApplication(updatedApp)

            // If accepted/completed, trigger relevant notifications and job status changes
            if (newStatus == "Accepted") {
                repository.updateJobStatus(application.jobId, "Accepted")
                repository.insertNotification(
                    NotificationEntity(
                        userId = application.workerId,
                        senderId = application.employerId,
                        title = "Application Accepted! 🎉",
                        message = "Congratulations! Your application for '${application.jobTitle}' has been accepted.",
                        category = "job"
                    )
                )
            } else if (newStatus == "Completed") {
                repository.updateJobStatus(application.jobId, "Completed")
                val job = allJobs.value.find { it.id == application.jobId }
                if (job != null) {
                    addDeletedPostHistory(
                        DeletedPostHistory(
                            title = job.title,
                            type = "Job Contract",
                            description = job.description,
                            originalId = job.id
                        )
                    )
                }
                repository.deleteJob(application.jobId)

                // Delete the chat messages between the worker and employer (Done Deal)
                repository.deleteChatMessages(application.workerId, application.employerId)
                if (_chatPartner.value?.id == application.workerId || _chatPartner.value?.id == application.employerId) {
                    _chatPartner.value = null
                }

                // Update worker's rating/jobs count
                val worker = repository.getUserById(application.workerId)
                if (worker != null) {
                    val updatedWorker = worker.copy(
                        completedJobs = worker.completedJobs + 1,
                        reviewsCount = worker.reviewsCount + 1,
                        rating = ((worker.rating * worker.completedJobs) + 5f) / (worker.completedJobs + 1) // Give a solid 5 star mock review
                    )
                    repository.updateUser(updatedWorker)
                }

                repository.insertNotification(
                    NotificationEntity(
                        userId = application.workerId,
                        senderId = application.employerId,
                        title = "Contract Completed! 💰",
                        message = "Employer marked '${application.jobTitle}' as successfully completed.",
                        category = "job"
                    )
                )
                incrementCompletedJobsToday()
            } else if (newStatus == "Cancelled") {
                repository.insertNotification(
                    NotificationEntity(
                        userId = application.workerId,
                        senderId = application.employerId,
                        title = "Application Update",
                        message = "Your application for '${application.jobTitle}' has been cancelled.",
                        category = "job"
                    )
                )
            }
        }
    }

    fun saveJob(jobId: Int) {
        val current = _savedJobIds.value
        if (current.contains(jobId)) {
            _savedJobIds.value = current - jobId
        } else {
            _savedJobIds.value = current + jobId
        }
    }

    fun saveListing(listingId: Int) {
        val current = _savedListingIds.value
        if (current.contains(listingId)) {
            _savedListingIds.value = current - listingId
        } else {
            _savedListingIds.value = current + listingId
        }
    }

    fun favouriteWorker(workerId: Int) {
        val current = _favouriteWorkerIds.value
        if (current.contains(workerId)) {
            _favouriteWorkerIds.value = current - workerId
        } else {
            _favouriteWorkerIds.value = current + workerId
        }
    }

    // =============================================================================
    // IN-APP REAL-TIME MESSAGING CENTER
    // =============================================================================

    fun selectChatPartner(partner: UserEntity?) {
        _chatPartner.value = partner
        if (partner != null) {
            _activeTab.value = "messages" // Switch to chat tab

            // Mark messages from this sender as read
            val user = _currentUser.value ?: return
            viewModelScope.launch {
                repository.markMessagesAsRead(senderId = partner.id, receiverId = user.id)
            }
        }
    }

    private fun getIntelligentMockReply(userMsg: String, userName: String, partnerRole: String): String {
        val msg = userMsg.lowercase()
        return when {
            msg.contains("hello") || msg.contains("hi") || msg.contains("hey") -> {
                "Hi $userName! Hope you're doing great. How can I help you with the contract or Giggz tasks today?"
            }
            msg.contains("budget") || msg.contains("pay") || msg.contains("rate") || msg.contains("wage") || msg.contains("money") -> {
                "That budget sounds fair and aligned with the Giggz rates. Let's make sure the scope is clear so we can proceed!"
            }
            msg.contains("time") || msg.contains("when") || msg.contains("schedule") || msg.contains("tomorrow") || msg.contains("today") -> {
                "Tomorrow works perfectly for me. I will prepare my tools and be ready at the scheduled time."
            }
            msg.contains("where") || msg.contains("location") || msg.contains("address") || msg.contains("meet") -> {
                "I've shared my live location pin! Let's meet there. It should be convenient for both of us."
            }
            msg.contains("contract") || msg.contains("job") || msg.contains("deal") || msg.contains("gigs") -> {
                "I am fully committed to delivering high-quality work for this contract. Let's seal the deal!"
            }
            msg.contains("tool") || msg.contains("equipment") || msg.contains("bring") -> {
                "No worries at all! I have my full professional toolset ready for this task. I'll make sure everything is sorted."
            }
            msg.contains("thank") || msg.contains("thanks") || msg.contains("great") || msg.contains("perfect") -> {
                "Awesome! You're very welcome. Let's keep in touch. Feel free to send over any specifications!"
            }
            else -> {
                "Thanks for the update! I am checking this now and will write back to you shortly."
            }
        }
    }

    fun sendMessage(
        text: String, 
        mediaPath: String = "", 
        mediaType: String = "text",
        replyToId: Int? = null,
        replyToText: String? = null
    ) {
        val user = _currentUser.value ?: return
        val partner = _chatPartner.value ?: return
        if (text.isBlank() && mediaPath.isBlank()) return

        viewModelScope.launch {
            val msg = MessageEntity(
                senderId = user.id,
                senderName = user.fullName,
                receiverId = partner.id,
                messageText = text,
                mediaPath = mediaPath,
                mediaType = mediaType,
                isRead = false,
                replyToId = replyToId,
                replyToText = replyToText,
                deliveryStatus = "Sent"
            )
            val msgId = repository.insertMessage(msg)

            // Send notification to recipient
            repository.insertNotification(
                NotificationEntity(
                    userId = partner.id,
                    senderId = user.id,
                    title = "New Message from ${user.fullName}",
                    message = if (mediaType == "image") "📷 Sent you an image." 
                              else if (mediaType == "pdf") "📄 Sent you a document." 
                              else if (mediaType == "location") "📍 Shared a location."
                              else if (mediaType == "voice") "🎙️ Sent a voice message."
                              else text,
                    category = "message"
                )
            )

            // Intelligent typing indicator and premium reply simulation
            launch {
                delay(800)
                // Simulate "Delivered" state
                // Simulate "Read" state
                delay(1200)
                _isPartnerTyping.value = true
                delay(2000)
                _isPartnerTyping.value = false

                val replyText = getIntelligentMockReply(text, user.fullName, partner.role)
                val replyMsg = MessageEntity(
                    senderId = partner.id,
                    senderName = partner.fullName,
                    receiverId = user.id,
                    messageText = replyText,
                    isRead = false,
                    deliveryStatus = "Read"
                )
                repository.insertMessage(replyMsg)

                repository.insertNotification(
                    NotificationEntity(
                        userId = user.id,
                        senderId = partner.id,
                        title = "${partner.fullName}",
                        message = replyText,
                        category = "message"
                    )
                )
            }
        }
    }

    fun addMessageReaction(messageId: Int, emoji: String) {
        viewModelScope.launch {
            repository.updateMessageReactions(messageId, emoji)
        }
    }

    fun deleteMessage(messageId: Int) {
        viewModelScope.launch {
            repository.deleteMessageForEveryone(messageId)
        }
    }

    fun clearChat(partnerId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteChatMessages(user.id, partnerId)
        }
    }

    fun reportUser(userId: Int, reason: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.insertReport(
                ReportEntity(
                    reporterId = user.id,
                    reporterName = user.fullName,
                    reportedId = userId,
                    reportedType = "User",
                    subject = "In-App Chat Report",
                    description = reason
                )
            )
            onComplete()
        }
    }

    fun blockUser(userId: Int, onComplete: () -> Unit = {}) {
        val context = _currentUser.value ?: return
        // For premium block, we can simulate blocking by removing them from current chat partner
        if (_chatPartner.value?.id == userId) {
            _chatPartner.value = null
        }
        onComplete()
    }

    // =============================================================================
    // AMA SAMPLE LOCAL MARKETPLACE
    // =============================================================================

    fun postListing(
        title: String,
        description: String,
        price: Double,
        category: String,
        imageUrls: String = "",
        isOfferService: Boolean = false,
        condition: String = "",
        onComplete: () -> Unit
    ) {
        val seller = _currentUser.value ?: return
        viewModelScope.launch {
            val item = ListingEntity(
                title = title,
                description = description,
                price = price,
                category = category,
                imageUrls = imageUrls,
                sellerId = seller.id,
                sellerName = seller.fullName,
                sellerRole = seller.role,
                sellerPhoto = seller.profilePhoto,
                status = "Available",
                isOfferService = isOfferService,
                condition = condition
            )
            repository.insertListing(item)
            onComplete()
        }
    }

    fun markListingAsSold(listingId: Int) {
        viewModelScope.launch {
            val listings = repository.allListings.first()
            val listing = listings.find { it.id == listingId }
            if (listing != null) {
                repository.insertListing(listing.copy(status = "Sold"))
            }
        }
    }

    fun deleteListing(listingId: Int) {
        viewModelScope.launch {
            val listing = allListings.value.find { it.id == listingId }
            if (listing != null) {
                addDeletedPostHistory(
                    DeletedPostHistory(
                        title = listing.title,
                        type = "Marketplace Listing",
                        description = listing.description,
                        originalId = listing.id
                    )
                )
            }
            repository.deleteListing(listingId)
        }
    }

    fun expressInterestInListing(listingId: Int, initialMessage: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val listings = repository.allListings.first()
            val listing = listings.find { it.id == listingId }
            if (listing != null) {
                // Parse existing interested user IDs
                val idList = listing.interestedUserIds.split(",")
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
                    .toMutableList()
                
                val myIdStr = user.id.toString()
                if (!idList.contains(myIdStr)) {
                    idList.add(myIdStr)
                }
                
                // Save updated listing
                val updatedListing = listing.copy(interestedUserIds = idList.joinToString(","))
                repository.insertListing(updatedListing)
                
                // Also, send the initial message to the seller
                val msg = MessageEntity(
                    senderId = user.id,
                    senderName = user.fullName,
                    receiverId = listing.sellerId,
                    messageText = initialMessage,
                    isRead = false
                )
                repository.insertMessage(msg)
                
                // Send notification to the seller
                repository.insertNotification(
                    NotificationEntity(
                        userId = listing.sellerId,
                        senderId = user.id,
                        title = "Interest in: ${listing.title}",
                        message = "👋 ${user.fullName} is interested in your listing and says: \"$initialMessage\"",
                        category = "message"
                    )
                )
            }
        }
    }

    fun raiseReport(reportedType: String, reportedId: Int, subject: String, description: String, onComplete: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val rep = ReportEntity(
                reporterId = user.id,
                reporterName = user.fullName,
                subject = subject,
                description = description,
                reportedType = reportedType,
                reportedId = reportedId
            )
            repository.insertReport(rep)
            onComplete()
        }
    }

    // =============================================================================
    // EVENT AREA SYSTEM CONTROLS
    // =============================================================================

    fun promoteEvent(
        title: String,
        description: String,
        date: String,
        location: String,
        imageUrl: String,
        category: String = "Community",
        isFeatured: Boolean = false,
        isTrending: Boolean = false,
        imageGallery: String = "",
        videoUrl: String = "",
        socialLinks: String = "",
        onComplete: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val event = EventEntity(
                title = title,
                description = description,
                date = date,
                location = location,
                promoterId = user.id,
                promoterName = user.fullName,
                promoterPhoto = user.profilePhoto,
                imageUrl = if (imageUrl.isNotBlank()) imageUrl else "https://images.unsplash.com/photo-1511578314322-379afb476865?w=800&auto=format&fit=crop",
                savedByUserIds = "",
                category = category,
                isFeatured = isFeatured,
                isTrending = isTrending,
                isVerified = user.rating >= 4.5f,
                goingUserIds = "",
                interestedUserIds = "",
                imageGallery = imageGallery,
                videoUrl = videoUrl,
                socialLinks = socialLinks
            )
            repository.insertEvent(event)
            onComplete()
        }
    }

    fun toggleSaveEvent(eventId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val event = allEvents.value.find { it.id == eventId }
            if (event != null) {
                val userList = event.savedByUserIds.split(",")
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
                    .toMutableList()
                
                val userIdStr = user.id.toString()
                if (userList.contains(userIdStr)) {
                    userList.remove(userIdStr)
                } else {
                    userList.add(userIdStr)
                }
                
                val updatedEvent = event.copy(savedByUserIds = userList.joinToString(","))
                repository.updateEvent(updatedEvent)
            }
        }
    }

    fun toggleGoingEvent(eventId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val event = allEvents.value.find { it.id == eventId }
            if (event != null) {
                val userList = event.goingUserIds.split(",")
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
                    .toMutableList()
                
                val userIdStr = user.id.toString()
                if (userList.contains(userIdStr)) {
                    userList.remove(userIdStr)
                } else {
                    userList.add(userIdStr)
                }
                
                val updatedEvent = event.copy(goingUserIds = userList.joinToString(","))
                repository.updateEvent(updatedEvent)
            }
        }
    }

    fun toggleInterestedEvent(eventId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val event = allEvents.value.find { it.id == eventId }
            if (event != null) {
                val userList = event.interestedUserIds.split(",")
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
                    .toMutableList()
                
                val userIdStr = user.id.toString()
                if (userList.contains(userIdStr)) {
                    userList.remove(userIdStr)
                } else {
                    userList.add(userIdStr)
                }
                
                val updatedEvent = event.copy(interestedUserIds = userList.joinToString(","))
                repository.updateEvent(updatedEvent)
            }
        }
    }

    // =============================================================================
    // ADMINISTRATOR SYSTEM CONTROLS
    // =============================================================================

    fun sendAnnouncement(title: String, content: String, announceType: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            // Save announcement
            val announce = AnnouncementEntity(
                title = title,
                content = content,
                type = announceType
            )
            repository.insertAnnouncement(announce)

            // Send notification to ALL registered users on the system
            val usersList = allUsers.value
            for (u in usersList) {
                repository.insertNotification(
                    NotificationEntity(
                        userId = u.id,
                        title = "📢 Admin announcement: $title",
                        message = content,
                        category = "admin"
                    )
                )
            }
            onComplete()
        }
    }

    fun approveOrSuspendUser(userId: Int, approve: Boolean) {
        viewModelScope.launch {
            repository.updateUserApproval(userId, approve)
            // Send warning/update notification
            val alert = if (approve) "Your Giggz profile has been fully approved by the Moderator." else "Your Giggz profile has been suspended due to policy violations."
            repository.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = "System Security Notification",
                    message = alert,
                    category = "admin"
                )
            )
        }
    }

    fun resolveReport(reportId: Int) {
        viewModelScope.launch {
            repository.updateReportStatus(reportId, "Resolved")
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            repository.deleteUser(id)
        }
    }

    fun deleteJob(id: Int) {
        viewModelScope.launch {
            repository.deleteJob(id)
        }
    }

    fun updateJobStatus(jobId: Int, status: String) {
        viewModelScope.launch {
            repository.updateJobStatus(jobId, status)
        }
    }

    fun insertJob(job: JobEntity) {
        viewModelScope.launch {
            repository.insertJob(job)
        }
    }

    fun insertNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            repository.insertNotification(notification)
        }
    }

    fun insertJournal(title: String, content: String) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.insertJournal(
                JournalEntity(
                    userId = userId,
                    title = title,
                    content = content
                )
            )
        }
    }

    fun deleteJournal(id: Int) {
        viewModelScope.launch {
            repository.deleteJournal(id)
        }
    }

    fun updateJournal(id: Int, title: String, content: String) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.insertJournal(
                JournalEntity(
                    id = id,
                    userId = userId,
                    title = title,
                    content = content
                )
            )
        }
    }

    // =============================================================================
    // AI JOB MATCHMAKER VIA GEMINI REST
    // =============================================================================

    fun loadAiRecommendations() {
        val worker = _currentUser.value ?: return
        if (worker.role != "Worker") return

        _aiLoading.value = true
        _aiRecommendationText.value = "Analyzing available market listings and aligning with your profile..."

        viewModelScope.launch {
            val activeJobs = allJobs.value.filter { it.status == "Active" }
            val recommendations = GeminiService.getJobMatchRecommendations(
                workerName = worker.fullName,
                workerSkills = worker.skills,
                workerBio = worker.bio,
                jobsList = activeJobs
            )
            _aiRecommendationText.value = recommendations
            _aiLoading.value = false
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                repository.archiveAllNotifications(user.id)
            }
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun deleteNotifications(ids: List<Int>) {
        viewModelScope.launch {
            ids.forEach { id ->
                repository.deleteNotification(id)
            }
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun toggleNotificationFavorite(id: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateNotificationFavorite(id, isFavorite)
        }
    }

    // =============================================================================
    // SCREEN & TAB SHIFTER NAVIGATION
    // =============================================================================

    fun navigateTo(screen: String) {
        if (screen == "login" && savedEmailOnLaunch != null) {
            viewModelScope.launch {
                val user = repository.getUserByEmail(savedEmailOnLaunch!!)
                if (user != null && user.isApproved) {
                    _currentUser.value = user
                    _currentScreen.value = "main"
                    _activeTab.value = if (user.role == "Employer") "my_contracts" else "dashboard"
                } else {
                    _currentScreen.value = "login"
                }
            }
        } else {
            _currentScreen.value = screen
        }
    }

    fun setTab(tab: String) {
        _activeTab.value = tab
    }

    fun selectWorker(worker: UserEntity?) {
        _selectedWorker.value = worker
    }

    fun selectJob(job: JobEntity) {
        _selectedJob.value = job
    }

    fun selectListing(listing: ListingEntity) {
        _selectedListing.value = listing
    }

    fun markNotificationsAsRead() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(user.id)
        }
    }

    fun submitReview(
        jobId: Int,
        jobTitle: String,
        reviewerId: Int,
        reviewerName: String,
        reviewerPhoto: String,
        revieweeId: Int,
        revieweeName: String,
        rating1: Float,
        rating2: Float,
        rating3: Float,
        rating4: Float,
        comment: String,
        imageUrl: String,
        isFromWorker: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val averageRating = (rating1 + rating2 + rating3 + rating4) / 4f
            val review = ReviewEntity(
                jobId = jobId,
                jobTitle = jobTitle,
                reviewerId = reviewerId,
                reviewerName = reviewerName,
                reviewerPhoto = reviewerPhoto,
                revieweeId = revieweeId,
                revieweeName = revieweeName,
                rating1 = rating1,
                rating2 = rating2,
                rating3 = rating3,
                rating4 = rating4,
                averageRating = averageRating,
                comment = comment,
                imageUrl = imageUrl,
                isFromWorker = isFromWorker,
                timestamp = System.currentTimeMillis()
            )
            repository.insertReview(review)

            // Update target user average rating and reviewsCount
            val targetUser = repository.getUserById(revieweeId)
            if (targetUser != null) {
                val updatedReviewsCount = targetUser.reviewsCount + 1
                val newRating = ((targetUser.rating * targetUser.reviewsCount) + averageRating) / updatedReviewsCount
                
                // Keep the completed count updated
                val completedCount = if (targetUser.role == "Worker" && !isFromWorker) {
                    targetUser.completedJobs + 1
                } else {
                    targetUser.completedJobs
                }

                repository.updateUser(targetUser.copy(
                    rating = newRating,
                    reviewsCount = updatedReviewsCount,
                    completedJobs = completedCount
                ))
            }

            // Insert notification for the user who got rated
            repository.insertNotification(
                NotificationEntity(
                    userId = revieweeId,
                    senderId = reviewerId,
                    title = "New Review Received! ⭐",
                    message = "$reviewerName rated your performance. Average rating: ${String.format("%.2f", averageRating)}/5.00",
                    category = "job"
                )
            )

            onComplete()
        }
    }

    // Giggz Recruit Premium State
    private val _recruiterWalletBalance = MutableStateFlow(450.0) // Simulated $450 starting balance
    val recruiterWalletBalance = _recruiterWalletBalance.asStateFlow()

    private val _isRecruiterSubscribed = MutableStateFlow(false)
    val isRecruiterSubscribed = _isRecruiterSubscribed.asStateFlow()

    private val _recruitmentCampaigns = MutableStateFlow<List<RecruitCampaign>>(
        listOf(
            RecruitCampaign(
                id = "camp-1",
                organisationName = "Vertex Solutions",
                recruitmentTitle = "Urgent: 15 Event Stewards Needed",
                description = "We are seeking 15 energetic students or freelance stewards to assist with the upcoming Tech Summit. Duties include greeting guests, checking tickets, and managing seating layouts.",
                peopleNeeded = 15,
                jobType = "Event / Temporary",
                paymentRange = "$25 / Hour",
                requirements = "Strong communication, friendly posture, smart dressing. Availability on July 18-20.",
                deadline = "July 15, 2026",
                location = "Lusaka Conference Center",
                logoPreset = "EventPro",
                applicantsCount = 28,
                shortlistedCount = 8,
                hiredCount = 3
            ),
            RecruitCampaign(
                id = "camp-2",
                organisationName = "Alpha Coders Hub",
                recruitmentTitle = "Summer Mobile Developer Internships",
                description = "A paid internship for student developers looking to build production applications in Jetpack Compose and Kotlin. Work alongside senior engineers to develop core feature modules.",
                peopleNeeded = 5,
                jobType = "Internship",
                paymentRange = "$800 / Month",
                requirements = "Basic understanding of OOP, Git, and Android Studio. Previous Kotlin or Java projects.",
                deadline = "July 31, 2026",
                location = "Remote / Hybrid (Lusaka)",
                logoPreset = "TechCorp",
                applicantsCount = 42,
                shortlistedCount = 12,
                hiredCount = 4
            )
        )
    )
    val recruitmentCampaigns = _recruitmentCampaigns.asStateFlow()

    fun updateRecruiterWallet(amount: Double) {
        _recruiterWalletBalance.value = (_recruiterWalletBalance.value + amount).coerceAtLeast(0.0)
    }

    fun setRecruiterSubscribed(subscribed: Boolean) {
        _isRecruiterSubscribed.value = subscribed
    }

    fun addRecruitmentCampaign(campaign: RecruitCampaign) {
        _recruitmentCampaigns.value = listOf(campaign) + _recruitmentCampaigns.value
    }

    fun updateCampaignStats(campaignId: String, applicants: Int, shortlisted: Int, hired: Int) {
        _recruitmentCampaigns.value = _recruitmentCampaigns.value.map {
            if (it.id == campaignId) {
                it.copy(applicantsCount = applicants, shortlistedCount = shortlisted, hiredCount = hired)
            } else {
                it
            }
        }
    }

    private val _deletedPostsHistory = MutableStateFlow<List<DeletedPostHistory>>(emptyList())
    val deletedPostsHistory = _deletedPostsHistory.asStateFlow()

    fun addDeletedPostHistory(history: DeletedPostHistory) {
        _deletedPostsHistory.value = listOf(history) + _deletedPostsHistory.value
    }

    private val _improvementSuggestions = MutableStateFlow<List<ImprovementSuggestion>>(
        listOf(
            ImprovementSuggestion(
                id = 1,
                senderName = "Mercy Achieng",
                senderRole = "Worker",
                suggestionText = "It would be great if we could filter the piece-work jobs by proximity or transport fares.",
                timestamp = System.currentTimeMillis() - 3600000 * 4
            ),
            ImprovementSuggestion(
                id = 2,
                senderName = "David Omwamba",
                senderRole = "Employer",
                suggestionText = "Please add a template feature for job postings so we don't have to retype the descriptions every time.",
                timestamp = System.currentTimeMillis() - 3600000 * 24
            )
        )
    )
    val improvementSuggestions = _improvementSuggestions.asStateFlow()

    fun sendImprovementSuggestion(senderName: String, senderRole: String, text: String) {
        val newId = (_improvementSuggestions.value.maxOfOrNull { it.id } ?: 0) + 1
        val newSuggestion = ImprovementSuggestion(
            id = newId,
            senderName = senderName,
            senderRole = senderRole,
            suggestionText = text
        )
        _improvementSuggestions.value = listOf(newSuggestion) + _improvementSuggestions.value
    }

    fun deleteImprovementSuggestion(id: Int) {
        _improvementSuggestions.value = _improvementSuggestions.value.filter { it.id != id }
    }

    private val _customerSummaries = MutableStateFlow<List<CustomerExperienceSummary>>(
        listOf(
            CustomerExperienceSummary(
                customerName = "Mercy Achieng",
                category = "Worker",
                experienceRating = 5.0f,
                summaryText = "Completed 5 successful carpentry contracts in Nairobi. Highly satisfied with peer-to-peer cash settlements."
            ),
            CustomerExperienceSummary(
                customerName = "David Omwamba",
                category = "Employer",
                experienceRating = 4.5f,
                summaryText = "Sourced 3 high-quality agricultural workers for field clearing. Found the reputation trust profiles highly accurate."
            )
        )
    )
    val customerSummaries = _customerSummaries.asStateFlow()

    fun addCustomerSummary(summary: CustomerExperienceSummary) {
        _customerSummaries.value = listOf(summary) + _customerSummaries.value
    }

    // =============================================================================
    // ADMIN DASHBOARD & AUDIT ENGINE
    // =============================================================================
    private val _auditLogs = MutableStateFlow<List<AuditLog>>(listOf(
        AuditLog(adminName = "System", action = "Database validation sweep: 0 corruption blocks, clean sync.", target = "Platform Ledger", timestamp = System.currentTimeMillis() - 1000 * 60 * 30),
        AuditLog(adminName = "Admin", action = "Settings mutated: Transaction protocols selected to secure offline synchronization", target = "App Config", timestamp = System.currentTimeMillis() - 1000 * 60 * 60),
        AuditLog(adminName = "Admin", action = "Contract state change: Job Completed for contract node #948", target = "Contract Node", timestamp = System.currentTimeMillis() - 1000 * 60 * 120)
    ))
    val auditLogs = _auditLogs.asStateFlow()

    fun addAuditLog(action: String, target: String) {
        val adminName = _currentUser.value?.fullName ?: "Platform Admin"
        val newLog = AuditLog(adminName = adminName, action = action, target = target)
        _auditLogs.value = listOf(newLog) + _auditLogs.value
    }

    // Support Tickets Engine
    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(listOf(
        SupportTicket(userName = "Mwelwa Phiri", subject = "Payment Issue on Premium", description = "I tried purchasing Giggz Premium via Airtel Money, but the payment is showing pending in my dashboard. Please assist.", priority = "High"),
        SupportTicket(userName = "Mwansa Banda", subject = "Profile Photo Upload Error", description = "The app gives me an offline error when uploading a high resolution png image. How can I compress it?", priority = "Low"),
        SupportTicket(userName = "Mutale Chanda", subject = "Job Contract Dispute", description = "Employer hasn't released the milestone funds even though the work was completed yesterday. Please check.", priority = "High")
    ))
    val supportTickets = _supportTickets.asStateFlow()

    fun addSupportTicket(userName: String, subject: String, description: String, priority: String) {
        val ticket = SupportTicket(userName = userName, subject = subject, description = description, priority = priority)
        _supportTickets.value = listOf(ticket) + _supportTickets.value
    }

    fun replySupportTicket(ticketId: String, replyText: String) {
        _supportTickets.value = _supportTickets.value.map {
            if (it.id == ticketId) {
                it.copy(subject = "${it.subject} (Replied)", status = "Open")
            } else it
        }
    }

    fun resolveSupportTicket(ticketId: String) {
        _supportTickets.value = _supportTickets.value.map {
            if (it.id == ticketId) it.copy(status = "Resolved") else it
        }
    }

    fun closeSupportTicket(ticketId: String) {
        _supportTickets.value = _supportTickets.value.map {
            if (it.id == ticketId) it.copy(status = "Closed") else it
        }
    }

    // Payments / Purchases Engine
    private val _payments = MutableStateFlow<List<PaymentTransaction>>(listOf(
        PaymentTransaction(userName = "Bwalya Mubanga", type = "Premium Purchase", amount = 150f, status = "Completed", timestamp = System.currentTimeMillis() - 1000 * 60 * 120),
        PaymentTransaction(userName = "Chileshe Mulenga", type = "Boost Profile", amount = 50f, status = "Completed", timestamp = System.currentTimeMillis() - 1000 * 60 * 45),
        PaymentTransaction(userName = "Kondwani Zulu", type = "Premium Purchase", amount = 150f, status = "Pending", timestamp = System.currentTimeMillis() - 1000 * 60 * 10),
        PaymentTransaction(userName = "Mwiza Simbeye", type = "Boost Profile", amount = 50f, status = "Completed", timestamp = System.currentTimeMillis() - 1000 * 60 * 200)
    ))
    val payments = _payments.asStateFlow()

    // Interactive Admin App Settings Flow
    val adminAppName = MutableStateFlow("Giggz Zambia")
    val adminMaintenanceMode = MutableStateFlow(false)
    val adminPushNotifications = MutableStateFlow(true)
    val adminContactInfo = MutableStateFlow("info@giggz.co.zm")

    suspend fun checkAndReset24HourCycle() {
        val now = System.currentTimeMillis()
        val duration = now - _cycleStartTime.value
        val dayMs = 24 * 60 * 60 * 1000L
        if (duration >= dayMs) {
            val df = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
            val dateLabel = df.format(java.util.Date(_cycleStartTime.value))
            
            repository.insertDailyJobsHistory(
                DailyJobsHistoryEntity(
                    dateString = dateLabel,
                    jobsCount = _jobsDoneTodayCount.value,
                    timestamp = _cycleStartTime.value
                )
            )
            
            _jobsDoneTodayCount.value = 0
            _cycleStartTime.value = now
        }
    }

    fun simulate24HourReset() {
        viewModelScope.launch {
            val df = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
            val dateLabel = df.format(java.util.Date(_cycleStartTime.value))
            
            repository.insertDailyJobsHistory(
                DailyJobsHistoryEntity(
                    dateString = dateLabel,
                    jobsCount = _jobsDoneTodayCount.value,
                    timestamp = _cycleStartTime.value
                )
            )
            
            _jobsDoneTodayCount.value = (1..10).random()
            _cycleStartTime.value = System.currentTimeMillis()
        }
    }

    fun incrementCompletedJobsToday() {
        _jobsDoneTodayCount.value += 1
    }
}

data class AuditLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val adminName: String,
    val action: String,
    val target: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SupportTicket(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userName: String,
    val subject: String,
    val description: String,
    val status: String = "Open", // "Open", "Resolved", "Closed"
    val priority: String = "Medium", // "High", "Medium", "Low"
    val timestamp: Long = System.currentTimeMillis()
)

data class PaymentTransaction(
    val id: String = "TXN-${java.util.UUID.randomUUID().toString().take(6).uppercase()}",
    val userName: String,
    val type: String, // "Premium Purchase", "Boost Profile"
    val amount: Float, // ZMW
    val status: String = "Completed", // "Completed", "Pending", "Failed"
    val timestamp: Long = System.currentTimeMillis()
)

data class DeletedPostHistory(
    val title: String,
    val type: String, // "Job Contract" or "Marketplace Listing"
    val description: String,
    val dateDeleted: Long = System.currentTimeMillis(),
    val originalId: Int
)

data class ImprovementSuggestion(
    val id: Int = 0,
    val senderName: String,
    val senderRole: String,
    val suggestionText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CustomerExperienceSummary(
    val id: String = java.util.UUID.randomUUID().toString(),
    val customerName: String,
    val category: String, // "Worker", "Employer", "General"
    val experienceRating: Float,
    val summaryText: String,
    val dateAdded: Long = System.currentTimeMillis()
)

// Recruitment Campaign representation
data class RecruitCampaign(
    val id: String,
    val organisationName: String,
    val recruitmentTitle: String,
    val description: String,
    val peopleNeeded: Int,
    val jobType: String, // "Internship", "Part-time", "Temporary", "Full-time"
    val paymentRange: String,
    val requirements: String,
    val deadline: String,
    val location: String,
    val logoPreset: String = "TechCorp", // Preset logos
    val status: String = "Active", // "Active", "Paused", "Completed"
    val applicantsCount: Int = 0,
    val shortlistedCount: Int = 0,
    val hiredCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

// ViewModel Factory
class GiggzViewModelFactory(private val repository: GiggzRepository, private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GiggzViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GiggzViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
