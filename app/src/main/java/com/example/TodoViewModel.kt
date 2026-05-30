package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class TaskPriority(val label: String, val color: Long) {
    HIGH("High", 0xFFEF5350),
    MEDIUM("Medium", 0xFFFFB74D),
    LOW("Low", 0xFF66BB6A)
}

enum class TaskCategory(val label: String, val icon: String) {
    WORK("Work", "💼"),
    PERSONAL("Personal", "🏠"),
    HEALTH("Health", "❤️"),
    STUDY("Study", "📚"),
    OTHER("Other", "✨")
}

data class SubTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

data class TodoTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.PERSONAL,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val tags: List<String> = emptyList(),
    val folder: String = "Inbox",
    val subtasks: List<SubTask> = emptyList(),
    val isFlagged: Boolean = false
)

data class ProjectNote(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val colorIndex: Int = 0,
    val dateString: String,
    val isPinned: Boolean = false,
    val tags: List<String> = emptyList()
)

data class TodoEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val tags: List<String> = emptyList(),
    val isCompleted: Boolean = false
)

data class CustomTimerPreset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String,
    val durationMinutes: Int,
    val tag: String = "Focus"
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ThemeOption(val label: String) {
    COSMIC_DEEP("Deep Cosmic"),
    SAGE_MINT("Sage Mint"),
    LILAC_BLOSSOM("Lilac Blossom"),
    CORAL_SUNSET("Coral Sunset")
}

class TodoViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<TodoTask>>(
        listOf(
            TodoTask(
                title = "Design mobile-first wireframes",
                description = "Draft sleek Figma boards for the dashboard tab and notes container",
                category = TaskCategory.WORK,
                priority = TaskPriority.HIGH,
                isCompleted = true,
                dueDate = "Today",
                folder = "Work",
                tags = listOf("Figma", "Design", "UI"),
                subtasks = listOf(
                    SubTask(title = "Sketch basic layouts", isCompleted = true),
                    SubTask(title = "Design micro-animations", isCompleted = true),
                    SubTask(title = "Get client feedback", isCompleted = false)
                ),
                isFlagged = true
            ),
            TodoTask(
                title = "Clean the workspace",
                description = "Organize desk and wipe screens for a productive setup",
                category = TaskCategory.PERSONAL,
                priority = TaskPriority.LOW,
                isCompleted = false,
                dueDate = "Tomorrow",
                folder = "Personal",
                tags = listOf("Home", "Organizer"),
                subtasks = listOf(
                    SubTask(title = "Dust drawers"),
                    SubTask(title = "Clean keyboard and monitors"),
                    SubTask(title = "Sort office mail")
                )
            ),
            TodoTask(
                title = "Morning run (30 mins)",
                description = "Keep up with the active wellness streak",
                category = TaskCategory.HEALTH,
                priority = TaskPriority.MEDIUM,
                isCompleted = false,
                dueDate = "Today",
                folder = "Health",
                tags = listOf("Cardio", "Habits")
            ),
            TodoTask(
                title = "Read Kotlin design patterns",
                description = "Read chapter 4 of Clean Architecture in Jetpack Compose",
                category = TaskCategory.STUDY,
                priority = TaskPriority.MEDIUM,
                isCompleted = false,
                dueDate = "In 2 days",
                folder = "Study",
                tags = listOf("Kotlin", "Architecture"),
                subtasks = listOf(
                    SubTask(title = "Read Chapter 4 core concepts", isCompleted = true),
                    SubTask(title = "Build coding sandbox project", isCompleted = false)
                )
            ),
            TodoTask(
                title = "Weekly grocery list",
                description = "Buy milk, organic avocados, whole grain bread, and dark chocolate",
                category = TaskCategory.PERSONAL,
                priority = TaskPriority.LOW,
                isCompleted = true,
                dueDate = "Completed",
                folder = "Shopping",
                tags = listOf("Grocery", "Eco-Friendly")
            )
        )
    )
    val tasks: StateFlow<List<TodoTask>> = _tasks.asStateFlow()

    private val _folders = MutableStateFlow<List<String>>(
        listOf("Inbox", "Work", "Personal", "Health", "Study", "Shopping", "Ideas")
    )
    val folders: StateFlow<List<String>> = _folders.asStateFlow()

    private val _notes = MutableStateFlow<List<ProjectNote>>(
        listOf(
            ProjectNote(title = "App Features Idea", content = "1. AI integration in Task Creator\n2. Audio notes upload\n3. Dark/Light toggles based on light sensors.", colorIndex = 0, dateString = "May 30, 2026", isPinned = true, tags = listOf("Idea", "Work")),
            ProjectNote(title = "Shopping Checklist", content = "- USB-C Adapter\n- Standing desk anti-fatigue mat\n- Ergonomic mouse", colorIndex = 1, dateString = "May 28, 2026", isPinned = false, tags = listOf("Shopping", "Personal")),
            ProjectNote(title = "Motivational Quotes", content = "'Simplicity is the soul of modern efficiency.' Keep interfaces minimal, interactive, and functional.", colorIndex = 2, dateString = "May 25, 2026", isPinned = false, tags = listOf("Idea", "Design"))
        )
    )
    val notes: StateFlow<List<ProjectNote>> = _notes.asStateFlow()

    private val _selectedTheme = MutableStateFlow(ThemeOption.COSMIC_DEEP)
    val selectedTheme: StateFlow<ThemeOption> = _selectedTheme.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _userName = MutableStateFlow("Developer")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _events = MutableStateFlow<List<TodoEvent>>(
        listOf(
            TodoEvent(
                title = "Figma Wireframe Review 🎨",
                description = "Present sleek interactive dashboard and notes mockups for client feedback",
                date = "May 30, 2026",
                time = "10:30 AM",
                location = "Zoom (ID: 882-991-042)",
                tags = listOf("Design", "Figma", "Work")
            ),
            TodoEvent(
                title = "Cardio Health Run 🏃‍♂️",
                description = "Scenic 5K park run keeping up with high energy active wellness goals",
                date = "May 30, 2026",
                time = "05:00 PM",
                location = "Central Park Track",
                tags = listOf("Health", "Cardio")
            ),
            TodoEvent(
                title = "Study Group: Kotlin 📚",
                description = "Interactive session detailing state optimization and coroutine design patterns",
                date = "June 02, 2026",
                time = "02:00 PM",
                location = "Online Meetup Lounge",
                tags = listOf("Kotlin", "Study")
            )
        )
    )
    val events: StateFlow<List<TodoEvent>> = _events.asStateFlow()

    // Folder Actions
    fun addFolder(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty() && !_folders.value.any { it.equals(trimmed, ignoreCase = true) }) {
            _folders.update { it + trimmed }
        }
    }

    // Task Actions
    fun addTask(task: TodoTask) {
        _tasks.update { it + task }
    }

    fun toggleTaskComplete(taskId: String) {
        _tasks.update { list ->
            list.map { if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it }
        }
    }

    fun toggleTaskFlagged(taskId: String) {
        _tasks.update { list ->
            list.map { if (it.id == taskId) it.copy(isFlagged = !it.isFlagged) else it }
        }
    }

    fun toggleSubtaskComplete(taskId: String, subtaskId: String) {
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val updatedSubtasks = task.subtasks.map { sub ->
                        if (sub.id == subtaskId) sub.copy(isCompleted = !sub.isCompleted) else sub
                    }
                    task.copy(subtasks = updatedSubtasks)
                } else task
            }
        }
    }

    fun deleteTask(taskId: String) {
        _tasks.update { it.filterNot { task -> task.id == taskId } }
    }

    // Note Actions
    fun addNote(note: ProjectNote) {
        _notes.update { it + note }
    }

    fun deleteNote(noteId: String) {
        _notes.update { it.filterNot { note -> note.id == noteId } }
    }

    fun updateNote(updatedNote: ProjectNote) {
        _notes.update { oldList ->
            oldList.map { oldNote ->
                if (oldNote.id == updatedNote.id) updatedNote else oldNote
            }
        }
    }

    fun toggleNotePinned(noteId: String) {
        _notes.update { oldList ->
            oldList.map { oldNote ->
                if (oldNote.id == noteId) oldNote.copy(isPinned = !oldNote.isPinned) else oldNote
            }
        }
    }

    // Event Actions
    fun addEvent(event: TodoEvent) {
        _events.update { it + event }
    }

    fun toggleEventComplete(eventId: String) {
        _events.update { list ->
            list.map { if (it.id == eventId) it.copy(isCompleted = !it.isCompleted) else it }
        }
    }

    fun deleteEvent(eventId: String) {
        _events.update { it.filterNot { event -> event.id == eventId } }
    }

    // Settings Actions
    fun setTheme(theme: ThemeOption) {
        _selectedTheme.value = theme
    }

    fun toggleNotifications() {
        _notificationsEnabled.update { !it }
    }

    fun updateUserName(name: String) {
        _userName.value = name
    }

    // --- FOCUS TIMER STATES & ACTIONS ---
    private val _customPresets = MutableStateFlow<List<CustomTimerPreset>>(
        listOf(
            CustomTimerPreset(label = "Study Session 📚", durationMinutes = 25, tag = "Study"),
            CustomTimerPreset(label = "Short Breathe 💨", durationMinutes = 5, tag = "Breathe"),
            CustomTimerPreset(label = "Deep Programming 💻", durationMinutes = 45, tag = "Coding"),
            CustomTimerPreset(label = "Strategic Review 🎯", durationMinutes = 15, tag = "Planning")
        )
    )
    val customPresets: StateFlow<List<CustomTimerPreset>> = _customPresets.asStateFlow()

    private val _activeTimerDurationSeconds = MutableStateFlow(1500) // 25 min default
    val activeTimerDurationSeconds: StateFlow<Int> = _activeTimerDurationSeconds.asStateFlow()

    private val _activeTimerRemainingSeconds = MutableStateFlow(1500)
    val activeTimerRemainingSeconds: StateFlow<Int> = _activeTimerRemainingSeconds.asStateFlow()

    private val _activeTimerRunning = MutableStateFlow(false)
    val activeTimerRunning: StateFlow<Boolean> = _activeTimerRunning.asStateFlow()

    private val _activeTimerLabel = MutableStateFlow("Study Session 📚")
    val activeTimerLabel: StateFlow<String> = _activeTimerLabel.asStateFlow()

    private val _activeTimerTag = MutableStateFlow("Study")
    val activeTimerTag: StateFlow<String> = _activeTimerTag.asStateFlow()

    private val _timerFinishedChannel = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val timerFinishedChannel: SharedFlow<String> = _timerFinishedChannel.asSharedFlow()

    private var timerJob: Job? = null

    fun selectPreset(preset: CustomTimerPreset) {
        pauseTimer()
        _activeTimerLabel.value = preset.label
        _activeTimerTag.value = preset.tag
        _activeTimerDurationSeconds.value = preset.durationMinutes * 60
        _activeTimerRemainingSeconds.value = preset.durationMinutes * 60
    }

    fun setCustomDurationMinutes(minutes: Int, label: String = "Custom Focus", tag: String = "Focus") {
        pauseTimer()
        _activeTimerLabel.value = label
        _activeTimerTag.value = tag
        _activeTimerDurationSeconds.value = minutes * 60
        _activeTimerRemainingSeconds.value = minutes * 60
    }

    fun startTimer() {
        if (_activeTimerRunning.value) return
        _activeTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_activeTimerRemainingSeconds.value > 0) {
                delay(1000L)
                _activeTimerRemainingSeconds.update { it - 1 }
            }
            // Timer Finished!
            _activeTimerRunning.value = false
            _timerFinishedChannel.emit(_activeTimerLabel.value)
            // Reset to original duration automatically so it's ready again
            _activeTimerRemainingSeconds.value = _activeTimerDurationSeconds.value
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        _activeTimerRunning.value = false
    }

    fun resetTimer() {
        pauseTimer()
        _activeTimerRemainingSeconds.value = _activeTimerDurationSeconds.value
    }

    fun addCustomPreset(preset: CustomTimerPreset) {
        _customPresets.update { it + preset }
    }

    fun deleteCustomPreset(presetId: String) {
        _customPresets.update { it.filterNot { it.id == presetId } }
    }

    // --- AI ASSISTANT STATES & ACTIONS ---
    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "Hello! I am your AI Planner. Tell me what folders, tasks, events, or notes you want to create, and I'll generate and integrate them instantly! 🚀\n\nTry saying:\n*\"Create a shopping list folder and add apple, bananas, and bread. Then, schedule a gym event on Friday at 6 PM.\"*"
            )
        )
    )
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _aiIsLoading = MutableStateFlow(false)
    val aiIsLoading: StateFlow<Boolean> = _aiIsLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    fun clearAiChat() {
        _aiMessages.value = listOf(
            ChatMessage(
                isUser = false,
                text = "Chat history cleared. What can I help you build next? 🎯"
            )
        )
        _aiError.value = null
    }

    fun sendAiMessage(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isEmpty()) return

        // Append user message
        _aiMessages.update { it + ChatMessage(isUser = true, text = trimmed) }
        _aiIsLoading.value = true
        _aiError.value = null

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _aiIsLoading.value = false
                    val errorMsg = "Gemini API key is not configured. Please register your GEMINI_API_KEY in the AI Studio Secrets panel."
                    _aiMessages.update { it + ChatMessage(isUser = false, text = "⚠️ $errorMsg") }
                    _aiError.value = errorMsg
                    return@launch
                }

                // Construct full context with current active folders
                val foldersList = _folders.value.joinToString(", ")
                val fullSystemInstruction = """
                    You are an expert AI productivity planner assistant.
                    Your job is to reply with a structured JSON object containing any folders, tasks, events, or notes requested by the user, plus an appropriate chat response confirming what was created.
                    Today's date is Saturday, May 30, 2026. Use this reference when the user specifies relative dates (e.g. 'tomorrow', 'this Monday', etc.).
                    Existing folders/lists in the app are: [$foldersList].

                    Format your output EXACTLY as this JSON structure:
                    {
                      "chatResponse": "Brief friendly confirmation detailing what was added.",
                      "folders": ["Folder1", "Folder2"],
                      "notes": [
                        {"title": "Note Title", "content": "Note markdown/text content"}
                      ],
                      "tasks": [
                        {"title": "Task Title", "description": "optional task details", "category": "WORK", "priority": "HIGH", "folderName": "Inbox", "dueDate": "May 30, 2026", "subtasks": ["subtask1"]}
                      ],
                      "events": [
                        {"title": "Event Title", "description": "optional description", "date": "May 30, 2026", "time": "10:30 AM", "location": "Zoom", "tags": ["tag1"]}
                      ]
                    }

                    Strict Rules:
                    1. Category MUST be exactly one of: WORK, PERSONAL, HEALTH, STUDY, OTHER.
                    2. Priority MUST be exactly one of: HIGH, MEDIUM, LOW.
                    3. If folderName is supplied, place it there. Double check that you also append to "folders" if the folder is new and doesn't exist yet!
                    4. Always respond with only the raw JSON. No markdown blocks, no ```json prefixes.
                """.trimIndent()

                // Compile request
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = trimmed)))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.2f
                    ),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = fullSystemInstruction)))
                )

                val response = GeminiClient.service.generateContent(apiKey, request)
                val rawText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (rawText != null) {
                    val parsed = GeminiClient.parseAiResponse(rawText)
                    if (parsed != null) {
                        // 1. Add chat message
                        _aiMessages.update { it + ChatMessage(isUser = false, text = parsed.chatResponse) }

                        // 2. Add Folders
                        parsed.folders?.forEach { folderName ->
                            addFolder(folderName)
                        }

                        // 3. Add Notes
                        parsed.notes?.forEach { noteReq ->
                            addNote(
                                ProjectNote(
                                    title = noteReq.title,
                                    content = noteReq.content,
                                    colorIndex = (0..5).random(),
                                    dateString = "May 30, 2026"
                                )
                            )
                        }

                        // 4. Add Tasks
                        parsed.tasks?.forEach { taskReq ->
                            val catEnum = try {
                                TaskCategory.valueOf(taskReq.category.uppercase())
                            } catch (e: Exception) {
                                TaskCategory.OTHER
                            }
                            val prioEnum = try {
                                TaskPriority.valueOf(taskReq.priority.uppercase())
                            } catch (e: Exception) {
                                TaskPriority.MEDIUM
                            }
                            val subtaskObjs = taskReq.subtasks.map { SubTask(title = it) }
                            addTask(
                                TodoTask(
                                    title = taskReq.title,
                                    description = taskReq.description,
                                    category = catEnum,
                                    priority = prioEnum,
                                    folder = if (taskReq.folderName.isNotEmpty()) taskReq.folderName else "Inbox",
                                    dueDate = if (taskReq.dueDate.isNotEmpty()) taskReq.dueDate else "Today",
                                    tags = if (taskReq.category.isNotEmpty()) listOf(taskReq.category) else emptyList(),
                                    subtasks = subtaskObjs
                                )
                            )
                        }

                        // 5. Add Events
                        parsed.events?.forEach { eventReq ->
                            addEvent(
                                TodoEvent(
                                    title = eventReq.title,
                                    description = eventReq.description,
                                    date = if (eventReq.date.isNotEmpty()) eventReq.date else "May 30, 2026",
                                    time = if (eventReq.time.isNotEmpty()) eventReq.time else "12:00 PM",
                                    location = eventReq.location,
                                    tags = eventReq.tags
                                )
                            )
                        }
                    } else {
                        // Plan fallback if JSON format parsing failed
                        _aiMessages.update { it + ChatMessage(isUser = false, text = rawText) }
                    }
                } else {
                    _aiMessages.update { it + ChatMessage(isUser = false, text = "Received an empty response from the AI.") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _aiError.value = e.message
                _aiMessages.update {
                    it + ChatMessage(
                        isUser = false,
                        text = "❌ Error connecting to Gemini API:\n\n${e.localizedMessage ?: "Network/timeout exception"}.\n\nPlease ensure your internet connection is active and that a valid GEMINI_API_KEY is registered in the Secrets panel."
                    )
                }
            } finally {
                _aiIsLoading.value = false
            }
        }
    }
}
