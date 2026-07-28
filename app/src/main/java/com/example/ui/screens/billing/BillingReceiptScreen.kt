package com.example.ui.screens.billing

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.OrderSummary
import com.example.domain.model.PaymentMethod
import com.example.domain.model.PaymentStatus
import com.example.ui.PosViewModel

@Composable
fun BillingReceiptScreen(viewModel: PosViewModel) {
    val context = LocalContext.current
    val allOrders by viewModel.allOrders.collectAsState()
    val activeBillingOrder by viewModel.activeBillingOrder.collectAsState()
    val config by viewModel.restaurantConfig.collectAsState()
    val thermalPrintText by viewModel.printedReceiptText.collectAsState()

    val unpaidOrders = allOrders.filter { it.paymentStatus == PaymentStatus.UNPAID }
    val selectedOrder = activeBillingOrder ?: unpaidOrders.firstOrNull() ?: allOrders.firstOrNull()

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var showPrintModal by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Left Column: Unpaid Orders List
        ElevatedCard(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Unpaid & Active Orders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allOrders) { order ->
                        val isSelected = selectedOrder?.id == order.id
                        Card(
                            onClick = { viewModel.selectOrderForBilling(order) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("billing_order_${order.orderNumber}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(order.orderNumber, fontWeight = FontWeight.Bold)
                                    Text("Table ${order.tableNumber ?: "N/A"} • ${order.orderType.label}", style = MaterialTheme.typography.bodySmall)
                                }

                                Text(
                                    text = "${config.currencySymbol}${String.format("%.2f", order.totalAmount)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Column: Checkout & Receipt Details
        ElevatedCard(
            modifier = Modifier.weight(1.5f).fillMaxHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (selectedOrder == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select an order to view bill & checkout")
                }
            } else {
                Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {
                    Text(
                        text = "Tax Invoice Preview - ${selectedOrder.orderNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Itemized table
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(selectedOrder.items) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.quantity}x ${item.menuItem.name}", modifier = Modifier.weight(1f))
                                Text("${config.currencySymbol}${String.format("%.2f", item.subtotal)}")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Math breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal")
                            Text("${config.currencySymbol}${String.format("%.2f", selectedOrder.subtotal)}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GST Tax (5%)")
                            Text("${config.currencySymbol}${String.format("%.2f", selectedOrder.taxAmount)}")
                        }
                        if (selectedOrder.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount")
                                Text("-${config.currencySymbol}${String.format("%.2f", selectedOrder.discount)}")
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                "${config.currencySymbol}${String.format("%.2f", selectedOrder.totalAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Select Payment Method:", fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        PaymentMethod.values().forEach { method ->
                            val isSelected = selectedPaymentMethod == method
                            OutlinedButton(
                                onClick = { selectedPaymentMethod = method },
                                modifier = Modifier.weight(1f).testTag("pay_method_${method.name}"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                            ) {
                                Text(method.label, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.printThermalReceipt(selectedOrder, context)
                                showPrintModal = true
                            },
                            modifier = Modifier.weight(1f).testTag("print_receipt_btn")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "Print")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thermal Print")
                        }

                        OutlinedButton(
                            onClick = { viewModel.generatePdfInvoice(selectedOrder, context) },
                            modifier = Modifier.weight(1f).testTag("pdf_invoice_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Invoice")
                        }

                        Button(
                            onClick = {
                                viewModel.updateOrderStatus(
                                    orderId = selectedOrder.id,
                                    status = com.example.domain.model.OrderStatus.COMPLETED,
                                    paymentStatus = PaymentStatus.PAID,
                                    method = selectedPaymentMethod
                                )
                            },
                            modifier = Modifier.weight(1.2f).testTag("complete_payment_btn")
                        ) {
                            Text("Settle & Close Bill")
                        }
                    }
                }
            }
        }
    }

    // Thermal Printer Simulator Modal
    if (showPrintModal && thermalPrintText != null) {
        AlertDialog(
            onDismissRequest = {
                showPrintModal = false
                viewModel.clearPrintPreview()
            },
            title = { Text("ESC/POS Thermal Printer Simulator") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = thermalPrintText ?: "",
                        color = Color(0xFF00FF66),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showPrintModal = false
                    viewModel.clearPrintPreview()
                }) {
                    Text("Close Simulator")
                }
            }
        )
    }
}
