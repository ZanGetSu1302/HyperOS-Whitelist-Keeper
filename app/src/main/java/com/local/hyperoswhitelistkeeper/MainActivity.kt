package com.local.hyperoswhitelistkeeper

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.local.hyperoswhitelistkeeper.ui.MainScreen
import com.local.hyperoswhitelistkeeper.ui.theme.HyperOSWhitelistKeeperTheme
import com.local.hyperoswhitelistkeeper.whitelist.WhitelistWorker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val writeSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.onWriteSettingsResult()
    }

    private val exactAlarmLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.onExactAlarmPermissionResult()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WhitelistWorker.cancelLegacySchedule(applicationContext)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is MainUiEvent.ShowSnackbar -> Unit
                        MainUiEvent.RequestWriteSettings -> {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                "package:$packageName".toUri(),
                            )
                            runCatching { writeSettingsLauncher.launch(intent) }
                                .onFailure { viewModel.onPermissionLaunchFailed() }
                        }

                        MainUiEvent.RequestExactAlarmPermission -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    "package:$packageName".toUri(),
                                )
                                runCatching { exactAlarmLauncher.launch(intent) }
                                    .onFailure {
                                        viewModel.onExactAlarmPermissionLaunchFailed()
                                    }
                            } else {
                                viewModel.onExactAlarmPermissionResult()
                            }
                        }
                    }
                }
            }
        }

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            HyperOSWhitelistKeeperTheme(themeMode = state.themeMode) {
                MainScreen(
                    state = state,
                    events = viewModel.events,
                    onToggleEntry = viewModel::setSelected,
                    onAddCustomApp = viewModel::addCustomApp,
                    onApply = viewModel::onApplyClicked,
                    onCycleTheme = viewModel::cycleTheme,
                    onCycleLanguage = viewModel::cycleLanguage,
                    onRunOnBootChanged = viewModel::setRunOnBootEnabled,
                    onScheduledRunChanged = viewModel::setScheduledRunEnabled,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.refreshActivationStatuses()
        viewModel.ensureScheduledAlarms()
    }
}
