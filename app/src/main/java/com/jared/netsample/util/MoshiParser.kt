package com.jared.netsample.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi


object MoshiParser {


    private val moshi=
        Moshi.Builder()
            .build()


    fun moshi():Moshi = moshi

    inline fun <reified T> fromJson(json: String): T {
        val t=object : TypeToken<T>(){}.type
        val adapter: JsonAdapter<T> = moshi().adapter(t)
        return adapter.fromJson(json) as T
    }


    inline fun <reified T> toJson(clazz: T): String

        = moshi().adapter(T::class.java).toJson(clazz)




}