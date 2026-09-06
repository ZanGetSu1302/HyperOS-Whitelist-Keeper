package com.local.hyperoswhitelistkeeper.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.local.hyperoswhitelistkeeper.model.AppEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PreferencesRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `selected packages survive save and reload`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(temporaryFolder.root, "keeper.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )
        val saved = linkedSetOf("A", "B", "C")

        PreferencesRepository(dataStore).saveSelectedPackages(saved)
        val reloaded = PreferencesRepository(dataStore).selectedPackages.first()

        assertEquals(saved, reloaded)
        scope.cancel()
    }

    @Test
    fun `missing selection uses product defaults`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(temporaryFolder.root, "defaults.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )

        val selected = PreferencesRepository(dataStore).selectedPackages.first()

        assertEquals(AppCatalog.defaultSelection, selected)
        scope.cancel()
    }

    @Test
    fun `language survives save and reload`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(temporaryFolder.root, "language.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )

        PreferencesRepository(dataStore).saveAppLanguage(AppLanguage.EN)
        val reloaded = PreferencesRepository(dataStore).appLanguage.first()

        assertEquals(AppLanguage.EN, reloaded)
        scope.cancel()
    }

    @Test
    fun `missing language defaults to Vietnamese`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(temporaryFolder.root, "language-default.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )

        val language = PreferencesRepository(dataStore).appLanguage.first()

        assertEquals(AppLanguage.VI, language)
        scope.cancel()
    }

    @Test
    fun `custom apps survive save and reload`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(temporaryFolder.root, "custom-apps.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )
        val saved = AppEntry("vn.example.bank", "Ngân hàng mẫu")
        val repository = PreferencesRepository(dataStore)

        repository.saveCustomApp(saved)
        val reloaded = PreferencesRepository(dataStore).customApps.first()

        assertEquals(listOf(saved), reloaded)
        scope.cancel()
    }
}
