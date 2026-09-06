package com.local.hyperoswhitelistkeeper.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.local.hyperoswhitelistkeeper.model.AppEntry
import java.text.Collator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerSheet(
    entries: List<AppEntry>,
    selectedIds: Set<String>,
    activationById: Map<String, Boolean>,
    isCheckingActivation: Boolean,
    strings: UiStrings,
    onToggle: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val collator = remember { Collator.getInstance(Locale.getDefault()) }
    val baseConfiguration = LocalConfiguration.current
    val useDarkSystemBars = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val sheetConfiguration = remember(baseConfiguration, useDarkSystemBars) {
        Configuration(baseConfiguration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (useDarkSystemBars) {
                    Configuration.UI_MODE_NIGHT_YES
                } else {
                    Configuration.UI_MODE_NIGHT_NO
                }
        }
    }
    val filtered = remember(entries, query) {
        val normalized = query.trim()
        entries.filter { entry ->
            normalized.isEmpty() ||
                entry.label.contains(normalized, ignoreCase = true) ||
                entry.id.contains(normalized, ignoreCase = true)
        }
    }
    val selected = filtered.filter { it.id in selectedIds }.sortedWith { a, b ->
        collator.compare(a.label, b.label)
    }
    val other = filtered.filterNot { it.id in selectedIds }.sortedWith { a, b ->
        collator.compare(a.label, b.label)
    }

    // Material 3's Android sheet window derives its system-bar icons from
    // isSystemInDarkTheme(). Provide a configuration matching the app's manual
    // theme so Dark mode remains legible even when the device itself is Light.
    CompositionLocalProvider(LocalConfiguration provides sheetConfiguration) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f),
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.chooseApps,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onDismiss) {
                    Text(strings.done)
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text(strings.searchHint) },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 12.dp),
            ) {
                if (selected.isNotEmpty()) {
                    item(key = "selected_header") { SectionHeader(strings.selectedHeader) }
                    items(selected, key = { "selected_${it.id}" }) { entry ->
                        AppPickerRow(
                            entry = entry,
                            checked = true,
                            isActivated = activationById[entry.id],
                            isCheckingActivation = isCheckingActivation,
                            strings = strings,
                            onToggle = { onToggle(entry.id, it) },
                        )
                    }
                }
                if (other.isNotEmpty()) {
                    item(key = "other_header") { SectionHeader(strings.otherAppsHeader) }
                    items(other, key = { "other_${it.id}" }) { entry ->
                        AppPickerRow(
                            entry = entry,
                            checked = false,
                            isActivated = activationById[entry.id],
                            isCheckingActivation = isCheckingActivation,
                            strings = strings,
                            onToggle = { onToggle(entry.id, it) },
                        )
                    }
                }
                if (selected.isEmpty() && other.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = strings.noAppsFound,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
    )
}

@Composable
private fun AppPickerRow(
    entry: AppEntry,
    checked: Boolean,
    isActivated: Boolean?,
    isCheckingActivation: Boolean,
    strings: UiStrings,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onToggle,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 12.dp),
        ) {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.bodyLarge,
            )
            val checking = isCheckingActivation && isActivated == null
            val active = isActivated == true
            val activeColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
                Color(0xFF81C784)
            } else {
                Color(0xFF2E7D32)
            }
            Text(
                text = when {
                    checking -> strings.checking
                    active -> strings.activated
                    else -> strings.notActivated
                },
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    checking -> MaterialTheme.colorScheme.onSurfaceVariant
                    active -> activeColor
                    else -> MaterialTheme.colorScheme.error
                },
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    )
}
