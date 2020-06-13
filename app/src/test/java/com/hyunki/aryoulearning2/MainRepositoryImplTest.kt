package com.hyunki.aryoulearning2

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hyunki.aryoulearning2.data.MainRepositoryImpl
import com.hyunki.aryoulearning2.data.db.ModelDatabase
import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ModelDao
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.data.network.main.MainApi
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import org.apache.tools.ant.Main
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

//TODO remake tests for refactored database logic
@RunWith(AndroidJUnit4::class)
class MainRepositoryImplTest {

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    @get:Rule
    val expectedExceptionRule: ExpectedException = ExpectedException.none()

    private lateinit var db: ModelDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, ModelDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @Test
    fun `assert getAllCats() returns size zero when empty`() {
        val modelDao = mock<ModelDao>()
        val catDao = spy(db.catDao())
        val api = mock<MainApi>()
        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 0

        val actual = repo.getAllCats().blockingGet().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getAllCats() returns list of 3 categories when 3 categories populated`() {
        val modelDao = mock<ModelDao>()
        val catDao = spy(db.catDao())
        val api = mock<MainApi>()

        db.catDao().insert(Category("category1", "image1"))
        db.catDao().insert(Category("category2", "image2"))
        db.catDao().insert(Category("category3", "image3"))

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 3

        val actual = repo.getAllCats().blockingGet().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getModelResponses() returns size 2 when returned list has 2 items`() {
        val modelDao = mock<ModelDao>()
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val response = ArrayList<ModelResponse>()
        response.add(ModelResponse(arrayListOf(), "category1", "backgroundImage1"))
        response.add(ModelResponse(arrayListOf(), "category2", "backgroundImage2"))

        whenever(api.getModels())
                .thenReturn(Observable.just(response))

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 2

        val actual = repo.getModelResponses().blockingSingle().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getModelResponses() returns size zero when empty`() {
        val modelDao = mock<ModelDao>()
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val response = ArrayList<ModelResponse>()

        whenever(api.getModels())
                .thenReturn(Observable.just(response))

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 0

        val actual = repo.getModelResponses().blockingSingle().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getModelsByCat() returns size zero when empty`() {
        val modelDao = mock<ModelDao>()
        val catDao = spy(db.catDao())
        val api = mock<MainApi>()
        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 0

        val actual = repo.getAllCats().blockingGet().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getModelsByCat() returns list of 2 models when 2 models of matching model-category is populated`() {
        val modelDao = spy(db.modelDao())
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val testCategory = "testCategory"
        val notTestCategory = "notTestCategory"

        db.modelDao().insert(Model(testCategory, "testModel1", "image1"))
        db.modelDao().insert(Model(testCategory, "testModel2", "image2"))
        db.modelDao().insert(Model(notTestCategory, "testModel3", "image3"))

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = 2

        val actual = repo.getModelsByCat(testCategory).blockingGet().size

        assertEquals(expected, actual)
    }

    @Test
    fun `assert getModelsByCat() returns list of expected category items`() {
        val modelDao = spy(db.modelDao())
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val testCategory = "testCategory"
        val notTestCategory = "notTestCategory"

        db.modelDao().insert(Model(testCategory, "testModel1", "image1"))
        db.modelDao().insert(Model(testCategory, "testModel2", "image2"))
        db.modelDao().insert(Model(notTestCategory, "testModel3", "image3"))

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val expected = testCategory

        val models = repo.getModelsByCat(testCategory).blockingGet()

        for (m in models) {
            val actual = m.category
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `verify insertCat() inserts expected item`() {
        val modelDao = mock<ModelDao>()
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val expected = Category("category1", "image1")

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        repo.insertCat(expected)

        argumentCaptor<Category>()
                .apply {
                    verify(catDao).insert(capture())
                    assertEquals(expected, firstValue)
                }
    }

    @Test
    fun `verify insertModel() inserts expected item`() {
        val modelDao = mock<ModelDao>()
        val catDao = mock<CategoryDao>()
        val api = mock<MainApi>()

        val expected = Model("category1", "testModel1", "image1")

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        repo.insertModel(expected)

        argumentCaptor<Model>()
                .apply {
                    verify(modelDao).insert(capture())
                    assertEquals(expected, firstValue)
                }
    }

    @Test
    fun `verify clearEntireDatabase() runs dao deleteAll()`() {
        val modelDao = spy(db.modelDao())
        val catDao = spy(db.catDao())
        val api = mock<MainApi>()

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        repo.clearEntireDatabase()

        verify(catDao).deleteAll()
        verify(modelDao).deleteAll()
    }

    @Test
    fun `verify db is empty after adding items and calling clearEntireDatabase()`() {
        val modelDao = spy(db.modelDao())
        val catDao = spy(db.catDao())
        val api = mock<MainApi>()

        val repo = MainRepositoryImpl(modelDao, catDao, api)

        val testCategory = "category1"

        db.modelDao().insert(Model(testCategory, "testModel1", "image1"))
        db.catDao().insert(Category("category1", "image1"))

        assertNotNull(db.modelDao().getModelsByCat(testCategory))
        assertNotNull(db.catDao().allCategories)

        assertEquals(1, db.modelDao().getModelsByCat(testCategory).blockingGet().size)
        assertEquals(1, db.catDao().allCategories.blockingGet().size)

        repo.clearEntireDatabase()

        verify(catDao).deleteAll()
        verify(modelDao).deleteAll()

        val expected = 0
        assertEquals(expected, db.modelDao().getModelsByCat(testCategory).blockingGet().size)
        assertEquals(expected, db.catDao().allCategories.blockingGet().size)
    }


    @After
    fun teardown() {
        db.close()
    }
}