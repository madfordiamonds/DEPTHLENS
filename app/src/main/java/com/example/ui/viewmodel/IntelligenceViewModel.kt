package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MessageEntity
import com.example.data.model.SessionEntity
import com.example.data.model.MemoryInsight
import com.example.data.repository.IntelligenceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IntelligenceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IntelligenceRepository(application)

    // Active session selection
    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    // Loading indicator dynamically mapped from the repository's background analyses set
    val isLoading: StateFlow<Boolean> = combine(
        _activeSessionId,
        IntelligenceRepository.runningAnalyses
    ) { activeId: String?, runningSet: Set<String> ->
        activeId != null && runningSet.contains(activeId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Attached media asset
    private val _attachedImageUri = MutableStateFlow<String?>(null)
    val attachedImageUri: StateFlow<String?> = _attachedImageUri.asStateFlow()

    // Live list of session histories
    val sessions: StateFlow<List<SessionEntity>> = repository.allSessionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live list of long-term memory logs
    val memoryInsights: StateFlow<List<MemoryInsight>> = repository.allMemoryInsightsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic message list for currently active session
    val activeMessages: StateFlow<List<MessageEntity>> = _activeSessionId
        .flatMapLatest { sessionId ->
            if (sessionId != null) {
                repository.getMessagesFlow(sessionId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Always open the Home Screen on app launch / entry
        _activeSessionId.value = null
        
        viewModelScope.launch {
            val existing = repository.allSessionsFlow.firstOrNull() ?: emptyList()
            if (existing.isEmpty()) {
                // Pre-seed a default session silently so workspace is ready
                repository.createNewSession("Global Intelligence Feed")
            }
        }
    }

    fun selectSession(sessionId: String?) {
        _activeSessionId.value = sessionId
        clearAttachment()
        clearContinuityBrief()
    }

    fun createSession(title: String) {
        viewModelScope.launch {
            val newSession = repository.createNewSession(title.ifBlank { "New Intelligence Thread" })
            _activeSessionId.value = newSession.id
            clearAttachment()
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                _activeSessionId.value = null
            }
        }
    }

    fun togglePinSession(sessionId: String) {
        viewModelScope.launch {
            repository.togglePinSession(sessionId)
        }
    }

    fun setAttachment(uriString: String?) {
        _attachedImageUri.value = uriString
    }

    fun clearAttachment() {
        _attachedImageUri.value = null
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessageById(messageId)
        }
    }

    fun retryLastAnalysis(errorMessageId: String) {
        val sessionId = _activeSessionId.value ?: return
        viewModelScope.launch {
            try {
                // Delete the error message
                repository.deleteMessageById(errorMessageId)
                // Determine if there are still any messages
                val existingHistory = repository.getMessagesFlow(sessionId).firstOrNull() ?: emptyList()
                val lastUserMsg = existingHistory.lastOrNull { it.role == "user" }
                if (lastUserMsg != null) {
                    // Trigger analysis again in background
                    repository.startBackgroundAnalysis(sessionId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun regenerateLastAnalysis(aiMessageId: String) {
        val sessionId = _activeSessionId.value ?: return
        viewModelScope.launch {
            try {
                // Delete this AI response
                repository.deleteMessageById(aiMessageId)
                // Trigger background analysis again
                repository.startBackgroundAnalysis(sessionId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendQuery(text: String) {
        val cleanQuery = text.trim()
        if (cleanQuery.isEmpty() && _attachedImageUri.value == null) return

        val attachedUri = _attachedImageUri.value
        clearAttachment()

        viewModelScope.launch {
            try {
                // Determine or create session if none active (e.g. from Home screen prompt)
                val sessionId = _activeSessionId.value ?: run {
                    val newSession = repository.createNewSession("Intelligence Diagnostic")
                    _activeSessionId.value = newSession.id
                    newSession.id
                }

                // Determine if this is the first user query in this conversation
                val existingHistory = repository.getMessagesFlow(sessionId).firstOrNull() ?: emptyList()
                val isFirstQuery = existingHistory.none { it.role == "user" }

                // 1. Insert user message to initiate continuity UI rendering
                repository.insertUserMessage(sessionId, cleanQuery, attachedUri)
                
                // 2. Perform intelligence analysis call to external models in background (non-blocking)
                repository.startBackgroundAnalysis(sessionId) {
                    // Asynchronously generate an aesthetic title if first query
                    if (isFirstQuery && cleanQuery.isNotEmpty()) {
                        viewModelScope.launch {
                            repository.generateTitleForSession(sessionId, cleanQuery)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Memory management options
    private val _isMemoryEnabled = MutableStateFlow(true)
    val isMemoryEnabled: StateFlow<Boolean> = _isMemoryEnabled.asStateFlow()

    private val _isCollectiveIntelligenceOptIn = MutableStateFlow(true)
    val isCollectiveIntelligenceOptIn: StateFlow<Boolean> = _isCollectiveIntelligenceOptIn.asStateFlow()

    fun setMemoryEnabled(enabled: Boolean) {
        _isMemoryEnabled.value = enabled
    }

    fun setCollectiveIntelligenceOptIn(optIn: Boolean) {
        _isCollectiveIntelligenceOptIn.value = optIn
    }

    fun clearAllMemoryInsights() {
        viewModelScope.launch {
            repository.clearAllMemoryInsights()
        }
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearAllData()
            _activeSessionId.value = null
            createSession("New Reality Intel")
        }
    }

    // Conversation Continuity States
    private val _continuityBrief = MutableStateFlow<String?>(null)
    val continuityBrief: StateFlow<String?> = _continuityBrief.asStateFlow()

    private val _continuityBriefStatus = MutableStateFlow("Idle")
    val continuityBriefStatus: StateFlow<String> = _continuityBriefStatus.asStateFlow()

    fun clearContinuityBrief() {
        _continuityBrief.value = null
        _continuityBriefStatus.value = "Idle"
    }

    fun reconnectConversationContext() {
        val sessionId = _activeSessionId.value ?: return
        _continuityBriefStatus.value = "Syncing"
        _continuityBrief.value = null
        viewModelScope.launch {
            try {
                val brief = repository.generateContinuityBrief(sessionId)
                _continuityBrief.value = brief
                _continuityBriefStatus.value = "Done"
            } catch (e: Exception) {
                e.printStackTrace()
                _continuityBriefStatus.value = "Error"
            }
        }
    }

    // Collapsible System Controls state saved in SharedPreferences
    private val prefs = getApplication<Application>().getSharedPreferences("depthlens_prefs", android.content.Context.MODE_PRIVATE)

    private val _isSystemControlsExpanded = MutableStateFlow(prefs.getBoolean("system_controls_expanded", false))
    val isSystemControlsExpanded: StateFlow<Boolean> = _isSystemControlsExpanded.asStateFlow()

    fun setSystemControlsExpanded(expanded: Boolean) {
        _isSystemControlsExpanded.value = expanded
        prefs.edit().putBoolean("system_controls_expanded", expanded).apply()
    }

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    private val _darkModeEnabled = MutableStateFlow(prefs.getBoolean("dark_mode_enabled", true))
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    fun setDarkModeEnabled(enabled: Boolean) {
        _darkModeEnabled.value = enabled
        prefs.edit().putBoolean("dark_mode_enabled", enabled).apply()
    }
}
