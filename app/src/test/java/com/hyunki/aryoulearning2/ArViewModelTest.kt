package com.hyunki.aryoulearning2

import android.app.Application
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.rules.CoroutineTestRule
import com.hyunki.aryoulearning2.rules.RxImmediateSchedulerRule
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArViewModel
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode
import java.lang.ClassCastException
import java.lang.Exception
import java.lang.NullPointerException
import java.util.concurrent.CompletableFuture
import kotlin.math.exp

//TODO refactor tests
//kotlinx.coroutines.test.TestCoroutineDispatcher.runBlockingTest
@ExperimentalCoroutinesApi
@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(AndroidJUnit4::class)
class ArViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @Rule
    @JvmField
    var testSchedulerRule = RxImmediateSchedulerRule()

    @Rule
    @JvmField
    val ruleForLivaData = InstantTaskExecutorRule()

    @Mock
    lateinit var repository: MainRepository

    private lateinit var model: ArViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val application = ApplicationProvider.getApplicationContext<Context>() as Application
        model = ArViewModel(application, repository, coroutinesTestRule.testDispatcherProvider)
    }

    private fun createObserver(): Observer<ArState> = spy(Observer { })

    @Test
    fun `assert getModelsFromRepositoryByCategory() emits arStateLoading on call before success`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val testCat = "testCategory"

        val data = listOf(Model("test1", testCat, "testImage"))

        whenever(repository.getModelsByCat(testCat))
                .thenReturn(data)

        val spyObserver = createObserver()

        model.getModelsFromRepositoryByCategory(testCat).observeForever(spyObserver)

        shadowOf(Looper.getMainLooper()).idle()

        val inOrder = inOrder(spyObserver)

        async { inOrder.verify(spyObserver).onChanged(ArState.Loading) }.await()

        async { inOrder.verify(spyObserver).onChanged(ArState.Success.OnModelsLoaded(data)) }.await()

    }

    @Test(expected = Exception::class)
    fun `assert getModelsFromRepositoryByCategory() emits arStateError on error`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val testCat = "testCategory"
        val error = Exception("")

        val spyObserver = createObserver()

        whenever(repository.getModelsByCat(testCat)).thenThrow(error)

        model.getModelsFromRepositoryByCategory(testCat).observeForever(spyObserver)
        val inOrder = inOrder(spyObserver)
        inOrder.verify(spyObserver).onChanged(ArState.Loading)
        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(ArState.Error::class.java, it::class.java)
        })

    }

//TODO makesure tests run all their lines

    @Test
    fun `assert getListOfMapsOfFutureModels() emits arStateLoading on call before success`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val testCat = "testCategory"
        val testInput = listOf(Model("test1", testCat, "testImage"))

        val spyObserver = createObserver()

        model.getListOfMapsOfFutureModels(testInput).observeForever(spyObserver)

        val inOrder = inOrder(spyObserver)
        inOrder.verify(spyObserver).onChanged(ArState.Loading)
        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(ArState.Success.OnFutureModelMapListLoaded::class.java, it::class.java)
        })
    }

