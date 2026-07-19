# Giggz Firebase Backend & Architecture Schema Blueprint

This document outlines the complete **Firebase Backend Structure** designed to match the `Giggz` local database schema. When you are ready to migrate from local Room persistence to an online cloud database, use this document to structure your **Firebase Authentication**, **Cloud Firestore (NoSQL)**, **Cloud Storage**, and **Firestore Security Rules**.

---

## 1. Firebase Authentication Schema
Giggz uses role-based access control (Admin, Employer, Worker). Firebase Auth handles user creation, and custom user claims or Firestore user profiles manage user roles.

- **Authentication Providers:**
  - Email & Password (Primary)
  - Google Sign-In (Optional)
- **User Record Metadata:**
  - `uid`: Unique User ID (serves as the Document ID in the `users` Firestore collection).
  - `email`: User's primary email.
  - `displayName`: Full Name.
  - `photoURL`: Profile picture link.

---

## 2. Cloud Firestore Schema (NoSQL Collections)

Here is the exact document structure for each of the Firestore collections, maintaining 100% feature parity with your Android Room schemas:

### Collection: `users`
*Document ID: `uid` (matching Firebase Auth UID)*

```json
{
  "id": "user_uid_12345",
  "email": "mwaleman05@gmail.com",
  "role": "Worker",            // Enum: "Worker", "Employer", "Admin"
  "fullName": "Mwaleman",
  "profilePhoto": "https://firebasestorage.googleapis.com/.../avatars/user_12345.jpg",
  "nationality": "Kenyan",
  "location": "Nairobi, Kenya",
  "phone": "+254712345678",
  "skills": ["Kotlin", "Jetpack Compose", "UI/UX Design", "Firebase"],
  "experience": 5,             // In years
  "portfolio": [
    "https://firebasestorage.googleapis.com/.../portfolio/item1.jpg",
    "https://firebasestorage.googleapis.com/.../portfolio/item2.jpg"
  ],
  "cvPath": "https://firebasestorage.googleapis.com/.../cvs/resume.pdf",
  "bio": "Expert Android Engineer focusing on Kotlin and Material 3 design systems.",
  "availabilityStatus": "Available", // Enum: "Available", "Busy", "Offline"
  "rating": 4.8,
  "completedJobs": 12,
  "reviewsCount": 4,
  "isApproved": true,
  "joinedAt": 1783928427014    // timestamp (milliseconds)
}
```

### Collection: `jobs`
*Document ID: Auto-generated UUID*

```json
{
  "id": "job_id_abcde",
  "title": "Build Android Chat Screen",
  "description": "We need a Jetpack Compose chatting screen integrated with edge-to-edge keyboard avoidance.",
  "budget": 250.0,
  "category": "Mobile App Development",
  "deadline": "2026-07-20",
  "location": "Remote",
  "employerId": "user_employer_uid_99",
  "employerName": "Giggz Recruitments",
  "employerPhoto": "https://firebasestorage.googleapis.com/.../logos/giggz.jpg",
  "status": "Active",         // Enum: "Active", "Completed", "Cancelled", "Accepted"
  "isPieceWork": false,
  "timeRequired": "3 Days",
  "payType": "Flat",          // Enum: "Flat", "Hourly"
  "images": [
    "https://firebasestorage.googleapis.com/.../jobs/spec1.png"
  ],
  "recommendedBudget": 220.0,
  "createdAt": 1783928427100
}
```

### Collection: `applications`
*Document ID: Auto-generated UUID*

```json
{
  "id": "app_id_98765",
  "jobId": "job_id_abcde",
  "jobTitle": "Build Android Chat Screen",
  "employerId": "user_employer_uid_99",
  "workerId": "user_uid_12345",
  "workerName": "Mwaleman",
  "workerPhoto": "https://firebasestorage.googleapis.com/.../avatars/user_12345.jpg",
  "coverLetter": "Hi, I am highly experienced in Jetpack Compose and keyboard-avoidance systems.",
  "pdfPath": "https://firebasestorage.googleapis.com/.../cvs/resume.pdf",
  "imagePath": "",
  "status": "Pending",        // Enum: "Pending", "Accepted", "Completed", "Cancelled"
  "appliedAt": 1783928428200
}
```

