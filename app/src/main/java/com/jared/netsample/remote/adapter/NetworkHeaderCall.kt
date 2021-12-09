package com.jared.netsample.remote.adapter


import com.jared.netsample.remote.enity.MyHeader
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class NetworkHeaderCall<S>(
    private val delegate: Call<S>
) : Call<MyHeader<S>> {

    override fun enqueue(callback: Callback<MyHeader<S>>) {
        return delegate.enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val body = response.body()
                //看header欄位的名稱
                val date = response.headers()["DATE"]?:""
                if(response.isSuccessful){
                   callback.onResponse(this@NetworkHeaderCall, Response.success(
                       MyHeader<S>(body!!,
                           date
                       )
                   ))
                }else{
                   callback.onResponse(this@NetworkHeaderCall, Response.error(response.code(),response.errorBody()!!))
                }

            }

            override fun onFailure(call: Call<S>, throwable: Throwable) {

                callback.onFailure(this@NetworkHeaderCall,throwable)
            }
        })
    }

    override fun isExecuted() = delegate.isExecuted

    override fun clone() = NetworkHeaderCall(delegate.clone())

    override fun isCanceled() = delegate.isCanceled

    override fun cancel() = delegate.cancel()

    override fun execute(): Response<MyHeader<S>> {
       val res= this@NetworkHeaderCall.delegate.execute()
       val body = res.body()
       val date = res.headers()["DATE"]?:""
       return if(res.isSuccessful){
           Response.success(
               MyHeader<S>(
                   body!!,
                   date
               )
           )
       }else{
           Response.error(res.code(),res.errorBody()!!)
       }
    }

    override fun request(): Request = delegate.request()
    override fun timeout(): Timeout =delegate.timeout()
}