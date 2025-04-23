package com.teamresourceful.resourcefulconfigkt.api.builders

import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigObjectEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryData
import com.teamresourceful.resourcefulconfigkt.KotlinObjectEntry

open class ObjectBuilder internal constructor() : EntriesBuilder() {

    internal fun build(options: EntryData): ResourcefulConfigObjectEntry {
        return KotlinObjectEntry(this, options, this.elements)
    }
}