package com.jared.netsample.remote.converter

import com.jared.netsample.remote.ServerException
import com.squareup.moshi.*
import com.squareup.moshi.internal.Util
import com.jared.netsample.remote.converter.ann.ArrayError
import com.jared.netsample.remote.converter.ann.ObjectError
import com.jared.netsample.remote.converter.ann.PrimitiveError
import com.jared.netsample.remote.enity.Empty
import com.jared.netsample.remote.enity.Nullable
import com.jared.netsample.util.TypeUtil
import okhttp3.ResponseBody
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import retrofit2.Converter
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/***
 * 用moshi解析json以下範例適用於回傳格式為
 * success:
 * {
 *      "status": "0x000",
 *      "message": "請求成功",
 *      "data": [
 *          {
 *              "id": 8,
 *                "name": "台北市",
 *          },
 *          {
 *              "id": 5,
 *                "name": "台中市",
 *          },
 *      ]
 *  }
 *
 * failure:
 * {
 *      "status": "1x052",
 *      "message": "請求失敗，",
 *      "errors":{
 *         "id":5
 *      }
 *  }
 *
 *
 *
 */
@Suppress("UNCHECKED_CAST")
internal class MoshiResponseBodyConverter<T>(
    private val moshi: Moshi,
    private val type: Type,
    private val ann: Array<Annotation>,
    private val lenient: Boolean,
    private val failOnUnknown: Boolean,
    private val serializeNulls: Boolean,
) :
    Converter<ResponseBody, T?> {

    private val names = JsonReader.Options.of("status", "data", "errors","message")

    private val errorAnn = listOf(
        PrimitiveError::class,
        ObjectError::class,
        ArrayError::class
    )


    companion object {
        private val UTF8_BOM: ByteString = "EFBBBF".decodeHex()

    }


    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T? {

        return value.source().use {


            // Moshi has no document-level API so the responsibility of BOM skipping falls to whatever
            // is delegating to it. Since it's a UTF-8-only library as well we only honor the UTF-8 BOM.
            if (it.rangeEquals(0, UTF8_BOM)) {
                it.skip(UTF8_BOM.size.toLong())
            }
            val reader = JsonReader.of(it)


            // 解析 status 欄位
            // peek() 用於獲得一個新的 JsonReader 以便重複解析。
            // 因為我們無法確定 status 和 data 哪一個會先讀取到，因此需要利用 peek 先單獨取出 status,
            // 然後再開始正式讀取。
            val peek=reader.peekJson()
            peek.beginObject()

            var status:String?=null
            var message:String?=null
            while (peek.hasNext()) {
                when (peek.selectName(names)) {
                    3 -> {
                        //message
                        message=peek.nextString()
                    }
                    0->{
                        status=peek.nextString()
                    }

                    //在names以外的
                    -1 -> {
                        peek.skipName()
                        peek.skipValue()
                    }
                    //除了目標index以外的
                    else -> peek.skipValue()
                }
            }
            peek.endObject()
            if(status==null)throw JsonDataException("Non-null value 'status' was null at ${reader.path}")
            if(message==null)throw JsonDataException("Non-null value 'message' was null at ${reader.path}")


           val clz = TypeUtil.getRawType(type)

           var result:T?

           if(status=="0x000"){
                when (clz) {
                    //Empty 代表不理會data
                    Empty::class.java -> {
                        result = Empty as T
                    }
                    //代表data可能是null 或是 連data key 都沒有
                    Nullable::class.java -> {

                        reader.beginObject()
                        if (type !is ParameterizedType) {
                            throw IllegalArgumentException("Expected a Class, ParameterizedType, or "
                                    + "GenericArrayType, but <" + type + "> is of type " + type.javaClass.name)
                        } else {
                            val data: Any? =
                                parse(reader, 1, TypeUtil.getParameterUpperBound(0, type))
                            result = Nullable(data) as T
                        }

                        reader.endObject()
                    }

                    else -> {

                        reader.beginObject()
                        val data:Any= parse(reader, 1, type)
                            ?: throw JsonDataException("'data' is not Nullable<> but found null value")

                        result=data as T

                        reader.endObject()
                    }
                }

            }else{

                val errorData= findErrorAnn()?.run {
                    reader.beginObject()
                    var e:Any?=null
                    when (this) {
                       is PrimitiveError -> e = parse(reader, 2,clazz.java)
                       is ObjectError -> e = parse(reader,2, clazz.java)
                       is ArrayError -> {
                            val t: Type = Types.newParameterizedType(MutableList::class.java,
                                clazz.java)
                            e=parse(reader,2,t)
                        }
                    }
                    reader.endObject()
                    e
                }
                throw ServerException(status,message,errorData)

            }



//            if (reader.peek() != JsonReader.Token.END_DOCUMENT) {
//                throw JsonDataException("JSON document was not fully consumed.")
//            }

            result
        }
    }

    private fun <T> parse(reader: JsonReader, index: Int, type: Type? = null):T?{

        var data:Any?=null
        while (reader.hasNext()) {
            when (reader.selectName(names)) {
                index -> {
                    data = when (val p = reader.peek()) {
                        JsonReader.Token.BEGIN_OBJECT, JsonReader.Token.BEGIN_ARRAY -> {
                            checkNotNull(type) {
                                "value is $p but type is null"
                            }
                            val adapter: JsonAdapter<out Any> = moshi.adapter(type,
                                Util.jsonAnnotations(ann))
                            if (lenient) adapter.lenient()
                            if (failOnUnknown) adapter.failOnUnknown()
                            if (serializeNulls) adapter.serializeNulls()
                            adapter.fromJson(reader)
                        }
                        JsonReader.Token.STRING -> reader.nextString()
                        JsonReader.Token.NUMBER -> {
                         val d=reader.nextDouble()
                         when(type){
                             Double::class.java->d
                             Long::class.java->d.toLong()
                             Int::class.java->d.toInt()
                             Float::class.java->d.toFloat()
                             Short::class.java->d.toInt().toShort()
                             else -> d
                         }
                        }
                        JsonReader.Token.BOOLEAN -> reader.nextBoolean()
                        JsonReader.Token.NULL -> reader.nextNull()
                        else -> {
                            throw IllegalStateException(
                                "Expected a value but was " + reader.peek() + " at path " + reader.path)
                        }
                    }
                }
                //在names以外的
                -1 -> {
                    reader.skipName()
                    reader.skipValue()
                }
                //除了目標index以外的
                else -> reader.skipValue()
            }
        }
        return data as T
    }

    private fun findErrorAnn():Annotation?{
        val count= ann.count {
            errorAnn.contains(it.annotationClass)
        }
        if(count>1){
            throw IllegalArgumentException("Can only set a single annotation 'ObjectError','ArrayError' or 'PrimitiveError'")
        }
        return ann.find {
            errorAnn.contains(it.annotationClass)}
    }


}