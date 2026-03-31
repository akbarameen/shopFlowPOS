package com.matechmatrix.shopflowpos.core.common.util

import com.matechmatrix.shopflowpos.core.model.ReceiptData
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object ReceiptBuilder {

    // ── Plain-text receipt (for WhatsApp sharing) ─────────────────────────────

    fun buildTextReceipt(r: ReceiptData): String = buildString {
        val dt = Instant.fromEpochMilliseconds(r.soldAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val date = "${dt.dayOfMonth.toString().padStart(2,'0')}/${dt.monthNumber.toString().padStart(2,'0')}/${dt.year}"
        val time = "${dt.hour.toString().padStart(2,'0')}:${dt.minute.toString().padStart(2,'0')}"

        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("       ${r.shopName.uppercase()}")
        if (r.shopAddress.isNotBlank()) appendLine("  ${r.shopAddress}")
        if (r.shopPhone.isNotBlank())   appendLine("  📞 ${r.shopPhone}")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("Invoice : ${r.invoiceNumber}")
        appendLine("Date    : $date  $time")
        if (!r.isWalkIn) {
            appendLine("Customer: ${r.customerName}")
            if (r.customerPhone.isNotBlank()) appendLine("Phone   : ${r.customerPhone}")
            if (!r.customerCnic.isNullOrBlank()) appendLine("CNIC    : ${r.customerCnic}")
        }
        appendLine("──────────────────────────────")
        appendLine("ITEMS")
        appendLine("──────────────────────────────")
        r.items.forEach { item ->
            val lineStr = "${r.currencySymbol} ${CurrencyFormatter.formatRs(item.lineTotal)}"
            appendLine("${item.productName}")
            if (!item.imei.isNullOrBlank()) appendLine("  IMEI: ${item.imei}")
            val qtyPrice = "  ${item.quantity} × ${r.currencySymbol} ${CurrencyFormatter.formatRs(item.unitPrice)}"
            appendLine(qtyPrice.padEnd(30) + lineStr)
            if (item.discount > 0) appendLine("  Disc: -${r.currencySymbol} ${CurrencyFormatter.formatRs(item.discount)}")
        }
        appendLine("──────────────────────────────")
        if (r.discountAmount > 0 || r.taxAmount > 0) {
            appendLine("Subtotal".padEnd(20) + "${r.currencySymbol} ${CurrencyFormatter.formatRs(r.subtotal)}")
            if (r.hasDiscount) appendLine("Discount".padEnd(20) + "-${r.currencySymbol} ${CurrencyFormatter.formatRs(r.discountAmount)}")
            if (r.hasTax)      appendLine("Tax".padEnd(20) + "${r.currencySymbol} ${CurrencyFormatter.formatRs(r.taxAmount)}")
        }
        appendLine("TOTAL".padEnd(20) + "${r.currencySymbol} ${CurrencyFormatter.formatRs(r.totalAmount)}")
        appendLine("──────────────────────────────")
        appendLine("PAYMENT")
        if (r.cashPaid > 0) appendLine("Cash".padEnd(20) + "${r.currencySymbol} ${CurrencyFormatter.formatRs(r.cashPaid)}")
        if (r.bankPaid > 0) appendLine("Bank".padEnd(20) + "${r.currencySymbol} ${CurrencyFormatter.formatRs(r.bankPaid)}")
        if (r.hasDue)       appendLine("Due".padEnd(20) + "${r.currencySymbol} ${CurrencyFormatter.formatRs(r.dueAmount)}")
        appendLine("Status  : ${r.paymentStatus.display}")
        if (!r.notes.isNullOrBlank()) {
            appendLine("──────────────────────────────")
            appendLine("Note: ${r.notes}")
        }
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("     Thank you for shopping!")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    // ── HTML receipt (for PDF / print) ────────────────────────────────────────

    fun buildHtmlReceipt(r: ReceiptData): String {
        val dt = Instant.fromEpochMilliseconds(r.soldAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val dateStr = "${dt.dayOfMonth.toString().padStart(2,'0')}/" +
                "${dt.monthNumber.toString().padStart(2,'0')}/" + "${dt.year}"
        val timeStr = "${dt.hour.toString().padStart(2,'0')}:${dt.minute.toString().padStart(2,'0')}"

        val sym = r.currencySymbol

        fun fmt(v: Double) = CurrencyFormatter.formatRs(v)
        fun row(label: String, value: String, bold: Boolean = false) =
            "<tr><td${if (bold) " style='font-weight:700'" else ""}>${label}</td>" +
                    "<td style='text-align:right${if (bold) ";font-weight:700" else ""}'>${value}</td></tr>"

        val itemsHtml = r.items.joinToString("") { item ->
            "<tr>" +
                    "<td>${item.productName}${if (!item.imei.isNullOrBlank()) "<br><small style='color:#888'>IMEI: ${item.imei}</small>" else ""}</td>" +
                    "<td style='text-align:center'>${item.quantity}</td>" +
                    "<td style='text-align:right'>$sym ${fmt(item.unitPrice)}${if (item.discount > 0) "<br><small style='color:#e74c3c'>-$sym ${fmt(item.discount)}</small>" else ""}</td>" +
                    "<td style='text-align:right;font-weight:600'>$sym ${fmt(item.lineTotal)}</td>" +
                    "</tr>"
        }

        val customerHtml = if (!r.isWalkIn) """
            <p style='margin:2px 0'><b>${r.customerName}</b></p>
            ${if (r.customerPhone.isNotBlank()) "<p style='margin:2px 0;color:#666'>📞 ${r.customerPhone}</p>" else ""}
            ${if (!r.customerCnic.isNullOrBlank()) "<p style='margin:2px 0;color:#666'>ID: ${r.customerCnic}</p>" else ""}
            ${if (r.customerAddress.isNotBlank()) "<p style='margin:2px 0;color:#666'>${r.customerAddress}</p>" else ""}
        """ else "<p style='color:#888'>Walk-in Customer</p>"

        val paymentRows = buildString {
            if (r.cashPaid > 0) append(row("Cash (${r.cashAccountName})", "$sym ${fmt(r.cashPaid)}"))
            if (r.bankPaid > 0) {
                val bankLabel = r.payments.firstOrNull { it.accountType == com.matechmatrix.shopflowpos.core.model.enums.AccountType.BANK }
                    ?.let { r.bankAccountNames[it.accountId] } ?: "Bank"
                append(row("Bank ($bankLabel)", "$sym ${fmt(r.bankPaid)}"))
            }
            if (r.hasDue) append("<tr style='color:#e74c3c'><td>Due (${r.paymentStatus.display})</td><td style='text-align:right'>$sym ${fmt(r.dueAmount)}</td></tr>")
        }

        val notesHtml = if (!r.notes.isNullOrBlank())
            "<div style='margin-top:12px;padding:10px;background:#f8f9fa;border-radius:6px;font-size:12px;color:#555'><b>Note:</b> ${r.notes}</div>"
        else ""

        return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Receipt ${r.invoiceNumber}</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f2f5; padding: 20px; }
    .receipt { background: #fff; max-width: 420px; margin: 0 auto; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.12); }
    .header { background: linear-gradient(135deg, #6C63FF, #3B82F6); color: #fff; padding: 24px 20px; text-align: center; }
    .header h1 { font-size: 22px; font-weight: 800; letter-spacing: -0.5px; }
    .header p  { font-size: 12px; opacity: 0.85; margin-top: 3px; }
    .badge { display: inline-block; background: rgba(255,255,255,0.2); border-radius: 100px; padding: 4px 14px; font-size: 13px; font-weight: 700; margin-top: 10px; letter-spacing: 0.5px; }
    .section { padding: 16px 20px; border-bottom: 1px solid #f0f0f0; }
    .section-title { font-size: 10px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; color: #aaa; margin-bottom: 8px; }
    .meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
    .meta-item label { font-size: 10px; color: #aaa; display: block; }
    .meta-item span { font-size: 13px; font-weight: 600; color: #333; }
    table { width: 100%; border-collapse: collapse; font-size: 13px; }
    table th { font-size: 10px; text-transform: uppercase; color: #aaa; font-weight: 700; padding: 0 0 8px; }
    table td { padding: 8px 0; border-bottom: 1px dashed #f0f0f0; vertical-align: top; color: #333; }
    table tr:last-child td { border-bottom: none; }
    .totals-table td { border: none; padding: 5px 0; }
    .total-row td { font-size: 16px; font-weight: 800; color: #3B82F6; border-top: 2px solid #f0f0f0; padding-top: 10px; }
    .payment-row td { color: #27ae60; }
    .due-row td { color: #e74c3c !important; }
    .footer { text-align: center; padding: 20px; background: #fafafa; }
    .footer p { font-size: 12px; color: #aaa; }
    @media print { body { background: white; padding: 0; } .receipt { box-shadow: none; } }
  </style>
</head>
<body>
  <div class="receipt">
    <!-- Header -->
    <div class="header">
      <h1>${r.shopName}</h1>
      ${if (r.shopAddress.isNotBlank()) "<p>${r.shopAddress}</p>" else ""}
      ${if (r.shopPhone.isNotBlank()) "<p>📞 ${r.shopPhone}</p>" else ""}
      <div class="badge">${r.invoiceNumber}</div>
    </div>
 
    <!-- Date + Status -->
    <div class="section">
      <div class="meta-grid">
        <div class="meta-item"><label>Date</label><span>$dateStr</span></div>
        <div class="meta-item"><label>Time</label><span>$timeStr</span></div>
        <div class="meta-item"><label>Status</label><span style="color:${if (r.hasDue) "#e74c3c" else "#27ae60"}">${r.paymentStatus.display}</span></div>
        <div class="meta-item"><label>Items</label><span>${r.items.sumOf { it.quantity }}</span></div>
      </div>
    </div>
 
    <!-- Customer -->
    <div class="section">
      <div class="section-title">Customer</div>
      $customerHtml
    </div>
 
    <!-- Items -->
    <div class="section">
      <div class="section-title">Items</div>
      <table>
        <thead><tr>
          <th style="text-align:left">Product</th>
          <th style="text-align:center">Qty</th>
          <th style="text-align:right">Price</th>
          <th style="text-align:right">Total</th>
        </tr></thead>
        <tbody>$itemsHtml</tbody>
      </table>
    </div>
 
    <!-- Totals -->
    <div class="section">
      <table class="totals-table">
        ${if (r.hasDiscount || r.hasTax) row("Subtotal", "$sym ${fmt(r.subtotal)}") else ""}
        ${if (r.hasDiscount) "<tr style='color:#e74c3c'><td>Discount</td><td style='text-align:right'>-$sym ${fmt(r.discountAmount)}</td></tr>" else ""}
        ${if (r.hasTax) row("Tax", "$sym ${fmt(r.taxAmount)}") else ""}
        <tr class="total-row"><td>Total</td><td style='text-align:right'>$sym ${fmt(r.totalAmount)}</td></tr>
      </table>
    </div>
 
    <!-- Payment -->
    <div class="section">
      <div class="section-title">Payment</div>
      <table class="totals-table">
        $paymentRows
      </table>
    </div>
 
    $notesHtml
 
    <!-- Footer -->
    <div class="footer">
      <p>Thank you for shopping at <b>${r.shopName}</b>!</p>
      <p style="margin-top:4px;font-size:10px;">Powered by ShopFlowPOS · Matech Matrix</p>
    </div>
  </div>
</body>
</html>
        """.trimIndent()
    }
}