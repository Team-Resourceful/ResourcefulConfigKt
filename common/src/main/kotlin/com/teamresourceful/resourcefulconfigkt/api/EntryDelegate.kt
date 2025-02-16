package com.teamresourceful.resourcefulconfigkt.api

class EntryDelegate<T> internal constructor(
    private val default: T,
    private var value: T
) {


    operator fun getValue(thisRef: Any?, property: Any?): T {
        return value
    }

    fun get(): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: T) {
        this.value = value
    }

    fun set(value: T) {
        this.value = value
    }

    fun reset() {
        this.value = default
    }
}