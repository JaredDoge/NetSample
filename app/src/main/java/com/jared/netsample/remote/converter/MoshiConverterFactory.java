package com.jared.netsample.remote.converter;


import static com.squareup.moshi.internal.Util.jsonAnnotations;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonQualifier;
import com.squareup.moshi.Moshi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * A {@linkplain Converter.Factory converter} which uses Moshi for JSON.
 *
 * <p>Because Moshi is so flexible in the types it supports, this converter assumes that it can
 * handle all types. If you are mixing JSON serialization with something else (such as protocol
 * buffers), you must {@linkplain Retrofit.Builder#addConverterFactory(Converter.Factory) add this
 * instance} last to allow the other converters a chance to see their types.
 *
 * <p>Any {@link JsonQualifier @JsonQualifier}-annotated annotations on the parameter will be used
 * when looking up a request body converter and those on the method will be used when looking up a
 * response body converter.
 */
public final class MoshiConverterFactory extends Converter.Factory {
    /** Create an instance using a default {@link Moshi} instance for conversion. */
    public static MoshiConverterFactory create() {
        return create(new Moshi.Builder().build());
    }

    /** Create an instance using {@code moshi} for conversion. */
    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static MoshiConverterFactory create(Moshi moshi) {
        if (moshi == null) throw new NullPointerException("moshi == null");
        return new MoshiConverterFactory(moshi, false, false, false);
    }

    private final Moshi moshi;
    private final boolean lenient;
    private final boolean failOnUnknown;
    private final boolean serializeNulls;

    private MoshiConverterFactory(
            Moshi moshi, boolean lenient, boolean failOnUnknown, boolean serializeNulls) {
        this.moshi = moshi;
        this.lenient = lenient;
        this.failOnUnknown = failOnUnknown;
        this.serializeNulls = serializeNulls;
    }

    /** Return a new factory which uses {@linkplain JsonAdapter#lenient() lenient} adapters. */
    public MoshiConverterFactory asLenient() {
        return new MoshiConverterFactory(moshi, true, failOnUnknown, serializeNulls);
    }

    /** Return a new factory which uses {@link JsonAdapter#failOnUnknown()} adapters. */
    public MoshiConverterFactory failOnUnknown() {
        return new MoshiConverterFactory(moshi, lenient, true, serializeNulls);
    }

    /** Return a new factory which includes null values into the serialized JSON. */
    public MoshiConverterFactory withNullSerialization() {
        return new MoshiConverterFactory(moshi, lenient, failOnUnknown, true);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type, Annotation[] annotations, Retrofit retrofit) {

        return new MoshiResponseBodyConverter<>(moshi,type,annotations,lenient,failOnUnknown,serializeNulls);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            Type type,
            Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations,
            Retrofit retrofit) {
        JsonAdapter<?> adapter = moshi.adapter(type, jsonAnnotations(parameterAnnotations));
        if (lenient) {
            adapter = adapter.lenient();
        }
        if (failOnUnknown) {
            adapter = adapter.failOnUnknown();
        }
        if (serializeNulls) {
            adapter = adapter.serializeNulls();
        }
        return new MoshiRequestBodyConverter<>(adapter);
    }


}