### Collection: `messages`
*Document ID: Auto-generated UUID*

```json
{
  "id": "msg_id_67890",
  "senderId": "user_uid_12345",
  "senderName": "Mwaleman",
  "receiverId": "user_employer_uid_99",
  "messageText": "Hello! I updated the design of the paper jet icon. Let me know what you think.",
  "mediaPath": "",
  "mediaType": "text",        // Enum: "text", "image", "pdf", "location", "voice"
  "timestamp": 1783928429000,
  "isRead": false,
  "replyToId": null,
  "replyToText": null,
  "reactions": ["👍"],        // Array of strings
  "isDeleted": false,
  "deliveryStatus": "Sent"   // Enum: "Sending", "Sent", "Delivered", "Read"
}
```

### Collection: `listings` (Marketplace)
*Document ID: Auto-generated UUID*

```json
{
  "id": "listing_id_xyz",
  "title": "Ergonomic Office Chair",
  "description": "Premium black mesh office chair. Perfect for developers.",
  "price": 120.0,
  "category": "Furniture",
  "imageUrls": [
    "https://firebasestorage.googleapis.com/.../listings/chair1.jpg"
  ],
  "sellerId": "user_uid_12345",
  "sellerName": "Mwaleman",
  "sellerRole": "Worker",
  "sellerPhoto": "https://firebasestorage.googleapis.com/.../avatars/user_12345.jpg",
  "status": "Available",      // Enum: "Available", "Sold"
  "isOfferService": false,    // False = Product sale, True = Service offering
  "interestedUserIds": [],    // Array of user UIDs
  "condition": "Slightly Used", // Enum: "Brand New", "Slightly Used", "Second Hand"
  "timestamp": 1783928429500
}
```

### Collection: `notifications`
*Document ID: Auto-generated UUID*

```json
{
  "id": "notif_id_333",
  "userId": "user_uid_12345",
  "senderId": "user_employer_uid_99",
  "title": "Application Accepted",
  "message": "Congratulations! Your application for Build Android Chat Screen has been accepted.",
  "category": "job",          // Enum: "job", "message", "admin", "promo"
  "timestamp": 1783928430000,
  "isRead": false,
  "isFavorite": false,
  "isArchived": false
}
```

### Collection: `reports`
*Document ID: Auto-generated UUID*

```json
{
  "id": "report_id_444",
  "reporterId": "user_uid_12345",
  "reporterName": "Mwaleman",
  "subject": "Inappropriate Listing",
  "description": "The item listing contains spam content.",
  "reportedType": "listing",  // Enum: "user", "listing", "job"
  "reportedId": "listing_id_xyz",
  "status": "Pending",        // Enum: "Pending", "Resolved"
  "timestamp": 1783928431000
}
```

---

## 3. Firestore Security Rules (`firestore.rules`)