////    //TODO test loadlistofmapsotfuturemodels for success case and test for correct/incorrect values

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `assert getListOfMapsOfFutureModels() emits arStateError on error`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val badTestInput = listOf(Any())

        val spyObserver = createObserver()

        model.getListOfMapsOfFutureModels(badTestInput as List<Model>).observeForever(spyObserver)

        val inOrder = inOrder(spyObserver)

        inOrder.verify(spyObserver).onChanged(ArState.Loading)
        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(ArState.Error::class.java, it::class.java)
        })
    }

    @Test
    fun `assert getMapOfFutureLetters() emits arStateLoading on call before success`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val mapList = arrayListOf<MutableMap<String, CompletableFuture<ModelRenderable>>>()
        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()

        map["abc"] = mockFutureModel
        mapList.add(map)

        val spyObserver = createObserver()
        model.getMapOfFutureLetters(mapList).observeForever(spyObserver)

        val inOrder = inOrder(spyObserver)
        inOrder.verify(spyObserver).onChanged(ArState.Loading)
        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(ArState.Success.OnFutureLetterMapLoaded::class.java, it::class.java)
        })
    }

    @Test
    fun `assert getMapOfFutureLetters() sets futureLetterMapLiveData with correct values`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val mapList = arrayListOf<MutableMap<String, CompletableFuture<ModelRenderable>>>()
        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        val map2 = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()

        map["abc"] = mockFutureModel
        mapList.add(map)
        map2["def"] = mockFutureModel
        mapList.add(map2)

        val expected = arrayListOf("a", "b", "c", "d", "e", "f")

        val spyObserver = createObserver()
        val inOrder = inOrder(spyObserver)

        model.getMapOfFutureLetters(mapList).observeForever(spyObserver)

        inOrder.verify(spyObserver).onChanged(ArState.Loading)
        inOrder.verify(spyObserver).onChanged(
                check { state ->
                    assertEquals(ArState.Success.OnFutureLetterMapLoaded::class.java, state.javaClass)

                    val mapState: ArState.Success.OnFutureLetterMapLoaded = state as ArState.Success.OnFutureLetterMapLoaded

                    val letterMap = mapState.futureLetterMap

                    assertTrue(expected.all {
                        letterMap.containsKey(it)
                    })
                })
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `assert getMapOfFutureLetters() emits arStateError on error`() = coroutinesTestRule.testDispatcher.runBlockingTest {

        val mapList = listOf(Any())

        val spyObserver = createObserver()

        model.getMapOfFutureLetters(mapList as List<Map<String, CompletableFuture<ModelRenderable>>>).observeForever(spyObserver)

        val inOrder = inOrder(spyObserver)

        inOrder.verify(spyObserver).onChanged(ArState.Loading)
        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(ArState.Error::class.java, it::class.java)
        })
    }

    @Test
    fun `assert getLetterRenderables() emits arStateLoading on call before success`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()
        map["a"] = mockFutureModel

        val spyObserver = createObserver()

        model.getLetterRenderables(map).observeForever(spyObserver)

        val inOrder = inOrder(spyObserver)

        inOrder.verify(spyObserver).onChanged(ArState.Loading)
        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(ArState.Success.OnLetterMapLoaded::class.java, it::class.java)
        })
    }

    //TODO test loadletterrenderables success case and test for correct/incorrect values

    @Suppress("UNCHECKED_CAST")
    @Test(expected = ClassCastException::class)
    fun `assert getLetterRenderables() emits arStateError on error`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val badValueMap = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        badValueMap["a"] = Any() as CompletableFuture<ModelRenderable>

        val spyObserver = createObserver()
        try {
            model.getLetterRenderables(badValueMap).observeForever(spyObserver)
        } finally {
            val inOrder = inOrder(spyObserver)
            inOrder.verify(spyObserver).onChanged(ArState.Loading)
            inOrder.verify(spyObserver).onChanged(check {
                assertEquals(ArState.Error::class.java, it::class.java)
            })
        }
    }

    @Test
    fun `assert getModelRenderables() emits arStateLoading on call before success`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val mapList = arrayListOf<MutableMap<String, CompletableFuture<ModelRenderable>>>()
        val map = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        val mockFutureModel = mock<CompletableFuture<ModelRenderable>>()

        map["abc"] = mockFutureModel
        mapList.add(map)

        val spyObserver = createObserver()
        try {
            model.getModelRenderables(mapList).observeForever(spyObserver)
        } finally {
            val inOrder = inOrder(spyObserver)
            inOrder.verify(spyObserver).onChanged(ArState.Loading)
            inOrder.verify(spyObserver).onChanged(check {
                assertEquals(ArState.Success.OnModelMapListLoaded::class.java, it::class.java)
            })
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test(expected = ClassCastException::class)
    fun `assert getModelRenderables() emits arStateError on error`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val badList = Any()

        val spyObserver = createObserver()

        model.getModelRenderables(badList as List<Map<String, CompletableFuture<ModelRenderable>>>).observeForever(spyObserver)

        val inOrder = inOrder(spyObserver)
        inOrder.verify(spyObserver).onChanged(ArState.Loading)
        inOrder.verify(spyObserver).onChanged(check {
            assertEquals(ArState.Error::class.java, it::class.java)
        })
    }
}