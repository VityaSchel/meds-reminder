package dev.hloth.medsreminder.tile

import android.app.TaskStackBuilder
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.FONT_WEIGHT_NORMAL
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.expression.ProtoLayoutExperimental
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import dev.hloth.medsreminder.presentation.MainActivity

private const val RESOURCES_VERSION = "0"

private val colors = mapOf(
    "triptan_forte" to 0xFF464652,
    "active" to 0xFFc5b7a8,
    "pankraza" to 0xFFd3553f,
    "metigast" to 0xFFd4e113,
    "nogast" to 0xFF638aba,
    "aspan" to 0xFF579c65,
    "empty" to 0xFFffffff
)

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ) = resources(requestParams)

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ) = tile(requestParams, this)
}

private fun resources(
    requestParams: RequestBuilders.ResourcesRequest
): ResourceBuilders.Resources {
    return ResourceBuilders.Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .build()
}

@androidx.annotation.OptIn(ProtoLayoutExperimental::class)
private fun createMedChip(
    context: Context,
    chipId: String,
    chipText: String
): LayoutElementBuilders.LayoutElement {
    return Button.Builder(
        context,
        Clickable.Builder()
            .setId(chipId)
            .setOnClick(
                ActionBuilders.launchAction(
                    ComponentName(context.packageName, context.packageName + ".WriteMed")
                )
            )
            .build()
    )
        .setCustomContent(
            LayoutElementBuilders.Text.Builder()
                .setText(chipText)
                .setFontStyle(
                    LayoutElementBuilders.FontStyle.Builder()
                        .setSize(sp(12f))
                        .setPreferredFontFamilies(LayoutElementBuilders.FontStyle.ROBOTO_FLEX_FONT)
                        .setWeight(FONT_WEIGHT_NORMAL)
                        .setColor(argb(0xFF000000.toInt()))
                        .build()
                )
                .setMaxLines(2)
                .build()

        )
        .setButtonColors(ButtonColors(colors[chipId]!!.toInt(), 0xFF000000.toInt()))
        .setSize(dp(47f))
        .build()
}

private fun createChipColumn(
    context: Context,
    chipData: List<Pair<String, String>>
): LayoutElementBuilders.LayoutElement {
    val columnBuilder = LayoutElementBuilders.Column.Builder()
        .setHeight(wrap())

    chipData.forEach { (chipId, chipText) ->
        columnBuilder.addContent(
            createMedChip(context, chipId, chipText)
        )
        columnBuilder.addContent(
            LayoutElementBuilders.Spacer.Builder()
                .setHeight(dp(4f))
                .build()
        )
    }

    return columnBuilder.build()
}

private fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val singleTileTimeline = TimelineBuilders.Timeline.Builder()
        .addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(tileLayout(requestParams, context))
                        .build()
                )
                .build()
        )
        .build()

    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(singleTileTimeline)
        .build()
}

private fun tileLayout(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): LayoutElementBuilders.LayoutElement {

    val gap = LayoutElementBuilders.Spacer.Builder()
        .setWidth(dp(4f))
        .build()

    val buttons = listOf(
        "triptan_forte" to "Трип-\nтан",
        "active" to "Актив\nфлоур",
        "pankraza" to "Панк-\nраза",
        "metigast" to "Мети-\nгаст",
        "nogast" to "Ногаст",
        "aspan" to "Аспан",
        "empty" to "См.\nзаписи",
    )

    return PrimaryLayout.Builder(requestParams.deviceConfiguration)
        .setResponsiveContentInsetEnabled(true)
        .setContent(
            LayoutElementBuilders.Column.Builder()
                .addContent(
                    LayoutElementBuilders.Row.Builder()
                        .setWidth(wrap())
                        .setHeight(wrap())
                        .addContent(
                            createChipColumn(context, listOf(buttons[0], buttons[5]))
                        )
                        .addContent(gap)
                        .addContent(
                            createChipColumn(context, listOf(buttons[1], buttons[6], buttons[4]))
                        )
                        .addContent(gap)
                        .addContent(
                            createChipColumn(context, listOf(buttons[2], buttons[3]))
                        )
                        .build()
                )
                .setWidth(wrap())
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .build()
        )
        .build()
}

@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(::resources) {
    tile(it, context)
}