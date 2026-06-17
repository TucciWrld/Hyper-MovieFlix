package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. Entities
// ==========================================

@Entity(tableName = "profiles")
data class DbProfile(
    @PrimaryKey val id: String,
    val name: String,
    val avatarColorHex: String, // We'll render beautiful vector avatars with colored background initials
    val isKids: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "media_items")
data class DbMedia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val overview: String,
    val bannerColor: String = "0xFF512DA8", // Beautiful gradient colors or high-quality styled cards
    val textPosterUrl: String = "", // Placeholders
    val rating: Float = 4.5f,
    val duration: String = "2h 15m",
    val year: Int = 2026,
    val directors: String = "Tucci Cyber Nation",
    val actors: String = "John Doe, Jane Smith",
    val category: String = "Movie", // Movie, TV Show, Anime, Cartoon, Documentary, Live Channel
    val genre: String = "Sci-Fi", // Action, Drama, Sci-Fi, Comedy, Sports
    val isPremium: Boolean = false,
    val isCustom: Boolean = false,
    val isTrending: Boolean = false,
    val isPopular: Boolean = false,
    val commentsJson: String = "[]", // Serialized JSON string of reviews/comments
    val audioTracks: String = "English, Spanish, Japanese",
    val subtitles: String = "English, Spanish, French, Japanese"
)

@Entity(tableName = "watch_history")
data class DbWatchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: String,
    val mediaId: Int,
    val watchedAt: Long = System.currentTimeMillis(),
    val durationSecs: Int = 7200,
    val progressSecs: Int = 1200
)

@Entity(tableName = "watchlist", primaryKeys = ["profileId", "mediaId"])
data class DbWatchlist(
    val profileId: String,
    val mediaId: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DbDownloadItem(
    @PrimaryKey val id: Int, // matches mediaId
    val profileId: String,
    val title: String,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val status: String, // Pending, Downloading, Completed, Paused
    val progressPercent: Int = 0,
    val speed: String = "15 MB/s"
)

// ==========================================
// 2. DAOs
// ==========================================

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY dateCreated ASC")
    fun getAllProfiles(): Flow<List<DbProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: DbProfile)

    @Delete
    suspend fun deleteProfile(profile: DbProfile)

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY id DESC")
    fun getAllMedia(): Flow<List<DbMedia>>

    @Query("SELECT * FROM media_items WHERE isTrending = 1")
    fun getTrendingMedia(): Flow<List<DbMedia>>

    @Query("SELECT * FROM media_items WHERE isPopular = 1")
    fun getPopularMedia(): Flow<List<DbMedia>>

    @Query("SELECT * FROM media_items WHERE category = :category")
    fun getMediaByCategory(category: String): Flow<List<DbMedia>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaById(id: Int): DbMedia?

    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%' OR actors LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%'")
    fun searchMedia(query: String): Flow<List<DbMedia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: DbMedia)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<DbMedia>)

    @Update
    suspend fun updateMedia(media: DbMedia)

    @Delete
    suspend fun deleteMedia(media: DbMedia)

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getMediaCount(): Int
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history WHERE profileId = :profileId ORDER BY watchedAt DESC")
    fun getWatchHistoryForProfile(profileId: String): Flow<List<DbWatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(history: DbWatchHistory)

    @Query("DELETE FROM watch_history WHERE profileId = :profileId AND mediaId = :mediaId")
    suspend fun deleteWatchHistory(profileId: String, mediaId: Int)

    @Query("DELETE FROM watch_history WHERE profileId = :profileId")
    suspend fun clearWatchHistory(profileId: String)
}

@Dao
interface WatchlistDao {
    @Query("SELECT mediaId FROM watchlist WHERE profileId = :profileId")
    fun getWatchlistMediaIds(profileId: String): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: DbWatchlist)

    @Query("DELETE FROM watchlist WHERE profileId = :profileId AND mediaId = :mediaId")
    suspend fun removeFromWatchlist(profileId: String, mediaId: Int)

    @Query("SELECT COUNT(*) FROM watchlist WHERE profileId = :profileId AND mediaId = :mediaId")
    suspend fun isMediaInWatchlist(profileId: String, mediaId: Int): Int
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads WHERE profileId = :profileId ORDER BY id DESC")
    fun getDownloadsForProfile(profileId: String): Flow<List<DbDownloadItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDownload(download: DbDownloadItem)

    @Query("DELETE FROM downloads WHERE id = :mediaId")
    suspend fun deleteDownload(mediaId: Int)
}

// ==========================================
// 3. Database class
// ==========================================

@Database(
    entities = [
        DbProfile::class,
        DbMedia::class,
        DbWatchHistory::class,
        DbWatchlist::class,
        DbDownloadItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun mediaDao(): MediaDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hyper_flix_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
