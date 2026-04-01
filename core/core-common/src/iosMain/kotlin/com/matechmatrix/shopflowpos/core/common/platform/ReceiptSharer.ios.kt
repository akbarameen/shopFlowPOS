package com.matechmatrix.shopflowpos.core.common.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberReceiptSharer(): ReceiptSharer = remember { IosReceiptSharer() }

private class IosReceiptSharer : ReceiptSharer {
    override fun shareViaWhatsApp(text: String, phone: String?) = shareViaText(text)
    override fun shareViaText(text: String) { /* TODO: UIActivityViewController */ }
    override fun openHtmlReceipt(html: String, fileName: String) { /* TODO: WKWebView / Files */ }
    override fun shareReceiptAsPdf(html: String, fileName: String) {
        TODO("Not yet implemented")
    }

    override fun saveReceiptAsPdf(html: String, fileName: String) {
        TODO("Not yet implemented")
    }
}