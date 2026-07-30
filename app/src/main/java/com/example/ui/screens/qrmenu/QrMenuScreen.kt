package com.example.ui.screens.qrmenu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.CustomerModel
import com.example.domain.model.TableStatus
import com.example.domain.util.QrCodeGenerator
import com.example.ui.PosViewModel
import com.example.ui.components.CameraQrScanner

@Composable
fun QrMenuScreen(viewModel: PosViewModel) {
    val tables by viewModel.tables.collectAsState()
    val config by viewModel.restaurantConfig.collectAsState()

    var selectedTableIndex by remember { mutableStateOf(0) }
    var selectedQrTypeIndex by remember { mutableStateOf(0) }
    var manualQrInput by remember { mutableStateOf("") }
    var scanFeedbackMessage by remember { mutableStateOf<String?>(null) }
    
    // Scanner Form Fields
    var scannedTableNumber by remember { mutableStateOf("T-01") }
    var isVipGuest by remember { mutableStateOf(false) }
    var guestName by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("") }
    var guestCount by remember { mutableIntStateOf(2) }
    var diningNotes by remember { mutableStateOf("") }
    var isDetailsFormOpen by remember { mutableStateOf(false) }

    fun processQrString(qrText: String) {
        manualQrInput = qrText
        scanFeedbackMessage = "Scanned payload: $qrText"

        // 1. Try URL parameter extraction (e.g. https://swadsutra.app/menu?table=T-03&name=Niket&vip=true&guests=4&phone=9876543210)
        var extractedTable = ""
        var extractedName = ""
        var extractedVip = false
        var extractedPax = 2
        var extractedPhone = ""

        try {
            if (qrText.contains("?") || qrText.contains("&") || qrText.contains("=")) {
                val queryParams = qrText.substringAfter("?", qrText)
                val pairs = queryParams.split("&")
                for (pair in pairs) {
                    val parts = pair.split("=")
                    if (parts.size == 2) {
                        val key = parts[0].lowercase().trim()
                        val value = try { java.net.URLDecoder.decode(parts[1], "UTF-8").trim() } catch (e: Exception) { parts[1].trim() }
                        when {
                            key in listOf("table", "tbl", "t", "tablenumber") -> extractedTable = value.uppercase()
                            key in listOf("name", "guest", "customer", "user") -> extractedName = value
                            key in listOf("vip", "is_vip", "premium") -> extractedVip = value.lowercase() in listOf("true", "1", "yes")
                            key in listOf("guests", "pax", "count", "people") -> extractedPax = value.toIntOrNull() ?: 2
                            key in listOf("phone", "mobile", "tel") -> extractedPhone = value
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Regex fallbacks for unstructured text
        if (extractedTable.isBlank()) {
            val tableMatch = Regex("(?i)(?:table|tbl|t)[=_:\\s]*([A-Za-z0-9-]+)").find(qrText)
            if (tableMatch != null) {
                extractedTable = tableMatch.groupValues[1].uppercase()
            } else if (qrText.contains("T-", ignoreCase = true)) {
                val matched = Regex("(?i)T-\\d+").find(qrText)
                if (matched != null) extractedTable = matched.value.uppercase()
            }
        }

        if (extractedName.isBlank()) {
            val nameMatch = Regex("(?i)(?:name|guest|customer)[=_:\\s]*([A-Za-z\\s]+)").find(qrText)
            if (nameMatch != null) extractedName = nameMatch.groupValues[1].trim()
        }

        if (!extractedVip && qrText.contains("VIP", ignoreCase = true)) {
            extractedVip = true
        }

        if (extractedPhone.isBlank()) {
            val phoneMatch = Regex("\\b\\d{10}\\b").find(qrText)
            if (phoneMatch != null) extractedPhone = phoneMatch.value
        }

        if (extractedTable.isNotBlank()) scannedTableNumber = extractedTable
        if (extractedName.isNotBlank()) guestName = extractedName
        if (extractedPhone.isNotBlank()) guestPhone = extractedPhone
        isVipGuest = extractedVip
        guestCount = extractedPax
        isDetailsFormOpen = true
    }

    val currentTable = tables.getOrNull(selectedTableIndex)

    val activeQrPayload = when (selectedQrTypeIndex) {
        0 -> "https://smartpos.menu/dine-in?table=${currentTable?.tableNumber ?: "T-01"}&rest=${config.restaurantName}"
        1 -> "WIFI:S:${config.restaurantName}_Guest_WiFi;T:WPA;P:dining2026;;"
        2 -> "LOYALTY:CUST_MEMBER_PASS"
        else -> "upi://pay?pa=smartpos@merchant&pn=${config.restaurantName}&am=15.00&tn=Table_${currentTable?.tableNumber ?: "T-01"}"
    }

    val realQrBitmap = remember(activeQrPayload) {
        QrCodeGenerator.generateQrBitmap(activeQrPayload, size = 450)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 750.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(if (isCompact) 12.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "QR Code Hub & ML Kit Camera Scanner",
                        style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Generate real ISO standard table QR codes & scan with CameraX + ML Kit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "ML Kit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ML Kit Active",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            @Composable
            fun GeneratorContent() {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Real QR Standee Generator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ScrollableTabRow(
                            selectedTabIndex = selectedQrTypeIndex,
                            edgePadding = 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Table Dine-In", "Wi-Fi Access", "Loyalty Card", "Payment QR").forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedQrTypeIndex == index,
                                    onClick = { selectedQrTypeIndex = index },
                                    text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedQrTypeIndex == 0 && tables.isNotEmpty()) {
                            Text(
                                text = "Select Table Standee:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ScrollableTabRow(
                                selectedTabIndex = selectedTableIndex,
                                edgePadding = 0.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                tables.forEachIndexed { index, table ->
                                    Tab(
                                        selected = selectedTableIndex == index,
                                        onClick = { selectedTableIndex = index },
                                        text = { Text(table.tableNumber) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Printable Standee Card View
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth(if (isCompact) 0.95f else 0.85f)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_restaurant_logo),
                                            contentDescription = "Logo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = config.restaurantName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = when (selectedQrTypeIndex) {
                                        0 -> "Scan to View Menu & Order"
                                        1 -> "Scan for Instant Wi-Fi Access"
                                        2 -> "Scan Member Pass for Points"
                                        else -> "Scan to Pay Bill via UPI"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    fontSize = 11.sp
                                )

                                if (selectedQrTypeIndex == 0) {
                                    Text(
                                        text = "Table ${currentTable?.tableNumber ?: "T-01"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Image(
                                    bitmap = realQrBitmap.asImageBitmap(),
                                    contentDescription = "ISO QR Code",
                                    modifier = Modifier.size(if (isCompact) 180.dp else 220.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = activeQrPayload,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    manualQrInput = activeQrPayload
                                    scanFeedbackMessage = "QR payload copied to scanner input"
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Payload", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    scanFeedbackMessage = "Standee sent to thermal printer!"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Print Standee", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            @Composable
            fun ScannerContent() {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qr_scanner_details_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CameraX ML Kit Live Viewfinder",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Point camera at table standee or phone QR to auto-fill details",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isVipGuest) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFD4AF37))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = "VIP", tint = Color.Black, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("VIP TABLE", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 200.dp else 240.dp)
                                .clip(RoundedCornerShape(18.dp))
                        ) {
                            CameraQrScanner(
                                onQrCodeScanned = { qrText ->
                                    processQrString(qrText)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = manualQrInput,
                            onValueChange = { input ->
                                processQrString(input)
                            },
                            label = { Text("Scanned QR Code String / Phone Link") },
                            placeholder = { Text("e.g. https://swadsutra.app/menu?table=T-02&name=Niket&vip=true&phone=9876543210") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("qr_scan_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- SCANNED TABLE & GUEST DETAILS FORM ---
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFF8C1D11).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Scanned Table & Guest Registration",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF8C1D11)
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("VIP Table", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Switch(
                                            checked = isVipGuest,
                                            onCheckedChange = { isVipGuest = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFFD4AF37)
                                            ),
                                            modifier = Modifier.testTag("vip_guest_switch")
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = scannedTableNumber,
                                        onValueChange = { scannedTableNumber = it },
                                        label = { Text("Table Number") },
                                        placeholder = { Text("e.g. T-01") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("scanned_table_field"),
                                        singleLine = true
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(top = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Guest Count (Log)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            IconButton(
                                                onClick = { if (guestCount > 1) guestCount-- },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                            }
                                            Text(
                                                text = "$guestCount",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp)
                                            )
                                            IconButton(
                                                onClick = { guestCount++ },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = guestName,
                                        onValueChange = { guestName = it },
                                        label = { Text("Guest / Customer Name") },
                                        placeholder = { Text("e.g. Niket Raj") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF8C1D11)) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("scanned_guest_name_field"),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = guestPhone,
                                        onValueChange = { guestPhone = it },
                                        label = { Text("Mobile Number") },
                                        placeholder = { Text("e.g. 9876543210") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("scanned_guest_phone_field"),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = diningNotes,
                                    onValueChange = { diningNotes = it },
                                    label = { Text("Special Dining Requests / Notes") },
                                    placeholder = { Text("e.g. Birthday Celebration / AC Seating") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val matchedTable = tables.find { it.tableNumber.equals(scannedTableNumber, ignoreCase = true) }
                                        if (matchedTable != null) {
                                            viewModel.selectTable(matchedTable)
                                            viewModel.updateTableStatus(matchedTable.id, TableStatus.OCCUPIED)
                                        }
                                        val finalCustomerName = if (guestName.isNotBlank()) guestName else "Guest (${scannedTableNumber})"
                                        val finalPhone = if (guestPhone.isNotBlank()) guestPhone else "9876543210"
                                        val customerModel = CustomerModel(
                                            id = System.currentTimeMillis().toInt(),
                                            name = finalCustomerName,
                                            phone = finalPhone,
                                            email = "",
                                            loyaltyPoints = if (isVipGuest) 100 else 10,
                                            totalSpent = 0.0
                                        )
                                        viewModel.selectCustomer(customerModel)
                                        scanFeedbackMessage = "Table $scannedTableNumber (${if (isVipGuest) "VIP" else "Regular"}) with $guestCount guests saved! Opening Menu..."
                                        viewModel.navigateTo("orders")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("enter_open_menu_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C1D11))
                                ) {
                                    Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu", tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ENTER & OPEN MENU", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                }
                            }
                        }

                        scanFeedbackMessage?.let { feedback ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = feedback,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            if (isCompact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GeneratorContent()
                    ScannerContent()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) { GeneratorContent() }
                    Box(modifier = Modifier.weight(1f)) { ScannerContent() }
                }
            }
        }
    }
}
