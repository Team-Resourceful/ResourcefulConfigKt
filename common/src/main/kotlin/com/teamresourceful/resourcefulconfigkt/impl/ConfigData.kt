package com.teamresourceful.resourcefulconfigkt.impl

import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigInfo
import com.teamresourceful.resourcefulconfigkt.api.builders.CategoryBuilder

internal data class ConfigKtInfo(private val config: CategoryBuilder) : ResourcefulConfigInfo {

    override fun title() = config.name
    override fun description() = config.description
    override fun icon() = config.icon
    override fun color() = config.color
    override fun links() = config.links
    override fun isHidden() = config.hidden

}