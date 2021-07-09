package fpinkotlin.chap11_self_balancing_trees

import fpinkotlin.chap07_exceptions.Result
import fpinkotlin.chap08_advanced_lists.*

class IMap<out K: Comparable<@UnsafeVariance K>, V>(private val delegate: Tree<IMapEntry<Int, MList<Pair<K, V>>>> = Tree()) {

    private fun getAll(key: @UnsafeVariance K): Result<MList<Pair<K, V>>> =
        delegate[IMapEntry(key.hashCode())]
            .flatMap { x -> x.value.map { lt -> lt.map { it } } }

    operator fun plus(entry: Pair<@UnsafeVariance K, V>): IMap<@UnsafeVariance K, V> {
        val list = getAll(entry.first).map { lt ->
            lt.foldLeft(MList(entry)) { lst ->
                { pair ->
                    if (pair.first == entry.first) lst else lst.cons(pair)
                }
            }
        }.getOrElse(MList(entry))
        return IMap(delegate + IMapEntry.of(entry.first.hashCode(), list))
    }

    operator fun minus(key: @UnsafeVariance K): IMap<@UnsafeVariance K, V> {
        val list = getAll(key).map { lt ->
            lt.foldLeft(MList()) { lst: MList<Pair<K, V>> ->
                { pair ->
                    if (pair.first == key) lst else lst.cons(pair)
                }
            }
        }.getOrElse(MList())
        return when {
            list.isEmpty() -> IMap(delegate - IMapEntry(key.hashCode()))
            else -> IMap(delegate + IMapEntry.of(key.hashCode(), list))
        }
    }

    fun contains(key: @UnsafeVariance K): Boolean = getAll(key)
        .map { list ->
            list.exists { pair ->
                pair.first == key
            }
        }.getOrElse(false)

    operator fun get(key: @UnsafeVariance K): Result<Pair<K, V>> = getAll(key)
        .flatMap { list ->
            list.filter { pair ->
                pair.first == key
            }.headSafe()
        }

    fun isEmpty(): Boolean = delegate.isEmpty
    val size: Int get() = delegate.size

    override fun toString() = delegate.toString()

    fun values(): MList<V> =
        sequence(delegate.foldInReverseOrder(MList<Result<V>>()) { lst1 ->
            { im ->
                { lst2 ->
                    lst2.concat(lst1.concat(im.value.map { it.map { Result(it.second) } }.getOrElse(MList())))
                }
            }
        }).getOrElse(MList())

    companion object {
        operator fun invoke(): IMap<Nothing, Nothing> = IMap()
    }
}

class IMapEntry<K: Any, V> // Annotating @UnsafeVariance will cause internal error in equals impl.
    private constructor(private val key: K, val value: Result<V>): Comparable<IMapEntry<K, V>> {
    override fun compareTo(other: IMapEntry<K, V>): Int =
        hashCode().compareTo(other.hashCode())

    override fun toString(): String = "MapEntry($key, $value)"

    override fun equals(other: Any?): Boolean =
        this === other || when (other) {
            is IMapEntry<*, *> -> key == other.key
            else -> false
        }

    override fun hashCode(): Int = key.hashCode()


    companion object {
        fun <K: Comparable<K>, V> of(key: K, value: V): IMapEntry<K, V> =
            IMapEntry(key, Result(value))

        operator fun <K: Comparable<K>, V> invoke(pair: Pair<K, V>): IMapEntry<K, V> =
            IMapEntry(pair.first, Result(pair.second))

        operator fun <K: Comparable<K>, V> invoke(key: K): IMapEntry<K, V> =
            IMapEntry(key, Result())
    }
}

fun main(args: Array<String>) {
    val map = IMap<Char, String>() + ('a' to "alice") + ('b' to "Bob") + ('c' to "Carol")

    println(map['a'])
    println(map['b'])
    println(map['d'])
}