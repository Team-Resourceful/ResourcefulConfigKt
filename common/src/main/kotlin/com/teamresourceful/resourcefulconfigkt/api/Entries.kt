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
) : RConfigKtEntry<T> {

    override var onChange: (T, T) -> Unit = { _, _ -> }
    override val parent: EntryDelegate<T> = this

    override operator fun getValue(thisRef: Any?, property: Any?): T = get()
    override operator fun setValue(thisRef: Any?, property: Any?, value: T) = set(value)

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
    val actualParent: RConfigKtEntry<T>,
    private val from: (R) -> T,
    private val to: (T) -> R,
) : RConfigKtEntry<R> {

    override val parent: RConfigKtEntry<R> = this

    override var onChange: (R, R) -> Unit
        get() = { p1, p2 -> actualParent.onChange(from(p1), from(p2)) }
        set(value) {
            actualParent.onChange = { p1, p2 -> value(to(p1), to(p2))}
        }

    override operator fun getValue(thisRef: Any?, property: Any?): R = to(actualParent.getValue(thisRef, property))
    override operator fun setValue(thisRef: Any?, property: Any?, value: R) = actualParent.setValue(thisRef, property, from(value))
}

class Entry<T, B : TypeBuilder> internal constructor(
    private val id: String?,
    private val type: EntryType,
    private val builderFactory: (String) -> B,
    private val builderFiller: (B) -> Unit,
    private val value: T,
) : ConfigDelegateProvider<RConfigKtEntry<T>> {

    override operator fun provideDelegate(entries: EntriesBuilder, prop: KProperty<*>): EntryDelegate<T> {
        val id = id ?: prop.name
        val entryBuilder = builderFactory(id).apply(builderFiller)
        val data = entryBuilder.toEntryData()
        val property = EntryDelegate<T>(this.value, this.value)

        entries.element(
            EntryElementKt(
                id,
                entryBuilder,
                KotlinConfigEntry<Any>(
                    type,
                    { property.set(it as T) },
                    { property.get() as Any },
                    data,
                    value as Any
                )
            )
        )
        return property
    }
}

class ObservableEntry<T>(
    private val entry: ConfigDelegateProvider<RConfigKtEntry<T>>,
    private val onChange: (T, T) -> Unit,
) : ConfigDelegateProvider<RConfigKtEntry<T>> {
    override operator fun provideDelegate(entries: EntriesBuilder, prop: KProperty<*>): RConfigKtEntry<T> {
        val property = entry.provideDelegate(entries, prop)
        property.onChange = onChange
        return property
    }
}

class TransformedEntry<T, R>(
    private val entry: ConfigDelegateProvider<RConfigKtEntry<T>>,
    private val from: (R) -> T,
    private val to: (T) -> R,
) : ConfigDelegateProvider<RConfigKtEntry<R>> {
    override operator fun provideDelegate(entries: EntriesBuilder, prop: KProperty<*>): RConfigKtEntry<R> {
        val property = entry.provideDelegate(entries, prop)
        return TransformedEntryDelegate(property.parent, from, to)
    }
}

class ObjectProperty<T : ObjectKt>(
    val instance: T,
    val factory: TypeBuilder.() -> Unit = {},
) {
    operator fun provideDelegate(entries: EntriesBuilder, prop: KProperty<*>): Lazy<T> {
        val builder = TypeBuilder(prop.name).apply(factory)
        entries.element(ObjectEntryElementKt(prop.name, builder, instance.build(builder.toEntryData())))
        return lazyOf(instance)
    }
}

interface RConfigKtEntry<T> {
    val parent: RConfigKtEntry<T>
    var onChange: (T, T) -> Unit
    operator fun getValue(thisRef: Any?, property: Any?): T
    operator fun setValue(thisRef: Any?, property: Any?, value: T)
}

interface ConfigDelegateProvider<D> {
    operator fun provideDelegate(entries: EntriesBuilder, prop: KProperty<*>): D
}