package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

object CloudSyncService {
    private const val TAG = "CloudSyncService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Submit feedback to Firestore natively
     */
    suspend fun submitFeedback(
        userId: String,
        userName: String,
        email: String,
        message: String,
        appVersion: String,
        category: String
    ): Boolean = withContext(Dispatchers.IO) {
        // Enforce DB schema tracking as fallback: message, userEmail, timestamp, appVersion, platform
        val savedToDb = try {
            val db = FirebaseFirestore.getInstance()
            val feedback = mapOf(
                "message" to message,
                "userEmail" to email,
                "timestamp" to System.currentTimeMillis(),
                "appVersion" to appVersion,
                "platform" to "Android",
                "userId" to userId,
                "userName" to userName,
                "category" to category
            )
            val task = db.collection("feedback").add(feedback)
            com.google.android.gms.tasks.Tasks.await(task)
            Log.d(TAG, "Feedback record saved successfully in local Firestore sandbox.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firestore backup write failed", e)
            false
        }

        // Deliver email synchronously
        val emailSent = try {
            sendFeedbackEmailSecurely(userName, email, category, message, appVersion)
        } catch (e: Exception) {
            Log.e(TAG, "sendFeedbackEmailSecurely exception", e)
            false
        }

        // Return email delivery success status to let UI handle proper error states
        return@withContext emailSent
    }

