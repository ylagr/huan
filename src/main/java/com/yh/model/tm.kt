package com.yh.model

import com.yh.model.TmChaiConsts.linePattern
import com.yh.model.TmChaiConsts.splitPattern
import java.io.*
import java.nio.charset.StandardCharsets

object TmChaiConsts {
    val linePattern =
        """(?<unicode>U\+[^$blankPattern]+)[$blankPattern]+(?<zhChar>.)[$blankPattern]+(?<chai>.+)""".toRegex()
    val splitPattern =
        """[$blankPattern]+""".toRegex()
}

data class TmChai(val char: Cjk, val chai: String, val ids: Ids = mkIds(char, chai)) {
    companion object {
        fun mkIds(char: Cjk, chai: String): Ids {
//            print(char.char)
            return Ids(char, chai)
        }
    }

}

data class Tm(val line: String) {
    val cjk: Cjk
    val chai: List<TmChai>
    fun toStr(newChai: List<TmChai>): String {
        return """${cjk.unicode} ${cjk.char} ${newChai.joinToString(" ") { it.ids.idsStr }}"""
    }

    init {
        val matchResult = linePattern.matchEntire(line)
        if (matchResult != null) {
            val unicode = matchResult.groups["unicode"]
            val zhChar = matchResult.groups["zhChar"]
            val chaiStr = matchResult.groups["chai"]
//            println(
//                """$unicode
//                $zhChar
//                $chaiStr
//                """
//            )
            cjk = Cjk.ofUnicode(unicode?.value!!)
            chai = chaiStr?.value?.split(splitPattern)!!.map { TmChai(cjk, it) }
        } else {
            println("解析错误:$line")
            throw Exception("解析错误:$line")
        }
    }
}

class TmChaiCtx(val tmList: List<Tm>) {

}

fun main() {
    val unin = 0x2CFB0
    val uni = Character.UnicodeBlock.of(unin)
    println(uni)
    val unis = Character.UnicodeScript.of(unin)
    println(unis)
    val s = unin.toString(16).uppercase()
    println(s)
    val a = Character.toString(unin)
    println(a)
    a.codePoints().forEach { println(it) }

    classLoader.getResourceAsStream("天码-递进拆分.txt")?.use {
        val tmList = mutableListOf<Tm>()
        BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8)).lines()?.forEach {
            tmList.add(Tm(it.trim()))
        }
        val tmChaiCtx = TmChaiCtx(tmList)

        PrintWriter(OutputStreamWriter(FileOutputStream("天码-revert.txt"))).use {

            for (tm in tmChaiCtx.tmList) {
                val newChai = tm.chai.map {
                    val nIds = it.ids.revert("辶".toCharPart())
                        .revert("廴".toCharPart())
                    TmChai(tm.cjk, nIds.idsStr, nIds)
                }
                it.println(tm.toStr(newChai))
            }
        }
        val a = 1
    }

}