package com.example

import java.util.Calendar
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardCapitalization

// Helper colors for notes (soft modern pastels)
val NoteColors = listOf(
    Color(0xFFFFF9C4), // Soft Yellow
    Color(0xFFE8F5E9), // Soft Green
    Color(0xFFE3F2FD), // Soft Blue
    Color(0xFFFCE4EC), // Soft Pink
    Color(0xFFF3E5F5), // Soft Lavender
    Color(0xFFE0F7FA)  // Soft Teal
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TodoViewModel,
    onNavigateToFocus: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val events by viewModel.events.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showAiAssistant by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<TaskCategory?>(null) }
    var selectedFolderFilter by remember { mutableStateOf("All") } // "All" or folder name
    var selectedStatusFilter by remember { mutableStateOf("All") } // "All", "Pending", "Completed"

    // Filter tasks comprehensively
    val filteredTasks = tasks.filter { task ->
        val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) || 
                            task.description.contains(searchQuery, ignoreCase = true) ||
                            task.tags.any { it.contains(searchQuery, ignoreCase = true) }
        val matchesCategory = selectedCategoryFilter == null || task.category == selectedCategoryFilter
        val matchesFolder = selectedFolderFilter == "All" || task.folder == selectedFolderFilter
        val matchesStatus = when (selectedStatusFilter) {
            "Completed" -> task.isCompleted
            "Pending" -> !task.isCompleted
            else -> true
        }
        matchesSearch && matchesCategory && matchesFolder && matchesStatus
    }

    // Calculations for progress gauge
    val totalCount = tasks.size
    val completedCount = tasks.count { it.isCompleted }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val animateProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 800)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Modern Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, $userName 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Keep track of your projects & ideas",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Banner Gauge Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("progress_banner_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = "Daily Productivity",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (totalCount > 0) {
                                "$completedCount of $totalCount tasks completed (${(progressFraction * 100).toInt()}%)"
                            } else {
                                "No tasks created yet!"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { animateProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Spark Streak Indicator
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak Flame",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "5 Days",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Planner Copilot Widget Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showAiAssistant = true }
                    .testTag("dashboard_ai_copilot_widget"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Assistant AI Copilot ✨",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tell your AI what to add — lists, notes, events, or tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open AI Chat Dialog",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Focus Hub Widget
            val activeTimerLabel by viewModel.activeTimerLabel.collectAsState()
            val activeTimerRemaining by viewModel.activeTimerRemainingSeconds.collectAsState()
            val activeTimerRunning by viewModel.activeTimerRunning.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigateToFocus?.invoke() }
                    .testTag("dashboard_focus_widget"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Focus Lounge ⚡",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (activeTimerRunning) "Ticking down: $activeTimerLabel" else "Ready: $activeTimerLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Digital Clock readout mini
                        val mins = String.format(java.util.Locale.US, "%02d", activeTimerRemaining / 60)
                        val secs = String.format(java.util.Locale.US, "%02d", activeTimerRemaining % 60)
                        Text(
                            text = "$mins:$secs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Inline play/pause trigger
                        IconButton(
                            onClick = {
                                if (activeTimerRunning) {
                                    viewModel.pauseTimer()
                                } else {
                                    viewModel.startTimer()
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (activeTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (activeTimerRunning) "Pause" else "Start",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upcoming Schedule & Events Widget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Upcoming Events 📅",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("upcoming_events_title")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${events.count { !it.isCompleted }} Active",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(
                    onClick = { showAddEventDialog = true },
                    modifier = Modifier.testTag("add_event_widget_btn").size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Schedule Event",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "No events today",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No events scheduled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(148.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        EventListItemCard(
                            event = event,
                            onToggleComplete = { viewModel.toggleEventComplete(event.id) },
                            onDelete = { viewModel.deleteEvent(event.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar & Filter Actions
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search title, description, or #tags...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Clear icon")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Folders / Lists Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Folders & Lists 📁",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // Add Folder Button
                var showAddFolderDialog by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showAddFolderDialog = true },
                    modifier = Modifier.testTag("add_folder_btn").size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "Create Folder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (showAddFolderDialog) {
                    var newFolderName by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showAddFolderDialog = false },
                        title = { Text("New Folder Name") },
                        text = {
                            OutlinedTextField(
                                value = newFolderName,
                                onValueChange = { newFolderName = it },
                                label = { Text("Folder / Project") },
                                modifier = Modifier.fillMaxWidth().testTag("new_folder_input"),
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newFolderName.isNotBlank()) {
                                        viewModel.addFolder(newFolderName)
                                        selectedFolderFilter = newFolderName.trim()
                                    }
                                    showAddFolderDialog = false
                                }
                            ) {
                                Text("Create")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddFolderDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFolderFilter == "All",
                        onClick = { selectedFolderFilter = "All" },
                        label = { Text("All Folders") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("folder_chip_all")
                    )
                }
                items(folders) { folderName ->
                    FilterChip(
                        selected = selectedFolderFilter == folderName,
                        onClick = { selectedFolderFilter = folderName },
                        label = { Text(folderName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("folder_chip_$folderName")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Horizontal Filter Row
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All Tasks") },
                        leadingIcon = { Icon(Icons.Default.Category, "All icon", modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                items(TaskCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategoryFilter == category,
                        onClick = { selectedCategoryFilter = category },
                        label = { Text("${category.icon} ${category.label}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Tabs Selector: All | Pending | Completed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("All", "Pending", "Completed").forEach { status ->
                    val isSelected = selectedStatusFilter == status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable { selectedStatusFilter = status }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = status,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tasks List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Checklist,
                            contentDescription = "Empty tasks indicator",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tasks found in this section",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tap the float action button at the bottom right to record a target note or deadline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskListItemCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskComplete(task.id) },
                            onDelete = { viewModel.deleteTask(task.id) },
                            onToggleFlagged = { viewModel.toggleTaskFlagged(task.id) },
                            onToggleSubtaskComplete = { subId -> viewModel.toggleSubtaskComplete(task.id, subId) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .testTag("add_task_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, "Add task icon", modifier = Modifier.size(28.dp))
        }
    }

    if (showAddTaskDialog) {
        CreateTaskDialog(
            viewModel = viewModel,
            onDismiss = { showAddTaskDialog = false },
            onAdd = { task ->
                viewModel.addTask(task)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddEventDialog) {
        CreateEventDialog(
            onDismiss = { showAddEventDialog = false },
            onAdd = { event ->
                viewModel.addEvent(event)
                showAddEventDialog = false
            }
        )
    }

    if (showAiAssistant) {
        AiAssistantDialog(
            viewModel = viewModel,
            onDismiss = { showAiAssistant = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDialog(
    viewModel: TodoViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.aiMessages.collectAsState()
    val isLoading by viewModel.aiIsLoading.collectAsState()
    val error by viewModel.aiError.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("ai_assistant_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header of AI Modal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Assistant AI Copilot ✨",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Powered by Gemini 3.5 Flash",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.clearAiChat() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Clear chat thread",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close panel",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Chat bubble list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        val isUser = message.isUser
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = if (isUser) 18.dp else 4.dp,
                                    topEnd = if (isUser) 4.dp else 18.dp,
                                    bottomStart = 18.dp,
                                    bottomEnd = 14.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .testTag(if (isUser) "user_chat_bubble" else "ai_chat_bubble"),
                                border = if (!isUser) {
                                    androidx.compose.foundation.BorderStroke(
                                        width = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                    )
                                } else null
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = message.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUser) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "AI is aligning your schedule...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Error Warning Notice Banner
                error?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Error: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Bottom entry/send bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Ask AI to coordinate something...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInput.trim().isNotEmpty() && !isLoading) {
                                    viewModel.sendAiMessage(textInput)
                                    textInput = ""
                                }
                            }
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (textInput.trim().isNotEmpty() && !isLoading) {
                                viewModel.sendAiMessage(textInput)
                                textInput = ""
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading && textInput.trim().isNotEmpty(),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("ai_send_btn"),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send text"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskListItemCard(
    task: TodoTask,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onToggleFlagged: () -> Unit,
    onToggleSubtaskComplete: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val completedSubtasks = task.subtasks.count { it.isCompleted }
    val totalSubtasks = task.subtasks.size
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_card_${task.id}")
            .border(
                width = if (task.isFlagged) 1.5.dp else 0.dp,
                color = if (task.isFlagged) Color(0xFFFFD54F) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else if (task.isFlagged) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isFlagged) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Elegant completion Checkbox Button
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onToggleComplete() }
                        .testTag("task_checkbox_button_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed check icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Text Titles & Information
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = task.category.icon + " " + task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (task.isCompleted) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Priority Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(task.priority.color).copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.priority.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(task.priority.color)
                            )
                        }

                        // Folder Project chip (if not Inbox)
                        if (task.folder.isNotEmpty() && task.folder != "Inbox") {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "📁 " + task.folder,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    if (task.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                }
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Render mini tags
                    if (task.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            task.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    if (task.dueDate.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar clock",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.dueDate,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Action Column: Flag + Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Flag Option
                    IconButton(
                        onClick = { onToggleFlagged() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (task.isFlagged) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = "Flag task as important",
                            tint = if (task.isFlagged) Color(0xFFFFD54F) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Action: Delete Icon Task
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier
                            .testTag("task_delete_button_${task.id}")
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete task item",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expanded Collapsible subtasks
            if (totalSubtasks > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isExpanded = !isExpanded }
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Checklist",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Checklist ($completedSubtasks/$totalSubtasks)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // mini linear indicator
                    LinearProgressIndicator(
                        progress = { completedSubtasks.toFloat() / totalSubtasks },
                        modifier = Modifier
                            .width(62.dp)
                            .height(5.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
                
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        task.subtasks.forEach { subtask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onToggleSubtaskComplete(subtask.id)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (subtask.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (subtask.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (subtask.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    viewModel: TodoViewModel,
    onDismiss: () -> Unit,
    onAdd: (TodoTask) -> Unit
) {
    val folders by viewModel.folders.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.PERSONAL) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf("Today") }
    
    // Tag builder state
    var tagInput by remember { mutableStateOf("") }
    var tagsList by remember { mutableStateOf(listOf<String>()) }
    
    // Folder picker state
    var selectedFolder by remember { mutableStateOf("Inbox") }
    var tempNewFolder by remember { mutableStateOf("") }
    var showFolderInput by remember { mutableStateOf(false) }
    
    // Checklist/Subtasks state
    var subtaskInput by remember { mutableStateOf("") }
    var subtasksList by remember { mutableStateOf(listOf<SubTask>()) }
    
    var isFlagged by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("create_task_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Advanced Task Creator",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close overlay")
                    }
                }
                
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Actionable Details / Notes") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_details"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
                
                // Flag / Star option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isFlagged = !isFlagged }
                        .background(
                            if (isFlagged) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFlagged) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = "Flag icon",
                            tint = if (isFlagged) Color(0xFFFFD54F) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Flag as Important",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                    Switch(
                        checked = isFlagged,
                        onCheckedChange = { isFlagged = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Folder/Project selection
                Text(
                    text = "Assign to Folder / Project List",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    items(folders) { folderName ->
                        val isSel = selectedFolder == folderName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable {
                                    selectedFolder = folderName
                                    showFolderInput = false
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📁 $folderName",
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (showFolderInput) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { showFolderInput = !showFolderInput }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ Custom Project",
                                color = if (showFolderInput) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                if (showFolderInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tempNewFolder,
                            onValueChange = { tempNewFolder = it },
                            label = { Text("New Project Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(
                            onClick = {
                                if (tempNewFolder.isNotBlank()) {
                                    val name = tempNewFolder.trim()
                                    viewModel.addFolder(name)
                                    selectedFolder = name
                                    tempNewFolder = ""
                                    showFolderInput = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Check, "Confirm Folder", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Category Selection
                Text(
                    text = "Category (Icon Tag)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TaskCategory.values().forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(category.icon, fontSize = 20.sp)
                                Text(category.label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Tags Builder
                Text(
                    text = "Tags / Labels",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        label = { Text("Add Tag (e.g. critical, ui)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    IconButton(
                        onClick = {
                            if (tagInput.isNotBlank()) {
                                val cleanTag = tagInput.trim().lowercase()
                                if (!tagsList.contains(cleanTag)) {
                                    tagsList = tagsList + cleanTag
                                }
                                tagInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, "Add Tag", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                if (tagsList.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tagsList.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable { tagsList = tagsList - tag }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove tag",
                                        modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Subtask Checklist Builder
                Text(
                    text = "Subtask Checklist",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = subtaskInput,
                        onValueChange = { subtaskInput = it },
                        label = { Text("Add checkpoint / checklist step...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    IconButton(
                        onClick = {
                            if (subtaskInput.isNotBlank()) {
                                subtasksList = subtasksList + SubTask(title = subtaskInput.trim())
                                subtaskInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddCircle, "Add Subtask", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                if (subtasksList.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        subtasksList.forEach { subtask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(subtask.title, fontSize = 13.sp)
                                }
                                IconButton(
                                    onClick = { subtasksList = subtasksList.filterNot { it.id == subtask.id } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete subtask",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Priority Level
                Text(
                    text = "Priority Level",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.values().forEach { priority ->
                        val isSelected = selectedPriority == priority
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(priority.color).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) Color(priority.color) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedPriority = priority }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = priority.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(priority.color) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Due Date input (Fast Simulation)
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date / Label") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("e.g. Today, Tomorrow, May 30") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons
                Button(
                    onClick = {
                        if (title.trim().isNotEmpty()) {
                            onAdd(
                                TodoTask(
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = selectedCategory,
                                    priority = selectedPriority,
                                    dueDate = dueDate.trim(),
                                    tags = tagsList,
                                    folder = selectedFolder,
                                    subtasks = subtasksList,
                                    isFlagged = isFlagged
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("add_task_confirm"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = title.trim().isNotEmpty()
                ) {
                    Text("Pin & Create Task", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}


// --- NOTES SCREEN LAYOUT ---

@Composable
fun NotesScreen(
    viewModel: TodoViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsState()
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<ProjectNote?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf("All") }

    // Collect all unique tags dynamically
    val allUniqueTags = remember(notes) {
        val tags = notes.flatMap { it.tags }.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        listOf("All") + tags
    }

    // Filter notes based on query and tag
    val filteredNotes = remember(notes, searchQuery, selectedTagFilter) {
        notes.filter { note ->
            val matchesQuery = note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true)
            val matchesTag = selectedTagFilter == "All" || note.tags.any { it.equals(selectedTagFilter, ignoreCase = true) }
            matchesQuery && matchesTag
        }
    }

    // Group into Pinned and Others
    val pinnedNotes = remember(filteredNotes) { filteredNotes.filter { it.isPinned } }
    val otherNotes = remember(filteredNotes) { filteredNotes.filter { !it.isPinned } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Notes Header
            Text(
                text = "Personal Scratchpad 📝",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Style notes, search quickly, pin favorites, and organize with tags.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar & Clear button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by title, details...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_search_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tag Filters Horizontal Row
            if (allUniqueTags.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("notes_tag_filters"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allUniqueTags) { tag ->
                        val isSelected = tag == selectedTagFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .clickable { selectedTagFilter = tag }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Empty notes indicator",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedTagFilter != "All") "No matching notes found" else "No notes saved yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedTagFilter != "All") "Try adjusting your filters or search keywords!" else "Click the edit button below to record sticky note ideas, travel drafts, or checklists.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("notes_grid"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    // Pinned notes section
                    if (pinnedNotes.isNotEmpty()) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pinned",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "PINNED NOTES",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        items(pinnedNotes, key = { "pinned_" + it.id }) { note ->
                            NoteGridCard(
                                note = note,
                                onDelete = { viewModel.deleteNote(note.id) },
                                onPinToggle = { viewModel.toggleNotePinned(note.id) },
                                onCardClick = { editingNote = note }
                            )
                        }
                    }

                    // Other notes section
                    if (otherNotes.isNotEmpty()) {
                        if (pinnedNotes.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                Text(
                                    text = "OTHERS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                        }
                        items(otherNotes, key = { "other_" + it.id }) { note ->
                            NoteGridCard(
                                note = note,
                                onDelete = { viewModel.deleteNote(note.id) },
                                onPinToggle = { viewModel.toggleNotePinned(note.id) },
                                onCardClick = { editingNote = note }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button for Notes
        FloatingActionButton(
            onClick = { showAddNoteDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .testTag("add_note_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Edit, "Add note icon", modifier = Modifier.size(24.dp))
        }
    }

    if (showAddNoteDialog) {
        CreateNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onAdd = { title, content, colorIndex, tags, isPinned ->
                val dateStr = "May 30, 2026"
                viewModel.addNote(
                    ProjectNote(
                        title = title,
                        content = content,
                        colorIndex = colorIndex,
                        dateString = dateStr,
                        isPinned = isPinned,
                        tags = tags
                    )
                )
                showAddNoteDialog = false
            }
        )
    }

    if (editingNote != null) {
        EditNoteDialog(
            note = editingNote!!,
            onDismiss = { editingNote = null },
            onUpdate = { updated ->
                viewModel.updateNote(updated)
                editingNote = null
            },
            onDelete = {
                viewModel.deleteNote(editingNote!!.id)
                editingNote = null
            }
        )
    }
}

@Composable
fun NoteGridCard(
    note: ProjectNote,
    onDelete: () -> Unit,
    onPinToggle: () -> Unit,
    onCardClick: () -> Unit
) {
    val bgColor = NoteColors[note.colorIndex % NoteColors.size]
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .clickable { onCardClick() }
            .testTag("note_card_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (note.isPinned) Color.Black.copy(alpha = 0.25f) else Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onPinToggle,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Toggle Pin",
                                tint = if (note.isPinned) Color.Black else Color.Black.copy(alpha = 0.25f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete note",
                            tint = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .clickable { onDelete() }
                                .testTag("delete_note_${note.id}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Black.copy(alpha = 0.8f)
                    ),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(modifier = Modifier.padding(top = 8.dp)) {
                // Display tags
                if (note.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        note.tags.take(2).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.08f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                        if (note.tags.size > 2) {
                            Text(
                                text = "+${note.tags.size - 2}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                Text(
                    text = note.dateString,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = Color.Black.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, List<String>, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }
    var isPinned by remember { mutableStateOf(false) }

    var tagList by remember { mutableStateOf(listOf<String>()) }
    var newTagText by remember { mutableStateOf("") }

    val shortcutSuggestions = listOf("Work", "Personal", "Health", "Study", "Idea")

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("create_note_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jot Down Sticky Note",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, "Close note creation overlay")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth().testTag("add_note_title"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Details & Brain dump...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("add_note_content"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 8
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPinned = !isPinned },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it }
                    )
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin Note icon",
                        modifier = Modifier.size(16.dp),
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Pin to top of board",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Add Tags / Topics:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTagText,
                            onValueChange = { newTagText = it },
                            placeholder = { Text("New tag") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                val clean = newTagText.trim()
                                if (clean.isNotEmpty() && !tagList.contains(clean)) {
                                    tagList = tagList + clean
                                    newTagText = ""
                                }
                            })
                        )

                        Button(
                            onClick = {
                                val clean = newTagText.trim()
                                if (clean.isNotEmpty() && !tagList.contains(clean)) {
                                    tagList = tagList + clean
                                    newTagText = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Add")
                        }
                    }

                    if (tagList.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(tagList) { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { tagList = tagList - tag }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove tag",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Suggestions:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        items(shortcutSuggestions) { suggestion ->
                            val alreadyHas = tagList.contains(suggestion)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (alreadyHas) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        tagList = if (alreadyHas) tagList - suggestion else tagList + suggestion
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (alreadyHas) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Select Sticky Note Accent Color:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NoteColors.forEachIndexed { index, color ->
                        val isSelected = selectedColorIndex == index
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (content.trim().isNotEmpty()) {
                            onAdd(title.ifEmpty { "Untitled" }, content, selectedColorIndex, tagList, isPinned)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("add_note_confirm"),
                    shape = RoundedCornerShape(14.dp),
                    enabled = content.trim().isNotEmpty()
                ) {
                    Text("Pin Sticky Note", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteDialog(
    note: ProjectNote,
    onDismiss: () -> Unit,
    onUpdate: (ProjectNote) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var selectedColorIndex by remember { mutableStateOf(note.colorIndex) }
    var isPinned by remember { mutableStateOf(note.isPinned) }

    var tagList by remember { mutableStateOf(note.tags) }
    var newTagText by remember { mutableStateOf("") }

    val shortcutSuggestions = listOf("Work", "Personal", "Health", "Study", "Idea")

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("edit_note_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customize sticky note",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, "Close note editing overlay")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_note_title"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Details & Brain dump...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("edit_note_content"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 8
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPinned = !isPinned },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it }
                    )
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin Note icon",
                        modifier = Modifier.size(16.dp),
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Pin to top of board",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tags / Topics:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTagText,
                            onValueChange = { newTagText = it },
                            placeholder = { Text("New tag") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                val clean = newTagText.trim()
                                if (clean.isNotEmpty() && !tagList.contains(clean)) {
                                    tagList = tagList + clean
                                    newTagText = ""
                                }
                            })
                        )

                        Button(
                            onClick = {
                                val clean = newTagText.trim()
                                if (clean.isNotEmpty() && !tagList.contains(clean)) {
                                    tagList = tagList + clean
                                    newTagText = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Add")
                        }
                    }

                    if (tagList.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(tagList) { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { tagList = tagList - tag }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove tag",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Suggestions:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        items(shortcutSuggestions) { suggestion ->
                            val alreadyHas = tagList.contains(suggestion)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (alreadyHas) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        tagList = if (alreadyHas) tagList - suggestion else tagList + suggestion
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (alreadyHas) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Select Sticky Note Accent Color:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NoteColors.forEachIndexed { index, color ->
                        val isSelected = selectedColorIndex == index
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }

                    Button(
                        onClick = {
                            if (content.trim().isNotEmpty()) {
                                onUpdate(
                                    note.copy(
                                        title = title.ifEmpty { "Untitled" },
                                        content = content,
                                        colorIndex = selectedColorIndex,
                                        isPinned = isPinned,
                                        tags = tagList
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1.5f).height(52.dp).testTag("edit_note_confirm"),
                        shape = RoundedCornerShape(14.dp),
                        enabled = content.trim().isNotEmpty()
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// --- SETTINGS SCREEN LAYOUT ---

@Composable
fun SettingsScreen(
    viewModel: TodoViewModel,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isNotificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    var editingName by remember { mutableStateOf(userName) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Preferences & Style ⚙️",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Personalize your mobile task environment and theme designs fully at runtime.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )
        }

        // Profile Customization Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_profile_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "User Identity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { 
                            editingName = it
                            viewModel.updateUserName(it)
                        },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Edit, "Edit name symbol", modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }

        // Multi-Theme Picker Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_theme_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme style icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Aesthetic App Color Styling",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Choose an eye-safe theme matching your device ambient condition:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ThemeOption.values().forEach { theme ->
                            val isSelected = selectedTheme == theme
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setTheme(theme) }
                                    .padding(vertical = 14.dp, horizontal = 16.dp)
                                    .testTag("theme_btn_${theme.name}"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (theme) {
                                                        ThemeOption.COSMIC_DEEP -> Color(0xFF00FFCC)
                                                        ThemeOption.SAGE_MINT -> Color(0xFF1E523A)
                                                        ThemeOption.LILAC_BLOSSOM -> Color(0xFF7C3AED)
                                                        ThemeOption.CORAL_SUNSET -> Color(0xFFFF5722)
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = theme.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active selected indicator",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notification Simulations Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Push Notification Alerts",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Daily task reminders and summaries",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() },
                        modifier = Modifier.testTag("notification_switch")
                    )
                }
            }
        }

        // App Information Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Build Information",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Version: v1.0.0 (Compose Interactive Native Showcase)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Environment: Optimized Streaming Mobile Sandbox",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Designed as requested to build the state-of-the-art layout with three dedicated tab sections first. Database and persistent layers are isolated, leaving components completely clean.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: TodoViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val now = remember { Calendar.getInstance().apply { set(2026, Calendar.MAY, 30) } } // Ambient metadata baseline anchor

    var currentMonth by remember { mutableStateOf(now.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(now.get(Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf(now.get(Calendar.DAY_OF_MONTH)) }

    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Dynamic month calculations
    val monthCalendar = remember(currentMonth, currentYear) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, ..., 7 = Saturday

    val selectedDateLabel = remember(selectedDay, currentMonth, currentYear) {
        String.format(Locale.US, "%s %d, %d", monthNames[currentMonth], selectedDay, currentYear)
    }

    // Get tasks for the current selected date
    val tasksForSelectedDate = tasks.filter { task ->
        isTaskDueOnDate(task, selectedDay, currentMonth, currentYear)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Section Header
            Text(
                text = "Interactive Planner 📅",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Map deadlines to dates, filter scheduled items, and trace your streaks dynamically.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Card Setup
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_container_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Month navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentMonth == Calendar.JANUARY) {
                                    currentMonth = Calendar.DECEMBER
                                    currentYear--
                                } else {
                                    currentMonth--
                                }
                                selectedDay = 1
                            },
                            modifier = Modifier.testTag("prev_month_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Month",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "${monthNames[currentMonth]} $currentYear",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("calendar_month_year_header")
                        )

                        IconButton(
                            onClick = {
                                if (currentMonth == Calendar.DECEMBER) {
                                    currentMonth = Calendar.JANUARY
                                    currentYear++
                                } else {
                                    currentMonth++
                                }
                                selectedDay = 1
                            },
                            modifier = Modifier.testTag("next_month_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Month",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Weekdays headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDayNames.forEach { dayName ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic blank pads & grid rendering
                    val daysGrid = remember(daysInMonth, firstDayOfWeek) {
                        val list = mutableListOf<Int?>()
                        val leadBlankCount = firstDayOfWeek - 1
                        for (i in 0 until leadBlankCount) {
                            list.add(null)
                        }
                        for (i in 1..daysInMonth) {
                            list.add(i)
                        }
                        while (list.size % 7 != 0) {
                            list.add(null)
                        }
                        list
                    }

                    val rowsCount = daysGrid.size / 7
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (r in 0 until rowsCount) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (c in 0..6) {
                                    val dayValue = daysGrid[r * 7 + c]
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dayValue != null) {
                                            val isSelected = selectedDay == dayValue
                                            val isToday = dayValue == 30 && currentMonth == Calendar.MAY && currentYear == 2026

                                            val dayTasks = tasks.filter { isTaskDueOnDate(it, dayValue, currentMonth, currentYear) }
                                            val hasTasks = dayTasks.isNotEmpty()

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isSelected -> MaterialTheme.colorScheme.primary
                                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .border(
                                                        width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                                        color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { selectedDay = dayValue }
                                                    .testTag("calendar_day_$dayValue"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Text(
                                                        text = dayValue.toString(),
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                        color = when {
                                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                                            isToday -> MaterialTheme.colorScheme.primary
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )

                                                    if (hasTasks) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(top = 1.dp)
                                                        ) {
                                                            val highCount = dayTasks.count { it.priority == TaskPriority.HIGH }
                                                            val medCount = dayTasks.count { it.priority == TaskPriority.MEDIUM }
                                                            val lowCount = dayTasks.count { it.priority == TaskPriority.LOW }

                                                            if (highCount > 0) {
                                                                Box(modifier = Modifier.size(4.dp).background(Color(TaskPriority.HIGH.color), CircleShape))
                                                            } else if (medCount > 0) {
                                                                Box(modifier = Modifier.size(4.dp).background(Color(TaskPriority.MEDIUM.color), CircleShape))
                                                            } else if (lowCount > 0) {
                                                                Box(modifier = Modifier.size(4.dp).background(Color(TaskPriority.LOW.color), CircleShape))
                                                            } else {
                                                                Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Agenda List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Agenda for $selectedDateLabel",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("agenda_date_header")
                )

                Text(
                    text = "${tasksForSelectedDate.size} Tasks",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub task listing or status banner
            if (tasksForSelectedDate.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "Event Free Indicator",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No milestones scheduled",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Tap 'Schedule Milestone' below to add a plan specifically for this date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(tasksForSelectedDate, key = { it.id }) { task ->
                        TaskListItemCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskComplete(task.id) },
                            onDelete = { viewModel.deleteTask(task.id) },
                            onToggleFlagged = { viewModel.toggleTaskFlagged(task.id) },
                            onToggleSubtaskComplete = { subId -> viewModel.toggleSubtaskComplete(task.id, subId) }
                        )
                    }
                }
            }
        }

        // Schedule Action Fab button
        ExtendedFloatingActionButton(
            onClick = { showAddTaskDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .testTag("schedule_task_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, "Add date task icon")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Milestone", fontWeight = FontWeight.Bold)
        }
    }

    if (showAddTaskDialog) {
        CreateTaskDialogPreloaded(
            preloadedDate = selectedDateLabel,
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, desc, category, priority, due ->
                viewModel.addTask(
                    TodoTask(
                        title = title,
                        description = desc,
                        category = category,
                        priority = priority,
                        dueDate = due
                    )
                )
                showAddTaskDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialogPreloaded(
    preloadedDate: String,
    onDismiss: () -> Unit,
    onAdd: (String, String, TaskCategory, TaskPriority, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.PERSONAL) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf(preloadedDate) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_task_preloaded_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Schedule Milestone",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close overlay")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("add_pre_task_title"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Details") },
                    modifier = Modifier.fillMaxWidth().testTag("add_pre_task_details"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Text(
                    text = "Category",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TaskCategory.values().forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(category.icon, fontSize = 20.sp)
                                Text(category.label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text(
                    text = "Priority Level",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.values().forEach { priority ->
                        val isSelected = selectedPriority == priority
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(priority.color).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) Color(priority.color) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedPriority = priority }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = priority.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(priority.color) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Preloaded Due Date") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (title.trim().isNotEmpty()) {
                            onAdd(title, description, selectedCategory, selectedPriority, dueDate)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("add_pre_task_confirm"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = title.trim().isNotEmpty()
                ) {
                    Text("Pin to Calendar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

val monthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

val weekDayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

fun isTaskDueOnDate(task: TodoTask, day: Int, monthIndex: Int, year: Int): Boolean {
    val taskDueLower = task.dueDate.trim().lowercase()
    if (taskDueLower.isEmpty()) return false

    // Ambient metadata baseline anchor is May 30, 2026 (2026-05-30)
    if (taskDueLower == "today") {
        return day == 30 && monthIndex == Calendar.MAY && year == 2026
    }
    if (taskDueLower == "tomorrow") {
        return day == 31 && monthIndex == Calendar.MAY && year == 2026
    }
    if (taskDueLower == "in 2 days") {
        return day == 1 && monthIndex == Calendar.JUNE && year == 2026
    }

    val monthName = monthNames[monthIndex].lowercase()
    val monthAbbrev = monthName.substring(0, minOf(3, monthName.length))

    if (taskDueLower.contains(monthName) && taskDueLower.contains(day.toString())) {
        return true
    }
    if (taskDueLower.contains(monthAbbrev) && taskDueLower.contains(day.toString())) {
        return true
    }

    val isoPattern1 = String.format(Locale.US, "%04d-%02d-%02d", year, monthIndex + 1, day)
    val isoPattern2 = String.format(Locale.US, "%04d-%d-%d", year, monthIndex + 1, day)
    if (taskDueLower == isoPattern1 || taskDueLower == isoPattern2) {
        return true
    }

    return false
}

@Composable
fun EventListItemCard(
    event: TodoEvent,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(260.dp)
            .padding(vertical = 4.dp)
            .testTag("event_item_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (event.isCompleted) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (event.isCompleted) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time & Date badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${event.time} • ${event.date}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Delete Event
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Event",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title and check status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Checkbox
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (event.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (event.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onToggleComplete() }
                        .testTag("event_checkbox_${event.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (event.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (event.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (event.isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (event.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 14.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (event.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (event.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    event.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    onDismiss: () -> Unit,
    onAdd: (TodoEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("May 30, 2026") }
    var timeString by remember { mutableStateOf("12:00 PM") }
    var locationString by remember { mutableStateOf("") }
    
    // Tags state
    var tagInput by remember { mutableStateOf("") }
    var tagsList by remember { mutableStateOf(listOf<String>()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("create_event_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Schedule New Event 📅",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close overlay")
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("add_event_title"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details & Description") },
                    modifier = Modifier.fillMaxWidth().testTag("add_event_details"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                // Date and Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f).testTag("add_event_date"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = timeString,
                        onValueChange = { timeString = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f).testTag("add_event_time"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Location Input
                OutlinedTextField(
                    value = locationString,
                    onValueChange = { locationString = it },
                    label = { Text("Location / Link") },
                    modifier = Modifier.fillMaxWidth().testTag("add_event_location"),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.LocationOn, "Location") },
                    singleLine = true
                )

                // Tags Builder
                Text(
                    text = "Event Tags",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        label = { Text("Add Tag") },
                        modifier = Modifier.weight(1f).testTag("add_event_tag_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    IconButton(
                        onClick = {
                            if (tagInput.isNotBlank()) {
                                val cleanTag = tagInput.trim().lowercase()
                                if (!tagsList.contains(cleanTag)) {
                                    tagsList = tagsList + cleanTag
                                }
                                tagInput = ""
                            }
                        },
                        modifier = Modifier.testTag("add_event_tag_btn")
                    ) {
                        Icon(Icons.Default.Add, "Add Event Tag", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (tagsList.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tagsList.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable { tagsList = tagsList - tag }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove tag",
                                        modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (title.trim().isNotEmpty()) {
                            onAdd(
                                TodoEvent(
                                    title = title.trim(),
                                    description = description.trim(),
                                    date = dateString.trim(),
                                    time = timeString.trim(),
                                    location = locationString.trim(),
                                    tags = tagsList
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("confirm_create_event"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = title.trim().isNotEmpty()
                ) {
                    Text("Pin & Schedule Event", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun FocusTimerScreen(viewModel: TodoViewModel) {
    val presets by viewModel.customPresets.collectAsState()
    val activeDuration by viewModel.activeTimerDurationSeconds.collectAsState()
    val activeRemaining by viewModel.activeTimerRemainingSeconds.collectAsState()
    val isRunning by viewModel.activeTimerRunning.collectAsState()
    val activeLabel by viewModel.activeTimerLabel.collectAsState()
    val activeTag by viewModel.activeTimerTag.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    var showCreatePresetDialog by remember { mutableStateOf(false) }
    var inAppNotificationTitle by remember { mutableStateOf<String?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    // Listen to finished timer events
    LaunchedEffect(Unit) {
        viewModel.timerFinishedChannel.collect { finishedLabel ->
            if (notificationsEnabled) {
                // Fire system notification!
                NotificationHelper.showNotification(
                    context,
                    "Focus Timer Completed! 🎯",
                    "Great job! Your '$finishedLabel' session has finished successfully."
                )
            }
            // Trigger in-app alert
            inAppNotificationTitle = finishedLabel
        }
    }

    if (inAppNotificationTitle != null) {
        AlertDialog(
            onDismissRequest = { inAppNotificationTitle = null },
            confirmButton = {
                Button(onClick = { inAppNotificationTitle = null }) {
                    Text("Awesome!")
                }
            },
            title = {
                Text(
                    text = "Timer Finished! 🎯",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Congratulations! Your focus session '$inAppNotificationTitle' has completed successfully. Take a well-deserved break!",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showCreatePresetDialog) {
        CreatePresetDialog(
            onDismiss = { showCreatePresetDialog = false },
            onAdd = { label, minutes, tag ->
                viewModel.addCustomPreset(CustomTimerPreset(label = label, durationMinutes = minutes, tag = tag))
                showCreatePresetDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Creative Header Unit
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = "Focus Lounge ⚡",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Block notifications, tune in, and accomplish your milestones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        // Core Countdown Ring Visual Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Label active session
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ACTIVE: $activeLabel (#$activeTag)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Interactive Progress Ring Frame
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    val progressFraction = if (activeDuration > 0) activeRemaining.toFloat() / activeDuration else 1f
                    val colorScheme = MaterialTheme.colorScheme
                    
                    // Outer glow canvas drawing progress
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        // grey trace background
                        drawArc(
                            color = colorScheme.outlineVariant.copy(alpha = 0.3f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        // colorful countdown radial
                        drawArc(
                            color = colorScheme.primary,
                            startAngle = -90f,
                            sweepAngle = progressFraction * 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }

                    // Numeric Inner timer
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val minsText = String.format(java.util.Locale.US, "%02d", activeRemaining / 60)
                        val secsText = String.format(java.util.Locale.US, "%02d", activeRemaining % 60)
                        Text(
                            text = "$minsText:$secsText",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isRunning) "KEEP FOCUSING!" else "TIMER READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Core control triggers row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    OutlinedButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Play / Pause Button
                    Button(
                        onClick = {
                            if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            contentColor = if (isRunning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause timer" else "Start timer",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Standard notification preferences toggle icon
                    OutlinedButton(
                        onClick = { viewModel.toggleNotifications() },
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (notificationsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Toggle system notifications alert finish",
                            tint = if (notificationsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Custom Preset Management Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sessions & Presets ⏳",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(
                onClick = { showCreatePresetDialog = true },
                modifier = Modifier.testTag("add_custom_preset_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Custom Session", fontWeight = FontWeight.Bold)
            }
        }

        // Custom Presets Grid/List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                val isActive = activeLabel == preset.label
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.selectPreset(preset) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    preset.label.firstOrNull()?.toString() ?: "⏱️",
                                    fontSize = 16.sp
                                )
                            }

                            Column {
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${preset.durationMinutes} min session",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "#${preset.tag}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "READY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            // Delete Option only if custom added
                            val defaultListLabels = listOf("Study Session 📚", "Short Breathe 💨", "Deep Programming 💻", "Strategic Review 🎯")
                            if (!defaultListLabels.contains(preset.label)) {
                                IconButton(
                                    onClick = { viewModel.deleteCustomPreset(preset.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete preset",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePresetDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var durationMinutesString by remember { mutableStateOf("25") }
    var tag by remember { mutableStateOf("Focus") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("create_preset_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Focus Timer ⏱️",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close overlay")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Timer Label *") },
                    modifier = Modifier.fillMaxWidth().testTag("add_preset_title"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = durationMinutesString,
                    onValueChange = { durationMinutesString = it },
                    label = { Text("Duration (Minutes) *") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_preset_duration"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Tag Label (e.g., Coding, Writing)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_preset_tag"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val durationParsed = durationMinutesString.toIntOrNull() ?: 0
                val canSubmit = title.trim().isNotEmpty() && durationParsed > 0

                Button(
                    onClick = {
                        if (canSubmit) {
                            onAdd(title.trim(), durationParsed, tag.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("confirm_create_preset"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = canSubmit
                ) {
                    Text("Pin & Build Custom Timer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
