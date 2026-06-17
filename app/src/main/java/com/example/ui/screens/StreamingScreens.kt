package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import kotlinx.coroutines.launch

// ==========================================
// Main Orchestrator Screen Entry
// ==========================================

@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                is AppScreen.Splash -> SplashScreen()
                is AppScreen.ProfileSelect -> ProfileSelectionScreen(viewModel)
                is AppScreen.MainHub -> MainHubScreen(viewModel)
                is AppScreen.MediaDetail -> MediaDetailScreen(viewModel, screen.mediaId)
                is AppScreen.VideoPlayer -> VideoPlayerScreen(viewModel, screen.mediaId)
            }
        }
    }
}

// ==========================================
// 1. Splash Screen
// ==========================================

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CinemaBlack, CinemaSurface)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(NeonPurple.copy(alpha = 0.2f))
                    .border(2.dp, NeonPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Logo Play",
                    tint = NeonPurple,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "HYPER MOVIEFLIX",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = HighContrastText
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Powered by Tucci Cyber Nation",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = SubtitleText
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(color = NeonPurple, strokeWidth = 3.dp)
        }
    }
}

// ==========================================
// 2. Profile Selection Screen
// ==========================================

@Composable
fun ProfileSelectionScreen(viewModel: MainViewModel) {
    val profileList by viewModel.profiles.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Who's Watching?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HighContrastText
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Choose a profile to load personalized feeds",
                style = MaterialTheme.typography.bodyMedium,
                color = SubtitleText,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Horizontal custom profiles grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                profileList.forEach { profile ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .testTag("profile_item_${profile.name}")
                            .clickable { viewModel.selectProfile(profile) }
                            .width(90.dp)
                    ) {
                        // Profile Avatar
                        val parsedColor = remember(profile.avatarColorHex) {
                            try {
                                Color(profile.avatarColorHex.removePrefix("0x").toLong(16))
                            } catch (e: Exception) {
                                NeonPurple
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(parsedColor)
                                .border(2.dp, HighContrastText, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            if (profile.isKids) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(AccentGold, RoundedCornerShape(topStart = 8.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "KIDS",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CinemaBlack
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = HighContrastText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Guest Mode Button
            OutlinedButton(
                onClick = {
                    val fallbackProfile = DbProfile("guest", "Guest User", "0xFF9E9E9E", isKids = false)
                    viewModel.selectProfile(fallbackProfile)
                },
                modifier = Modifier.testTag("guest_mode_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
                border = BorderStroke(1.dp, NeonPurple)
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Guest")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enter as Guest")
            }
        }
    }
}

// ==========================================
// 3. Main Hub Screen Wrapper
// ==========================================

@Composable
fun MainHubScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CinemaSurface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == HubTab.HOME,
                    onClick = { viewModel.selectTab(HubTab.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPurple,
                        selectedTextColor = NeonPurple,
                        unselectedIconColor = SubtitleText,
                        unselectedTextColor = SubtitleText,
                        indicatorColor = CinemaCard
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HubTab.HYPER_AI,
                    onClick = { viewModel.selectTab(HubTab.HYPER_AI) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "AI Assistant") },
                    label = { Text("Hyper AI") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPurple,
                        selectedTextColor = NeonPurple,
                        unselectedIconColor = SubtitleText,
                        unselectedTextColor = SubtitleText,
                        indicatorColor = CinemaCard
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HubTab.DOWNLOADS,
                    onClick = { viewModel.selectTab(HubTab.DOWNLOADS) },
                    icon = { Text("⬇", color = if (selectedTab == HubTab.DOWNLOADS) NeonPurple else SubtitleText, fontWeight = FontWeight.Bold) },
                    label = { Text("Downloads") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPurple,
                        selectedTextColor = NeonPurple,
                        unselectedIconColor = SubtitleText,
                        unselectedTextColor = SubtitleText,
                        indicatorColor = CinemaCard
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HubTab.COMMUNITY,
                    onClick = { viewModel.selectTab(HubTab.COMMUNITY) },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Community") },
                    label = { Text("Social") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPurple,
                        selectedTextColor = NeonPurple,
                        unselectedIconColor = SubtitleText,
                        unselectedTextColor = SubtitleText,
                        indicatorColor = CinemaCard
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HubTab.PROFILE_ADMIN,
                    onClick = { viewModel.selectTab(HubTab.PROFILE_ADMIN) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Setup") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPurple,
                        selectedTextColor = NeonPurple,
                        unselectedIconColor = SubtitleText,
                        unselectedTextColor = SubtitleText,
                        indicatorColor = CinemaCard
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CinemaBlack)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                HubTab.HOME -> HomeDashboard(viewModel)
                HubTab.HYPER_AI -> ChatBotScreen(viewModel)
                HubTab.DOWNLOADS -> DownloadsScreen(viewModel)
                HubTab.COMMUNITY -> CommunityScreen(viewModel)
                HubTab.PROFILE_ADMIN -> SettingsAdminScreen(viewModel)
            }
        }
    }
}

// ==========================================
// 4. Home Dashboard Screen
// ==========================================

@Composable
fun HomeDashboard(viewModel: MainViewModel) {
    val allMediaList by viewModel.allMedia.collectAsStateWithLifecycle()
    val trendingList by viewModel.trendingMedia.collectAsStateWithLifecycle()
    val popularList by viewModel.popularMedia.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val watchHistoryList by viewModel.watchHistory.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var activeCategoryFilter by remember { mutableStateOf("All") }

    val categories = listOf("All", "Movie", "TV Show", "Anime", "Cartoon", "Documentary", "Live Channel")

    val filteredMedia = remember(allMediaList, searchQuery, activeCategoryFilter) {
        allMediaList.filter { media ->
            val matchesQuery = media.title.contains(searchQuery, ignoreCase = true) ||
                    media.genre.contains(searchQuery, ignoreCase = true) ||
                    media.actors.contains(searchQuery, ignoreCase = true)
            val matchesCategory = activeCategoryFilter == "All" || media.category == activeCategoryFilter
            matchesQuery && matchesCategory
        }
    }

    // Featured media selection
    val featuredMedia = remember(allMediaList) {
        allMediaList.find { it.isTrending && !it.isPremium } ?: allMediaList.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // App header & Search area
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CinemaSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HYPER FLIX",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = HighContrastText
                            )
                        )
                    }

                    // Profile switcher shortcut button
                    Box(
                        modifier = Modifier
                            .testTag("avatar_quick_switch")
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    Color(
                                        activeProfile?.avatarColorHex
                                            ?.removePrefix("0x")
                                            ?.toLong(16) ?: 0xFF9C27B0
                                    )
                                } catch (e: Exception) {
                                    NeonPurple
                                }
                            )
                            .border(1.dp, HighContrastText, CircleShape)
                            .clickable { viewModel.logoutProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeProfile?.name?.take(1)?.uppercase() ?: "G",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search movies, actors, channels, genres...", color = SubtitleText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_input")
                        .clip(RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CinemaBlack,
                        unfocusedContainerColor = CinemaBlack,
                        disabledContainerColor = CinemaBlack,
                        cursorColor = NeonPurple,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = HighContrastText,
                        unfocusedTextColor = HighContrastText
                    ),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SubtitleText) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SubtitleText)
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Categories Quick Tags Horizontal Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = activeCategoryFilter == cat
                        Surface(
                            modifier = Modifier
                                .testTag("filter_chip_$cat")
                                .clickable { activeCategoryFilter = cat },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) NeonPurple else CinemaCard,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) NeonPurple else Color.DarkGray
                            )
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else HighContrastText
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        if (searchQuery.isEmpty() && activeCategoryFilter == "All") {
            // HERO Featured Banner Movie Display
            featuredMedia?.let { media ->
                item {
                    val parsedColor = remember(media.bannerColor) {
                        try {
                            Color(media.bannerColor.removePrefix("0x").toLong(16))
                        } catch (e: Exception) {
                            CinemaCard
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(parsedColor.copy(alpha = 0.5f), CinemaCard)
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(NeonPurple, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "TUI CO-ORIGINAL",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = media.genre,
                                    color = AccentGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = media.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = HighContrastText
                                )
                            )

                            Text(
                                text = media.overview,
                                style = MaterialTheme.typography.bodySmall,
                                color = SubtitleText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.navigateTo(AppScreen.VideoPlayer(media.id)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play Now", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.navigateTo(AppScreen.MediaDetail(media.id)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CinemaSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color.DarkGray)
                                ) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = HighContrastText)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Details", fontWeight = FontWeight.Bold, color = HighContrastText)
                                }
                            }
                        }
                    }
                }
            }

            // Continue watching local row
            if (watchHistoryList.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Continue Watching",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = HighContrastText
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(watchHistoryList) { h ->
                                val mediaMatched = allMediaList.find { it.id == h.mediaId }
                                mediaMatched?.let { m ->
                                    val progressPercentage = (h.progressSecs.toFloat() / h.durationSecs.toFloat()).coerceIn(0f, 1f)
                                    Column(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .clickable { viewModel.navigateTo(AppScreen.VideoPlayer(m.id)) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .height(80.dp)
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    try { Color(m.bannerColor.removePrefix("0x").toLong(16)) }
                                                    catch (e: Exception) { CinemaCard }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Resume",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                        // Slider progress indicators
                                        LinearProgressIndicator(
                                            progress = progressPercentage,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp),
                                            color = NeonPurple,
                                            trackColor = Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = m.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = HighContrastText
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Trending blockbusters
            item {
                MediaHorizontalRow(
                    title = "Trending Blockbusters 🔥",
                    mediaItems = trendingList,
                    onMediaClick = { viewModel.navigateTo(AppScreen.MediaDetail(it.id)) }
                )
            }

            // Popular TV series
            item {
                val tvShows = remember(allMediaList) { allMediaList.filter { it.category == "TV Show" } }
                MediaHorizontalRow(
                    title = "Popular TV Series 📺",
                    mediaItems = tvShows,
                    onMediaClick = { viewModel.navigateTo(AppScreen.MediaDetail(it.id)) }
                )
            }

            // Anime row
            item {
                val animeShows = remember(allMediaList) { allMediaList.filter { it.category == "Anime" } }
                MediaHorizontalRow(
                    title = "Anime & Cartoons Kyoto Collection ⛩️",
                    mediaItems = animeShows,
                    onMediaClick = { viewModel.navigateTo(AppScreen.MediaDetail(it.id)) }
                )
            }

            // Live streaming tv channels
            item {
                val liveChannels = remember(allMediaList) { allMediaList.filter { it.category == "Live Channel" } }
                MediaHorizontalRow(
                    title = "Live TV & Channels Broadcaster 📡",
                    mediaItems = liveChannels,
                    onMediaClick = { viewModel.navigateTo(AppScreen.MediaDetail(it.id)) }
                )
            }
        } else {
            // SEARCH RESULTS GRID
            item {
                Text(
                    text = "Search Curations (${filteredMedia.size} hits)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HighContrastText
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (filteredMedia.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(CinemaSurface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, "Empty Info", tint = Color.DarkGray)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No match found", color = HighContrastText, fontWeight = FontWeight.Bold)
                            Text("Try searching Action, Sci-Fi, Live, or another tag", color = SubtitleText, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(filteredMedia) { media ->
                    MediaSearchItemCard(media) {
                        viewModel.navigateTo(AppScreen.MediaDetail(media.id))
                    }
                }
            }
        }
    }
}

// Media Row items helper
@Composable
fun MediaHorizontalRow(title: String, mediaItems: List<DbMedia>, onMediaClick: (DbMedia) -> Unit) {
    if (mediaItems.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = HighContrastText
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(mediaItems) { m ->
                val parsedColor = remember(m.bannerColor) {
                    try {
                        Color(m.bannerColor.removePrefix("0x").toLong(16))
                    } catch (e: Exception) {
                        CinemaCard
                    }
                }

                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .testTag("media_card_${m.title}")
                        .clickable { onMediaClick(m) },
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .height(160.dp)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(parsedColor, parsedColor.copy(alpha = 0.6f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = m.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                // Category pill
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        m.category,
                                        fontSize = 7.sp,
                                        color = SubtitleText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // If premium tag
                            if (m.isPremium) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(AccentGold, RoundedCornerShape(bottomStart = 8.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "PLUS",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CinemaBlack
                                    )
                                }
                            }
                        }

                        // Info text
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = m.title,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = HighContrastText),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${m.year} • ${m.genre}",
                                    fontSize = 9.sp,
                                    color = SubtitleText
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "RatingStar",
                                        tint = AccentGold,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = " ${m.rating}",
                                        fontSize = 9.sp,
                                        color = HighContrastText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaSearchItemCard(media: DbMedia, onClick: () -> Unit) {
    val parsedColor = remember(media.bannerColor) {
        try {
            Color(media.bannerColor.removePrefix("0x").toLong(16))
        } catch (e: Exception) {
            CinemaCard
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(parsedColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = media.title.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    fontWeight = FontWeight.Bold,
                    color = HighContrastText,
                    fontSize = 14.sp
                )
                Text(
                    text = "${media.category} • ${media.year} • ${media.genre}",
                    color = SubtitleText,
                    fontSize = 12.sp
                )
                Text(
                    text = media.overview,
                    color = SubtitleText.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("➜", color = SubtitleText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 5. Chat Bot Screen (Hyper AI Tab)
// ==========================================

@Composable
fun ChatBotScreen(viewModel: MainViewModel) {
    val messages by viewModel.aiChatHistory.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberSerializableLazyListState()

    // Quick suggestions to speed up prompts
    val suggestionPrompts = listOf(
        "Recommend me 3 classic action thriller movies",
        "Introduce anime titles in the catalog",
        "Suggest family-friendly kids cartoons"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // AI Title Box info
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(NeonPurple.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, "AI Icon", tint = NeonPurple)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("HYPER AI MOVIE CURATOR", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = HighContrastText))
                    Text("Live neural curation matching your personal watch history", fontSize = 11.sp, color = SubtitleText)
                }
            }
        }

        // Messages streams
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isAi = msg.sender == "ai"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isAi) 0.dp else 12.dp,
                                    bottomEnd = if (isAi) 12.dp else 0.dp
                                )
                            )
                            .background(if (isAi) CinemaSurface else NeonPurple)
                            .border(
                                1.dp,
                                if (isAi) Color.DarkGray else NeonPurple,
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isAi) 0.dp else 12.dp,
                                    bottomEnd = if (isAi) 12.dp else 0.dp
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = if (isAi) HighContrastText else Color.White
                            )
                        }
                    }
                }
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = NeonPurple,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thinking...", fontSize = 11.sp, color = SubtitleText)
                    }
                }
            }
        }

        // Suggestion list (rendered when input is clean/empty)
        if (inputText.isBlank()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestionPrompts) { prompt ->
                    Surface(
                        modifier = Modifier.clickable {
                            viewModel.sendChatMessage(prompt)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = CinemaSurface,
                        border = BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            color = HighContrastText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Input Field text
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask Hyper AI...", color = SubtitleText) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("ai_assistant_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CinemaCard,
                    unfocusedContainerColor = CinemaCard,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = HighContrastText,
                    unfocusedTextColor = HighContrastText
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendChatMessage(inputText)
                            inputText = ""
                            focusManager.clearFocus()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .testTag("submit_message_button")
                    .size(48.dp)
                    .background(NeonPurple, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Direct",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun rememberSerializableLazyListState(): androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

// ==========================================
// 6. Download Center Screen (DOWNLOADS Tab)
// ==========================================

@Composable
fun DownloadsScreen(viewModel: MainViewModel) {
    val downloadsCenter by viewModel.downloads.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Downloads Queue",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HighContrastText)
        )
        Text(
            "Offline viewing hub • Fully synchronized downloads",
            fontSize = 11.sp,
            color = SubtitleText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Storage visual
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Device Storage", fontSize = 12.sp, color = HighContrastText, fontWeight = FontWeight.Bold)
                    Text("44.5 GB Free / 64.0 GB", fontSize = 11.sp, color = SubtitleText)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = 0.35f,
                    color = NeonPurple,
                    trackColor = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (downloadsCenter.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(CinemaSurface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⬇", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your Offline list is clean", color = HighContrastText, fontWeight = FontWeight.Bold)
                    Text("Find movies on the dashboard to trigger local caching!", color = SubtitleText, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(downloadsCenter) { d ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                        border = BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(d.title, fontWeight = FontWeight.Bold, color = HighContrastText)
                                    Text(
                                        "Status: ${d.status} • %${d.progressPercent} • ${d.speed}",
                                        fontSize = 11.sp,
                                        color = if (d.status == "Downloading") NeonPurple else SubtitleText
                                    )
                                }

                                Row {
                                    if (d.status == "Downloading") {
                                        IconButton(onClick = { viewModel.pauseDownload(d.id) }) {
                                            Text("⏸", color = HighContrastText, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (d.status == "Paused") {
                                        IconButton(onClick = { viewModel.resumeDownload(d.id) }) {
                                            Icon(Icons.Default.PlayArrow, "Resume", tint = NeonPurple)
                                        }
                                    } else if (d.status == "Completed") {
                                        IconButton(onClick = { viewModel.navigateTo(AppScreen.VideoPlayer(d.id)) }) {
                                            Text("▶", color = AccentGold, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteDownload(d.id) }) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.8f))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = d.progressPercent.toFloat() / 100f,
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = if (d.status == "Completed") AccentGold else NeonPurple,
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. Community Screen (COMMUNITY Tab)
// ==========================================

@Composable
fun CommunityScreen(viewModel: MainViewModel) {
    val allMediaList by viewModel.allMedia.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    var userReviewText by remember { mutableStateOf("") }
    var selectedMovieRating by remember { mutableStateOf(5.0f) }
    var selectedMovieToReview by remember { mutableStateOf<DbMedia?>(null) }

    val moviesWithComments = remember(allMediaList) {
        allMediaList.filter { viewModel.parseComments(it.commentsJson).isNotEmpty() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Community Forums & Reviews",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HighContrastText)
        )
        Text(
            "Hear what the Tucci Cyber Nation counts as premium!",
            fontSize = 11.sp,
            color = SubtitleText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Post a review form
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface),
            border = BorderStroke(1.dp, Color.DarkGray)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Submit Real-time Movie Review",
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple,
                    fontSize = 13.sp
                )

                // Dropdown mock or Select Movie trigger
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(CinemaBlack, RoundedCornerShape(4.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                        .clickable {
                            // Cycle selection
                            if (allMediaList.isNotEmpty()) {
                                val currentIdx = allMediaList.indexOf(selectedMovieToReview)
                                val nextIdx = (currentIdx + 1) % allMediaList.size
                                selectedMovieToReview = allMediaList[nextIdx]
                            }
                        }
                        .padding(10.dp)
                ) {
                    Text(
                        text = selectedMovieToReview?.title ?: "👉 Tap to choose Movie/Show to review",
                        color = if (selectedMovieToReview == null) SubtitleText else HighContrastText,
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Rating (1-5 Stars)", fontSize = 11.sp, color = SubtitleText)
                    Row {
                        (1..5).forEach { star ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .clickable { selectedMovieRating = star.toFloat() }
                            ) {
                                Text(
                                    text = if (star <= selectedMovieRating) "★" else "☆",
                                    color = AccentGold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = userReviewText,
                    onValueChange = { userReviewText = it },
                    placeholder = { Text("What did you think of the plot? Tell community...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CinemaBlack,
                        unfocusedContainerColor = CinemaBlack,
                        focusedTextColor = HighContrastText,
                        unfocusedTextColor = HighContrastText
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val selected = selectedMovieToReview
                        if (selected != null && userReviewText.isNotBlank()) {
                            viewModel.addComment(
                                mediaId = selected.id,
                                author = activeProfile?.name ?: "Tucci Guest",
                                text = userReviewText,
                                rating = selectedMovieRating
                            )
                            userReviewText = ""
                            selectedMovieToReview = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedMovieToReview != null && userReviewText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Broadcast Review Forum", fontWeight = FontWeight.Bold)
                }
            }
        }

        // List of submissions
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Trending Discussions",
                    fontWeight = FontWeight.Bold,
                    color = HighContrastText,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (moviesWithComments.isEmpty()) {
                item {
                    Text(
                        "No discussions posted yet. Be the first to share review!",
                        fontSize = 12.sp,
                        color = SubtitleText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    )
                }
            } else {
                items(moviesWithComments) { m ->
                    val comments = viewModel.parseComments(m.commentsJson)
                    comments.forEach { c ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(24.dp).background(NeonPurple, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(c.user.take(1).uppercase(), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(c.user, fontWeight = FontWeight.Bold, color = HighContrastText, fontSize = 13.sp)
                                    }
                                    Row {
                                        Icon(Icons.Default.Star, "rating star", tint = AccentGold, modifier = Modifier.size(14.dp))
                                        Text(" ${c.rating}", fontSize = 11.sp, color = HighContrastText, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Reviewed on: ${m.title}",
                                    fontSize = 10.sp,
                                    color = NeonPurple,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(c.comment, fontSize = 12.sp, color = SubtitleText)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. Administrative Settings Screen
// ==========================================

@Composable
fun SettingsAdminScreen(viewModel: MainViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val isPremiumAccount by viewModel.isPremiumAccount.collectAsStateWithLifecycle()
    val allMediaList by viewModel.allMedia.collectAsStateWithLifecycle()

    var showAdminForm by remember { mutableStateOf(false) }

    // Admin state bindings
    var inputTitle by remember { mutableStateOf("") }
    var inputOverview by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("Movie") }
    var inputGenre by remember { mutableStateOf("Action") }
    var inputBannerHex by remember { mutableStateOf("FF512DA8") }
    var inputDuration by remember { mutableStateOf("2h 10m") }
    var inputYear by remember { mutableStateOf(2026) }
    var inputDirectors by remember { mutableStateOf("Tucci Nation Studios") }
    var inputActors by remember { mutableStateOf("Emma Tucci, Giovanni T") }
    var inputPremiumFlag by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stripe Premium upgrade card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Hyper Movies Plus 💎",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AccentGold)
                            )
                            Text(
                                "Unlock Ad-free, 4K Cinema, Offline sync priority",
                                fontSize = 11.sp,
                                color = SubtitleText
                            )
                        }

                        if (isPremiumAccount) {
                            Box(
                                modifier = Modifier
                                    .background(AccentGold, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("ACTIVE", color = CinemaBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isPremiumAccount) {
                        Button(
                            onClick = { viewModel.activatePremiumPlus() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                        ) {
                            Text("Upgrade to Plus • $9.99/mo (Simulate Stripe)", color = CinemaBlack, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            "Upgrade Completed! You have unlocked unlimited dynamic UHD channels and ad-free priority support.",
                            fontSize = 11.sp,
                            color = HighContrastText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Active profile info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Profile: ${activeProfile?.name ?: "Guest"}", fontWeight = FontWeight.Bold, color = HighContrastText)
                        Text("Role: ${if (activeProfile?.isKids == true) "Kids Restricted" else "Adult Admin access"}", fontSize = 11.sp, color = SubtitleText)
                    }
                    Button(
                        onClick = { viewModel.logoutProfile() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                    ) {
                        Text("Log Out")
                    }
                }
            }
        }

        // Admin section (Allowed if profile is not kids)
        if (activeProfile?.isKids == false) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Admin Management Console",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HighContrastText)
                    )
                    IconButton(onClick = { showAdminForm = !showAdminForm }) {
                        Text(
                            text = if (showAdminForm) "[-] Collapse" else "[+] Expand",
                            color = NeonPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (showAdminForm) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                        border = BorderStroke(1.dp, NeonPurple)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("SeedTest - Inject new movie", fontWeight = FontWeight.Bold, color = NeonPurple)

                            TextField(
                                value = inputTitle,
                                onValueChange = { inputTitle = it },
                                placeholder = { Text("Title") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(focusedContainerColor = CinemaBlack)
                            )
                            TextField(
                                value = inputOverview,
                                onValueChange = { inputOverview = it },
                                placeholder = { Text("Overview") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(focusedContainerColor = CinemaBlack)
                            )

                            // Combo mocks for categories
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { inputCategory = "Movie" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (inputCategory == "Movie") NeonPurple else Color.DarkGray
                                    )
                                ) { Text("Movie") }

                                Button(
                                    onClick = { inputCategory = "TV Show" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (inputCategory == "TV Show") NeonPurple else Color.DarkGray
                                    )
                                ) { Text("TV Show") }

                                Button(
                                    onClick = { inputCategory = "Anime" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (inputCategory == "Anime") NeonPurple else Color.DarkGray
                                    )
                                ) { Text("Anime") }
                            }

                            TextField(
                                value = inputGenre,
                                onValueChange = { inputGenre = it },
                                placeholder = { Text("Genre (e.g. Action, Comedy)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(focusedContainerColor = CinemaBlack)
                            )

                            TextField(
                                value = inputBannerHex,
                                onValueChange = { inputBannerHex = it },
                                placeholder = { Text("Banner hex color (e.g. FF512DA8)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(focusedContainerColor = CinemaBlack)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Require Plus Premium? ", color = SubtitleText)
                                Switch(
                                    checked = inputPremiumFlag,
                                    onCheckedChange = { inputPremiumFlag = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = AccentGold)
                                )
                            }

                            Button(
                                onClick = {
                                    if (inputTitle.isNotBlank()) {
                                        viewModel.adminAddMedia(
                                            title = inputTitle,
                                            overview = inputOverview,
                                            category = inputCategory,
                                            genre = inputGenre,
                                            bannerHex = inputBannerHex,
                                            duration = inputDuration,
                                            year = inputYear,
                                            directors = inputDirectors,
                                            actors = inputActors,
                                            isPremium = inputPremiumFlag
                                        )
                                        // Reset fields
                                        inputTitle = ""
                                        inputOverview = ""
                                        showAdminForm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                            ) {
                                Text("Insert Movie to Stream DB", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Real-time Analytics Dashboard visualization section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Platform Analytics",
                            fontWeight = FontWeight.Bold,
                            color = HighContrastText,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Platform reports provided by Tucci Cyber Nation CDN",
                            fontSize = 11.sp,
                            color = SubtitleText,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Active Accounts", fontSize = 11.sp, color = SubtitleText)
                                Text("1,245,690", fontSize = 16.sp, fontWeight = FontWeight.Black, color = NeonPurple)
                            }
                            Column {
                                Text("Hrs Streamed", fontSize = 11.sp, color = SubtitleText)
                                Text("14,892,102 Hrs", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Column {
                                Text("Catalog Size", fontSize = 11.sp, color = SubtitleText)
                                Text("${allMediaList.size} Titles", fontSize = 16.sp, fontWeight = FontWeight.Black, color = AccentGold)
                            }
                        }
                    }
                }
            }

            // Database items deletion list
            item {
                Text("Database Catalog Admin Editor", fontWeight = FontWeight.Bold, color = HighContrastText)
            }

            val customList = allMediaList.filter { it.isCustom }
            if (customList.isEmpty()) {
                item {
                    Text("No custom movies loaded yet. Use form above to add!", fontSize = 11.sp, color = SubtitleText)
                }
            } else {
                items(customList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, color = HighContrastText)
                                Text("${item.category} • ${item.genre}", fontSize = 11.sp, color = SubtitleText)
                            }
                            IconButton(onClick = { viewModel.adminDeleteMedia(item) }) {
                                Icon(Icons.Default.Delete, "Delete from catalog", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. Media Detail Screen
// ==========================================

@Composable
fun MediaDetailScreen(viewModel: MainViewModel, mediaId: Int) {
    val media by viewModel.selectedMedia.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isCurrentInWatchlist.collectAsStateWithLifecycle()

    if (media == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NeonPurple)
        }
        return
    }

    val item = media!!

    val parsedColor = remember(item.bannerColor) {
        try {
            Color(item.bannerColor.removePrefix("0x").toLong(16))
        } catch (e: Exception) {
            CinemaCard
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero detail banner with back button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(parsedColor, CinemaBlack)
                    )
                )
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.MainHub) },
                modifier = Modifier
                    .testTag("detail_back_button")
                    .padding(30.dp)
                    .align(Alignment.TopStart)
                    .background(CinemaBlack.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back icon", tint = Color.White)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(AccentGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.isPremium) "PLUS" else "FREE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = CinemaBlack
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.genre,
                        fontSize = 12.sp,
                        color = AccentGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = HighContrastText
                    )
                )
            }
        }

        // Details Panel Body content
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.VideoPlayer(item.id)) },
                    modifier = Modifier.weight(1f).testTag("detail_play_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, "Play")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("START STREAMING", fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { viewModel.toggleWatchlist(item.id) },
                    modifier = Modifier
                        .background(CinemaSurface, RoundedCornerShape(10.dp))
                        .size(48.dp)
                        .testTag("detail_watchlist_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = "Watchlist toggle",
                        tint = if (isFavorite) AccentGold else HighContrastText
                    )
                }

                IconButton(
                    onClick = { viewModel.triggerDownload(item) },
                    modifier = Modifier
                        .background(CinemaSurface, RoundedCornerShape(10.dp))
                        .size(48.dp)
                        .testTag("detail_download_button")
                ) {
                    Text(
                        text = "⬇",
                        color = HighContrastText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            // Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, "Rating", tint = AccentGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${item.rating}", color = HighContrastText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Text("${item.year}", color = SubtitleText, fontSize = 13.sp)
                Text(item.duration, color = SubtitleText, fontSize = 13.sp)
                Text("HDR 4K UHD", color = SubtitleText, fontSize = 13.sp)
            }

            // Overview Text
            Text(
                text = item.overview,
                style = MaterialTheme.typography.bodyMedium,
                color = SubtitleText
            )

            // Cast members
            Column {
                Text("Cast Details", fontWeight = FontWeight.Bold, color = HighContrastText, fontSize = 14.sp)
                Text("Starring: ${item.actors}", fontSize = 12.sp, color = SubtitleText)
                Text("Director: ${item.directors}", fontSize = 12.sp, color = SubtitleText)
            }

            // Expandable Season Selector or Episodes UI list (Representing TV Shows & Anime)
            if (item.category == "TV Show" || item.category == "Anime") {
                Column {
                    Text(
                        "Season 1 Episodes",
                        fontWeight = FontWeight.Bold,
                        color = HighContrastText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    (1..4).forEach { ep ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.navigateTo(AppScreen.VideoPlayer(item.id)) },
                            colors = CardDefaults.cardColors(containerColor = CinemaSurface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .background(parsedColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, "Episode playing", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Episode $ep: Quantum Link Expansion", fontWeight = FontWeight.Bold, color = HighContrastText, fontSize = 13.sp)
                                    Text("Duration: 45m", color = SubtitleText, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Subtitles and translations
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Audio & Translation tracks", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HighContrastText)
                    Text("Audio: ${item.audioTracks}", fontSize = 11.sp, color = SubtitleText)
                    Text("Subtitles available: ${item.subtitles}", fontSize = 11.sp, color = SubtitleText)
                }
            }
        }
    }
}

// ==========================================
// 10. Immersive Mock Video Player Screen
// ==========================================

@Composable
fun VideoPlayerScreen(viewModel: MainViewModel, mediaId: Int) {
    val media by viewModel.selectedMedia.collectAsStateWithLifecycle()

    var isPlaying by remember { mutableStateOf(true) }
    var playbackProgressSeconds by remember { mutableStateOf(120) }
    val totalProgressSeconds = 7200 // 2 Hours

    // Settings selectors states
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }

    // Mock interactive volume/brightness gestures sliders
    var brightGesturePercent by remember { mutableStateOf(75) }
    var volumeGesturePercent by remember { mutableStateOf(60) }

    val activeAudio by viewModel.selectedAudioTrack.collectAsStateWithLifecycle()
    val activeSub by viewModel.selectedSubtitle.collectAsStateWithLifecycle()
    val activeSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val activeQuality by viewModel.streamingQuality.collectAsStateWithLifecycle()

    // Trigger history save on close
    DisposableEffect(key1 = mediaId) {
        onDispose {
            viewModel.recordWatchHistory(mediaId, playbackProgressSeconds, totalProgressSeconds)
        }
    }

    if (media == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NeonPurple)
        }
        return
    }

    val item = media!!

    // Run active watch progress clock ticking
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (playbackProgressSeconds < totalProgressSeconds) {
                kotlinx.coroutines.delay(1000)
                playbackProgressSeconds += 1
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
    ) {
        // Player Video Canvas - Render custom dynamic geometric pulse representation representing streaming track!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            try { Color(item.bannerColor.removePrefix("0x").toLong(16)).copy(alpha = 0.4f) }
                            catch (e: Exception) { CinemaCard },
                            CinemaBlack
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .border(2.dp, NeonPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPlaying) "⏸" else "▶",
                            color = NeonPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isPlaying) "Streaming in Ultra 4K UHD" else "Playback Paused",
                    fontWeight = FontWeight.Bold,
                    color = HighContrastText,
                    fontSize = 16.sp
                )
                Text(
                    text = "Adaptive buffering via Cloudflare CDN",
                    color = SubtitleText,
                    fontSize = 11.sp
                )
            }
        }

        // Top controls bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.MediaDetail(item.id)) },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.title, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Quality: $activeQuality • Audio: $activeAudio", color = SubtitleText, fontSize = 11.sp)
                }
            }

            IconButton(
                onClick = { /* Chromecast connection simulation */ },
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Share, "Share", tint = Color.White)
            }
        }

        // Left/Right customized swipe adjustment simulation tags (Brightness & Volume controls)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔆", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$brightGesturePercent%",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Click steps
            IconButton(
                onClick = { brightGesturePercent = (brightGesturePercent + 10).coerceIn(10, 100) },
                modifier = Modifier.size(24.dp)
            ) { Icon(Icons.Default.Add, "brightness up", tint = Color.White) }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔊", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$volumeGesturePercent%",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            IconButton(
                onClick = { volumeGesturePercent = (volumeGesturePercent + 10).coerceIn(0, 100) },
                modifier = Modifier.size(24.dp)
            ) { Icon(Icons.Default.Add, "volume up", tint = Color.White) }
        }

        // Bottom timeline seek bar and controls panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            val progressPercentage = playbackProgressSeconds.toFloat() / totalProgressSeconds.toFloat()

            // Time strings: formatted hh:mm:ss
            val formattedCurrent = remember(playbackProgressSeconds) {
                val h = playbackProgressSeconds / 3600
                val m = (playbackProgressSeconds % 3600) / 60
                val s = playbackProgressSeconds % 60
                String.format("%02d:%02d:%02d", h, m, s)
            }
            val formattedTotal = "02:00:00"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formattedCurrent, color = Color.White, fontSize = 11.sp)

                // Linear Timeline Seekbar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .height(18.dp)
                        .clickable {
                            // Seed random seek progress
                            playbackProgressSeconds = (300..totalProgressSeconds).random()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    LinearProgressIndicator(
                        progress = progressPercentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = NeonPurple,
                        trackColor = Color.DarkGray
                    )
                }

                Text(formattedTotal, color = Color.White, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Control Buttons: Subtitle languages, speed, video quality audio triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subtitles
                TextButton(onClick = { showSubtitleDialog = true }) {
                    Icon(Icons.Default.Settings, "Subs Settings", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Subs: $activeSub", color = Color.White, fontSize = 11.sp)
                }

                // Audio Track
                TextButton(onClick = { showAudioDialog = true }) {
                    Icon(Icons.Default.Settings, "Audio Settings", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Audio: $activeAudio", color = Color.White, fontSize = 11.sp)
                }

                // Speed
                TextButton(onClick = { showSpeedDialog = true }) {
                    Icon(Icons.Default.Settings, "Speed Settings", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Speed: $activeSpeed", color = Color.White, fontSize = 11.sp)
                }

                // Adaptive stream quality lines selector
                TextButton(onClick = { showQualityDialog = true }) {
                    Icon(Icons.Default.Settings, "Quality Settings", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(activeQuality, color = Color.White, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Skip recap and Skip Intro overlay tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        // Skip 85 seconds forward (Simulator skip intro recap!)
                        playbackProgressSeconds = (playbackProgressSeconds + 85).coerceAtMost(totalProgressSeconds)
                    },
                    modifier = Modifier.testTag("skip_intro_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White, containerColor = CinemaSurface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text("Skip Intro / Recap ➜", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Dialogue modals trigger overlays
        if (showSubtitleDialog) {
            AlertDialog(
                onDismissRequest = { showSubtitleDialog = false },
                title = { Text("Subtitle Settings") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Off", "English", "Spanish", "French", "Japanese").forEach { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectedSubtitle.value = s
                                        showSubtitleDialog = false
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s, color = HighContrastText)
                                if (activeSub == s) Icon(Icons.Default.Check, "Selected", tint = NeonPurple)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showSubtitleDialog = false }) { Text("Dismiss") } }
            )
        }

        if (showAudioDialog) {
            AlertDialog(
                onDismissRequest = { showAudioDialog = false },
                title = { Text("Audio Track Settings") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("English Stereo", "English 5.1 Surround", "Spanish Audio", "Japanese Audio (Original)").forEach { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectedAudioTrack.value = s
                                        showAudioDialog = false
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s, color = HighContrastText)
                                if (activeAudio == s) Icon(Icons.Default.Check, "Selected", tint = NeonPurple)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showAudioDialog = false }) { Text("Dismiss") } }
            )
        }

        if (showSpeedDialog) {
            AlertDialog(
                onDismissRequest = { showSpeedDialog = false },
                title = { Text("Playback Speed") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("0.5x", "1.0x", "1.5x", "2.0x").forEach { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.playbackSpeed.value = s
                                        showSpeedDialog = false
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s, color = HighContrastText)
                                if (activeSpeed == s) Icon(Icons.Default.Check, "Selected", tint = NeonPurple)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showSpeedDialog = false }) { Text("Dismiss") } }
            )
        }

        if (showQualityDialog) {
            AlertDialog(
                onDismissRequest = { showQualityDialog = false },
                title = { Text("Select Stream Quality (Adaptive)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Auto (1080p)", "Auto (4K UHD)", "720p HD", "480p SD").forEach { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.streamingQuality.value = s
                                        showQualityDialog = false
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s, color = HighContrastText)
                                if (activeQuality == s) Icon(Icons.Default.Check, "Selected", tint = NeonPurple)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showQualityDialog = false }) { Text("Dismiss") } }
            )
        }
    }
}
