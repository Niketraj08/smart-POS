package com.example.ui.screens.tables

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.domain.model.TableInfo
import com.example.domain.model.TableStatus
import com.example.ui.PosViewModel

@Composable
fun TableManagementScreen(viewModel: PosViewModel) {
    val tables by viewModel.tables.collectAsState()
    var selectedSection by remember { mutableStateOf("All") }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTableForAction by remember { mutableStateOf<TableInfo?>(null) }

    val sections = listOf("All") + tables.map { it.section }.distinct()
    val filteredTables = if (selectedSection == "All") tables else tables.filter { it.section == selectedSection }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isCompact = maxWidth < 600.dp
                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column {
                            Text(
                                text = "Table Floor Plan",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage seating & reservations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item { StatusLegendBadge("Available", Color(0xFF2E7D32)) }
                            item { StatusLegendBadge("Occupied", Color(0xFFC62828)) }
                            item { StatusLegendBadge("Reserved", Color(0xFFF57F17)) }
                            item { StatusLegendBadge("Billed", Color(0xFF1565C0)) }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Table Floor Plan",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage table seating, reservations & active orders",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusLegendBadge("Available", Color(0xFF2E7D32))
                            StatusLegendBadge("Occupied", Color(0xFFC62828))
                            StatusLegendBadge("Reserved", Color(0xFFF57F17))
                            StatusLegendBadge("Billed", Color(0xFF1565C0))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                sections.forEach { section ->
                    FilterChip(
                        selected = selectedSection == section,
                        onClick = { selectedSection = section },
                        label = { Text(section) },
                        modifier = Modifier.testTag("section_chip_$section")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Table Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTables) { table ->
                    TableCard(
                        table = table,
                        onClick = { selectedTableForAction = table }
                    )
                }
            }
        }

        // Floating Add Table Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_table_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Table")
        }

        // Table Action Dialog
        selectedTableForAction?.let { table ->
            TableActionDialog(
                table = table,
                onDismiss = { selectedTableForAction = null },
                onUpdateStatus = { status ->
                    viewModel.updateTableStatus(table.id, status, if (status == TableStatus.AVAILABLE) null else table.activeOrderId)
                    selectedTableForAction = null
                },
                onTakeOrder = {
                    viewModel.selectTable(table)
                    viewModel.navigateTo("orders")
                    selectedTableForAction = null
                },
                onViewBill = {
                    viewModel.selectTable(table)
                    viewModel.navigateTo("billing")
                    selectedTableForAction = null
                }
            )
        }

        // Add Table Dialog
        if (showAddDialog) {
            AddTableDialog(
                onDismiss = { showAddDialog = false },
                onSave = { newTable ->
                    viewModel.saveTable(newTable)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun StatusLegendBadge(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
    }
}

@Composable
private fun TableCard(
    table: TableInfo,
    onClick: () -> Unit
) {
    val statusColor = when (table.status) {
        TableStatus.AVAILABLE -> Color(0xFF2E7D32)
        TableStatus.OCCUPIED -> Color(0xFFC62828)
        TableStatus.RESERVED -> Color(0xFFF57F17)
        TableStatus.BILLED -> Color(0xFF1565C0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .testTag("table_card_${table.tableNumber}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = table.tableNumber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )

                Box(
                    modifier = Modifier
                        .background(statusColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = table.status.label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Capacity",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${table.capacity} Seats",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = table.section,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                if (table.activeOrderId != null) {
                    Text(
                        text = table.activeOrderId,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TableActionDialog(
    table: TableInfo,
    onDismiss: () -> Unit,
    onUpdateStatus: (TableStatus) -> Unit,
    onTakeOrder: () -> Unit,
    onViewBill: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Table ${table.tableNumber} Operations") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Section: ${table.section}  |  Seats: ${table.capacity}")
                Text("Current Status: ${table.status.label}", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onTakeOrder,
                    modifier = Modifier.fillMaxWidth().testTag("table_dialog_take_order")
                ) {
                    Text("Take New / Add Order")
                }

                if (table.status == TableStatus.OCCUPIED || table.status == TableStatus.BILLED) {
                    OutlinedButton(
                        onClick = onViewBill,
                        modifier = Modifier.fillMaxWidth().testTag("table_dialog_settle_bill")
                    ) {
                        Text("View Bill & Checkout")
                    }
                }

                Text("Quick Status Override:", style = MaterialTheme.typography.labelSmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TableStatus.values().forEach { st ->
                        OutlinedButton(
                            onClick = { onUpdateStatus(st) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(st.name.take(3), fontSize = 10.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun AddTableDialog(
    onDismiss: () -> Unit,
    onSave: (TableInfo) -> Unit
) {
    var tableNum by remember { mutableStateOf("") }
    var capacityStr by remember { mutableStateOf("4") }
    var section by remember { mutableStateOf("Main Dining") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Table") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = tableNum,
                    onValueChange = { tableNum = it },
                    label = { Text("Table Number / Name (e.g. T-09)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = capacityStr,
                    onValueChange = { capacityStr = it },
                    label = { Text("Seating Capacity") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Dining Section (e.g. Patio, VIP)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tableNum.isNotBlank()) {
                        onSave(
                            TableInfo(
                                id = 0,
                                tableNumber = tableNum,
                                capacity = capacityStr.toIntOrNull() ?: 4,
                                status = TableStatus.AVAILABLE,
                                section = section
                            )
                        )
                    }
                }
            ) {
                Text("Save Table")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
