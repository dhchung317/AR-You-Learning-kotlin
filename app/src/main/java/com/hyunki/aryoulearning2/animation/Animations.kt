package com.hyunki.aryoulearning2.animation

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.cardview.widget.CardView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.math.Vector3Evaluator
import com.hyunki.aryoulearning2.R

class Animations {

    class AR {

        fun createRotationAnimator(): ObjectAnimator {
            val orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0f)
            val orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 120f)
            val orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 240f)
            val orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 360f)

            val orbitAnimation = ObjectAnimator()
            orbitAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4)
            orbitAnimation.propertyName = "localRotation"
            orbitAnimation.setEvaluator(QuaternionEvaluator())

            orbitAnimation.repeatCount = ObjectAnimator.INFINITE
            orbitAnimation.repeatMode = ObjectAnimator.RESTART
            orbitAnimation.interpolator = LinearInterpolator()
            orbitAnimation.setAutoCancel(true)

            return orbitAnimation
        }

        fun createFloatAnimator(animatedNode: Node): ObjectAnimator {
            val floater = ObjectAnimator.ofObject(
                    animatedNode,
                    "localPosition",
                    Vector3Evaluator(),
                    animatedNode.localPosition,
                    Vector3(
                            animatedNode.localPosition.x,
                            animatedNode.localPosition.y + .5f,
                            animatedNode.localPosition.z))

            floater.repeatCount = ObjectAnimator.INFINITE
            floater.repeatMode = ObjectAnimator.REVERSE
            return floater
        }
    }


    class Normal {
        fun setCardFadeInAnimator(cv: CardView): ObjectAnimator {
            cv.alpha = 0f
            cv.visibility = View.VISIBLE
            val fadeAnimation = ObjectAnimator.ofFloat(cv, "alpha", 0f, 1.0f)
            fadeAnimation.duration = 1000
            fadeAnimation.startDelay = 500
            return fadeAnimation
        }

        fun setCardFadeOutAnimator(cv: CardView): ObjectAnimator {
            val fadeAnimation = ObjectAnimator.ofFloat(cv, "alpha", 1.0f, 0f)
            fadeAnimation.duration = 1000
            return fadeAnimation
        }

        fun getVibrator(itemView: View): Animation {
            return AnimationUtils.loadAnimation(itemView.context, R.anim.vibrate)
        }
    }
}
