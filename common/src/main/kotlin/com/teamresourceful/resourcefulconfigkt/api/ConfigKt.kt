package com.teamresourceful.resourcefulconfigkt.api

import com.google.gson.JsonObject
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig
import com.teamresourceful.resourcefulconfig.common.loader.ParsedConfig
import com.teamresourceful.resourcefulconfigkt.api.builders.CategoryBuilder
import com.teamresourceful.resourcefulconfigkt.impl.ConfigKtInfo
import org.intellij.lang.annotations.Pattern
import org.jetbrains.annotations.Range
import java.util.function.UnaryOperator

open class ConfigKt(
    @Pattern("^[a-z0-9_/-]+$") private val file: String
) : CategoryBuilder(file) {

    private var registered: Boolean = false

    open val version: @Range(from = 0L, to = 2147483647L) Int = 0
    open val patches: Map<Int, UnaryOperator<JsonObject>> = mapOf()

    override fun build(parent: ResourcefulConfig?): ResourcefulConfig {
        val config = ParsedConfig(this.version, this.file, ConfigKtInfo(this), this.entries, LinkedHashMap<String, ResourcefulConfig>(), this.buttons)
        for ((id, builder) in this.categories) {
            config.categories[id] = builder.build(config)
        }
        return config
    }

    fun register(configurator: Configurator) {
        require(!registered) { "Config already registered" }

        configurator.register(build(null)) { event ->
            this.patches.forEach(event::register)
        }
    }
}