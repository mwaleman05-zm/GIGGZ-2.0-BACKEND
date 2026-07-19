package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// =============================================================================
// ROOM ENTITIES
// =============================================================================

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String,
    val role: String, // "Worker", "Employer", "Admin"
    val fullName: String = "",
    val profilePhoto: String = "", // URL or local asset key
    val nationality: String = "",
    val location: String = "",
    val phone: String = "",
    val skills: String = "", // Comma-separated list
    val experience: Int = 0, // In years
    val portfolio: String = "", // Comma-separated list of image references
    val cvPath: String = "", // CV filename or mock PDF reference
    val bio: String = "",
    val availabilityStatus: String = "Available", // "Available", "Busy", "Offline"
    val rating: Float = 4.8f,
    val completedJobs: Int = 0,
    val reviewsCount: Int = 0,
    val isApproved: Boolean = true,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val budget: Double,
    val category: String,
    val deadline: String,
    val location: String,
    val employerId: Int,
    val employerName: String,
    val employerPhoto: String = "",
    val status: String = "Active", // "Active", "Completed", "Cancelled", "Accepted"
    val isPieceWork: Boolean = false,
    val timeRequired: String = "", // "2 hours", "Same Day", "Weekend", etc.
    val payType: String = "Flat", // "Flat", "Hourly"
    val images: String = "", // Comma-separated images
    val recommendedBudget: Double = 0.0, // Recommended worker budget / suggestion
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    val jobTitle: String,
    val employerId: Int,
    val workerId: Int,
    val workerName: String,
    val workerPhoto: String = "",
    val coverLetter: String = "",
    val pdfPath: String = "", // CV name attached
    val imagePath: String = "", // optional picture
    val status: String = "Pending", // "Pending", "Accepted", "Completed", "Cancelled"
    val appliedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: Int,
    val senderName: String,
    val receiverId: Int,
    val messageText: String,
    val mediaPath: String = "",
    val mediaType: String = "text", // "text", "image", "pdf", "location", "voice"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val replyToId: Int? = null,
    val replyToText: String? = null,
    val reactions: String? = null, // Comma-separated or JSON string, e.g., "👍,❤️"
    val isDeleted: Boolean = false,
    val deliveryStatus: String = "Read" // "Sending", "Sent", "Delivered", "Read"
)

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrls: String = "", // Comma-separated images
    val sellerId: Int,
    val sellerName: String,
    val sellerRole: String = "Worker",
    val sellerPhoto: String = "",
    val status: String = "Available", // "Available", "Sold"
    val isOfferService: Boolean = false, // Service vs Product trade
    val interestedUserIds: String = "", // Comma-separated user IDs who expressed interest
    val condition: String = "", // "Brand New", "Slightly Used", "Second Hand"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val senderId: Int? = null,
    val title: String,
    val message: String,
    val category: String, // "job", "message", "admin", "promo"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val type: String = "push", // "push", "email", "app"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reporterId: Int,
    val reporterName: String,
    val subject: String,
    val description: String,
    val reportedType: String, // "user", "listing", "job"
    val reportedId: Int,
    val status: String = "Pending", // "Pending", "Resolved"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val promoterId: Int,
    val promoterName: String,
    val promoterPhoto: String = "",
    val imageUrl: String = "", // Suitable for CDN
    val savedByUserIds: String = "", // Comma-separated user IDs who saved this
    val timestamp: Long = System.currentTimeMillis(),
    
    // Upgraded Fields for Premium Event System
    val category: String = "Community", // "Music", "Education", "Community", "Sports", "Parties", "Church", "Business"
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isVerified: Boolean = false,
    val goingUserIds: String = "", // Comma-separated user IDs who are going
    val interestedUserIds: String = "", // Comma-separated user IDs who are interested
    val imageGallery: String = "", // Comma-separated additional images (up to 5)
    val videoUrl: String = "", // Optional short video link
    val socialLinks: String = "" // Comma-separated social media platform links (Instagram, Facebook, etc.)
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    val jobTitle: String,
    val reviewerId: Int,
    val reviewerName: String,
    val reviewerPhoto: String = "",
    val revieweeId: Int,
    val revieweeName: String,
    
    // Ratings
    val rating1: Float, // Quality of work / Fairness of payment
    val rating2: Float, // Punctuality / Clarity of instructions
    val rating3: Float, // Communication / Respect
    val rating4: Float, // Professionalism / Responsiveness
    
    val averageRating: Float,
    val comment: String,
    val imageUrl: String = "", // Optional photo proof
    val isFromWorker: Boolean, // True if Worker reviews Client, False if Client reviews Worker
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "journals")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val content: String,
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_jobs_history")
data class DailyJobsHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String,
    val jobsCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)



