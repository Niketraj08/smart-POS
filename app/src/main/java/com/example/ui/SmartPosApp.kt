package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserRole
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.billing.BillingReceiptScreen
import com.example.ui.screens.customers.CustomerManagementScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.employees.EmployeeManagementScreen
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.kds.KitchenDisplayScreen
import com.example.ui.screens.menu.MenuManagementScreen
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
        NavItem("tables", "Tables", Icons.Default.TableBar),
        NavItem("kds", "KDS", Icons.Default.Kitchen),
        NavItem("billing", "Billing", Icons.Default.Receipt),
        NavItem("menu", "Menu", Icons.Default.MenuBook),
        NavItem("qrmenu", "QR Code", Icons.Default.QrCode),
        NavItem("inventory", "Inventory", Icons.Default.Inventory),
        NavItem("customers", "Customers", Icons.Default.People),
        NavItem("employees", "Staff", Icons.Default.Badge),
        NavItem("reports", "Reports", Icons.Default.Assessment),
        NavItem("settings", "Settings", Icons.Default.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = "Logo",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(config.restaurantName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("SmartPOS v1.0", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                        }
                    }
                },
                actions = {
                    // Role Switcher Dropdown Chip
                    Box {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { showRoleDropdown = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("role_switcher_chip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Role: ${currentUser?.role?.displayName}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp
                            )
                        }

                        DropdownMenu(
                            expanded = showRoleDropdown,
                            onDismissRequest = { showRoleDropdown = false }
                        ) {
                            UserRole.values().forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role.displayName) },
                                    onClick = {
                                        viewModel.switchRole(role)
                                        showRoleDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Side Navigation Rail
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                navItems.forEach { item ->
                    NavigationRailItem(
                        selected = currentScreen == item.id,
                        onClick = { viewModel.navigateTo(item.id) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_rail_${item.id}")
                    )
                }
            }

            // Screen Content Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    "dashboard" -> DashboardScreen(viewModel = viewModel)
                    "orders" -> OrderRegisterScreen(viewModel = viewModel)
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
            }
        }
    }
}