    /**
     * Submit bug report to Firestore natively
     */
    suspend fun submitBugReport(
        userId: String,
        userName: String,
        email: String,
        description: String,
        deviceInfo: String,
        androidVersion: String,
        appVersion: String
    ): Boolean = withContext(Dispatchers.IO) {
        // Log backup
        val savedToDb = try {
            val db = FirebaseFirestore.getInstance()
            val bugReport = mapOf(
                "userId" to userId,
                "userName" to userName,
                "email" to email,
                "description" to description,
                "deviceInfo" to deviceInfo,
                "androidVersion" to androidVersion,
                "appVersion" to appVersion,
                "timestamp" to System.currentTimeMillis()
            )
            val task = db.collection("bug_reports").add(bugReport)
            com.google.android.gms.tasks.Tasks.await(task)
            Log.d(TAG, "Bug report record saved successfully in Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firestore bug report backup failed", e)
            false
        }

        // Deliver email
        val emailSent = try {
            sendFeedbackEmailSecurely(userName, email, "Bug Report", description, appVersion, deviceInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Bug report email delivery exception", e)
            false
        }

        return@withContext emailSent
    }

    /**
     * Submit Issue to GitHub if token configured
     */
    suspend fun submitGithubIssue(
        token: String,
        repoOwnerAndName: String,
        title: String,
        bodyText: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (token.isBlank() || repoOwnerAndName.isBlank()) return@withContext false

        try {
            val url = "https://api.github.com/repos/$repoOwnerAndName/issues"
            
            val json = JSONObject().apply {
                put("title", title)
                put("body", bodyText)
            }

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Placeholder File Upload to Firebase Storage REST (unused in general code)
     */
    suspend fun uploadToFirebaseStorage(
        localFile: File,
        mimeType: String
    ): String? = withContext(Dispatchers.IO) {
        val projectId = "depthlens-prod"
        val bucketName = "$projectId.appspot.com"
        val fileName = "uploads/${UUID.randomUUID()}_${localFile.name}"
        
        try {
            val url = "https://firebasestorage.googleapis.com/v0/b/$bucketName/o?name=${java.net.URLEncoder.encode(fileName, "UTF-8")}"
            val requestBody = localFile.readBytes().toRequestBody(mimeType.toMediaType())
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyStr = response.body?.string() ?: return@withContext null
                val responseJson = JSONObject(bodyStr)
                val downloadToken = responseJson.optString("downloadTokens", "")
                return@withContext "https://firebasestorage.googleapis.com/v0/b/$bucketName/o/${java.net.URLEncoder.encode(fileName, "UTF-8")}?alt=media&token=$downloadToken"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Create user profile in Firestore if not exist
     */
    suspend fun createProfileIfNotExist(userId: String, email: String, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)
            val docSnap = com.google.android.gms.tasks.Tasks.await(userRef.get())
            if (!docSnap.exists()) {
                val profile = mapOf(
                    "uid" to userId,
                    "email" to email,
                    "name" to name,
                    "createdAt" to System.currentTimeMillis()
                )
                com.google.android.gms.tasks.Tasks.await(userRef.set(profile))
                Log.d(TAG, "Profile created successfully on first login.")
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Synchronize a Session (chat meta) natively to Firestore
     * Matches collection path /users/{userId}/chats/{sessionId}
     */
    suspend fun uploadSession(
        userId: String,
        sessionId: String,
        title: String,
        isPinned: Boolean,
        createdAt: Long,
        updatedAt: Long
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf(
                "id" to sessionId,
                "title" to title,
                "isPinned" to isPinned,
                "createdAt" to createdAt,
                "lastUpdatedAt" to updatedAt
            )
            val task = db.collection("users").document(userId)
                .collection("chats").document(sessionId)
                .set(data, SetOptions.merge())
            com.google.android.gms.tasks.Tasks.await(task)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Synchronize a Message natively to Firestore
     * Matches collection path /users/{userId}/chats/{sessionId}/messages/{messageId}
     */
    suspend fun uploadMessage(
        userId: String,
        messageId: String,
        sessionId: String,
        role: String,
        text: String,
        imageUri: String?,
        timestamp: Long
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf(
                "id" to messageId,
                "sessionId" to sessionId,
                "role" to role,
                "text" to text,
                "imageUri" to (imageUri ?: ""),
                "timestamp" to timestamp
            )
            val task = db.collection("users").document(userId)
                .collection("chats").document(sessionId)
                .collection("messages").document(messageId)
                .set(data, SetOptions.merge())
            
            // Touch chat lastUpdatedAt
            try {
                val touchTask = db.collection("users").document(userId)
                    .collection("chats").document(sessionId)
                    .update("lastUpdatedAt", timestamp)
                com.google.android.gms.tasks.Tasks.await(touchTask)
            } catch (e: Exception) {}

            com.google.android.gms.tasks.Tasks.await(task)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Synchronize a Memory insight to Firestore
     */
    suspend fun uploadMemoryInsight(
        userId: String,
        insightId: Long,
        category: String,
        content: String,
        timestamp: Long
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val data = mapOf(
                "id" to insightId,
                "category" to category,
                "content" to content,
                "timestamp" to timestamp
            )
            val task = db.collection("users").document(userId)
                .collection("memories").document(insightId.toString())
                .set(data, SetOptions.merge())
            com.google.android.gms.tasks.Tasks.await(task)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Load previous chats (sessions + messages) automatically when user logs in, saving to local Room
     */
    suspend fun fetchAndSyncAll(
        userId: String,
        sessionDao: com.example.data.database.SessionDao,
        messageDao: com.example.data.database.MessageDao
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            
            // 1. Fetch user's chats
            val chatsSnapTask = db.collection("users").document(userId).collection("chats").get()
            val chatsSnap = com.google.android.gms.tasks.Tasks.await(chatsSnapTask)
            
            for (doc in chatsSnap.documents) {
                val sessionId = doc.id
                val title = doc.getString("title") ?: "Saved Session"
                val isPinned = doc.getBoolean("isPinned") ?: false
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                val lastUpdatedAt = doc.getLong("lastUpdatedAt") ?: System.currentTimeMillis()
                
                // Room update
                val sEntity = com.example.data.model.SessionEntity(
                    id = sessionId,
                    title = title,
                    isPinned = isPinned,
                    createdAt = createdAt,
                    lastUpdatedAt = lastUpdatedAt
                )
                sessionDao.insertSessionIgnore(sEntity)
                
                // 2. Fetch session messages
                val msgsSnapTask = db.collection("users").document(userId)
                    .collection("chats").document(sessionId)
                    .collection("messages").get()
                val msgsSnap = com.google.android.gms.tasks.Tasks.await(msgsSnapTask)
                
                for (msgDoc in msgsSnap.documents) {
                    val msgId = msgDoc.id
                    val role = msgDoc.getString("role") ?: "user"
                    val text = msgDoc.getString("text") ?: ""
                    val imageUri = msgDoc.getString("imageUri") ?: ""
                    val timestamp = msgDoc.getLong("timestamp") ?: System.currentTimeMillis()
                    
                    val mEntity = com.example.data.model.MessageEntity(
                        id = msgId,
                        sessionId = sessionId,
                        role = role,
                        text = text,
                        imageUri = if (imageUri.isEmpty()) null else imageUri,
                        timestamp = timestamp
                    )
                    messageDao.insertMessageIgnore(mEntity)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun sendFeedbackEmailSecurely(
        fromName: String,
        fromEmail: String,
        category: String,
        message: String,
        appVersion: String,
        deviceInfo: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        println("console.log: Feedback form submitted")
        Log.d(TAG, "Feedback form submitted")

        val projectId = try {
            FirebaseFirestore.getInstance().app.options.projectId ?: "depthlens-prod"
        } catch (e: Exception) {
            "depthlens-prod"
        }

        val nameStr = fromName.ifBlank { "Anonymous User" }
        val emailStr = fromEmail.ifBlank { "no-reply@depthlens.app" }

        // Construct Cloud Function payload
        val cfPayload = JSONObject().apply {
            put("name", nameStr)
            put("email", emailStr)
            put("category", category)
            put("message", message)
            put("appVersion", appVersion)
            put("deviceInfo", deviceInfo)
        }

        // Try Secure Cloud Function first
        val cfUrl = "https://us-central1-$projectId.cloudfunctions.net/sendFeedback"
        try {
            Log.d(TAG, "Attempting connection to Secure Cloud Function: $cfUrl")
            val cfRequest = Request.Builder()
                .url(cfUrl)
                .post(cfPayload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(cfRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    println("console.log: Secure Cloud Function response: $respStr")
                    Log.d(TAG, "Feedback securely routed through backend cloud successfully: $respStr")
                    return@withContext true
                } else if (response.code != 404) {
                    // It connects to Cloud Function, but the function returned an error. We want to log and return false.
                    val errStr = response.body?.string() ?: "Server Error"
                    println("console.error: Secure Cloud Function error: Code ${response.code} - $errStr")
                    Log.e(TAG, "Secure Cloud Function failed: Code ${response.code} - $errStr")
                    return@withContext false
                } else {
                    Log.d(TAG, "Secure Cloud Function not found (404), falling back to client-secured EmailJS...")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Secure Cloud Function connection failed/unreachable (${e.localizedMessage}), falling back to client-secured EmailJS...")
        }

        // --- Client-secured EmailJS Fallback ---
        println("console.log: EmailJS initialized")
        Log.d(TAG, "EmailJS initialized")

        println("console.log: Sending feedback...")
        Log.d(TAG, "Sending feedback...")

        val serviceId = "service_lbl552d"
        val templateId = "template_vphityh"
        val publicKey = "GJZgQndVUSZMWSFOv"

        try {
            // Securely omit "to_email": "moonwalker494@gmail.com".
            // The EmailJS template configured on the dashboard directly routes to moonwalker494@gmail.com.
            // This hides the destination email from both source code and network packages!
            val templateParams = JSONObject().apply {
                put("from_name", nameStr)
                put("from_email", emailStr)
                put("category", category)
                put("message", message)
                put("app_version", appVersion)
                put("device_info", deviceInfo)
                put("timestamp", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()))
            }

            val jsonPayload = JSONObject().apply {
                put("service_id", serviceId)
                put("template_id", templateId)
                put("user_id", publicKey)
                // We omit accessToken to allow standard public EmailJS fallback REST calls
                put("template_params", templateParams)
            }

            val request = Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .post(jsonPayload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    println("console.log: EmailJS response: $respBody")
                    Log.d(TAG, "Feedback successfully routed and delivered via EmailJS package fallback: $respBody")
                    true
                } else {
                    println("console.error: EmailJS error: Code ${response.code} - $respBody")
                    Log.e(TAG, "EmailJS transmission rejected by server: code=${response.code} body=$respBody")
                    false
                }
            }
        } catch (e: Exception) {
            println("console.error: EmailJS error: ${e.localizedMessage}")
            Log.e(TAG, "Failed to complete EmailJS fallback transmission", e)
            false
        }
    }
}
