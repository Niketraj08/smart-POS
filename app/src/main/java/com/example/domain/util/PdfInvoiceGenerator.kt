package com.example.domain.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.R
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
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait size points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val crimsonPaint = Paint().apply {
            color = Color.parseColor("#8C1D11") // Deep Luxury Crimson
            textSize = 22f
            isFakeBoldText = true
        }
        val headerInfoPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
        }
        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10.5f
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
        val framePaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val startX = 35f
        val endX = 560f

        // Draw Outer Luxury Frame
        canvas.drawRect(15f, 15f, 580f, 827f, framePaint)
        canvas.drawRect(18f, 18f, 577f, 824f, Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        })

        var yPos = 35f

        // Top Header Banner Box (Luxury Crimson)
        val headerBox = RectF(startX, yPos, endX, yPos + 80f)
        canvas.drawRoundRect(headerBox, 8f, 8f, Paint().apply { color = Color.parseColor("#8C1D11") })

        // Draw Restaurant Logo inside Top Header
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_swad_sutra_logo)
            if (logoBitmap != null) {
                val logoRect = RectF(startX + 12f, yPos + 10f, startX + 72f, yPos + 70f)
                canvas.drawBitmap(logoBitmap, null, logoRect, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Restaurant Branding Inside Banner
        canvas.drawText("SWAD SUTRA FINE DINING", startX + 82f, yPos + 30f, Paint().apply {
            color = Color.parseColor("#D4AF37")
            textSize = 19f
            isFakeBoldText = true
        })
        canvas.drawText("LUXURY GASTRONOMY & POS SUITE", startX + 82f, yPos + 46f, Paint().apply {
            color = Color.WHITE
            textSize = 9.5f
        })

        val gstinStr = if (config.gstin.isNotBlank()) config.gstin else "07AAAAA0000A1Z5"
        canvas.drawText("GSTIN: $gstinStr", 360f, yPos + 28f, Paint().apply {
            color = Color.parseColor("#D4AF37")
            textSize = 11f
            isFakeBoldText = true
        })
        canvas.drawText("FSSAI Lic No: 12421008000451", 360f, yPos + 44f, Paint().apply {
            color = Color.WHITE
            textSize = 9.5f
        })
        canvas.drawText("Phone: ${config.phone}", 360f, yPos + 58f, Paint().apply {
            color = Color.LTGRAY
            textSize = 9f
        })

        yPos += 92f

        // Address Row
        canvas.drawText("${config.address} • Email: info@swadsutra.com", startX, yPos, headerInfoPaint)
        yPos += 14f

        // Gold Decorative Line
        canvas.drawLine(startX, yPos, endX, yPos, goldLinePaint)
        yPos += 20f

        // Invoice Title & Info Card
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))
        val infoRect = RectF(startX, yPos, endX, yPos + 52f)
        canvas.drawRoundRect(infoRect, 6f, 6f, Paint().apply { color = Color.parseColor("#FAF6EE") })

        canvas.drawText("OFFICIAL TAX INVOICE", startX + 12f, yPos + 18f, crimsonPaint.apply { textSize = 13f })
        canvas.drawText("Invoice ID: ${order.orderNumber}", startX + 12f, yPos + 36f, boldPaint)
        canvas.drawText("Date: $dateStr", 340f, yPos + 18f, regularPaint)
        canvas.drawText("Table: ${order.tableNumber ?: "N/A"} (${order.orderType.label})", 340f, yPos + 36f, regularPaint)
        canvas.drawText("Customer: ${order.customerName ?: "Walk-in Guest"}", startX + 180f, yPos + 36f, regularPaint)

        yPos += 68f

        // Table Header
        val thRect = RectF(startX, yPos, endX, yPos + 22f)
        canvas.drawRect(thRect, Paint().apply { color = Color.parseColor("#8C1D11") })
        val thPaint = Paint().apply { color = Color.WHITE; textSize = 10f; isFakeBoldText = true }

        yPos += 15f
        canvas.drawText("#", startX + 8f, yPos, thPaint)
        canvas.drawText("Item Description", startX + 30f, yPos, thPaint)
        canvas.drawText("Qty", 320f, yPos, thPaint)
        canvas.drawText("Rate (₹)", 385f, yPos, thPaint)
        canvas.drawText("Amount (₹)", 470f, yPos, thPaint)
        yPos += 12f

        // Items List
        var itemIdx = 1
        for (item in order.items) {
            val bgRowColor = if (itemIdx % 2 == 0) Color.parseColor("#FAFAFA") else Color.WHITE
            canvas.drawRect(startX, yPos, endX, yPos + 18f, Paint().apply { color = bgRowColor })

            canvas.drawText("$itemIdx.", startX + 8f, yPos + 13f, regularPaint)
            canvas.drawText(item.menuItem.name, startX + 30f, yPos + 13f, boldPaint)
            canvas.drawText("${item.quantity}", 325f, yPos + 13f, regularPaint)
            canvas.drawText("₹${String.format(Locale.US, "%.2f", item.menuItem.price)}", 385f, yPos + 13f, regularPaint)
            canvas.drawText("₹${String.format(Locale.US, "%.2f", item.subtotal)}", 470f, yPos + 13f, boldPaint)
            yPos += 18f
            itemIdx++
        }

        yPos += 10f
        canvas.drawLine(startX, yPos, endX, yPos, goldLinePaint)
        yPos += 18f

        // Financial & GST Tax Breakdown Box
        val cGst = order.taxAmount / 2.0
        val sGst = order.taxAmount / 2.0

        val taxBox = RectF(280f, yPos, endX, yPos + 110f)
        canvas.drawRoundRect(taxBox, 6f, 6f, Paint().apply { color = Color.parseColor("#FAF6EE") })

        var taxY = yPos + 16f
        canvas.drawText("Subtotal:", 295f, taxY, regularPaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", order.subtotal)}", 470f, taxY, regularPaint)
        taxY += 16f

        if (order.discount > 0) {
            canvas.drawText("Discount:", 295f, taxY, Paint().apply { color = Color.parseColor("#2E7D32"); textSize = 10f })
            canvas.drawText("-₹${String.format(Locale.US, "%.2f", order.discount)}", 470f, taxY, Paint().apply { color = Color.parseColor("#2E7D32"); textSize = 10f })
            taxY += 16f
        }

        canvas.drawText("CGST (2.5%):", 295f, taxY, regularPaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", cGst)}", 470f, taxY, regularPaint)
        taxY += 16f

        canvas.drawText("SGST (2.5%):", 295f, taxY, regularPaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", sGst)}", 470f, taxY, regularPaint)
        taxY += 16f

        canvas.drawLine(290f, taxY - 4f, endX - 10f, taxY - 4f, goldLinePaint)

        val totalPaint = Paint().apply {
            color = Color.parseColor("#8C1D11")
            textSize = 13f
            isFakeBoldText = true
        }
        canvas.drawText("GRAND TOTAL:", 295f, taxY + 12f, totalPaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", order.totalAmount)}", 465f, taxY + 12f, totalPaint)

        yPos += 125f

        // Payment status & Auth Stamp
        val payStatusStr = "Payment Status: ${order.paymentStatus} (${order.paymentMethod?.label ?: "UNPAID"})"
        canvas.drawText(payStatusStr, startX, yPos, boldPaint)
        canvas.drawText("Authorized Signatory: ___________________", 320f, yPos, regularPaint)
        yPos += 30f

        // Bottom Company Footer & AstraCognix Solution Attribution
        canvas.drawLine(startX, yPos, endX, yPos, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
        yPos += 18f
        canvas.drawText("Thank you for dining with Swad Sutra Fine Dining! Visit us again soon.", startX, yPos, headerInfoPaint)
        yPos += 16f
        val creditPaint = Paint().apply {
            color = Color.parseColor("#B8860B")
            textSize = 9.5f
            isFakeBoldText = true
        }
        canvas.drawText("Software Developed & Powered by AstraCognix Solution • www.astracognixsolutions.in", startX, yPos, creditPaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "SwadSutra_TaxInvoice_${order.orderNumber}.pdf")
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

    /**
     * Generates a 80mm thermal receipt optimized PDF layout with top restaurant logo
     */
    fun generateThermalReceiptPdf(
        context: Context,
        order: OrderSummary,
        config: RestaurantConfig
    ): File? {
        val pdfDocument = PdfDocument()
        // 80mm paper width = ~226 points width, height dynamic ~600 points
        val pageInfo = PdfDocument.PageInfo.Builder(226, 620, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val boldPaint = Paint().apply { color = Color.BLACK; textSize = 9f; isFakeBoldText = true }
        val titlePaint = Paint().apply { color = Color.parseColor("#8C1D11"); textSize = 11f; isFakeBoldText = true }
        val regularPaint = Paint().apply { color = Color.BLACK; textSize = 8.5f }
        val centerPaint = Paint().apply { color = Color.BLACK; textSize = 8.5f; textAlign = Paint.Align.CENTER }
        val centerTitlePaint = Paint().apply { color = Color.parseColor("#8C1D11"); textSize = 11f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }
        val centerGoldPaint = Paint().apply { color = Color.parseColor("#B8860B"); textSize = 8f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }
        val linePaint = Paint().apply { color = Color.GRAY; strokeWidth = 1f }

        val startX = 10f
        val endX = 216f
        val centerX = 113f
        var yPos = 15f

        // Top Logo image for thermal receipt
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_swad_sutra_logo)
            if (logoBitmap != null) {
                val logoRect = RectF(centerX - 22f, yPos, centerX + 22f, yPos + 44f)
                canvas.drawBitmap(logoBitmap, null, logoRect, null)
                yPos += 50f
            }
        } catch (e: Exception) {
            e.printStackTrace()
            yPos += 5f
        }

        canvas.drawText("SWAD SUTRA FINE DINING", centerX, yPos, centerTitlePaint)
        yPos += 12f
        canvas.drawText(config.address.take(35), centerX, yPos, centerPaint)
        yPos += 11f
        canvas.drawText("Tel: ${config.phone}", centerX, yPos, centerPaint)
        yPos += 11f
        val gstinStr = if (config.gstin.isNotBlank()) config.gstin else "07AAAAA0000A1Z5"
        canvas.drawText("GSTIN: $gstinStr", centerX, yPos, centerGoldPaint)
        yPos += 14f

        canvas.drawLine(startX, yPos, endX, yPos, linePaint)
        yPos += 12f

        val dateStr = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(order.createdAt))
        canvas.drawText("Order #: ${order.orderNumber}", startX, yPos, boldPaint)
        canvas.drawText(dateStr, 130f, yPos, regularPaint)
        yPos += 12f

        canvas.drawText("Table: ${order.tableNumber ?: "N/A"} (${order.orderType.label})", startX, yPos, regularPaint)
        canvas.drawText("Guest: ${order.customerName?.take(10) ?: "Walk-in"}", 130f, yPos, regularPaint)
        yPos += 14f

        canvas.drawLine(startX, yPos, endX, yPos, linePaint)
        yPos += 12f

        // Table Header
        canvas.drawText("QTY", startX, yPos, boldPaint)
        canvas.drawText("ITEM", startX + 30f, yPos, boldPaint)
        canvas.drawText("AMT(₹)", endX - 35f, yPos, boldPaint)
        yPos += 12f

        for (item in order.items) {
            canvas.drawText("${item.quantity}x", startX, yPos, regularPaint)
            canvas.drawText(item.menuItem.name.take(18), startX + 28f, yPos, regularPaint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.subtotal), endX - 38f, yPos, boldPaint)
            yPos += 13f
        }

        canvas.drawLine(startX, yPos, endX, yPos, linePaint)
        yPos += 12f

        canvas.drawText("Subtotal:", startX + 30f, yPos, regularPaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", order.subtotal)}", endX - 38f, yPos, regularPaint)
        yPos += 12f

        if (order.discount > 0) {
            canvas.drawText("Discount:", startX + 30f, yPos, regularPaint)
            canvas.drawText("-₹${String.format(Locale.US, "%.2f", order.discount)}", endX - 38f, yPos, regularPaint)
            yPos += 12f
        }

        canvas.drawText("GST (5%):", startX + 30f, yPos, regularPaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", order.taxAmount)}", endX - 38f, yPos, regularPaint)
        yPos += 14f

        canvas.drawLine(startX, yPos, endX, yPos, linePaint)
        yPos += 14f

        canvas.drawText("TOTAL:", startX + 30f, yPos, titlePaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", order.totalAmount)}", endX - 42f, yPos, titlePaint)
        yPos += 20f

        canvas.drawText("*** THANK YOU FOR VISITING ***", centerX, yPos, centerPaint)
        yPos += 12f
        canvas.drawText("Powered by AstraCognix Solution", centerX, yPos, centerGoldPaint)
        yPos += 10f
        canvas.drawText("www.astracognixsolutions.in", centerX, yPos, centerPaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "SwadSutra_ThermalReceipt_${order.orderNumber}.pdf")
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
            color = Color.parseColor("#8C1D11")
            textSize = 20f
            isFakeBoldText = true
        }
        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#B8860B")
            textSize = 12f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 9.5f
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

        var yPos = 35f
        val startX = 35f
        val endX = 560f

        // Top Logo image for Master Orders report
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_swad_sutra_logo)
            if (logoBitmap != null) {
                val logoRect = RectF(startX, yPos, startX + 45f, yPos + 45f)
                canvas.drawBitmap(logoBitmap, null, logoRect, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Header
        canvas.drawText("SWAD SUTRA FINE DINING", startX + 55f, yPos + 22f, titlePaint)
        val gstinStr = if (config.gstin.isNotBlank()) config.gstin else "07AAAAA0000A1Z5"
        canvas.drawText("GSTIN: $gstinStr", 390f, yPos + 22f, subTitlePaint)
        yPos += 38f
        canvas.drawText("MASTER ORDERS HISTORY & SALES TAX REPORT", startX + 55f, yPos, subTitlePaint)
        yPos += 18f
        val generatedAt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Generated On: $generatedAt  |  Total Orders: ${orders.size}", startX, yPos, headerPaint)
        yPos += 18f

        canvas.drawLine(startX, yPos, endX, yPos, goldLinePaint)
        yPos += 18f

        // Total Revenue Summary Box
        val totalRevenue = orders.sumOf { it.totalAmount }
        val totalGst = orders.sumOf { it.taxAmount }
        val servedCount = orders.count { it.status == com.example.domain.model.OrderStatus.SERVED }
        val paidCount = orders.count { it.paymentStatus == com.example.domain.model.PaymentStatus.PAID }

        val summaryBox = RectF(startX, yPos, endX, yPos + 35f)
        canvas.drawRoundRect(summaryBox, 6f, 6f, Paint().apply { color = Color.parseColor("#FAF6EE") })
        canvas.drawText("TOTAL REVENUE: ₹${String.format(Locale.US, "%.2f", totalRevenue)}", startX + 10f, yPos + 22f, titlePaint.apply { textSize = 12f })
        canvas.drawText("GST Collected: ₹${String.format(Locale.US, "%.2f", totalGst)}  |  Paid: $paidCount  |  Served: $servedCount", 270f, yPos + 22f, boldPaint)
        yPos += 48f

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
                // Page Footer
                canvas.drawText("Page $pageNum • Developed by AstraCognix Solution (www.astracognixsolutions.in)", startX, 810f, headerPaint)
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
        canvas.drawText("Swad Sutra Fine Dining • Official Sales Report (GSTIN: $gstinStr)", startX, yPos, headerPaint)
        yPos += 15f
        canvas.drawText("Software Developed & Powered by AstraCognix Solution • www.astracognixsolutions.in", startX, yPos, footerCreditPaint)

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

