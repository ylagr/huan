package com.yh.model

fun String.toCharPart(): CharPart {
    return CharPart(this)
}

fun String.toBianma(): Bianma {
    return Bianma(this)
}

fun CharPart.toCjk(): Cjk {
    return Cjk.ofChar(this.value)
}

fun Cjk.toCharPart(): CharPart {
    return CharPart(this.char)
}

data class CharPart(val value: String) {}

data class Bianma(val value: String) {}

data class Zigen(val bianma: Bianma, val charPart: CharPart) {}