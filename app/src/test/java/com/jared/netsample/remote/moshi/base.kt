package com.jared.netsample.remote.moshi

import com.squareup.moshi.JsonClass


sealed class IntBase(open val type:Int){

    @JsonClass(generateAdapter = true)
    data class Data(override val type: Int, val data:Int):IntBase(type)

    @JsonClass(generateAdapter = true)
    data class Data2(override val type: Int, val data2:Boolean):IntBase(type)
}

sealed class BooleanBase(open val type:Boolean){

    @JsonClass(generateAdapter = true)
    data class Data(override val type: Boolean, val data:Int):BooleanBase(type)

    @JsonClass(generateAdapter = true)
    data class Data2(override val type: Boolean, val data2:String):BooleanBase(type)
}

sealed class StringBase(open val type:String){

    @JsonClass(generateAdapter = true)
    data class Data(override val type: String, val data:Int):StringBase(type)

    @JsonClass(generateAdapter = true)
    data class Data2(override val type: String, val data2:Boolean):StringBase(type)
}

sealed class DoubleBase(open val type:Double){

    @JsonClass(generateAdapter = true)
    data class Data(override val type: Double, val data:Int):DoubleBase(type)

    @JsonClass(generateAdapter = true)
    data class Data2(override val type: Double, val data2:Boolean):DoubleBase(type)
}
