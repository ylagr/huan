package com.yh.model

import java.util.*


data class Ids(val char: Cjk, val idsStr: String, val idsTree: IdsTree = IdsTree(idsStr)) {
    fun revert(charPart: CharPart): Ids {
        val ids = Ids(this.char, this.idsStr)
        ids.idsTree.revert(charPart)
        return Ids(char, ids.idsTree.toIdsStr())
    }

    data class IdsTree(val idsStr: String) {
        companion object {
            val identifierMap: Map<Cjk, Byte> = mapOf(
                "⿰".toCjk() to 2,
                "⿱".toCjk() to 2,
                "⿲".toCjk() to 3,
                "⿳".toCjk() to 3,
                "⿴".toCjk() to 2,
                "⿵".toCjk() to 2,
                "⿶".toCjk() to 2,
                "⿷".toCjk() to 2,
                "⿸".toCjk() to 2,
                "⿹".toCjk() to 2,
                "⿺".toCjk() to 2,
                "⿻".toCjk() to 2,
            )

            //(?<!\[)(?!\[.*])
            val idsPattern = """(?:\[.*?]|([^\{}])|(\{[^}]+}))""".toRegex()
            val locatePattern = """(?<locate>\[.*?])""".toRegex()
        }

        var locate: String? = null
        var idsTreeNode: Node? = null

        init {
            val idsStrList = idsPattern.findAll(idsStr)
                .map { it.value }
                .toList()
            println(idsStrList)
            idsStrList.forEach { it ->
                if (it.startsWith("[")) {
                    val locate = locatePattern.find(idsStr)
                    this.locate = locate?.groups["locate"]?.value
                    return@forEach
                } else {
                    val cjk = it.toCjk()
                    if (idsTreeNode == null) {
                        if (identifierMap.contains(cjk)) {
                            idsTreeNode = IdsNode(cjk)
                        } else {
                            idsTreeNode = CharNode(it.toCharPart())
                        }
                    } else {
                        if (identifierMap.contains(cjk)) {
                            idsTreeNode?.add(IdsNode(cjk));
                        } else {
                            idsTreeNode?.add(CharNode(it.toCharPart()))
                        }
                    }
                }
            }
        }

        fun revert(charPart: CharPart): IdsTree {
            if (idsTreeNode is IdsNode) {
                (idsTreeNode as IdsNode).revert(charPart)
            }
            return this
        }

        fun toIdsStr(): String {
            val a = idsTreeNode?.run { dfs(this) }
            locate?.let {
                a?.append(it)
            }
            return a.toString()
        }

        fun dfs(node: Node): StringBuilder {
            val sb = StringBuilder()
            val stack = Stack<Node>()
            stack.push(node)
            while (!stack.isEmpty()) {
                val node = stack.pop()
                if (node is IdsNode) {
                    for (i in node.children.lastIndex downTo 0) {
                        stack.push(node.children[i])
                    }
                    //                    if (node.second != null) {
//                        stack.push(node.second)
//                    }
//
//                    if (node.first != null) {
//                        stack.push(node.first)
//                    }
                }
                sb.append(node.toStr())
            }
            return sb
        }

    }

    interface Node {
        fun add(node: Node);
        fun toStr(): String;
    }

    data class IdsNode(val identifier: Cjk, val size: Byte = IdsTree.identifierMap[identifier]!!) : Node {
        //        var first: Node? = null;
//        var second: Node? = null;
        val children = mutableListOf<Node>()
        var previous: Node? = null
        var full: Boolean = false
        fun revert(charPart: CharPart) {
            if (children.size > 1) {
                val charNode = CharNode(charPart)
                val indexOf = children.indexOf(charNode)
                if (indexOf >= 1) {
                    children.removeAt(indexOf)
                    children.add(0, charNode)
                }
                children.forEach {
                    if (it is IdsNode) {
                        it.revert(charPart)
                    }
                }
            }
//            if (first is CharNode && second != null) {
//                if (((first as CharNode).char == charPart)) {
//                    val tmp = first;
//                    first = second;
//                    second = tmp;
//                    return
//                }
//            }
//            if (second is CharNode) {
//                if (((second as CharNode).char == charPart)) {
//                    val tmp = first;
//                    first = second;
//                    second = tmp;
//                    return
//                }
//            }
//            if (first is IdsNode) {
//                (first as IdsNode).revert(charPart)
//            }
//            if (second is IdsNode) {
//                (second as IdsNode).revert(charPart)
//            }
        }

