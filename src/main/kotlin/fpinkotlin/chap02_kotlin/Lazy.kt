package fpinkotlin.chap02_kotlin

import java.io.File

fun main(args: Array<String>) {
    val name: String by lazy { getName() }
    println("hey1")
    val name2: String by lazy { name }
    println("hey2")

    println(name)
    println(name2)
    println(name)
    println(name2)
}

fun getName(): String {
    println("computing name...")
    return "Mickey"
}

val lines: List<String> = File("myFile.txt")
    .inputStream()
    .use {
        it.bufferedReader()
            .lineSequence()
            .toList()
    }

//fun main() {
//    lines.forEach(::println)
//
//    File("myFile.txt").forEachLine { println(it) }
//
//    File("myFile.txt").useLines { it.forEach(::println) }
//}