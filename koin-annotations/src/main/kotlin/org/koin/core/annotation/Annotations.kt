package org.koin.core.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS,AnnotationTarget.FUNCTION)
annotation class Single(val binds: Array<KClass<*>> = [], val createdAtStart: Boolean = false)
@Target(AnnotationTarget.CLASS,AnnotationTarget.FUNCTION)
annotation class Factory(val binds: Array<KClass<*>> = [])
@Target(AnnotationTarget.CLASS,AnnotationTarget.FUNCTION)
annotation class Qualifier(val value: String)
@Target(AnnotationTarget.CLASS)
annotation class Scope(val value: String = "")
@Target(AnnotationTarget.CLASS,AnnotationTarget.FUNCTION)
annotation class Scoped(val value: KClass<*>, val binds: Array<KClass<*>> = [])

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Param(val value: String = "")

@Target(AnnotationTarget.PROPERTY)
annotation class ComponentScan(val value: String = "")