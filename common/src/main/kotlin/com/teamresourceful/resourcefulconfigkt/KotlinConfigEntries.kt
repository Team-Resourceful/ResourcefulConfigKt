package com.teamresourceful.resourcefulconfigkt

import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigElement
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigEntryElement
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigObjectEntry
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigValueEntry
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import com.teamresourceful.resourcefulconfig.api.types.options.EntryData
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfig.api.types.options.Option
import net.minecraft.network.chat.Component
import kotlin.reflect.KMutableProperty1

internal class KotlinConfigEntry<T>(
    private val type: EntryType,
    private val setter: (Any) -> Unit,
    private val getter: () -> Any,
    private val options: EntryData,
    private val default: Any
) : ResourcefulConfigValueEntry {

    constructor(entryType: EntryType, property: KMutableProperty1<T, Any>, instance: T): this(
        entryType,
        { property.set(instance, it) },
        { property.get(instance) },
        EntryData.of(property.annotationGetter, property.javaClass),
        property.get(instance)
    )

    override fun type() = type
    override fun options() = options

    override fun get() = getter()
    fun set(value: Any) = setter(value)

    override fun reset() = set(defaultValue())
    override fun defaultValue() = default

    override fun objectType(): Class<*> {
        val klass = get().javaClass
        return if (klass.isArray) klass.componentType else klass
    }

    override fun isArray() = get().javaClass.isArray

    override fun getArray() = get() as Array<*>
    override fun setArray(array: Array<out Any>) = runCatching {
        val newArray = java.lang.reflect.Array.newInstance(objectType(), array.size)
        System.arraycopy(array, 0, newArray, 0, array.size)
        set(newArray)
    }.isSuccess

    override fun getByte() = get() as Byte
    override fun setByte(value: Byte) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value.toDouble())) error("Value out of range")
        set(value)
    }.isSuccess

    override fun getShort() = get() as Short
    override fun setShort(value: Short) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value.toDouble())) error("Value out of range")
        set(value)
    }.isSuccess

    override fun getInt() = get() as Int
    override fun setInt(value: Int) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value.toDouble())) error("Value out of range")
        set(value)
    }.isSuccess

    override fun getLong() = get() as Long
    override fun setLong(value: Long) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value.toDouble())) error("Value out of range")
        set(value)
    }.isSuccess

    override fun getFloat() = get() as Float
    override fun setFloat(value: Float) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value.toDouble())) error("Value out of range")
        set(value)
    }.isSuccess

    override fun getDouble() = get() as Double
    override fun setDouble(value: Double) = runCatching {
        if (options.hasOption(Option.RANGE) && !options.inRange(value)) error("Value out of range")
        set(value)
    }.isSuccess

    override fun getBoolean() = get() as Boolean
    override fun setBoolean(value: Boolean) = runCatching { set(value) }.isSuccess

    override fun getString() = get() as String

    override fun setString(value: String) = runCatching {
        if (options.hasOption(Option.REGEX) && !options.getOption(Option.REGEX).matcher(value).matches()) {
            error("Value does not match regex")
        }
        set(value)
    }.isSuccess

    override fun getEnum() = get() as Enum<*>

    override fun setEnum(value: Enum<*>) = runCatching { set(value) }.isSuccess
}

internal class KotlinObjectEntry(
    private val instance: Any,
    private val options: EntryData,
    private val elements: MutableList<ResourcefulConfigElement>
) : ResourcefulConfigObjectEntry {

    constructor(instance: Any, options: EntryData) : this(instance, options, mutableListOf<ResourcefulConfigElement>())

    override fun type() = EntryType.OBJECT
    override fun options() = options
    override fun reset() = elements.forEach { (it as? ResourcefulConfigEntryElement)?.entry()?.reset() }
    override fun elements() = elements
    override fun getTitle(fallback: Component): Component = Translatable.toSpeifiedComponent(this.instance, fallback)
}