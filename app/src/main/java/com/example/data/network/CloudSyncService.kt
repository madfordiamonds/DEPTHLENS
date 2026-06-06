package com.example.data.network

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
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

    // Helper to get configuration
    private fun getFirebaseApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key.isNotEmpty() && key != "MY_GEMINI_API_KEY") key else ""
    }

    private fun getProjectId(): String {
        return "depthlens-prod" // Default fallback project ID
    }

    /**
     * Submit feedback to Firestore
     */
    suspend fun submitFeedback(
        userId: String,
        userName: String,
        email: String,
        message: String,
        appVersion: String,
        category: String
    ): Boolean = withContext(Dispatchers.IO) {
        val apiKey = getFirebaseApiKey()
        val projectId = getProjectId()
        if (apiKey.isEmpty()) {
            Log.e(TAG, "Missing API Key for Firestore submittal")
            return@withContext false
        }

        try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/feedback?key=$apiKey"
            
            val json = JSONObject().apply {
                val fields = JSONObject().apply {
                    put("userId", JSONObject().put("stringValue", userId))
                    put("userName", JSONObject().put("stringValue", userName))
                    put("email", JSONObject().put("stringValue", email))
                    put("message", JSONObject().put("stringValue", message))
                    put("timestamp", JSONObject().put("integerValue", System.currentTimeMillis().toString()))
                    put("appVersion", JSONObject().put("stringValue", appVersion))
                    put("category", JSONObject().put("stringValue", category))
                }
                put("fields", fields)
            }

            val request = Request.Builder()
                .url(url)
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Firestore write failed: ${response.code} ${response.message}")
                    return@withContext false
                }
                Log.d(TAG, "Feedback submitted successfully to Firestore")
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Submit bug report to Firestore
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
        val apiKey = getFirebaseApiKey()
        val projectId = getProjectId()
        if (apiKey.isEmpty()) return@withContext false

        try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/bug_reports?key=$apiKey"

            val json = JSONObject().apply {
                val fields = JSONObject().apply {
                    put("userId", JSONObject().put("stringValue", userId))
                    put("userName", JSONObject().put("stringValue", userName))
                    put("email", JSONObject().put("stringValue", email))
                    put("description", JSONObject().put("stringValue", description))
                    put("deviceInfo", JSONObject().put("stringValue", deviceInfo))
                    put("androidVersion", JSONObject().put("stringValue", androidVersion))
                    put("appVersion", JSONObject().put("stringValue", appVersion))
                    put("timestamp", JSONObject().put("integerValue", System.currentTimeMillis().toString()))
                }
                put("fields", fields)
            }

            val request = Request.Builder()
                .url(url)
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Firestore bug report failed: ${response.code}")
                    return@withContext false
                }
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Submit Issue to GitHub if token configured
     */
    suspend fun submitGithubIssue(
        token: String,
        repoOwnerAndName: String, // e.g. "myname/myrepo"
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
                if (!response.isSuccessful) {
                    Log.e(TAG, "GitHub Issue failed: ${response.code}")
                    return@withContext false
                }
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Upload File to Firebase Storage
     */
    suspend fun uploadToFirebaseStorage(
        localFile: File,
        mimeType: String
    ): String? = withContext(Dispatchers.IO) {
        val projectId = getProjectId()
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
                if (!response.isSuccessful) {
                    Log.e(TAG, "Storage upload failed: ${response.code}")
                    return@withContext null
                }
                val bodyStr = response.body?.string() ?: return@withContext null
                val responseJson = JSONObject(bodyStr)
                val downloadToken = responseJson.optString("downloadTokens", "")
                
                // Return accessible download URL
                return@withContext "https://firebasestorage.googleapis.com/v0/b/$bucketName/o/${java.net.URLEncoder.encode(fileName, "UTF-8")}?alt=media&token=$downloadToken"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Synchronize a Session to Firestore
     */
    suspend fun uploadSession(userId: String, sessionId: String, title: String, isPinned: Boolean, createdAt: Long, updatedAt: Long): Boolean = withContext(Dispatchers.IO) {
        val apiKey = getFirebaseApiKey()
        val projectId = getProjectId()
        if (apiKey.isEmpty() || userId.isEmpty()) return@withContext false

        try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/users/$userId/sessions/$sessionId?key=$apiKey"
            
            val json = JSONObject().apply {
                val fields = JSONObject().apply {
                    put("id", JSONObject().put("stringValue", sessionId))
                    put("title", JSONObject().put("stringValue", title))
                    put("isPinned", JSONObject().put("booleanValue", isPinned))
                    put("createdAt", JSONObject().put("integerValue", createdAt.toString()))
                    put("lastUpdatedAt", JSONObject().put("integerValue", updatedAt.toString()))
                }
                put("fields", fields)
            }

            val request = Request.Builder()
                .url(url)
                .patch(json.toString().toRequestBody(jsonMediaType)) // Use PATCH to create/overwrite
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
     * Synchronize a Message to Firestore
     */
    suspend fun uploadMessage(userId: String, messageId: String, sessionId: String, role: String, text: String, imageUri: String?, timestamp: Long): Boolean = withContext(Dispatchers.IO) {
        val apiKey = getFirebaseApiKey()
        val projectId = getProjectId()
        if (apiKey.isEmpty() || userId.isEmpty()) return@withContext false

        try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/users/$userId/messages/$messageId?key=$apiKey"
            
            val json = JSONObject().apply {
                val fields = JSONObject().apply {
                    put("id", JSONObject().put("stringValue", messageId))
                    put("sessionId", JSONObject().put("stringValue", sessionId))
                    put("role", JSONObject().put("stringValue", role))
                    put("text", JSONObject().put("stringValue", text))
                    put("imageUri", JSONObject().put("stringValue", imageUri ?: ""))
                    put("timestamp", JSONObject().put("integerValue", timestamp.toString()))
                }
                put("fields", fields)
            }

            val request = Request.Builder()
                .url(url)
                .patch(json.toString().toRequestBody(jsonMediaType))
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
     * Synchronize a Memory insight to Firestore
     */
    suspend fun uploadMemoryInsight(userId: String, insightId: Long, category: String, content: String, timestamp: Long): Boolean = withContext(Dispatchers.IO) {
        val apiKey = getFirebaseApiKey()
        val projectId = getProjectId()
        if (apiKey.isEmpty() || userId.isEmpty()) return@withContext false

        try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/users/$userId/memories/$insightId?key=$apiKey"
            
            val json = JSONObject().apply {
                val fields = JSONObject().apply {
                    put("id", JSONObject().put("integerValue", insightId.toString()))
                    put("category", JSONObject().put("stringValue", category))
                    put("content", JSONObject().put("stringValue", content))
                    put("timestamp", JSONObject().put("integerValue", timestamp.toString()))
                }
                put("fields", fields)
            }

            val request = Request.Builder()
                .url(url)
                .patch(json.toString().toRequestBody(jsonMediaType))
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
     * Retrieve all messages synced in Firebase
     */
    suspend fun fetchCloudMessages(userId: String): List<JSONObject> = withContext(Dispatchers.IO) {
        val apiKey = getFirebaseApiKey()
        val projectId = getProjectId()
        if (apiKey.isEmpty() || userId.isEmpty()) return@withContext emptyList()

        try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/users/$userId/messages?key=$apiKey"
            val request = Request.Builder().url(url).get().build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val responseJson = JSONObject(bodyStr)
                val documents = responseJson.optJSONArray("documents") ?: return@withContext emptyList()
                val result = mutableListOf<JSONObject>()
                for (i in 0 until documents.length()) {
                    val doc = documents.getJSONObject(i)
                    val fields = doc.getJSONObject("fields")
                    val message = JSONObject().apply {
                        put("id", fields.getJSONObject("id").getString("stringValue"))
                        put("sessionId", fields.getJSONObject("sessionId").getString("stringValue"))
                        put("role", fields.getJSONObject("role").getString("stringValue"))
                        put("text", fields.getJSONObject("text").getString("stringValue"))
                        put("imageUri", fields.optJSONObject("imageUri")?.optString("stringValue", "") ?: "")
                        put("timestamp", fields.getJSONObject("timestamp").getString("integerValue").toLong())
                    }
                    result.add(message)
                }
                return@withContext result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    /**
     * Retrieve all sessions synced in Firebase
     */
    suspend fun fetchCloudSessions(userId: String): List<JSONObject> = withContext(Dispatchers.IO) {
        val apiKey = getFirebaseApiKey()
        val projectId = getProjectId()
        if (apiKey.isEmpty() || userId.isEmpty()) return@withContext emptyList()

        try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/users/$userId/sessions?key=$apiKey"
            val request = Request.Builder().url(url).get().build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val responseJson = JSONObject(bodyStr)
                val documents = responseJson.optJSONArray("documents") ?: return@withContext emptyList()
                val result = mutableListOf<JSONObject>()
                for (i in 0 until documents.length()) {
                    val doc = documents.getJSONObject(i)
                    val fields = doc.getJSONObject("fields")
                    val session = JSONObject().apply {
                        put("id", fields.getJSONObject("id").getString("stringValue"))
                        put("title", fields.getJSONObject("title").getString("stringValue"))
                        put("isPinned", fields.getJSONObject("isPinned").getBoolean("booleanValue"))
                        put("createdAt", fields.getJSONObject("createdAt").getString("integerValue").toLong())
                        put("lastUpdatedAt", fields.getJSONObject("lastUpdatedAt").getString("integerValue").toLong())
                    }
                    result.add(session)
                }
                return@withContext result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    /**
     * Retrieve all memory insights synced in Firebase
     */
    suspend fun fetchCloudMemoryInsights(userId: String): List<JSONObject> = withContext(Dispatchers.IO) {
        val apiKey = getFirebaseApiKey()
        val projectId = getProjectId()
        if (apiKey.isEmpty() || userId.isEmpty()) return@withContext emptyList()

        try {
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/users/$userId/memories?key=$apiKey"
            val request = Request.Builder().url(url).get().build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val responseJson = JSONObject(bodyStr)
                val documents = responseJson.optJSONArray("documents") ?: return@withContext emptyList()
                val result = mutableListOf<JSONObject>()
                for (i in 0 until documents.length()) {
                    val doc = documents.getJSONObject(i)
                    val fields = doc.getJSONObject("fields")
                    val insight = JSONObject().apply {
                        put("id", fields.getJSONObject("id").getString("integerValue").toLong())
                        put("category", fields.getJSONObject("category").getString("stringValue"))
                        put("content", fields.getJSONObject("content").getString("stringValue"))
                        put("timestamp", fields.getJSONObject("timestamp").getString("integerValue").toLong())
                    }
                    result.add(insight)
                }
                return@withContext result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
}