To keep your backend secure, paste the following rules into the **Rules** tab of your Firebase Console. These enforce clean, strict, and production-ready role permissions.

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }

    function isOwner(userId) {
      return request.auth.uid == userId;
    }

    function getUserData() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid)).data;
    }

    function isAdmin() {
      return isAuthenticated() && getUserData().role == 'Admin';
    }

    function isEmployer() {
      return isAuthenticated() && getUserData().role == 'Employer';
    }

    // USERS COLLECTION
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && isOwner(userId);
      allow update: if isAuthenticated() && (isOwner(userId) || isAdmin());
      allow delete: if isAdmin();
    }

    // JOBS COLLECTION
    match /jobs/{jobId} {
      allow read: if isAuthenticated();
      allow create: if isEmployer() || isAdmin();
      allow update: if isAuthenticated() && (request.resource.data.employerId == request.auth.uid || isAdmin());
      allow delete: if isAuthenticated() && (resource.data.employerId == request.auth.uid || isAdmin());
    }

    // APPLICATIONS COLLECTION
    match /applications/{appId} {
      allow read: if isAuthenticated() && (
        resource.data.workerId == request.auth.uid || 
        resource.data.employerId == request.auth.uid || 
        isAdmin()
      );
      allow create: if isAuthenticated();
      allow update: if isAuthenticated() && (
        resource.data.workerId == request.auth.uid || 
        resource.data.employerId == request.auth.uid || 
        isAdmin()
      );
      allow delete: if isAdmin();
    }

    // MESSAGES COLLECTION
    match /messages/{msgId} {
      allow read: if isAuthenticated() && (
        resource.data.senderId == request.auth.uid || 
        resource.data.receiverId == request.auth.uid
      );
      allow create: if isAuthenticated() && request.resource.data.senderId == request.auth.uid;
      allow update: if isAuthenticated() && (
        resource.data.senderId == request.auth.uid || 
        resource.data.receiverId == request.auth.uid
      );
      allow delete: if isAuthenticated() && resource.data.senderId == request.auth.uid;
    }

    // LISTINGS COLLECTION
    match /listings/{listingId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update: if isAuthenticated() && (resource.data.sellerId == request.auth.uid || isAdmin());
      allow delete: if isAuthenticated() && (resource.data.sellerId == request.auth.uid || isAdmin());
    }

    // NOTIFICATIONS COLLECTION
    match /notifications/{notifId} {
      allow read: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow write: if isAuthenticated() && (request.resource.data.userId == request.auth.uid || isAdmin());
    }

    // REPORTS COLLECTION
    match /reports/{reportId} {
      allow read, write: if isAdmin();
      allow create: if isAuthenticated();
    }

    // EVENTS COLLECTION
    match /events/{eventId} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated(); // Allow users to RSVP or join events
      allow create: if isAuthenticated();
      allow delete: if isAdmin() || resource.data.promoterId == request.auth.uid;
    }

    // REVIEWS COLLECTION
    match /reviews/{reviewId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update, delete: if isAdmin();
    }

    // JOURNALS COLLECTION
    match /journals/{journalId} {
      allow read, write: if isAuthenticated() && resource.data.userId == request.auth.uid;
    }
  }
}
```

---

## 4. Firebase Cloud Storage Bucket Structures

All media assets (CV PDFs, profile pictures, job references, and marketplace listings) are organized in structured buckets.

### File Directory Tree:
```text
gs://giggz-app.appspot.com/
├── profiles/
│   └── {userId}/
│       ├── avatar.jpg
│       └── portfolio/
│           ├── item_1.jpg
│           └── item_2.jpg
├── cvs/
│   └── {userId}/
│       └── resume.pdf
├── jobs/
│   └── {jobId}/
│       └── reference_spec.png
├── marketplace/
│   └── {listingId}/
│       ├── item_image_1.jpg
│       └── item_image_2.jpg
└── chats/
    └── {chatRoomId}/
        ├── audio_1.3gp
        └── snapshot_1.png
```

### Storage Security Rules:
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // Helpers
    function isAuthenticated() {
      return request.auth != null;
    }
    function isOwner(userId) {
      return request.auth.uid == userId;
    }

    // Profile photos & portfolio
    match /profiles/{userId}/{allPaths=**} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && isOwner(userId);
    }

    // CV resumes (PDFs)
    match /cvs/{userId}/{allPaths=**} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && isOwner(userId);
    }

    // Job specs and images
    match /jobs/{jobId}/{allPaths=**} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated();
    }

    // Marketplace item listing images
    match /marketplace/{listingId}/{allPaths=**} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated();
    }

    // Direct chat media attachments
    match /chats/{chatRoomId}/{allPaths=**} {
      allow read, write: if isAuthenticated();
    }
  }
}
```
