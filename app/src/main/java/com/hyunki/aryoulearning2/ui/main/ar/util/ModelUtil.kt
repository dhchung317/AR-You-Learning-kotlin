package com.hyunki.aryoulearning2.ui.main.ar.util

import android.animation.ObjectAnimator

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

object ModelUtil {

    private val r = Random()
    private val collisionSet = HashSet<Vector3>()

    private//x
    //y
    //z
    val randomCoordinates: Vector3
        get() = Vector3(getRandom(5, -5).toFloat(),
                getRandom(1, -2).toFloat(),
                getRandom(-2, -10).toFloat())

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

        //        for (int i = 0; i < name.length(); i++) {
        //            createLetter(
        //                    Character.toString(name.charAt(i)),
        //                    name, base, model);
        //        }

        val rotate = Animations.AR().createRotationAnimator()

        rotate.target = mainModel
        rotate.duration = 7000
        rotate.start()

        return base
        //        collisionSet.clear(); // should call this outside of this method as it is being called

    }


    fun getLetter(parent: Node, renderable: ModelRenderable?, arFragment: ArFragment): AnchorNode {

        val pos = floatArrayOf(0f, //x
                0f, //y
                0f)//z
        val rotation = floatArrayOf(0f, 0f, 0f, 0f)

        val session = arFragment.arSceneView.session
        var anchor: Anchor? = null

        if (session != null) {

            try {
                session.resume()

            } catch (e: CameraNotAvailableException) {
                e.printStackTrace()
            }

            anchor = session.createAnchor(Pose(pos, rotation))

        }

        val base = AnchorNode(anchor)
        val trNode = TransformableNode(arFragment.transformationSystem)
        trNode.renderable = renderable
        trNode.setParent(base)

        var coordinates = randomCoordinates

        while (checkDoesLetterCollide(coordinates, parent.localPosition)) {
            coordinates = randomCoordinates
        }

        trNode.localPosition = coordinates
        trNode.setLookDirection(Vector3(0f, 0f, getRandom(4, 0).toFloat()))
        trNode.localScale = Vector3(1.0f, getRandom(10, 0) * .1f, 1.0f)

        val floating = Animations.AR().createFloatAnimator(trNode)
        val rotate = Animations.AR().createRotationAnimator()
        rotate.target = trNode
        rotate.duration = getRandom(4000, 3000).toLong()
        rotate.start()

        floating.target = trNode
        floating.duration = getRandom(2500, 2000).toLong()
        floating.start()

        return base
    }

    private fun getRandom(max: Int, min: Int): Int {
        return r.nextInt(max - min) + min
    }

    private fun checkDoesLetterCollide(newV3: Vector3, parentModel: Vector3): Boolean {

        if (collisionSet.isEmpty()) {
            collisionSet.add(newV3)
            return false
        }


        if (newV3.x < parentModel.x + 2 && newV3.x > parentModel.x - 2
                && newV3.y < parentModel.y + 2 && newV3.y > parentModel.y - 2
                && newV3.z < parentModel.z + 2 && newV3.z > parentModel.z - 10) {
            return true
        }

        for (v in collisionSet) {
            //if the coordinates are within a range of any existing coordinates
            if (newV3.x < v.x + 3 && newV3.x > v.x - 3
                    && newV3.y < v.y + 4 && newV3.y > v.y - 4
                    && newV3.z < v.z + 5 && newV3.z > v.z - 5) {
                return true
            } else {
                collisionSet.add(newV3)
                return false
            }
        }
        return true
    }

    fun refreshCollisionSet() {
        collisionSet.clear()
    }
}
