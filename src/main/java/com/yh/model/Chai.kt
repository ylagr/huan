package com.yh.model


data class Chai(val zigenList: List<Zigen>, val cjk: Cjk) {
    val code: String

    init {
        val totalLen = 4;
        val codeSb = StringBuilder()
        val firstZigen = zigenList[0]

        codeSb.append(firstZigen.bianma.value)
        for (i in 1..zigenList.size - 2) {
            codeSb.append(zigenList[i].bianma.value.toCharArray()[0])
            if (codeSb.length == totalLen - 1) {
                break;
            }
        }
        val lastIndex = zigenList.size - 1
        if (lastIndex > 0) {
            if (codeSb.length == totalLen - 1) {
                codeSb.append(zigenList[lastIndex].bianma.value.toCharArray()[0])
            } else {
                codeSb.append(zigenList[lastIndex].bianma.value)
            }
        }
        code = codeSb.toString()
    }
}
