package com.teamresourceful.resourcefulconfigkt

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Config
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigButton
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.loader.ConfigParser
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig
import com.teamresourceful.resourcefulconfig.api.types.entries.Observable
import com.teamresourceful.resourcefulconfig.api.types.options.EntryData
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfig.common.info.ParsedInfo
import com.teamresourceful.resourcefulconfig.common.loader.ParsedCategory
import com.teamresourceful.resourcefulconfig.common.loader.ParsedConfig
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class KotlinConfigParser : ConfigParser {

    override fun priority() = 0

    override fun parse(clazz: Class<*>): ResourcefulConfig? {
        val klass = clazz.kotlin
        if (klass.objectInstance == null) return null
        val data = klass.findAnnotation<Config>() ?: error("Config annotation not found on class $klass")
        return populateEntries(
            klass.objectInstance!!,
            ParsedConfig(data, ParsedInfo.of(clazz, data.value)),
            data.categories
        )
    }

    private fun <T : Any> populateEntries(
        instance: T,
        config: ResourcefulConfig,
        categories: Array<KClass<*>>
    ): ResourcefulConfig {
        val klass = instance::class
        val properties: List<KProperty1<T, Any>> = klass.jvmOrderedProperties.filterIsInstance<KProperty1<T, Any>>()
        for (property in properties) {
            assertEntry(instance, property)?.let { data ->
                if (data.type == EntryType.OBJECT) {
                    val subInstance = property.get(instance)
                    val objectEntry = KotlinObjectEntry(EntryData.of(property.annotationGetter, property.javaClass))
                    populateEntries(subInstance, objectEntry)
                    config.entries()[data.id] = objectEntry
                } else if (property.returnType.isSubtypeOf(Observable::class.starProjectedType)) {
                    config.entries()[data.id] = ParsedObservableEntry(data.type, property, instance)
                } else {
                    config.entries()[data.id] = KotlinConfigEntry(data.type, property as KMutableProperty1<T, Any>, instance)
                }
            }
            assertButton(instance, property)?.let { (data, runnable) ->
                val target = config.entries().lastEntry()?.key ?: ""
                config.buttons().add(ParsedButton(data, target, property, runnable))
            }
        }

        for (category in categories) {
            val subInstance = category.objectInstance ?: continue
            val data = category.findAnnotation<Category>() ?: error("Category annotation not found on class $category")
            config.categories()[data.value] = populateEntries(
                subInstance,
                ParsedCategory(data, ParsedInfo.of(category.java, data.value), config),
                data.categories
            )
        }
        return config
    }

    private fun <T : Any> populateEntries(instance: T, config: KotlinObjectEntry) {
        val klass = instance::class
        val properties: List<KProperty1<T, Any>> = klass.jvmOrderedProperties.filterIsInstance<KProperty1<T, Any>>()
        for (property in properties) {
            val data = assertEntry(instance, property) ?: continue
            val entry = if (data.type == EntryType.OBJECT) {
                error("Entry ${property.name} cannot be an object!")
            } else if (property.returnType.isSubtypeOf(Observable::class.starProjectedType)) {
                ParsedObservableEntry(data.type, property, instance)
            } else {
                KotlinConfigEntry(data.type, property as KMutableProperty1<T, Any>, instance)
            }

            if (entry.defaultValue() == null) error("Entry ${property.name} has a null default value!")

            config.entries()[data.id] = entry
        }
    }

    private val <T : Any> KClass<T>.jvmOrderedProperties: List<KProperty1<T, *>>
        get() {
            var index = 0
            val indexes = this.java.declaredFields.associate { it.name to index++ }

            return this.declaredMemberProperties.sortedBy {
                indexes[it.name] ?: error("Property ${it.name} not found in class ${this.simpleName}")
            }
        }

    private fun <T : Any> assertEntry(instance: T, property: KProperty1<T, *>): ConfigEntry? {
        val data = property.getAnnotation<ConfigEntry>() ?: return null
        if (data.type.mustBeFinal() == property is KMutableProperty<*>) error("Property ${property.name} in must be ${if (data.type.mustBeFinal()) "val" else "var"}")
        if (property.returnType.isMarkedNullable) error("Property ${property.name} in must not be nullable")

        val klass = getFieldType(instance, property, data.type)
        if (!data.type.test(klass)) error("Property ${property.name} is not of type ${data.type.name}!")
        if (data.id.contains(".")) error("Entry ${property.name} has an invalid id! Ids must not contain '.'")

        return data
    }

    private fun <T : Any> assertButton(instance: T, property: KProperty1<T, *>): Pair<ConfigButton, () -> Unit>? {
        val data = property.getAnnotation<ConfigButton>() ?: return null
        if (property is KMutableProperty<*>) error("Property ${property.name} in must be val")
        if (property.returnType.isMarkedNullable) error("Property ${property.name} in must not be nullable")
        if (property.returnType.isSubtypeOf(Runnable::class.starProjectedType)) {
            val runnable = property.get(instance)!! as Runnable
            return Pair(data) { runnable.run() }
        } else if (property.returnType.isSubtypeOf(Function0::class.starProjectedType)) {
            val function = property.get(instance)!! as () -> Any?
            return Pair(data) { function() }
        }
        error("Property ${property.name} is not of type Runnable or Function0!")
    }

    private fun <T : Any> getFieldType(instance: T, property: KProperty1<T, *>, entry: EntryType): Class<*> {
        val type =  property.javaClass
        if (type.isArray) {
            require(entry.isAllowedInArrays) {
                "Entry " + property.name + " is an array but its type is not allowed in arrays!"
            }
            return type.componentType
        } else if (property.returnType.isSubtypeOf(Observable::class.starProjectedType)) {
            return runCatching {
                (property.get(instance) as Observable<*>).type()
            }.getOrElse {
                error("Property ${property.name} is an Observable but its type could not be determined!")
            }
        }
        return type
    }
}