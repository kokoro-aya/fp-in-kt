package fpinkotlin

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility

class IOCContainer {
    private val container: MutableMap<KClass<*>, Collection<KFunction<*>>> = mutableMapOf()

    fun bind(clazz: KClass<*>) {
        container[clazz] = clazz.constructors.filter { it.visibility != KVisibility.PRIVATE }
    }

    operator fun get(clazz: KClass<*>, vararg arguments: Any): Any {
        val classes = arguments.map { it::class }
        val ctor = container[clazz]
            ?.filter { it.parameters.size == classes.size }
            ?.firstOrNull { ctor ->
                ctor.parameters.map { para -> para.type.classifier }
                    .mapIndexed { i, it -> it == classes[i] }
                    .reduce(Boolean::and)
            } ?: throw Exception("Mismatch arguments: no constructor hit")
        return ctor.call(*arguments) ?: throw Exception("Nullable is unsupported or something else happened")
    }
}

class Foo private constructor(val a: Int, val b: Double, val c: String) {
    constructor(a: Int, b: Int, c: Double, d: String): this(a - b, c, d)
    constructor(a: Int, b: Int, c: Double): this(a + b, c, "foo")
    constructor(mono: Double): this(mono.toInt(), mono, mono.toString())

    override fun toString(): String = "Foo(a=$a, b=$b, c='$c')"
}

fun main() {
    val cont = IOCContainer()
    cont.bind(Foo::class)

    println(cont[Foo::class, 1, 2, 4.50, "05"])

    println(cont[Foo::class, 12, 45, 0.5])

    println(cont[Foo::class, 12.450])

    println(cont[Foo::class, 12, 4.5, "0"])
}