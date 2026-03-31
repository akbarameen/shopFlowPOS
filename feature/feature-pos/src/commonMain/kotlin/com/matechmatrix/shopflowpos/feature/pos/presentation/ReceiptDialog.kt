// ════════════════════════════════════════════════════════════════════════════
// feature/pos/presentation/ReceiptDialog.kt
// ════════════════════════════════════════════════════════════════════════════
package com.matechmatrix.shopflowpos.feature.pos.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.matechmatrix.shopflowpos.core.common.platform.rememberReceiptSharer
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.common.util.ReceiptBuilder
import com.matechmatrix.shopflowpos.core.model.ReceiptData
import com.matechmatrix.shopflowpos.core.ui.theme.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ReceiptDialog(
    receipt  : ReceiptData,
    currency : String,
    onNewSale: () -> Unit
) {
    val receiptSharer = rememberReceiptSharer()
    val receiptText   = remember(receipt) { ReceiptBuilder.buildTextReceipt(receipt) }
    val receiptHtml   = remember(receipt) { ReceiptBuilder.buildHtmlReceipt(receipt) }
    
    // Format: date_invoiceNumber.pdf
    val fileName = remember(receipt) {
        val dt = Instant.fromEpochMilliseconds(receipt.soldAt).toLocalDateTime(TimeZone.currentSystemDefault())
        val date = "${dt.dayOfMonth.toString().padStart(2,'0')}${dt.monthNumber.toString().padStart(2,'0')}${dt.year}"
        "${date}_${receipt.invoiceNumber}"
    }

    Dialog(
        onDismissRequest = onNewSale,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier  = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.fillMaxSize()) {
                // Header
                Box(
                    Modifier.fillMaxWidth()
                        .background(SuccessContainer)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Success.copy(0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = Success, modifier = Modifier.size(30.dp))
                        }
                        Text("Sale Complete!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Success)
                        Surface(shape = RoundedCornerShape(100.dp), color = PrimaryContainer) {
                            Text(
                                receipt.invoiceNumber,
                                Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium, color = Primary, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Receipt body (scrollable)
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date + Customer
                    val dt   = Instant.fromEpochMilliseconds(receipt.soldAt).toLocalDateTime(TimeZone.currentSystemDefault())
                    val date = "${dt.dayOfMonth.toString().padStart(2,'0')}/${dt.monthNumber.toString().padStart(2,'0')}/${dt.year}"
                    val time = "${dt.hour.toString().padStart(2,'0')}:${dt.minute.toString().padStart(2,'0')}"

                    ReceiptInfoRow("Date", "$date  $time")
                    if (!receipt.isWalkIn) {
                        HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                        ReceiptInfoRow("Customer", receipt.customerName)
                        if (receipt.customerPhone.isNotBlank()) ReceiptInfoRow("Phone", receipt.customerPhone)
                        if (!receipt.customerCnic.isNullOrBlank()) ReceiptInfoRow("CNIC", receipt.customerCnic!!)
                        if (receipt.customerAddress.isNotBlank()) ReceiptInfoRow("Address", receipt.customerAddress)
                    }

                    // Items
                    HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                    Text("Items", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextMuted)
                    receipt.items.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(item.productName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                if (!item.imei.isNullOrBlank()) Text(item.imei!!, style = MaterialTheme.typography.labelSmall, color = Primary)
                                Text(
                                    "${item.quantity} × $currency ${CurrencyFormatter.formatRs(item.unitPrice)}",
                                    style = MaterialTheme.typography.labelSmall, color = TextMuted
                                )
                            }
                            Text(
                                "$currency ${CurrencyFormatter.formatRs(item.lineTotal)}",
                                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary
                            )
                        }
                    }

                    // Totals
                    HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                    if (receipt.hasDiscount) ReceiptInfoRow("Discount", "-$currency ${CurrencyFormatter.formatRs(receipt.discountAmount)}", Danger)
                    if (receipt.hasTax) ReceiptInfoRow("Tax", "$currency ${CurrencyFormatter.formatRs(receipt.taxAmount)}")
                    ReceiptInfoRow("Total", "$currency ${CurrencyFormatter.formatRs(receipt.totalAmount)}", Primary, bold = true)

                    // Payments
                    HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                    if (receipt.cashPaid > 0) ReceiptInfoRow("Cash Paid", "$currency ${CurrencyFormatter.formatRs(receipt.cashPaid)}", Success)
                    if (receipt.bankPaid > 0) ReceiptInfoRow("Bank Paid", "$currency ${CurrencyFormatter.formatRs(receipt.bankPaid)}", Success)
                    if (receipt.hasDue) ReceiptInfoRow(
                        "Due (${receipt.paymentStatus.display})",
                        "$currency ${CurrencyFormatter.formatRs(receipt.dueAmount)}",
                        Danger, bold = true
                    )
                    if (!receipt.notes.isNullOrBlank()) {
                        HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                        Text(
                            "📝 ${receipt.notes}",
                            style = MaterialTheme.typography.bodySmall, color = TextMuted
                        )
                    }
                }

                // Action buttons
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Share row
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // WhatsApp share (as PDF)
                        val phone = receipt.customerPhone.ifBlank { null }
                        OutlinedButton(
                            onClick   = { receiptSharer.shareReceiptAsPdf(receiptHtml, fileName) },
                            modifier  = Modifier.weight(1f),
                            shape     = RoundedCornerShape(12.dp),
                            border    = BorderStroke(1.5.dp, Color(0xFF25D366)),
                            colors    = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Rounded.Share, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("WhatsApp PDF", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // Download PDF
                        OutlinedButton(
                            onClick   = { receiptSharer.saveReceiptAsPdf(receiptHtml, fileName) },
                            modifier  = Modifier.weight(1f),
                            shape     = RoundedCornerShape(12.dp),
                            border    = BorderStroke(1.5.dp, Primary),
                            colors    = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                        ) {
                            Icon(Icons.Rounded.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Download PDF", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // New Sale
                    Button(
                        onClick  = onNewSale,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Rounded.AddShoppingCart, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("New Sale", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptInfoRow(label: String, value: String, color: Color = TextPrimary, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold, color = color)
    }
}