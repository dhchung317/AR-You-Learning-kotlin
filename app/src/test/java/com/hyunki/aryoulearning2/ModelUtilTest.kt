package com.hyunki.aryoulearning2

import com.google.ar.core.Session
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformationSystem
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ModelUtil
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ModelUtilTest {

    lateinit var modelUtil: ModelUtil

    private var xRange: Int = 0
    private var yRange: Int = 0
    private var zRange: Int = 0

    @Mock
    lateinit var arFragment: ArFragment
    @Mock
    lateinit var transformationSystem: TransformationSystem
    @Mock
    lateinit var sceneView: ArSceneView
    @Mock
   lateinit var session: Session

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

//    @Test
//    fun `test getLetter()`(){
//        val node: Node = mock()
//        val modelRenderable: ModelRenderable = mock()
//
//        whenever(arFragment.transformationSystem).thenReturn(transformationSystem)
//        whenever(arFragment.arSceneView).thenReturn(sceneView)
//        whenever(arFragment.arSceneView.session).thenReturn(session)
//
//        val expected = modelRenderable.id
//
//        val x = modelUtil.getLetter(node,modelRenderable,arFragment)
//
//        for(node in x.children){
//            val actual = node.renderable?.id?.get()
//            assertEquals(expected, actual)
//        }
//    }
}