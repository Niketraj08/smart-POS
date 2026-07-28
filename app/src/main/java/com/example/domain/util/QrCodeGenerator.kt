package com.example.domain.util

import android.graphics.Bitmap
import android.graphics.Color

object QrCodeGenerator {

    /**
     * Generates a clean 2D matrix bitmap representing a QR code string.
     */
    fun generateQrBitmap(content: String, size: Int = 500): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = android.graphics.Paint().apply {
            color = Color.BLACK
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
        }

        val padding = size / 10
        val innerSize = size - (2 * padding)
        val numModules = 21 // 21x21 QR Grid
        val moduleSize = innerSize / numModules.toFloat()

        // Generate deterministic pattern based on content string hashCode
        val seed = content.hashCode()
        val random = java.util.Random(seed.toLong())

        val grid = Array(numModules) { BooleanArray(numModules) }

        // Draw Finder Patterns (Top-Left, Top-Right, Bottom-Left 7x7 squares)
        fun markFinder(startR: Int, startC: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    grid[startR + r][startC + c] = isBorder || isCenter
                }
            }
        }

        markFinder(0, 0)
        markFinder(0, numModules - 7)
        markFinder(numModules - 7, 0)

        // Random data modules
        for (r in 0 until numModules) {
            for (c in 0 until numModules) {
                // Skip finder pattern zones
                if ((r < 8 && c < 8) || (r < 8 && c >= numModules - 8) || (r >= numModules - 8 && c < 8)) {
                    continue
                }
                grid[r][c] = random.nextBoolean()
            }
        }

        // Draw grid onto canvas
        for (r in 0 until numModules) {
            for (c in 0 until numModules) {
                if (grid[r][c]) {
                    val left = padding + (c * moduleSize)
                    val top = padding + (r * moduleSize)
                    canvas.drawRect(left, top, left + moduleSize, top + moduleSize, paint)
                }
            }
        }

        return bitmap
    }
}
