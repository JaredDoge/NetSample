package com.jared.netsample.remote.moshi;

import androidx.annotation.Nullable;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 擴充PolymorphicJsonAdapterFactory
 * 原本的PolymorphicJsonAdapterFactory只支援string 當type
 * 這裡支援int,string,boolean,double
 * sample:
 *  Moshi.Builder()
 *        .add(PolymorphicJsonAdapterFactory2.intTypeOf(Parent::class.java,"type")
          .withSubtype(Child::class.java, 1)
          .withSubtype(Child::class.java, 2)
 * @param <T>
 * @param <V>
 */

public final class PolymorphicJsonAdapterFactory2<T, V> implements JsonAdapter.Factory {
    final Class<T> baseType;
    final Class<V> labelType;
    final String labelKey;
    final List<V> labels;
    final List<Type> subtypes;


    final JsonAdapter<Object> fallbackJsonAdapter;

  private PolymorphicJsonAdapterFactory2(
            Class<T> baseType,
            Class<V> labelType,
            String labelKey,
            List<V> labels,
            List<Type> subtypes,
            @Nullable JsonAdapter<Object> fallbackJsonAdapter) {
        this.baseType = baseType;
        this.labelKey = labelKey;
        this.labelType = labelType;
        this.labels = labels;
        this.subtypes = subtypes;
        this.fallbackJsonAdapter = fallbackJsonAdapter;

    }

    /**
     * @param baseType The base type for which this factory will create adapters. Cannot be Object.
     * @param labelKey The key in the JSON object whose value determines the type to which to map the
     * @return
     */
    public static <T> PolymorphicJsonAdapterFactory2<T, Integer> intTypeOf(Class<T> baseType, String labelKey) {
        if (baseType == null) throw new NullPointerException("baseType == null");
        if (labelKey == null) throw new NullPointerException("labelKey == null");
        return new PolymorphicJsonAdapterFactory2<>(
                baseType, Integer.class, labelKey,Collections.emptyList(),Collections.emptyList(), null);
    }
    /**
     * @param baseType The base type for which this factory will create adapters. Cannot be Object.
     * @param labelKey The key in the JSON object whose value determines the type to which to map the
     * @return
     */

    public static <T> PolymorphicJsonAdapterFactory2<T, String> stringTypeOf(Class<T> baseType, String labelKey) {
        if (baseType == null) throw new NullPointerException("baseType == null");
        if (labelKey == null) throw new NullPointerException("labelKey == null");
        return new PolymorphicJsonAdapterFactory2<>(
                baseType, String.class, labelKey, Collections.emptyList(),Collections.emptyList(), null);
    }
    /**
     * @param baseType The base type for which this factory will create adapters. Cannot be Object.
     * @param labelKey The key in the JSON object whose value determines the type to which to map the
     * @return
     */
    public static <T> PolymorphicJsonAdapterFactory2<T, Boolean> booleanTypeOf(Class<T> baseType, String labelKey) {
        if (baseType == null) throw new NullPointerException("baseType == null");
        if (labelKey == null) throw new NullPointerException("labelKey == null");
        return new PolymorphicJsonAdapterFactory2<>(
                baseType, Boolean.class, labelKey, Collections.emptyList(),Collections.emptyList(), null);
    }
    /**
     * @param baseType The base type for which this factory will create adapters. Cannot be Object.
     * @param labelKey The key in the JSON object whose value determines the type to which to map the
     * @return
     */
    public static <T> PolymorphicJsonAdapterFactory2<T, Double> doubleTypeOf(Class<T> baseType, String labelKey) {
        if (baseType == null) throw new NullPointerException("baseType == null");
        if (labelKey == null) throw new NullPointerException("labelKey == null");
        return new PolymorphicJsonAdapterFactory2<>(
                baseType, Double.class, labelKey,Collections.emptyList(),Collections.emptyList(), null);
    }


    /**
     * Returns a new factory that decodes instances of {@code subtype}.
     */
    public PolymorphicJsonAdapterFactory2<T, V> withSubtype(Class<? extends T> subtype, V label) {
        if (subtype == null) throw new NullPointerException("subtype == null");
        if (label == null) throw new NullPointerException("label == null");
        if (labels.contains(label)) {
            throw new IllegalArgumentException("Labels must be unique.");
        }
        List<V> newLabels = new ArrayList<>(labels);
        newLabels.add(label);
        List<Type> newSubtypes = new ArrayList<>(subtypes);
        newSubtypes.add(subtype);
        return new PolymorphicJsonAdapterFactory2<>(
                baseType, labelType, labelKey, newLabels,newSubtypes, fallbackJsonAdapter);
    }

    /**
     * Returns a new factory that with default to {@code fallbackJsonAdapter.fromJson(reader)} upon
     * decoding of unrecognized labels.
     *
     * <p>The {@link JsonReader} instance will not be automatically consumed, so make sure to consume
     * it within your implementation of {@link JsonAdapter#fromJson(JsonReader)}
     */
    public PolymorphicJsonAdapterFactory2<T, V> withFallbackJsonAdapter(
            @Nullable JsonAdapter<Object> fallbackJsonAdapter) {
        return new PolymorphicJsonAdapterFactory2<>(
                baseType, labelType, labelKey,labels, subtypes,fallbackJsonAdapter);
    }

    /**
     * Returns a new factory that will default to {@code defaultValue} upon decoding of unrecognized
     * labels. The default value should be immutable.
     */
    public PolymorphicJsonAdapterFactory2<T, V> withDefaultValue(@Nullable T defaultValue) {
        return withFallbackJsonAdapter(buildFallbackJsonAdapter(defaultValue));
    }

    private JsonAdapter<Object> buildFallbackJsonAdapter(final T defaultValue) {
        return new JsonAdapter<Object>() {
            @Override
            public @Nullable
            Object fromJson(JsonReader reader) throws IOException {
                reader.skipValue();
                return defaultValue;
            }

            @Override
            public void toJson(JsonWriter writer, Object value) throws IOException {
                throw new IllegalArgumentException(
                        "Expected one of "
                                + subtypes
                                + " but found "
                                + value
                                + ", a "
                                + value.getClass()
                                + ". Register this subtype.");
            }
        };
    }

    @Override
    public JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Moshi moshi) {
        if (Types.getRawType(type) != baseType || !annotations.isEmpty()) {
            return null;
        }

        List<JsonAdapter<Object>> jsonAdapters = new ArrayList<>(subtypes.size());
        for (int i = 0, size = subtypes.size(); i < size; i++) {
            jsonAdapters.add(moshi.adapter(subtypes.get(i)));
        }
        return new PolymorphicJsonAdapter<>(labelKey,labelType, labels, subtypes,jsonAdapters, fallbackJsonAdapter)
                .nullSafe();
    }

    static final class PolymorphicJsonAdapter<V> extends JsonAdapter<Object> {
        final String labelKey;
        final Class<V> labelType;
        final List<V> labels;
        final List<Type> subtypes;
        final List<JsonAdapter<Object>> jsonAdapters;
        @Nullable
        final JsonAdapter<Object> fallbackJsonAdapter;

        /**
         * Single-element options containing the label's key only.
         */
        final JsonReader.Options labelKeyOptions;
        PolymorphicJsonAdapter(
                String labelKey,
                Class<V> labelType,
                List<V> labels,
                List<Type> subtypes,
                List<JsonAdapter<Object>> jsonAdapters,
                @Nullable JsonAdapter<Object> fallbackJsonAdapter) {
            this.labelKey = labelKey;
            this.labels = labels;
            this.subtypes = subtypes;
            this.labelType = labelType;
            this.jsonAdapters = jsonAdapters;
            this.fallbackJsonAdapter = fallbackJsonAdapter;

            this.labelKeyOptions = JsonReader.Options.of(labelKey);
        }

        @Override
        public Object fromJson(JsonReader reader) throws IOException {
            JsonReader peeked = reader.peekJson();
            peeked.setFailOnUnknown(false);
            int labelIndex;
            try {
                labelIndex = labelIndex(peeked);
            } finally {
                peeked.close();
            }
            if (labelIndex==-1) {
                return this.fallbackJsonAdapter.fromJson(reader);
            } else {
                return jsonAdapters.get(labelIndex).fromJson(reader);
            }
        }

        private int labelIndex(JsonReader reader) throws IOException {
            reader.beginObject();
            while (reader.hasNext()) {
                if (reader.selectName(labelKeyOptions) == -1) {
                    reader.skipName();
                    reader.skipValue();
                    continue;
                }

                Object value = reader.readJsonValue();


                if(value==null){
                    throw new JsonDataException("label key '"
                            +labelKey
                            +"' value is null");
                }

                //處理number 在json裡會被轉為double的問題
                if(value.getClass()==Double.class){
                    if(labelType==Integer.class){
                        value=((Double)value).intValue();
                    }else if(labelType==Float.class){
                        value=((Double)value).floatValue();
                    }else if(labelType==Short.class){
                        value=((Double)value).shortValue();
                    }else if(labelType==Long.class){
                        value=((Double)value).longValue();
                    }
                }

                 if (value.getClass() != labelType) {
                    throw new JsonDataException(
                            "Expected label's type is '"
                                    + labelType
                                    + "' but found '"
                                    + value.getClass()
                                    + "'. Register a subtype for this label.");

                }

                int labelIndex= labels.indexOf(value);

                if (labelIndex==-1 && this.fallbackJsonAdapter == null) {
                    throw new JsonDataException(
                            "Expected one of "
                                    + labels
                                    + " for key '"
                                    + labelKey
                                    + "' but found '"
                                    + value.toString()
                                    + "'. Register a subtype for this label.");
                }
                return labelIndex;
            }

            throw new JsonDataException("Missing label for " + labelKey);
        }

        @Override
        public void toJson(JsonWriter writer, Object value) throws IOException {
            Class<?> type = value.getClass();
            int labelIndex = subtypes.indexOf(type);
            final JsonAdapter<Object> adapter;
            if (labelIndex == -1) {
                if (fallbackJsonAdapter == null) {
                    throw new IllegalArgumentException(
                            "Expected one of "
                                    + subtypes
                                    + " but found "
                                    + value
                                    + ", a "
                                    + value.getClass()
                                    + ". Register this subtype.");
                }
                adapter = fallbackJsonAdapter;
            } else {
                adapter = jsonAdapters.get(labelIndex);
            }

            writer.beginObject();
            if (adapter != fallbackJsonAdapter) {
                writer.name(labelKey).jsonValue(labels.get(labelIndex));
            }
            int flattenToken = writer.beginFlatten();
            adapter.toJson(writer, value);
            writer.endFlatten(flattenToken);
            writer.endObject();
        }

        @Override
        public String toString() {
            return "PolymorphicJsonAdapter(" + labelKey + ")";
        }
    }
}