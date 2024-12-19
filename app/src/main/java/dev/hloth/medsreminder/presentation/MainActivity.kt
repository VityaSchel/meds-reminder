@file:OptIn(ExperimentalHorologistApi::class)

package dev.hloth.medsreminder.presentation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.wear.compose.material.TitleCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        val clickableId =
            intent.getStringExtra(TileService.EXTRA_CLICKABLE_ID)

        setContent {
            WearApp(clickableId)
        }
    }
}

@Composable
fun WearApp(clickableId: String?) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.SingleButton
        )
    )

    MedsReminderTheme {
        ScreenScaffold(scrollState = columnState) {
            ScalingLazyColumn(
//                columnState = columnState
            ) {
                item {
                    ResponsiveListHeader(contentPadding = firstItemPadding()) {
                        Text(text = "Последние записи")
                    }
                }
                item {
                    Chip(
                        label = { Text("Example Chip") },
                        onClick = { },
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
    WearApp(null)
}