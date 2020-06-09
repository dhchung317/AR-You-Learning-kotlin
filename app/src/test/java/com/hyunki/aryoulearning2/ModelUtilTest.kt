package com.hyunki.aryoulearning2

import com.google.ar.sceneform.math.Vector3
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ModelUtil
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ModelUtilTest {

    lateinit var modelUtil: ModelUtil

    private var xRange: Int = 0
    private var yRange: Int = 0
    private var zRange: Int = 0

    @Before
    fun setup() {
        modelUtil = ModelUtil()

        xRange = ModelUtil.xRange
        yRange = ModelUtil.yRange
        zRange = ModelUtil.zRange
    }

    @Test
    fun `assert true when checkLetterDoesCollide() compares vectors with equal properties`() {
        val vector1 = Vector3(1f,1f,1f)
        val vector2 = Vector3(1f,1f,1f)

        val expected = true

        val actual = modelUtil.checkDoesLetterCollide(vector1,vector2)

        assertEquals(expected,actual)
    }

    @Test
    fun `assert true when checkLetterDoesCollide() compares vectors with properties within a plus range`() {
        val vector1 = Vector3(1f ,1f,1f)
        val vector2 = Vector3(1f + xRange,1f + yRange,1f + zRange)

        val expected = true

        val actual = modelUtil.checkDoesLetterCollide(vector1,vector2)

        assertEquals(expected,actual)
    }

    @Test
    fun `assert true when checkLetterDoesCollide() compares vectors with properties within a minus range`() {
        val vector1 = Vector3(1f ,1f,1f)
        val vector2 = Vector3(1f - xRange,1f - yRange,1f - zRange)

        val expected = true

        val actual = modelUtil.checkDoesLetterCollide(vector1,vector2)

        assertEquals(expected,actual)
    }

    @Test
    fun `assert false when checkLetterDoesCollide() compares vectors with properties outside a minus range`() {
        val vector1 = Vector3(1f ,1f, 1f)
        val vector2 = Vector3(1f - (xRange + 1),1f - (yRange + 1),1f - (zRange + 1))

        val expected = false

        val actual = modelUtil.checkDoesLetterCollide(vector1,vector2)

        assertEquals(expected,actual)
    }

    @Test
    fun `assert  when checkLetterDoesCollide() compares vectors with properties outside a plus range`() {
        val vector1 = Vector3(1f ,1f, 1f)
        val vector2 = Vector3(1f + (xRange + 1),1f + (yRange + 1),1f + (zRange + 1))

        val expected = false

        val actual = modelUtil.checkDoesLetterCollide(vector1,vector2)

        assertEquals(expected,actual)
    }
}