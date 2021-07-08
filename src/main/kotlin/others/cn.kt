
val array = mapOf(
    "a" to mapOf(
        "aa" to 1, "ab" to 2
    ),
    "b" to mapOf(
        "ba" to 2, "bc" to 3
    ),
    "c" to mapOf(
        "ca" to 3, "cd" to 4
    )
)

val three_level_array = mapOf(
    "a" to mapOf(
        "aa" to mapOf(
            "aaa" to 1,
            "aab" to 2
        ),
        "ab" to mapOf(
            "aba" to 2,
            "abb" to 3
        ),
    ),
    "b" to mapOf(
        "ba" to mapOf(
            "baa" to 3,
            "bab" to 4
        )
    ),
    "c" to mapOf(
        "ca" to mapOf(
            "caa" to 5,
            "cab" to 6
        ),
        "cb" to mapOf(
            "caa" to 7,
            "cab" to 7
        ),
        "cc" to mapOf(
            "caa" to 7,
            "cab" to 8
        ),
    )
)

@JvmName("extract1")
fun <T, U, V> extract(map: Map<T, Map<U, V>>): List<V> {
    return map.values.flatMap { it.values }
}

@JvmName("extract2")
fun <T, U, V, W, X, Y> extract(
    map: Map<T, Map<U, Map<V, Map<W, Map<X, Y>>>>>
): List<Y> =
    map.values
        .flatMap { it.values }
        .flatMap { it.values }
        .flatMap { it.values }
        .flatMap { it.values }

@JvmName("extract3")
fun <T, U> extractMap(map: Map<T, U>): List<Any> {
    return if (map.values.first() is Map<*, *>)
        map.values.map { m -> extractMap(m as Map<Any, Any>)
        }.flatten()
    else
        map.values.toList() as List<Any>
}

fun extractMapWith(map: Map<Any, Any>, depth: Int): List<Any?> {
     fun extractMapWith_(lastStep: Collection<Any>, depth: Int): List<Any> {
        return if (depth > 1)
            extractMapWith_(lastStep.map { m -> (m as Map<Any, Any>).values }
                .flatten(), depth - 1)
        else
            lastStep.toList()
    }
    return extractMapWith_(map.values, depth)
}

fun main() {
//    extractMap(three_level_array).forEach(::println)
    extractMapWith(three_level_array as Map<Any, Any>, 1).forEach(::println)

    println()

    extractMapWith(three_level_array as Map<Any, Any>, 2).forEach(::println)

    println()

    extractMapWith(three_level_array as Map<Any, Any>, 3).forEach(::println)
}