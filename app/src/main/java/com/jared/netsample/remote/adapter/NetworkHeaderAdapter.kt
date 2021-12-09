package com.jared.netsample.remote.adapter

import com.jared.netsample.remote.enity.MyHeader
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class NetworkHeaderAdapter<S : Any>(
    private val type: Type
) : CallAdapter<S, Call<MyHeader<S>>> {

    override fun responseType(): Type = type

    override fun adapt(call: Call<S>): Call<MyHeader<S>> {
        return NetworkHeaderCall(call)
    }
}