// =============================================================================
// ROOM DAOS
// =============================================================================

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY joinedAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = 'Worker' ORDER BY rating DESC")
    fun getAllWorkersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isApproved = :approved WHERE id = :id")
    suspend fun updateUserApproval(id: Int, approved: Boolean)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Int)
}

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAllJobsFlow(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE employerId = :employerId ORDER BY createdAt DESC")
    fun getJobsByEmployerFlow(employerId: Int): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE isPieceWork = 1 ORDER BY createdAt DESC")
    fun getPieceWorksFlow(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity): Long

    @Update
    suspend fun updateJob(job: JobEntity)

    @Query("UPDATE jobs SET status = :status WHERE id = :id")
    suspend fun updateJobStatus(id: Int, status: String)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteJob(id: Int)

    @Query("DELETE FROM jobs")
    suspend fun deleteAllJobs()
}

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY appliedAt DESC")
    fun getAllApplicationsFlow(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE workerId = :workerId ORDER BY appliedAt DESC")
    fun getApplicationsByWorkerFlow(workerId: Int): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE employerId = :employerId ORDER BY appliedAt DESC")
    fun getApplicationsByEmployerFlow(employerId: Int): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE jobId = :jobId ORDER BY appliedAt DESC")
    fun getApplicationsForJobFlow(jobId: Int): Flow<List<ApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(app: ApplicationEntity): Long

    @Update
    suspend fun updateApplication(app: ApplicationEntity)

    @Query("UPDATE applications SET status = :status WHERE id = :id")
    suspend fun updateApplicationStatus(id: Int, status: String)

    @Query("DELETE FROM applications WHERE id = :id")
    suspend fun deleteApplication(id: Int)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    fun getChatMessagesFlow(userId1: Int, userId2: Int): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadMessagesCountFlow(userId: Int): Flow<Int>

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getAllMessagesFlowForUser(userId: Int): Flow<List<MessageEntity>>

    @Query("SELECT DISTINCT CASE WHEN senderId = :userId THEN receiverId ELSE senderId END FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getChatPartnersFlow(userId: Int): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :receiverId AND senderId = :senderId")
    suspend fun markMessagesAsRead(senderId: Int, receiverId: Int)

    @Query("UPDATE messages SET reactions = :reactions WHERE id = :messageId")
    suspend fun updateMessageReactions(messageId: Int, reactions: String?)

    @Query("UPDATE messages SET isDeleted = 1, messageText = 'This message was deleted' WHERE id = :messageId")
    suspend fun deleteMessageForEveryone(messageId: Int)

    @Query("DELETE FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1)")
    suspend fun deleteChatMessages(userId1: Int, userId2: Int)
}

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings ORDER BY timestamp DESC")
    fun getAllListingsFlow(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE sellerId = :sellerId ORDER BY timestamp DESC")
    fun getListingsBySellerFlow(sellerId: Int): Flow<List<ListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity): Long

    @Update
    suspend fun updateListing(listing: ListingEntity)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListing(id: Int)

    @Query("DELETE FROM listings")
    suspend fun deleteAllListings()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsFlow(userId: Int): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isArchived = 0 ORDER BY timestamp DESC")
    fun getActiveNotificationsFlow(userId: Int): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0 AND isArchived = 0")
    fun getUnreadCountFlow(userId: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Int)

    @Query("UPDATE notifications SET isArchived = 1 WHERE userId = :userId")
    suspend fun archiveAll(userId: Int)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifications SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Int)

    @Query("DELETE FROM notifications WHERE timestamp < :thresholdTime")
    suspend fun deleteNotificationsOlderThan(thresholdTime: Long)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Query("UPDATE reports SET status = :status WHERE id = :id")
    suspend fun updateReportStatus(id: Int, status: String)
}


@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEvent(id: Int)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviewsFlow(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE revieweeId = :userId ORDER BY timestamp DESC")
    fun getReviewsForUserFlow(userId: Int): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journals WHERE userId = :userId ORDER BY dateCreated DESC")
    fun getJournalsFlow(userId: Int): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: JournalEntity): Long

    @Query("DELETE FROM journals WHERE id = :id")
    suspend fun deleteJournal(id: Int)
}

@Dao
interface DailyJobsHistoryDao {
    @Query("SELECT * FROM daily_jobs_history ORDER BY timestamp ASC")
    fun getDailyJobsHistoryFlow(): Flow<List<DailyJobsHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyJobsHistory(entity: DailyJobsHistoryEntity): Long

    @Query("DELETE FROM daily_jobs_history")
    suspend fun clearAll()
}



// =============================================================================
// ROOM DATABASE
// =============================================================================

@Database(
    entities = [
        UserEntity::class,
        JobEntity::class,
        ApplicationEntity::class,
        MessageEntity::class,
        ListingEntity::class,
        NotificationEntity::class,
        AnnouncementEntity::class,
        ReportEntity::class,
        EventEntity::class,
        ReviewEntity::class,
        JournalEntity::class,
        DailyJobsHistoryEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class GiggzDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun messageDao(): MessageDao
    abstract fun listingDao(): ListingDao
    abstract fun notificationDao(): NotificationDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun reportDao(): ReportDao
    abstract fun eventDao(): EventDao
    abstract fun reviewDao(): ReviewDao
    abstract fun journalDao(): JournalDao
    abstract fun dailyJobsHistoryDao(): DailyJobsHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: GiggzDatabase? = null

        fun getDatabase(context: Context): GiggzDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GiggzDatabase::class.java,
                    "giggz_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
