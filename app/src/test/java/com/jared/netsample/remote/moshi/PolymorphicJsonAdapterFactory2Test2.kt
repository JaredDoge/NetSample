package com.jared.netsample.remote.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail
import org.junit.Test


class PolymorphicJsonAdapterFactory2Test2 {

    @Test
    fun fromJsonInt() {
        val moshi = Moshi.Builder().add(
            PolymorphicJsonAdapterFactory2.intTypeOf(IntBase::class.java, "type")
                .withSubtype(IntBase.Data::class.java, 1)
                .withSubtype(IntBase.Data2::class.java, 2)
        ).build()


        val jsonAdapter:JsonAdapter<IntBase> = moshi.adapter(IntBase::class.java)

        assertThat(
            jsonAdapter.fromJson(
                """
             {
                  "type":1,
                  "data":"1"
             }
        """.trimIndent()
            )
        ).isEqualTo(IntBase.Data(1, 1))
        assertThat(
            jsonAdapter.fromJson(
                """
             {
                  "type":2,
                  "data2":true
             }
        """.trimIndent()
            )
        ).isEqualTo(IntBase.Data2(2, true))
    }

    @Test
    fun fromJsonBoolean() {
        val moshi = Moshi.Builder().add(
            PolymorphicJsonAdapterFactory2.booleanTypeOf(BooleanBase::class.java, "type")
                .withSubtype(BooleanBase.Data::class.java, true)
                .withSubtype(BooleanBase.Data2::class.java, false)
        ).build()


        val jsonAdapter:JsonAdapter<BooleanBase> = moshi.adapter(BooleanBase::class.java)


        assertThat(
            jsonAdapter.fromJson(
                """
             {
                  "type":true,
                  "data":"1"
             }
        """.trimIndent()
            )
        ).isEqualTo(BooleanBase.Data(true, 1))
        assertThat(
            jsonAdapter.fromJson(
                """
             {
                  "type":false,
                  "data2":"2"
             }
        """.trimIndent()
            )
        ).isEqualTo(BooleanBase.Data2(false, "2"))

    }

    @Test
    fun fromJsonString() {
        val moshi = Moshi.Builder().add(
            PolymorphicJsonAdapterFactory2.stringTypeOf(StringBase::class.java, "type")
                .withSubtype(StringBase.Data::class.java, "type1")
                .withSubtype(StringBase.Data2::class.java, "type2")
        ).build()


        val jsonAdapter:JsonAdapter<StringBase> = moshi.adapter(StringBase::class.java)


        assertThat(
            jsonAdapter.fromJson(
                """
             {
                  "type":"type1",
                  "data":"1"
             }
        """.trimIndent()
            )
        ).isEqualTo(StringBase.Data("type1", 1))
        assertThat(
            jsonAdapter.fromJson(
                """
             {
                  "type":"type2",
                  "data2":false
             }
        """.trimIndent()
            )
        ).isEqualTo(StringBase.Data2("type2", false))

    }

    @Test
    fun fromJsonDouble() {
        val moshi = Moshi.Builder().add(
            PolymorphicJsonAdapterFactory2.doubleTypeOf(DoubleBase::class.java, "type")
                .withSubtype(DoubleBase.Data::class.java, 1.0)
                .withSubtype(DoubleBase.Data2::class.java, 2.0)
        ).build()


        val jsonAdapter:JsonAdapter<DoubleBase> = moshi.adapter(DoubleBase::class.java)


        assertThat(
            jsonAdapter.fromJson(
                """
             {
                  "type":1.0,
                  "data":"1"
             }
        """.trimIndent()
            )
        ).isEqualTo(DoubleBase.Data(1.0, 1))
        assertThat(
            jsonAdapter.fromJson(
                """
             {
                  "type":2.0,
                  "data2":false
             }
        """.trimIndent()
            )
        ).isEqualTo(DoubleBase.Data2(2.0, false))

    }

    @Test
    fun unregisteredLabelValue() {
        val moshi = Moshi.Builder().add(
            PolymorphicJsonAdapterFactory2.stringTypeOf(StringBase::class.java, "type")
                .withSubtype(StringBase.Data::class.java, "type1")
                .withSubtype(StringBase.Data2::class.java, "type2")
        ).build()


        val adapter: JsonAdapter<StringBase> = moshi.adapter(StringBase::class.java)
        val reader: JsonReader =
            JsonReader.of(
                Buffer().writeUtf8(
                    """
             {
                  "type":"type3",
                  "data":"1"
             }
        """.trimIndent()
                )
            )
        try {
            adapter.fromJson(reader)
            fail()
        } catch (expected: JsonDataException) {
            assertThat(expected)
                .hasMessage(
                    "Expected one of [type1, type2] for key 'type' but found"
                            + " 'type3'. Register a subtype for this label."
                )
        }
        assertThat(reader.peek()).isEqualTo(JsonReader.Token.BEGIN_OBJECT)
    }


    @Test
    fun specifiedFallbackSubtype() {

        val def= StringBase.Data("default",-1)
        val moshi = Moshi.Builder().add(
            PolymorphicJsonAdapterFactory2.stringTypeOf(StringBase::class.java, "type")
                .withSubtype(StringBase.Data::class.java, "type1")
                .withSubtype(StringBase.Data2::class.java, "type2")
                .withDefaultValue(def)
        ).build()

        val json="""
            {
                "type":"type3",
                "data":"1"
            }
            """.trimIndent()
        val adapter= moshi.adapter(StringBase::class.java)

        val stringBase=adapter.fromJson(json)
        assertThat(stringBase).isSameAs(def)

    }

    @Test
    fun specifiedNullFallbackSubtype() {

        val moshi = Moshi.Builder().add(
            PolymorphicJsonAdapterFactory2.stringTypeOf(StringBase::class.java, "type")
                .withSubtype(StringBase.Data::class.java, "type1")
                .withSubtype(StringBase.Data2::class.java, "type2")
                .withDefaultValue(null)
        ).build()

        val json="""
            {
                "type":"type3",
                "data":"1"
            }
            """.trimIndent()
        val adapter= moshi.adapter(StringBase::class.java)

        val stringBase=adapter.fromJson(json)
        assertThat(stringBase).isNull()

    }
}