package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MediaRepository(private val db: AppDatabase) {

    private val profileDao = db.profileDao()
    private val mediaDao = db.mediaDao()
    private val watchHistoryDao = db.watchHistoryDao()
    private val watchlistDao = db.watchlistDao()
    private val downloadDao = db.downloadDao()

    // Profiles
    val allProfiles: Flow<List<DbProfile>> = profileDao.getAllProfiles()
    suspend fun insertProfile(profile: DbProfile) = profileDao.insertProfile(profile)
    suspend fun deleteProfile(profile: DbProfile) = profileDao.deleteProfile(profile)

    // Media Catalog
    val allMedia: Flow<List<DbMedia>> = mediaDao.getAllMedia()
    val trendingMedia: Flow<List<DbMedia>> = mediaDao.getTrendingMedia()
    val popularMedia: Flow<List<DbMedia>> = mediaDao.getPopularMedia()

    fun getMediaByCategory(category: String): Flow<List<DbMedia>> = mediaDao.getMediaByCategory(category)
    suspend fun getMediaById(id: Int): DbMedia? = mediaDao.getMediaById(id)
    fun searchMedia(query: String): Flow<List<DbMedia>> = mediaDao.searchMedia(query)

    suspend fun insertMedia(media: DbMedia) = mediaDao.insertMedia(media)
    suspend fun updateMedia(media: DbMedia) = mediaDao.updateMedia(media)
    suspend fun deleteMedia(media: DbMedia) = mediaDao.deleteMedia(media)

    // Watch History
    fun getWatchHistory(profileId: String): Flow<List<DbWatchHistory>> = watchHistoryDao.getWatchHistoryForProfile(profileId)
    suspend fun addWatchHistory(history: DbWatchHistory) = watchHistoryDao.insertWatchHistory(history)
    suspend fun deleteWatchHistory(profileId: String, mediaId: Int) = watchHistoryDao.deleteWatchHistory(profileId, mediaId)
    suspend fun clearWatchHistory(profileId: String) = watchHistoryDao.clearWatchHistory(profileId)

    // Watchlist (Favorites)
    fun getWatchlistIds(profileId: String): Flow<List<Int>> = watchlistDao.getWatchlistMediaIds(profileId)
    suspend fun addToWatchlist(profileId: String, mediaId: Int) = watchlistDao.addToWatchlist(DbWatchlist(profileId, mediaId))
    suspend fun removeFromWatchlist(profileId: String, mediaId: Int) = watchlistDao.removeFromWatchlist(profileId, mediaId)
    suspend fun isInWatchlist(profileId: String, mediaId: Int): Boolean = watchlistDao.isMediaInWatchlist(profileId, mediaId) > 0

    // Downloads
    fun getDownloads(profileId: String): Flow<List<DbDownloadItem>> = downloadDao.getDownloadsForProfile(profileId)
    suspend fun saveDownload(download: DbDownloadItem) = downloadDao.saveDownload(download)
    suspend fun deleteDownload(mediaId: Int) = downloadDao.deleteDownload(mediaId)

    // Seeding Routine
    suspend fun ensureDatabaseSeeded() {
        // Clear or inject if empty
        if (profileDao.getProfileCount() == 0) {
            // Seed premium profiles
            profileDao.insertProfile(DbProfile("prof_1", "Emma (Primary)", "0xFFAB47BC", isKids = false))
            profileDao.insertProfile(DbProfile("prof_2", "Tucci Cyber", "0xFF00ACC1", isKids = false))
            profileDao.insertProfile(DbProfile("prof_3", "Flix Kids", "0xFFFFB300", isKids = true))
        }

        if (mediaDao.getMediaCount() == 0) {
            val seedMedia = listOf(
                DbMedia(
                    title = "Cyber Renegades: Dawn of Tokyo",
                    overview = "In a futuristic dystopian Tokyo, an elite cybersecurity hacker uncovers a massive corporate espionage plot that threatens to plunge the global network into absolute darkness.",
                    bannerColor = "0xFF512DA8", // Purple Glow
                    rating = 4.8f,
                    duration = "2h 24m",
                    year = 2026,
                    directors = "Giovanni Tucci",
                    actors = "Ren Kimura, Sophia Stone, Daniel Park",
                    category = "Movie",
                    genre = "Sci-Fi",
                    isPremium = false,
                    isTrending = true,
                    isPopular = true,
                    commentsJson = """
                        [
                          {"user": "Alex99", "comment": "A cinematic masterpiece! The soundtrack is incredible.", "rating": 5.0},
                          {"user": "CyberFan", "comment": "Excellent visuals, Giovanni Tucci is a genius.", "rating": 4.5}
                        ]
                    """.trimIndent()
                ),
                DbMedia(
                    title = "Shadows in the Mist",
                    overview = "A seasoned detective is called to check a mysterious mountain town where citizens vanish whenever the purple dense mist flows in from the peaks.",
                    bannerColor = "0xFF303F9F", // Indigo Deep
                    rating = 4.5f,
                    duration = "1h 58m",
                    year = 2025,
                    directors = "Marcus Vance",
                    actors = "David Harbour, Emily Blunt",
                    category = "Movie",
                    genre = "Drama",
                    isPremium = false,
                    isTrending = true,
                    isPopular = false,
                    commentsJson = """
                        [
                          {"user": "MysteryLover", "comment": "Suspenseful from beginning to end! Great acting.", "rating": 5.0}
                        ]
                    """.trimIndent()
                ),
                DbMedia(
                    title = "Tucci Cyber Nation: The Revolution",
                    overview = "An epic documentary exploring the global hackers and tech enthusiasts fighting digital monopoly to deliver open-source streaming and cybersecurity tools worldwide.",
                    bannerColor = "0xFF7B1FA2", // Dark Magenta
                    rating = 4.9f,
                    duration = "1h 45m",
                    year = 2026,
                    directors = "Tucci Cyber Nation",
                    actors = "Tucci Founders, Cyber Security Experts",
                    category = "Documentary",
                    genre = "Tech",
                    isPremium = true,
                    isTrending = true,
                    isPopular = true,
                    commentsJson = """
                        [
                          {"user": "TechGuru", "comment": "Extremely educational, very inspiring project!", "rating": 5.0}
                        ]
                    """.trimIndent()
                ),
                DbMedia(
                    title = "Chronicles of the Whispering Wind",
                    overview = "A visually stunning fantasy anime following an apprentice monk who discovers they can read the memories carried by the mountain breeze, unlocking ancient wizard secrets.",
                    bannerColor = "0xFFFF80AB", // Light Pink Accent
                    rating = 4.7f,
                    duration = "24 Episodes",
                    year = 2026,
                    directors = "Hayao Miyazaki Studio Clone",
                    actors = "Haru Tanaka, Yuri Nakamoto",
                    category = "Anime",
                    genre = "Fantasy",
                    isPremium = false,
                    isTrending = true,
                    isPopular = true,
                    commentsJson = "[]"
                ),
                DbMedia(
                    title = "Samurai Mech Alpha",
                    overview = "In the year 3045, high-stakes combat involves giant bio-mechanical robots styled with ancient standard samurai heavy battle techniques. Ultimate championship.",
                    bannerColor = "0xFFDD2C00", // Crimson Orange
                    rating = 4.6f,
                    duration = "12 Episodes",
                    year = 2025,
                    directors = "Hideaki Anno Team",
                    actors = "Takeshi Kovacs, Kazuya Mishima",
                    category = "Anime",
                    genre = "Sci-Fi",
                    isPremium = true,
                    isTrending = false,
                    isPopular = true,
                    commentsJson = "[]"
                ),
                DbMedia(
                    title = "Gizmo: The Adventurous Smart Drone",
                    overview = "A delightful and educational cartoon for children. Gizmo is a little companion drone that flies around solving micro puzzles and teaching standard math and friendship.",
                    bannerColor = "0xFF00C853", // Vibrant Green
                    rating = 4.9f,
                    duration = "52 Episodes",
                    year = 2026,
                    directors = "Tucci Kids Studio",
                    actors = "Gizmo Vocaloids, Clara Miller",
                    category = "Cartoon",
                    genre = "Comedy",
                    isPremium = false,
                    isTrending = false,
                    isPopular = true,
                    commentsJson = "[]"
                ),
                DbMedia(
                    title = "Live: Formula 1 Monaco GP",
                    overview = "Immersive, real-time live channel broadcast of the Monaco Grand Prix, presenting full-latency multi-camera angles, live timings, pit lanes, and driver telemetry overlays.",
                    bannerColor = "0xFFE65100", // Racing Orange
                    rating = 4.9f,
                    duration = "LIVE",
                    year = 2026,
                    directors = "FIA Streaming Service",
                    actors = "F1 Broadcasters, Drivers",
                    category = "Live Channel",
                    genre = "Sports",
                    isPremium = true,
                    isTrending = true,
                    isPopular = true,
                    commentsJson = "[]"
                ),
                DbMedia(
                    title = "Live: Earth Orbit Station Cam",
                    overview = "Relax with a peaceful 24/7 direct feed from the Space Station, looking down on the majestic spinning globe with atmospheric synth music backdrops.",
                    bannerColor = "0xFF00B0FF", // Space cyan
                    rating = 4.4f,
                    duration = "LIVE",
                    year = 2026,
                    directors = "NASA Public Stream",
                    actors = "Earth Orbital Citizens",
                    category = "Live Channel",
                    genre = "Sci-Fi",
                    isPremium = false,
                    isTrending = false,
                    isPopular = true,
                    commentsJson = "[]"
                ),
                DbMedia(
                    title = "Echoes of Quantum Minds",
                    overview = "A mind-bending drama series exploring standard parallel lives where a single minor choice splits the universe into divergent cybernetic outcomes.",
                    bannerColor = "0xFF00E676", // Quantum Green
                    rating = 4.6f,
                    duration = "Season 1 (10 Ep)",
                    year = 2026,
                    directors = "Christopher Nolan Partner",
                    actors = "Cillian Murphy, Florence Pugh",
                    category = "TV Show",
                    genre = "Sci-Fi",
                    isPremium = true,
                    isTrending = false,
                    isPopular = true,
                    commentsJson = "[]"
                ),
                DbMedia(
                    title = "The Culinary Expeditions of Italy",
                    overview = "A beautiful and mouth-watering docu-journey exploring the deep-rooted truffle fields, authentic pasta cellars, and remote organic vineyards of modern culinary artists.",
                    bannerColor = "0xFFFFD600", // Yellow gold
                    rating = 4.3f,
                    duration = "1h 22m",
                    year = 2024,
                    directors = "Chef Tucci",
                    actors = "Local Italian Chefs, Vineyard Keepers",
                    category = "Documentary",
                    genre = "Cooking",
                    isPremium = false,
                    isTrending = false,
                    isPopular = false,
                    commentsJson = "[]"
                )
            )
            mediaDao.insertMediaList(seedMedia)
        }
    }
}
