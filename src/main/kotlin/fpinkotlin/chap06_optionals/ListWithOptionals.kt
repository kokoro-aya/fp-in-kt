package fpinkotlin.chap06_optionals

import fpinkotlin.chap05_lists.IList
import fpinkotlin.chap05_lists.map

//fun <A> sequence(list: IList<Optional<A>>): Optional<IList<A>> =
//    list.coFoldRight(Optional(IList())) { x ->
//        { y: Optional<IList<A>> ->
//            map2(x, y) { a ->
//                { b: IList<A> -> b.cons(a) }
//            }
//        }
//    } // I have given up for this...

fun <A, B> traverse(list: IList<A>, func: (A) -> Optional<B>): Optional<IList<B>> =
    list.coFoldRight(Optional(IList())) { x ->
        { y: Optional<IList<B>> ->
            map2(func(x), y) { a ->
                { b: IList<B> -> b.cons(a) }
            }
        }
    } // modified directly from the last func

fun <A> sequence(list: IList<Optional<A>>): Optional<IList<A>> =
    traverse(list) { it }

fun main(args: Array<String>) {
    val parseWithRadix: (Int) -> (String) -> Int = { radix ->
        { string ->
            Integer.parseInt(string, radix)
        }
    }

    val parse16 = hLift(parseWithRadix(16))
    val list = IList("4", "5", "6", "7", "8", "9", "A", "B")
    val result = sequence(list.map(parse16))

    println(result)
}