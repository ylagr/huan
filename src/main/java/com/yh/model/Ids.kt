package com.yh.model

import com.yh.debug
import java.util.*

fun Ids.toZigenList(
    zigenMap: Map<CharPart, Zigen>,
    cjkMap: Map<Cjk, List<IdsChai>>,
    extendChaiCtx: ChaiCtx
): List<Zigen> {
    val zigenList = mutableListOf<Zigen>()
    this.idsTree.forEach { node ->
        if (node is Ids.IdsNode) {
            return@forEach;
        }
        if (node is Ids.CharNode) {
            val z = zigenMap[node.char]
            if (z == null) {
                // cycle parse
                val nz = extendChaiCtx.zigenMap[node.char]
                if (nz != null) {
                    zigenList.add(nz)
                    return@forEach;
                }
                val nb = cjkMap[node.char.toCjk()]
                if (nb == null) {
                    println("error : ${node.char} can't find zigen set")
                    return@forEach
                }
                // real code
                val chaiStack = Stack<Chai>()
                nb.get(0)
                while (chaiStack.isNotEmpty()) {

                }
                return@forEach
            } else {
                zigenList.add(z)
            }
            return@forEach;
        }
        throw Exception("error: un support node type: $node ")
    }

    return zigenList
}

data class Ids(val char: Cjk, val idsStr: String, val idsTree: IdsTree = IdsTree(idsStr)) {
    fun revert(charPart: CharPart): Ids {
        val ids = Ids(this.char, this.idsStr)
        ids.idsTree.revert(charPart)
        return Ids(char, ids.idsTree.toIdsStr())
    }

    data class IdsTree(val idsStr: String) : Iterable<Node> {
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

        private var locate: String? = null
        private var rootNode: Node? = null
        private val nodeList: MutableList<Node> = mutableListOf()
        fun getRootNode(): Node? {
            return rootNode
        }

        fun getLocate(): String? {
            return locate
        }

        init {
            val idsStrList = idsPattern.findAll(idsStr)
                .map { it.value }
                .toList()
            if (debug) {
                println(idsStrList)
            }
            idsStrList.forEach { it ->
                if (it.startsWith("[")) {
                    val locate = locatePattern.find(idsStr)
                    this.locate = locate?.groups["locate"]?.value
                    return@forEach
                } else {
                    val cjk = it.toCjk()
                    if (rootNode == null) {
                        if (identifierMap.contains(cjk)) {
                            rootNode = IdsNode(cjk)
                        } else {
                            rootNode = CharNode(it.toCharPart())
                        }
                        nodeList.add(rootNode!!)
                    } else {
                        var curNode: Node
                        if (identifierMap.contains(cjk)) {
                            curNode = IdsNode(cjk)
                        } else {
                            curNode = CharNode(it.toCharPart())
                        }
                        rootNode?.add(curNode)
                        nodeList.add(curNode)
                    }
                }
            }
        }

        override fun iterator(): Iterator<Node> {
            return nodeList.iterator()
        }

        fun revert(charPart: CharPart): IdsTree {
            if (rootNode is IdsNode) {
                (rootNode as IdsNode).revert(charPart)
            }
            return this
        }

        fun toIdsStr(): String {
            val a = rootNode?.run { dfs(this) }
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
    val idsRoot = ids.idsTree.getRootNode()
    println(idsRoot == Ids.IdsNode("⿸".toCjk()))
    val idsFirst = (idsRoot as Ids.IdsNode).children[0]
    println(idsFirst == Ids.CharNode("尸".toCharPart()))
    val idsSecond = (idsRoot as Ids.IdsNode).children[1]
    println(idsSecond == Ids.IdsNode("⿺".toCjk()))
    val idsSecondFirst = (idsSecond as Ids.IdsNode).children[0]
    println(idsSecondFirst == Ids.CharNode("{㢟右}".toCharPart()))
    val idsSecondSecond = (idsSecond as Ids.IdsNode).children[1]
    println(idsSecondSecond == Ids.CharNode("辶".toCharPart()))
    println(ids.idsTree.getLocate() == "[U]")
    println(ids.revert("辶".toCharPart()).idsStr == idsStr2)

}

fun check1() {
    val idsStr = "⿺⿱火⿰\uD840\uDC87十辶[HTJK]"
    val idsStr2 = "⿺辶⿱火⿰\uD840\uDC87十[HTJK]"
    val ids = Ids("䢠".toCjk(), idsStr)
    val idsRoot = ids.idsTree.getRootNode()
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
    println(ids.idsTree.getLocate() == "[HTJK]")
    println(ids.revert("辶".toCharPart()).idsStr == idsStr2)

}
