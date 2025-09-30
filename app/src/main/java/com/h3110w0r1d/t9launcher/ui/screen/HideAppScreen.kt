package com.h3110w0r1d.t9launcher.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig
import com.h3110w0r1d.t9launcher.model.LocalGlobalViewModel
import com.h3110w0r1d.t9launcher.ui.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HideAppScreen() {
    val navController = LocalNavController.current!!
    val viewModel = LocalGlobalViewModel.current
    val appConfig = LocalAppConfig.current
    val hideAppList by viewModel.hideAppList.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.searchHideApp("")
    }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    BackHandler(enabled = true) {
        if (isSearching) {
            isSearching = false
            searchText = ""
            viewModel.searchHideApp("")
        } else {
            navController.popBackStack()
        }
    }
    Scaffold(
        topBar = {
            if (isSearching) {
                SearchBar(
                    expanded = false,
                    onExpandedChange = {},
                    inputField = {
                        SearchBarDefaults.InputField(
                            expanded = false,
                            query = searchText,
                            onQueryChange = {
                                searchText = it
                                viewModel.searchHideApp(searchText)
                            },
                            onSearch = {},
                            onExpandedChange = {},
                            modifier = Modifier.focusRequester(focusRequester),
                            leadingIcon = {
                                IconButton(onClick = { isSearching = false }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                    )
                                }
                            },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchText = ""
                                        viewModel.searchHideApp("")
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            },
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) { }
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.hide_app_list)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                isSearching = !isSearching
                                searchText = ""
                            },
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            items(hideAppList.size) { i ->
                ListItem(
                    leadingContent = {
                        Image(
                            bitmap = hideAppList[i].appIcon,
                            contentDescription = hideAppList[i].appName,
                            modifier =
                                Modifier
                                    .width(44.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(percent = 26)),
                        )
                    },
                    headlineContent = { Text(hideAppList[i].appName) },
                    supportingContent = { Text(hideAppList[i].packageName) },
                    trailingContent = {
                        Box(modifier = Modifier.padding(vertical = 10.dp)) {
                            Checkbox(
                                checked = appConfig.search.hiddenComponentIds.contains(hideAppList[i].componentId()),
                                onCheckedChange = null,
                            )
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clickable(
                                enabled = true,
                                onClick = {
                                    viewModel.switchAppHide(hideAppList[i])
                                },
                            ),
                )
            }
        }
    }
}
