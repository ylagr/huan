package com.yh.model

fun String.toCharPart(): CharPart {
    return CharPart(this)
}

fun String.toBianMa(): Bianma {
    return Bianma(this)
}

data class CharPart(val value: String) {}

data class Bianma(val value: String) {}

data class Zigen(val bianma: Bianma, val charPart: CharPart) {}