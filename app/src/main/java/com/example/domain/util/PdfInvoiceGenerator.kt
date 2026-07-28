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

        val titlePaint = Paint().apply {
            color = Color.parseColor("#8C1D11") // Deep Luxury Crimson
            textSize = 22f
            isFakeBoldText = true
        }
        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#B8860B") // Dark Goldenrod
            textSize = 13f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
        }
        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isFakeBoldText = true
        }
        val regularPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        val goldLinePaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 1.5f
        }

        var yPos = 45f
        val startX = 40f
        val endX = 555f

        // Restaurant Title with Gold & Crimson Branding
        canvas.drawText("SWAD SUTRA FINE DINING", startX, yPos, titlePaint)
        yPos += 18f
        canvas.drawText("LUXURY RESTAURANT & POS SUITE", startX, yPos, subTitlePaint)
        yPos += 16f
        canvas.drawText(config.address, startX, yPos, headerPaint)
        yPos += 14f
        canvas.drawText("Phone: ${config.phone}  |  GSTIN: ${config.gstin}", startX, yPos, headerPaint)
        yPos += 20f

        // Gold Decorative Line
        canvas.drawLine(startX, yPos, endX, yPos, goldLinePaint)
        yPos += 22f

        // Invoice Header Details
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))
        canvas.drawText("OFFICIAL TAX INVOICE", startX, yPos, boldPaint)
        yPos += 16f
        canvas.drawText("Order ID: ${order.orderNumber}", startX, yPos, regularPaint)
        canvas.drawText("Date: $dateStr", 340f, yPos, regularPaint)
        yPos += 14f
        canvas.drawText("Table: ${order.tableNumber ?: "N/A"} (${order.orderType.label})", startX, yPos, regularPaint)
        canvas.drawText("Customer: ${order.customerName ?: "Walk-in Guest"}", 340f, yPos, regularPaint)
        yPos += 22f

        // Table Header
        canvas.drawRect(startX, yPos, endX, yPos + 22f, Paint().apply { color = Color.parseColor("#F5F0E6") })
        canvas.drawLine(startX, yPos, endX, yPos, goldLinePaint)
        yPos += 15f
        canvas.drawText("Item Description", startX + 8f, yPos, boldPaint)
        canvas.drawText("Qty", 320f, yPos, boldPaint)
        canvas.drawText("Rate (₹)", 385f, yPos, boldPaint)
        canvas.drawText("Amount (₹)", 470f, yPos, boldPaint)
        yPos += 12f
        canvas.drawLine(startX, yPos, endX, yPos, goldLinePaint)
        yPos += 18f

        // Items List
        for (item in order.items) {
            canvas.drawText(item.menuItem.name, startX + 8f, yPos, regularPaint)
            canvas.drawText("${item.quantity}", 325f, yPos, regularPaint)
            canvas.drawText("₹${String.format(Locale.US, "%.2f", item.menuItem.price)}", 385f, yPos, regularPaint)
            canvas.drawText("₹${String.format(Locale.US, "%.2f", item.subtotal)}", 470f, yPos, regularPaint)
            yPos += 18f
        }

        yPos += 8f
        canvas.drawLine(startX, yPos, endX, yPos, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
        yPos += 18f

        // Summary Breakdown
        canvas.drawText("Subtotal:", 340f, yPos, regularPaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", order.subtotal)}", 470f, yPos, regularPaint)
        yPos += 16f

        if (order.discount > 0) {
            canvas.drawText("Discount:", 340f, yPos, regularPaint)
            canvas.drawText("-₹${String.format(Locale.US, "%.2f", order.discount)}", 470f, yPos, regularPaint)
            yPos += 16f
        }

        canvas.drawText("GST Tax (5%):", 340f, yPos, regularPaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", order.taxAmount)}", 470f, yPos, regularPaint)
        yPos += 20f

        canvas.drawLine(330f, yPos, endX, yPos, goldLinePaint)
        yPos += 18f

        val totalPaint = Paint().apply {
            color = Color.parseColor("#8C1D11")
            textSize = 13f
            isFakeBoldText = true
        }
        canvas.drawText("GRAND TOTAL:", 330f, yPos, totalPaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", order.totalAmount)}", 465f, yPos, totalPaint)
        yPos += 22f

        // Payment status
        val payStatusStr = "Payment Status: ${order.paymentStatus} (${order.paymentMethod?.label ?: "UNPAID"})"
        canvas.drawText(payStatusStr, startX, yPos, boldPaint)
        yPos += 35f

        // Company Footer & Credits
        canvas.drawLine(startX, yPos, endX, yPos, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
        yPos += 18f
        canvas.drawText("Thank you for dining with Swad Sutra Fine Dining!", startX, yPos, headerPaint)
        yPos += 16f
        val creditPaint = Paint().apply {
            color = Color.parseColor("#B8860B")
            textSize = 9.5f
            isFakeBoldText = true
        }
        canvas.drawText("Powered & Developed by AstraCognix Solution • www.astracognix.com", startX, yPos, creditPaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "SwadSutra_Invoice_${order.orderNumber}.pdf")
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

    fun generateAllOrdersPdf(
        context: Context,
        orders: List<OrderSummary>,
        config: RestaurantConfig
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#8C1D11") // Deep Luxury Crimson
            textSize = 22f
            isFakeBoldText = true
        }
        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#B8860B") // Dark Gold
            textSize = 12f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
        }
        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isFakeBoldText = true
        }
        val regularPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
        }
        val goldLinePaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 1.5f
        }

        var yPos = 40f
        val startX = 35f
        val endX = 560f

        // Document Header
        canvas.drawText("SWAD SUTRA FINE DINING", startX, yPos, titlePaint)
        yPos += 18f
        canvas.drawText("MASTER ORDERS HISTORY & SALES REPORT", startX, yPos, subTitlePaint)
        yPos += 14f
        val generatedAt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Generated On: $generatedAt  |  Total Orders: ${orders.size}", startX, yPos, headerPaint)
        yPos += 18f

        canvas.drawLine(startX, yPos, endX, yPos, goldLinePaint)
        yPos += 20f

        // Total Revenue Summary Box
        val totalRevenue = orders.sumOf { it.totalAmount }
        val servedCount = orders.count { it.status == com.example.domain.model.OrderStatus.SERVED }
        val paidCount = orders.count { it.paymentStatus == com.example.domain.model.PaymentStatus.PAID }
        val cancelledCount = orders.count { it.status == com.example.domain.model.OrderStatus.CANCELLED }

        canvas.drawRect(startX, yPos, endX, yPos + 30f, Paint().apply { color = Color.parseColor("#FAF6EE") })
        canvas.drawText("TOTAL REVENUE: ₹${String.format(Locale.US, "%.2f", totalRevenue)}", startX + 10f, yPos + 18f, titlePaint.apply { textSize = 13f })
        canvas.drawText("Paid: $paidCount  |  Served: $servedCount  |  Cancelled: $cancelledCount", 320f, yPos + 18f, boldPaint)
        yPos += 42f

        // Table Header
        canvas.drawRect(startX, yPos, endX, yPos + 20f, Paint().apply { color = Color.parseColor("#8C1D11") })
        val tableHeaderPaint = Paint().apply { color = Color.WHITE; textSize = 9.5f; isFakeBoldText = true }
        canvas.drawText("Order #", startX + 6f, yPos + 14f, tableHeaderPaint)
        canvas.drawText("Table/Type", 115f, yPos + 14f, tableHeaderPaint)
        canvas.drawText("Customer", 205f, yPos + 14f, tableHeaderPaint)
        canvas.drawText("Status", 310f, yPos + 14f, tableHeaderPaint)
        canvas.drawText("Payment", 385f, yPos + 14f, tableHeaderPaint)
        canvas.drawText("Amount (₹)", 480f, yPos + 14f, tableHeaderPaint)
        yPos += 24f

        var pageNum = 1

        for (order in orders) {
            if (yPos > 760f) {
                // Footer for current page
                canvas.drawText("Page $pageNum • Powered & Developed by AstraCognix Solution", startX, 810f, headerPaint)
                pdfDocument.finishPage(page)

                pageNum++
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                yPos = 40f

                // Re-draw table header
                canvas.drawRect(startX, yPos, endX, yPos + 20f, Paint().apply { color = Color.parseColor("#8C1D11") })
                canvas.drawText("Order #", startX + 6f, yPos + 14f, tableHeaderPaint)
                canvas.drawText("Table/Type", 115f, yPos + 14f, tableHeaderPaint)
                canvas.drawText("Customer", 205f, yPos + 14f, tableHeaderPaint)
                canvas.drawText("Status", 310f, yPos + 14f, tableHeaderPaint)
                canvas.drawText("Payment", 385f, yPos + 14f, tableHeaderPaint)
                canvas.drawText("Amount (₹)", 480f, yPos + 14f, tableHeaderPaint)
                yPos += 24f
            }

            canvas.drawText(order.orderNumber, startX + 6f, yPos, boldPaint)
            val tableTypeStr = "${order.tableNumber ?: "N/A"} (${order.orderType.label})"
            canvas.drawText(tableTypeStr, 115f, yPos, regularPaint)
            canvas.drawText(order.customerName ?: "Walk-in Guest", 205f, yPos, regularPaint)
            canvas.drawText(order.status.label, 310f, yPos, boldPaint)
            canvas.drawText("${order.paymentStatus} (${order.paymentMethod?.name ?: "-"})", 385f, yPos, regularPaint)
            canvas.drawText("₹${String.format(Locale.US, "%.2f", order.totalAmount)}", 480f, yPos, boldPaint)

            yPos += 18f
            canvas.drawLine(startX, yPos - 6f, endX, yPos - 6f, Paint().apply { color = Color.parseColor("#E5E0D8"); strokeWidth = 0.5f })
        }

        yPos += 20f
        canvas.drawLine(startX, yPos, endX, yPos, goldLinePaint)
        yPos += 18f

        // Footer AstraCognix Solution Credit
        val footerCreditPaint = Paint().apply {
            color = Color.parseColor("#B8860B")
            textSize = 10f
            isFakeBoldText = true
        }
        canvas.drawText("Swad Sutra Fine Dining • Official Sales Report", startX, yPos, headerPaint)
        yPos += 15f
        canvas.drawText("Software Developed & Powered by AstraCognix Solution • www.astracognix.com", startX, yPos, footerCreditPaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "SwadSutra_Master_Orders_Report.pdf")
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

