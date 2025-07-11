package com.yh.model

import com.yh.debug
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


data class ZhenSource(val cjk: Cjk, val bianMa: Bianma) {

}

class ZhenSourceCtx {
    val zhenSourceMap: Map<Cjk, List<ZhenSource>>

    init {
        zhenSourceMap = parse()
    }

    private fun parse(): Map<Cjk, List<ZhenSource>> {
        val zhenSourceMap = mutableMapOf<Cjk, MutableList<ZhenSource>>()
        val zhSource = classLoader.getResourceAsStream("真码-原始码表.txt")
        zhSource?.use {
            BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8)).use {
                val textPattern = """([^$blankPattern/]+)""".toRegex()
                val lines = it.lines()
                if (lines != null) {
                    lines.forEach {
                        val allTexts = textPattern.findAll(it).map { it.value }.toList()
                        zhenSourceMap
                            .computeIfAbsent(allTexts[0].toCjk()) { mutableListOf() }
                            .add(ZhenSource(allTexts[0].toCjk(), allTexts[1].toBianma()))
                    }
                }
            }
        }
        return zhenSourceMap
    }

    fun check(zhenChaiCtx: ZhenChaiCtx) {
        for (entry in this.zhenSourceMap) {
            var checkErr = true
            zhenChaiCtx.zhenZhcharMap[entry.key]?.forEach {

                for (source in entry.value) {
                    if (it.code == source.bianMa.value) {
                        checkErr = false
                    }
                }
            }
            if (checkErr) {
                if (zhenChaiCtx.zigenZhcodeSet.contains(entry.key.char.toCharPart())) {
                    continue
                }
                if (zhenChaiCtx.zhenZhcharMap[entry.key] == null) {
                    println("entry:$entry 汉字拆分缺失")
                    continue
                }
                println(
                    """entry:$entry
                       zhenCtx:${zhenChaiCtx.zhenZhcharMap[entry.key]}
                       zhCode:${zhenChaiCtx.zhenZhcharMap[entry.key]?.get(0)?.code}
                       entryKey:${entry.key}                       
                    """
                )
                throw Exception("原始码表与真码拆分不匹配")
            }
        }
    }
}

class ZhenChaiCtx {
    // 汉字拆分
    val zhenZhcharMap: Map<Cjk, List<Chai>>

    // key: 字根编码
    val zhenZigenEncodeMap: Map<Bianma, Set<Zigen>>

    //  key：字根zhCode
    val zhenZigenZhcodeMap: Map<CharPart, Zigen>

    // value 字根编码
    val zigenEncodeSet: Set<Bianma>

    // value 字根ZhCode
    val zigenZhcodeSet: Set<CharPart>

    init {
        zhenZhcharMap = parse()

        val zhenZigenEncodeMapMut = mutableMapOf<Bianma, MutableSet<Zigen>>()
        val zhenZigenZhcodeMapMut = mutableMapOf<CharPart, Zigen>()
        val zigenEncodeSetMut = mutableSetOf<Bianma>()
        val zigenZhcodeSetMut = mutableSetOf<CharPart>()

        for (entry in zhenZhcharMap) {
            entry.value.forEach { chai ->
                val zhChar = chai.cjk
                if (chai.zigenList.size == 1) {
                    zigenZhcodeSetMut.add(zhChar.char.toCharPart())
                    val zigen = chai.zigenList[0]
                    val old = zhenZigenZhcodeMapMut.put(zhChar.char.toCharPart(), chai.zigenList[0])
                    if (old != null && old != zigen) {
                        println("error: $old zigen repeat")
                    }
                    //                    val zigenSet = zhenZigenZhcodeMapMut
//                        .computeIfAbsent(zhChar.char.toCharPart()) { mutableSetOf<Zigen>() }
//                    zigenSet.add(chai.zhenZigenList[0])
//                    if (zigenSet.size > 1) {
//                        println("err zigen size > 1")
//                    }
                }
                chai.zigenList.forEach { zigen ->
                    zhenZigenEncodeMapMut
                        .computeIfAbsent(zigen.bianma) { mutableSetOf<Zigen>() }
                        .add(zigen)
//                    val zigenSet = zhenZigenZhcodeMapMut
//                        .computeIfAbsent(zigen.charPart) { mutableSetOf<Zigen>() }
//                    zigenSet.add(zigen)
//                    if (zigenSet.size > 1) {
//                        println("err zigen size > 1")
//                    }
                    val old = zhenZigenZhcodeMapMut.put(zigen.charPart, zigen)
                    if (old != null && old != zigen) {
                        println("error: $old zigen repeat")
                    }
                    zigenEncodeSetMut.add(zigen.bianma)
                    zigenZhcodeSetMut.add(zigen.charPart)
                }
            }
        }
        zhenZigenEncodeMap = zhenZigenEncodeMapMut
        zhenZigenZhcodeMap = zhenZigenZhcodeMapMut
        zigenEncodeSet = zigenEncodeSetMut
        zigenZhcodeSet = zigenZhcodeSetMut
    }

