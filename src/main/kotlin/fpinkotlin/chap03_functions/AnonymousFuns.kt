package fpinkotlin.chap03_functions

val f: (Double) -> Double = { Math.PI / 2 - it }
val sin: (Double) -> Double = Math::sin
val cos: Double = compose1(f, sin)(2.0)

val cosValue: Double =
    compose1({ x: Double -> Math.PI / 2 - x }, Math::sin)(2.0)

val higherCos = higherCompose<Double, Double, Double>()()
    { x: Double -> Math.PI / 2 - x }(Math::sin)

val higherCosValue = higherCos(2.0)

fun <A, B, C> partialAp(a: A, func: (A) -> (B) -> C): (B) -> C = func(a)

fun <A, B, C> partialAp2(b: B, func: (A) -> (B) -> C): (A) -> C =
    { a ->
            func(a)(b)
    }

fun <A, B, C, D> curriedFunc(): (A) -> (B) -> (C) -> (D) -> String =
    { a ->
        { b ->
            { c ->
                { d ->
                    "$a, $b, $c, $d"
                }
            }
        }
    }

fun <A, B, C> curry(func: (A, B) -> C): (A) -> (B) -> C =
    { a ->
        { b ->
            func(a, b)
        }
    }

fun <A, B, C> swapArgs(func: (A) -> (B) -> C): (B) -> (A) -> C =
    { b ->
        { a ->
            func(a)(b)
        }
    }

fun <T> id() = { x: T -> x }