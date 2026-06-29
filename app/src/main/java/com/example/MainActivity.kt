package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseRepo
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NearBlackBg
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SurfaceCard
import com.example.util.AppLanguage
import com.example.util.Translator

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val repo = remember { FirebaseRepo.getInstance(context) }
                
                // Track dynamic language selection (saves immediately, updates entire layout)
                var language by remember { 
                    val sp = context.getSharedPreferences("autoqr_prefs", MODE_PRIVATE)
                    val savedLangCode = sp.getString("app_language", "en") ?: "en"
                    val parsedLang = AppLanguage.values().firstOrNull { it.code == savedLangCode } ?: AppLanguage.ENGLISH
                    mutableStateOf(parsedLang)
                }

                val onLanguageChanged: (AppLanguage) -> Unit = { newLang ->
                    language = newLang
                    context.getSharedPreferences("autoqr_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("app_language", newLang.code)
                        .apply()
                }

                val currentUser by repo.currentUser.collectAsState()
                val currentTrip by repo.currentTrip.collectAsState()
                val isConnected by repo.isInternetConnected.collectAsState()

                // State routers
                var activeScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
                var selectedTripIdForPreview by remember { mutableStateOf("") }

                // Check deep link intents
                LaunchedEffect(intent?.data) {
                    intent?.data?.let { uri ->
                        if (uri.scheme == "autoqr" && uri.host == "trip") {
                            val tripId = uri.lastPathSegment
                            if (tripId != null) {
                                selectedTripIdForPreview = tripId
                                if (currentUser != null) {
                                    activeScreen = AppScreen.PassengerTripPreview
                                }
                            }
                        }
                    }
                }

                // Auth state synchronization
                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        activeScreen = AppScreen.Auth
                    } else if (activeScreen == AppScreen.Splash || activeScreen == AppScreen.Auth) {
                        if (currentUser?.role == "driver") {
                            activeScreen = AppScreen.DriverDashboard
                        } else if (currentUser?.role == "passenger") {
                            activeScreen = AppScreen.PassengerDashboard
                        } else {
                            activeScreen = AppScreen.RoleSelect
                        }
                    }
                }

                // Global Scaffold layout wrapper with background, offline banner
                Scaffold(
                    modifier = Modifier.fillMaxSize().background(NearBlackBg),
                    topBar = {
                        Column {
                            NoInternetBanner(visible = !isConnected, language = language)
                            
                            // Highly stylized top banner for AutoQR branding
                            if (currentUser != null && 
                                activeScreen != AppScreen.DriverActiveTrip && 
                                activeScreen != AppScreen.PassengerActiveTrip) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceCard)
                                        .statusBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "AutoQR 🚗",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronPrimary
                                    )
                                    Text(
                                        text = if (currentUser?.role == "driver") "DRIVER" else "PASSENGER",
                                        fontSize = 11.sp,
                                        color = SaffronPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        // Display bottom bar only if logged in and not in active transit screens
                        if (currentUser != null &&
                            activeScreen != AppScreen.RoleSelect &&
                            activeScreen != AppScreen.DriverTripWaiting &&
                            activeScreen != AppScreen.DriverActiveTrip &&
                            activeScreen != AppScreen.DriverTripCompleted &&
                            activeScreen != AppScreen.PassengerTripPreview &&
                            activeScreen != AppScreen.PassengerActiveTrip &&
                            activeScreen != AppScreen.PassengerPayment) {
                            
                            NavigationBar(
                                containerColor = SurfaceCard,
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                if (currentUser?.role == "driver") {
                                    // DRIVER NAVIGATION
                                    NavigationBarItem(
                                        selected = activeScreen == AppScreen.DriverDashboard,
                                        onClick = { activeScreen = AppScreen.DriverDashboard },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                                        label = { Text(Translator.translate("nav_dashboard", language), fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = SaffronPrimary,
                                            indicatorColor = SaffronPrimary,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = activeScreen == AppScreen.DriverQr,
                                        onClick = { activeScreen = AppScreen.DriverQr },
                                        icon = { Icon(Icons.Default.Share, contentDescription = "My QR") },
                                        label = { Text(Translator.translate("nav_qr_code", language), fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = SaffronPrimary,
                                            indicatorColor = SaffronPrimary,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = activeScreen == AppScreen.DriverHistory,
                                        onClick = { activeScreen = AppScreen.DriverHistory },
                                        icon = { Icon(Icons.Default.List, contentDescription = "History") },
                                        label = { Text(Translator.translate("nav_history", language), fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = SaffronPrimary,
                                            indicatorColor = SaffronPrimary,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = activeScreen == AppScreen.DriverSettings,
                                        onClick = { activeScreen = AppScreen.DriverSettings },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                        label = { Text(Translator.translate("nav_settings", language), fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = SaffronPrimary,
                                            indicatorColor = SaffronPrimary,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )
                                } else {
                                    // PASSENGER NAVIGATION
                                    NavigationBarItem(
                                        selected = activeScreen == AppScreen.PassengerDashboard,
                                        onClick = { activeScreen = AppScreen.PassengerDashboard },
                                        icon = { Icon(Icons.Default.Search, contentDescription = "Scan") },
                                        label = { Text("Scan QR", fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = SaffronPrimary,
                                            indicatorColor = SaffronPrimary,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = activeScreen == AppScreen.PassengerHistory,
                                        onClick = { activeScreen = AppScreen.PassengerHistory },
                                        icon = { Icon(Icons.Default.List, contentDescription = "History") },
                                        label = { Text(Translator.translate("nav_history", language), fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = SaffronPrimary,
                                            indicatorColor = SaffronPrimary,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NearBlackBg)
                            .padding(innerPadding)
                    ) {
                        when (activeScreen) {
                            AppScreen.Splash -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = SaffronPrimary)
                                }
                            }

                            AppScreen.Auth -> {
                                AuthScreen(
                                    repo = repo,
                                    language = language,
                                    onAuthenticated = { uid ->
                                        // Once authenticated, route accordingly
                                        if (currentUser != null) {
                                            activeScreen = if (currentUser?.role == "driver") AppScreen.DriverDashboard else AppScreen.PassengerDashboard
                                        } else {
                                            activeScreen = AppScreen.RoleSelect
                                        }
                                    }
                                )
                            }

                            AppScreen.RoleSelect -> {
                                RoleSelectionScreen(
                                    repo = repo,
                                    uid = currentUser?.uid ?: "temp_uid",
                                    phone = currentUser?.phone ?: "+919876543210",
                                    language = language,
                                    onRoleCompleted = {
                                        activeScreen = if (repo.currentUser.value?.role == "driver") {
                                            AppScreen.DriverDashboard
                                        } else {
                                            AppScreen.PassengerDashboard
                                        }
                                    }
                                )
                            }

                            // DRIVER SCREENS
                            AppScreen.DriverDashboard -> {
                                DriverDashboardScreen(
                                    repo = repo,
                                    language = language,
                                    onStartTrip = {
                                        repo.startTrip(currentUser!!) { tripId ->
                                            activeScreen = AppScreen.DriverTripWaiting
                                        }
                                    },
                                    onGoToSettings = { activeScreen = AppScreen.DriverSettings }
                                )
                            }

                            AppScreen.DriverQr -> {
                                DriverQrScreen(repo = repo, language = language)
                            }

                            AppScreen.DriverTripWaiting -> {
                                DriverTripWaitingScreen(
                                    repo = repo,
                                    tripId = currentTrip?.tripId ?: "",
                                    language = language,
                                    onActiveTripStarted = { activeScreen = AppScreen.DriverActiveTrip },
                                    onCancel = {
                                        repo.clearActiveTrip()
                                        activeScreen = AppScreen.DriverDashboard
                                    }
                                )
                            }

                            AppScreen.DriverActiveTrip -> {
                                DriverActiveTripScreen(
                                    repo = repo,
                                    language = language,
                                    onTripEnded = { activeScreen = AppScreen.DriverTripCompleted }
                                )
                            }

                            AppScreen.DriverTripCompleted -> {
                                DriverTripCompletedScreen(
                                    repo = repo,
                                    language = language,
                                    onDone = { activeScreen = AppScreen.DriverDashboard }
                                )
                            }

                            AppScreen.DriverHistory -> {
                                TripHistoryScreen(repo = repo, language = language)
                            }

                            AppScreen.DriverSettings -> {
                                DriverSettingsScreen(
                                    repo = repo,
                                    language = language,
                                    onLanguageSwitched = onLanguageChanged
                                )
                            }

                            // PASSENGER SCREENS
                            AppScreen.PassengerDashboard -> {
                                PassengerDashboardScreen(
                                    repo = repo,
                                    language = language,
                                    onTripScanned = { tripId ->
                                        selectedTripIdForPreview = tripId
                                        activeScreen = AppScreen.PassengerTripPreview
                                    }
                                )
                            }

                            AppScreen.PassengerTripPreview -> {
                                PassengerTripPreviewScreen(
                                    repo = repo,
                                    tripId = selectedTripIdForPreview,
                                    language = language,
                                    onTripConfirmed = { activeScreen = AppScreen.PassengerActiveTrip },
                                    onCancel = { activeScreen = AppScreen.PassengerDashboard }
                                )
                            }

                            AppScreen.PassengerActiveTrip -> {
                                PassengerActiveTripScreen(
                                    repo = repo,
                                    language = language,
                                    onTripCompleted = { activeScreen = AppScreen.PassengerPayment }
                                )
                            }

                            AppScreen.PassengerPayment -> {
                                PassengerPaymentScreen(
                                    repo = repo,
                                    language = language,
                                    onPaymentDone = { activeScreen = AppScreen.PassengerHistory }
                                )
                            }

                            AppScreen.PassengerHistory -> {
                                PassengerHistoryScreen(repo = repo, language = language)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class AppScreen {
    Splash,
    Auth,
    RoleSelect,
    DriverDashboard,
    DriverQr,
    DriverTripWaiting,
    DriverActiveTrip,
    DriverTripCompleted,
    DriverHistory,
    DriverSettings,
    PassengerDashboard,
    PassengerTripPreview,
    PassengerActiveTrip,
    PassengerPayment,
    PassengerHistory
}
