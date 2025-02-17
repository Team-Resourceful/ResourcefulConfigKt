package com.teamresourceful.resourcefulconfigkt.api.builders

import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigButton
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigColor
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.Position
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfig.common.info.ParsedColor
import com.teamresourceful.resourcefulconfig.common.loader.ParsedCategory
import com.teamresourceful.resourcefulconfig.common.loader.buttons.ParsedButton
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfigkt.impl.ConfigKtInfo
import kotlin.collections.contains

open class CategoryBuilder internal constructor(internal val id: String) : EntriesBuilder() {

    internal val categories: LinkedHashMap<String, CategoryBuilder> = LinkedHashMap<String, CategoryBuilder>()
    internal val buttons: MutableList<ResourcefulConfigButton> = mutableListOf()

    open val name: TranslatableValue get() = TranslatableValue(id)
    open val description: TranslatableValue get() = TranslatableValue.EMPTY
    open val icon: String get() = "box"
    open val color: ResourcefulConfigColor get() = ParsedColor.DEFAULT
    open val links: Array<ResourcefulConfigLink> get() = emptyArray()
    open val hidden: Boolean get() = false

    fun <T : ObjectKt> obj(id: String, instance: T, builder: TypeBuilder.() -> Unit = {}): T {
        require(id !in entries) { "Entry with id $id already exists" }
        require(id.isNotEmpty()) { "Entry id cannot be empty" }
        require('.' !in id) { "Entry id $id cannot contain '.'" }

        entries[id] = instance.build(TypeBuilder(id).apply(builder).toEntryData())
        return instance
    }

    fun category(id: String, init: CategoryBuilder.() -> Unit = {}): CategoryBuilder {
        val category = CategoryKt(id).apply(init)
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

    internal open fun build(parent: ResourcefulConfig?): ResourcefulConfig {
        val category = ParsedCategory(this.id, parent!!, ConfigKtInfo(this), this.entries, LinkedHashMap<String, ResourcefulConfig>(), this.buttons)
        for ((id, builder) in this.categories) {
            category.categories[id] = builder.build(category)
        }
        return category
    }
}