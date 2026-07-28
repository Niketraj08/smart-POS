package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.OrderStatus
import com.example.domain.model.TableStatus
import com.example.domain.model.UserRole
import com.example.ui.PosViewModel

data class PosModuleShortcut(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun DashboardScreen(viewModel: PosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val tables by viewModel.tables.collectAsState()
    val kitchenOrders by viewModel.kitchenOrders.collectAsState()
    val config by viewModel.restaurantConfig.collectAsState()

    val totalSales = orders.sumOf { it.totalAmount }
    val openOrdersCount = orders.count { it.status in listOf(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY) }
    val occupiedTablesCount = tables.count { it.status == TableStatus.OCCUPIED }
    val totalTables = tables.size
    val pendingKdsCount = kitchenOrders.sumOf { order -> order.items.count { it.status == OrderStatus.PENDING } }

    val modules = listOf(
        PosModuleShortcut("orders", "Order Register", "Take dine-in, takeaway & delivery orders", Icons.Default.PointOfSale, Color(0xFFE65100)),
        PosModuleShortcut("tables", "Table Floor Plan", "Visual table status, seating & orders", Icons.Default.TableBar, Color(0xFF1976D2)),
        PosModuleShortcut("kds", "Kitchen Display (KDS)", "Live order tickets & item cooking state", Icons.Default.Kitchen, Color(0xFF388E3C)),
        PosModuleShortcut("billing", "Billing & Receipt", "Print thermal bills & export PDF invoices", Icons.Default.Receipt, Color(0xFFD32F2F)),
        PosModuleShortcut("menu", "Menu Management", "Manage dishes, pricing, & categories", Icons.Default.MenuBook, Color(0xFF7B1FA2)),
        PosModuleShortcut("qrmenu", "QR Code Dine-In", "Generate table QR codes & self-order link", Icons.Default.QrCode, Color(0xFF00796B)),
        PosModuleShortcut("inventory", "Inventory & Stock", "Raw ingredients, stock audit & alerts", Icons.Default.Inventory, Color(0xFFF57C00)),
        PosModuleShortcut("customers", "Customer Directory", "Loyalty points & purchase history", Icons.Default.People, Color(0xFF0288D1)),
        PosModuleShortcut("employees", "Staff Roster", "Employee PINs, roles & permissions", Icons.Default.Badge, Color(0xFF5D4037)),
        PosModuleShortcut("reports", "Reports & Analytics", "Sales graphs, GST tax breakdown & charts", Icons.Default.Assessment, Color(0xFF303F9F)),
        PosModuleShortcut("settings", "POS Settings", "Restaurant info, printer & tax config", Icons.Default.Settings, Color(0xFF455A64))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome Banner
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Welcome back, ${currentUser?.name ?: "Staff"}!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${config.restaurantName} • Active Role: ${currentUser?.role?.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentUser?.role?.name ?: "STAFF",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // KPI Summary Metric Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Today's Sales",
                value = "${config.currencySymbol}${String.format("%.2f", totalSales)}",
                subtitle = "${orders.size} Total Orders",
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Open Orders",
                value = "$openOrdersCount",
                subtitle = "Active in Register",
                color = Color(0xFFE65100),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Active Tables",
                value = "$occupiedTablesCount / $totalTables",
                subtitle = "${totalTables - occupiedTablesCount} Available",
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "KDS Tickets",
                value = "$pendingKdsCount",
                subtitle = "Items Cooking",
                color = Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "POS Modules & Operations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Module Shortcuts Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 220.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(modules) { module ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("shortcut_${module.id}")
                        .clickable { viewModel.navigateTo(module.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(module.color.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = module.icon,
                                contentDescription = module.title,
                                tint = module.color,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = module.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = module.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
