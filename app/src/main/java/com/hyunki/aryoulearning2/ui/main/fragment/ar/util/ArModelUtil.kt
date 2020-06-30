package com.hyunki.aryoulearning2.ui.main.fragment.ar.util

import com.hyunki.aryoulearning2.animation.Animations
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

import java.util.HashSet
import java.util.Random

class ArModelUtil {
    companion object {
        const val xRange = 3
        const val yRange = 3
        const val zRange = 2
    }

    private val r = Random()
    private val collisionSet = HashSet<Vector3>()

    private val randomCoordinates: Vector3
        get() = Vector3(getRandom(5, -5).toFloat(),
                getRandom(2, -6).toFloat(),
                getRandom(-2, -10).toFloat())

    private fun getRandom(max: Int, min: Int): Float {
        return min + r.nextFloat() * (max - min)
    }

    fun getGameAnchor(model: ModelRenderable): Node {
        val base = Node()
        val mainModel = Node()
        mainModel.setParent(base)
        mainModel.renderable = model
        mainModel.localPosition = Vector3(base.localPosition.x, //x
                base.localPosition.y, //y
                base.localPosition.z)

        mainModel.setLookDirection(Vector3(0f, 0f, 4f))
        mainModel.localScale = Vector3(1.0f, 1.0f, 1.0f)

        val rotate = Animations.AR().createRotationAnimator()

        rotate.target = mainModel
        rotate.duration = 7000
        rotate.start()

        return base
    }

    fun getLetter(parent: Node, renderable: ModelRenderable?, arFragment: ArFragment): AnchorNode {

        val session = arFragment.arSceneView.session
        var anchor: Anchor? = setUpAnchorAndReturn(session)

        val base = AnchorNode(anchor)
        val trNode = setUpTrNodeAndReturn(arFragment,renderable,parent)
        trNode.setParent(base)
        return base
    }

    private fun setUpAnchorAndReturn(session: Session?): Anchor? {
        lateinit var anchor: Anchor

        if (session != null) {
            try {
                session.resume()
            } catch (e: CameraNotAvailableException) {
                e.printStackTrace()
            }
            anchor = session.createAnchor(Pose(getPos(), getRotation()))
        }
        return anchor
    }

    private fun getPos(): FloatArray {
        return floatArrayOf(0f, 0f, 0f)
    }

    private fun getRotation(): FloatArray {
        return floatArrayOf(0f, 0f, 0f, 0f)
    }

    private fun setUpTrNodeAndReturn(arFragment: ArFragment, renderable: ModelRenderable?, parent: Node): TransformableNode {
        val trNode = TransformableNode(arFragment.transformationSystem)

        return trNode.apply {
            this.renderable = renderable
            this.localPosition = getRandomUniqueCoordinates(parent.localPosition)
        }.let {
            setTrNodeLookAndScale(it)
            animateTrNode(it)
        }
    }

    private fun setTrNodeLookAndScale(trNode: TransformableNode): TransformableNode{
        trNode.setLookDirection(Vector3(0f, 0f, getRandom(4, 0).toFloat()))
        trNode.localScale = Vector3(1.0f, getRandom(10, 0) * .1f, 1.0f)
        return trNode
    }

    private fun animateTrNode(trNode: TransformableNode): TransformableNode{
        val floating = Animations.AR().createFloatAnimator(trNode)
        val rotate = Animations.AR().createRotationAnimator()
        rotate.target = trNode
        rotate.duration = getRandom(4000, 3000).toLong()
        rotate.start()

        floating.target = trNode
        floating.duration = getRandom(2500, 2000).toLong()
        floating.start()

        return trNode
    }
    
    fun checkDoesLetterCollide(newV3: Vector3, parentModel: Vector3): Boolean {
        if ((newV3.x <= parentModel.x + xRange && newV3.x >= parentModel.x - xRange || newV3.x == parentModel.x)
                && (newV3.y <= parentModel.y + yRange && newV3.y >= parentModel.y - yRange || newV3.y == parentModel.y)
                && (newV3.z <= parentModel.z + zRange && newV3.z >= parentModel.z - zRange || newV3.z == parentModel.z)) {
            return true
        }

        if (collisionSet.isEmpty()) {
            collisionSet.add(newV3)
            return false
        }
        //if the coordinates are within a range of any existing coordinates
        for (v in collisionSet) {
            return if ((newV3.x <= v.x + xRange && newV3.x >= v.x - xRange || newV3.x == v.x)
                    && (newV3.y <= v.y + yRange && newV3.y >= v.y - yRange || newV3.y == v.y)
                    && (newV3.z <= v.z + zRange && newV3.z >= v.z - zRange|| newV3.z == v.z)) {
                true
            } else {
                collisionSet.add(newV3)
                false
            }
        }
        return true
    }

    private fun getRandomUniqueCoordinates(checkAgainstTheseCoordinates: Vector3): Vector3{
        var coordinates = randomCoordinates

        while (checkDoesLetterCollide(coordinates, checkAgainstTheseCoordinates)) {
            coordinates = randomCoordinates
        }
        return coordinates
    }

    fun refreshCollisionSet() {
        collisionSet.clear()
    }
}
