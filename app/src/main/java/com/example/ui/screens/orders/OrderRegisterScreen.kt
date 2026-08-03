package com.example.ui.screens.orders

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TableBar
import com.example.ui.components.VoiceOrderAssistantDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.CartItem
import com.example.domain.model.MenuItemModel
import com.example.domain.model.OrderType
import com.example.ui.PosViewModel

@Composable
fun OrderRegisterScreen(viewModel: PosViewModel) {
    val categories by viewModel.categories.collectAsState()
    val filteredItems by viewModel.filteredMenuItems.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val cart by viewModel.cart.collectAsState()
    val orderType by viewModel.orderType.collectAsState()
    val selectedTable by viewModel.selectedTable.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val discount by viewModel.discountAmount.collectAsState()
    val config by viewModel.restaurantConfig.collectAsState()

    var showDiscountDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var itemForNotes by remember { mutableStateOf<CartItem?>(null) }
    var selectedMobileTab by remember { mutableStateOf(0) } // 0: Menu, 1: Cart

    val subtotal = cart.sumOf { it.subtotal }
    val gstTax = cart.sumOf { it.gstAmount }
    val grandTotal = (subtotal - discount) + gstTax

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 700.dp

        if (isCompact) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Mobile Tab Switcher
                androidx.compose.material3.TabRow(selectedTabIndex = selectedMobileTab) {
                    androidx.compose.material3.Tab(
                        selected = selectedMobileTab == 0,
                        onClick = { selectedMobileTab = 0 },
                        text = { Text("1. Menu Items", fontWeight = FontWeight.Bold) }
                    )
                    androidx.compose.material3.Tab(
                        selected = selectedMobileTab == 1,
                        onClick = { selectedMobileTab = 1 },
                        text = {
                            Text(
                                "2. Cart (${cart.sumOf { it.quantity }})",
                                fontWeight = FontWeight.Bold,
                                color = if (cart.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }

                if (selectedMobileTab == 0) {
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            // --- Active Scanned Phone & Table Details Session Banner ---
                            if (selectedTable != null || selectedCustomer != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF8C1D11).copy(alpha = 0.08f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .border(1.dp, Color(0xFF8C1D11).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF8C1D11))
                                                    .padding(6.dp)
                                            ) {
                                                Icon(Icons.Default.TableBar, contentDescription = "Table", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Table: ${selectedTable?.tableNumber ?: "T-01"}",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF8C1D11)
                                                    )
                                                    if ((selectedCustomer?.loyaltyPoints ?: 0) >= 100) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(Color(0xFFD4AF37))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("VIP GUEST", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "Guest: ${selectedCustomer?.name ?: "Walk-in Guest"} • Mobile: ${selectedCustomer?.phone ?: "Not set"}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.selectTable(null)
                                                viewModel.selectCustomer(null)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            // Search & Table Badge & AI Voice Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    placeholder = { Text("Search menu...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                    modifier = Modifier.weight(1f).testTag("pos_search_input"),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = { showVoiceDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C1D11)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(54.dp).testTag("open_voice_order_button")
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Voice", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Categories Filter Chips
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCatId == null,
                                        onClick = { viewModel.selectCategory(null) },
                                        label = { Text("All") }
                                    )
                                }
                                items(categories) { cat ->
                                    FilterChip(
                                        selected = selectedCatId == cat.id,
                                        onClick = { viewModel.selectCategory(cat.id) },
                                        label = { Text(cat.name) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Menu Items Grid
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 140.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredItems) { item ->
                                    PosItemTile(
                                        item = item,
                                        currencySymbol = config.currencySymbol,
                                        onClick = { viewModel.addToCart(item) }
                                    )
                                }
                            }
                        }

                        // Floating View Cart Bar if items exist
                        if (cart.isNotEmpty()) {
                            Button(
                                onClick = { selectedMobileTab = 1 },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .testTag("floating_view_cart_btn"),
                                shape = RoundedCornerShape(14.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("View Cart (${cart.sumOf { it.quantity }} items)", fontWeight = FontWeight.Bold)
                                    Text("${config.currencySymbol}${String.format("%.2f", grandTotal)}  →", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Mobile Order Cart Panel
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Order Summary Ticket",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(OrderType.values()) { type ->
                                val isSelected = orderType == type
                                Button(
                                    onClick = { viewModel.setOrderType(type) },
                                    modifier = Modifier.testTag("order_type_${type.name}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(type.label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Cart Items List
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cart) { item ->
                                CartRowItem(
                                    item = item,
                                    currencySymbol = config.currencySymbol,
                                    onAdd = { viewModel.updateCartQuantity(item, 1) },
                                    onMinus = { viewModel.updateCartQuantity(item, -1) },
                                    onNotes = { itemForNotes = item }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Summary Billing Math
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", style = MaterialTheme.typography.bodySmall)
                                Text("${config.currencySymbol}${String.format("%.2f", subtotal)}", style = MaterialTheme.typography.bodySmall)
                            }

                            if (discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Discount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text("-${config.currencySymbol}${String.format("%.2f", discount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("GST Tax (5%)", style = MaterialTheme.typography.bodySmall)
                                Text("${config.currencySymbol}${String.format("%.2f", gstTax)}", style = MaterialTheme.typography.bodySmall)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    text = "${config.currencySymbol}${String.format("%.2f", grandTotal)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { showDiscountDialog = true },
                                modifier = Modifier.weight(1f).testTag("apply_discount_btn")
                            ) {
                                Text("Discount", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.sendOrderToKitchen { createdOrder ->
                                        viewModel.navigateTo("kds")
                                    }
                                },
                                enabled = cart.isNotEmpty(),
                                modifier = Modifier.weight(1.5f).testTag("send_to_kitchen_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Send to Kitchen", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Wide Screen Split View
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel: Menu Items Grid (60% width)
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    // --- Active Scanned Phone & Table Details Session Banner ---
                    if (selectedTable != null || selectedCustomer != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF8C1D11).copy(alpha = 0.08f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(1.dp, Color(0xFF8C1D11).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFF8C1D11))
                                            .padding(8.dp)
                                    ) {
                                        Icon(Icons.Default.TableBar, contentDescription = "Table", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Table: ${selectedTable?.tableNumber ?: "T-01"}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF8C1D11)
                                            )
                                            if ((selectedCustomer?.loyaltyPoints ?: 0) >= 100) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFD4AF37))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text("VIP GUEST", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                        Text(
                                            text = "Guest Name: ${selectedCustomer?.name ?: "Walk-in Guest"} • Mobile: ${selectedCustomer?.phone ?: "Not set"}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.selectTable(null)
                                        viewModel.selectCustomer(null)
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        }
                    }

                    // Search & Category Bar with AI Voice Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search dish or beverage...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            modifier = Modifier.weight(1f).testTag("pos_search_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = { showVoiceDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C1D11)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(54.dp).testTag("open_voice_order_button_expanded")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Voice Order", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Categories Filter Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedCatId == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("All") }
                        )
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCatId == cat.id,
                                onClick = { viewModel.selectCategory(cat.id) },
                                label = { Text(cat.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Menu Items Grid
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredItems) { item ->
                            PosItemTile(
                                item = item,
                                currencySymbol = config.currencySymbol,
                                onClick = { viewModel.addToCart(item) }
                            )
                        }
                    }
                }

                // Right Panel: Active Order Ticket (40% width)
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(top = 16.dp, end = 16.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Ticket Header: Order Type Selector
                        Text(
                            text = "Active Ticket",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OrderType.values().forEach { type ->
                                val isSelected = orderType == type
                                Button(
                                    onClick = { viewModel.setOrderType(type) },
                                    modifier = Modifier.weight(1f).testTag("order_type_${type.name}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(type.label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cart Items List
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cart) { item ->
                                CartRowItem(
                                    item = item,
                                    currencySymbol = config.currencySymbol,
                                    onAdd = { viewModel.updateCartQuantity(item, 1) },
                                    onMinus = { viewModel.updateCartQuantity(item, -1) },
                                    onNotes = { itemForNotes = item }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Summary Billing Math
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", style = MaterialTheme.typography.bodySmall)
                                Text("${config.currencySymbol}${String.format("%.2f", subtotal)}", style = MaterialTheme.typography.bodySmall)
                            }

                            if (discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Discount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text("-${config.currencySymbol}${String.format("%.2f", discount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("GST Tax (5%)", style = MaterialTheme.typography.bodySmall)
                                Text("${config.currencySymbol}${String.format("%.2f", gstTax)}", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    text = "${config.currencySymbol}${String.format("%.2f", grandTotal)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { showDiscountDialog = true },
                                modifier = Modifier.weight(1f).testTag("apply_discount_btn")
                            ) {
                                Text("Discount")
                            }

                            Button(
                                onClick = {
                                    viewModel.sendOrderToKitchen { createdOrder ->
                                        viewModel.navigateTo("kds")
                                    }
                                },
                                enabled = cart.isNotEmpty(),
                                modifier = Modifier.weight(1.5f).testTag("send_to_kitchen_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send to Kitchen")
                            }
                        }
                    }
                }
            }
        }
    }

    // Special Notes Modal Dialog
    itemForNotes?.let { cartItem ->
        var notesText by remember { mutableStateOf(cartItem.specialNotes) }
        AlertDialog(
            onDismissRequest = { itemForNotes = null },
            title = { Text("Kitchen Notes: ${cartItem.menuItem.name}") },
            text = {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Instructions (e.g. Extra spicy, No onions)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateCartItemNotes(cartItem, notesText)
                    itemForNotes = null
                }) {
                    Text("Save Notes")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemForNotes = null }) { Text("Cancel") }
            }
        )
    }

    // Discount Dialog
    if (showDiscountDialog) {
        var discStr by remember { mutableStateOf(discount.toString()) }
        AlertDialog(
            onDismissRequest = { showDiscountDialog = false },
            title = { Text("Apply Order Discount") },
            text = {
                OutlinedTextField(
                    value = discStr,
                    onValueChange = { discStr = it },
                    label = { Text("Discount Amount ($)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setDiscount(discStr.toDoubleOrNull() ?: 0.0)
                    showDiscountDialog = false
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscountDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Gemini AI Voice Order Dialog
    if (showVoiceDialog) {
        VoiceOrderAssistantDialog(
            viewModel = viewModel,
            onDismiss = { showVoiceDialog = false }
        )
    }
}

@Composable
private fun PosItemTile(
    item: MenuItemModel,
    currencySymbol: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pos_tile_${item.name}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Food Thumbnail Header Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_food_special),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                // Veg / Non-Veg Indicator Badge on top-left of image
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .border(1.dp, if (item.isVeg) Color(0xFF2E7D32) else Color(0xFFC62828), RoundedCornerShape(4.dp))
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (item.isVeg) Color(0xFF2E7D32) else Color(0xFFC62828), CircleShape)
                    )
                }

                // Rating Badge on top-right of image
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "4.8 ★",
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.categoryName.ifBlank { "Special" },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currencySymbol${String.format("%.2f", item.price)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Add",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartRowItem(
    item: CartItem,
    currencySymbol: String,
    onAdd: () -> Unit,
    onMinus: () -> Unit,
    onNotes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.menuItem.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = "$currencySymbol${String.format("%.2f", item.subtotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (item.specialNotes.isNotBlank()) {
                    Text(
                        text = "Note: ${item.specialNotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(onClick = onNotes, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.EditNote, contentDescription = "Notes", tint = MaterialTheme.colorScheme.secondary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMinus, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Minus")
                }
                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    }
}
