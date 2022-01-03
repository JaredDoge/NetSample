package com.jared.netsample.sample

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


// ↓這個一定要加 加在每個data class上面
@JsonClass(generateAdapter = true)
data class Test(
    //↓這個也是，跟gson一樣
    @Json(name="test_string")
    val testString:String,
    @Json(name="test2")
    val test2:List<Test2>){

    // ↓這個一定要加 加在每個data class上面
    @JsonClass(generateAdapter = true)
    //如果test_int這個key server有可能傳null的話 ，請加上 " ? "
    //如果沒有標nullable server又傳null的話，會報錯
    data class Test2(
        @Json(name="test_int")
        val testInt:Int?
        )

}

//如果再而sealed class裡面的 data class 也要加 @JsonClass
//而sealed本身不用加
sealed class TestSeal{

    @JsonClass(generateAdapter = true)
    data class Test3(
        @Json(name="i")
        val i:Int
        ): TestSeal()

}
