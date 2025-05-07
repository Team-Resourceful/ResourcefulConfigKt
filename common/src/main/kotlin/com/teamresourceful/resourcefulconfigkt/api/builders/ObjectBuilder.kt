package com.teamresourceful.resourcefulconfigkt.api.builders

import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigObjectEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryData
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.KotlinObjectEntry
import com.teamresourceful.resourcefulconfigkt.impl.ButtonElementKt
import com.teamresourceful.resourcefulconfigkt.impl.SeparatorElementKt

open class ObjectBuilder internal constructor() : EntriesBuilder() {

    fun button(builder: ButtonBuilder.() -> Unit) {
        val button = ButtonBuilder().apply(builder)
        elements.add(ButtonElementKt(button.title, button.description, button.callback::invoke, button.condition, button.text))
    }

    fun separator(builder: SeparatorBuilder.() -> Unit) {
        val separator = SeparatorBuilder().apply(builder)
        elements.add(SeparatorElementKt(TranslatableValue("", separator.title), TranslatableValue("", separator.description), separator.condition))
    }

    internal fun build(options: EntryData): ResourcefulConfigObjectEntry {
        return KotlinObjectEntry(this, options, this.elements)
    }
}