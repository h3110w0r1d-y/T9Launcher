package com.h3110w0r1d.t9launcher.ui.widget

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h3110w0r1d.t9launcher.model.AppConfig
import com.h3110w0r1d.t9launcher.vo.AppInfo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItem(
    app: AppInfo,
    onClick: () -> Unit = {},
    onLongPress: (Offset) -> Unit = {},
    appConfig: AppConfig = AppConfig(),
) {
    var darkTheme =
        if (appConfig.nightModeFollowSystem) {
            isSystemInDarkTheme()
        } else {
            appConfig.nightModeEnabled
        }
    var scaleTarget by remember { mutableFloatStateOf(1f) }
    val scaleState by animateFloatAsState(
        targetValue = scaleTarget,
        label = "AppItemScale",
    )
    val annotatedName by app.annotatedName.collectAsState()
    val matchRange by app.matchRange.collectAsState()
    val highlightColor = if (darkTheme) colorScheme.primaryFixedDim else colorScheme.primary
    app.updateAnnotatedName(highlightColor)
    LaunchedEffect(matchRange) {
        app.updateAnnotatedName(highlightColor)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            scaleTarget = 1.1f
                            onLongPress(it)
                        },
                        onPress = {
                            // 在手指按下的瞬间触发
                            scaleTarget = 0.9f
                            try {
                                awaitRelease()
                            } catch (_: Exception) {
                                // 长按成功，这里不做处理
                            }
                            scaleTarget = 1f
                        },
                        onTap = {
                            onClick()
                        },
                    )
                }.scale(scaleState)
                .animateContentSize(),
    ) {
        Box(
            modifier =
                Modifier.padding(
                    vertical = appConfig.iconVerticalPadding.dp,
                    horizontal = appConfig.iconHorizonPadding.dp,
                ),
        ) {
            Image(
                bitmap = app.appIcon,
                contentDescription = app.appName,
                modifier =
                    Modifier
                        .width(appConfig.iconSize.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(percent = appConfig.iconCornerRadius)),
            )
        }
        Text(
            text = if (appConfig.highlightSearchResultEnabled) annotatedName else AnnotatedString(app.appName),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = appConfig.appNameSize.sp,
            lineHeight = (appConfig.appNameSize * 1.2).sp,
            modifier =
                Modifier
                    .padding(bottom = appConfig.rowSpacing.dp)
                    .padding(horizontal = 4.dp),
        )
    }
}
