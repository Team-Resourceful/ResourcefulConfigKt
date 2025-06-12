package com.teamresourceful.resourcefulconfigkt.api

import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.KotlinConfigEntry
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder
import com.teamresourceful.resourcefulconfigkt.impl.EntryElementKt
import com.teamresourceful.resourcefulconfigkt.impl.ObjectEntryElementKt
import kotlin.reflect.KProperty

class EntryDelegate<T> internal constructor(
    private val default: T,
    private var value: T,
) {

    internal var onChange: (T, T) -> Unit = { _, _ -> }

    operator fun getValue(thisRef: Any?, property: Any?): T = get()
    operator fun setValue(thisRef: Any?, property: Any?, value: T) = set(value)

    fun get(): T {
        return value
    }

    fun set(newValue: T) {
        val oldValue = this.value
        this.value = newValue

        if (oldValue != newValue) {
            this.onChange(oldValue, newValue)
        }
    }

    fun reset() {
        this.value = default
    }
}

class TransformedEntryDelegate<T, R> internal constructor(
    private val parent: EntryDelegate<T>,
    private val from: (R) -> T,
    private val to: (T) -> R,
) {

    private var value: R = to(parent.get())

    init {
        parent.onChange = { old, new ->
            value = to(new)
        }
    }

    operator fun getValue(thisRef: Any?, property: Any?): R = value
    operator fun setValue(thisRef: Any?, property: Any?, value: R) {
        parent.set(from(value))
        this.value = value
    }
}

class Entry<T, B : TypeBuilder> internal constructor(
    private val id: String?,
    private val type: EntryType,
    private val builderFactory: (String) -> B,
    private val builderFiller: (B) -> Unit,
    private val value: T,
) {

    operator fun provideDelegate(builder: EntriesBuilder, prop: KProperty<*>): EntryDelegate<T> {
        val id = id ?: prop.name
        val entryBuilder = builderFactory(id).apply(builderFiller)
        var data = entryBuilder.toEntryData()
        val property = EntryDelegate<T>(this.value, this.value)

        builder.element(EntryElementKt(
            id,
            entryBuilder,
            KotlinConfigEntry<Any>(
                type,
                { property.set(it as T) },
                { property.get() as Any },
                data,
                value as Any
            )
        ))
        return property
    }
}

class ObservableEntry<T, B : TypeBuilder>(
    private val entry: Entry<T, B>,
    private val onChange: (T, T) -> Unit
) {
    operator fun provideDelegate(builder: EntriesBuilder, prop: KProperty<*>): EntryDelegate<T> {
        val property = entry.provideDelegate(builder, prop)
        property.onChange = onChange
        return property
    }
}

class TransformedEntry<T, B : TypeBuilder, R>(
    private val entry: Entry<T, B>,
    private val from: (R) -> T,
    private val to: (T) -> R,
) {
    operator fun provideDelegate(builder: EntriesBuilder, prop: KProperty<*>): TransformedEntryDelegate<T, R> {
        val property = entry.provideDelegate(builder, prop)
        return TransformedEntryDelegate(property, from, to)
    }
}

class ObjectProperty<T : ObjectKt>(
    val instance: T,
    val factory: TypeBuilder.() -> Unit = {}
) {

    operator fun provideDelegate(entries: EntriesBuilder, prop: KProperty<*>): Lazy<T> {
        val builder = TypeBuilder(prop.name).apply(factory)
        entries.element(ObjectEntryElementKt(prop.name, builder, instance.build(builder.toEntryData())))
        return lazyOf(instance)
    }

}
