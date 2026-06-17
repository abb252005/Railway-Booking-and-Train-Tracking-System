@file:OptIn(InternalResourceApi::class)

package railway_booking_and_train_tracking_system.shared.generated.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceContentHash
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String =
    "composeResources/railway_booking_and_train_tracking_system.shared.generated.resources/"

@delegate:ResourceContentHash(-1_430_559_082)
internal val Res.drawable.bcg_admin_1: DrawableResource by lazy {
      DrawableResource("drawable:bcg_admin_1", setOf(
        ResourceItem(setOf(), "${MD}drawable/bcg_admin_1.jpg", -1, -1),
      ))
    }

@delegate:ResourceContentHash(-1_274_413_324)
internal val Res.drawable.bcg_admin_2: DrawableResource by lazy {
      DrawableResource("drawable:bcg_admin_2", setOf(
        ResourceItem(setOf(), "${MD}drawable/bcg_admin_2.jpg", -1, -1),
      ))
    }

@delegate:ResourceContentHash(-1_430_559_082)
internal val Res.drawable.nature_bg: DrawableResource by lazy {
      DrawableResource("drawable:nature_bg", setOf(
        ResourceItem(setOf(), "${MD}drawable/nature_bg.jpg", -1, -1),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainDrawable0Resources(map: MutableMap<String, DrawableResource>) {
  map.put("bcg_admin_1", Res.drawable.bcg_admin_1)
  map.put("bcg_admin_2", Res.drawable.bcg_admin_2)
  map.put("nature_bg", Res.drawable.nature_bg)
}
