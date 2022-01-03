package com.jared.netsample.sample

import com.jared.netsample.remote.converter.ann.ArrayError
import com.jared.netsample.remote.converter.ann.ObjectError
import com.jared.netsample.remote.converter.ann.PrimitiveError
import com.jared.netsample.remote.enity.MyHeader
import com.jared.netsample.remote.enity.Nullable
import retrofit2.http.GET

//retrofit
interface Api {


    //如果server 回傳的data key是原始class(Int、String、Boolean之類的)也可以直接解析
    @GET("int")
    suspend fun getInt():Int


    //如果server 回傳的data key是原始class(Int、String、Boolean之類的)，並且有可能是null 或 沒有data 這個key
    //要在外面包一個Nullable<T>
    @GET("int/nullable")
    suspend fun getIntNullable(): Nullable<Int>

    //如果server 回傳的data key 並且有可能是null 或 沒有data 這個key
    //要在外面包一個Nullable<T>
    //jsonArray同理 ex:Nullable<List<Test>>
    @GET("json/nullable")
    suspend fun getJsonObjectNullable():Nullable<Test>

    //注意:
    //如果server回傳的data key 是 null 或是 沒有data key 而又沒有加上Nullable<T>的話
    //會報 解析錯誤-0x002

    //如果要用MyHeader的話 要包在最外層
    @GET("array/nullable/header")
    suspend fun getJsonArrayNullableWithHeader():MyHeader<Nullable<List<Test>>>


    //如果server在error時會傳errors 這個key的話，要加上key的class
    /**
     * @see PrimitiveError 原生class(Int、String、Boolean之類的)，不需要帶是什麼類型
     * @see ArrayError 如果errors 是 JsonArray的話請帶入List裡面的類型 範例在下面
     * @see ObjectError  如果errors 是 JsonObject的話請帶入類型 範例在下面
     */

    /***
     *error data 部分 ↓
     */

    //原始類型只需要標上註解，會自動解析
    @PrimitiveError(Float::class)
    @GET("error/pri")
    suspend fun getPrimitiveError(): MyHeader<Nullable<ArrayList<Test>>>

    @ObjectError(Test::class)
    @GET("error/obj")
    suspend fun getObjectError():MyHeader<Nullable<List<Test>>>

    //注意 這邊代表 回傳的errors是一個 List<Test>
    @ArrayError(Test::class)
    @GET("error/array")
    suspend fun getArrayError():MyHeader<Nullable<List<Test>>>


}