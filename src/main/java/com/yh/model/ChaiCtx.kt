package com.yh.model

class ChaiCtx() {
    val zigenMap: Map<CharPart, Zigen>
    private val zigenMutMap = mutableMapOf<CharPart, Zigen>()

    init {
        this.zigenMap = zigenMutMap
        this.add("我", "t")
        this.add("耂", "j")
    }

    private fun add(zigen: String, bianma: String) {
        val zigenCharPart = zigen.toCharPart()
        zigenMutMap.put(zigenCharPart, Zigen(bianma.toBianma(), zigenCharPart))
    }

}