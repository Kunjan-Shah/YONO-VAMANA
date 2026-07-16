package com.yono.yono_vamana.navigation

/** Navigation routes for the YONO-VAMANA app graph. */
sealed class VamanaDestination(val route: String) {

    data object SecuritySetup : VamanaDestination("security_setup")

    data object Dashboard : VamanaDestination("dashboard")

    data object LayerDetail : VamanaDestination("layer_detail/{layerId}") {
        const val ARG_LAYER_ID = "layerId"
        fun createRoute(layerId: String) = "layer_detail/$layerId"
    }
}
