package io.github.aouarius.nudibranche.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Turns a rendered card into a picture with a "Nudidex" line and opens the share sheet. */
object CardShare {

    suspend fun share(context: Context, card: Bitmap, fileName: String, chooserTitle: String) {
        val file = withContext(Dispatchers.Default) {
            val picture = framed(card)
            withContext(Dispatchers.IO) {
                val dir = File(context.cacheDir, "shared").apply { mkdirs() }
                dir.listFiles()?.forEach { it.delete() }
                File(dir, "$fileName.png").also { file ->
                    file.outputStream().use { picture.compress(Bitmap.CompressFormat.PNG, 100, it) }
                }
            }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val send = Intent(Intent.ACTION_SEND)
            .setType("image/png")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** The card on the app's dark background, with the app name underneath. */
    private fun framed(card: Bitmap): Bitmap {
        val software = if (card.config == Bitmap.Config.HARDWARE) card.copy(Bitmap.Config.ARGB_8888, false) else card
        val pad = software.width / 10
        val footer = software.width / 7
        val result = Bitmap.createBitmap(software.width + 2 * pad, software.height + pad + footer, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(0xFF16181D.toInt())
        canvas.drawBitmap(software, pad.toFloat(), pad.toFloat(), null)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFB993FF.toInt()
            textAlign = Paint.Align.CENTER
            textSize = footer * 0.42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        canvas.drawText("NUDIDEX", result.width / 2f, pad + software.height + footer * 0.62f, paint)
        return result
    }
}
