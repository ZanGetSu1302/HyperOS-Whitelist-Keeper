package com.local.hyperoswhitelistkeeper

import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WhitelistWorker.schedule(applicationContext)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (event == MainUiEvent.RequestWriteSettings) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_WRITE_SETTINGS,
                            "package:$packageName".toUri(),
                        )
                        runCatching { writeSettingsLauncher.launch(intent) }
                            .onFailure { viewModel.onPermissionLaunchFailed() }
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
                    onApply = viewModel::onApplyClicked,
                    onCycleTheme = viewModel::cycleTheme,
                    onCycleLanguage = viewModel::cycleLanguage,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.refreshActivationStatuses()
    }
}
