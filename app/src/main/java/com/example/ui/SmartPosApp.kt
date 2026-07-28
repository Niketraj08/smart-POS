package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.History
import com.example.R
import com.example.domain.model.UserRole
import com.example.ui.components.CurrentOrderDrawer
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.billing.BillingReceiptScreen
import com.example.ui.screens.customers.CustomerManagementScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.employees.EmployeeManagementScreen
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.kds.KitchenDisplayScreen
import com.example.ui.screens.menu.MenuManagementScreen
import com.example.ui.screens.orders.OrderHistoryScreen
import com.example.ui.screens.orders.OrderRegisterScreen
import com.example.ui.screens.qrmenu.QrMenuScreen
import com.example.ui.screens.reports.ReportsAnalyticsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.tables.TableManagementScreen

data class NavItem(val id: String, val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPosApp(viewModel: PosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val config by viewModel.restaurantConfig.collectAsState()

    var showRoleDropdown by remember { mutableStateOf(false) }

    if (currentUser == null) {
        LoginScreen(viewModel = viewModel)
        return
    }

    val navItems = listOf(
        NavItem("dashboard", "Dashboard", Icons.Default.Dashboard),
        NavItem("orders", "Register", Icons.Default.PointOfSale),
        NavItem("history", "Order History", Icons.Default.History),
        NavItem("tables", "Tables", Icons.Default.TableBar),
        NavItem("kds", "KDS", Icons.Default.Kitchen),
        NavItem("billing", "Billing", Icons.Default.Receipt),
        NavItem("menu", "Menu", Icons.Default.MenuBook),
        NavItem("qrmenu", "QR Scanner", Icons.Default.QrCode),
        NavItem("inventory", "Inventory", Icons.Default.Inventory),
        NavItem("customers", "Customers", Icons.Default.People),
        NavItem("employees", "Staff", Icons.Default.Badge),
        NavItem("reports", "Reports", Icons.Default.Assessment),
        NavItem("settings", "Settings", Icons.Default.Settings)
    )

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 720.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isCompact) 36.dp else 44.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = Color(0xFFD4AF37),
                                        shape = CircleShape
                                    )
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

                            Spacer(modifier = Modifier.width(if (isCompact) 8.dp else 12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Swad ",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = if (isCompact) 15.sp else 18.sp,
                                        color = Color(0xFFD4AF37) // Luxury Gold
                                    )
                                    Text(
                                        text = "Sutra",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = if (isCompact) 15.sp else 18.sp,
                                        color = Color(0xFFC62828) // Luxury Crimson
                                    )
                                }
                                if (!isCompact) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00E676))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Swad Sutra • Fine Dining & Live POS",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.navigateTo("qrmenu") },
                            modifier = Modifier
                                .testTag("topbar_qr_scanner_btn")
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .size(if (isCompact) 36.dp else 40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan QR Code",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(if (isCompact) 4.dp else 10.dp))

                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { showRoleDropdown = true }
                                    .padding(horizontal = if (isCompact) 8.dp else 14.dp, vertical = if (isCompact) 5.dp else 7.dp)
                                    .testTag("role_switcher_chip"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isCompact) "${currentUser?.role?.displayName}" else "Role: ${currentUser?.role?.displayName}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = if (isCompact) 11.sp else 12.sp
                                )
                            }

                            DropdownMenu(
                                expanded = showRoleDropdown,
                                onDismissRequest = { showRoleDropdown = false }
                            ) {
                                UserRole.values().forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role.displayName, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            viewModel.switchRole(role)
                                            showRoleDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(if (isCompact) 4.dp else 10.dp))

                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .testTag("logout_button")
                                .size(if (isCompact) 36.dp else 40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (isCompact) {
                    val bottomNavItems = listOf(
                        NavItem("dashboard", "Dashboard", Icons.Default.Dashboard),
                        NavItem("orders", "Orders", Icons.Default.PointOfSale),
                        NavItem("history", "History", Icons.Default.History),
                        NavItem("menu", "Menu", Icons.Default.MenuBook),
                        NavItem("settings", "Settings", Icons.Default.Settings)
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bottom_navigation_bar")
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = currentScreen == item.id
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.id) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_bottom_${item.id}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (!isCompact) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        navItems.forEach { item ->
                            val isSelected = currentScreen == item.id
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.id) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_rail_${item.id}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (currentScreen) {
                        "dashboard" -> DashboardScreen(viewModel = viewModel)
                        "orders" -> OrderRegisterScreen(viewModel = viewModel)
                        "history" -> OrderHistoryScreen(viewModel = viewModel)
                        "tables" -> TableManagementScreen(viewModel = viewModel)
                        "kds" -> KitchenDisplayScreen(viewModel = viewModel)
                        "billing" -> BillingReceiptScreen(viewModel = viewModel)
                        "menu" -> MenuManagementScreen(viewModel = viewModel)
                        "qrmenu" -> QrMenuScreen(viewModel = viewModel)
                        "inventory" -> InventoryScreen(viewModel = viewModel)
                        "customers" -> CustomerManagementScreen(viewModel = viewModel)
                        "employees" -> EmployeeManagementScreen(viewModel = viewModel)
                        "reports" -> ReportsAnalyticsScreen(viewModel = viewModel)
                        "settings" -> SettingsScreen(viewModel = viewModel)
                        else -> DashboardScreen(viewModel = viewModel)
                    }

                    CurrentOrderDrawer(
                        viewModel = viewModel,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
