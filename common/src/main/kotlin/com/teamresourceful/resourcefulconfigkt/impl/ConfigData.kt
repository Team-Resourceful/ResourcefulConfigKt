package com.teamresourceful.resourcefulconfigkt.impl

import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigButton
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigEntryElement
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigSeparatorElement
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigInfo
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.builders.CategoryBuilder

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
    val entry: ResourcefulConfigEntry
) : ResourcefulConfigEntryElement {
    override fun id(): String = id
    override fun entry(): ResourcefulConfigEntry = entry
}

internal data class ButtonElementKt(
    val title: String,
    val description: String,
    val callback: () -> Unit,
    val text: String?,
) : ResourcefulConfigButton {
    override fun title(): String = title
    override fun description(): String = description
    override fun text(): String = text ?: "Click"
    override fun invoke(): Boolean = runCatching { callback() }.isSuccess
}

internal data class SeparatorElementKt(
    val title: TranslatableValue,
    val description: TranslatableValue,
) : ResourcefulConfigSeparatorElement {
    override fun title(): TranslatableValue = title
    override fun description(): TranslatableValue = description
}
