package fpinkotlin.chap06_optionals

import kotlin.math.pow

val mean: (List<Double>) -> Optional<Double> = { list -> when {
        list.isEmpty() -> Optional()
        else -> Optional((list.sum() / list.size))
    }
}

val variance: (List<Double>) -> Optional<Double> = { list ->
    mean(list).flatMap { m ->
        mean(list.map { x ->
            (x - m).pow(2)
        })
    }
}