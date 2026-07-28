package com.example.domain.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.domain.model.OrderSummary
import com.example.domain.model.RestaurantConfig
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfInvoiceGenerator {

    fun generateInvoicePdf(
        context: Context,
        order: OrderSummary,
        config: RestaurantConfig
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }
        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
        }
        val regularPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }

        var yPos = 50f
        val startX = 40f
        val endX = 555f

        // Restaurant Title
        canvas.drawText(config.restaurantName, startX, yPos, titlePaint)
        yPos += 20f
        canvas.drawText(config.address, startX, yPos, headerPaint)
        yPos += 16f
        canvas.drawText("Phone: ${config.phone}  |  GSTIN: ${config.gstin}", startX, yPos, headerPaint)
        yPos += 25f

        // Divider
        canvas.drawLine(startX, yPos, endX, yPos, Paint().apply { color = Color.GRAY; strokeWidth = 1f })
        yPos += 25f

        // Invoice Header Details
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))
        canvas.drawText("TAX INVOICE / RECEIPT", startX, yPos, boldPaint)
        yPos += 18f
        canvas.drawText("Order ID: ${order.orderNumber}", startX, yPos, regularPaint)
        canvas.drawText("Date: $dateStr", 350f, yPos, regularPaint)
        yPos += 16f
        canvas.drawText("Table: ${order.tableNumber ?: "N/A"} (${order.orderType.label})", startX, yPos, regularPaint)
        canvas.drawText("Customer: ${order.customerName ?: "Walk-in Guest"}", 350f, yPos, regularPaint)
        yPos += 25f

        // Table Header
        canvas.drawLine(startX, yPos, endX, yPos, Paint().apply { color = Color.BLACK; strokeWidth = 1.5f })
        yPos += 20f
        canvas.drawText("Item Description", startX, yPos, boldPaint)
        canvas.drawText("Qty", 320f, yPos, boldPaint)
        canvas.drawText("Rate", 390f, yPos, boldPaint)
        canvas.drawText("Amount", 480f, yPos, boldPaint)
        yPos += 10f
        canvas.drawLine(startX, yPos, endX, yPos, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
        yPos += 20f

        // Items List
        for (item in order.items) {
            canvas.drawText(item.menuItem.name, startX, yPos, regularPaint)
            canvas.drawText("${item.quantity}", 325f, yPos, regularPaint)
            canvas.drawText("${config.currencySymbol}${String.format("%.2f", item.menuItem.price)}", 385f, yPos, regularPaint)
            canvas.drawText("${config.currencySymbol}${String.format("%.2f", item.subtotal)}", 480f, yPos, regularPaint)
            yPos += 18f
        }

        yPos += 10f
        canvas.drawLine(startX, yPos, endX, yPos, Paint().apply { color = Color.GRAY; strokeWidth = 1f })
        yPos += 20f

        // Summary Breakdown
        canvas.drawText("Subtotal:", 350f, yPos, regularPaint)
        canvas.drawText("${config.currencySymbol}${String.format("%.2f", order.subtotal)}", 480f, yPos, regularPaint)
        yPos += 18f

        if (order.discount > 0) {
            canvas.drawText("Discount:", 350f, yPos, regularPaint)
            canvas.drawText("-${config.currencySymbol}${String.format("%.2f", order.discount)}", 480f, yPos, regularPaint)
            yPos += 18f
        }

        canvas.drawText("GST Tax (5%):", 350f, yPos, regularPaint)
        canvas.drawText("${config.currencySymbol}${String.format("%.2f", order.taxAmount)}", 480f, yPos, regularPaint)
        yPos += 22f

        canvas.drawLine(340f, yPos, endX, yPos, Paint().apply { color = Color.BLACK; strokeWidth = 1.5f })
        yPos += 20f

        val totalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isFakeBoldText = true
        }
        canvas.drawText("GRAND TOTAL:", 340f, yPos, totalPaint)
        canvas.drawText("${config.currencySymbol}${String.format("%.2f", order.totalAmount)}", 475f, yPos, totalPaint)
        yPos += 25f

        // Payment status
        val payStatusStr = "Payment Status: ${order.paymentStatus} (${order.paymentMethod?.label ?: "UNPAID"})"
        canvas.drawText(payStatusStr, startX, yPos, boldPaint)
        yPos += 40f

        // Footer Message
        canvas.drawText("Thank you for dining with us! Please come again.", startX, yPos, headerPaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Invoice_${order.orderNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
