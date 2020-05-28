package com.hyunki.aryoulearning2.animation

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout

import com.airbnb.lottie.LottieAnimationView

class LottieHelper {

    enum class AnimationType {
        SPARKLES,
        TAP,
        ERROR
    }

    fun getAnimationView(context: Context?, type: AnimationType): LottieAnimationView {
        val lav = LottieAnimationView(context)
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
    fun addAnimationViewOnTopOfLetter(lav: LottieAnimationView, x: Int, y: Int, f: FrameLayout) {
        lav.x = x.toFloat()
        lav.y = y.toFloat()
        f.addView(lav, 300, 300)
        lav.playAnimation()
        lav.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                f.removeView(lav)
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    fun addTapAnimationToScreen(lavTap: LottieAnimationView, activity: Activity, f: FrameLayout) {
        val width = activity.window.decorView.width
        val height = activity.window.decorView.height
        lavTap.x = (width / 2 - 50).toFloat()
        lavTap.y = (height / 2 - 50).toFloat()
        f.addView(lavTap, 500, 500)
        lavTap.playAnimation()
    }
}
