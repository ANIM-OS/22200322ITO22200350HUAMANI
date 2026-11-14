package dev.dreamteam.a22200322_ito_22200350_huamani.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

enum class SpaceSymbol { Image, Orbit, Heart, Back, Refresh, Arrow, Play, Calendar }

@Composable
fun SpaceIcon(symbol: SpaceSymbol, modifier: Modifier = Modifier, color: Color = LocalContentColor.current, filled: Boolean = false) {
    Canvas(modifier.size(24.dp)) {
        scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
            val stroke = Stroke(1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            when (symbol) {
                SpaceSymbol.Image -> {
                    drawRoundRect(color, Offset(3f, 4f), Size(18f, 16f), CornerRadius(3f), style = stroke)
                    drawCircle(color, 2f, Offset(8f, 9f))
                    val p = Path().apply { moveTo(4f, 18f); lineTo(10f, 12f); lineTo(14f, 16f); lineTo(17f, 13f); lineTo(21f, 17f) }
                    drawPath(p, color, style = stroke)
                }
                SpaceSymbol.Orbit -> {
                    drawCircle(color, 5f, Offset(12f, 12f), style = stroke)
                    rotate(-35f, Offset(12f, 12f)) { drawOval(color, Offset(1f, 8f), Size(22f, 8f), style = stroke) }
                    drawCircle(color, 1.7f, Offset(19f, 5f))
                }
                SpaceSymbol.Heart -> {
                    val p = Path().apply {
                        moveTo(12f, 21f); cubicTo(8f, 17f, 2f, 13f, 2f, 8f)
                        cubicTo(2f, 2f, 9f, 1f, 12f, 6f); cubicTo(15f, 1f, 22f, 2f, 22f, 8f)
                        cubicTo(22f, 13f, 16f, 17f, 12f, 21f); close()
                    }
                    if (filled) drawPath(p, color) else drawPath(p, color, style = stroke)
                }
                SpaceSymbol.Back, SpaceSymbol.Arrow -> {
                    val direction = if (symbol == SpaceSymbol.Back) -1 else 1
                    drawLine(color, Offset(4f, 12f), Offset(20f, 12f), 1.7f, StrokeCap.Round)
                    val x = if (direction == 1) 20f else 4f
                    drawLine(color, Offset(x, 12f), Offset(x - direction * 6f, 6f), 1.7f, StrokeCap.Round)
                    drawLine(color, Offset(x, 12f), Offset(x - direction * 6f, 18f), 1.7f, StrokeCap.Round)
                }
                SpaceSymbol.Refresh -> {
                    drawArc(color, 40f, 290f, false, Offset(4f, 4f), Size(16f, 16f), style = stroke)
                    val p = Path().apply { moveTo(15f, 3f); lineTo(20f, 4f); lineTo(19f, 9f) }
                    drawPath(p, color, style = stroke)
                }
                SpaceSymbol.Play -> {
                    val p = Path().apply { moveTo(8f, 4f); lineTo(21f, 12f); lineTo(8f, 20f); close() }
                    drawPath(p, color, style = stroke)
                }
                SpaceSymbol.Calendar -> {
                    drawRoundRect(color, Offset(4f, 5f), Size(16f, 16f), CornerRadius(3f), style = stroke)
                    drawLine(color, Offset(4f, 10f), Offset(20f, 10f), 1.7f)
                    drawLine(color, Offset(8f, 3f), Offset(8f, 7f), 1.7f, StrokeCap.Round)
                    drawLine(color, Offset(16f, 3f), Offset(16f, 7f), 1.7f, StrokeCap.Round)
                }
            }
        }
    }
}
