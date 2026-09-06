package com.local.hyperoswhitelistkeeper.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.local.hyperoswhitelistkeeper.MainUiEvent
import com.local.hyperoswhitelistkeeper.MainUiState
import com.local.hyperoswhitelistkeeper.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainUiState,
    events: Flow<MainUiEvent>,
    onToggleEntry: (String, Boolean) -> Unit,
    onAddCustomApp: (String, String) -> Unit,
    onApply: () -> Unit,
    onCycleTheme: () -> Unit,
    onCycleLanguage: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showPicker by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(false) }
    val strings = uiStrings(state.language)

    LaunchedEffect(events) {
        events.collectLatest { event ->
            if (event is MainUiEvent.ShowSnackbar) {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Whitelist Keeper",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    TextButton(
                        onClick = onCycleLanguage,
                        modifier = Modifier
                            .width(48.dp)
                            .semantics { contentDescription = strings.languageDescription },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text = strings.languageButton,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    IconButton(onClick = { showGuide = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_help_outline),
                            contentDescription = strings.guideDescription,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = onCycleTheme) {
                        Icon(
                            painter = painterResource(R.drawable.ic_theme_mode),
                            contentDescription = strings.themeDescription(state.themeMode),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            ProtectedAppsCard(
                selectedCount = state.selectedPackages.size,
                strings = strings,
                onClick = { showPicker = true },
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onApply,
                enabled = !state.isApplying && state.selectedPackages.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                if (state.isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = if (state.isApplying) strings.applying else strings.applyWhitelist,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    if (showPicker) {
        AppPickerSheet(
            entries = state.entries,
            selectedIds = state.selectedPackages,
            activationById = state.activationById,
            isCheckingActivation = state.isCheckingActivation,
            strings = strings,
            onToggle = onToggleEntry,
            onAddCustomApp = onAddCustomApp,
            onDismiss = { showPicker = false },
        )
    }

    if (showGuide) {
        AlertDialog(
            onDismissRequest = { showGuide = false },
            title = { Text(strings.guideTitle) },
            text = { Text(strings.guideBody) },
            confirmButton = {
                TextButton(onClick = { showGuide = false }) {
                    Text(strings.guideClose)
                }
            },
        )
    }
}

@Composable
private fun ProtectedAppsCard(
    selectedCount: Int,
    strings: UiStrings,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = strings.protectedApps,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.selectedApps(selectedCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = strings.automaticRepair,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
