package com.hyunki.aryoulearning2.ui.main.fragment.ar.util

import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.ux.ArFragment
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArViewModel
import com.hyunki.aryoulearning2.ui.main.fragment.ar.GameViewModel
import kotlin.math.sqrt

private const val MIN_DISTANCE_METERS = 0.5f
private const val MAX_DISTANCE_METERS = 1.5f
private fun distanceBetween(cameraPose: Pose, hitPose: Pose): Float {
    val camT = cameraPose.translation
    val hitT = hitPose.translation
    val dx = camT[0] - hitT[0]
    val dy = camT[1] - hitT[1]
    val dz = camT[2] - hitT[2]
    return sqrt(dx * dx + dy * dy + dz * dz)
}

fun handleAutoPlacementFrame(
    sceneView: ArSceneView,
    arFragment: ArFragment,
    gameViewModel: GameViewModel,
    arViewModel: ArViewModel,
    autoPlacementListener: Scene.OnUpdateListener,
    checkHit: (HitResult?) -> Boolean
) {
    // Stop if game already placed
    if (gameViewModel.hasPlacedGame.value == true) {
        sceneView.scene.removeOnUpdateListener(autoPlacementListener)
        return
    }

    // Stop if AR/game not ready
    if (!arViewModel.isLettersLoaded() ||
        !arViewModel.isModelsLoaded() ||
        gameViewModel.currentWord == null
    ) return

    val frame = sceneView.arFrame ?: return
    if (sceneView.width == 0 || sceneView.height == 0) return

    val cameraPose = frame.camera.pose
    val cx = sceneView.width / 2f
    val cy = sceneView.height / 2f

    val hits = frame.hitTest(cx, cy)
    if (hits.isEmpty()) return

    // Pick the first good hit within our distance band
    val chosenHit = hits.firstOrNull { hit ->
        val plane = hit.trackable as? Plane ?: return@firstOrNull false
        if (!plane.isPoseInPolygon(hit.hitPose) ||
            plane.trackingState != TrackingState.TRACKING
        ) return@firstOrNull false

        val dist = distanceBetween(cameraPose, hit.hitPose)
        dist in MIN_DISTANCE_METERS..MAX_DISTANCE_METERS
    } ?: return

    if (!checkHit(chosenHit)) return

    gameViewModel.setHasPlacedGame(true)

    // Hide plane visuals & discovery
    sceneView.planeRenderer.isEnabled = false
    arFragment.planeDiscoveryController.hide()
    arFragment.planeDiscoveryController.setInstructionView(null)

    sceneView.scene.removeOnUpdateListener(autoPlacementListener)
}
