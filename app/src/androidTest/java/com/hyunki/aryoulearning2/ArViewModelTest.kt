package com.hyunki.aryoulearning2

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.rules.RxImmediateSchedulerRule
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.check
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ArViewModelTest {

    @Rule
    @JvmField
    var testSchedulerRule = RxImmediateSchedulerRule()

    @Rule
    @JvmField
    val ruleForLivaData = InstantTaskExecutorRule()

    @Mock
    lateinit var repository: MainRepository
    lateinit var model: ArViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val application = ApplicationProvider.getApplicationContext<Context>() as Application
        model = ArViewModel(application, repository)
    }

    private val mockObserver: Observer<ArState> = mock()

    // -------------------------------------------------------------
    // loadListOfMapsOfFutureModels  -> futureModelMapListLiveData
    // -------------------------------------------------------------

    @Test
    fun loadListOfMaps_emitsLoadingState_initially() {
        model.futureModelMapListLiveData.observeForever(mockObserver)
        model.loadListofMapsOfFutureModels(listOf())

        verify(mockObserver).onChanged(check {
            Assert.assertEquals(ArState.Loading, it)
        })
    }

    @Test
    fun loadListOfMaps_emitsSuccessState() {
        val modelList = arrayListOf(Model("test", "abc", "image1"))
        val expectedClass = ArState.Success.OnFutureModelMapListLoaded::class

        model.futureModelMapListLiveData.observeForever(mockObserver)
        val inOrder = inOrder(mockObserver)

        model.loadListofMapsOfFutureModels(modelList)

        inOrder.verify(mockObserver).onChanged(ArState.Loading)
        inOrder.verify(mockObserver).onChanged(check {
            Assert.assertEquals(expectedClass, it::class)
        })
    }

    @Test
    fun loadListOfMaps_emitsErrorState_onFailure() {
        model.futureModelMapListLiveData.observeForever(mockObserver)
        val inOrder = inOrder(mockObserver)

        model.loadListofMapsOfFutureModels(listOf())

        inOrder.verify(mockObserver).onChanged(ArState.Loading)
        inOrder.verify(mockObserver).onChanged(check {
            Assert.assertEquals(ArState.Error, it)
        })
    }

    // -------------------------------------------------------------
    // loadLetterFuturesFromModels -> futureLetterMapLiveData
    // -------------------------------------------------------------

    @Test
    fun loadLetterFutures_emitsLoadingState_initially() {
        model.futureLetterMapLiveData.observeForever(mockObserver)
        model.loadLetterFuturesFromModels(listOf())

        verify(mockObserver).onChanged(check {
            Assert.assertEquals(ArState.Loading, it)
        })
    }

    // -------------------------------------------------------------
    // loadLetterRenderables -> letterMapLiveData
    // (these two were previously observing the wrong LiveData)
    // -------------------------------------------------------------

    @Test
    fun loadLetterRenderables_emitsSuccessState() {
        val expectedClass = ArState.Success.OnLetterMapLoaded::class
        val renderable = mock<ModelRenderable>()
        val map = mutableMapOf(
            "a" to CompletableFuture.completedFuture(renderable)
        )

        val latch = CountDownLatch(1)
        val states = mutableListOf<ArState>()
        val observer = Observer<ArState> {
            states.add(it)
            if (it is ArState.Success.OnLetterMapLoaded) latch.countDown()
        }

        model.letterMapLiveData.observeForever(observer)

        try {
            model.loadLetterRenderables(map)

            Assert.assertTrue(latch.await(1, TimeUnit.SECONDS))
            Assert.assertTrue(states.first() is ArState.Loading)
            Assert.assertTrue(states.any { it::class == expectedClass })
        } finally {
            model.letterMapLiveData.removeObserver(observer)
        }
    }

    @Test
    fun loadLetterRenderables_emitsCorrectValues() {
        val renderable = mock<ModelRenderable>()
        val map = mutableMapOf(
            "abc" to CompletableFuture.completedFuture(renderable)
        )

        val states = mutableListOf<ArState>()
        val latch = CountDownLatch(1)

        val observer = Observer<ArState> { state ->
            states.add(state)
            if (state is ArState.Success.OnLetterMapLoaded) latch.countDown()
        }

        model.letterMapLiveData.observeForever(observer)

        try {
            model.loadLetterRenderables(map)

            Assert.assertTrue(
                "Timed out waiting for Success. States = $states",
                latch.await(1, TimeUnit.SECONDS)
            )

            val success = states.last { it is ArState.Success.OnLetterMapLoaded }
                    as ArState.Success.OnLetterMapLoaded

            val letterMap = success.letterMap  // adjust if your field differs

            val expectedKeys = listOf("abc")

            Assert.assertTrue(
                "Expected keys $expectedKeys but got ${letterMap.keys}",
                expectedKeys.all { letterMap.containsKey(it) }
            )
        } finally {
            model.letterMapLiveData.removeObserver(observer)
        }
    }

    @Test
    fun loadLetterRenderables_emitsErrorState_onFailure() {
        val failedFuture = CompletableFuture<ModelRenderable>().apply {
            completeExceptionally(RuntimeException("boom"))
        }
        val map = mutableMapOf("a" to failedFuture)

        val latch = CountDownLatch(1)
        val states = mutableListOf<ArState>()
        val observer = Observer<ArState> {
            states.add(it)
            if (it is ArState.Error) latch.countDown()
        }

        model.letterMapLiveData.observeForever(observer)

        try {
            model.loadLetterRenderables(map)

            Assert.assertTrue(latch.await(1, TimeUnit.SECONDS))
            Assert.assertTrue(states.first() is ArState.Loading)
            Assert.assertTrue(states.any { it is ArState.Error })
        } finally {
            model.letterMapLiveData.removeObserver(observer)
        }
    }

    // -------------------------------------------------------------
    // loadModelRenderables -> modelMapListLiveData
    // -------------------------------------------------------------

    @Test
    fun loadModelRenderables_emitsLoadingState_initially() {
        model.modelMapListLiveData.observeForever(mockObserver)
        model.loadModelRenderables(listOf(mutableMapOf()))

        verify(mockObserver).onChanged(check {
            Assert.assertEquals(ArState.Loading, it)
        })
    }

    @Test
    fun loadModelRenderables_emitsSuccessState() {
        val expectedClass = ArState.Success.OnModelMapListLoaded::class

        val renderable = mock<ModelRenderable>()
        val map = mutableMapOf(
            "abc" to CompletableFuture.completedFuture(renderable)
        )
        val mapList = arrayListOf(map)

        val latch = CountDownLatch(1)
        val states = mutableListOf<ArState>()
        val observer = Observer<ArState> {
            states.add(it)
            if (it is ArState.Success.OnModelMapListLoaded) latch.countDown()
        }

        model.modelMapListLiveData.observeForever(observer)

        try {
            model.loadModelRenderables(mapList)

            Assert.assertTrue(latch.await(1, TimeUnit.SECONDS))
            Assert.assertTrue(states.first() is ArState.Loading)
            Assert.assertTrue(states.any { it::class == expectedClass })
        } finally {
            model.modelMapListLiveData.removeObserver(observer)
        }
    }
}