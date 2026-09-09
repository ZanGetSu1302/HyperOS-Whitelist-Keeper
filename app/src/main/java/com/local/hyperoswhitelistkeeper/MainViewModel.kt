package com.local.hyperoswhitelistkeeper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.local.hyperoswhitelistkeeper.automation.DailyAlarmScheduler
import com.local.hyperoswhitelistkeeper.data.AppCatalog
import com.local.hyperoswhitelistkeeper.data.AppLanguage
import com.local.hyperoswhitelistkeeper.data.PreferencesRepository
import com.local.hyperoswhitelistkeeper.data.ThemeMode
import com.local.hyperoswhitelistkeeper.model.AppEntry
import com.local.hyperoswhitelistkeeper.ui.UiStrings
import com.local.hyperoswhitelistkeeper.ui.uiStrings
import com.local.hyperoswhitelistkeeper.whitelist.WhitelistRepository
import com.local.hyperoswhitelistkeeper.whitelist.WhitelistResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val entries: List<AppEntry> = emptyList(),
    val selectedPackages: Set<String> = AppCatalog.defaultSelection,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.VI,
    val isApplying: Boolean = false,
    val activationById: Map<String, Boolean> = emptyMap(),
    val isCheckingActivation: Boolean = true,
    val runOnBootEnabled: Boolean = false,
    val scheduledRunEnabled: Boolean = false,
)

