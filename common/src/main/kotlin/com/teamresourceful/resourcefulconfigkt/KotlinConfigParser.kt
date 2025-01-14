package com.teamresourceful.resourcefulconfigkt

import com.teamresourceful.resourcefulconfig.api.annotations.*
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
                val type = getEntryType(instance, property)
                if (type == EntryType.OBJECT) {
                    val subInstance = property.get(instance)
                    val objectEntry = KotlinObjectEntry(subInstance, EntryData.of(property.annotationGetter, property.javaClass))
                    populateEntries(subInstance, objectEntry)
                    config.entries()[data.id] = objectEntry
                } else if (property.returnType.isSubtypeOf(Observable::class.starProjectedType)) {
                    config.entries()[data.id] = ParsedObservableEntry(type, property, instance)
                } else {
                    config.entries()[data.id] = KotlinConfigEntry(type, property as KMutableProperty1<T, Any>, instance)
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
            val type = getEntryType(instance, property)
            val entry = if (type == EntryType.OBJECT) {
                error("Entry ${property.name} cannot be an object!")
            } else if (property.returnType.isSubtypeOf(Observable::class.starProjectedType)) {
                ParsedObservableEntry(type, property, instance)
            } else {
                KotlinConfigEntry(type, property as KMutableProperty1<T, Any>, instance)
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
        val name = property.name
        return collectErrors({ "Entry $name is invalid!\n\t$this\n" }) {
            val type = getEntryType(instance, property)
            val isObservable = property.returnType.isSubtypeOf(Observable::class.starProjectedType)
            if (!isObservable && type.mustBeFinal() == property is KMutableProperty<*>) add("Property ${property.name} in must be ${if (type.mustBeFinal()) "val" else "var"}")
            if (isObservable && property is KMutableProperty<*>) add("Property ${property.name} in must be val")
            if (property.returnType.isMarkedNullable) add("Property ${property.name} in must not be nullable")

            val klass = getFieldType(instance, property, type)
            if (!type.test(klass)) add("Property ${property.name} is not of type ${type.name}!")
            if (data.id.contains(".")) add("Entry ${property.name} has an invalid id! Ids must not contain '.'")

            return@collectErrors data
        }
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
        var type =  property.javaClass
        if (property.returnType.isSubtypeOf(Observable::class.starProjectedType)) {
            type = runCatching {
                (property.get(instance) as Observable<*>).type()
            }.getOrElse {
                error("Property ${property.name} is an Observable but its type could not be determined!")
            }
        }
        if (type.isArray) {
            require(entry.isAllowedInArrays) {
                "Entry " + property.name + " is an array but its type is not allowed in arrays!"
            }
            return type.componentType
        }
        return type
    }

    private fun <T : Any> getEntryType(instance: T, property: KProperty1<T, *>): EntryType {
        var fieldType: Class<*> = property.javaClass
        if (fieldType == Observable::class.java) fieldType = (property.get(instance) as Observable<*>).type()
        if (fieldType.isArray) fieldType = fieldType.componentType
        return getEntryType(fieldType)
    }

    private fun getEntryType(type: Class<*>): EntryType {
        return when {
            type.getAnnotation(ConfigObject::class.java) != null -> EntryType.OBJECT
            type == java.lang.Long.TYPE || type == java.lang.Long::class.java || type == Long::class.java -> EntryType.LONG
            type == java.lang.Integer.TYPE || type == java.lang.Integer::class.java || type == Int::class.java -> EntryType.INTEGER
            type == java.lang.Short.TYPE || type == java.lang.Short::class.java || type == Short::class.java -> EntryType.SHORT
            type == java.lang.Byte.TYPE || type == java.lang.Byte::class.java || type == Byte::class.java -> EntryType.BYTE
            type == java.lang.Double.TYPE || type == java.lang.Double::class.java || type == Double::class.java -> EntryType.DOUBLE
            type == java.lang.Float.TYPE || type == java.lang.Float::class.java || type == Float::class.java -> EntryType.FLOAT
            type == java.lang.Boolean.TYPE || type == java.lang.Boolean::class.java || type == Boolean::class.java -> EntryType.BOOLEAN
            type == String::class.java -> EntryType.STRING
            type.isEnum -> EntryType.ENUM
            else -> error("Could not infer entry type for ${type.name}")
        }
    }

    private fun <R> collectErrors(response: String.() -> String, block: MutableList<String>.() -> R): R {
        val errors = mutableListOf<String>()
        val result = errors.block()
        if (errors.isNotEmpty()) error(response(errors.joinToString("\n\t")))
        return result
    }
}