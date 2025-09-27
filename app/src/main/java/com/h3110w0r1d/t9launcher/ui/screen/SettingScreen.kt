package com.h3110w0r1d.t9launcher.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.LayersClear
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.h3110w0r1d.t9launcher.BuildConfig
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.model.AppViewModel
import com.h3110w0r1d.t9launcher.ui.theme.getPrimaryColorMap

@SuppressLint("ShowToast")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    viewModel: AppViewModel,
) {
    val appConfig by viewModel.appConfig.collectAsState()
    val context = LocalContext.current

    val isDarkMode =
        if (appConfig.nightModeFollowSystem) {
            isSystemInDarkTheme()
        } else {
            appConfig.nightModeEnabled
        }
    var selectColorDialogOpened by remember { mutableStateOf(false) }

    val themeColorNamesMap =
        hashMapOf(
            "amber" to stringResource(R.string.amber_theme),
            "blue_grey" to stringResource(R.string.blue_grey_theme),
            "blue" to stringResource(R.string.blue_theme),
            "brown" to stringResource(R.string.brown_theme),
            "cyan" to stringResource(R.string.cyan_theme),
            "deep_orange" to stringResource(R.string.deep_orange_theme),
            "deep_purple" to stringResource(R.string.deep_purple_theme),
            "green" to stringResource(R.string.green_theme),
            "indigo" to stringResource(R.string.indigo_theme),
            "light_blue" to stringResource(R.string.light_blue_theme),
            "light_green" to stringResource(R.string.light_green_theme),
            "lime" to stringResource(R.string.lime_theme),
            "orange" to stringResource(R.string.orange_theme),
            "pink" to stringResource(R.string.pink_theme),
            "purple" to stringResource(R.string.purple_theme),
            "red" to stringResource(R.string.red_theme),
            "sakura" to stringResource(R.string.sakura_theme),
            "teal" to stringResource(R.string.teal_theme),
            "yellow" to stringResource(R.string.yellow_theme),
        )
    val themeColorKeys = themeColorNamesMap.keys.toList()
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setting)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .padding(innerPadding)
                    .verticalScroll(scrollState),
        ) {
            SettingItemGroup(stringResource(R.string.hide))
            SettingItem(
                imageVector = Icons.Outlined.LayersClear,
                title = stringResource(R.string.hide_system_app),
                trailingContent = {
                    Switch(
                        checked = appConfig.hideSystemAppEnabled,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.setIsHideSystemApp(!appConfig.hideSystemAppEnabled)
                },
            )
            SettingItem(
                imageVector = Icons.Outlined.VisibilityOff,
                title = stringResource(R.string.hide_app_list),
                onClick = {
                    navController.navigate("hide_app")
                },
            )
            SettingItem(
                imageVector = ImageVector.vectorResource(R.drawable.flash_on_24px),
                title = stringResource(R.string.shortcut_setting),
                onClick = {
                    navController.navigate("shortcut")
                },
            )
            SettingItem(
                imageVector = ImageVector.vectorResource(R.drawable.match_word_24px),
                title = stringResource(R.string.english_fuzzy_match),
                description = "udio -> Audio",
                trailingContent = {
                    Switch(
                        checked = appConfig.englishFuzzyMatchEnabled,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.setEnglishFuzzyMatch(!appConfig.englishFuzzyMatchEnabled)
                },
            )

            SettingItemGroup(stringResource(R.string.appearance))

            SettingItem(
                imageVector = ImageVector.vectorResource(R.drawable.app_registration_24px),
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

            SettingItem(
                imageVector = ImageVector.vectorResource(R.drawable.ink_highlighter_24px),
                title = stringResource(R.string.is_highlight_search_result),
                trailingContent = {
                    Switch(
                        checked = appConfig.highlightSearchResultEnabled,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.setIsHighlightSearchResult(!appConfig.highlightSearchResultEnabled)
                },
            )

            SettingItem(
                imageVector = ImageVector.vectorResource(R.drawable.invert_colors_24px),
                title = stringResource(R.string.night_mode_follow_system),
                trailingContent = {
                    Switch(
                        checked = appConfig.nightModeFollowSystem,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.setNightModeFollowSystem(!appConfig.nightModeFollowSystem)
                },
            )
            if (!appConfig.nightModeFollowSystem) {
                SettingItem(
                    imageVector = ImageVector.vectorResource(R.drawable.dark_mode_24px),
                    title = stringResource(R.string.night_mode_enabled),
                    trailingContent = {
                        Switch(
                            checked = appConfig.nightModeEnabled,
                            onCheckedChange = null,
                        )
                    },
                    onClick = {
                        viewModel.setNightModeEnabled(!appConfig.nightModeEnabled)
                    },
                )
            }

//            SettingItem(
//                imageVector = ImageVector.vectorResource(R.drawable.palette_24px),
//                title = stringResource(R.string.use_system_color),
//                trailingContent = {
//                    Switch(
//                        checked = appConfig.isUseSystemColor,
//                        onCheckedChange = null,
//                    )
//                },
//                onClick = {
//                    viewModel.setIsUseSystemColor(!appConfig.isUseSystemColor)
//                },
//            )
//            if (!appConfig.isUseSystemColor) {
//                SettingItem(
//                    imageVector = ImageVector.vectorResource(R.drawable.colors_24px),
//                    title = stringResource(R.string.theme_color),
//                    description = themeColorNamesMap.get(appConfig.themeColor),
//                    onClick = {
//                        selectColorDialogOpened = true
//                    },
//                )
//            }

            SettingItemGroup(stringResource(R.string.about))

            SettingItem(
                imageVector = Icons.Outlined.Book,
                title = stringResource(R.string.user_guide),
                onClick = {
                    navController.navigate("onboarding")
                },
            )

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
    if (selectColorDialogOpened) {
        Dialog(onDismissRequest = {
            selectColorDialogOpened = false
        }) {
            Card(
                colors =
                    CardDefaults.cardColors().copy(
                        containerColor = colorScheme.background,
                    ),
                modifier =
                    Modifier
                        .fillMaxHeight(.7f),
            ) {
                LazyColumn(
                    modifier =
                        Modifier
                            .padding(8.dp, 16.dp),
                ) {
                    items(themeColorKeys) { it ->
                        ListItem(
                            leadingContent = {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(40.dp)
                                            .background(
                                                color = getPrimaryColorMap(isDarkMode, it),
                                                shape = RoundedCornerShape(100.dp),
                                            ),
                                )
                            },
                            headlineContent = {
                                Text(text = themeColorNamesMap.get(it) ?: "")
                            },
                            modifier =
                                Modifier.clickable(
                                    enabled = true,
                                    onClick = {
                                        viewModel.setThemeColor(it)
                                        selectColorDialogOpened = false
                                    },
                                ),
                        )
                    }
                }
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
