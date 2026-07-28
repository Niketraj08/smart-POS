package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.OrderStatus
import com.example.domain.model.TableStatus
import com.example.domain.util.QrCodeGenerator
import com.example.ui.PosViewModel
import com.example.ui.components.CameraQrScanner

data class PosModuleShortcut(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val badge: String? = null
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

    var activeQrTableIndex by remember { mutableStateOf(0) }
    var qrTypeIndex by remember { mutableStateOf(0) }
    val selectedTable = tables.getOrNull(activeQrTableIndex)

    val qrPayload = when (qrTypeIndex) {
        0 -> "https://smartpos.menu/dine-in?table=${selectedTable?.tableNumber ?: "T-01"}&rest=${config.restaurantName}"
        1 -> "WIFI:S:${config.restaurantName}_Guest_WiFi;T:WPA;P:dining2026;;"
        2 -> "LOYALTY:${currentUser?.name ?: "Guest"}_PASS"
        else -> "upi://pay?pa=smartpos@merchant&pn=${config.restaurantName}&am=25.00&tn=Table_${selectedTable?.tableNumber ?: "T-01"}"
    }

    val qrBitmap = remember(qrPayload) {
        QrCodeGenerator.generateQrBitmap(qrPayload, size = 320)
    }

    val modules = listOf(
        PosModuleShortcut("orders", "Order Register", "Take dine-in, takeaway & delivery", Icons.Default.PointOfSale, Color(0xFFFF6D00), "$openOrdersCount Active"),
        PosModuleShortcut("tables", "Table Floor Plan", "Visual seating & table orders", Icons.Default.TableBar, Color(0xFF1E88E5), "$occupiedTablesCount/$totalTables Occupied"),
        PosModuleShortcut("kds", "Kitchen Display", "Live order tickets & cooking", Icons.Default.Kitchen, Color(0xFF43A047), "$pendingKdsCount Pending"),
        PosModuleShortcut("billing", "Billing & Tax", "Thermal receipt print & PDF", Icons.Default.Receipt, Color(0xFFE53935)),
        PosModuleShortcut("menu", "Menu Master", "Manage dishes & pricing", Icons.Default.MenuBook, Color(0xFF8E24AA)),
        PosModuleShortcut("qrmenu", "QR Code Hub", "CameraX ML Kit Scanner & Codes", Icons.Default.QrCodeScanner, Color(0xFF00897B), "Real ML Kit"),
        PosModuleShortcut("inventory", "Inventory", "Stock alerts & ingredients", Icons.Default.Inventory, Color(0xFFFB8C00)),
        PosModuleShortcut("customers", "Customers", "Loyalty points & history", Icons.Default.People, Color(0xFF039BE5)),
        PosModuleShortcut("employees", "Staff Roster", "Employee PINs & permissions", Icons.Default.Badge, Color(0xFF6D4C41)),
        PosModuleShortcut("reports", "Analytics", "Sales graphs & tax audit", Icons.Default.Assessment, Color(0xFF3949AB)),
        PosModuleShortcut("settings", "POS Settings", "Restaurant & printer config", Icons.Default.Settings, Color(0xFF546E7A))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Branding Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_pos_hero_banner),
                    contentDescription = "Restaurant Interior",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark Gradient Scrim Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.88f),
                                    Color.Black.copy(alpha = 0.45f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB300)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_restaurant_logo),
                                    contentDescription = "Logo Badge",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Gourmet POS Suite",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Welcome, ${currentUser?.name ?: "Manager"}!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "${config.restaurantName} • Real-time Dine-In & ML Barcode Scanner Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }

                    Button(
                        onClick = { viewModel.navigateTo("qrmenu") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("hero_camera_scan_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Camera Scanner",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Camera Scanner", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // KPI Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricGradientCard(
                title = "Today's Sales",
                value = "${config.currencySymbol}${String.format("%.2f", totalSales)}",
                badge = "+14.2% vs yesterday",
                gradient = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)),
                modifier = Modifier.weight(1f)
            )

            MetricGradientCard(
                title = "Active Dine-In Orders",
                value = "$openOrdersCount",
                badge = "In Kitchen & Register",
                gradient = listOf(Color(0xFFE65100), Color(0xFFF57C00)),
                modifier = Modifier.weight(1f)
            )

            MetricGradientCard(
                title = "Table Floor Occupancy",
                value = "$occupiedTablesCount / $totalTables",
                badge = "${totalTables - occupiedTablesCount} Tables Available",
                gradient = listOf(Color(0xFF0D47A1), Color(0xFF1976D2)),
                modifier = Modifier.weight(1f)
            )

            MetricGradientCard(
                title = "Live KDS Tickets",
                value = "$pendingKdsCount",
                badge = "Cooking Items",
                gradient = listOf(Color(0xFFB71C1C), Color(0xFFD32F2F)),
                modifier = Modifier.weight(1f)
            )
        }

        // Main Front Page Split: Real QR Code Showcase & Live Camera Scanner Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Real Standard QR Code Standee Card
            ElevatedCard(
                modifier = Modifier
                    .weight(1.1f)
                    .height(340.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Real QR Code",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Real Standard QR Code Standee",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ISO Standard",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // QR Category Tabs
                    ScrollableTabRow(
                        selectedTabIndex = qrTypeIndex,
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Table Dine-In", "Wi-Fi Access", "Loyalty Pass", "UPI Payment").forEachIndexed { index, label ->
                            Tab(
                                selected = qrTypeIndex == index,
                                onClick = { qrTypeIndex = index },
                                text = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // QR Image
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .size(180.dp)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Standard QR Code",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Table Selector & QR Payload Metadata
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (qrTypeIndex) {
                                    0 -> "Scan to Self-Order Dine-In Menu"
                                    1 -> "Connect Guest Wi-Fi"
                                    2 -> "Scan Member Loyalty Card"
                                    else -> "Instant Table Payment QR"
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (qrTypeIndex == 0) {
                                Text(
                                    text = "Select Table Number:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    tables.take(4).forEachIndexed { index, table ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (activeQrTableIndex == index) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { activeQrTableIndex = index }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = table.tableNumber,
                                                color = if (activeQrTableIndex == index) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = qrPayload,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.navigateTo("qrmenu") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Scan with ML Kit Camera")
                            }
                        }
                    }
                }
            }

            // Live CameraX ML Kit Preview Widget on Front Page
            ElevatedCard(
                modifier = Modifier
                    .weight(0.9f)
                    .height(340.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Live Scanner",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live CameraX QR Scanner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ML Kit", color = Color(0xFF00C853), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    CameraQrScanner(
                        onQrCodeScanned = { scannedCode ->
                            if (scannedCode.contains("T-")) {
                                val matchedTable = tables.find { scannedCode.contains(it.tableNumber) }
                                if (matchedTable != null) {
                                    viewModel.selectTable(matchedTable)
                                    viewModel.navigateTo("orders")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Text(
            text = "Restaurant POS Modules & Operations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // POS Operations Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 220.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            items(modules) { module ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .testTag("shortcut_${module.id}")
                        .clickable { viewModel.navigateTo(module.id) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(module.color.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = module.icon,
                                    contentDescription = module.title,
                                    tint = module.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            module.badge?.let { badgeText ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(module.color.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        color = module.color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = module.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = module.description,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
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
private fun MetricGradientCard(
    title: String,
    value: String,
    badge: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = badge,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
