package com.hyunki.aryoulearning2

import com.google.ar.sceneform.math.Vector3
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ModelUtil
import org.junit.Before
import java.util.*

class ModelUtilTest {

    private lateinit var collisionSet: HashSet<Vector3>

    @Before
    fun setup() {
        collisionSet = HashSet<Vector3>()
    }

    companion object {
        private val r = Random()

        private fun getRandom(max: Int, min: Int): Int {
            return r.nextInt(max - min) + min
        }

        private val randomCoordinates: Vector3
            get() = Vector3(getRandom(5, -5).toFloat(),
                    getRandom(2, -6).toFloat(),
                    getRandom(-2, -10).toFloat())
    }
}