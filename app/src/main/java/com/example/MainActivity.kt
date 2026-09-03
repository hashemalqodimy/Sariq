package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AmanPhoneDatabase
import com.example.data.repository.PhoneReportRepository
import com.example.ui.AmanPhoneViewModel
import com.example.ui.AmanPhoneViewModelFactory
import com.example.ui.components.AppInfoDialog
import com.example.ui.components.ReportDetailDialog
import com.example.ui.components.YemenFlagBadge
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImeiCheckScreen
import com.example.ui.screens.NewReportScreen
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AlertRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.util.NotificationHelper

enum class NavDestination(val title: String, val icon: ImageVector) {
    HOME("الرئيسية", Icons.Default.Home),
    IMEI_CHECK("فحص الـ IMEI", Icons.Default.Search),
    NEW_REPORT("إبلاغ جديد", Icons.Default.AddCircle),
    ALERTS("التنبيهات", Icons.Default.Notifications)
}

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel
        NotificationHelper.createNotificationChannel(applicationContext)

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val database = AmanPhoneDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = PhoneReportRepository(
            database.reportDao(),
            database.alertDao(),
            database.imeiCheckDao(),
            applicationContext
        )

        val viewModel: AmanPhoneViewModel by viewModels {
            AmanPhoneViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AmanPhoneMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmanPhoneMainApp(viewModel: AmanPhoneViewModel) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var currentUserName by remember { mutableStateOf("هاشم القديمي") }
    var currentUserEmail by remember { mutableStateOf("hashem@amanphone.ye") }
    var showAppInfoDialog by remember { mutableStateOf(false) }

    if (!isAuthenticated) {
        AuthScreen(
            onAuthSuccess = { name, email ->
                currentUserName = name
                currentUserEmail = email
                isAuthenticated = true
            }
        )
        return
    }

    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }
    val unreadAlertsCount by viewModel.unreadAlertsCount.collectAsStateWithLifecycle()
    val selectedReport by viewModel.selectedReport.collectAsStateWithLifecycle()
    val successMessage by viewModel.submissionSuccessMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        YemenFlagBadge()
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "منصة بلاغات الهواتف المسروقة - اليمن",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showAppInfoDialog = true },
                        modifier = Modifier.testTag("top_bar_info_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryBlue.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "معلومات التطبيق والمطور",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { currentDestination = NavDestination.ALERTS },
                        modifier = Modifier.testTag("top_bar_alerts_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadAlertsCount > 0) {
                                    Badge(
                                        containerColor = AlertRed,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = unreadAlertsCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "التنبيهات",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            if (destination == NavDestination.ALERTS && unreadAlertsCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = AlertRed,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = unreadAlertsCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(imageVector = destination.icon, contentDescription = destination.title)
                                }
                            } else {
                                Icon(imageVector = destination.icon, contentDescription = destination.title)
                            }
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                NavDestination.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToNewReport = { currentDestination = NavDestination.NEW_REPORT },
                    onNavigateToAlerts = { currentDestination = NavDestination.ALERTS }
                )

                NavDestination.IMEI_CHECK -> ImeiCheckScreen(
                    viewModel = viewModel
                )

                NavDestination.NEW_REPORT -> NewReportScreen(
                    viewModel = viewModel,
                    onReportSubmitted = {
                        currentDestination = NavDestination.HOME
                    }
                )

                NavDestination.ALERTS -> AlertsScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    // Detail Dialog when a report is selected
    selectedReport?.let { report ->
        ReportDetailDialog(
            report = report,
            onDismiss = { viewModel.selectReport(null) },
            onMarkRecovered = { r ->
                viewModel.markReportAsRecovered(r)
            }
        )
    }

    // Success Dialog on New Report submission
    if (successMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSuccessMessage() },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تم نشر البلاغ بنجاح!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF065F46)
                )
            },
            text = {
                Text(
                    text = "تم بث وتعميم التنبيه الفوري لكافة محلات الجوالات والمستخدمين في محافظات الجمهورية اليمنية بنجاح.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearSuccessMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("تم")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // App & Developer Info Dialog
    if (showAppInfoDialog) {
        AppInfoDialog(
            currentUser = currentUserName,
            userEmail = currentUserEmail,
            onLogout = {
                isAuthenticated = false
            },
            onDismiss = {
                showAppInfoDialog = false
            }
        )
    }
}

// Keep Greeting composable so GreetingScreenshotTest remains compatible
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
