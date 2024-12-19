package dev.hloth.medsreminder.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.SuspendingTileService

private const val RESOURCES_VERSION = "0"

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

private fun createMedChip(
    context: Context,
    chipId: String,
    chipText: String
): LayoutElementBuilders.LayoutElement {
    return Chip.Builder(
        context,
        Clickable.Builder()
            .setId(chipId)
            .setOnClick(ActionBuilders.LoadAction.Builder().build())
            .build(),
        buildDeviceParameters(context.resources)
    )
        .setPrimaryLabelContent(chipText)
        .setWidth(expand())
        .setChipColors(ChipColors.secondaryChipColors(Colors.DEFAULT))
        .build()
}

private fun createChipRow(
    context: Context,
    leftChipId: String,
    leftChipText: String,
    rightChipId: String,
    rightChipText: String
): LayoutElementBuilders.LayoutElement {
    return LayoutElementBuilders.Row.Builder()
        .setWidth(expand())
        .setHeight(wrap())
        .addContent(
            createMedChip(context, leftChipId, leftChipText)
        )
        .addContent(
            createMedChip(context, rightChipId, rightChipText)
        )
        .build()
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

    return PrimaryLayout.Builder(requestParams.deviceConfiguration)
        .setResponsiveContentInsetEnabled(true)
        .setContent(
            LayoutElementBuilders.Column.Builder()
                .setWidth(expand())
                .setHeight(wrap())
                .addContent(
                    createChipRow(context, "med_1", "Med 1", "med_2", "Med 2")
                )
                .addContent(
                    createChipRow(context, "med_3", "Med 3", "med_4", "Med 4")
                )
                .addContent(
                    createChipRow(context, "med_6", "Med 6", "med_7", "Med 7")
                )
                .build()
        )
        .build()
}

@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(::resources) {
    tile(it, context)
}