package com.teamresourceful.resourcefulconfigkt.api.builders

import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigElement
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigEntryElement
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigDelegateProvider
import com.teamresourceful.resourcefulconfigkt.api.Entry
import com.teamresourceful.resourcefulconfigkt.api.RConfigKtEntry
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import com.teamresourceful.resourcefulconfigkt.api.TransformedEntry

open class EntriesBuilder {

    internal val reserved = mutableListOf<String>()
    internal val elements = mutableListOf<ResourcefulConfigElement>()

    fun element(element: ResourcefulConfigElement) {
        if (element is ResourcefulConfigEntryElement) {
            val id = element.id()
            require(id !in reserved) { "Entry with id $id already exists" }
            require(id.isNotEmpty()) { "Entry id cannot be empty" }
            require('.' !in id) { "Entry id $id cannot contain '.'" }

            this.reserved.add(element.id())
        }
        this.elements.add(element)
    }

    fun byte(value: Byte, builder: NumberBuilder<Byte>.() -> Unit = {}) = Entry(null, EntryType.BYTE, ::NumberBuilder, builder, value)
    fun byte(id: String, value: Byte, builder: NumberBuilder<Byte>.() -> Unit = {}) = Entry(id, EntryType.BYTE, ::NumberBuilder, builder, value)

    fun bytes(vararg value: Byte, builder: NumberBuilder<Byte>.() -> Unit = {}) = Entry(null, EntryType.BYTE, ::NumberBuilder, builder, byteArrayOf(*value))
    fun bytes(id: String, vararg value: Byte, builder: NumberBuilder<Byte>.() -> Unit = {}) = Entry(id, EntryType.BYTE, ::NumberBuilder, builder, byteArrayOf(*value))

    fun short(value: Short, builder: NumberBuilder<Short>.() -> Unit = {}) = Entry(null, EntryType.SHORT, ::NumberBuilder, builder, value)
    fun short(id: String, value: Short, builder: NumberBuilder<Short>.() -> Unit = {}) = Entry(id, EntryType.SHORT, ::NumberBuilder, builder, value)

    fun shorts(vararg value: Short, builder: NumberBuilder<Short>.() -> Unit = {}) = Entry(null, EntryType.SHORT, ::NumberBuilder, builder, shortArrayOf(*value))
    fun shorts(id: String, vararg value: Short, builder: NumberBuilder<Short>.() -> Unit = {}) = Entry(id, EntryType.SHORT, ::NumberBuilder, builder, shortArrayOf(*value))

    fun int(value: Int, builder: NumberBuilder<Int>.() -> Unit = {}) = Entry(null, EntryType.INTEGER, ::NumberBuilder, builder, value)
    fun int(id: String, value: Int, builder: NumberBuilder<Int>.() -> Unit = {}) = Entry(id, EntryType.INTEGER, ::NumberBuilder, builder, value)

    fun ints(vararg value: Int, builder: NumberBuilder<Int>.() -> Unit = {}) = Entry(null, EntryType.INTEGER, ::NumberBuilder, builder, intArrayOf(*value))
    fun ints(id: String, vararg value: Int, builder: NumberBuilder<Int>.() -> Unit = {}) = Entry(id, EntryType.INTEGER, ::NumberBuilder, builder, intArrayOf(*value))

    fun long(value: Long, builder: NumberBuilder<Long>.() -> Unit = {}) = Entry(null, EntryType.LONG, ::NumberBuilder, builder, value)
    fun long(id: String, value: Long, builder: NumberBuilder<Long>.() -> Unit = {}) = Entry(id, EntryType.LONG, ::NumberBuilder, builder, value)

    fun longs(vararg value: Long, builder: NumberBuilder<Long>.() -> Unit = {}) = Entry(null, EntryType.LONG, ::NumberBuilder, builder, longArrayOf(*value))
    fun longs(id: String, vararg value: Long, builder: NumberBuilder<Long>.() -> Unit = {}) = Entry(id, EntryType.LONG, ::NumberBuilder, builder, longArrayOf(*value))

    fun float(value: Float, builder: NumberBuilder<Float>.() -> Unit = {}) = Entry(null, EntryType.FLOAT, ::NumberBuilder, builder, value)
    fun float(id: String, value: Float, builder: NumberBuilder<Float>.() -> Unit = {}) = Entry(id, EntryType.FLOAT, ::NumberBuilder, builder, value)

    fun floats(vararg value: Float, builder: NumberBuilder<Float>.() -> Unit = {}) = Entry(null, EntryType.FLOAT, ::NumberBuilder, builder, floatArrayOf(*value))
    fun floats(id: String, vararg value: Float, builder: NumberBuilder<Float>.() -> Unit = {}) = Entry(id, EntryType.FLOAT, ::NumberBuilder, builder, floatArrayOf(*value))

    fun double(value: Double, builder: NumberBuilder<Double>.() -> Unit = {}) = Entry(null, EntryType.DOUBLE, ::NumberBuilder, builder, value)
    fun double(id: String, value: Double, builder: NumberBuilder<Double>.() -> Unit = {}) = Entry(id, EntryType.DOUBLE, ::NumberBuilder, builder, value)

