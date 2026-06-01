package com.example.ui.splash

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.Constants
import com.example.R

@Composable
fun RiqahTextCanvas(
    text: String,
    textColor: Color,
    animationDuration: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fontFamily = FontFamily(Font(R.font.riqah_font))
    val typeface = fontFamily.resolveAsTypeface(LocalContext.current).value ?: Typeface.DEFAULT

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(key1 = text, key2 = animationDuration) {
        animatedProgress.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = animationDuration.toInt(),
                easing = LinearEasing
            )
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                this.color = textColor.toArgb()
                this.typeface = typeface
                this.textSize = 80.sp.toPx() // Adjust text size as needed
                this.isAntiAlias = true
            }

            // Measure the text to center it
            val textWidth = paint.measureText(text)
            val textHeight = paint.fontMetrics.descent - paint.fontMetrics.ascent
            val x = (size.width - textWidth) / 2
            val y = (size.height / 2) - (textHeight / 2) - paint.fontMetrics.ascent

            // Draw the text dynamically based on animation progress
            val textToDraw = text.substring(0, (text.length * animatedProgress.value).toInt())
            canvas.nativeCanvas.drawText(textToDraw, x, y, paint)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRiqahTextCanvas() {
    RiqahTextCanvas(
        text = Constants.SPLASH_TEXT_ARABIC,
        textColor = Color(0xFFFFD700), // Gold color
        animationDuration = 3000L
    )
}
