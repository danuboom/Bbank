package com.danunant.bbank.util


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import com.danunant.bbank.core.formatTHB
import com.danunant.bbank.core.formatThaiDateTime
import com.danunant.bbank.data.Txn
import java.io.File
import java.io.FileOutputStream


fun buildSlipBitmap(txn: Txn): Bitmap {
    val w = 1080
    val h = 600
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.textSize = 48f
    c.drawARGB(255, 250, 250, 250)
    p.setARGB(255, 0, 0, 0)
    c.drawText("Bbank Transfer Slip", 40f, 100f, p)
    c.drawText("Txn: ${'$'}{txn.id}", 40f, 180f, p)
    c.drawText("Date: ${'$'}{formatThaiDateTime(txn.at)}", 40f, 260f, p)
    c.drawText("Amount: ${'$'}{formatTHB(txn.amountSatang)}", 40f, 340f, p)
    return bmp
}


fun shareBitmap(context: Context, bmp: Bitmap, filename: String) {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, filename)
    FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
    val uri: Uri = FileProvider.getUriForFile(context, "com.danunant.bbank.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share slip"))
}