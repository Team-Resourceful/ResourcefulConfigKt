package com.teamresourceful.resourcefulconfigkt.api.builders

import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigColor
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink

class InfoBuilder internal constructor() {

    var icon: String = "box"
    var color: ResourcefulConfigColor? = null
    var links: Array<ResourcefulConfigLink> = emptyArray()

}