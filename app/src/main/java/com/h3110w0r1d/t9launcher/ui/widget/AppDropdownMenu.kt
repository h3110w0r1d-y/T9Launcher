package com.h3110w0r1d.t9launcher.ui.widget

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/** Keep the menu in the accessibility window instead of creating a Popup subwindow. */
@Composable
fun OverlayAppMenu(
    app: AppInfo,
    onDismiss: () -> Unit,
    onExternalAction: () -> Unit,
) {
    val context = LocalContext.current
    fun finish(started: Boolean) {
        if (started) {
            onDismiss()
            onExternalAction()
        } else {
            Toast.makeText(context, R.string.overlay_launch_failed, Toast.LENGTH_SHORT).show()
        }
    }
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .3f)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.safeDrawingPadding().padding(24.dp).widthIn(max = 360.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(app.appName, Modifier.padding(16.dp))
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.app_info)) },
                    leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    onClick = { finish(app.detail(context)) },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.copy_package_name)) },
                    leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                    onClick = { app.copyPackageName(context); onDismiss() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.uninstall_app)) },
                    leadingIcon = { Icon(Icons.Outlined.DeleteForever, contentDescription = null) },
                    onClick = { finish(app.uninstall(context)) },
                )
            }
        }
    }
}
