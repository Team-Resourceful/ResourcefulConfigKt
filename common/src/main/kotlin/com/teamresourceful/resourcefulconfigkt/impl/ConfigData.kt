package com.teamresourceful.resourcefulconfigkt.impl

import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigButton
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigEntryElement
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigSeparatorElement
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigInfo
import com.teamresourceful.resourcefulconfig.api.types.options.Option
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.builders.CategoryBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder

internal data class ConfigKtInfo(private val config: CategoryBuilder) : ResourcefulConfigInfo {

    override fun title() = config.name
    override fun description() = config.description
    override fun icon() = config.icon
    override fun color() = config.color
    override fun links() = config.links
    override fun isHidden() = config.hidden

}

internal data class EntryElementKt(
    val id: String,
    val condition: () -> Boolean,
    val entry: ResourcefulConfigEntry
) : ResourcefulConfigEntryElement {

    constructor(id: String, entry: ResourcefulConfigEntry) : this(
        id,
        { !entry.options().hasOption(Option.HIDDEN) },
        entry
    )

    constructor(id: String, builder: TypeBuilder, entry: ResourcefulConfigEntry) : this(
        id,
        builder.condition,
        entry
    )


    override fun id(): String = id
    override fun entry(): ResourcefulConfigEntry = entry
    override fun isHidden(): Boolean = !condition()
}

internal data class ButtonElementKt(
    val title: String,
    val description: String,
    val callback: () -> Unit,
    val condition: () -> Boolean,
    val text: String?,
) : ResourcefulConfigButton {
    override fun title(): String = title
    override fun description(): String = description
    override fun text(): String = text ?: "Click"
    override fun invoke(): Boolean = runCatching { callback() }.isSuccess
    override fun isHidden(): Boolean = !condition()
}

internal data class SeparatorElementKt(
    val title: TranslatableValue,
    val description: TranslatableValue,
    val condition: () -> Boolean,
) : ResourcefulConfigSeparatorElement {
    override fun title(): TranslatableValue = title
    override fun description(): TranslatableValue = description
    override fun isHidden(): Boolean = !condition()
}