    ///    丂	一㇉ [ k x ]
    ///蘒	艹禾⺈{龜下} / 艹禾丿{龜下} [ f td qv px / f td t px ]
    private fun parse(): Map<Cjk, List<Chai>> {
        val chaiMap = mutableMapOf<Cjk, MutableList<Chai>>()
        val zhSplit = classLoader.getResourceAsStream("真码拆分.txt")
        zhSplit?.use {
            BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8)).use {
                val pattern = """
        ^
        [$blankPattern]*
        (?<char>[^$blankPattern]+)               # 第一个字符（非制表符）
        [$blankPattern]*                         # 制表符分隔
        (?<text>[^\]]+[^$blankPattern]*[^\[])    # 文字部分（直到左中括号）
        [$blankPattern]+\[[$blankPattern]*       # 左中括号
        (?<letters>[^\]]+[^$blankPattern]*[^\]]) # 字母组合（直到右中括号）
        [$blankPattern]+\][$blankPattern]*       # 右中括号
        $
    """.trimIndent().toRegex(RegexOption.COMMENTS)
                var hasErr = false
                val lines = it.lines()
                if (lines != null) {
                    val zigenPattern = """([^\{}])|(\{[^}]+})""".trimIndent().toRegex()
                    val textPattern = """([^$blankPattern/]+)""".toRegex()
                    val letterRegex = """([^$blankPattern/]+)""".toRegex()
                    it.lines()?.forEach {
                        val matchResult = pattern.matchEntire(it.trim())

                        if (matchResult != null) {
                            val char = matchResult.groups["char"]?.value ?: ""
                            val text = matchResult.groups["text"]?.value ?: ""
                            val letters = matchResult.groups["letters"]?.value ?: ""
                            if (debug) {
                                println("\n原始字符串: $it")
                                println("字符: '$char'")
                                if (char == "嚇") {
                                    print("char: $char")
                                }
                                println("文字部分: '$text'")
                                println("字母部分: '$letters'")
                            }
                            val allTexts = textPattern.findAll(text).map { it.value }.toList()
                            // 提取所有字母（不依赖split）
                            val allLetters = letterRegex.findAll(letters).map { it.value }.toList()

                            var letterIndex = 0
                            val chaiList = mutableListOf<Chai>()
                            allTexts.forEach {
                                val zhenZigenList = mutableListOf<Zigen>()
                                val zigenStrList = zigenPattern.findAll(it).map { it.value }.toList()
                                for (i in zigenStrList.indices) {
                                    if (debug) {
                                        println("第 $i 个字根=${zigenStrList[i]},第 $i 个字根编码=${allLetters[letterIndex]}")
                                    }
                                    zhenZigenList.add(
                                        Zigen(
                                            allLetters[letterIndex].toBianma(),
                                            zigenStrList[i].toCharPart()
                                        )
                                    )
                                    letterIndex++
                                }
                                chaiList.add(Chai(zhenZigenList, char.toCjk()))
                            }
                            chaiMap[char.toCjk()] = chaiList
                        } else {
                            println("\n!!!!无法解析字符串: $it")
                            hasErr = true
                        }
                    }
                }
                println()
                println(pattern)
                println(blankPattern.toString())
                if (hasErr) {
                    println()
                    throw Exception("解析错误？？？？？")
                } else {
                    println("解析成功")
                }
            }
        }
        return chaiMap
    }
}

