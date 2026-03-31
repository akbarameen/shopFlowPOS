package com.matechmatrix.shopflowpos.core.common.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
interface ReceiptSharer {
    /** Opens WhatsApp with pre-filled text. */
    fun shareViaWhatsApp(text: String, phone: String?)
    
    /** Opens the system share sheet with receipt as plain text. */
    fun shareViaText(text: String)
    
    /** Saves or opens the HTML receipt file. */
    fun openHtmlReceipt(html: String, fileName: String)

    /** Generates a PDF from HTML and shares it (e.g., to WhatsApp). */
    fun shareReceiptAsPdf(html: String, fileName: String)

    /** Generates a PDF from HTML and saves it to the device. */
    fun saveReceiptAsPdf(html: String, fileName: String)
}

@Composable
expect fun rememberReceiptSharer(): ReceiptSharer
