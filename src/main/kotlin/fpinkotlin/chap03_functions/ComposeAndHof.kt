package fpinkotlin.chap03_functions

fun square(n: Int) = n * n
fun triple(n: Int) = n * 3

// Ex 3.1
fun compose(f: (Int) -> Int, g: (Int) -> Int): (Int) -> Int = { x ->
    f(g(x))
}

// Ex 3.2
fun <T, U, V> compose1(f: (U) -> V, g: (T) -> U): (T) -> V = { f(g(it)) }

// Ex 3.3
//fun add(a: Int): (Int) -> Int = { b ->
//    a + b
//} // wrong as fun is not val (lambda)

//val addd = ::add
val add: (Int) -> (Int) -> Int = { a -> { b -> a + b }} // correct answer

//fun main() {
//    println(add(3)(5))
//}

// Ex 3.4
val compose2: ((Int) -> Int) -> ((Int) -> Int) -> ((Int) -> Int) = {
        f -> {
            g -> {
                x -> f(g(x))
        }
    }
}

// Ex 3.5
//class PolyCompose <T, U, V> {
//    val compose: ((U) -> V) -> ((T) -> U) -> ((T) -> V) = {
//        f -> {
//            g -> {
//                x -> f(g(x))
//            }
//        }
//    }
//}

fun <T, U, V> higherCompose(): ((U) -> V) -> ((T) -> U) -> (T) -> V =
    { f ->
        { g ->
            { x -> f(g(x)) }
        }
    }

fun <T, U, V> higherAndThen(): ((T) -> U) -> ((U) -> V) -> (T) -> V =
    { g ->
        { f ->
            { x -> f(g(x)) }
        }
    }