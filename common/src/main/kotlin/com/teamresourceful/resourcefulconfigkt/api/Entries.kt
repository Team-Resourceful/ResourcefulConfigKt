package com.teamresourceful.resourcefulconfigkt.api

import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.KotlinConfigEntry
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder
import com.teamresourceful.resourcefulconfigkt.impl.EntryElementKt
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

class Entry<T, B : TypeBuilder> internal constructor(
    private val id: String?,
    private val type: EntryType,
    private val builderFactory: (String) -> B,
    private val builderFiller: (B) -> Unit,
    private val value: T,
) {

    operator fun provideDelegate(builder: EntriesBuilder, prop: KProperty<*>): EntryDelegate<T> {
        val id = id ?: prop.name
        require(id !in builder.reserved) { "Entry with id $id already exists" }
        require(id.isNotEmpty()) { "Entry id cannot be empty" }
        require('.' !in id) { "Entry id $id cannot contain '.'" }

        val entryBuilder = builderFactory(id).apply(builderFiller)
        var data = entryBuilder.toEntryData()
        val property = EntryDelegate<T>(this.value, this.value)

        builder.reserved.add(id)
        builder.elements.add(EntryElementKt(
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
