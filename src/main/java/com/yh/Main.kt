package com.yh

import com.yh.model.*
import java.io.*
import java.nio.charset.StandardCharsets

// val debug = true;
val debug = false;
fun main() {
    val tmSourceTxtPath = "天码-递进拆分.txt"
    val tmRevertTxtPath = "天码-revert.txt"
    //revertTm(tmSourceTxtPath, tmRevertTxtPath);
    val zhenChaiCtx = ZhenChaiCtx()
    val check = true
//        val check = false
    if (check) {  // checkParse
        val zhenSourceCtx = ZhenSourceCtx()
        zhenSourceCtx.check(zhenChaiCtx)
    }
    val chaiCtx = readChai(tmRevertTxtPath)
    val extendChaiCtx = ChaiCtx()
    chaiCtx.tmList.forEach { tm ->
        tm.chai.forEach { tmChai ->
            val a = tmChai.ids.toZigenList(
                zhenChaiCtx.zhenZigenZhcodeMap,
                chaiCtx.chaiMap,
                extendChaiCtx
            )

        }
    }
    val a = 1;
}

fun revertTm(sourcePath: String, targetPath: String) {
    classLoader.getResourceAsStream(sourcePath)!!.use {

        val tmChaiCtx = BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8)).lines().map {
            Tm(it.trim())
        }.toList().run { TmChaiCtx(this) }

        PrintWriter(OutputStreamWriter(FileOutputStream(targetPath))).use { pw ->
            tmChaiCtx.tmList.forEach {
                it.chai.map { chai ->
                    val nIds = chai.ids
                        .revert("辶".toCharPart())
                        .revert("廴".toCharPart())
                    IdsChai(it.cjk, nIds.idsStr, nIds)
                }.run { it.toStr(this) }.run { pw.println(this) }
            }
//                for (tm in tmChaiCtx.tmList) {
//                    val newChai = tm.chai.map {
//                        val nIds = it.ids.revert("辶".toCharPart())
//                            .revert("廴".toCharPart())
//                        TmChai(tm.cjk, nIds.idsStr, nIds)
//                    }
//                    it.println(tm.toStr(newChai))
//                }
        }
    }
}

fun readChai(sourcePath: String): TmChaiCtx {
    FileInputStream(sourcePath)!!.run {

        return BufferedReader(InputStreamReader(this, StandardCharsets.UTF_8)).lines().map {
            Tm(it.trim())
        }.toList().run { TmChaiCtx(this) }
    }
}
