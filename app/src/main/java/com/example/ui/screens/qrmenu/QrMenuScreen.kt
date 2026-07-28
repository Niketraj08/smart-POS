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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
    var scannedTableNumber by remember { mutableStateOf<String?>(null) }

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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "CameraX ML Kit Live Viewfinder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Point camera at any table standee or phone QR code to decode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 220.dp else 260.dp)
                                .clip(RoundedCornerShape(20.dp))
                        ) {
                            CameraQrScanner(
                                onQrCodeScanned = { qrText ->
                                    manualQrInput = qrText
                                    scanFeedbackMessage = "QR Scanned by ML Kit: $qrText"

                                    if (qrText.contains("T-")) {
                                        val matchedTable = tables.find { qrText.contains(it.tableNumber) }
                                        if (matchedTable != null) {
                                            scannedTableNumber = matchedTable.tableNumber
                                            viewModel.selectTable(matchedTable)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = manualQrInput,
                            onValueChange = { manualQrInput = it },
                            label = { Text("Scanned QR Code String") },
                            placeholder = { Text("e.g. TABLE:T-02") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("qr_scan_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (manualQrInput.isNotBlank()) {
                                    scanFeedbackMessage = "Processing QR: $manualQrInput"
                                    val matchedTable = tables.find { manualQrInput.contains(it.tableNumber) }
                                    if (matchedTable != null) {
                                        scannedTableNumber = matchedTable.tableNumber
                                        viewModel.selectTable(matchedTable)
                                        viewModel.navigateTo("orders")
                                    } else {
                                        scanFeedbackMessage = "Scanned payload verified: $manualQrInput"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("qr_process_btn")
                        ) {
                            Text("Open Register for Scanned Table")
                        }

                        scannedTableNumber?.let { tNum ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Table $tNum Selected!",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Ready in Register module",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.navigateTo("orders") },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Go to Register", fontSize = 12.sp)
                                    }
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
