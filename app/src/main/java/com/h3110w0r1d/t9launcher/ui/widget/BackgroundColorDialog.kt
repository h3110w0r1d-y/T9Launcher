package com.h3110w0r1d.t9launcher.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.h3110w0r1d.t9launcher.R

@Composable
fun BackgroundColorDialog(initialColor: Color, onDismiss: () -> Unit, onConfirm: (Int?) -> Unit) {
    val hsv = FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.toArgb(), it) }
    var hue by rememberSaveable { mutableFloatStateOf(hsv[0]) }
    var saturation by rememberSaveable { mutableFloatStateOf(hsv[1]) }
    var value by rememberSaveable { mutableFloatStateOf(hsv[2]) }
    val selected = Color.hsv(hue, saturation, value)
    val paletteLabel = stringResource(R.string.background_palette)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.background_color)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Box(Modifier.fillMaxWidth().height(40.dp).background(selected))
                Text("#%06X".format(selected.toArgb() and 0xFFFFFF))
                Canvas(
                    Modifier.fillMaxWidth().height(180.dp)
                        .semantics { contentDescription = paletteLabel }
                        .pointerInput(Unit) {
                            detectTapGestures { position ->
                                saturation = (position.x / size.width).coerceIn(0f, 1f)
                                value = 1f - (position.y / size.height).coerceIn(0f, 1f)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                saturation = (change.position.x / size.width).coerceIn(0f, 1f)
                                value = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                            }
                        },
                ) {
                    drawRect(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f))))
                    drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                    val point = Offset(saturation * size.width, (1f - value) * size.height)
                    drawCircle(Color.Black, 7.dp.toPx(), point, style = Stroke(3.dp.toPx()))
                    drawCircle(Color.White, 7.dp.toPx(), point, style = Stroke(1.dp.toPx()))
                }
                ColorSlider(stringResource(R.string.color_hue), hue, 0f..359.9f) { hue = it }
                ColorSlider(stringResource(R.string.color_saturation), saturation, 0f..1f) { saturation = it }
                ColorSlider(stringResource(R.string.color_brightness), value, 0f..1f) { value = it }
                TextButton(onClick = { onConfirm(null) }) {
                    Text(stringResource(R.string.background_adaptive_reset))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toArgb()) }) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
    )
}

@Composable
private fun ColorSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Text(label)
    Slider(value = value, onValueChange = onChange, valueRange = range, modifier = Modifier.semantics { contentDescription = label })
}
