package com.jared.netsample.remote.moshi

import com.google.common.reflect.TypeToken
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

class MoshiArrayListJsonAdapterTest {


    val adapter = MoshiArrayListJsonAdapter.FACTORY

    lateinit var moshi: Moshi

    val t= object : TypeToken<ArrayList<Int>>() {}.type

    @Before
    fun setUp() {
        moshi = Moshi.Builder().add(adapter).build()

    }

    @Test
    fun fromJson() {
        val json = """
           [1,2,3,4,5]       
       """.trimIndent()
        val adapter: JsonAdapter<ArrayList<Int>> = moshi.adapter(t)

        val list = adapter.fromJson(json)

        assertThat(list is ArrayList).isTrue

        assertThat(list).isEqualTo(arrayListOf(1, 2, 3, 4, 5))

    }

    @Test
    fun toJson() {
        val list = arrayListOf(1, 2, 3, 4, 5)

        val adapter: JsonAdapter<ArrayList<Int>> = moshi.adapter(t)
        val json = adapter.toJson(list)

        assertThat(json).isEqualTo("[1,2,3,4,5]")


    }
}