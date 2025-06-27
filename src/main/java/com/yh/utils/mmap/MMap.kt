package com.yh.utils.mmap

import java.io.File
import java.io.RandomAccessFile
import java.io.Serial
import java.io.Serializable
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.random.Random


class MMap<T>(path: String) {
    val mmapBuffer: MappedByteBuffer
    val file: File = File(path)

    init {
        file.createNewFile()
        mmapBuffer = RandomAccessFile(file, "rw").getChannel()
            .map(FileChannel.MapMode.READ_WRITE, 0, 1024 * 1024 * 100)
    }

    //    private val file = File(path);
    fun <T> read(): T? {
//        return mmapBuffer.load()
        return null
    }

    fun <T> write(obj: T) {

    }
}

fun main() {
    val seed = 1635315516541665351L
    val mmap = MMap<Integer>("mmap.txt")
    print("")
    val sb = StringBuilder()
    println(' '.code)
    println('z'.code)
    println('0'.code)

    println('Z'.code)
    println(UShort.MAX_VALUE)
    println(Char(65534))
    val random = Random(seed);
    for (i in 0..10_0000) {
//        sb.append(random.nextUInt(65535u).toString())
        val b = 32..122
        sb.append(b.random(random).toChar())
    }
    val d = Data(sb.toString())
    mmap.write(d)

}


class Data(var text: String) : Serializable {
    private companion object {
        @Serial
        const val serialVersionUID: Long = -6430539691155161871L
    }
}

interface MMapWrite {

}

interface MMapRead {
    fun read(): Array<String>
}