package com.example.ui.screens.qrmenu

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.util.QrCodeGenerator
import com.example.ui.PosViewModel

@Composable
fun QrMenuScreen(viewModel: PosViewModel) {
    val tables by viewModel.tables.collectAsState()
    val config by viewModel.restaurantConfig.collectAsState()

    var selectedTableIndex by remember { mutableStateOf(0) }
    var scannedQrInput by remember { mutableStateOf("") }
    var scanFeedback by remember { mutableStateOf<String?>(null) }

    val currentTable = tables.getOrNull(selectedTableIndex)
    val qrPayload = "https://smartpos.menu/dine-in?table=${currentTable?.tableNumber ?: "T-01"}&rest=${config.restaurantName}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "QR Code Digital Menu & Scanner",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Dine-in self-ordering QR codes & CameraX scanner simulator",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Column: Table QR Generator Card
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Table QR Standee Generator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (tables.isNotEmpty()) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTableIndex,
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // QR Code Display Box
                    currentTable?.let { table ->
                        val qrBitmap = remember(qrPayload) {
                            QrCodeGenerator.generateQrBitmap(qrPayload, 400)
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = config.restaurantName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Scan to Order • Table ${table.tableNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Table QR Code",
                                    modifier = Modifier.size(200.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = qrPayload,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Right Column: CameraX / ZXing QR Scanner Simulator Card
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "CameraX / ZXing QR Scanner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scan guest QR codes or loyalty passes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Camera Viewfinder Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Camera Viewfinder",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "[ Simulated Camera Preview ]",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = scannedQrInput,
                        onValueChange = { scannedQrInput = it },
                        label = { Text("Scan / Type QR Code Data") },
                        placeholder = { Text("e.g. TABLE:T-03 or CUST:Emily") },
                        modifier = Modifier.fillMaxWidth().testTag("qr_scan_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (scannedQrInput.isNotBlank()) {
                                scanFeedback = "QR Code Successfully Verified: $scannedQrInput"
                                if (scannedQrInput.contains("T-")) {
                                    val matchedTable = tables.find { scannedQrInput.contains(it.tableNumber) }
                                    if (matchedTable != null) {
                                        viewModel.selectTable(matchedTable)
                                        viewModel.navigateTo("orders")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("qr_process_btn")
                    ) {
                        Text("Process Scanned QR Code")
                    }

                    scanFeedback?.let { feedback ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = feedback,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
