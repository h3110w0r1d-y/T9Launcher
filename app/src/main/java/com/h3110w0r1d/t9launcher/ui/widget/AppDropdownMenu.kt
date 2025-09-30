package com.h3110w0r1d.t9launcher.ui.widget

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.app.AppInfo

@Composable
fun AppDropdownMenu(
    app: AppInfo,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        shape = RoundedCornerShape(16.dp),
        containerColor = colorScheme.surfaceContainer,
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(id = R.string.app_info)) },
            onClick = {
                app.detail(context)
                onExpandedChange(false)
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(id = R.string.copy_package_name)) },
            onClick = {
                app.copyPackageName(context)
                onExpandedChange(false)
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Outlined.DeleteForever,
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(id = R.string.uninstall_app)) },
            onClick = {
                app.uninstall(context)
                onExpandedChange(false)
            },
        )
    }
}
