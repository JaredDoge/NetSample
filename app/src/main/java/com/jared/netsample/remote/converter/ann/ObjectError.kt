package com.jared.netsample.remote.converter.ann

import kotlin.reflect.KClass


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ObjectError(val clazz : KClass<*>)
