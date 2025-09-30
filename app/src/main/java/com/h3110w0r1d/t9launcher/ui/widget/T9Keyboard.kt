package com.h3110w0r1d.t9launcher.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.app.AppInfo
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig

@Composable
fun T9Keyboard(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit = {},
    onLongClick: (String) -> Unit = {},
    onCancel: (() -> Unit)? = null,
    appMap: Map<String, AppInfo> = mapOf(),
) {
    val appConfig = LocalAppConfig.current
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = appConfig.keyboardStyle.keyboardBottomPadding.dp)
                .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (i in 1..8 step 3) {
            T9ButtonRow(
                btnTexts = Array(3) { j -> "${i + j}" },
                onClick = onClick,
                onLongClick = onLongClick,
                appMap = appMap,
            )
        }
        T9ButtonRow(
            btnTexts = arrayOf("setting", "0", "delete"),
            onClick = onClick,
            onLongClick = onLongClick,
            onCancel = onCancel,
        )
    }
}

@Composable
fun T9ButtonRow(
    btnTexts: Array<String>,
    onClick: (String) -> Unit = {},
    onLongClick: (String) -> Unit = {},
    onCancel: (() -> Unit)? = null,
    appMap: Map<String, AppInfo> = mapOf(),
) {
    val appConfig = LocalAppConfig.current
    Row(
        modifier = Modifier.fillMaxWidth(appConfig.keyboardStyle.keyboardWidth),
        horizontalArrangement = Arrangement.Center,
    ) {
        for (btnText in btnTexts) {
            T9Button(
                text = btnText,
                modifier =
                    Modifier
                        .weight(1f),
                onClick = { onClick(btnText) },
                onLongClick = { onLongClick(btnText) },
                onCancel = onCancel,
                appMap = appMap,
            )
        }
    }
}

@Composable
fun T9Button(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onCancel: (() -> Unit)? = null,
    appMap: Map<String, AppInfo>,
) {
    val appConfig = LocalAppConfig.current
    val letterMap =
        mapOf(
            "1" to "^_^",
            "2" to "ABC",
            "3" to "DEF",
            "4" to "GHI",
            "5" to "JKL",
            "6" to "MNO",
            "7" to "PQRS",
            "8" to "TUV",
            "9" to "WXYZ",
            "0" to "*",
        )

    var isCancel by remember { mutableStateOf(true) }
    TextButton(
        onClick = {
            isCancel = false
        },
        shape = RoundedCornerShape(12.dp),
        modifier =
            modifier
                .height(appConfig.keyboardStyle.keyboardButtonHeight.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        isCancel = true
                        val down = awaitFirstDown(false)
                        val longPress = awaitLongPressOrCancellation(down.id)
                        if (longPress != null) {
                            onLongClick()
                        } else {
                            if (text == "delete" && isCancel) {
                                onCancel?.invoke()
                            } else {
                                onClick()
                            }
                        }
                    }
                },
    ) {
        if (letterMap[text] != null) {
            Box {
                if (text.toInt() > 0 && !appConfig.shortcutConfig[text.toInt() - 1].isEmpty()) {
                    val componentId = appConfig.shortcutConfig[text.toInt() - 1]
                    val appInfo = appMap[componentId]
                    if (appInfo != null) {
                        Image(
                            bitmap = appInfo.appIcon,
                            contentDescription = appInfo.appName,
                            modifier =
                                Modifier
                                    .width(appConfig.keyboardStyle.keyboardQSIconSize.dp)
                                    .aspectRatio(1f)
                                    .alpha(appConfig.keyboardStyle.keyboardQSIconAlpha)
                                    .clip(RoundedCornerShape(percent = appConfig.appListStyle.iconCornerRadius))
                                    .align(Alignment.Center),
                        )
                    }
                }
                Column {
                    Text(
                        text = text,
                        fontSize = 20.sp,
                        color = colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = letterMap[text] ?: "",
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = colorScheme.outline,
                    )
                }
            }
        } else {
            Icon(
                modifier =
                    Modifier
                        .size(20.dp),
                imageVector = if (text == "delete") Icons.AutoMirrored.Outlined.Backspace else Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.setting),
                tint = colorScheme.onSurface,
            )
        }
    }
}
