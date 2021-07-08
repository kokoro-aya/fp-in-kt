package fpinkotlin.chap03_functions

data class Product(val name: String, val price: Price, val weight: Weight)

// Value Types
data class Price private constructor(private val value: Double) {
    operator fun plus(price: Price) = Price(this.value + price.value)
    operator fun times(num: Int) = Price(this.value * num)

    override fun toString(): String = value.toString()

    companion object {
        val identity = Price(0.0)

        operator fun invoke(value: Double) =
            if (value > 0)
                Price(value)
            else
                throw IllegalArgumentException("Price must be positive or null")
    }
}
data class Weight private constructor(private val value: Double) {
    operator fun plus(weight: Weight) = Weight(this.value + weight.value)
    operator fun times(num: Int) = Weight(this.value * num)

    override fun toString(): String = value.toString()

    companion object {
        val identity = Weight(0.0)

        operator fun invoke(value: Double) =
            if (value > 0)
                Weight(value)
            else
                throw IllegalArgumentException("Weight must be positive or null")

    }
}

data class OrderLine(val product: Product, val count: Int) {
    fun weight() = product.weight * count
    fun amount() = product.price * count
}

object Store {
    @JvmStatic
    fun main(args: Array<String>) {
        val toothPaste = Product("Tooth paste", Price(1.5), Weight(0.5))
        val toothBrush = Product("Tooth brush", Price(3.5), Weight(0.3))
        val orderLines = listOf(
            OrderLine(toothPaste, 2),
            OrderLine(toothBrush, 3))
//        val weight = orderLines.sumOf { it.amount() }
//        val price = orderLines.sumOf { it.weight() }
        val weight = orderLines.fold(Weight.identity) { e, f ->  e + f.weight() }
        val price = orderLines.fold(Price.identity) { e, f -> e + f.amount() }
        println("Total price: $price")
        println("Total weight: $weight")
    }
}