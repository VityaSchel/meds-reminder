@file:OptIn(ExperimentalHorologistApi::class)

package dev.hloth.medsreminder.presentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.wear.compose.material.TitleCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.startActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.Colors
import androidx.wear.tiles.TileService
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import dev.hloth.medsreminder.presentation.theme.MedsReminderTheme
import com.google.android.horologist.compose.material.ResponsiveListHeader
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.prefs.Preferences

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        val clickableId =
            intent.getStringExtra(TileService.EXTRA_CLICKABLE_ID)

        val storageHelper = StorageHelper(this)


        setContent {
            WearApp(this, clickableId, storageHelper)
        }
    }
}

fun openInstalledApp(context: Context) {
    val intent = Intent().apply {
        component = ComponentName(
            "com.samsung.android.watch.timer",
            "com.samsung.android.watch.timer.activity.TimerHomeActivity"
        )
    }
    startActivity(context, intent, null)
}

fun formatDate(dateTimeString: String): String {
    val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    val dateTime = LocalDateTime.parse(dateTimeString, inputFormatter)

    val now = LocalDateTime.now()
    val formatterTime = DateTimeFormatter.ofPattern("HH:mm")
    val formatterDayMonth = DateTimeFormatter.ofPattern("d MMM")

    return when {
        dateTime.toLocalDate() == now.toLocalDate() -> "сегодня, ${dateTime.format(formatterTime)}"
        dateTime.toLocalDate() == now.minusDays(1).toLocalDate() -> "вчера, ${dateTime.format(formatterTime)}"
        else -> "${dateTime.format(formatterDayMonth)}, ${dateTime.format(formatterTime)}"
    }
}

private val colors = listOf(
    "triptan_forte" to 0xFF202023,
    "active" to 0xFF3F3B36,
    "pankraza" to 0xFF3A0E07,
    "metigast" to 0xFF373A07,
    "nogast" to 0xFF071e3a,
)

private val Context.dataStore by preferencesDataStore(name = "meds_records_storage_v6")

data class UserRecord(val id: String, val med: String, val timestamp: String)

class StorageHelper(private val context: Context) {

    private val gson = Gson()

    private val recordsKey = stringPreferencesKey("meds_records_json")

    suspend fun saveToStorage(records: List<UserRecord>) {
        val json = gson.toJson(records)
        context.dataStore.edit { preferences ->
            preferences[recordsKey] = json
        }
    }

    suspend fun removeFromStorage(recordId: String) {
        val currentRecords = loadFromStorage().toMutableList()
        currentRecords.removeIf { it.id == recordId }
        saveToStorage(currentRecords)
    }

    suspend fun loadFromStorage(): List<UserRecord> {

        return try {
            val json = context.dataStore.data
                .map { preferences -> preferences[recordsKey] ?: "[]" }
                .first()

            gson.fromJson(json, Array<UserRecord>::class.java).toList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

@Composable
fun WearApp(context: Context, clickableId: String?, storageHelper: StorageHelper) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.SingleButton
        )
    )

    val storedEntries = remember { mutableStateListOf<Triple<String, String, String>>() }
    val recordToRemove = remember { mutableStateOf<Triple<String, String, String>?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val records = storageHelper.loadFromStorage()
        storedEntries.clear()
        storedEntries.addAll(records.map { Triple(it.id, it.med, it.timestamp) })
        isLoading.value = false
    }

    LaunchedEffect(clickableId, isLoading.value) {
        if (isLoading.value) return@LaunchedEffect
        clickableId?.let { med ->
            if(med == "hour_timer") {
                openInstalledApp(context)
            } else if (colors.any { it.first == med }) {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val uniqueKey = UUID.randomUUID().toString()
                storedEntries.add(0, Triple(uniqueKey, med, timestamp))
                storageHelper.saveToStorage(storedEntries.map {
                    UserRecord(
                        it.first,
                        it.second,
                        it.third
                    )
                })
            }
        }
    }

    LaunchedEffect(recordToRemove.value) {
        recordToRemove.value?.let { record ->
            storageHelper.removeFromStorage(record.first)
            storedEntries.removeIf { it.first == record.first }
            recordToRemove.value = null
        }
    }

    MedsReminderTheme {
        ScreenScaffold(scrollState = columnState) {
            ScalingLazyColumn(
//                state = columnState
            ) {
                item {
                    ResponsiveListHeader(contentPadding = firstItemPadding()) {
                        Text(text = "Последние записи")
                    }
                }
                items(
                    count = storedEntries.size,
                    key = { index -> storedEntries[index].first }
                ) { index ->
                    val entry = storedEntries[index]
                    Chip(
                        label = { Text(mapOf(
                            "triptan_forte" to "Триптан форте",
                            "active" to "Актив флора дуо",
                            "metigast" to "Метигаст",
                            "pankraza" to "Панкраза",
                            "nogast" to "Ногаст",
                        )[entry.second] ?: "Unknown") },
                        secondaryLabel = { Text(formatDate(entry.third)) },
                        onClick = { recordToRemove.value = entry },
                        modifier = Modifier.fillMaxSize(),
                        colors = ChipDefaults.secondaryChipColors()
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val mockContext = LocalContext.current
    val mockStorageHelper = StorageHelper(mockContext)

    WearApp(mockContext, null, mockStorageHelper)
}