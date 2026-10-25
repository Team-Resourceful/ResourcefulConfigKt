package com.teamresourceful.resourcefulconfigkt

import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigElement
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigEntryElement
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigListEntry
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigObjectEntry
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigValueEntry
import com.teamresourceful.resourcefulconfig.api.types.info.ListEntryInfoProvider
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import com.teamresourceful.resourcefulconfig.api.types.options.EntryData
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfig.api.types.options.Option
import net.minecraft.network.chat.Component
import java.util.*
import kotlin.jvm.java
import kotlin.reflect.KMutableProperty1


internal class KotlinConfigEntry<T>(
    private val type: EntryType,
    private val setter: (Any) -> Unit,
    private val getter: () -> Any,
    private val options: EntryData,
    private val default: Lazy<Any>,
) : ResourcefulConfigValueEntry {

    constructor(type: EntryType, setter: (Any) -> Unit, getter: () -> Any, options: EntryData, default: Any) : this(type, setter, getter, options, lazyOf(default))
    constructor(entryType: EntryType, property: KMutableProperty1<T, Any>, instance: T): this(
        entryType,
        { property.set(instance, it) },
        { property.get(instance) },
        EntryData.of(property.annotationGetter, property.javaClass),
        property.get(instance),
    )

    override fun type() = type
    override fun options() = options

    override fun get() = getter()
    fun set(value: Any) = setter(value)

    override fun reset() = set(defaultValue())
    override fun defaultValue() = default.value

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
    override fun instance(): Any? = instance
}

internal class KotlinParsedListEntry(
    private val objectType: Class<*>,
    private val list: MutableList<Any>,
    private val options: EntryData,
    private val elementType: EntryType,
    private val factory: ((index: Int) -> Any)? = null,
    private val objectEntryFactory: ((obj: Any) -> ResourcefulConfigEntry)? = null,
    private val objectCopier: ((from: Any, to: Any) -> Unit)? = null,
) : ResourcefulConfigListEntry {

    // we create a snapshot of the list so it doesn't change
    private val defaults: List<Any> = snapshot(list)

    override fun type() = EntryType.LIST
    override fun options() = options

    override fun reset() {
        list.clear()
        list.addAll(snapshot(defaults))
    }

    override fun objectType(): Class<*> = objectType
    override fun size(): Int = list.size

    override fun get(index: Int): ResourcefulConfigEntry {
        val i = wrap(index)

        if (elementType == EntryType.OBJECT) {
            val factory = requireNotNull(objectEntryFactory) {
                "No objectEntryFactory provided for list of $objectType"
            }
            return factory(list[i])
        }

        return KotlinConfigEntry<Any>(
            type = elementType,
            setter = { value -> list[i] = value },
            getter = { list[i] },
            options = options,
            default = lazy { defaultElement(i) },
        )
    }

    override fun move(from: Int, to: Int) {
        if (list.size < 2) return
        Collections.swap(list, wrap(from), wrap(to))
    }

    override fun remove(index: Int) {
        if (list.isEmpty()) return
        list.removeAt(wrap(index))
    }

    override fun clear() = list.clear()

    override fun add(index: Int) {
        val i = if (list.isEmpty()) 0 else Math.floorMod(index, list.size + 1)
        list.add(i, defaultElement(i))
    }

    override fun getTitle(index: Int): Component {
        val item = list[wrap(index)]
        if (item is ListEntryInfoProvider) return item.getTitle(index)
        return Component.literal("Entry #${index + 1}")
    }

    override fun getDescription(index: Int): Component? {
        val item = list[wrap(index)]
        if (item is ListEntryInfoProvider) return item.getDescription(index)
        return Component.empty()
    }

    override fun wrap(index: Int): Int = Math.floorMod(index, list.size)

    private fun defaultElement(index: Int): Any {
        if (factory != null) return factory(index)
        return when (elementType) {
            EntryType.BYTE -> 0.toByte()
            EntryType.SHORT -> 0.toShort()
            EntryType.INTEGER -> 0
            EntryType.LONG -> 0L
            EntryType.FLOAT -> 0.0f
            EntryType.DOUBLE -> 0.0
            EntryType.BOOLEAN -> false
            EntryType.STRING -> ""
            EntryType.ENUM -> objectType.enumConstants.first()
            EntryType.OBJECT -> {
                try {
                    return objectType.getDeclaredConstructor().newInstance()
                } catch (e: Exception) {
                    throw RuntimeException("Failed to instantiate list element $objectType", e)
                }
            }

            else -> throw IllegalStateException("Unsupported list element type: $objectType")
        }
    }

    private fun copyObject(from: Any, to: Any) {
        if (objectCopier != null) return objectCopier(from, to)
        // this is used by kotlin config parser, not the dsl
        for (f in from.javaClass.declaredFields) {
            if (!f.isAnnotationPresent(ConfigEntry::class.java)) continue
            try {
                f.isAccessible = true
                f.set(to, f.get(from))
            } catch (_: Exception) {
            }
        }
    }

    private fun snapshot(source: List<Any>): List<Any> {
        if (elementType != EntryType.OBJECT) return ArrayList(source)
        return source.mapIndexed { i, item ->
            defaultElement(i).also { copy ->
                copyObject(item, copy)
            }
        }
    }
}