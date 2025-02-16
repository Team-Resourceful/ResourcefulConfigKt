package com.teamresourceful.resourcefulconfigkt.api.builders

import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigButton
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigInfo
import com.teamresourceful.resourcefulconfig.api.types.options.Position
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfig.common.info.ParsedColor
import com.teamresourceful.resourcefulconfig.common.info.ParsedInfo
import com.teamresourceful.resourcefulconfig.common.loader.ParsedCategory
import com.teamresourceful.resourcefulconfig.common.loader.buttons.ParsedButton
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

open class CategoryBuilder internal constructor(internal val id: String) : EntriesBuilder() {

    internal val categories: LinkedHashMap<String, CategoryBuilder> = LinkedHashMap<String, CategoryBuilder>()
    internal val buttons: MutableList<ResourcefulConfigButton> = mutableListOf()

    var name: TranslatableValue = TranslatableValue(id)
    var description: TranslatableValue = TranslatableValue.EMPTY
    var hidden: Boolean = false

    var info: InfoBuilder? = null

    fun category(id: String, init: CategoryBuilder.() -> Unit = {}): CategoryBuilder {
        val category = CategoryBuilder(id).apply(init)
        require(id !in categories) { "Category with id $id already exists" }
        categories[id] = category
        return category
    }

    fun <T : CategoryKt> category(category: T) : T {
        require(category.id !in categories) { "Category with id ${category.id} already exists" }
        categories[category.id] = category
        return category
    }

    fun button(builder: ButtonBuilder.() -> Unit) {
        val button = ButtonBuilder().apply(builder)
        buttons.add(ParsedButton(
            button.title,
            button.description,
            this.entries.lastEntry()?.key ?: "",
            Position.AFTER,
            button.callback,
            button.text
        ))
    }

    internal fun buildInfo(): ResourcefulConfigInfo {
        return ParsedInfo(
            this.name,
            this.description,
            info?.icon,
            info?.color ?: ParsedColor.DEFAULT,
            info?.links ?: emptyArray(),
            this.hidden
        )
    }

    internal open fun build(parent: ResourcefulConfig?): ResourcefulConfig {
        val category = ParsedCategory(this.id, parent!!, this.buildInfo(), this.entries, LinkedHashMap<String, ResourcefulConfig>(), this.buttons)
        for ((id, builder) in this.categories) {
            category.categories[id] = builder.build(category)
        }
        return category
    }

    companion object {

        fun info(builder: InfoBuilder.() -> Unit): InfoBuilder {
            return InfoBuilder().apply(builder)
        }
    }
}