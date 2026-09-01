package com.fyllo.filemanager.ui.screens

import androidx.compose.ui.graphics.ColorMatrix

data class FilterData(val name: String, val matrix: ColorMatrix)

object EditFilters {

    fun getFilters(): List<FilterData> = listOf(
        FilterData("Original", ColorMatrix()),
        FilterData("Lite", ColorMatrix(floatArrayOf(
            1.05f, 0f, 0f, 0f, 10f,
            0f, 1.05f, 0f, 0f, 10f,
            0f, 0f, 1.05f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Playa", ColorMatrix(floatArrayOf(
            1.15f, 0.05f, 0f, 0f, 15f,
            0f, 1.08f, 0f, 0f, 10f,
            0f, 0f, 0.92f, 0f, 5f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Honey", ColorMatrix(floatArrayOf(
            1.2f, 0.1f, 0f, 0f, 10f,
            0f, 1.05f, 0f, 0f, 8f,
            0f, 0f, 0.8f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Moody", ColorMatrix(floatArrayOf(
            0.9f, 0f, 0f, 0f, -10f,
            0f, 0.9f, 0f, 0f, -5f,
            0f, 0f, 1.1f, 0f, 15f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Romantic", ColorMatrix(floatArrayOf(
            1.1f, 0.1f, 0.1f, 0f, 15f,
            0f, 1.05f, 0f, 0f, 5f,
            0f, 0f, 1.05f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Love", ColorMatrix(floatArrayOf(
            1.2f, 0f, 0.1f, 0f, 25f,
            0f, 0.9f, 0f, 0f, 0f,
            0f, 0f, 1.1f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Isla", ColorMatrix(floatArrayOf(
            1.05f, 0f, 0.05f, 0f, 5f,
            0f, 1.1f, 0f, 0f, 5f,
            0.05f, 0f, 1.1f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Desert", ColorMatrix(floatArrayOf(
            1.1f, 0.05f, 0f, 0f, 10f,
            0f, 1.0f, 0f, 0f, 5f,
            0f, 0f, 0.9f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )).apply {
            val s = 0.85f
            values[0] = 0.213f * (1 - s) + s
        }),
        FilterData("Clay", ColorMatrix(floatArrayOf(
            1.1f, 0.08f, 0.02f, 0f, 8f,
            0.02f, 1.0f, 0.02f, 0f, 5f,
            0f, 0f, 0.88f, 0f, 5f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Palma", ColorMatrix(floatArrayOf(
            0.95f, 0f, 0f, 0f, 0f,
            0f, 1.12f, 0f, 0f, 5f,
            0f, 0f, 0.95f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Blush", ColorMatrix(floatArrayOf(
            1.1f, 0.05f, 0.05f, 0f, 12f,
            0f, 0.98f, 0f, 0f, 5f,
            0.02f, 0f, 1.02f, 0f, 8f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Alpaca", ColorMatrix(floatArrayOf(
            1.1f, 0.05f, 0f, 0f, 10f,
            0f, 1.02f, 0f, 0f, 8f,
            0f, 0f, 0.9f, 0f, 5f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Modena", ColorMatrix(floatArrayOf(
            1.15f, 0.05f, 0f, 0f, 5f,
            0.02f, 1.08f, 0f, 0f, 5f,
            0f, 0f, 0.95f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("West", ColorMatrix(floatArrayOf(
            0.95f, 0f, 0.05f, 0f, 5f,
            0f, 0.98f, 0.02f, 0f, 5f,
            0f, 0.05f, 1.05f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Metro", ColorMatrix(floatArrayOf(
            1.235f, 0f, 0.05f, 0f, -38.4f,
            0f, 1.274f, 0.02f, 0f, -38.4f,
            0f, 0.02f, 1.365f, 0f, -38.4f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Reel", ColorMatrix(floatArrayOf(
            1.1f, 0f, 0f, 0f, 5f,
            0f, 1.05f, 0.05f, 0f, 0f,
            0f, 0.05f, 1.15f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Bazaar", ColorMatrix(floatArrayOf(
            1.15f, 0.05f, 0f, 0f, 5f,
            0f, 1.05f, 0f, 0f, 3f,
            0f, 0f, 0.92f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Ollie", ColorMatrix(floatArrayOf(
            0.95f, 0.05f, 0f, 0f, 15f,
            0.02f, 1.0f, 0.03f, 0f, 12f,
            0f, 0.05f, 0.92f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Onyx", ColorMatrix(floatArrayOf(
            0.429f, 0.429f, 0.429f, 0f, -38.4f,
            0.429f, 0.429f, 0.429f, 0f, -38.4f,
            0.429f, 0.429f, 0.429f, 0f, -38.4f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Eiffel", ColorMatrix(floatArrayOf(
            0.33f, 0.33f, 0.33f, 0f, 5f,
            0.33f, 0.33f, 0.33f, 0f, 5f,
            0.33f, 0.33f, 0.33f, 0f, 5f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Vogue", ColorMatrix(floatArrayOf(
            0.437f, 0.412f, 0.4f, 0f, -29f,
            0.412f, 0.425f, 0.412f, 0f, -30f,
            0.4f, 0.412f, 0.412f, 0f, -32f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Vista", ColorMatrix(floatArrayOf(
            0.28f, 0.28f, 0.28f, 0f, 34.2f,
            0.28f, 0.28f, 0.28f, 0f, 34.2f,
            0.28f, 0.28f, 0.28f, 0f, 34.2f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Astro", ColorMatrix(floatArrayOf(
            0.7f, 0.3f, 0.1f, 0f, 0f,
            0.2f, 0.7f, 0.1f, 0f, 0f,
            0.2f, 0.3f, 0.8f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("Negative", ColorMatrix(floatArrayOf(
            -1f,  0f,  0f, 0f, 255f,
             0f, -1f,  0f, 0f, 255f,
             0f,  0f, -1f, 0f, 255f,
             0f,  0f,  0f, 1f,   0f
        ))),
        FilterData("1800s", ColorMatrix(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("1900s", ColorMatrix(floatArrayOf(
            0.45f, 0.45f, 0.45f, 0f, -20f,
            0.45f, 0.45f, 0.45f, 0f, -20f,
            0.45f, 0.45f, 0.45f, 0f, -20f,
            0f, 0f, 0f, 1f, 0f
        ))),
        FilterData("2000s", ColorMatrix(floatArrayOf(
            1.2f, 0f, 0f, 0f, 10f,
            0f, 1.2f, 0f, 0f, 10f,
            0f, 0f, 1.2f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        )))
    )
}