sealed interface MainUiEvent {
    data class ShowSnackbar(val message: String) : MainUiEvent
    data object RequestWriteSettings : MainUiEvent
    data object RequestExactAlarmPermission : MainUiEvent
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesRepository = PreferencesRepository(application)
    private val whitelistRepository = WhitelistRepository(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MainUiEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<MainUiEvent> = _events.asSharedFlow()

    private val selectionSaves = Channel<Set<String>>(capacity = Channel.UNLIMITED)
    private var activationRefreshJob: Job? = null

    init {
        viewModelScope.launch {
            preferencesRepository.selectedPackages.collect { selected ->
                _uiState.update { it.copy(selectedPackages = selected) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.appLanguage.collect { language ->
                _uiState.update { it.copy(language = language) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.runOnBootEnabled.collect { enabled ->
                _uiState.update { it.copy(runOnBootEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.scheduledRunEnabled.collect { enabled ->
                _uiState.update { it.copy(scheduledRunEnabled = enabled) }
                if (enabled) {
                    DailyAlarmScheduler.scheduleAll(application)
                } else {
                    DailyAlarmScheduler.cancelAll(application)
                }
            }
        }
        viewModelScope.launch {
            val builtInEntries = AppCatalog.resolveEntries(application)
            val builtInIds = builtInEntries.mapTo(hashSetOf(), AppEntry::id)
            preferencesRepository.customApps.collect { customEntries ->
                val entries = builtInEntries + customEntries.filterNot { it.id in builtInIds }
                _uiState.update { it.copy(entries = entries) }
            }
        }
        viewModelScope.launch {
            for (selection in selectionSaves) {
                preferencesRepository.saveSelectedPackages(selection)
            }
        }
    }

    fun setSelected(id: String, selected: Boolean) {
        val updated = _uiState.value.selectedPackages.toMutableSet().apply {
            if (selected) add(id) else remove(id)
        }.toSet()
        _uiState.update { it.copy(selectedPackages = updated) }
        selectionSaves.trySend(updated)
    }

    fun addCustomApp(packageName: String, appName: String) {
        val normalizedPackage = packageName.trim()
        val normalizedName = appName.trim()
        val current = _uiState.value
        if (
            normalizedName.isBlank() ||
            !AppCatalog.isValidPackageName(normalizedPackage) ||
            normalizedPackage in AppCatalog.ids ||
            current.entries.any { it.id == normalizedPackage }
        ) {
            return
        }

        val entry = AppEntry(id = normalizedPackage, label = normalizedName)
        val updatedSelection = current.selectedPackages + normalizedPackage
        _uiState.update {
            it.copy(
                entries = it.entries + entry,
                selectedPackages = updatedSelection,
            )
        }
        selectionSaves.trySend(updatedSelection)
        viewModelScope.launch { preferencesRepository.saveCustomApp(entry) }
        refreshActivationStatuses()
    }

    fun cycleTheme() {
        val next = _uiState.value.themeMode.next()
        _uiState.update { it.copy(themeMode = next) }
        viewModelScope.launch { preferencesRepository.saveThemeMode(next) }
    }

    fun cycleLanguage() {
        val next = _uiState.value.language.next()
        _uiState.update { it.copy(language = next) }
        viewModelScope.launch { preferencesRepository.saveAppLanguage(next) }
    }

    fun setRunOnBootEnabled(enabled: Boolean) {
        _uiState.update { it.copy(runOnBootEnabled = enabled) }
        viewModelScope.launch { preferencesRepository.saveRunOnBootEnabled(enabled) }
    }

    fun setScheduledRunEnabled(enabled: Boolean) {
        _uiState.update { it.copy(scheduledRunEnabled = enabled) }
        viewModelScope.launch { preferencesRepository.saveScheduledRunEnabled(enabled) }

        if (!enabled) {
            DailyAlarmScheduler.cancelAll(getApplication())
            return
        }

        if (!DailyAlarmScheduler.scheduleAll(getApplication())) {
            _events.tryEmit(MainUiEvent.ShowSnackbar(currentStrings().exactAlarmPermissionRequired))
            _events.tryEmit(MainUiEvent.RequestExactAlarmPermission)
        }
    }

    fun onExactAlarmPermissionResult() {
        if (!_uiState.value.scheduledRunEnabled) return
        if (!DailyAlarmScheduler.scheduleAll(getApplication())) {
            _events.tryEmit(MainUiEvent.ShowSnackbar(currentStrings().exactAlarmPermissionRequired))
        }
    }

    fun ensureScheduledAlarms() {
        if (_uiState.value.scheduledRunEnabled) {
            DailyAlarmScheduler.scheduleAll(getApplication())
        }
    }

    fun onApplyClicked() {
        if (_uiState.value.isApplying) return
        if (!whitelistRepository.canWrite()) {
            _events.tryEmit(MainUiEvent.ShowSnackbar(currentStrings().permissionRequired))
            _events.tryEmit(MainUiEvent.RequestWriteSettings)
            return
        }
        applyCurrentSelection()
    }

    fun onWriteSettingsResult() {
        if (whitelistRepository.canWrite()) {
            applyCurrentSelection()
        } else {
            _events.tryEmit(MainUiEvent.ShowSnackbar(currentStrings().permissionRequired))
        }
    }

    fun onPermissionLaunchFailed() {
        _events.tryEmit(MainUiEvent.ShowSnackbar(currentStrings().permissionRequired))
    }

    fun onExactAlarmPermissionLaunchFailed() {
        _events.tryEmit(MainUiEvent.ShowSnackbar(currentStrings().exactAlarmPermissionRequired))
    }

    fun refreshActivationStatuses() {
        activationRefreshJob?.cancel()
        activationRefreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isCheckingActivation = true) }
            val entryIds = (AppCatalog.ids + _uiState.value.entries.map(AppEntry::id)).distinct()
            val statuses = runCatching {
                whitelistRepository.activationStatuses(entryIds)
            }.getOrElse {
                entryIds.associateWith { false }
            }
            _uiState.update {
                it.copy(
                    activationById = statuses,
                    isCheckingActivation = false,
                )
            }
        }
    }

    private fun applyCurrentSelection() {
        if (_uiState.value.isApplying) return
        val selection = _uiState.value.selectedPackages
        viewModelScope.launch {
            val strings = currentStrings()
            _uiState.update { it.copy(isApplying = true) }
            _events.emit(MainUiEvent.ShowSnackbar(strings.updating))
            val result = whitelistRepository.apply(selection)
            _uiState.update { it.copy(isApplying = false) }
            _events.emit(
                MainUiEvent.ShowSnackbar(
                    when (result) {
                        is WhitelistResult.Success -> strings.updateSuccess
                        WhitelistResult.AlreadyApplied -> strings.alreadyApplied
                        WhitelistResult.PermissionMissing -> strings.permissionRequired
                        is WhitelistResult.Failure -> strings.updateFailure
                    },
                ),
            )
            refreshActivationStatuses()
        }
    }

    private fun currentStrings(): UiStrings = uiStrings(_uiState.value.language)
}
