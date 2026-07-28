package com.example.domain.util

import com.example.domain.model.OrderSummary
import com.example.domain.model.RestaurantConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ThermalPrinterSimulator {

    fun formatThermalReceiptText(order: OrderSummary, config: RestaurantConfig, paperWidthMm: Int = 80): String {
        val width = if (paperWidthMm == 58) 32 else 48
        val divider = "-".repeat(width)
        val doubleDivider = "=".repeat(width)

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.createdAt))
        val sb = StringBuilder()

        sb.appendLine(centerText(config.restaurantName, width))
        sb.appendLine(centerText(config.address, width))
        sb.appendLine(centerText("TEL: ${config.phone}", width))
        sb.appendLine(centerText("GSTIN: ${config.gstin}", width))
        sb.appendLine(doubleDivider)

        sb.appendLine(formatTwoColumns("ORDER: ${order.orderNumber}", "DATE: $dateStr", width))
        sb.appendLine(formatTwoColumns("TYPE: ${order.orderType.label}", "TABLE: ${order.tableNumber ?: "N/A"}", width))
        sb.appendLine(formatTwoColumns("CUST: ${order.customerName ?: "Walk-in"}", "STATUS: ${order.paymentStatus}", width))
        sb.appendLine(divider)

        sb.appendLine(formatItemHeader(width))
        sb.appendLine(divider)

        for (item in order.items) {
            val name = item.menuItem.name
            val qty = "${item.quantity}x"
            val price = "${config.currencySymbol}${String.format("%.2f", item.subtotal)}"
            sb.appendLine(formatItemRow(name, qty, price, width))
            if (item.specialNotes.isNotBlank()) {
                sb.appendLine("  * Note: ${item.specialNotes}")
            }
        }

        sb.appendLine(divider)
        sb.appendLine(formatTwoColumns("SUBTOTAL:", "${config.currencySymbol}${String.format("%.2f", order.subtotal)}", width))
        if (order.discount > 0) {
            sb.appendLine(formatTwoColumns("DISCOUNT:", "-${config.currencySymbol}${String.format("%.2f", order.discount)}", width))
        }
        sb.appendLine(formatTwoColumns("GST TAX (5%):", "${config.currencySymbol}${String.format("%.2f", order.taxAmount)}", width))
        sb.appendLine(doubleDivider)
        sb.appendLine(formatTwoColumns("TOTAL AMOUNT:", "${config.currencySymbol}${String.format("%.2f", order.totalAmount)}", width))
        sb.appendLine(doubleDivider)

        sb.appendLine()
        sb.appendLine(centerText("*** THANK YOU FOR VISITING ***", width))
        sb.appendLine(centerText("POWERED BY SMARTPOS", width))
        sb.appendLine("\n\n\n")

        return sb.toString()
    }

    private fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val padding = (width - text.length) / 2
        return " ".repeat(padding) + text
    }

    private fun formatTwoColumns(left: String, right: String, width: Int): String {
        val available = width - right.length
        return if (left.length >= available) {
            left.take(available - 1) + " " + right
        } else {
            left + " ".repeat(available - left.length) + right
        }
    }

    private fun formatItemHeader(width: Int): String {
        val qtyWidth = 5
        val priceWidth = 10
        val nameWidth = width - qtyWidth - priceWidth
        return "QTY".padEnd(qtyWidth) + "ITEM".padEnd(nameWidth) + "PRICE".padStart(priceWidth)
    }

    private fun formatItemRow(name: String, qty: String, price: String, width: Int): String {
        val qtyWidth = 5
        val priceWidth = 10
        val nameWidth = width - qtyWidth - priceWidth
        val truncatedName = if (name.length > nameWidth) name.take(nameWidth - 1) + "." else name.padEnd(nameWidth)
        return qty.padEnd(qtyWidth) + truncatedName + price.padStart(priceWidth)
    }
}
