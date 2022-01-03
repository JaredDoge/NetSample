package com.jared.netsample.remote.converter

import com.jared.netsample.remote.ServerException
import com.jared.netsample.remote.adapter.NetworkHeaderAdapterFactory
import com.jared.netsample.remote.converter.ann.ArrayError
import com.jared.netsample.remote.converter.ann.ObjectError
import com.jared.netsample.remote.converter.ann.PrimitiveError
import com.jared.netsample.remote.enity.MyHeader
import com.jared.netsample.remote.enity.Nullable
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import okhttp3.internal.tls.OkHostnameVerifier.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.http.GET

class MoshiResponseBodyConverterTest {

    @JsonClass(generateAdapter = true)
    data class Msg(
            @Json(name = "msg")
            val msg: String,
    )

    @JsonClass(generateAdapter = true)
    data class ErrorData(
            @Json(name = "msg")
            val msg: String,
    )


    //api
    interface Api {

        @GET("/")
        suspend fun getInt(): Int

        @GET("/")
        suspend fun getIntNullable(): Nullable<Int>

        @GET("/")
        suspend fun getJsonObject(): MyHeader<Msg>

        @GET("/")
        suspend fun getJsonObjectNullable(): Nullable<Msg>

        @GET("/")
        suspend fun getJsonArray(): List<Msg>

        @GET("/")
        suspend fun getJsonArrayNullable(): Nullable<List<Msg>>

        @PrimitiveError(String::class)
        @GET("/")
        suspend fun getPrimitiveError(): Msg

        @ObjectError(Msg::class)
        @GET("/")
        suspend fun getObjectError(): Msg


        @ArrayError(Msg::class)
        @GET("/")
        suspend fun getArrayError(): Msg

    }

    private val server: MockWebServer = MockWebServer()

    lateinit var api: Api


    @Before
    fun setUp() {
        api = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addCallAdapterFactory(NetworkHeaderAdapterFactory())
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(Api::class.java)
    }


    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testInt() {

        runBlocking {

            server.enqueue(MockResponse().setResponseCode(200).setBody("""
                {
                   "status":"0x000",
                   "message":"success",
                   "data":99
                }
            """.trimIndent()))
            val i = api.getInt()

            assertEquals(i, 99)

        }
    }

    @Test
    fun testIntNullable() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""
                {
                   "status":"0x000",
                   "message":"success"
                }
            """.trimIndent()))

            val nullable = api.getIntNullable()

            assertNull(nullable.data)
        }
    }


    @Test
    fun testJsonObject() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""
                {
                   "status":"0x000",
                   "message":"success",
                   "data":{
                      "msg":"msg"
                   }
                }
            """.trimIndent()))
            val s=api.getJsonObject()

           // assertEquals(s.msg,"msg")
        }
    }

    @Test
    fun testJsonObjectNullable(){
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""
                {
                   "status":"0x000",
                   "message":"success"
                }
            """.trimIndent()))
            val s=api.getJsonObjectNullable()

            assertNull(s.data)
        }
    }


    @Test
    fun testJsonArray(){
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""
                {
                   "status":"0x000",
                   "message":"success",
                   "data":[
                      {
                         "msg":"msg1"
                      },
                      {
                         "msg":"msg2"
                      }
                   ]
                }
            """.trimIndent()))
            val s=api.getJsonArray()

            assertEquals(s[0].msg,"msg1")
            assertEquals(s[1].msg,"msg2")
        }
    }

    @Test
    fun testJsonArrayNullable(){
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""
                {
                   "status":"0x000",
                   "message":"success"
                }
            """.trimIndent()))
            val s=api.getJsonArrayNullable()

            assertNull(s.data)
        }
    }

    @Test
    fun testPrimitiveError(){
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""
                {
                   "status":"0x129",
                   "message":"failure",
                   "errors":"errorMsg1"              
                }
            """.trimIndent()))
            runCatching {

                api.getPrimitiveError()
            }.onFailure {

                assertTrue("it is not ServerException",it is ServerException)
                val e=it as ServerException
                assertEquals(e.errorData,"errorMsg1")
            }

        }
    }



    @Test
    fun tesObjectError(){
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""
                {
                   "status":"0x129",
                   "message":"failure",
                   "errors":{
                      "msg":"errorMsg"
                   }
                }
            """.trimIndent()))
            runCatching {

                api.getObjectError()
            }.onFailure {

                assertTrue("it is not ServerException",it is ServerException)
                val e=it as ServerException
                assertTrue("errors is not Msg.class",e.errorData is Msg)
                val m=e.errorData as Msg
                assertEquals(m.msg,"errorMsg")
            }

        }
    }

    @Test
    fun tesArrayError(){
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""
                {
                   "status":"0x129",
                   "message":"failure",
                   "errors":[
                      {
                         "msg":"errorMsg1"
                      },
                      {
                         "msg":"errorMsg2"
                      }
                   ]
                }
            """.trimIndent()))
            runCatching {

                api.getArrayError()
            }.onFailure {

                assertTrue("it is not ServerException , is $it",it is ServerException)
                val e=it as ServerException
                assertEquals(e.errorData, listOf(Msg("errorMsg1"),Msg("errorMsg2")))

            }

        }
    }




}