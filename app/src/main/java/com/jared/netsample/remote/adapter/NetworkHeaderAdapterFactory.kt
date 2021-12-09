package com.jared.netsample.remote.adapter

import com.jared.netsample.remote.enity.MyHeader
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class NetworkHeaderAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {

        // suspend functions wrap the response type in `Call`
        if (Call::class.java != getRawType(returnType)) {
            return null
        }

        if(returnType !is ParameterizedType){
            return null
        }

        // get the response type inside the `Call` type
        val responseType = getParameterUpperBound(0, returnType)
        // if the response type is not ApiResponse then we can't handle this type, so we return null
        if (getRawType(responseType) != MyHeader::class.java) {
            return null
        }

        if(responseType !is ParameterizedType){
            return null
        }

        val successBodyType = getParameterUpperBound(0, responseType)

        return NetworkHeaderAdapter<Any>(successBodyType)
    }
}