package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TodoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val selectedTheme by viewModel.selectedTheme.collectAsState()

            MyApplicationTheme(selectedTheme = selectedTheme) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: TodoViewModel
) {
    var currentTab by remember { mutableStateOf(0) } // 0 = Dashboard, 1 = Calendar, 2 = Notes, 3 = Settings

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard Tab"
                        )
                    },
                    label = { Text("Dashboard") },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 1) Icons.Filled.CalendarToday else Icons.Outlined.CalendarToday,
                            contentDescription = "Calendar Tab"
                        )
                    },
                    label = { Text("Calendar") },
                    modifier = Modifier.testTag("nav_tab_calendar")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 2) Icons.Filled.Timer else Icons.Outlined.Timer,
                            contentDescription = "Focus Tab"
                        )
                    },
                    label = { Text("Focus") },
                    modifier = Modifier.testTag("nav_tab_focus")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 3) Icons.Filled.Description else Icons.Outlined.Description,
                            contentDescription = "Notes Tab"
                        )
                    },
                    label = { Text("Scratchpad") },
                    modifier = Modifier.testTag("nav_tab_notes")
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 4) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings Tab"
                        )
                    },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentTab,
            modifier = Modifier.padding(innerPadding),
            label = "tab_cross_fade"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToFocus = { currentTab = 2 }
                )
                1 -> CalendarScreen(viewModel = viewModel)
                2 -> FocusTimerScreen(viewModel = viewModel)
                3 -> NotesScreen(viewModel = viewModel)
                4 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
