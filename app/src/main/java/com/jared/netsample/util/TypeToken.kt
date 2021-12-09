package com.jared.netsample.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

open class TypeToken<T>(){
        val type: Type = (javaClass
            .genericSuperclass as ParameterizedType).actualTypeArguments[0]
}
