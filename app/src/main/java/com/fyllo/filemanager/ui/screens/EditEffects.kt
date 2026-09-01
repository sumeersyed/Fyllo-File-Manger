package com.fyllo.filemanager.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

object EditEffects {

    suspend fun applyEffect(context: Context, uri: Uri, effectName: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.createSource(context.contentResolver, uri)
                } else {
                    return@withContext null
                }
                
                // Ensure bitmap is mutable
                val originalBitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
                
                val resultBitmap = when (effectName) {
                    "Posterize" -> posterizeBitmap(originalBitmap)
                    "Edges" -> edgesBitmap(originalBitmap)
                    else -> originalBitmap
                }
                
                val tempFile = File(context.cacheDir, "effect_${System.currentTimeMillis()}.png")
                FileOutputStream(tempFile).use { out ->
                    resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Uri.fromFile(tempFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    private fun posterizeBitmap(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val dest = Bitmap.createBitmap(width, height, src.config ?: Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val levels = 4 // Posterize levels
        val step = 255 / (levels - 1)
        
        for (i in pixels.indices) {
            val color = pixels[i]
            val a = Color.alpha(color)
            var r = Color.red(color)
            var g = Color.green(color)
            var b = Color.blue(color)
            
            r = (r / step) * step
            g = (g / step) * step
            b = (b / step) * step
            
            pixels[i] = Color.argb(a, r, g, b)
        }
        
        dest.setPixels(pixels, 0, width, 0, 0, width, height)
        return dest
    }

    private fun edgesBitmap(src: Bitmap): Bitmap {
        // Simplified Sobel Edge Detection
        val width = src.width
        val height = src.height
        val dest = Bitmap.createBitmap(width, height, src.config ?: Bitmap.Config.ARGB_8888)
        
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)
        val destPixels = IntArray(width * height)
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val p00 = pixels[(y - 1) * width + (x - 1)]
                val p01 = pixels[(y - 1) * width + x]
                val p02 = pixels[(y - 1) * width + (x + 1)]
                val p10 = pixels[y * width + (x - 1)]
                val p12 = pixels[y * width + (x + 1)]
                val p20 = pixels[(y + 1) * width + (x - 1)]
                val p21 = pixels[(y + 1) * width + x]
                val p22 = pixels[(y + 1) * width + (x + 1)]
                
                val gxR = Color.red(p02) + 2 * Color.red(p12) + Color.red(p22) - (Color.red(p00) + 2 * Color.red(p10) + Color.red(p20))
                val gyR = Color.red(p00) + 2 * Color.red(p01) + Color.red(p02) - (Color.red(p20) + 2 * Color.red(p21) + Color.red(p22))
                val valR = minOf(255, abs(gxR) + abs(gyR))
                
                val gxG = Color.green(p02) + 2 * Color.green(p12) + Color.green(p22) - (Color.green(p00) + 2 * Color.green(p10) + Color.green(p20))
                val gyG = Color.green(p00) + 2 * Color.green(p01) + Color.green(p02) - (Color.green(p20) + 2 * Color.green(p21) + Color.green(p22))
                val valG = minOf(255, abs(gxG) + abs(gyG))
                
                val gxB = Color.blue(p02) + 2 * Color.blue(p12) + Color.blue(p22) - (Color.blue(p00) + 2 * Color.blue(p10) + Color.blue(p20))
                val gyB = Color.blue(p00) + 2 * Color.blue(p01) + Color.blue(p02) - (Color.blue(p20) + 2 * Color.blue(p21) + Color.blue(p22))
                val valB = minOf(255, abs(gxB) + abs(gyB))
                
                destPixels[y * width + x] = Color.argb(255, valR, valG, valB)
            }
        }
        
        dest.setPixels(destPixels, 0, width, 0, 0, width, height)
        return dest
    }
}
