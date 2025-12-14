package com.teamresourceful.resourcefulconfigkt.api.builders

import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption
import com.teamresourceful.resourcefulconfig.api.types.options.EntryData
import com.teamresourceful.resourcefulconfig.api.types.options.Option
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class Options internal constructor() {

    internal val options: MutableMap<Option<*, *>, Any> = mutableMapOf()

    operator fun <T : Any> plusAssign(entry: Pair<Option<*, T>, T>) {
        options[entry.first] = entry.second
    }
}

open class TypeBuilder internal constructor(val id: String) {

    var name: TranslatableValue = TranslatableValue(id)
    var description: TranslatableValue = TranslatableValue.EMPTY
    var renderer: ResourceLocation? = null
    var condition: () -> Boolean = { true }
    var searchTerms: List<String> = emptyList()
    val options: Options = Options()

    @Deprecated("Use condition instead")
    var hidden: Boolean = false

    var translation: String
        get() = ""
        set(value) {
            this.name = TranslatableValue(this.id, value)
            this.description = TranslatableValue("", "$value.desc")
        }

    internal fun toEntryData(): EntryData = EntryData(
        name, description,
        buildMap<Option<*, *>, Any?>(::buildOptions).filter { entry -> entry.value != null }
    )

    protected open fun buildOptions(options: MutableMap<Option<*, *>, Any?>) {
        options.putAll(this.options.options)
        options.put(Option.HIDDEN, if (this.hidden) ConfigOption.Hidden() else null)
        options.put(Option.RENDERER, this.renderer)
        options.put(Option.SEARCH_TERM, this.searchTerms)
    }
}

class NumberBuilder<T> internal constructor(id: String) : TypeBuilder(id) where T : Number, T : Comparable<T> {

    var range: ClosedRange<T>? = null
    var slider: Boolean = false

    override fun buildOptions(options: MutableMap<Option<*, *>, Any?>) {
        super.buildOptions(options)

        options.put(Option.RANGE, range?.let { ConfigOption.Range(it.start.toDouble(), it.endInclusive.toDouble())})
        options.put(Option.SLIDER, if (this.slider) ConfigOption.Slider() else null)
    }
}

class StringBuilder internal constructor(id: String) : TypeBuilder(id) {

    var regex: Regex? = null
    var multiline: Boolean = false

    override fun buildOptions(options: MutableMap<Option<*, *>, Any?>) {
        super.buildOptions(options)

        options.put(Option.REGEX, regex?.toPattern())
        options.put(Option.MULTILINE, if (this.multiline) ConfigOption.Multiline() else null)
    }
}

// special

class KeyBuilder internal constructor(id: String) : TypeBuilder(id) {

    override fun buildOptions(options: MutableMap<Option<*, *>, Any?>) {
        super.buildOptions(options)
        options.put(Option.KEYBIND, ConfigOption.Keybind())
    }
}

class ColorBuilder internal constructor(id: String) : TypeBuilder(id) {

    var presets: IntArray = intArrayOf()
    var allowAlpha: Boolean = false

    override fun buildOptions(options: MutableMap<Option<*, *>, Any?>) {
        super.buildOptions(options)

        options.put(Option.COLOR, ConfigOption.Color(presets, allowAlpha))
    }
}

class SelectBuilder<T : Enum<T>> internal constructor(id: String) : TypeBuilder(id) {

    override fun buildOptions(options: MutableMap<Option<*, *>, Any?>) {
        super.buildOptions(options)

        options.put(Option.SELECT, Component.literal("Select"))
    }
}

class DraggableBuilder<T : Enum<T>> internal constructor(id: String, private val empty: Array<T>) : TypeBuilder(id) {

    var duplicatable: Array<T>? = null
    var range: Pair<Int, Int>? = null

    override fun buildOptions(options: MutableMap<Option<*, *>, Any?>) {
        super.buildOptions(options)

        options.put(Option.DRAGGABLE, this.duplicatable ?: empty)
        options.put(Option.RANGE, range?.let { ConfigOption.Range(it.first.toDouble(), it.second.toDouble())})
    }
}

class ButtonBuilder {

    var title: String = ""
    var description: String = ""
    var text: String? = null
    var condition: () -> Boolean = { true }
    internal var callback: () -> Unit = {}

    fun onClick(callback: () -> Unit) {
        this.callback = callback
    }
}

class SeparatorBuilder {

    var title: String = ""
    var description: String = ""
    var condition: () -> Boolean = { true }
}