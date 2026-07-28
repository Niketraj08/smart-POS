package com.example.ui.screens.orders

import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.OrderSummary
import com.example.domain.model.OrderStatus
import com.example.domain.model.PaymentStatus
import com.example.ui.PosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderHistoryScreen(viewModel: PosViewModel) {
    val context = LocalContext.current
    val allOrders by viewModel.allOrders.collectAsState()
    val generatedPdf by viewModel.generatedPdfFile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, SERVED, PAID, CANCELLED, IN_PROGRESS

    // Filter logic
    val filteredOrders = remember(allOrders, searchQuery, selectedFilter) {
        allOrders.filter { order ->
            val matchesSearch = searchQuery.isBlank() ||
                    order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                    (order.tableNumber?.contains(searchQuery, ignoreCase = true) == true) ||
                    (order.customerName?.contains(searchQuery, ignoreCase = true) == true) ||
                    order.items.any { it.menuItem.name.contains(searchQuery, ignoreCase = true) }

            val matchesFilter = when (selectedFilter) {
                "SERVED" -> order.status == OrderStatus.SERVED
                "PAID" -> order.paymentStatus == PaymentStatus.PAID || order.status == OrderStatus.BILLED
                "CANCELLED" -> order.status == OrderStatus.CANCELLED
                "IN_PROGRESS" -> order.status == OrderStatus.PENDING || order.status == OrderStatus.PREPARING || order.status == OrderStatus.READY
                else -> true
            }

            matchesSearch && matchesFilter
        }.sortedByDescending { it.createdAt }
    }

    // Revenue calculations
    val totalRevenue = remember(allOrders) {
        allOrders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalAmount }
    }
    val servedCount = remember(allOrders) { allOrders.count { it.status == OrderStatus.SERVED } }
    val paidCount = remember(allOrders) { allOrders.count { it.paymentStatus == PaymentStatus.PAID || it.status == OrderStatus.BILLED } }
    val cancelledCount = remember(allOrders) { allOrders.count { it.status == OrderStatus.CANCELLED } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // --- Top Header Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFFD4AF37), CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_swad_sutra_logo),
                        contentDescription = "Swad Sutra Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Swad ",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFFD4AF37)
                        )
                        Text(
                            text = "Sutra",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Order History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Luxury Register Logs & Order Archives",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Export All PDF Button
            Button(
                onClick = {
                    if (filteredOrders.isEmpty()) {
                        Toast.makeText(context, "No orders to export!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.generateAllOrdersPdf(filteredOrders, context)
                        Toast.makeText(context, "Luxury PDF Sales Report Generated!", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8C1D11),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("download_all_orders_pdf_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download All Orders PDF",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Summary Stats Metrics Banner ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Sales Metric
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TOTAL REVENUE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", totalRevenue)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${allOrders.size} total register logs",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Served Filter Stat
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SERVED ORDERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$servedCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            // Paid Filter Stat
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PAID & BILLED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$paidCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
            }

            // Cancelled Filter Stat
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CANCELLED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$cancelledCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Search Bar and Status Filters Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Order ID, Table #, Customer or Dish...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD4AF37),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("order_history_search_input")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf(
                "ALL" to "All Orders (${allOrders.size})",
                "SERVED" to "Served ($servedCount)",
                "PAID" to "Paid ($paidCount)",
                "CANCELLED" to "Cancelled ($cancelledCount)",
                "IN_PROGRESS" to "In Progress"
            )

            items(filters) { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = {
                        Text(
                            text = label,
                            fontWeight = if (selectedFilter == key) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF8C1D11),
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_chip_$key")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Generated PDF Notice Banner ---
        if (generatedPdf != null) {
            Surface(
                color = Color(0xFFFFF8E1),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF388E3C))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PDF Generated: ${generatedPdf?.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "Saved in app cache • Tap Share to send or print",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val file = generatedPdf ?: return@Button
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Order PDF"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Main Order List (LazyColumn) ---
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No orders found in history",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try adjusting your search query or status filter",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("order_history_lazy_column")
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderHistoryCard(
                        order = order,
                        onGeneratePdf = { viewModel.generatePdfInvoice(order, context) },
                        onPrintThermal = {
                            viewModel.printThermalReceipt(order, context)
                            Toast.makeText(context, "Receipt sent to printer simulator", Toast.LENGTH_SHORT).show()
                        },
                        onCancelOrder = {
                            viewModel.updateOrderStatus(order.id, OrderStatus.CANCELLED)
                            Toast.makeText(context, "Order ${order.orderNumber} cancelled", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- AstraCognix Solution Company Promotional Credit Footer ---
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Developed & Powered by AstraCognix Solution",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Swad Sutra Fine Dining POS v2.5",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun OrderHistoryCard(
    order: OrderSummary,
    onGeneratePdf: () -> Unit,
    onPrintThermal: () -> Unit,
    onCancelOrder: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(order.createdAt) { dateFormat.format(Date(order.createdAt)) }

    val statusBgColor = when (order.status) {
        OrderStatus.SERVED -> Color(0xFFE8F5E9)
        OrderStatus.BILLED, OrderStatus.COMPLETED -> Color(0xFFE3F2FD)
        OrderStatus.CANCELLED -> Color(0xFFFFEBEE)
        OrderStatus.PREPARING -> Color(0xFFFFF3E0)
        OrderStatus.READY -> Color(0xFFE0F2F1)
        OrderStatus.PENDING -> Color(0xFFF3E5F5)
    }

    val statusTextColor = when (order.status) {
        OrderStatus.SERVED -> Color(0xFF2E7D32)
        OrderStatus.BILLED, OrderStatus.COMPLETED -> Color(0xFF1565C0)
        OrderStatus.CANCELLED -> Color(0xFFC62828)
        OrderStatus.PREPARING -> Color(0xFFE65100)
        OrderStatus.READY -> Color(0xFF00695C)
        OrderStatus.PENDING -> Color(0xFF6A1B9A)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .testTag("order_card_${order.id}"),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order Number, Table, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.orderNumber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${order.tableNumber ?: "Takeaway"} (${order.orderType.label})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Payment Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (order.paymentStatus == PaymentStatus.PAID) Color(0xFFFFF8E1) else Color.LightGray.copy(alpha = 0.3f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (order.paymentStatus == PaymentStatus.PAID) "PAID (${order.paymentMethod?.name ?: "CASH"})" else "UNPAID",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.paymentStatus == PaymentStatus.PAID) Color(0xFFB8860B) else Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBgColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = order.status.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Guest: ${order.customerName ?: "Walk-in Guest"} • $formattedDate",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Order Items Breakdown
            Text(
                text = "Order Items (${order.items.size}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}x  ${item.menuItem.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", item.subtotal)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Total Amount in Rupees
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (order.discount > 0) {
                        Text(
                            text = "Discount applied: -₹${String.format(Locale.US, "%.2f", order.discount)}",
                            fontSize = 11.sp,
                            color = Color(0xFF388E3C)
                        )
                    }
                    Text(
                        text = "Incl. 5% GST Tax: ₹${String.format(Locale.US, "%.2f", order.taxAmount)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TOTAL BILL: ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", order.totalAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF8C1D11)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGeneratePdf,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF Invoice", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onPrintThermal,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print", fontSize = 12.sp)
                }

                if (order.status != OrderStatus.CANCELLED) {
                    OutlinedButton(
                        onClick = onCancelOrder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
