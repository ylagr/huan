package com.yh.model

fun String.toCjk(): Cjk {
    return Cjk.ofChar(this)
}

fun Int.toCjk(): Cjk {
    return Cjk.ofCodePoint(this)
}


data class Cjk(
    val char: String,
    val uniInt: Int = char.codePointAt(0),
    val uniStr: String = uniInt.toString(16),
    val unicode: String = "U+${uniInt.toString(16).uppercase()}"
) {
    fun unicodeName(): String {
        return Character.getName(uniInt)
    }

    fun unicodeBlock(): Character.UnicodeBlock {
        return Character.UnicodeBlock.of(uniInt)
    }

    fun itsPrivateUseArea(): Boolean {
        return Character.UnicodeBlock.of(uniInt) == Character.UnicodeBlock.PRIVATE_USE_AREA
    }

    companion object {
        val unicodePattern = """U\+[0-9a-fA-F]+""".toRegex()
        fun ofCodePoint(codePoint: Int): Cjk {
            return Cjk(Character.toString(codePoint), codePoint)
        }

        fun ofUnicode(unicode: String): Cjk {
            if (unicodePattern.matches(unicode)) {
                val uniStr = unicode.substring(2)
                val codePoint = uniStr.toInt(16)
                return Cjk(Character.toString(codePoint), codePoint, uniStr, unicode)
            } else {
                throw Exception("unicode格式错误")
            }
        }

        fun ofChar(char: String): Cjk {
            return Cjk(char)
        }

    }
}

