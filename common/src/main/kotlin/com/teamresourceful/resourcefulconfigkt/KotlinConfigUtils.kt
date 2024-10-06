package com.teamresourceful.resourcefulconfigkt

import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigButton
import com.teamresourceful.resourcefulconfig.api.types.entries.Observable
import com.teamresourceful.resourcefulconfig.api.types.options.AnnotationGetter
import com.teamresourceful.resourcefulconfig.api.types.options.EntryData
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfig.api.types.options.Position
import com.teamresourceful.resourcefulconfig.common.loader.buttons.ParsedButton
import com.teamresourceful.resourcefulconfig.common.loader.entries.ParsedObservableEntry
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

internal class KotlinPropertyAnnotationGetter<T>(private val property: KProperty<T>) : AnnotationGetter {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Annotation> get(annotationClass: Class<T>): T? {
        var annotation = property.annotations.find { it.annotationClass == annotationClass }
        if (annotation != null) return annotation as T
        annotation = property.getter.annotations.find { it.annotationClass == annotationClass }
        if (annotation != null) return annotation as T
        annotation = (property as? KMutableProperty<*>)?.setter?.annotations?.find { it.annotationClass == annotationClass }
        if (annotation != null) return annotation as T
        return property.javaField?.annotations?.find { it.annotationClass == annotationClass.kotlin } as T?
    }
}

internal val <T> KProperty<T>.annotationGetter: AnnotationGetter
    get() = KotlinPropertyAnnotationGetter(this)

internal val <T> KProperty<T>.javaClass: Class<out Any>
    get() = (this.returnType.classifier as KClass<*>).java

internal fun <T> ParsedObservableEntry(entryType: EntryType, property: KProperty1<T, *>, instance: T): ParsedObservableEntry {
    val observable = property.get(instance) as Observable<*>
    val type = if (observable.type().isArray) observable.type().componentType else observable.type()
    val default = observable.get()
    return ParsedObservableEntry(
        entryType,
        type,
        observable,
        EntryData.of(property.annotationGetter, observable.type()),
        default
    )
}

internal fun <T> ParsedButton(button: ConfigButton, target: String, property: KProperty1<T, *>, runnable: () -> Unit): ParsedButton {
    return ParsedButton(
        button.title,
        property.getAnnotation<Comment>()?.value ?: "",
        target,
        Position.AFTER,
        runnable,
        button.text
    )
}

internal inline fun <reified T : Annotation> KProperty<*>.getAnnotation(): T? {
    var annotation = this.findAnnotation<T>() ?: this.getter.findAnnotation<T>()
    if (annotation != null) return annotation
    annotation = (this as? KMutableProperty<*>)?.setter?.findAnnotation<T>()
    if (annotation != null) return annotation
    return this.javaField?.annotations?.find { it.annotationClass == T::class } as T?
}