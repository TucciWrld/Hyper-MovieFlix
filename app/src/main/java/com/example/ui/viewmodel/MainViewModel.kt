package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Navigation state
sealed interface AppScreen {
    object Splash : AppScreen
    object ProfileSelect : AppScreen
    object MainHub : AppScreen
    data class MediaDetail(val mediaId: Int) : AppScreen
    data class VideoPlayer(val mediaId: Int) : AppScreen
}

// Bottom tab in MainHub representation
enum class HubTab {
    HOME, HYPER_AI, DOWNLOADS, COMMUNITY, PROFILE_ADMIN
}

// Chat bubble structure for Hyper AI helper
data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timeMillis: Long = System.currentTimeMillis()
)

// Comment data structure
data class MovieComment(
    val user: String,
    val comment: String,
    val rating: Float
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MediaRepository(database)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val commentListAdapterType = Types.newParameterizedType(List::class.java, MovieComment::class.java)
    private val commentListAdapter = moshi.adapter<List<MovieComment>>(commentListAdapterType)

    // UI States
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _activeProfile = MutableStateFlow<DbProfile?>(null)
    val activeProfile: StateFlow<DbProfile?> = _activeProfile.asStateFlow()

    private val _selectedTab = MutableStateFlow(HubTab.HOME)
    val selectedTab: StateFlow<HubTab> = _selectedTab.asStateFlow()

    // Loaded media from Room db
    val profiles = repository.allProfiles.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allMedia = repository.allMedia.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val trendingMedia = repository.trendingMedia.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val popularMedia = repository.popularMedia.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Selected Media details for active detail screen
    private val _selectedMedia = MutableStateFlow<DbMedia?>(null)
    val selectedMedia: StateFlow<DbMedia?> = _selectedMedia.asStateFlow()

    // Check if selected media is in watchlist
    private val _isCurrentInWatchlist = MutableStateFlow(false)
    val isCurrentInWatchlist: StateFlow<Boolean> = _isCurrentInWatchlist.asStateFlow()

    // Watch history for active profile
    private val _watchHistory = MutableStateFlow<List<DbWatchHistory>>(emptyList())
    val watchHistory: StateFlow<List<DbWatchHistory>> = _watchHistory.asStateFlow()

    // Downloads list for active profile
    private val _downloads = MutableStateFlow<List<DbDownloadItem>>(emptyList())
    val downloads: StateFlow<List<DbDownloadItem>> = _downloads.asStateFlow()

    // Hyper AI Recommendations List & Conversation Chat
    private val _aiChatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("ai", "Hello! I am your Hyper AI Movie Assistant. Ask me to recommend high-octane thrillers, vintage anime, or check sports summaries! Try typing: 'Suggest 3 sci-fi movies for an action enthusiast' or 'What is a good documentary here?'")
        )
    )
    val aiChatHistory: StateFlow<List<ChatMessage>> = _aiChatHistory.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Selected audio track, subtitle, playback speed
    val selectedAudioTrack = MutableStateFlow("English")
    val selectedSubtitle = MutableStateFlow("English")
    val playbackSpeed = MutableStateFlow("1.0x")
    val streamingQuality = MutableStateFlow("Auto (1080p)")

    // Premium Subscription Status
    private val _isPremiumAccount = MutableStateFlow(false)
    val isPremiumAccount: StateFlow<Boolean> = _isPremiumAccount.asStateFlow()

    init {
        // Run database initializer
        viewModelScope.launch {
            repository.ensureDatabaseSeeded()
            // Wait brief moment for splash
            delay(1500)
            _currentScreen.value = AppScreen.ProfileSelect
        }
    }

    // Nav-flow actions
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        // If entering details or player, load item details
        if (screen is AppScreen.MediaDetail) {
            loadMediaDetails(screen.mediaId)
        } else if (screen is AppScreen.VideoPlayer) {
            loadMediaDetails(screen.mediaId)
        }
    }

    fun selectTab(tab: HubTab) {
        _selectedTab.value = tab
    }

    fun selectProfile(profile: DbProfile) {
        _activeProfile.value = profile
        _isPremiumAccount.value = !profile.isKids // Kids profile gets standard, customized Emma profile gets premium!
        _currentScreen.value = AppScreen.MainHub
        _selectedTab.value = HubTab.HOME

        // Load specific profile elements
        viewModelScope.launch {
            repository.getWatchHistory(profile.id).collect {
                _watchHistory.value = it
            }
        }
        viewModelScope.launch {
            repository.getDownloads(profile.id).collect {
                _downloads.value = it
            }
        }
    }

    fun logoutProfile() {
        _activeProfile.value = null
        _currentScreen.value = AppScreen.ProfileSelect
    }

    private fun loadMediaDetails(mediaId: Int) {
        viewModelScope.launch {
            val item = repository.getMediaById(mediaId)
            _selectedMedia.value = item
            val profile = _activeProfile.value
            if (profile != null) {
                _isCurrentInWatchlist.value = repository.isInWatchlist(profile.id, mediaId)
            }
        }
    }

    // Toggle watchlist (favorites)
    fun toggleWatchlist(mediaId: Int) {
        val profile = _activeProfile.value ?: return
        viewModelScope.launch {
            val inList = repository.isInWatchlist(profile.id, mediaId)
            if (inList) {
                repository.removeFromWatchlist(profile.id, mediaId)
                _isCurrentInWatchlist.value = false
            } else {
                repository.addToWatchlist(profile.id, mediaId)
                _isCurrentInWatchlist.value = true
            }
        }
    }

    // Add Movie Review/Feedback from Discussion thread
    fun addComment(mediaId: Int, author: String, text: String, rating: Float) {
        viewModelScope.launch {
            val media = repository.getMediaById(mediaId) ?: return@launch
            val decodedComments = parseComments(media.commentsJson).toMutableList()
            decodedComments.add(MovieComment(author, text, rating))

            val updatedJson = commentListAdapter.toJson(decodedComments) ?: "[]"
            val updatedMedia = media.copy(commentsJson = updatedJson, rating = ((media.rating + rating) / 2f))
            repository.updateMedia(updatedMedia)

            // Reload loaded detail screen
            if (_selectedMedia.value?.id == mediaId) {
                _selectedMedia.value = updatedMedia
            }
        }
    }

    fun parseComments(json: String): List<MovieComment> {
        return try {
            commentListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Simulate Profile Downloads Center
    fun triggerDownload(media: DbMedia) {
        val profile = _activeProfile.value ?: return
        viewModelScope.launch {
            // Check if already is downloading/completed
            val existing = _downloads.value.find { it.id == media.id }
            if (existing != null) return@launch

            // Save standard downloading entity
            val down = DbDownloadItem(
                id = media.id,
                profileId = profile.id,
                title = media.title,
                downloadedBytes = 0,
                totalBytes = 250 * 1024 * 1024L, // 250 MB
                status = "Downloading",
                progressPercent = 0,
                speed = "18 MB/s"
            )
            repository.saveDownload(down)

            // Simulate progress ticks
            viewModelScope.launch {
                var progress = 0
                while (progress < 100) {
                    delay(800)
                    progress += (10..22).random()
                    if (progress > 100) progress = 100
                    val currentDown = DbDownloadItem(
                        id = media.id,
                        profileId = profile.id,
                        title = media.title,
                        downloadedBytes = (250 * 1024 * 1024L / 100) * progress,
                        totalBytes = 250 * 1024 * 1024L,
                        status = if (progress == 100) "Completed" else "Downloading",
                        progressPercent = progress,
                        speed = if (progress == 100) "0 KB/s" else "${(12..25).random()} MB/s"
                    )
                    repository.saveDownload(currentDown)
                }
            }
        }
    }

    fun pauseDownload(mediaId: Int) {
        val match = _downloads.value.find { it.id == mediaId } ?: return
        viewModelScope.launch {
            repository.saveDownload(match.copy(status = "Paused", speed = "0 KB/s"))
        }
    }

    fun resumeDownload(mediaId: Int) {
        val match = _downloads.value.find { it.id == mediaId } ?: return
        viewModelScope.launch {
            repository.saveDownload(match.copy(status = "Downloading"))
            // Simulate continuation progress
            viewModelScope.launch {
                var progress = match.progressPercent
                while (progress < 100) {
                    delay(800)
                    progress += (8..18).random()
                    if (progress > 100) progress = 100
                    val currentDown = DbDownloadItem(
                        id = mediaId,
                        profileId = match.profileId,
                        title = match.title,
                        downloadedBytes = (match.totalBytes / 100) * progress,
                        totalBytes = match.totalBytes,
                        status = if (progress == 100) "Completed" else "Downloading",
                        progressPercent = progress,
                        speed = if (progress == 100) "0 KB/s" else "${(10..22).random()} MB/s"
                    )
                    repository.saveDownload(currentDown)
                }
            }
        }
    }

    fun deleteDownload(mediaId: Int) {
        viewModelScope.launch {
            repository.deleteDownload(mediaId)
        }
    }

    // Save playing movie history
    fun recordWatchHistory(mediaId: Int, progressSecs: Int, durationSecs: Int) {
        val profile = _activeProfile.value ?: return
        viewModelScope.launch {
            val history = DbWatchHistory(
                profileId = profile.id,
                mediaId = mediaId,
                progressSecs = progressSecs,
                durationSecs = durationSecs
            )
            repository.addWatchHistory(history)
        }
    }

    // Admin Panel Database inserts! This directly expands local database records
    fun adminAddMedia(
        title: String,
        overview: String,
        category: String,
        genre: String,
        bannerHex: String,
        duration: String,
        year: Int,
        directors: String,
        actors: String,
        isPremium: Boolean
    ) {
        viewModelScope.launch {
            val maxHex = if (bannerHex.startsWith("0x")) bannerHex else "0xFF$bannerHex"
            val media = DbMedia(
                title = title,
                overview = overview,
                category = category,
                genre = genre,
                bannerColor = maxHex,
                duration = duration,
                year = year,
                directors = directors,
                actors = actors,
                isPremium = isPremium,
                isCustom = true
            )
            repository.insertMedia(media)
        }
    }

    fun adminDeleteMedia(media: DbMedia) {
        viewModelScope.launch {
            repository.deleteMedia(media)
        }
    }

    // Handle Premium upgrade (Simulates successful Stripe callback)
    fun activatePremiumPlus() {
        _isPremiumAccount.value = true
        // Can raise alert or change primary status
    }

    // AI Recommender Interaction
    fun sendChatMessage(query: String) {
        if (query.isBlank()) return

        val userMsg = ChatMessage("user", query)
        _aiChatHistory.value = _aiChatHistory.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val systemContext = """
                You are Hyper AI, the ultra-smart movie, series, and live TV curation assistant built into Hyper MovieFlix (developed by Tucci Cyber Nation).
                Give brief, exciting, and extremely stylish responses styled for a premium cinema streaming application!
                Be friendly and suggest content based on matching user interests. If they ask for content that exists in our mock database (like 'Cyber Renegades', 'Tucci Cyber Nation documentary', 'Chronicles of the Whispering Wind', 'Gizmo the Drone', 'Formula 1 Monaco GP'), enthusiastically highlight those titles and tell them they can watch them right here in the app!
                Do not talk about website Next.js codebase internals. Talk purely as a helpful streaming recommendation expert. Suggest 2-3 matching actual titles or realistic themed custom recommendations in a clear, formatted outline.
            """.trimIndent()

            val aiAnswer = GeminiService.getAIResponse(query, systemContext)

            val parsedAnswer = if (aiAnswer == "API_KEY_MISSING") {
                """
                🔑 **Hyper AI Offline mode Active!**
                To unlock live Gemini-powered curation, add a valid `GEMINI_API_KEY` in the AI Studio Secrets panel.
                
                Based on your inquiry, here are your curated matches:
                1. **Cyber Renegades: Dawn of Tokyo** (Action Sci-Fi - Available Now)
                2. **Tucci Cyber Nation: The Revolution** (Tech Documentary - Premium)
                3. **Chronicles of the Whispering Wind** (Stunning Fantasy Anime - Available Now)
                
                Would you like to start streaming one of these immediately? 🎞️
                """.trimIndent()
            } else {
                aiAnswer
            }

            _aiChatHistory.value = _aiChatHistory.value + ChatMessage("ai", parsedAnswer)
            _isAiLoading.value = false
        }
    }
}
