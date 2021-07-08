package fpinkotlin.chap01_introduction

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DonutShopKtTest {
    @Test
    fun testBuyDonuts() {
        val creditCard = CreditCard("25533578149", 1234)
        val purchase = buyDonuts(5, creditCard)
        assertEquals(Donut.price * 5, purchase.payment.amount)
        assertEquals(creditCard, purchase.payment.creditCard)
    }
}