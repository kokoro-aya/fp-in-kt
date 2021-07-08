package fpinkotlin.chap01_introduction

data class CreditCard(val number: String, private val code: Int)

class Donut {
    companion object {
        const val price: Double = 3.5
    }
}

class Payment(val creditCard: CreditCard, val amount: Double) {
    fun combine(payment: Payment): Payment =
        if (creditCard == payment.creditCard) {
            Payment(creditCard, amount + payment.amount)
        } else {
            throw IllegalStateException("Cards don't match.")
        }

    companion object {
        fun groupByCard(payments: List<Payment>): List<Payment> =
            payments.groupBy { it.creditCard }
                .values
                .map { it.reduce(Payment::combine) }
    }
}

data class Purchase(val donut: List<Donut>, val payment: Payment)

//fun buyDonut(creditCard: CreditCard): Purchase {
//    val donut = Donut()
//    val payment = Payment(creditCard, donut.price)
//    return Purchase(donut, payment)
//}

fun buyDonuts(quantity: Int = 1, creditCard: CreditCard): Purchase =
    Purchase(List(quantity) { Donut() },
        Payment(creditCard, Donut.price * quantity))