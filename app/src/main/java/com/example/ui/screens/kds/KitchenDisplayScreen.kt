package com.example.ui.screens.kds

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.OrderSummary
import com.example.domain.model.OrderStatus
import com.example.ui.PosViewModel

@Composable
fun KitchenDisplayScreen(viewModel: PosViewModel) {
    val kitchenOrders by viewModel.kitchenOrders.collectAsState()
    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) } // null = All active

    val filteredOrders = when (selectedFilter) {
        null -> kitchenOrders
        else -> kitchenOrders.filter { it.status == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Kitchen Display System (KDS)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time ticket dispatch, cooking state & serve tracking",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KdsLegend("Normal (<5m)", Color(0xFF2E7D32))
                KdsLegend("Warning (5-15m)", Color(0xFFF57F17))
                KdsLegend("Delayed (>15m)", Color(0xFFC62828))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All Active (${kitchenOrders.size})") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == OrderStatus.PENDING,
                    onClick = { selectedFilter = OrderStatus.PENDING },
                    label = { Text("New / Pending (${kitchenOrders.count { it.status == OrderStatus.PENDING }})") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == OrderStatus.PREPARING,
                    onClick = { selectedFilter = OrderStatus.PREPARING },
                    label = { Text("Cooking (${kitchenOrders.count { it.status == OrderStatus.PREPARING }})") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == OrderStatus.READY,
                    onClick = { selectedFilter = OrderStatus.READY },
                    label = { Text("Ready to Serve (${kitchenOrders.count { it.status == OrderStatus.READY }})") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = "Kitchen Empty",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedFilter == null) "All kitchen tickets completed! No active orders." else "No orders matching selected status.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredOrders) { order ->
                    KitchenTicketCard(
                        order = order,
                        onStartCooking = {
                            viewModel.updateOrderStatus(order.id, OrderStatus.PREPARING)
                        },
                        onMarkOrderReady = {
                            viewModel.updateOrderStatus(order.id, OrderStatus.READY)
                        },
                        onMarkServed = {
                            viewModel.updateOrderStatus(order.id, OrderStatus.SERVED)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KdsLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(text = label, fontSize = 11.sp)
    }
}

@Composable
private fun KitchenTicketCard(
    order: OrderSummary,
    onStartCooking: () -> Unit,
    onMarkOrderReady: () -> Unit,
    onMarkServed: () -> Unit
) {
    val elapsedMinutes = ((System.currentTimeMillis() - order.createdAt) / (1000 * 60)).toInt()
    val timerColor = when {
        elapsedMinutes < 5 -> Color(0xFF2E7D32)
        elapsedMinutes in 5..15 -> Color(0xFFF57F17)
        else -> Color(0xFFC62828)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kds_ticket_${order.orderNumber}"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Ticket Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = order.orderNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OrderStatusBadge(order.status)
                    }
                    Text(
                        text = "Table ${order.tableNumber ?: "N/A"} • ${order.orderType.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(timerColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${elapsedMinutes}m elapsed",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            for (item in order.items) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.quantity}x  ${item.menuItem.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (item.specialNotes.isNotBlank()) {
                            Text(
                                text = "  * ${item.specialNotes}",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                when (item.status) {
                                    OrderStatus.READY -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                                    OrderStatus.PREPARING -> Color(0xFFF57F17).copy(alpha = 0.2f)
                                    OrderStatus.SERVED -> Color(0xFF1976D2).copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.status.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workflow Step Buttons: Pending -> Cooking -> Ready -> Serve
            when (order.status) {
                OrderStatus.PENDING -> {
                    Button(
                        onClick = onStartCooking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kds_start_cooking_${order.orderNumber}"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = "Start Cooking")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🍳 Start Cooking")
                    }
                }
                OrderStatus.PREPARING -> {
                    Button(
                        onClick = onMarkOrderReady,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kds_mark_ready_${order.orderNumber}"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Mark Cooked")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("✅ Cooked & Ready")
                    }
                }
                OrderStatus.READY -> {
                    Button(
                        onClick = onMarkServed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kds_mark_served_${order.orderNumber}"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LocalDining, contentDescription = "Serve Order")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🍽️ Serve & Deliver")
                    }
                }
                else -> {
                    Button(
                        onClick = onMarkServed,
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(12.dp),
                        enabled = false
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Completed")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Completed")
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusBadge(status: OrderStatus) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PENDING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        OrderStatus.PREPARING -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        OrderStatus.READY -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        OrderStatus.SERVED -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        else -> Color.LightGray to Color.DarkGray
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
