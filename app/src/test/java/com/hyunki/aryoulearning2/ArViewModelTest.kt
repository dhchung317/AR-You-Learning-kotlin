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
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.CompletableFuture
//TODO remake tests for coroutines
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
        MockitoAnnotations.initMocks(this)
        val application = ApplicationProvider.getApplicationContext<Context>() as Application
        model = ArViewModel(application, repository)
    }


    private fun createObserver(): Observer<ArState> = spy(Observer { })

    @Test
    fun `assert fetchModelsFromRepository() sets modelLiveData to arStateLoading on call`() {
        val testCat = "testCat"

        val expected = ArState.Loading

        whenever(repository.getModelsByCat(testCat))
                .thenReturn(Single.never())

        model.fetchModelsFromRepository(testCat)

        val actual = model.getModelLiveData().value

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `assert fetchModelsFromRepository() sets modelLiveData to arStateSuccess on complete`() {
        val testCat = "testCat"
        val testList = arrayListOf<Model>()
        testList.add(Model(testCat, "test1", "image1"))

        val expected = ArState.Success.OnModelsLoaded(testList)

        whenever(repository.getModelsByCat(testCat))
                .thenReturn(Single.just(testList))

        model.fetchModelsFromRepository(testCat)

        val actual = model.getModelLiveData().value

        assertNotNull(actual)
        assertEquals(expected, actual)

        val state = actual as ArState.Success.OnModelsLoaded
        val stateVal = state.models

        assertTrue(stateVal[0].category == testCat)
    }

    @Test
    fun `assert fetchModelsFromRepository() sets modelLiveData to arStateError on error`() {
        val testCat = "testCat"

        val expected = ArState.Error

        whenever(repository.getModelsByCat(testCat))
                .thenReturn(Single.error(Throwable()))

        model.fetchModelsFromRepository(testCat)

        val actual = model.getModelLiveData().value

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `assert loadListOfMapsOfFutureModels() sets futureModelMapListLiveData to arStateLoading on call before complete`() {
        val expected = ArState.Loading

        val mockObserver = this.createObserver()

        model.getFutureModelMapListLiveData().observeForever(mockObserver)

        model.loadListOfMapsOfFutureModels(Single.never())

        verify(mockObserver).onChanged(check {
            assertEquals(expected, it)
        })
    }

    //TODO test loadlistofmapsotfuturemodels for success case and test for correct/incorrect values

    @Test
    fun `assert loadListOfMapsOfFutureModels() sets futureModelMapListLiveData to arStateSuccess on success`() {

        val modelList = arrayListOf<Model>()
        modelList.add(Model("test","abc","image1"))

        val expected = ArState.Success.OnFutureModelMapListLoaded::class

        val mockObserver = this.createObserver()

        model.getFutureModelMapListLiveData().observeForever(mockObserver)

        val inOrder = inOrder(mockObserver)

        model.loadListOfMapsOfFutureModels(Single.just(modelList))

        inOrder.verify(mockObserver).onChanged(ArState.Loading)
        inOrder.verify(mockObserver).onChanged(check {
            assertEquals(expected, it::class)
        })
    }

    @Test
    fun `assert loadListOfMapsOfFutureModels() sets futureModelMapListLiveData to arStateError on error`() {
        val expected = ArState.Error

        val mockObserver = this.createObserver()

        model.getFutureModelMapListLiveData().observeForever(mockObserver)

        val inOrder = inOrder(mockObserver)

        model.loadListOfMapsOfFutureModels(Single.error(Throwable()))

        inOrder.verify(mockObserver).onChanged(ArState.Loading)
        inOrder.verify(mockObserver).onChanged(check {
            assertEquals(expected, it)
        })
    }

    @Test
    fun `assert loadMapOfFutureLetters() sets futureModelMapListLiveData to arStateLoading on call before complete`() {
        val expected = ArState.Loading

        val mockObserver = this.createObserver()

        model.getFutureLetterMapLiveData().observeForever(mockObserver)

        model.loadMapOfFutureLetters(Observable.never())

        verify(mockObserver).onChanged(check {
            assertEquals(expected, it)
        })
    }

    @Test
    fun `assert loadMapOfFutureLetters() sets futureLetterMapLiveData to arStateSuccess on success`() {
        val expected = ArState.Success.OnFutureLetterMapLoaded::class

        val mapList = arrayListOf<MutableMap<String, CompletableFuture<ModelRenderable>>>()
        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()

        map["abc"] = mockFutureModel
        mapList.add(map)

        val mockObserver = this.createObserver()
        model.getFutureLetterMapLiveData().observeForever(mockObserver)
        val inOrder = inOrder(mockObserver)

        model.loadMapOfFutureLetters(Observable.just(mapList))

        inOrder.verify(mockObserver).onChanged(ArState.Loading)
        inOrder.verify(mockObserver).onChanged(check {
            assertEquals(expected, it::class)
        })
    }

    @Test
    fun `assert loadMapOfFutureLetters() sets futureLetterMapLiveData with correct values`() {
        val expected = arrayListOf("a", "b", "c", "d", "e", "f")

        val mapList = arrayListOf<MutableMap<String, CompletableFuture<ModelRenderable>>>()
        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        val map2 = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()

        map["abc"] = mockFutureModel
        mapList.add(map)

        map2["def"] = mockFutureModel
        mapList.add(map2)

        val mockObserver = this.createObserver()
        model.getFutureLetterMapLiveData().observeForever(mockObserver)
        val inOrder = inOrder(mockObserver)

        model.loadMapOfFutureLetters(Observable.just(mapList))

        inOrder.verify(mockObserver).onChanged(ArState.Loading)
        inOrder.verify(mockObserver).onChanged(check { state ->
            assertEquals(ArState.Success.OnFutureLetterMapLoaded::class.java, state.javaClass)
            val mapState: ArState.Success.OnFutureLetterMapLoaded = state as ArState.Success.OnFutureLetterMapLoaded
            val letterMap = mapState.futureLetterMap
            assertTrue(expected.all {
                letterMap.containsKey(it)
            })
        })
    }

    @Test
    fun `assert loadMapOfFutureLetters() sets futureModelMapListLiveData to arStateLoading on error`() {
        val expected = ArState.Error
      
        val mockObserver = this.createObserver()

        model.getFutureLetterMapLiveData().observeForever(mockObserver)

        val inOrder = inOrder(mockObserver)

        model.loadMapOfFutureLetters(Observable.error(Throwable()))

        inOrder.verify(mockObserver).onChanged(ArState.Loading)
        inOrder.verify(mockObserver).onChanged(check {
            assertEquals(expected, it)
        })
    }

    @Test
    fun `assert loadLetterRenderables() sets letterMapLiveData to arStateLoading on call before complete`() {
        val expected = ArState.Loading

        val mockObserver = this.createObserver()

        model.getLetterMapLiveData().observeForever(mockObserver)

        model.loadLetterRenderables(Observable.never())

        verify(mockObserver).onChanged(check {
            assertEquals(expected, it)
        })
    }

    //TODO test loadletterrenderables success case and test for correct/incorrect values

    @Test
    fun `assert loadLetterRenderables() sets futureLetterMapLiveData to arStateSuccess on success`() {
        val expected = ArState.Success.OnLetterMapLoaded::class

        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()

        map["a"] = mockFutureModel

        val mockObserver = this.createObserver()
        model.getLetterMapLiveData().observeForever(mockObserver)
        val inOrder = inOrder(mockObserver)

        model.loadLetterRenderables(Observable.just(map))

        inOrder.verify(mockObserver).onChanged(ArState.Loading)
        inOrder.verify(mockObserver).onChanged(check {
            assertEquals(expected, it::class)
        })
    }

    @Test
    fun `assert loadLetterRenderables() sets letterMapLiveData to arStateError on error`() {
        val expected = ArState.Error

        val mockObserver = this.createObserver()
        model.getLetterMapLiveData().observeForever(mockObserver)

        val inOrder = inOrder(mockObserver)

        model.loadLetterRenderables(Observable.error(Throwable()))

        inOrder.verify(mockObserver).onChanged(ArState.Loading)

        inOrder.verify(mockObserver).onChanged(check {
            assertEquals(expected, it)
        })
    }

    @Test
    fun `assert loadModelRenderables() sets modelMapListLiveData to arStateLoading on call before complete`() {
        val expected = ArState.Loading

        val mockObserver = this.createObserver()
        model.getModelMapListLiveData().observeForever(mockObserver)

        model.loadModelRenderables(Observable.never())

        verify(mockObserver).onChanged(check {
            assertEquals(expected, it)
        })
    }

    @Test
    fun `assert loadModelRenderables() sets modelMapListLiveData to arStateSuccess on success`() {
        val expected = ArState.Success.OnModelMapListLoaded::class

        val mapList = arrayListOf<MutableMap<String, CompletableFuture<ModelRenderable>>>()
        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()

        map["abc"] = mockFutureModel
        mapList.add(map)

        val mockObserver = this.createObserver()
        model.getModelMapListLiveData().observeForever(mockObserver)
        val inOrder = inOrder(mockObserver)

        model.loadModelRenderables(Observable.just(mapList))

        inOrder.verify(mockObserver).onChanged(ArState.Loading)
        inOrder.verify(mockObserver).onChanged(check { state ->
            assertEquals(expected, state::class)
        })
    }

    @Test
    fun `assert loadModelRenderables() sets modelMapListLiveData to arStateError on error`() {
        val expected = ArState.Error

        val mockObserver = this.createObserver()
        model.getModelMapListLiveData().observeForever(mockObserver)

        val inOrder = inOrder(mockObserver)

        model.loadModelRenderables(Observable.error(Throwable()))

        inOrder.verify(mockObserver).onChanged(ArState.Loading)
        inOrder.verify(mockObserver).onChanged(check {
            assertEquals(expected, it)
        })
    }
}