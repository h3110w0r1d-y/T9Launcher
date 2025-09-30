package com.h3110w0r1d.t9launcher.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig
import com.h3110w0r1d.t9launcher.model.LocalGlobalViewModel
import com.h3110w0r1d.t9launcher.ui.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutScreen() {
    val navController = LocalNavController.current!!
    val viewModel = LocalGlobalViewModel.current
    val appConfig = LocalAppConfig.current
    val appMap by viewModel.appMap.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shortcut_setting)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            items(9) { i ->
                val componentId = appConfig.shortcutConfig.getOrNull(i) ?: ""
                val appInfo = appMap[componentId]
                ListItem(
                    leadingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(40.dp)
                                        .border(
                                            width = 2.dp, // 边框宽度
                                            color = colorScheme.onSurface, // 边框颜色
                                            shape = RoundedCornerShape(10.dp), // 设置圆角
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = (i + 1).toString(),
                                    fontSize = 20.sp,
                                )
                            }
                            if (appInfo != null) {
                                Image(
                                    bitmap = appInfo.appIcon,
                                    contentDescription = appInfo.appName,
                                    modifier =
                                        Modifier
                                            .padding(start = 8.dp)
                                            .width(42.dp)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(percent = 26)),
                                )
                            }
                        }
                    },
                    headlineContent = {
                        if (appInfo != null) {
                            Text(appInfo.appName)
                        } else {
                            Text(stringResource(R.string.not_set))
                        }
                    },
                    supportingContent = {
                        if (appInfo != null) {
                            Text(
                                appInfo.packageName,
                                maxLines = 1, // 设置最大行数为1，强制文本不换行
                                overflow = TextOverflow.Ellipsis, // 当文本溢出时，用省略号代替
                            )
                        }
                    },
                    trailingContent = {
                        if (appInfo != null) {
                            Box(modifier = Modifier.padding(vertical = 10.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier =
                                        Modifier.clickable(
                                            enabled = true,
                                            onClick = {
                                                viewModel.setQuickStartApp(i, "")
                                            },
                                        ),
                                )
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clickable(
                                enabled = true,
                                onClick = {
                                    navController.navigate("select_shortcut/$i")
                                },
                            ),
                )
            }
        }
    }
}
