package com.jmcaamanog.gymnemo.tile

import android.content.ComponentName
import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.material3.ButtonDefaults
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.jmcaamanog.gymnemo.R

private const val RESOURCES_VERSION = "2"

class MainTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> =
        Futures.immediateFuture(tile(requestParams, this))

    override fun onTileResourcesRequest(requestParams: ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> =
        Futures.immediateFuture(resources(requestParams))
}

private fun resources(requestParams: ResourcesRequest): ResourceBuilders.Resources {
    return ResourceBuilders.Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .build()
}

private fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val mainActivity = ComponentName(context.packageName, "com.jmcaamanog.gymnemo.presentation.MainActivity")
    
    val dashboardClick = ModifiersBuilders.Clickable.Builder()
        .setOnClick(ActionBuilders.launchAction(mainActivity))
        .setId("open_dashboard")
        .build()

    val trainClick = ModifiersBuilders.Clickable.Builder()
        .setOnClick(ActionBuilders.launchAction(mainActivity))
        .setId("open_train")
        .build()
    
    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(
            TimelineBuilders.Timeline.fromLayoutElement(
                materialScope(context, requestParams.deviceConfiguration) {
                    primaryLayout(
                        mainSlot = {
                            button(
                                onClick = dashboardClick,
                                labelContent = { text("Dashboard".layoutString) },
                                colors = ButtonDefaults.buttonColors(argb(0xFF1E1E1E), argb(0xFF00E5FF))
                            )
                        },
                        bottomSlot = {
                            button(
                                onClick = trainClick,
                                labelContent = { text("Entrenar".layoutString) },
                                colors = ButtonDefaults.buttonColors(argb(0xFF00E5FF), argb(0xFF000000))
                            )
                        }
                    )
                }
            )
        )
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(::resources) {
    tile(it, context)
}