    fun doubles(vararg value: Double, builder: NumberBuilder<Double>.() -> Unit = {}) = Entry(null, EntryType.DOUBLE, ::NumberBuilder, builder, doubleArrayOf(*value))
    fun doubles(id: String, vararg value: Double, builder: NumberBuilder<Double>.() -> Unit = {}) = Entry(id, EntryType.DOUBLE, ::NumberBuilder, builder, doubleArrayOf(*value))

    fun boolean(value: Boolean, builder: TypeBuilder.() -> Unit = {}) = Entry(null, EntryType.BOOLEAN, ::TypeBuilder, builder, value)
    fun boolean(id: String? = null, value: Boolean, builder: TypeBuilder.() -> Unit = {}) = Entry(id, EntryType.BOOLEAN, ::TypeBuilder, builder, value)

    fun booleans(vararg value: Boolean, builder: TypeBuilder.() -> Unit = {}) = Entry(null, EntryType.BOOLEAN, ::TypeBuilder, builder, booleanArrayOf(*value))
    fun booleans(id: String, vararg value: Boolean, builder: TypeBuilder.() -> Unit = {}) = Entry(id, EntryType.BOOLEAN, ::TypeBuilder, builder, booleanArrayOf(*value))

    fun string(value: String, builder: StringBuilder.() -> Unit = {}) = Entry(null, EntryType.STRING, ::StringBuilder, builder, value)
    fun string(id: String, value: String, builder: StringBuilder.() -> Unit = {}) = Entry(id, EntryType.STRING, ::StringBuilder, builder, value)

    // Very hacky but sadly the varargs if its nullable makes it weird
    fun strings(vararg value: String, builder: StringBuilder.() -> Unit = {}) = Entry(null, EntryType.STRING, ::StringBuilder, builder, value)
    fun stringsWithId(id: String, vararg value: String, builder: StringBuilder.() -> Unit = {}) = Entry(id, EntryType.STRING, ::StringBuilder, builder, value)

    fun <T : Enum<T>> enum(value: T, builder: TypeBuilder.() -> Unit = {}) = Entry(null, EntryType.ENUM, ::TypeBuilder, builder, value)
    fun <T : Enum<T>> enum(id: String, value: T, builder: TypeBuilder.() -> Unit = {}) = Entry(id, EntryType.ENUM, ::TypeBuilder, builder, value)

    fun <T : Enum<T>> enums(vararg value: T, builder: TypeBuilder.() -> Unit = {}) = Entry(null, EntryType.ENUM, ::TypeBuilder, builder, value)
    fun <T : Enum<T>> enums(id: String, vararg value: T, builder: TypeBuilder.() -> Unit = {}) = Entry(id, EntryType.ENUM, ::TypeBuilder, builder, value)

    // special
    fun key(value: Int, builder: KeyBuilder.() -> Unit = {}) = Entry(null, EntryType.INTEGER, ::KeyBuilder, builder, value)
    fun key(id: String, value: Int, builder: KeyBuilder.() -> Unit = {}) = Entry(id, EntryType.INTEGER, ::KeyBuilder, builder, value)

    fun color(value: Int, builder: ColorBuilder.() -> Unit = {}) = Entry(null, EntryType.INTEGER, ::ColorBuilder, builder, value)
    fun color(id: String, value: Int, builder: ColorBuilder.() -> Unit = {}) = Entry(id, EntryType.INTEGER, ::ColorBuilder, builder, value)

    fun <T : Enum<T>> select(vararg value: T, builder: SelectBuilder<T>.() -> Unit = {}) = Entry(null, EntryType.ENUM, ::SelectBuilder, builder, value)
    fun <T : Enum<T>> select(id: String, vararg value: T, builder: SelectBuilder<T>.() -> Unit = {}) = Entry(id, EntryType.ENUM, ::SelectBuilder, builder, value)

    fun <T : Enum<T>> draggable(vararg value: T, builder: DraggableBuilder<T>.() -> Unit = {}) = Entry(null, EntryType.ENUM, { DraggableBuilder(it, getEmptyArray<T>(value.javaClass)) }, builder, value)
    fun <T : Enum<T>> draggable(id: String, vararg value: T, builder: DraggableBuilder<T>.() -> Unit = {}) = Entry(id, EntryType.ENUM, { DraggableBuilder(it, getEmptyArray<T>(value.javaClass)) }, builder, value)

    fun <T> observable(entry: Entry<T, *>, onChange: (T, T) -> Unit) = ObservableEntry(entry, onChange)
    fun <T> observable(entry: ConfigDelegateProvider<RConfigKtEntry<T>>, onChange: (T, T) -> Unit) = ObservableEntry(entry, onChange)
    fun <T, R> transform(entry: Entry<T, *>, from: (R) -> T, to: (T) -> R) = TransformedEntry(entry, from, to)
    fun <T, R> transform(entry: ConfigDelegateProvider<RConfigKtEntry<T>>, from: (R) -> T, to: (T) -> R) = TransformedEntry(entry, from, to)

    companion object {

        fun Translated(key: String) : TranslatableValue = TranslatableValue("", key)
        fun Literal(value: String) : TranslatableValue = TranslatableValue(value, "")

        @Suppress("UNCHECKED_CAST")
        private fun <T> getEmptyArray(arrayClass: Class<*>): Array<T> {
            return java.lang.reflect.Array.newInstance(arrayClass.componentType, 0) as Array<T>
        }
    }
}
