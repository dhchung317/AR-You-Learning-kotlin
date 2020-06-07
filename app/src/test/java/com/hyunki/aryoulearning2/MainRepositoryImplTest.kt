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
import com.hyunki.aryoulearning2.data.network.main.MainApi
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.apache.tools.ant.Main
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(AndroidJUnit4::class)
class MainRepositoryImplTest {

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    @get:Rule
    val expectedExceptionRule: ExpectedException = ExpectedException.none()

    private lateinit var db: ModelDatabase
//    private lateinit var spyCatDao: CategoryDao
//    private lateinit var spyModelDao: CategoryDao
//    private lateinit var mainApi: MainApi

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

    @After
    fun teardown() {
        db.close()
    }
}