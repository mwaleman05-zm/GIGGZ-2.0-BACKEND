package com.example.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

/**
 * Production-ready Firebase Backend Integration Service for Giggz.
 * Handles:
 * 1. Firebase Authentication state mapping.
 * 2. Real-time Cloud Firestore updates via Flow snapshot listeners.
 * 3. Media asset uploads (CV, Portfolios, Listings) to Firebase Cloud Storage.
 */
class FirebaseGiggzService {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    /**
     * Safely checks if the default Firebase app is initialized.
     */
    fun isAvailable(): Boolean {
        return try {
            com.google.firebase.FirebaseApp.getInstance()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val TAG = "FirebaseGiggzService"
        
        // Firestore Collection Keys
        private const val COLL_USERS = "users"
        private const val COLL_JOBS = "jobs"
        private const val COLL_APPLICATIONS = "applications"
        private const val COLL_MESSAGES = "messages"
        private const val COLL_LISTINGS = "listings"
        private const val COLL_NOTIFICATIONS = "notifications"
        private const val COLL_REPORTS = "reports"
        private const val COLL_EVENTS = "events"
        private const val COLL_REVIEWS = "reviews"
        private const val COLL_JOURNALS = "journals"
    }

    // =============================================================================
    // AUTHENTICATION OPERATIONS
    // =============================================================================

    /**
     * Get the currently logged-in user's UID.
     */
    val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Observes current login state in real-time.
     */
    fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Log out current user from Firebase.
     */
    fun logout() {
        auth.signOut()
    }

    // =============================================================================
    // FIRESTORE OPERATIONS (Real-time Flows)
    // =============================================================================

    /**
     * Real-time stream of all jobs, sorted by creation date.
     */
    fun observeAllJobs(): Flow<List<JobEntity>> = callbackFlow {
        val registration = firestore.collection(COLL_JOBS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to jobs: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val jobs = snapshot.documents.mapNotNull { doc ->
                        // Manual mapping to retain Room Compatibility
                        val idStr = doc.id
                        val idInt = idStr.hashCode() // Map UUID string to Int safely
                        JobEntity(
                            id = idInt,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            budget = doc.getDouble("budget") ?: 0.0,
                            category = doc.getString("category") ?: "",
                            deadline = doc.getString("deadline") ?: "",
                            location = doc.getString("location") ?: "",
                            employerId = doc.getLong("employerId")?.toInt() ?: 0,
                            employerName = doc.getString("employerName") ?: "",
                            employerPhoto = doc.getString("employerPhoto") ?: "",
                            status = doc.getString("status") ?: "Active",
                            isPieceWork = doc.getBoolean("isPieceWork") ?: false,
                            timeRequired = doc.getString("timeRequired") ?: "",
                            payType = doc.getString("payType") ?: "Flat",
                            images = doc.getString("images") ?: "",
                            recommendedBudget = doc.getDouble("recommendedBudget") ?: 0.0,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    }
                    trySend(jobs)
                }
            }
        awaitClose { registration.remove() }
    }

    /**
     * Observe real-time direct chat messages between two users.
     */
    fun observeChatMessages(userId1: String, userId2: String): Flow<List<MessageEntity>> = callbackFlow {
        // Since Firestore doesn't support multiple OR queries natively, we pull messages involving both
        // and filter client-side, or use combined chat-room IDs.
        val registration = firestore.collection(COLL_MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to messages: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        val sender = doc.getString("senderId") ?: ""
                        val receiver = doc.getString("receiverId") ?: ""
                        
                        // Client-side direct message filter
                        if ((sender == userId1 && receiver == userId2) || (sender == userId2 && receiver == userId1)) {
                            MessageEntity(
                                id = doc.id.hashCode(),
                                senderId = sender.hashCode(),
                                senderName = doc.getString("senderName") ?: "",
                                receiverId = receiver.hashCode(),
                                messageText = doc.getString("messageText") ?: "",
                                mediaPath = doc.getString("mediaPath") ?: "",
                                mediaType = doc.getString("mediaType") ?: "text",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                isRead = doc.getBoolean("isRead") ?: false,
                                replyToId = doc.getLong("replyToId")?.toInt(),
                                replyToText = doc.getString("replyToText"),
                                reactions = doc.getString("reactions"),
                                isDeleted = doc.getBoolean("isDeleted") ?: false,
                                deliveryStatus = doc.getString("deliveryStatus") ?: "Sent"
                            )
                        } else null
                    }
                    trySend(messages)
                }
            }
        awaitClose { registration.remove() }
    }

    /**
     * Saves a new job to Firebase Firestore.
     */
    fun saveJob(job: JobEntity, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val jobMap = hashMapOf(
            "title" to job.title,
            "description" to job.description,
            "budget" to job.budget,
            "category" to job.category,
            "deadline" to job.deadline,
            "location" to job.location,
            "employerId" to job.employerId,
            "employerName" to job.employerName,
            "employerPhoto" to job.employerPhoto,
            "status" to job.status,
            "isPieceWork" to job.isPieceWork,
            "timeRequired" to job.timeRequired,
            "payType" to job.payType,
            "images" to job.images,
            "recommendedBudget" to job.recommendedBudget,
            "createdAt" to job.createdAt
        )

        firestore.collection(COLL_JOBS)
            .add(jobMap)
            .addOnSuccessListener { ref -> onSuccess(ref.id) }
            .addOnFailureListener { e -> onFailure(e) }
    }

    /**
     * Sends a chat message in real-time.
     */
    fun sendMessage(msg: MessageEntity, senderUidStr: String, receiverUidStr: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val messageMap = hashMapOf(
            "senderId" to senderUidStr,
            "senderName" to msg.senderName,
            "receiverId" to receiverUidStr,
            "messageText" to msg.messageText,
            "mediaPath" to msg.mediaPath,
            "mediaType" to msg.mediaType,
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false,
            "replyToId" to msg.replyToId,
            "replyToText" to msg.replyToText,
            "reactions" to msg.reactions,
            "isDeleted" to false,
            "deliveryStatus" to "Sent"
        )

        firestore.collection(COLL_MESSAGES)
            .add(messageMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // =============================================================================
    // CLOUD STORAGE OPERATIONS (CV & Media Uploads)
    // =============================================================================

    /**
     * Upload profile avatar or portfolio reference photo.
     */
    fun uploadUserMedia(
        userId: String,
        fileUri: Uri,
        isAvatar: Boolean,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val path = if (isAvatar) "profiles/$userId/avatar.jpg" else "profiles/$userId/portfolio/${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(path)

        ref.putFile(fileUri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    /**
     * Upload resume PDF to secure CV directory.
     */
    fun uploadResumePdf(
        userId: String,
        pdfFile: File,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val ref = storage.reference.child("cvs/$userId/resume.pdf")
        val fileUri = Uri.fromFile(pdfFile)

        ref.putFile(fileUri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
