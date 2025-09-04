package com.h3110w0r1d.t9launcher.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AppRegistration
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.LayersClear
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.h3110w0r1d.t9launcher.BuildConfig
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.model.AppViewModel

@SuppressLint("ShowToast")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    viewModel: AppViewModel,
) {
    val appConfig by viewModel.appConfig.collectAsState()
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.setting)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier =
                Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .fillMaxHeight(),
        ) {
            item {
                SettingItemGroup(stringResource(R.string.hide))
                SettingItem(
                    imageVector = Icons.Outlined.LayersClear,
                    title = stringResource(R.string.hide_system_app),
                    trailingContent = {
                        Switch(
                            checked = appConfig.isHideSystemApp,
                            onCheckedChange = null,
                        )
                    },
                    onClick = {
                        viewModel.setIsHideSystemApp(!appConfig.isHideSystemApp)
                    },
                )
                SettingItem(
                    imageVector = Icons.Outlined.VisibilityOff,
                    title = stringResource(R.string.hide_app_list),
                    onClick = {
                        navController.navigate("hide_app")
                    },
                )

                SettingItemGroup(stringResource(R.string.appearance))

                SettingItem(
                    imageVector = Icons.Outlined.AppRegistration,
                    title = stringResource(R.string.app_list_style),
                    onClick = {
                        navController.navigate("app_list_style")
                    },
                )

                SettingItem(
                    imageVector = Icons.Outlined.Keyboard,
                    title = stringResource(R.string.keyboard_style),
                    onClick = {
                        navController.navigate("keyboard_style")
                    },
                )

                SettingItemGroup(stringResource(R.string.about))

                SettingItem(
                    imageVector = Icons.Outlined.Person,
                    title = stringResource(R.string.author),
                    description = "@h3110w0r1d-y",
                    onClick = {
                        val uri = "https://github.com/h3110w0r1d-y".toUri()
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                )

                SettingItem(
                    imageVector = Icons.Outlined.Merge,
                    title = stringResource(R.string.repository),
                    description = "https://github.com/h3110w0r1d-y/T9Launcher",
                    onClick = {
                        val uri = "https://github.com/h3110w0r1d-y/T9Launcher".toUri()
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                )
                var clickCount by remember { mutableIntStateOf(0) }
                var mToast by remember { mutableStateOf<Toast?>(null) }
                var lastTimeStamp by remember { mutableLongStateOf(0L) }
                val maxClickCount = 7
                SettingItem(
                    imageVector = Icons.Outlined.Info,
                    title = stringResource(R.string.version),
                    description = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    onClick = {
                        if (mToast != null) {
                            mToast?.cancel()
                        }
                        if (System.currentTimeMillis() - lastTimeStamp < 500) {
                            clickCount++
                        } else {
                            clickCount = 1
                        }
                        lastTimeStamp = System.currentTimeMillis()
                        if (clickCount >= maxClickCount) {
                            mToast = Toast.makeText(context, "啥都木有", Toast.LENGTH_SHORT)
                            mToast?.show()
                        } else {
                            mToast =
                                Toast.makeText(
                                    context,
                                    "Click $clickCount times",
                                    Toast.LENGTH_SHORT,
                                )
                            mToast?.show()
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun SettingItemGroup(title: String) {
    Text(
        text = title,
        color = colorScheme.onPrimaryContainer,
        fontSize = 14.sp,
        modifier =
            Modifier
                .padding(start = 16.dp, top = 16.dp)
                .padding(vertical = 4.dp),
    )
}

@Composable
fun SettingItem(
    imageVector: ImageVector,
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = if (description != null)Modifier.height(42.dp) else Modifier,
            )
        },
        headlineContent = { Text(title) },
        supportingContent = { if (description != null) Text(description) },
        trailingContent = trailingContent,
        modifier =
            Modifier.clickable(
                onClick = onClick,
            ),
    )
}
