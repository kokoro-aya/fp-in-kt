package fpinkotlin.chap06_optionals

val parseWithRadix: (Int) -> (String) -> Int =
    { radix -> { string -> Integer.parseInt(string, radix) } }

val parseHex: (String) -> Int = parseWithRadix(16)

fun <A, B, C> map2(a: Optional<A>, b: Optional<B>, func: (A) -> (B) -> C): Optional<C> =
    a.flatMap { x -> b.map { y -> func(x)(y) } }