fun printCharUnicode(char: Char) {
    println("字符: '$char'")
    println("Unicode 编码: U+${char.code.toString(16).uppercase().padStart(4, '0')}")
    println("Unicode 名称: ${Character.getName(char.code) ?: "未知"}")
}

val blankPattern = """\p{Z}\t\s""".toRegex()

fun checkZhSplit() {
    val line1 = "丂\t一㇉ [ k x ]"
    val line2 = "﨓\t木艹亻寸 [ d f r gy ]"
    val line3 = "蘒\t艹禾⺈{龜下} / 艹禾丿{龜下} [ f td qv px / f td t px ]"
    val line4 = "蘒\t艹禾⺈{龜下} / 艹禾丿{龜下} [ f td qv px / f td t px / f td t px ]"
    val func = { it: String ->
        val pattern = """
        ^
        [$blankPattern]*
        (?<char>[^$blankPattern]+)               # 第一个字符（非制表符）
        [$blankPattern]*                         # 制表符分隔
        (?<text>[^\]]+[^$blankPattern]*[^\[])    # 文字部分（直到左中括号）
        [$blankPattern]+\[[$blankPattern]*       # 左中括号
        (?<letters>[^\]]+[^$blankPattern]*[^\]]) # 字母组合（直到右中括号）
        [$blankPattern]+\][$blankPattern]*       # 右中括号
        $
    """.trimIndent().toRegex(RegexOption.COMMENTS)
        val matchResult = pattern.matchEntire(it.trim())
        if (matchResult != null) {
            val char = matchResult.groups["char"]?.value ?: ""
            val text = matchResult.groups["text"]?.value ?: ""
            val letters = matchResult.groups["letters"]?.value ?: ""

            if (debug) {
                println("\n原始字符串: $it")
                println("字符: '$char'")
                println("文字部分: '$text'")
                println("字母部分: '$letters'")
            }
            val textPattern =
                """([^$blankPattern/]+)""".toRegex()
            val allTexts = textPattern.findAll(text).map { it.value }.toList()
            println(allTexts)
            val lettersPattern =
                """
                    ^(([^$blankPattern][^/])+)
                    |(([^$blankPattern])+)
                    |(([^/][^$blankPattern][^/])+)
                    |(([^/][^$blankPattern])+)
                    $
               """.trimIndent().toRegex()
            val allLetters = lettersPattern.findAll(letters).map { it.value }.toList()
            println("字母部分: '$allLetters'")
        } else {
            println("\n!!!!无法解析字符串: $it")
        }

    }
    func(line1)
    func(line2)
    func(line3)
    println(""".""".toRegex().matches("\uD87A\uDE29"))
    val a =
        """([^\{}])|(\{.+})""".trimIndent().toRegex().findAll("金土\uD87A\uDE29金土{幸下}").map { it.value }.toList()
    println(a)
}

val classLoader = ClassLoader.getSystemClassLoader()
fun main() {
    checkZhSplit()
    val t = " \n"
    println(" ".matches("""\s*\t*\p{Z}*""".toRegex()));
    // zh\tzh
    //    丂	一㇉ [ k x ]
    //蘒	艹禾⺈{龜下} / 艹禾丿{龜下} [ f td qv px / f td t px ]
    val zhenChaiCtx = ZhenChaiCtx()
    println(zhenChaiCtx.zhenZhcharMap.get("真".toCjk()))
    println(zhenChaiCtx.zhenZhcharMap.get("真".toCjk())?.get(0)?.code)
    val zhenSourceCtx = ZhenSourceCtx()
    zhenSourceCtx.check(zhenChaiCtx)
    val a = "\uE85D".toCjk()
    println(a)
    println(a.unicodeBlock())
    println(a.unicodeName())
}