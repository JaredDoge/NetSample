package com.jared.netsample.remote.adapter

import com.google.common.reflect.TypeToken
import com.jared.netsample.remote.enity.MyHeader
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.GET
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


class NetworkHeaderAdapterFactoryTest {

    private val ann = arrayOfNulls<Annotation>(0)
    private val factory: CallAdapter.Factory = NetworkHeaderAdapterFactory()
    private lateinit var retrofit: Retrofit

    @Before
    fun setUp() {
        retrofit = Retrofit.Builder()
                .baseUrl("http://localhost:1")
                .addCallAdapterFactory(factory)
                .build()
    }

    @Test
    fun typeReturnNull() {
        val stringClz = factory.get(String::class.java, ann, retrofit)

        assertThat(stringClz).isNull()


        val adapterType = object : TypeToken<Call<String>>() {}.type

        val callClz = factory.get(adapterType, ann, retrofit)

        assertThat(callClz).isNull()
    }

    @Test
    fun responseTypes() {

        val st = object : TypeToken<Call<MyHeader<String>>>() {}.type

        val sClz = factory.get(st, ann, retrofit)?.responseType()

        assertThat(sClz).isEqualTo(String::class.java)


        val lt = object : TypeToken<Call<MyHeader<List<String>>>>() {}.type

        val lClz = factory.get(lt, ann, retrofit)?.responseType()

        assertThat(lClz).isEqualTo(object : TypeToken<List<String>>() {}.type)


    }

    @Test
    fun header() {
        val server = MockWebServer()
        server.enqueue(MockResponse()
                .setResponseCode(200)
                .setHeader("DATE", "2021-10-10 10:10:10")
                .setBody("{}"))
        val api = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addCallAdapterFactory(factory)
                .addConverterFactory(StringConverterFactory())
                .build()
                .create(Api::class.java)

        runBlocking {
           val h= api.headerDate()

           assertThat(h.serverDate).isEqualTo("2021-10-10 10:10:10")

        }



    }



    interface Api{
       @GET("/")
       suspend fun headerDate():MyHeader<String>
    }

    internal class StringConverterFactory : Converter.Factory() {
        override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<ResponseBody, String> {
            return Converter<ResponseBody, String> { it.string() }
        }

        override fun requestBodyConverter(type: Type, parameterAnnotations: Array<out Annotation>, methodAnnotations: Array<out Annotation>, retrofit: Retrofit): Converter<String, RequestBody> {
            return Converter<String, RequestBody> {it.toRequestBody("text/plain".toMediaType())}
        }
    }



}