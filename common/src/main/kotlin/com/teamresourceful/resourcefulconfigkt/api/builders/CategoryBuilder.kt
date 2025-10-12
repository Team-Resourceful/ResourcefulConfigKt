package com.teamresourceful.resourcefulconfigkt.api.builders

import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigColor
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigColorValue
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfig.common.loader.ParsedCategory
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfigkt.api.ObjectProperty
import com.teamresourceful.resourcefulconfigkt.impl.ConfigKtInfo
import com.teamresourceful.resourcefulconfigkt.impl.ObjectEntryElementKt

open class CategoryBuilder internal constructor(internal val id: String) : EntriesBuilder() {

    internal val categories: LinkedHashMap<String, CategoryBuilder> = LinkedHashMap<String, CategoryBuilder>()

    open val name: TranslatableValue get() = TranslatableValue(id)
    open val description: TranslatableValue get() = TranslatableValue.EMPTY
    open val icon: String get() = "box"
    open val color: ResourcefulConfigColor get() = ResourcefulConfigColorValue.create("#ffffff")
    open val links: Array<ResourcefulConfigLink> get() = emptyArray()
    open val hidden: Boolean get() = false

    fun <T : ObjectKt> obj(id: String, instance: T, builder: TypeBuilder.() -> Unit = {}): T {
        val builder = TypeBuilder(id).apply(builder)
        element(ObjectEntryElementKt(id, builder, instance.build(builder.toEntryData())))
        return instance
    }

    fun <T : ObjectKt> obj(instance: T, builder: TypeBuilder.() -> Unit = {}): ObjectProperty<T> {
        return ObjectProperty(instance, builder)
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

    internal open fun build(parent: ResourcefulConfig?): ResourcefulConfig {
        val category = ParsedCategory(this.id, parent!!, ConfigKtInfo(this), this.elements, LinkedHashMap<String, ResourcefulConfig>())
        for ((id, builder) in this.categories) {
            category.categories[id] = builder.build(category)
        }
        return category
    }
}