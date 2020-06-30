package com.hyunki.aryoulearning2.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.findFragment

import com.airbnb.lottie.LottieAnimationView
import com.google.ar.sceneform.ux.ArFragment

class LottieHelper {

    enum class AnimationType {
        SPARKLES,
        TAP,
        ERROR
    }

    fun getAnimationView(context: Context?, type: AnimationType): LottieAnimationView {
        val lav = LottieAnimationView(context)
        lav.id = type.ordinal
        lav.visibility = View.VISIBLE
        lav.loop(false)
        lav.setAnimation(getType(type))
        lav.scale = 1f
        lav.speed = .8f

        return lav
    }

    private fun getType(type: AnimationType): String {
        return when (type) {
            AnimationType.SPARKLES -> "explosionA.json"
            AnimationType.TAP -> "tap.json"
            AnimationType.ERROR -> "error.json"
        }
    }

    //adds a lottie view to the corresposnding x and y coordinates
    fun getAnimationViewOnTopOfLetter(lav: LottieAnimationView, x: Int, y: Int): LottieAnimationView {
        lav.x = x.toFloat()
        lav.y = y.toFloat()
        lav.playAnimation()
        return lav
    }

    fun placeTapAnimationOnScreen(lavTap: LottieAnimationView, width: Int, height: Int): LottieAnimationView {
        lavTap.x = (width / 2 - 50).toFloat()
        lavTap.y = (height / 2 - 50).toFloat()
        lavTap.playAnimation()
        return lavTap
    }
}
