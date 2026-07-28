package com.example.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PosViewModel

@Composable
fun ReportsAnalyticsScreen(viewModel: PosViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val config by viewModel.restaurantConfig.collectAsState()

    val totalRevenue = orders.sumOf { it.totalAmount }
    val totalTaxCollected = orders.sumOf { it.taxAmount }
    val totalDiscountsGiven = orders.sumOf { it.discount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Reports & Sales Analytics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Financial performance, revenue charts & tax summaries",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Revenue Canvas Line Chart
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Daily Sales Trend (This Week)", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                val samplePoints = listOf(120.0, 280.0, 450.0, 390.0, 680.0, 850.0, totalRevenue.coerceAtLeast(400.0))
                val maxVal = samplePoints.maxOrNull() ?: 1000.0

                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (samplePoints.size - 1)

                    val path = Path()
                    samplePoints.forEachIndexed { i, pt ->
                        val x = i * spacing
                        val y = height - (pt.toFloat() / maxVal.toFloat() * height)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(color = Color(0xFFE65100), radius = 6f, center = Offset(x, y))
                    }

                    drawPath(path, color = Color(0xFFE65100), style = Stroke(width = 4f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Breakdown Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReportMetricBox(
                title = "Total Gross Sales",
                value = "${config.currencySymbol}${String.format("%.2f", totalRevenue)}",
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            ReportMetricBox(
                title = "GST Tax Liability",
                value = "${config.currencySymbol}${String.format("%.2f", totalTaxCollected)}",
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
            ReportMetricBox(
                title = "Discounts Offered",
                value = "${config.currencySymbol}${String.format("%.2f", totalDiscountsGiven)}",
                color = Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // GST Tax Summary Table Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("GST Tax Summary Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tax Category", fontWeight = FontWeight.Bold)
                    Text("Rate", fontWeight = FontWeight.Bold)
                    Text("Tax Amount", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CGST (Central GST)")
                    Text("2.5%")
                    Text("${config.currencySymbol}${String.format("%.2f", totalTaxCollected / 2.0)}")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SGST (State GST)")
                    Text("2.5%")
                    Text("${config.currencySymbol}${String.format("%.2f", totalTaxCollected / 2.0)}")
                }
            }
        }
    }
}

@Composable
private fun ReportMetricBox(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
