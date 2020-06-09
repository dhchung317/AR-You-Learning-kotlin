package com.hyunki.aryoulearning2

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hyunki.aryoulearning2.animation.LottieHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LottieHelperTest {

    lateinit var lottieHelper: LottieHelper

    lateinit var context: Context
    @Before
    fun setup() {
        lottieHelper = LottieHelper()
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `assert that getType() with params SPARKLES returns view with sparkles animation`() {
        val expected = LottieHelper.AnimationType.SPARKLES.ordinal

        val actual = lottieHelper.getAnimationView(context,LottieHelper.AnimationType.SPARKLES).id

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that getType() with params TAP returns view with tap animation`() {
        val expected = LottieHelper.AnimationType.TAP.ordinal

        val actual = lottieHelper.getAnimationView(context,LottieHelper.AnimationType.TAP).id

        assertEquals(expected, actual)
    }

    @Test
    fun `assert that getType() with params ERROR returns view with error animation`() {
        val expected = LottieHelper.AnimationType.ERROR.ordinal

        val actual = lottieHelper.getAnimationView(context,LottieHelper.AnimationType.ERROR).id

        assertEquals(expected, actual)
    }
}