        override fun add(node: Node) {
            if (children.isEmpty()) {
                children.add(node)
                return;
            }

            val indices = children.indices;
            for (i in indices) {
                val child = children[i]
                if (child is IdsNode) {
                    if (!child.full) {
                        child.previous = this
                        child.add(node)
                        return
                    }
                }
            }

//            if (first == null) {
//                first = node;
//                return;
//            }

//            if (first is IdsNode) {
//                if (!(first as IdsNode).full) {
//                    (first as IdsNode).previous = this
//                    (first as IdsNode).add(node)
//                    return
//                }
//            }
//            if (second == null) {
//                second = node;
//                return;
//            }
//            if (second is IdsNode) {
//                if (!(second as IdsNode).full) {
//                    (second as IdsNode).add(node)
//                    (second as IdsNode).previous = this
//                    return;
//                }
//            }
//            full = true
            if (size > children.size.toByte()) {
                children.add(node)
                return
            }
            if (size == children.size.toByte()) {
                full = true
            }
            previous?.add(node)
        }

        override fun toStr(): String {
            return identifier.char
        }
    }

    data class CharNode(val char: CharPart) : Node {
        override fun add(node: Node) {

        }

        override fun toStr(): String {
            return char.value
        }
    }


}

fun main() {
    check1()
    check2()
    val ids2 = Ids("⺫".toCjk(), "{柬中}")
    val a = 1

}

fun check3() {

}

fun check2() {
//    𡱦 ⿸尸⿺{㢟右}辶[U] ⿸尸𨑭
    val idsStr = "⿸尸⿺{㢟右}辶[U]"
    val idsStr2 = "⿸尸⿺辶{㢟右}[U]"
    val ids = Ids("\uD847\uDC66".toCjk(), idsStr)
    val idsRoot = ids.idsTree.idsTreeNode
    println(idsRoot == Ids.IdsNode("⿸".toCjk()))
    val idsFirst = (idsRoot as Ids.IdsNode).children[0]
    println(idsFirst == Ids.CharNode("尸".toCharPart()))
    val idsSecond = (idsRoot as Ids.IdsNode).children[1]
    println(idsSecond == Ids.IdsNode("⿺".toCjk()))
    val idsSecondFirst = (idsSecond as Ids.IdsNode).children[0]
    println(idsSecondFirst == Ids.CharNode("{㢟右}".toCharPart()))
    val idsSecondSecond = (idsSecond as Ids.IdsNode).children[1]
    println(idsSecondSecond == Ids.CharNode("辶".toCharPart()))
    println(ids.idsTree.locate == "[U]")
    println(ids.revert("辶".toCharPart()).idsStr == idsStr2)

}

fun check1() {
    val idsStr = "⿺⿱火⿰\uD840\uDC87十辶[HTJK]"
    val idsStr2 = "⿺辶⿱火⿰\uD840\uDC87十[HTJK]"
    val ids = Ids("䢠".toCjk(), idsStr)
    val idsRoot = ids.idsTree.idsTreeNode
    val root = Ids.IdsNode("⿺".toCjk())
    println(idsRoot == root)
    val idsFirst = (idsRoot as Ids.IdsNode).children[0]
    println(idsFirst == Ids.IdsNode("⿱".toCjk()))
    val idsFirstFirst = (idsFirst as Ids.IdsNode).children[0]
    println(idsFirstFirst == Ids.CharNode("火".toCharPart()))
    val idsFirstSecond = idsFirst.children[1]
    println(idsFirstSecond == Ids.IdsNode("⿰".toCjk()))
    val idsFirstSecondFirst = (idsFirstSecond as Ids.IdsNode).children[0]
    println(idsFirstSecondFirst == Ids.CharNode("\uD840\uDC87".toCharPart()))
    val idsFirstSecondSecond = idsFirstSecond.children[1]
    println(idsFirstSecondSecond == Ids.CharNode("十".toCharPart()))
    val idsSecond = idsRoot.children[1]
    println(idsSecond == Ids.CharNode("辶".toCharPart()))
    println(ids.idsTree.locate == "[HTJK]")
    println(ids.revert("辶".toCharPart()).idsStr == idsStr2)

}
