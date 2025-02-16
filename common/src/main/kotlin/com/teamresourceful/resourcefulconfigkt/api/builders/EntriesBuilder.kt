package com.teamresourceful.resourcefulconfigkt.api.builders

import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.KotlinConfigEntry
import com.teamresourceful.resourcefulconfigkt.api.EntryDelegate
import java.util.LinkedHashMap

open class EntriesBuilder {

    internal val entries: LinkedHashMap<String, ResourcefulConfigEntry> = LinkedHashMap()

    internal fun <T, B : TypeBuilder> entry(
        id: String,
        type: EntryType,
        builderFactory: (String) -> B,
        builderPopulator: (B) -> Unit,
        value: T,
    ): EntryDelegate<T> {
        val property = EntryDelegate(value, value)
        var data = builderFactory(id).apply(builderPopulator).toEntryData()

        entries[id] = KotlinConfigEntry<Any>(
            type,
            { property.set(it as T) },
            { property.get() as Any },
            data,
            value as Any
        )
        return property
    }

    fun byte(id: String, value: Byte, builder: NumberBuilder<Byte>.() -> Unit = {}) = entry(id, EntryType.BYTE, ::NumberBuilder, builder, value)
    fun bytes(id: String, vararg value: Byte, builder: NumberBuilder<Byte>.() -> Unit = {}) = entry(id, EntryType.BYTE, ::NumberBuilder, builder, byteArrayOf(*value))

    fun short(id: String, value: Short, builder: NumberBuilder<Short>.() -> Unit = {}) = entry(id, EntryType.SHORT, ::NumberBuilder, builder, value)
    fun shorts(id: String, vararg value: Short, builder: NumberBuilder<Short>.() -> Unit = {}) = entry(id, EntryType.SHORT, ::NumberBuilder, builder, shortArrayOf(*value))

    fun int(id: String, value: Int, builder: NumberBuilder<Int>.() -> Unit = {}) = entry(id, EntryType.INTEGER, ::NumberBuilder, builder, value)
    fun ints(id: String, vararg value: Int, builder: NumberBuilder<Int>.() -> Unit = {}) = entry(id, EntryType.INTEGER, ::NumberBuilder, builder, intArrayOf(*value))

    fun long(id: String, value: Long, builder: NumberBuilder<Long>.() -> Unit = {}) = entry(id, EntryType.LONG, ::NumberBuilder, builder, value)
    fun longs(id: String, vararg value: Long, builder: NumberBuilder<Long>.() -> Unit = {}) = entry(id, EntryType.LONG, ::NumberBuilder, builder, longArrayOf(*value))

    fun float(id: String, value: Float, builder: NumberBuilder<Float>.() -> Unit = {}) = entry(id, EntryType.FLOAT, ::NumberBuilder, builder, value)
    fun floats(id: String, vararg value: Float, builder: NumberBuilder<Float>.() -> Unit = {}) = entry(id, EntryType.FLOAT, ::NumberBuilder, builder, floatArrayOf(*value))

    fun double(id: String, value: Double, builder: NumberBuilder<Double>.() -> Unit = {}) = entry(id, EntryType.DOUBLE, ::NumberBuilder, builder, value)
    fun doubles(id: String, vararg value: Double, builder: NumberBuilder<Double>.() -> Unit = {}) = entry(id, EntryType.DOUBLE, ::NumberBuilder, builder, doubleArrayOf(*value))

    fun boolean(id: String, value: Boolean, builder: TypeBuilder.() -> Unit = {}) = entry(id, EntryType.BOOLEAN, ::TypeBuilder, builder, value)
    fun booleans(id: String, vararg value: Boolean, builder: TypeBuilder.() -> Unit = {}) = entry(id, EntryType.BOOLEAN, ::TypeBuilder, builder, booleanArrayOf(*value))

    fun string(id: String, value: String, builder: StringBuilder.() -> Unit = {}) = entry(id, EntryType.STRING, ::StringBuilder, builder, value)
    fun strings(id: String, vararg value: String, builder: StringBuilder.() -> Unit = {}) = entry(id, EntryType.STRING, ::StringBuilder, builder, value)

    fun <T : Enum<T>> enum(id: String, value: T, builder: TypeBuilder.() -> Unit = {}): EntryDelegate<T> = entry(id, EntryType.ENUM, ::TypeBuilder, builder, value)
    fun <T : Enum<T>> enums(id: String, vararg value: T, builder: TypeBuilder.() -> Unit = {}) = entry(id, EntryType.ENUM, ::TypeBuilder, builder, value)

    // special
    fun key(id: String, value: Int, builder: KeyBuilder.() -> Unit = {}) = entry(id, EntryType.INTEGER, ::KeyBuilder, builder, value)
    fun color(id: String, value: Int, builder: ColorBuilder.() -> Unit = {}) = entry(id, EntryType.INTEGER, ::ColorBuilder, builder, value)
    fun <T : Enum<T>> select(id: String, vararg value: T, builder: SelectBuilder<T>.() -> Unit = {}) = entry(id, EntryType.ENUM, ::SelectBuilder, builder, value)
    fun <T : Enum<T>> draggable(id: String, vararg value: T, builder: DraggableBuilder<T>.() -> Unit = {}) = entry(id, EntryType.ENUM, { DraggableBuilder(id, getEmptyArray<T>(value.javaClass)) }, builder, value)

    companion object {

        fun Translated(key: String) : TranslatableValue = TranslatableValue("", key)
        fun Literal(value: String) : TranslatableValue = TranslatableValue(value, "")

        @Suppress("UNCHECKED_CAST")
        private fun <T> getEmptyArray(arrayClass: Class<*>): Array<T> {
            return java.lang.reflect.Array.newInstance(arrayClass.componentType, 0) as Array<T>
        }
    }
}