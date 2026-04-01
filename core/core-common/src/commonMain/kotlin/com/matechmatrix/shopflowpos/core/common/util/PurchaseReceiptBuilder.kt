package com.matechmatrix.shopflowpos.core.common.util

import com.matechmatrix.shopflowpos.core.model.PurchaseReceiptData
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object PurchaseReceiptBuilder {

    // ── Plain text (WhatsApp) ─────────────────────────────────────────────────
    fun buildText(r: PurchaseReceiptData): String = buildString {
        val dt   = Instant.fromEpochMilliseconds(r.purchasedAt).toLocalDateTime(TimeZone.currentSystemDefault())
        val date = "${dt.dayOfMonth.toString().padStart(2,'0')}/${dt.monthNumber.toString().padStart(2,'0')}/${dt.year}"
        val time = "${dt.hour.toString().padStart(2,'0')}:${dt.minute.toString().padStart(2,'0')}"
        val sym  = r.currencySymbol

        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("  📥 PURCHASE ORDER RECEIPT")
        appendLine("  ${r.shopName.uppercase()}")
        if (r.shopAddress.isNotBlank()) appendLine("  ${r.shopAddress}")
        if (r.shopPhone.isNotBlank())   appendLine("  📞 ${r.shopPhone}")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("PO #    : ${r.poNumber}")
        appendLine("Date    : $date  $time")
        appendLine("Supplier: ${r.supplierName}")
        if (r.supplierPhone.isNotBlank()) appendLine("Phone   : ${r.supplierPhone}")
        appendLine("──────────────────────────────")
        appendLine("ITEMS PURCHASED")
        appendLine("──────────────────────────────")
        r.items.forEach { item ->
            appendLine(item.productName)
            if (!item.imei.isNullOrBlank()) appendLine("  IMEI: ${item.imei}")
            val qtyLine = "  ${item.quantity} × $sym ${CurrencyFormatter.formatRs(item.unitCost)}"
            appendLine(qtyLine.padEnd(28) + "$sym ${CurrencyFormatter.formatRs(item.totalCost)}")
        }
        appendLine("──────────────────────────────")
        if (r.hasDiscount) appendLine("Subtotal".padEnd(20) + "$sym ${CurrencyFormatter.formatRs(r.subtotal)}")
        if (r.hasDiscount) appendLine("Discount".padEnd(20) + "-$sym ${CurrencyFormatter.formatRs(r.discountAmount)}")
        appendLine("TOTAL".padEnd(20) + "$sym ${CurrencyFormatter.formatRs(r.totalAmount)}")
        appendLine("──────────────────────────────")
        appendLine("PAYMENT")
        if (r.cashPaid > 0) appendLine("Cash".padEnd(20)      + "$sym ${CurrencyFormatter.formatRs(r.cashPaid)}")
        if (r.bankPaid > 0) appendLine("Bank".padEnd(20)      + "$sym ${CurrencyFormatter.formatRs(r.bankPaid)}")
        if (r.hasDue)        appendLine("Due (Payable)".padEnd(20) + "$sym ${CurrencyFormatter.formatRs(r.dueAmount)}")
        appendLine("Status  : ${r.paymentStatus.display}")
        appendLine("Goods   : ${r.goodsStatus.display}")
        if (!r.notes.isNullOrBlank()) { appendLine("──────────────────────────────"); appendLine("Note: ${r.notes}") }
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("     Powered by ShopFlowPOS")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    // ── HTML (download/print) ─────────────────────────────────────────────────
    fun buildHtml(r: PurchaseReceiptData): String {
        val dt   = Instant.fromEpochMilliseconds(r.purchasedAt).toLocalDateTime(TimeZone.currentSystemDefault())
        val date = "${dt.dayOfMonth.toString().padStart(2,'0')}/${dt.monthNumber.toString().padStart(2,'0')}/${dt.year}"
        val time = "${dt.hour.toString().padStart(2,'0')}:${dt.minute.toString().padStart(2,'0')}"
        val sym  = r.currencySymbol
        fun fmt(v: Double) = CurrencyFormatter.formatRs(v)
        fun row(l: String, v: String, bold: Boolean = false) =
            "<tr><td${if (bold) " style='font-weight:700'" else ""}>${l}</td><td style='text-align:right${if (bold) ";font-weight:700" else ""}'>${v}</td></tr>"

        val itemsHtml = r.items.joinToString("") { item ->
            "<tr>" +
                    "<td>${item.productName}${if (!item.imei.isNullOrBlank()) "<br><small style='color:#888'>IMEI: ${item.imei}</small>" else ""}</td>" +
                    "<td style='text-align:center'>${item.quantity}</td>" +
                    "<td style='text-align:right'>$sym ${fmt(item.unitCost)}</td>" +
                    "<td style='text-align:right;font-weight:600'>$sym ${fmt(item.totalCost)}</td>" +
                    "</tr>"
        }

        val paymentRows = buildString {
            if (r.cashPaid > 0) append(row("Cash (${r.cashAccountName})", "$sym ${fmt(r.cashPaid)}"))
            if (r.bankPaid > 0) {
                val bankLabel = r.payments.firstOrNull { it.accountType == AccountType.BANK }
                    ?.let { r.bankAccountNames[it.accountId] } ?: "Bank"
                append(row("Bank ($bankLabel)", "$sym ${fmt(r.bankPaid)}"))
            }
            if (r.hasDue) append("<tr style='color:#e74c3c'><td>Due Payable (${r.paymentStatus.display})</td><td style='text-align:right'>$sym ${fmt(r.dueAmount)}</td></tr>")
        }

        return """
<!DOCTYPE html>
<html><head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0">
  <title>PO ${r.poNumber}</title>
  <style>
    *{box-sizing:border-box;margin:0;padding:0}
    body{font-family:'Segoe UI',Arial,sans-serif;background:#f0f2f5;padding:20px}
    .receipt{background:#fff;max-width:440px;margin:0 auto;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.12)}
    .header{background:linear-gradient(135deg,#E67E22,#F39C12);color:#fff;padding:24px 20px;text-align:center}
    .header h1{font-size:22px;font-weight:800}
    .badge{display:inline-block;background:rgba(255,255,255,.2);border-radius:100px;padding:4px 14px;font-size:13px;font-weight:700;margin-top:10px}
    .section{padding:14px 20px;border-bottom:1px solid #f0f0f0}
    .section-title{font-size:10px;font-weight:700;letter-spacing:1px;text-transform:uppercase;color:#aaa;margin-bottom:8px}
    .meta-grid{display:grid;grid-template-columns:1fr 1fr;gap:6px}
    .meta-item label{font-size:10px;color:#aaa;display:block}
    .meta-item span{font-size:13px;font-weight:600;color:#333}
    table{width:100%;border-collapse:collapse;font-size:13px}
    table th{font-size:10px;text-transform:uppercase;color:#aaa;font-weight:700;padding:0 0 8px}
    table td{padding:8px 0;border-bottom:1px dashed #f0f0f0;vertical-align:top;color:#333}
    table tr:last-child td{border-bottom:none}
    .totals-table td{border:none;padding:5px 0}
    .total-row td{font-size:16px;font-weight:800;color:#E67E22;border-top:2px solid #f0f0f0;padding-top:10px}
    .footer{text-align:center;padding:20px;background:#fafafa}
    .footer p{font-size:12px;color:#aaa}
    @media print{body{background:white;padding:0}.receipt{box-shadow:none}}
  </style>
</head><body>
  <div class="receipt">
    <div class="header">
      <p style="font-size:11px;opacity:.8;margin-bottom:4px">📥 PURCHASE ORDER</p>
      <h1>${r.shopName}</h1>
      ${if (r.shopAddress.isNotBlank()) "<p style='font-size:12px;opacity:.85;margin-top:3px'>${r.shopAddress}</p>" else ""}
      <div class="badge">${r.poNumber}</div>
    </div>
    <div class="section">
      <div class="meta-grid">
        <div class="meta-item"><label>Date</label><span>$date</span></div>
        <div class="meta-item"><label>Time</label><span>$time</span></div>
        <div class="meta-item"><label>Payment</label><span style="color:${if (r.hasDue) "#e74c3c" else "#27ae60"}">${r.paymentStatus.display}</span></div>
        <div class="meta-item"><label>Goods</label><span>${r.goodsStatus.display}</span></div>
      </div>
    </div>
    <div class="section">
      <div class="section-title">Supplier</div>
      <p style="margin:2px 0"><b>${r.supplierName}</b></p>
      ${if (r.supplierPhone.isNotBlank()) "<p style='margin:2px 0;color:#666;font-size:12px'>📞 ${r.supplierPhone}</p>" else ""}
    </div>
    <div class="section">
      <div class="section-title">Items Purchased</div>
      <table>
        <thead><tr><th style="text-align:left">Product</th><th style="text-align:center">Qty</th><th style="text-align:right">Cost</th><th style="text-align:right">Total</th></tr></thead>
        <tbody>$itemsHtml</tbody>
      </table>
    </div>
    <div class="section">
      <table class="totals-table">
        ${if (r.hasDiscount) row("Subtotal", "$sym ${fmt(r.subtotal)}") else ""}
        ${if (r.hasDiscount) "<tr style='color:#e74c3c'><td>Discount</td><td style='text-align:right'>-$sym ${fmt(r.discountAmount)}</td></tr>" else ""}
        <tr class="total-row"><td>Total</td><td style='text-align:right'>$sym ${fmt(r.totalAmount)}</td></tr>
      </table>
    </div>
    <div class="section">
      <div class="section-title">Payment</div>
      <table class="totals-table">$paymentRows</table>
    </div>
    ${if (!r.notes.isNullOrBlank()) "<div class='section' style='font-size:12px;color:#555'><b>Note:</b> ${r.notes}</div>" else ""}
    <div class="footer">
      <p>Powered by <b>ShopFlowPOS</b> · Matech Matrix</p>
    </div>
  </div>
</body></html>
        """.trimIndent()
    }
}