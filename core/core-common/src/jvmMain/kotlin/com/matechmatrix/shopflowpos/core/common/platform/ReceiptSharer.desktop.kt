package com.matechmatrix.shopflowpos.core.common.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

@Composable
actual fun rememberReceiptSharer(): ReceiptSharer = remember { DesktopReceiptSharer() }

private class DesktopReceiptSharer : ReceiptSharer {
    override fun shareViaWhatsApp(text: String, phone: String?) = shareViaText(text)
    override fun shareViaText(text: String) {
        // Copy to clipboard on Desktop
        val sel = StringSelection(text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(sel, null)
    }
    override fun openHtmlReceipt(html: String, fileName: String) {
        val file = File(System.getProperty("user.home"), "Downloads/$fileName.html")
        file.writeText(html)
        java.awt.Desktop.getDesktop().open(file)
    }

    override fun shareReceiptAsPdf(html: String, fileName: String) {
        val file = File(System.getProperty("user.home"), "Downloads/$fileName.html")
        file.writeText(html)
        java.awt.Desktop.getDesktop().open(file)
    }

    override fun saveReceiptAsPdf(html: String, fileName: String) {
        val file = File(System.getProperty("user.home"), "Downloads/$fileName.html")
        file.writeText(html)
        java.awt.Desktop.getDesktop().open(file)
    }
}