package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class GiggzRepository(private val db: GiggzDatabase) {

    val firebaseService = FirebaseGiggzService()

    fun isFirebaseAvailable(): Boolean {
        return firebaseService.isAvailable()
    }

    // Exposure of Flows
    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsersFlow()
    val allWorkers: Flow<List<UserEntity>> = db.userDao().getAllWorkersFlow()
    val allJobs: Flow<List<JobEntity>> = db.jobDao().getAllJobsFlow()
    val allPieceWorks: Flow<List<JobEntity>> = db.jobDao().getPieceWorksFlow()
    val allApplications: Flow<List<ApplicationEntity>> = db.applicationDao().getAllApplicationsFlow()
    val allListings: Flow<List<ListingEntity>> = db.listingDao().getAllListingsFlow()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = db.announcementDao().getAllAnnouncementsFlow()
    val allReports: Flow<List<ReportEntity>> = db.reportDao().getAllReportsFlow()
    val allReviews: Flow<List<ReviewEntity>> = db.reviewDao().getAllReviewsFlow()

    fun getReviewsForUser(userId: Int): Flow<List<ReviewEntity>> = db.reviewDao().getReviewsForUserFlow(userId)
    suspend fun insertReview(review: ReviewEntity): Long = db.reviewDao().insertReview(review)

    // User Operations
    suspend fun getUserByEmail(email: String): UserEntity? = db.userDao().getUserByEmail(email)
    suspend fun getUserById(id: Int): UserEntity? = db.userDao().getUserById(id)
    fun getUserByIdFlow(id: Int): Flow<UserEntity?> = db.userDao().getUserByIdFlow(id)
    suspend fun insertUser(user: UserEntity): Long = db.userDao().insertUser(user)
    suspend fun updateUser(user: UserEntity) = db.userDao().updateUser(user)
    suspend fun deleteUser(id: Int) = db.userDao().deleteUser(id)
    suspend fun updateUserApproval(id: Int, approved: Boolean) = db.userDao().updateUserApproval(id, approved)

    // Job Operations
    fun getJobsByEmployer(employerId: Int): Flow<List<JobEntity>> = db.jobDao().getJobsByEmployerFlow(employerId)
    suspend fun insertJob(job: JobEntity): Long {
        val insertedId = db.jobDao().insertJob(job)
        if (isFirebaseAvailable()) {
            firebaseService.saveJob(job.copy(id = insertedId.toInt()), { docId ->
                android.util.Log.d("GiggzRepository", "Successfully synced job to Firestore with docId: $docId")
            }, { e ->
                android.util.Log.e("GiggzRepository", "Failed to sync job to Firestore: ${e.message}")
            })
        }
        return insertedId
    }
    suspend fun updateJob(job: JobEntity) = db.jobDao().updateJob(job)
    suspend fun updateJobStatus(id: Int, status: String) = db.jobDao().updateJobStatus(id, status)
    suspend fun deleteJob(id: Int) = db.jobDao().deleteJob(id)

    // Application Operations
    fun getApplicationsByWorker(workerId: Int): Flow<List<ApplicationEntity>> = db.applicationDao().getApplicationsByWorkerFlow(workerId)
    fun getApplicationsByEmployer(employerId: Int): Flow<List<ApplicationEntity>> = db.applicationDao().getApplicationsByEmployerFlow(employerId)
    fun getApplicationsForJob(jobId: Int): Flow<List<ApplicationEntity>> = db.applicationDao().getApplicationsForJobFlow(jobId)
    suspend fun insertApplication(app: ApplicationEntity): Long = db.applicationDao().insertApplication(app)
    suspend fun updateApplication(app: ApplicationEntity) = db.applicationDao().updateApplication(app)
    suspend fun updateApplicationStatus(id: Int, status: String) = db.applicationDao().updateApplicationStatus(id, status)
    suspend fun deleteApplication(id: Int) = db.applicationDao().deleteApplication(id)

    // Messaging Operations
    fun getChatMessages(userId1: Int, userId2: Int): Flow<List<MessageEntity>> = db.messageDao().getChatMessagesFlow(userId1, userId2)
    fun getUnreadMessagesCount(userId: Int): Flow<Int> = db.messageDao().getUnreadMessagesCountFlow(userId)
    fun getAllMessagesFlowForUser(userId: Int): Flow<List<MessageEntity>> = db.messageDao().getAllMessagesFlowForUser(userId)
    fun getChatPartners(userId: Int): Flow<List<Int>> = db.messageDao().getChatPartnersFlow(userId)
    suspend fun insertMessage(message: MessageEntity): Long {
        val insertedId = db.messageDao().insertMessage(message)
        if (isFirebaseAvailable()) {
            val senderUidStr = "user_${message.senderId}"
            val receiverUidStr = "user_${message.receiverId}"
            firebaseService.sendMessage(message.copy(id = insertedId.toInt()), senderUidStr, receiverUidStr, {
                android.util.Log.d("GiggzRepository", "Successfully synced chat message to Firestore")
            }, { e ->
                android.util.Log.e("GiggzRepository", "Failed to sync chat message to Firestore: ${e.message}")
            })
        }
        return insertedId
    }
    suspend fun markMessagesAsRead(senderId: Int, receiverId: Int) = db.messageDao().markMessagesAsRead(senderId, receiverId)
    suspend fun deleteChatMessages(userId1: Int, userId2: Int) = db.messageDao().deleteChatMessages(userId1, userId2)
    suspend fun updateMessageReactions(messageId: Int, reactions: String?) = db.messageDao().updateMessageReactions(messageId, reactions)
    suspend fun deleteMessageForEveryone(messageId: Int) = db.messageDao().deleteMessageForEveryone(messageId)

    // Listing Operations
    fun getListingsBySeller(sellerId: Int): Flow<List<ListingEntity>> = db.listingDao().getListingsBySellerFlow(sellerId)
    suspend fun insertListing(listing: ListingEntity): Long = db.listingDao().insertListing(listing)
    suspend fun updateListing(listing: ListingEntity) = db.listingDao().updateListing(listing)
    suspend fun deleteListing(id: Int) = db.listingDao().deleteListing(id)

    // Notification Operations
    fun getNotifications(userId: Int): Flow<List<NotificationEntity>> = db.notificationDao().getNotificationsFlow(userId)
    fun getActiveNotifications(userId: Int): Flow<List<NotificationEntity>> = db.notificationDao().getActiveNotificationsFlow(userId)
    fun getUnreadNotificationsCount(userId: Int): Flow<Int> = db.notificationDao().getUnreadCountFlow(userId)
    suspend fun insertNotification(notification: NotificationEntity): Long = db.notificationDao().insertNotification(notification)
    suspend fun markAllNotificationsAsRead(userId: Int) = db.notificationDao().markAllAsRead(userId)
    suspend fun archiveAllNotifications(userId: Int) = db.notificationDao().archiveAll(userId)
    suspend fun markNotificationAsRead(id: Int) = db.notificationDao().markAsRead(id)
    suspend fun updateNotificationFavorite(id: Int, isFavorite: Boolean) = db.notificationDao().updateFavoriteStatus(id, isFavorite)
    suspend fun deleteNotification(id: Int) = db.notificationDao().deleteNotification(id)
    suspend fun deleteNotificationsOlderThan(thresholdTime: Long) = db.notificationDao().deleteNotificationsOlderThan(thresholdTime)

    // Announcement Operations
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long = db.announcementDao().insertAnnouncement(announcement)

    // Report Operations
    suspend fun insertReport(report: ReportEntity): Long = db.reportDao().insertReport(report)
    suspend fun updateReportStatus(id: Int, status: String) = db.reportDao().updateReportStatus(id, status)

    // Event Operations
    val allEvents: Flow<List<EventEntity>> = db.eventDao().getAllEventsFlow()
    suspend fun insertEvent(event: EventEntity): Long = db.eventDao().insertEvent(event)
    suspend fun updateEvent(event: EventEntity) = db.eventDao().updateEvent(event)
    suspend fun deleteEvent(id: Int) = db.eventDao().deleteEvent(id)

    // Journal Operations
    fun getJournals(userId: Int): Flow<List<JournalEntity>> = db.journalDao().getJournalsFlow(userId)
    suspend fun insertJournal(journal: JournalEntity): Long = db.journalDao().insertJournal(journal)
    suspend fun deleteJournal(id: Int) = db.journalDao().deleteJournal(id)

    // Daily Jobs History Operations
    val allDailyJobsHistory: Flow<List<DailyJobsHistoryEntity>> = db.dailyJobsHistoryDao().getDailyJobsHistoryFlow()
    suspend fun insertDailyJobsHistory(entity: DailyJobsHistoryEntity): Long = db.dailyJobsHistoryDao().insertDailyJobsHistory(entity)
    suspend fun clearDailyJobsHistory() = db.dailyJobsHistoryDao().clearAll()

    // Prepopulate Database with Realistic Initial Mock Data
    suspend fun prepopulateMockData() {
        val count = db.userDao().getUserByEmail("admin@giggz.com")
        if (count != null) return // Already populated

        // 1. Prepopulate Roles/Users
        // Workers
        val worker1 = UserEntity(
            email = "john.carpenter@giggz.com",
            password = "password",
            role = "Worker",
            fullName = "John Carpenter",
            profilePhoto = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=150",
            nationality = "Kenyan",
            location = "Nairobi CBD",
            phone = "+254 712 345678",
            skills = "Carpentry, Roofing, Furniture Making, Wood Polishing",
            experience = 6,
            portfolio = "img_portfolio_1,img_portfolio_2",
            cvPath = "john_carpenter_resume.pdf",
            bio = "Professional artisan specializing in handcrafted teak furniture and residential roof installations. 6+ years in private contracts.",
            availabilityStatus = "Available",
            rating = 4.9f,
            completedJobs = 42,
            reviewsCount = 18
        )
        val worker2 = UserEntity(
            email = "lucy.spark@giggz.com",
            password = "password",
            role = "Worker",
            fullName = "Lucy Sparks",
            profilePhoto = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150",
            nationality = "Nigerian",
            location = "Lekki, Lagos",
            phone = "+234 803 123 4567",
            skills = "Electrical Wiring, Appliance Repair, Solar Systems, Smart Home Install",
            experience = 4,
            portfolio = "img_electrical_1,img_electrical_2",
            cvPath = "lucy_sparks_cv.pdf",
            bio = "Certified electrician focused on residential wiring, grid maintenance, and clean solar installations. Quick turnaround guaranteed.",
            availabilityStatus = "Available",
            rating = 4.7f,
            completedJobs = 28,
            reviewsCount = 12
        )
        val worker3 = UserEntity(
            email = "sam.fixer@giggz.com",
            password = "password",
            role = "Worker",
            fullName = "Sam Handy",
            profilePhoto = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150",
            nationality = "South African",
            location = "Sandton, Johannesburg",
            phone = "+27 82 123 4567",
            skills = "Plumbing, Leak Repair, Pipe Fitting, Water Pump Maintenance, Tiling",
            experience = 8,
            portfolio = "img_plumbing_1",
            cvPath = "sam_plumbing_cv.pdf",
            bio = "Expert plumber and home repair consultant. No job is too small, from leaking kitchen sinks to entire building renovations.",
            availabilityStatus = "Busy",
            rating = 4.9f,
            completedJobs = 64,
            reviewsCount = 29
        )
        val worker4 = UserEntity(
            email = "maria.dev@giggz.com",
            password = "password",
            role = "Worker",
            fullName = "Maria Chen",
            profilePhoto = "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150",
            nationality = "Singaporean",
            location = "Downtown Singapore",
            phone = "+65 9123 4567",
            skills = "Mobile App Development, Kotlin, Jetpack Compose, UI/UX Design, Firebase",
            experience = 5,
            portfolio = "img_dev_1,img_dev_2",
            cvPath = "maria_chen_resume.pdf",
            bio = "Android Developer creating beautiful, robust, and accessible Compose interfaces. Freelancer open to contract gigs or full-time roles.",
            availabilityStatus = "Available",
            rating = 5.0f,
            completedJobs = 15,
            reviewsCount = 10
        )

        // Job Givers (Employers)
        val employer1 = UserEntity(
            email = "buildtech@giggz.com",
            password = "password",
            role = "Employer",
            fullName = "BuildTech Solutions",
            profilePhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            nationality = "Global",
            location = "Lavington, Nairobi",
            phone = "+254 722 000111",
            bio = "We design, build, and remodel premium residential structures across East Africa.",
            availabilityStatus = "Available"
        )
        val employer2 = UserEntity(
            email = "greenleaf@giggz.com",
            password = "password",
            role = "Employer",
            fullName = "Sarah Jenkins (GreenLeaf Properties)",
            profilePhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            nationality = "American",
            location = "New York, USA",
            phone = "+1 212 555 0199",
            bio = "High-end real estate and hospitality providers managing 30+ estates across tourist hubs.",
            availabilityStatus = "Available"
        )

        // Admin
        val admin = UserEntity(
            email = "admin@giggz.com",
            password = "admin123",
            role = "Admin",
            fullName = "Giggz Administrator",
            profilePhoto = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150",
            nationality = "System",
            location = "Remote",
            phone = "+1 800 GIGGZ",
            bio = "Principal administrator for the Giggz Smart Job Marketplace & Local Community Commerce ecosystem.",
            availabilityStatus = "Available"
        )

        val w1Id = db.userDao().insertUser(worker1).toInt()
        val w2Id = db.userDao().insertUser(worker2).toInt()
        val w3Id = db.userDao().insertUser(worker3).toInt()
        val w4Id = db.userDao().insertUser(worker4).toInt()

        val emp1Id = db.userDao().insertUser(employer1).toInt()
        val emp2Id = db.userDao().insertUser(employer2).toInt()

        db.userDao().insertUser(admin)


        // 2. Prepopulate Job Listings
        // Normal contracts
        val job1 = JobEntity(
            title = "Modern Duplex Teak Cabinetry Work",
            description = "We require an expert carpenter to design and install floor-to-ceiling built-in teak wooden cabinets for our primary living room in Lavington. Must have experience working with solid wood and premium finishes. Budget includes materials, design, and mounting. Apply with a strong portfolio of woodcraft.",
            budget = 1200.0,
            category = "Carpentry",
            deadline = "2026-07-25",
            location = "Lavington, Nairobi",
            employerId = emp1Id,
            employerName = "BuildTech Solutions",
            employerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            status = "Active",
            isPieceWork = false,
            images = "img_furniture_ref"
        )
        val job2 = JobEntity(
            title = "Android App Codebase Polish & Compose refactor",
            description = "Looking for a seasoned Kotlin engineer to review our current retail application and refactor legacy XML fragments into Jetpack Compose. Must ensure standard performance recomposition rules are met. This is a 2-week contract. Work can be done remotely.",
            budget = 2500.0,
            category = "Mobile App Development",
            deadline = "2026-07-28",
            location = "Remote (USA/Global)",
            employerId = emp2Id,
            employerName = "Sarah Jenkins",
            employerPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            status = "Active",
            isPieceWork = false
        )
        val job3 = JobEntity(
            title = "Custom Solid Oak Pergola Construction",
            description = "BuildTech Solutions is contracting an experienced artisan carpenter to build an elegant 4-pillar backyard pergola in solid oak wood. Work includes concrete base anchoring, beam alignment, weatherproofing, and matte varnishing. All timber and fasteners are supplied on-site.",
            budget = 1600.0,
            category = "Carpentry",
            deadline = "2026-08-05",
            location = "Lavington, Nairobi",
            employerId = emp1Id,
            employerName = "BuildTech Solutions",
            employerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            status = "Active",
            isPieceWork = false
        )
        val job4 = JobEntity(
            title = "Full Bathroom Plumbing & Shower Renovation",
            description = "Seeking a professional plumber to overhaul plumbing lines for a master suite renovation. Scope includes removing galvanized piping, running clean PEX lines, installing a wall-mounted dual shower valve, tiling drainage alignment, and coupling the pedestal sink.",
            budget = 950.0,
            category = "Plumbing",
            deadline = "2026-08-12",
            location = "Lekki, Lagos",
            employerId = emp1Id,
            employerName = "BuildTech Solutions",
            employerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            status = "Active",
            isPieceWork = false
        )
        val job5 = JobEntity(
            title = "Smart Home Network Design & Ethernet Wiring",
            description = "Configure a secure high-speed gigabit Ethernet backhaul for a newly built luxury villa. Task involves punching down Cat6 wires to a patch panel, mounting a 16-port PoE switch, setting up 4 ceiling-mounted Wi-Fi 6 access points, and testing signal attenuation across three floors.",
            budget = 1400.0,
            category = "Electrical Wiring",
            deadline = "2026-08-18",
            location = "Sandton, Johannesburg",
            employerId = emp1Id,
            employerName = "BuildTech Solutions",
            employerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            status = "Active",
            isPieceWork = false
        )
        val job6 = JobEntity(
            title = "Jetpack Compose Landing Page & Analytics Dashboard",
            description = "Contract to create a responsive, edge-to-edge landing page and interactive charts dashboard using Compose for Android and Tablet. Must consume REST APIs gracefully, support dynamic dark/light themes, and have clean animations. Design provided in Figma.",
            budget = 1800.0,
            category = "Mobile App Development",
            deadline = "2026-08-22",
            location = "Remote",
            employerId = emp2Id,
            employerName = "Sarah Jenkins",
            employerPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            status = "Active",
            isPieceWork = false
        )

        // Piece Works (Quick/Same day/Hourly gigs)
        val gig1 = JobEntity(
            title = "Kitchen Water Pipe Leak Urgent Seal",
            description = "Our main kitchen drainpipe has cracked and is causing slow leakage. Need an expert plumber with their own sealant/tools to come today within the next 2 hours and patch it completely. Instant payment on completion.",
            budget = 45.0,
            category = "Plumbing",
            deadline = "Today",
            location = "Nairobi CBD",
            employerId = emp1Id,
            employerName = "BuildTech Solutions",
            employerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            status = "Active",
            isPieceWork = true,
            timeRequired = "2 hours",
            payType = "Hourly"
        )
        val gig2 = JobEntity(
            title = "Commercial Office Solar panel wiring inspection",
            description = "Need a technician to check the wiring linking our office's solar panels to the main inverter grid this Sunday morning. Clean the solar surfaces, inspect for any battery shorts, and run diagnostic safety reports.",
            budget = 180.0,
            category = "Electrical Wiring",
            deadline = "This Sunday",
            location = "Lekki, Lagos",
            employerId = emp1Id,
            employerName = "BuildTech Solutions",
            employerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            status = "Active",
            isPieceWork = true,
            timeRequired = "Weekend",
            payType = "Flat"
        )
        val gig3 = JobEntity(
            title = "Assemble 4 Flatpack Desks & Office Chairs",
            description = "We just received 4 office study desks and ergonomic mesh chairs from a local furniture store. Need someone handy and fast with tools to assemble them completely. Manual and Allen keys are inside the boxes. Simple, straightforward task.",
            budget = 60.0,
            category = "Carpentry",
            deadline = "Same Day",
            location = "Sandton, Johannesburg",
            employerId = emp1Id,
            employerName = "BuildTech Solutions",
            employerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            status = "Active",
            isPieceWork = true,
            timeRequired = "Same Day",
            payType = "Flat"
        )
        val gig4 = JobEntity(
            title = "Emergency Toilet Overflow Fix & Pipe Clear",
            description = "Urgent plumbing call: The guest restroom toilet is clogged and overflowing when flushed. Needs clearing of the obstruction with a heavy duty drain snake or mechanical auger. Hygiene is critical. Pay is premium for speed.",
            budget = 85.0,
            category = "Plumbing",
            deadline = "Immediate",
            location = "Nairobi CBD",
            employerId = emp1Id,
            employerName = "BuildTech Solutions",
            employerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            status = "Active",
            isPieceWork = true,
            timeRequired = "1 hour",
            payType = "Flat"
        )
        val gig5 = JobEntity(
            title = "Mount 65-inch Smart TV & Soundbar",
            description = "Need an experienced hand to securely wall-mount a new 65-inch television onto a brick chimney breast. Please bring your own stud finder, high-quality wall anchors, drill, and level. Will also mount a matching soundbar directly underneath.",
            budget = 55.0,
            category = "Services",
            deadline = "Same Day",
            location = "Lavington, Nairobi",
            employerId = emp2Id,
            employerName = "Sarah Jenkins",
            employerPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            status = "Active",
            isPieceWork = true,
            timeRequired = "2 hours",
            payType = "Flat"
        )
        val gig6 = JobEntity(
            title = "Urgent Backyard Leaf Raking & Grass Mowing",
            description = "Our property's lawn is overgrown and currently covered in heavy autumn foliage. Need a quick, diligent landscaping gig helper to rake all leaves into waste bags, mow the front/back grass, and edge the walkways nicely.",
            budget = 40.0,
            category = "Services",
            deadline = "Tomorrow",
            location = "Lekki, Lagos",
            employerId = emp2Id,
            employerName = "Sarah Jenkins",
            employerPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            status = "Active",
            isPieceWork = true,
            timeRequired = "Same Day",
            payType = "Hourly"
        )
        val gig7 = JobEntity(
            title = "Replace 4 Broken Wall Outlets & Install USB Sockets",
            description = "Looking for a basic electrical handyman. We have 4 standard white wall outlets that have loose contacts or minor cracks. Want to swap them out for heavy duty sockets that have built-in dual USB charging ports. We have purchased the replacement outlets.",
            budget = 70.0,
            category = "Electrical Wiring",
            deadline = "This Weekend",
            location = "Sandton, Johannesburg",
            employerId = emp1Id,
            employerName = "BuildTech Solutions",
            employerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            status = "Active",
            isPieceWork = true,
            timeRequired = "2 hours",
            payType = "Flat"
        )
        val gig8 = JobEntity(
            title = "Android Room Database Offline Migration Help",
            description = "Need a quick virtual pairing session with a senior Android dev. Our Room DB schema is failing to migrate from version 2 to 3 because of a new non-null foreign key constraint. Need to draft a clean SQL migration script.",
            budget = 120.0,
            category = "Mobile App Development",
            deadline = "Tonight",
            location = "Remote",
            employerId = emp2Id,
            employerName = "Sarah Jenkins",
            employerPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            status = "Active",
            isPieceWork = true,
            timeRequired = "1 hour",
            payType = "Hourly"
        )

        db.jobDao().insertJob(job1)
        db.jobDao().insertJob(job2)
        db.jobDao().insertJob(job3)
        db.jobDao().insertJob(job4)
        db.jobDao().insertJob(job5)
        db.jobDao().insertJob(job6)
        val gig1Id = db.jobDao().insertJob(gig1).toInt()
        db.jobDao().insertJob(gig2)
        db.jobDao().insertJob(gig3)
        db.jobDao().insertJob(gig4)
        db.jobDao().insertJob(gig5)
        db.jobDao().insertJob(gig6)
        db.jobDao().insertJob(gig7)
        db.jobDao().insertJob(gig8)


        // 3. Prepopulate Marketplace Listings (Ama Sample)
        val item1 = ListingEntity(
            title = "iPhone 14 Pro Max 256GB - Space Black",
            description = "Battery health 91%. Body is in absolute flawless condition, protected with screen guard and case from day one. Unlocked to all networks. Comes with original charging cable and box. Selling because I upgraded.",
            price = 850.0,
            category = "Phones",
            imageUrls = "img_phone_1",
            sellerId = w1Id,
            sellerName = "John Carpenter",
            sellerRole = "Worker",
            sellerPhoto = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=150",
            status = "Available",
            isOfferService = false
        )
        val item2 = ListingEntity(
            title = "Professional Premium Sound Bar with Subwoofer",
            description = "Selling a slightly used Sony HT-S400 sound bar. Powerful 330W sound, 2.1 channel speaker with active subwoofer. Mind-blowing bass for movies and house parties. Supports Bluetooth and optical ARC input.",
            price = 140.0,
            category = "Electronics",
            imageUrls = "img_audio_1",
            sellerId = w2Id,
            sellerName = "Lucy Sparks",
            sellerRole = "Worker",
            sellerPhoto = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150",
            status = "Available",
            isOfferService = false
        )
        val item3 = ListingEntity(
            title = "On-site House Painting & Wall Scraping Services",
            description = "Offering high-quality professional wall painting, mold scraping, and premium textured finish services. Standard prices starting at K100 per room. Multi-room discounts are available. Let us refresh your home aesthetic!",
            price = 50.0,
            category = "Services",
            imageUrls = "img_paint_1",
            sellerId = w3Id,
            sellerName = "Sam Handy",
            sellerRole = "Worker",
            sellerPhoto = "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150",
            status = "Available",
            isOfferService = true
        )
        val item4 = ListingEntity(
            title = "Ergonomic Mesh Swivel Office Chair",
            description = "High back rest, adjustable lumbar support, and heavy-duty smooth rotating wheels. Extremely comfortable for long hours of software development or study. Selling due to office relocation.",
            price = 95.0,
            category = "Furniture",
            imageUrls = "img_chair_1",
            sellerId = emp1Id,
            sellerName = "BuildTech Solutions",
            sellerRole = "Employer",
            sellerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            status = "Available",
            isOfferService = false
        )

        db.listingDao().insertListing(item1)
        db.listingDao().insertListing(item2)
        db.listingDao().insertListing(item3)
        db.listingDao().insertListing(item4)


        // 4. Prepopulate Messages (In-App Chats)
        val msg1 = MessageEntity(
            senderId = emp1Id,
            senderName = "BuildTech Solutions",
            receiverId = w3Id, // Sam Handy
            messageText = "Hello Sam, I noticed you have over 8 years of plumbing experience. Are you available for an urgent plumbing contract on our duplex site in Nairobi CBD?",
            timestamp = System.currentTimeMillis() - 3600000 * 3, // 3 hours ago
            isRead = true
        )
        val msg2 = MessageEntity(
            senderId = w3Id,
            senderName = "Sam Handy",
            receiverId = emp1Id,
            messageText = "Hi BuildTech! Yes, absolutely. I am finishing up a water pump replacement today, but I am free starting tomorrow. What is the precise scope of work?",
            timestamp = System.currentTimeMillis() - 3600000 * 2, // 2 hours ago
            isRead = true
        )
        val msg3 = MessageEntity(
            senderId = emp1Id,
            senderName = "BuildTech Solutions",
            receiverId = w3Id,
            messageText = "Perfect! It involves laying down copper tubes for 3 luxury bathrooms and coupling the master bath sewage lines. I can send over the design blueprint.",
            timestamp = System.currentTimeMillis() - 1800000, // 30 mins ago
            isRead = false
        )

        db.messageDao().insertMessage(msg1)
        db.messageDao().insertMessage(msg2)
        db.messageDao().insertMessage(msg3)


        // 5. Prepopulate Notifications
        val notif1 = NotificationEntity(
            userId = w1Id,
            title = "Welcome to Giggz!",
            message = "Your profile is active. Check out local job matches or create listings in the Ama Sampo today!",
            category = "promo",
            timestamp = System.currentTimeMillis() - 3600000 * 12
        )
        val notif2 = NotificationEntity(
            userId = w3Id,
            title = "New Message from Employer",
            message = "BuildTech Solutions sent you a message regarding a plumbing project.",
            category = "message",
            timestamp = System.currentTimeMillis() - 1800000
        )

        db.notificationDao().insertNotification(notif1)
        db.notificationDao().insertNotification(notif2)


        // 6. Prepopulate Announcements
        val ann1 = AnnouncementEntity(
            title = "Introducing Giggz Ama Sampo!",
            content = "We have officially launched Ama Sampo—our hyper-local neighborhood marketplace where you can trade electronics, purchase professional services, buy furniture, and negotiate directly with sellers using real-time chat. Click 'Marketplace' to list your first item now!",
            type = "app"
        )
        db.announcementDao().insertAnnouncement(ann1)

        // 7. Prepopulate Events
        val event1 = EventEntity(
            title = "Nairobi Tech Expo 2026",
            description = "Kenya's premier tech and gig workers conference. Discover high-paying remote roles, find local partners, and get hands-on training with AI and tech platforms.",
            date = "July 24, 2026",
            location = "Nairobi Convention Centre",
            promoterId = 5,
            promoterName = "Jane Doe (TechOrg)",
            promoterPhoto = "emp1",
            imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop",
            savedByUserIds = "2,3"
        )
        val event2 = EventEntity(
            title = "Giggz Community Cleanup & Gig Fair",
            description = "Clean up the central community park and match instantly with local neighborhood employers for landscaping, painting, plumbing, and delivery jobs.",
            date = "July 12, 2026",
            location = "Central Park Community Hub",
            promoterId = 6,
            promoterName = "BuildTech Solutions",
            promoterPhoto = "emp2",
            imageUrl = "https://images.unsplash.com/photo-1528605248644-14dd04022da1?w=800&auto=format&fit=crop",
            savedByUserIds = "3"
        )
        db.eventDao().insertEvent(event1)
        db.eventDao().insertEvent(event2)

        // 8. Prepopulate Reviews
        val rev1 = ReviewEntity(
            jobId = 1,
            jobTitle = "Modern Duplex Teak Cabinetry Work",
            reviewerId = emp1Id,
            reviewerName = "BuildTech Solutions",
            reviewerPhoto = "https://images.unsplash.com/photo-1507207611509-ec012433ff52?w=150",
            revieweeId = w1Id,
            revieweeName = "John Carpenter",
            rating1 = 5f, // Quality of work
            rating2 = 4f, // Punctuality
            rating3 = 5f, // Communication
            rating4 = 5f, // Professionalism
            averageRating = 4.75f,
            comment = "John is a highly reliable carpenter with excellent communication and beautiful woodcraft. Finished the teak cabinetry on schedule and within the budget.",
            isFromWorker = false,
            imageUrl = "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?w=500" // Woodwork proof
        )

        val rev2 = ReviewEntity(
            jobId = 2,
            jobTitle = "Solar Battery Cell Maintenance",
            reviewerId = emp2Id,
            reviewerName = "Sarah Jenkins",
            reviewerPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            revieweeId = w2Id,
            revieweeName = "Lucy Sparks",
            rating1 = 5f, // Quality
            rating2 = 5f, // Punctuality
            rating3 = 4f, // Communication
            rating4 = 5f, // Professionalism
            averageRating = 4.75f,
            comment = "Lucy is extremely punctual and did a very clean installation of our solar battery cells. Extremely knowledgeable about smart grid settings.",
            isFromWorker = false
        )

        val rev3 = ReviewEntity(
            jobId = 1,
            jobTitle = "Modern Duplex Teak Cabinetry Work",
            reviewerId = w1Id,
            reviewerName = "John Carpenter",
            reviewerPhoto = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=150",
            revieweeId = emp1Id,
            revieweeName = "BuildTech Solutions",
            rating1 = 5f, // Fairness of payment
            rating2 = 5f, // Clarity of instructions
            rating3 = 5f, // Respect
            rating4 = 5f, // Responsiveness
            averageRating = 5f,
            comment = "BuildTech Solutions is a phenomenal employer. Paid immediately upon delivery, provided very clear layout guidelines, and was always responsive to questions.",
            isFromWorker = true
        )

        db.reviewDao().insertReview(rev1)
        db.reviewDao().insertReview(rev2)
        db.reviewDao().insertReview(rev3)
    }

    private var lastCycleIndex = 0

    suspend fun cycleMockData() {
        val adminUser = db.userDao().getUserByEmail("admin@giggz.com")
        if (adminUser == null) {
            prepopulateMockData()
            return
        }

        val index = (lastCycleIndex + 1) % 3
        lastCycleIndex = index

        val w1 = db.userDao().getUserByEmail("john.carpenter@giggz.com")
        val w2 = db.userDao().getUserByEmail("lucy.spark@giggz.com")
        val w3 = db.userDao().getUserByEmail("sam.fixer@giggz.com")
        val w4 = db.userDao().getUserByEmail("maria.dev@giggz.com")

        val emp1 = db.userDao().getUserByEmail("buildtech@giggz.com")
        val emp2 = db.userDao().getUserByEmail("greenleaf@giggz.com")

        val w1Id = w1?.id ?: 1
        val w2Id = w2?.id ?: 2
        val w3Id = w3?.id ?: 3
        val w4Id = w4?.id ?: 4

        val emp1Id = emp1?.id ?: 5
        val emp2Id = emp2?.id ?: 6

        db.jobDao().deleteAllJobs()
        db.listingDao().deleteAllListings()
        db.eventDao().deleteAllEvents()

        when (index) {
            1 -> {
                val j1 = JobEntity(
                    title = "Custom Mahogany Dining Table Craft",
                    description = "Seeking an experienced carpenter to design and build a custom 10-seater mahogany dining table with detailed claw feet and premium gloss finish. Design blueprint will be shared. Materials are fully covered by us.",
                    budget = 1500.0,
                    category = "Carpentry",
                    deadline = "2026-08-05",
                    location = "Lavington, Nairobi",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false,
                    images = "img_furniture_ref"
                )
                val j2 = JobEntity(
                    title = "Offline-First SQLite Database Integration",
                    description = "We need an Android developer to migrate our existing networking cache from simple SharedPreferences into a structured offline-first SQLite database using Android Room. Must handle database migrations and state flows elegantly.",
                    budget = 3200.0,
                    category = "Mobile App Development",
                    deadline = "2026-08-10",
                    location = "Remote (USA/Global)",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )
                val j3 = JobEntity(
                    title = "Full Kitchen Copper Pipe Renovation",
                    description = "BuildTech Solutions requires an expert plumber to inspect, disassemble, and replace aging copper pipe connections in our Lavington duplex kitchen. Contractor must supply their own tools. Materials will be billed separately with a valid invoice.",
                    budget = 1100.0,
                    category = "Plumbing",
                    deadline = "2026-08-14",
                    location = "Lavington, Nairobi",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )
                val j4 = JobEntity(
                    title = "Outdoor Security Lighting Grid Design",
                    description = "Looking for a certified electrical engineer to draft a secure, low-energy LED outdoor security lighting perimeter layout. Will coordinate with construction leads to wire conduit tubing, mount smart photocell motion floodlights, and set up automatic twilight toggles.",
                    budget = 950.0,
                    category = "Electrical Wiring",
                    deadline = "2026-08-20",
                    location = "Lekki, Lagos",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )
                val j5 = JobEntity(
                    title = "Social Media App UI Redesign (Figma to Compose)",
                    description = "Sarah Jenkins (GreenLeaf Properties) is contracting a mobile UI specialist to refactor an existing social dashboard from raw XML/JSON configurations to polished, beautiful Jetpack Compose views. Highlights include interactive animations and seamless transition flows.",
                    budget = 1900.0,
                    category = "Mobile App Development",
                    deadline = "2026-08-26",
                    location = "Remote",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )

                val g1 = JobEntity(
                    title = "Clogged Bathroom Drain Emergency Clearance",
                    description = "The master bathroom bathtub and toilet are completely clogged and water is slowly backing up. Need an emergency plumber with a heavy-duty drain snake or auger to clear the line immediately.",
                    budget = 80.0,
                    category = "Plumbing",
                    deadline = "Today",
                    location = "Nairobi CBD",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "2 hours",
                    payType = "Hourly"
                )
                val g2 = JobEntity(
                    title = "Smart Security Camera Network Installation",
                    description = "Need a technician to mount and wire 6 outdoor smart security cameras around our commercial building, wire them to the central power supply, and configure the PoE switch. Cat6 cables are already laid.",
                    budget = 220.0,
                    category = "Electrical Wiring",
                    deadline = "This Saturday",
                    location = "Lekki, Lagos",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "Weekend",
                    payType = "Flat"
                )
                val g3 = JobEntity(
                    title = "Fix Squeaky Floorboards & Door Hinges",
                    description = "Several wooden floorboards in our hallway squeak loudly when stepped on, and three bedroom doors have misaligned hinges that scrape the floor. Need a carpenter for a quick 3-hour house call to silence and align them.",
                    budget = 75.0,
                    category = "Carpentry",
                    deadline = "Same Day",
                    location = "Sandton, Johannesburg",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "Same Day",
                    payType = "Flat"
                )
                val g4 = JobEntity(
                    title = "Help Move Heavy Refrigerator & 5 Moving Boxes",
                    description = "Need a quick piece of help. Moving a double-door steel refrigerator from our kitchen to the ground floor loading bay, plus 5 sealed boxes. Must have a hand truck or furniture straps to prevent scuffs.",
                    budget = 40.0,
                    category = "Services",
                    deadline = "Today",
                    location = "Nairobi CBD",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "1 hour",
                    payType = "Flat"
                )
                val g5 = JobEntity(
                    title = "Assemble IKEA Wardrobe with Sliding Doors",
                    description = "Need an assembly helper. We have a flatpack wardrobe with twin frosted-glass sliding doors. All packages are in the bedroom. Please bring cordless drivers and proper bits.",
                    budget = 80.0,
                    category = "Carpentry",
                    deadline = "Tomorrow",
                    location = "Lavington, Nairobi",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "3 hours",
                    payType = "Flat"
                )

                db.jobDao().insertJob(j1)
                db.jobDao().insertJob(j2)
                db.jobDao().insertJob(j3)
                db.jobDao().insertJob(j4)
                db.jobDao().insertJob(j5)
                db.jobDao().insertJob(g1)
                db.jobDao().insertJob(g2)
                db.jobDao().insertJob(g3)
                db.jobDao().insertJob(g4)
                db.jobDao().insertJob(g5)

                val item1 = ListingEntity(
                    title = "Samsung Galaxy S23 Ultra 512GB - Cream",
                    description = "Unlocked cream edition. Comes with original S-Pen and box. No scratches on screen, minor wear on the bottom bezel. 12GB RAM, incredible 200MP zoom camera. Battery lasts 2 full days.",
                    price = 780.0,
                    category = "Phones",
                    imageUrls = "img_phone_1",
                    sellerId = w1Id,
                    sellerName = w1?.fullName ?: "John Carpenter",
                    sellerRole = "Worker",
                    sellerPhoto = w1?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = false
                )
                val item2 = ListingEntity(
                    title = "Over-Ear Noise Cancelling Wireless Headphones",
                    description = "Sony WH-1000XM4 in pristine matte black. Industry-leading active noise cancellation (ANC), up to 30 hours of battery life, and touch control sensor. Complete with premium carry case and audio cable.",
                    price = 160.0,
                    category = "Electronics",
                    imageUrls = "img_audio_1",
                    sellerId = w2Id,
                    sellerName = w2?.fullName ?: "Lucy Sparks",
                    sellerRole = "Worker",
                    sellerPhoto = w2?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = false
                )
                val item3 = ListingEntity(
                    title = "Premium Car Detailing & Paint Protection",
                    description = "Mobile car wash and professional detailing. Deep vacuum, leather conditioning, clay bar treatment, and hand wax polish right at your driveway. We bring our own water and power generators.",
                    price = 40.0,
                    category = "Services",
                    imageUrls = "img_paint_1",
                    sellerId = w3Id,
                    sellerName = w3?.fullName ?: "Sam Handy",
                    sellerRole = "Worker",
                    sellerPhoto = w3?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = true
                )
                val item4 = ListingEntity(
                    title = "Rustic Solid Oak Coffee Table",
                    description = "Beautifully handcrafted coffee table with a rustic oak top and industrial matte-black metal frame. Features a bottom shelf for magazine/book storage. Solid, sturdy, and heavy.",
                    price = 120.0,
                    category = "Furniture",
                    imageUrls = "img_chair_1",
                    sellerId = emp1Id,
                    sellerName = emp1?.fullName ?: "BuildTech Solutions",
                    sellerRole = "Employer",
                    sellerPhoto = emp1?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = false
                )

                db.listingDao().insertListing(item1)
                db.listingDao().insertListing(item2)
                db.listingDao().insertListing(item3)
                db.listingDao().insertListing(item4)

                val event1 = EventEntity(
                    title = "East Africa Freelancer Summit 2026",
                    description = "Join 1,000+ top independent builders, remote contractors, and digital designers. Panels on cross-border payments, remote work taxes, and networking.",
                    date = "August 15, 2026",
                    location = "Kenyatta International Conference Center",
                    promoterId = 5,
                    promoterName = "Jane Doe (TechOrg)",
                    promoterPhoto = "emp1",
                    imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop",
                    savedByUserIds = "2,3"
                )
                val event2 = EventEntity(
                    title = "Lusaka Artisans Craftsmanship Fair",
                    description = "A vibrant outdoor marketplace and workshop series showcasing local carpenters, metalworkers, and custom furniture makers. Free entry and live demonstrations.",
                    date = "August 10, 2026",
                    location = "Lusaka National Showgrounds",
                    promoterId = 6,
                    promoterName = "BuildTech Solutions",
                    promoterPhoto = "emp2",
                    imageUrl = "https://images.unsplash.com/photo-1528605248644-14dd04022da1?w=800&auto=format&fit=crop",
                    savedByUserIds = "3"
                )

                db.eventDao().insertEvent(event1)
                db.eventDao().insertEvent(event2)
            }
            2 -> {
                val j1 = JobEntity(
                    title = "Teak Wood Pergola Construction",
                    description = "Looking for a seasoned carpenter to build an outdoor backyard teak wood pergola for shade and relaxation. Must handle wood treatment, weatherproofing, and ground anchoring. Budget includes labor and tools.",
                    budget = 1800.0,
                    category = "Carpentry",
                    deadline = "2026-09-01",
                    location = "Lavington, Nairobi",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false,
                    images = "img_furniture_ref"
                )
                val j2 = JobEntity(
                    title = "Real-time Chat Feature via WebSockets/Ktor",
                    description = "Seeking an Android Kotlin specialist to implement an in-app real-time messaging system using WebSockets or Ktor Client. Must support message persistence, delivery indicators (Sent, Delivered, Read), and image uploads.",
                    budget = 2800.0,
                    category = "Mobile App Development",
                    deadline = "2026-09-10",
                    location = "Remote (USA/Global)",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )
                val j3 = JobEntity(
                    title = "Multi-Apartment Sewage Vent Restoration",
                    description = "BuildTech Solutions is hiring a commercial plumber to repair and redirect corroded cast-iron sewage ventilation shafts in a three-story residential apartment block. Must read blueprints, replace seals, and conduct leak-barrier diagnostics.",
                    budget = 1400.0,
                    category = "Plumbing",
                    deadline = "2026-09-15",
                    location = "Nairobi CBD",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )
                val j4 = JobEntity(
                    title = "Warehouse Electrical Recertification Audit",
                    description = "Contract for a licensed industrial electrician to audit and recertify a 2,500 sq ft logistics depot. Task involves testing 3-phase power distribution boxes, validating grounding spikes, testing fire alarms, and drafting an compliance certificate.",
                    budget = 2200.0,
                    category = "Electrical Wiring",
                    deadline = "2026-09-22",
                    location = "Lekki, Lagos",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )
                val j5 = JobEntity(
                    title = "E-Commerce Jetpack Compose Multiplatform Upgrade",
                    description = "Seeking a senior Kotlin developer to upgrade our existing Compose mobile application into a unified Compose Multiplatform project targets Android and iOS. Needs strong architectural skills in Kotlin Multiplatform (KMP) & Decompose.",
                    budget = 3500.0,
                    category = "Mobile App Development",
                    deadline = "2026-09-28",
                    location = "Remote",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )

                val g1 = JobEntity(
                    title = "Hot Water Heater Thermostat Replacement",
                    description = "Our solar hot water heater has stopped heating properly. Diagnostics show the thermostat has shorted. Need a professional plumber/electrician to safely install a new thermostat today. We have the spare unit.",
                    budget = 90.0,
                    category = "Plumbing",
                    deadline = "Today",
                    location = "Nairobi CBD",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "2 hours",
                    payType = "Hourly"
                )
                val g2 = JobEntity(
                    title = "LED Chandelier & Dimmer Switch Mounting",
                    description = "Looking for an electrician to wire and hang a heavy crystal LED chandelier in our high-ceiling living room, and replace the standard wall switch with a digital dimmer dial.",
                    budget = 130.0,
                    category = "Electrical Wiring",
                    deadline = "This Sunday",
                    location = "Lekki, Lagos",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "Weekend",
                    payType = "Flat"
                )
                val g3 = JobEntity(
                    title = "Repair Broken Kitchen Cabinet Hinges",
                    description = "Two cabinet doors in our kitchen have detached from their hydraulic hinges. Need a skilled hand to replace the brackets and hinges so they close smoothly. We have the replacement hinges ready.",
                    budget = 50.0,
                    category = "Carpentry",
                    deadline = "Same Day",
                    location = "Sandton, Johannesburg",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "Same Day",
                    payType = "Flat"
                )
                val g4 = JobEntity(
                    title = "Unclog Kitchen Double-Sink Waste Disposal",
                    description = "Quick kitchen call: Our twin sink drain is completely blocked due to food pulp backup in the garbage disposal unit. Need a plumber to isolate power, disassemble the trap, flush lines, and reset the blade unit.",
                    budget = 65.0,
                    category = "Plumbing",
                    deadline = "Same Day",
                    location = "Nairobi CBD",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "2 hours",
                    payType = "Flat"
                )
                val g5 = JobEntity(
                    title = "Clean Outdoor Solar Panels & Gutters",
                    description = "Looking for a seasonal roof helper. Clean dust and dry leaves off a 12-panel solar array, wash with non-abrasive soft water, and clean pine needles out of the gutters. Safety harness provided.",
                    budget = 90.0,
                    category = "Services",
                    deadline = "This Saturday",
                    location = "Lavington, Nairobi",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "Weekend",
                    payType = "Flat"
                )
                val g6 = JobEntity(
                    title = "Local Store Delivery Assistant (3 Hours)",
                    description = "Sarah Jenkins (GreenLeaf Properties) is hiring a driver's assistant to help unload 15 flatpack boxes from a shipping truck and stack them in the main hallway. Clean, simple lifting gig.",
                    budget = 45.0,
                    category = "Services",
                    deadline = "Today",
                    location = "Sandton, Johannesburg",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "3 hours",
                    payType = "Hourly"
                )

                db.jobDao().insertJob(j1)
                db.jobDao().insertJob(j2)
                db.jobDao().insertJob(j3)
                db.jobDao().insertJob(j4)
                db.jobDao().insertJob(j5)
                db.jobDao().insertJob(g1)
                db.jobDao().insertJob(g2)
                db.jobDao().insertJob(g3)
                db.jobDao().insertJob(g4)
                db.jobDao().insertJob(g5)
                db.jobDao().insertJob(g6)

                val item1 = ListingEntity(
                    title = "Google Pixel 8 Pro 128GB - Bay Blue",
                    description = "Bay blue shade, factory unlocked. Brand new condition, used for only three weeks as a review unit. Spectacular AI photo processing and pure stock Android experience. Screen guard pre-applied.",
                    price = 690.0,
                    category = "Phones",
                    imageUrls = "img_phone_1",
                    sellerId = w1Id,
                    sellerName = w1?.fullName ?: "John Carpenter",
                    sellerRole = "Worker",
                    sellerPhoto = w1?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = false
                )
                val item2 = ListingEntity(
                    title = "4K Ultra-Short Throw Laser Projector",
                    description = "Bring the cinema home with this 2500-lumen laser projector. Projects up to 120 inches from just inches away from the wall. Integrated Dolby Audio speakers. Android TV built-in.",
                    price = 950.0,
                    category = "Electronics",
                    imageUrls = "img_audio_1",
                    sellerId = w2Id,
                    sellerName = w2?.fullName ?: "Lucy Sparks",
                    sellerRole = "Worker",
                    sellerPhoto = w2?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = false
                )
                val item3 = ListingEntity(
                    title = "Professional Landscaping & Lawn Sculpting",
                    description = "Transform your outdoor garden. Services include lawn mowing, flower bed weeding, hedge trimming, and landscape design consult. Fast, clean, and reliable weekend visits.",
                    price = 65.0,
                    category = "Services",
                    imageUrls = "img_paint_1",
                    sellerId = w3Id,
                    sellerName = w3?.fullName ?: "Sam Handy",
                    sellerRole = "Worker",
                    sellerPhoto = w3?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = true
                )
                val item4 = ListingEntity(
                    title = "Minimalist L-Shaped Corner Study Desk",
                    description = "Spacious corner desk with smooth dark walnut finish and steel structure. Features a built-in cable management tray. Great for dual-monitor setups and creative workspace.",
                    price = 110.0,
                    category = "Furniture",
                    imageUrls = "img_chair_1",
                    sellerId = emp1Id,
                    sellerName = emp1?.fullName ?: "BuildTech Solutions",
                    sellerRole = "Employer",
                    sellerPhoto = emp1?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = false
                )

                db.listingDao().insertListing(item1)
                db.listingDao().insertListing(item2)
                db.listingDao().insertListing(item3)
                db.listingDao().insertListing(item4)

                val event1 = EventEntity(
                    title = "AI & Future of Work Symposium",
                    description = "Understand how generative AI is shifting developer, writer, and designer workflows. Learn how to stay ahead of the curve and leverage AI tools for contract work.",
                    date = "September 02, 2026",
                    location = "The Greenhouse Innovation Hub",
                    promoterId = 5,
                    promoterName = "Jane Doe (TechOrg)",
                    promoterPhoto = "emp1",
                    imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop",
                    savedByUserIds = "2,3"
                )
                val event2 = EventEntity(
                    title = "Neighborhood Greenery & Planting Drive",
                    description = "Support local sustainability. We are planting 500 indigenous trees across the suburban green belts. Lunch and gardening gear provided to all volunteers.",
                    date = "September 18, 2026",
                    location = "Loresho Community Reserve",
                    promoterId = 6,
                    promoterName = "BuildTech Solutions",
                    promoterPhoto = "emp2",
                    imageUrl = "https://images.unsplash.com/photo-1528605248644-14dd04022da1?w=800&auto=format&fit=crop",
                    savedByUserIds = "3"
                )

                db.eventDao().insertEvent(event1)
                db.eventDao().insertEvent(event2)
            }
            else -> {
                val j1 = JobEntity(
                    title = "Modern Duplex Teak Cabinetry Work",
                    description = "We require an expert carpenter to design and install floor-to-ceiling built-in teak wooden cabinets for our primary living room in Lavington. Must have experience working with solid wood and premium finishes. Budget includes materials, design, and mounting. Apply with a strong portfolio of woodcraft.",
                    budget = 1200.0,
                    category = "Carpentry",
                    deadline = "2026-07-25",
                    location = "Lavington, Nairobi",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false,
                    images = "img_furniture_ref"
                )
                val j2 = JobEntity(
                    title = "Android App Codebase Polish & Compose refactor",
                    description = "Looking for a seasoned Kotlin engineer to review our current retail application and refactor legacy XML fragments into Jetpack Compose. Must ensure standard performance recomposition rules are met. This is a 2-week contract. Work can be done remotely.",
                    budget = 2500.0,
                    category = "Mobile App Development",
                    deadline = "2026-07-28",
                    location = "Remote (USA/Global)",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )
                val j3 = JobEntity(
                    title = "Custom Solid Oak Pergola Construction",
                    description = "BuildTech Solutions is contracting an experienced artisan carpenter to build an elegant 4-pillar backyard pergola in solid oak wood. Work includes concrete base anchoring, beam alignment, weatherproofing, and matte varnishing. All timber and fasteners are supplied on-site.",
                    budget = 1600.0,
                    category = "Carpentry",
                    deadline = "2026-08-05",
                    location = "Lavington, Nairobi",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )
                val j4 = JobEntity(
                    title = "Full Bathroom Plumbing & Shower Renovation",
                    description = "Seeking a professional plumber to overhaul plumbing lines for a master suite renovation. Scope includes removing galvanized piping, running clean PEX lines, installing a wall-mounted dual shower valve, tiling drainage alignment, and coupling the pedestal sink.",
                    budget = 950.0,
                    category = "Plumbing",
                    deadline = "2026-08-12",
                    location = "Lekki, Lagos",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )
                val j5 = JobEntity(
                    title = "Smart Home Network Design & Ethernet Wiring",
                    description = "Configure a secure high-speed gigabit Ethernet backhaul for a newly built luxury villa. Task involves punching down Cat6 wires to a patch panel, mounting a 16-port PoE switch, setting up 4 ceiling-mounted Wi-Fi 6 access points, and testing signal attenuation across three floors.",
                    budget = 1400.0,
                    category = "Electrical Wiring",
                    deadline = "2026-08-18",
                    location = "Sandton, Johannesburg",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )
                val j6 = JobEntity(
                    title = "Jetpack Compose Landing Page & Analytics Dashboard",
                    description = "Contract to create a responsive, edge-to-edge landing page and interactive charts dashboard using Compose for Android and Tablet. Must consume REST APIs gracefully, support dynamic dark/light themes, and have clean animations. Design provided in Figma.",
                    budget = 1800.0,
                    category = "Mobile App Development",
                    deadline = "2026-08-22",
                    location = "Remote",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = false
                )

                // Piece Works (Quick/Same day/Hourly gigs)
                val g1 = JobEntity(
                    title = "Kitchen Water Pipe Leak Urgent Seal",
                    description = "Our main kitchen drainpipe has cracked and is causing slow leakage. Need an expert plumber with their own sealant/tools to come today within the next 2 hours and patch it completely. Instant payment on completion.",
                    budget = 45.0,
                    category = "Plumbing",
                    deadline = "Today",
                    location = "Nairobi CBD",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "2 hours",
                    payType = "Hourly"
                )
                val g2 = JobEntity(
                    title = "Commercial Office Solar panel wiring inspection",
                    description = "Need a technician to check the wiring linking our office's solar panels to the main inverter grid this Sunday morning. Clean the solar surfaces, inspect for any battery shorts, and run diagnostic safety reports.",
                    budget = 180.0,
                    category = "Electrical Wiring",
                    deadline = "This Sunday",
                    location = "Lekki, Lagos",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "Weekend",
                    payType = "Flat"
                )
                val g3 = JobEntity(
                    title = "Assemble 4 Flatpack Desks & Office Chairs",
                    description = "We just received 4 office study desks and ergonomic mesh chairs from a local furniture store. Need someone handy and fast with tools to assemble them completely. Manual and Allen keys are inside the boxes. Simple, straightforward task.",
                    budget = 60.0,
                    category = "Carpentry",
                    deadline = "Same Day",
                    location = "Sandton, Johannesburg",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "Same Day",
                    payType = "Flat"
                )
                val g4 = JobEntity(
                    title = "Emergency Toilet Overflow Fix & Pipe Clear",
                    description = "Urgent plumbing call: The guest restroom toilet is clogged and overflowing when flushed. Needs clearing of the obstruction with a heavy duty drain snake or mechanical auger. Hygiene is critical. Pay is premium for speed.",
                    budget = 85.0,
                    category = "Plumbing",
                    deadline = "Immediate",
                    location = "Nairobi CBD",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "1 hour",
                    payType = "Flat"
                )
                val g5 = JobEntity(
                    title = "Mount 65-inch Smart TV & Soundbar",
                    description = "Need an experienced hand to securely wall-mount a new 65-inch television onto a brick chimney breast. Please bring your own stud finder, high-quality wall anchors, drill, and level. Will also mount a matching soundbar directly underneath.",
                    budget = 55.0,
                    category = "Services",
                    deadline = "Same Day",
                    location = "Lavington, Nairobi",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "2 hours",
                    payType = "Flat"
                )
                val g6 = JobEntity(
                    title = "Urgent Backyard Leaf Raking & Grass Mowing",
                    description = "Our property's lawn is overgrown and currently covered in heavy autumn foliage. Need a quick, diligent landscaping gig helper to rake all leaves into waste bags, mow the front/back grass, and edge the walkways nicely.",
                    budget = 40.0,
                    category = "Services",
                    deadline = "Tomorrow",
                    location = "Lekki, Lagos",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "Same Day",
                    payType = "Hourly"
                )
                val g7 = JobEntity(
                    title = "Replace 4 Broken Wall Outlets & Install USB Sockets",
                    description = "Looking for a basic electrical handyman. We have 4 standard white wall outlets that have loose contacts or minor cracks. Want to swap them out for heavy duty sockets that have built-in dual USB charging ports. We have purchased the replacement outlets.",
                    budget = 70.0,
                    category = "Electrical Wiring",
                    deadline = "This Weekend",
                    location = "Sandton, Johannesburg",
                    employerId = emp1Id,
                    employerName = emp1?.fullName ?: "BuildTech Solutions",
                    employerPhoto = emp1?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "2 hours",
                    payType = "Flat"
                )
                val g8 = JobEntity(
                    title = "Android Room Database Offline Migration Help",
                    description = "Need a quick virtual pairing session with a senior Android dev. Our Room DB schema is failing to migrate from version 2 to 3 because of a new non-null foreign key constraint. Need to draft a clean SQL migration script.",
                    budget = 120.0,
                    category = "Mobile App Development",
                    deadline = "Tonight",
                    location = "Remote",
                    employerId = emp2Id,
                    employerName = emp2?.fullName ?: "Sarah Jenkins",
                    employerPhoto = emp2?.profilePhoto ?: "",
                    status = "Active",
                    isPieceWork = true,
                    timeRequired = "1 hour",
                    payType = "Hourly"
                )

                db.jobDao().insertJob(j1)
                db.jobDao().insertJob(j2)
                db.jobDao().insertJob(j3)
                db.jobDao().insertJob(j4)
                db.jobDao().insertJob(j5)
                db.jobDao().insertJob(j6)
                db.jobDao().insertJob(g1)
                db.jobDao().insertJob(g2)
                db.jobDao().insertJob(g3)
                db.jobDao().insertJob(g4)
                db.jobDao().insertJob(g5)
                db.jobDao().insertJob(g6)
                db.jobDao().insertJob(g7)
                db.jobDao().insertJob(g8)

                val item1 = ListingEntity(
                    title = "iPhone 14 Pro Max 256GB - Space Black",
                    description = "Battery health 91%. Body is in absolute flawless condition, protected with screen guard and case from day one. Unlocked to all networks. Comes with original charging cable and box. Selling because I upgraded.",
                    price = 850.0,
                    category = "Phones",
                    imageUrls = "img_phone_1",
                    sellerId = w1Id,
                    sellerName = w1?.fullName ?: "John Carpenter",
                    sellerRole = "Worker",
                    sellerPhoto = w1?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = false
                )
                val item2 = ListingEntity(
                    title = "Professional Premium Sound Bar with Subwoofer",
                    description = "Selling a slightly used Sony HT-S400 sound bar. Powerful 330W sound, 2.1 channel speaker with active subwoofer. Mind-blowing bass for movies and house parties. Supports Bluetooth and optical ARC input.",
                    price = 140.0,
                    category = "Electronics",
                    imageUrls = "img_audio_1",
                    sellerId = w2Id,
                    sellerName = w2?.fullName ?: "Lucy Sparks",
                    sellerRole = "Worker",
                    sellerPhoto = w2?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = false
                )
                val item3 = ListingEntity(
                    title = "On-site House Painting & Wall Scraping Services",
                    description = "Offering high-quality professional wall painting, mold scraping, and premium textured finish services. Standard prices starting at K100 per room. Multi-room discounts are available. Let us refresh your home aesthetic!",
                    price = 50.0,
                    category = "Services",
                    imageUrls = "img_paint_1",
                    sellerId = w3Id,
                    sellerName = w3?.fullName ?: "Sam Handy",
                    sellerRole = "Worker",
                    sellerPhoto = w3?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = true
                )
                val item4 = ListingEntity(
                    title = "Ergonomic Mesh Swivel Office Chair",
                    description = "High back rest, adjustable lumbar support, and heavy-duty smooth rotating wheels. Extremely comfortable for long hours of software development or study. Selling due to office relocation.",
                    price = 95.0,
                    category = "Furniture",
                    imageUrls = "img_chair_1",
                    sellerId = emp1Id,
                    sellerName = emp1?.fullName ?: "BuildTech Solutions",
                    sellerRole = "Employer",
                    sellerPhoto = emp1?.profilePhoto ?: "",
                    status = "Available",
                    isOfferService = false
                )

                db.listingDao().insertListing(item1)
                db.listingDao().insertListing(item2)
                db.listingDao().insertListing(item3)
                db.listingDao().insertListing(item4)

                val event1 = EventEntity(
                    title = "Nairobi Tech Expo 2026",
                    description = "Kenya's premier tech and gig workers conference. Discover high-paying remote roles, find local partners, and get hands-on training with AI and tech platforms.",
                    date = "July 24, 2026",
                    location = "Nairobi Convention Centre",
                    promoterId = 5,
                    promoterName = "Jane Doe (TechOrg)",
                    promoterPhoto = "emp1",
                    imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop",
                    savedByUserIds = "2,3"
                )
                val event2 = EventEntity(
                    title = "Giggz Community Cleanup & Gig Fair",
                    description = "Clean up the central community park and match instantly with local neighborhood employers for landscaping, painting, plumbing, and delivery jobs.",
                    date = "July 12, 2026",
                    location = "Central Park Community Hub",
                    promoterId = 6,
                    promoterName = "BuildTech Solutions",
                    promoterPhoto = "emp2",
                    imageUrl = "https://images.unsplash.com/photo-1528605248644-14dd04022da1?w=800&auto=format&fit=crop",
                    savedByUserIds = "3"
                )

                db.eventDao().insertEvent(event1)
                db.eventDao().insertEvent(event2)
            }
        }
    }
}
