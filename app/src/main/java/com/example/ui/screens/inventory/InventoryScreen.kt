package com.example.ui.screens.inventory

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.domain.model.InventoryModel
import com.example.ui.PosViewModel

@Composable
fun InventoryScreen(viewModel: PosViewModel) {
    val inventory by viewModel.inventory.collectAsState()
    val config by viewModel.restaurantConfig.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Inventory & Raw Material Stock",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Track ingredient levels, unit costs & low stock alerts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(inventory) { item ->
                    val isLowStock = item.currentStock <= item.minThreshold
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("inventory_item_${item.itemName}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLowStock) Color(0xFFC62828).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    if (isLowStock) {
                                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                        Icon(Icons.Default.Warning, contentDescription = "Low", tint = Color(0xFFC62828))
                                    }
                                }
                                Text("${item.category}  •  Cost: ${config.currencySymbol}${String.format("%.2f", item.costPerUnit)}/${item.unit}", style = MaterialTheme.typography.bodySmall)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${item.currentStock} ${item.unit}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    Text("Min: ${item.minThreshold} ${item.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                IconButton(onClick = { viewModel.deleteInventory(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).testTag("add_inventory_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
        }

        if (showAddDialog) {
            var name by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("Produce") }
            var stockStr by remember { mutableStateOf("10.0") }
            var unit by remember { mutableStateOf("kg") }
            var costStr by remember { mutableStateOf("5.0") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add New Stock Item") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Meat, Dairy)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = stockStr, onValueChange = { stockStr = it }, label = { Text("Current Stock Quantity") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (kg, L, can, pcs)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = costStr, onValueChange = { costStr = it }, label = { Text("Cost Per Unit ($)") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            viewModel.saveInventory(
                                InventoryModel(
                                    itemName = name,
                                    category = category,
                                    currentStock = stockStr.toDoubleOrNull() ?: 10.0,
                                    minThreshold = 5.0,
                                    unit = unit,
                                    costPerUnit = costStr.toDoubleOrNull() ?: 5.0
                                )
                            )
                            showAddDialog = false
                        }
                    }) {
                        Text("Save Ingredient")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
