package com.teamresourceful.resourcefulconfigkt

import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigObjectEntry
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigValueEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryData
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfig.api.types.options.Option
import kotlin.reflect.KMutableProperty1

internal class KotlinConfigEntry<T>(
    private val type: EntryType,
    private val property: KMutableProperty1<T, Any>,
    private val options: EntryData,
    private val instance: T,
    private val default: Any
) : ResourcefulConfigValueEntry {

    constructor(entryType: EntryType, property: KMutableProperty1<T, Any>, instance: T): this(
        entryType,
        property,
        EntryData.of(property.annotationGetter, property.javaClass),
        instance,
        property.get(instance)
    )

    override fun type() = type
    override fun options() = options

    override fun reset() = property.set(instance, defaultValue())
    override fun defaultValue() = default

    override fun objectType(): Class<*> {
        val klass = property.javaClass
        return if (klass.isArray) klass.componentType else klass
    }

    override fun isArray() = property.javaClass.isArray

    override fun get() = property.get(instance)

    override fun getArray() = property.get(instance) as Array<*>
    override fun setArray(array: Array<out Any>) = runCatching { property.set(instance, array) }.isSuccess

    override fun getByte() = property.get(instance) as Byte
    override fun setByte(value: Byte) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value.toDouble())) error("Value out of range")
        property.set(instance, value)
    }.isSuccess

    override fun getShort() = property.get(instance) as Short
    override fun setShort(value: Short) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value.toDouble())) error("Value out of range")
        property.set(instance, value)
    }.isSuccess

    override fun getInt() = property.get(instance) as Int
    override fun setInt(value: Int) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value.toDouble())) error("Value out of range")
        property.set(instance, value)
    }.isSuccess

    override fun getLong() = property.get(instance) as Long
    override fun setLong(value: Long) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value.toDouble())) error("Value out of range")
        property.set(instance, value)
    }.isSuccess

    override fun getFloat() = property.get(instance) as Float
    override fun setFloat(value: Float) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value.toDouble())) error("Value out of range")
        property.set(instance, value)
    }.isSuccess

    override fun getDouble() = property.get(instance) as Double
    override fun setDouble(value: Double) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value)) error("Value out of range")
        property.set(instance, value)
    }.isSuccess

    override fun getBoolean() = property.get(instance) as Boolean
    override fun setBoolean(value: Boolean) = runCatching { property.set(instance, value) }.isSuccess

    override fun getString() = property.get(instance) as String

    override fun setString(value: String) = runCatching {
        if (options.hasOption(Option.REGEX) && !options.getOption(Option.REGEX).matcher(value).matches()) {
            error("Value does not match regex")
        }
        property.set(instance, value)
    }.isSuccess

    override fun getEnum() = property.get(instance) as Enum<*>

    override fun setEnum(value: Enum<*>) = runCatching { property.set(instance, value) }.isSuccess
}

internal class KotlinObjectEntry(
    private val options: EntryData,
    private val entries: LinkedHashMap<String, ResourcefulConfigEntry>,
) : ResourcefulConfigObjectEntry {

    constructor(options: EntryData) : this(options, LinkedHashMap())

    override fun type() = EntryType.OBJECT
    override fun options() = options
    override fun reset() = entries.values.forEach { it.reset() }
    override fun entries() = entries

}