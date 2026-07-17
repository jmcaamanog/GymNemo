package com.jmcaamanog.gymnemo.tile

import android.content.ComponentName
import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.ButtonDefaults
import androidx.wear.protolayout.material3.icon
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

private const val RESOURCES_VERSION = "3"

class MainTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> =
        Futures.immediateFuture(tile(requestParams, this))

    override fun onTileResourcesRequest(requestParams: ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> =
        Futures.immediateFuture(resources(requestParams))
}

private fun resources(requestParams: ResourcesRequest): ResourceBuilders.Resources {
    return ResourceBuilders.Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .addIdToImageMapping(
            "ic_dashboard",
            ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(
                    ResourceBuilders.AndroidImageResourceByResId.Builder()
                        .setResourceId(R.drawable.ic_dashboard)
                        .build()
                )
                .build()
        )
        .addIdToImageMapping(
            "ic_pesa_gym",
            ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(
                    ResourceBuilders.AndroidImageResourceByResId.Builder()
                        .setResourceId(R.drawable.ic_pesa_gym)
                        .build()
                )
                .build()
        )
        .build()
}

private fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val mainActivity = ComponentName(context.packageName, "com.jmcaamanog.gymnemo.presentation.MainActivity")
    
    val dashboardClick = ModifiersBuilders.Clickable.Builder()
        .setOnClick(
            ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(
                    ActionBuilders.AndroidActivity.Builder()
                        .setClassName("com.jmcaamanog.gymnemo.presentation.MainActivity")
                        .setPackageName(context.packageName)
                        .addKeyToExtraMapping("startScreen", ActionBuilders.stringExtra("dashboard"))
                        .build()
                )
                .build()
        )
        .setId("open_dashboard")
        .build()

    val trainClick = ModifiersBuilders.Clickable.Builder()
        .setOnClick(
            ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(
                    ActionBuilders.AndroidActivity.Builder()
                        .setClassName("com.jmcaamanog.gymnemo.presentation.MainActivity")
                        .setPackageName(context.packageName)
                        .addKeyToExtraMapping("startScreen", ActionBuilders.stringExtra("train_category"))
                        .build()
                )
                .build()
        )
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
                                iconContent = { icon("ic_dashboard") },
                                labelContent = { text("Dashboard".layoutString) },
                                colors = ButtonDefaults.run { filledTonalButtonColors() }
                            )
                        },
                        bottomSlot = {
                            button(
                                onClick = trainClick,
                                iconContent = { icon("ic_pesa_gym") },
                                labelContent = { text("Entrenar".layoutString) },
                                colors = ButtonDefaults.run { filledButtonColors() }
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
