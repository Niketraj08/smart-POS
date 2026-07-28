package com.example

import com.example.domain.model.CartItem
import com.example.domain.model.MenuItemModel
import com.example.domain.model.OrderSummary
import com.example.domain.model.OrderStatus
import com.example.domain.model.OrderType
import com.example.domain.model.RestaurantConfig
import com.example.domain.util.ThermalPrinterSimulator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartPosUnitTest {

    @Test
    fun cartItem_subtotalAndGstMath_isCorrect() {
        val item = MenuItemModel(
            id = 1,
            categoryId = 1,
            name = "Truffle Pasta",
            description = "Gourmet Pasta",
            price = 20.0,
            gstPercentage = 5.0
        )

        val cartItem = CartItem(menuItem = item, quantity = 2)

        assertEquals(40.0, cartItem.subtotal, 0.01)
        assertEquals(2.0, cartItem.gstAmount, 0.01)
        assertEquals(42.0, cartItem.totalWithGst, 0.01)
    }

    @Test
    fun thermalReceipt_formattingContainsHeader() {
        val config = RestaurantConfig(
            restaurantName = "SmartPOS Bistro",
            address = "123 Main St",
            phone = "+1 555-0192",
            gstin = "GST12345"
        )

        val order = OrderSummary(
            id = "ORD-1",
            orderNumber = "ORD-1001",
            tableNumber = "T-01",
            orderType = OrderType.DINE_IN,
            status = OrderStatus.PENDING,
            subtotal = 100.0,
            discount = 10.0,
            taxAmount = 4.5,
            totalAmount = 94.5
        )

        val receiptText = ThermalPrinterSimulator.formatThermalReceiptText(order, config, paperWidthMm = 80)

        assertTrue(receiptText.contains("SmartPOS Bistro"))
        assertTrue(receiptText.contains("ORD-1001"))
        assertTrue(receiptText.contains("GST12345"))
    }
}
