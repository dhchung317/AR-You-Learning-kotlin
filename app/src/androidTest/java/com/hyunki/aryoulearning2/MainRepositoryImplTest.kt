package com.hyunki.aryoulearning2

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hyunki.aryoulearning2.data.MainRepositoryImpl
import com.hyunki.aryoulearning2.data.db.ModelDatabase
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.data.network.main.MainApi
import io.reactivex.Observable
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class MainRepositoryImplTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var db: ModelDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ModelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    // -------------------------------------------------------------
    // getAllCats()
    // -------------------------------------------------------------

    @Test
    fun getAllCats_returnsZero_whenDatabaseEmpty() {
        val repo = MainRepositoryImpl(
            db.modelDao(),     // ✅ real
            db.catDao(),       // ✅ real
            mock()             // api unused here
        )

        val result = repo.getAllCats().blockingGet().size
        assertEquals(0, result)
    }

    @Test
    fun getAllCats_returnsCorrectCount_whenDatabaseHasThreeItems() {
        db.catDao().insert(Category("category1", "image1"))
        db.catDao().insert(Category("category2", "image2"))
        db.catDao().insert(Category("category3", "image3"))

        val repo = MainRepositoryImpl(
            db.modelDao(),     // ✅ real
            db.catDao(),       // ✅ real
            mock()
        )

        val result = repo.getAllCats().blockingGet().size
        assertEquals(3, result)
    }

    // -------------------------------------------------------------
    // getModelResponses() (API behavior tests)
    // -------------------------------------------------------------

    @Test
    fun getModelResponses_returnsCorrectSize_whenApiReturnsTwoItems() {
        val api = mock<MainApi>()
        val response = arrayListOf(
            ModelResponse(arrayListOf(), "category1", "backgroundImage1"),
            ModelResponse(arrayListOf(), "category2", "backgroundImage2")
        )

        whenever(api.getModels()).thenReturn(Observable.just(response))

        val repo = MainRepositoryImpl(
            db.modelDao(),  // ✅ real (safe even if repo stores results)
            db.catDao(),    // ✅ real
            api
        )

        val result = repo.getModelResponses().blockingSingle().size
        assertEquals(2, result)
    }

    @Test
    fun getModelResponses_returnsZero_whenApiReturnsEmpty() {
        val api = mock<MainApi>()
        whenever(api.getModels()).thenReturn(Observable.just(arrayListOf()))

        val repo = MainRepositoryImpl(
            db.modelDao(),  // ✅ real
            db.catDao(),    // ✅ real
            api
        )

        val result = repo.getModelResponses().blockingSingle().size
        assertEquals(0, result)
    }

    // -------------------------------------------------------------
    // getModelsByCat()
    // -------------------------------------------------------------

    @Test
    fun getModelsByCat_returnsZero_whenEmpty() {
        val repo = MainRepositoryImpl(
            db.modelDao(),  // ✅ real
            db.catDao(),    // ✅ real
            mock()
        )

        val result = repo.getModelsByCat("anyCat").blockingGet().size
        assertEquals(0, result)
    }

    @Test
    fun getModelsByCat_returnsCorrectCount_forMatchingCategory() {
        db.modelDao().insert(Model("testCategory", "model1", "image1"))
        db.modelDao().insert(Model("testCategory", "model2", "image2"))
        db.modelDao().insert(Model("otherCategory", "model3", "image3"))

        val repo = MainRepositoryImpl(
            db.modelDao(),  // ✅ real
            db.catDao(),    // ✅ real
            mock()
        )

        val result = repo.getModelsByCat("testCategory").blockingGet().size
        assertEquals(2, result)
    }

    @Test
    fun getModelsByCat_returnsOnlyItemsMatchingCategory() {
        db.modelDao().insert(Model("testCategory", "model1", "image1"))
        db.modelDao().insert(Model("testCategory", "model2", "image2"))
        db.modelDao().insert(Model("other", "model3", "image3"))

        val repo = MainRepositoryImpl(
            db.modelDao(),  // ✅ real
            db.catDao(),    // ✅ real
            mock()
        )

        val models = repo.getModelsByCat("testCategory").blockingGet()
        models.forEach { assertEquals("testCategory", it.category) }
    }

    // -------------------------------------------------------------
    // insertCat() (interaction test with mocks)
    // -------------------------------------------------------------

    @Test
    fun insertCat_insertsExpectedItem() {
        val catDao = mock<com.hyunki.aryoulearning2.data.db.dao.CategoryDao>()
        val repo = MainRepositoryImpl(mock(), catDao, mock())

        val expected = Category("category1", "image1")
        repo.insertCat(expected)

        argumentCaptor<Category>().apply {
            verify(catDao).insert(capture())
            assertEquals(expected, firstValue)
        }
    }

    // -------------------------------------------------------------
    // insertModel() (interaction test with mocks)
    // -------------------------------------------------------------

    @Test
    fun insertModel_insertsExpectedItem() {
        val modelDao = mock<com.hyunki.aryoulearning2.data.db.dao.ModelDao>()
        val repo = MainRepositoryImpl(modelDao, mock(), mock())

        val expected = Model("category1", "model1", "image1")
        repo.insertModel(expected)

        argumentCaptor<Model>().apply {
            verify(modelDao).insert(capture())
            assertEquals(expected, firstValue)
        }
    }

    // -------------------------------------------------------------
    // clearEntireDatabase()
    // -------------------------------------------------------------

    @Test
    fun clearEntireDatabase_clearsDatabaseCompletely() {
        val repo = MainRepositoryImpl(
            db.modelDao(),  // ✅ real
            db.catDao(),    // ✅ real
            mock()
        )

        db.modelDao().insert(Model("category1", "model1", "image1"))
        db.catDao().insert(Category("category1", "image1"))

        assertNotNull(db.modelDao().getModelsByCat("category1"))
        assertNotNull(db.catDao().allCategories)

        assertEquals(1, db.modelDao().getModelsByCat("category1").blockingGet().size)
        assertEquals(1, db.catDao().allCategories.blockingGet().size)

        repo.clearEntireDatabase()

        assertEquals(0, db.modelDao().getModelsByCat("category1").blockingGet().size)
        assertEquals(0, db.catDao().allCategories.blockingGet().size)
    }

    @After
    fun teardown() {
        db.close()
    }
}