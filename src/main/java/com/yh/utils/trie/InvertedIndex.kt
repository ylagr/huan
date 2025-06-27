package com.yh.utils.trie

class InvertedIndex<K, V : IfCanPart<SubV>, SubV> {
    private val index = mutableMapOf<K, V>()
    private val invertIndex = mutableMapOf<SubV, MutableSet<K>>()

    fun save(key: K, value: V) {
        index[key] = value
        val subVPart = value.part<SubV>()
        subVPart.forEach {
            invertIndex.computeIfAbsent(it) { mutableSetOf<K>() }.add(key)
        }
    }

    fun findByKey(key: K): V? {
        return index[key]
    }

    fun findByValue(subV: SubV): Set<K>? {
        return invertIndex[subV]
    }
}


interface IfCanPart<T> {
    fun <T> part(): Array<T>

}