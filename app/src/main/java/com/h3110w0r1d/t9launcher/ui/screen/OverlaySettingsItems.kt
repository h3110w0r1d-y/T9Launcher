package com.h3110w0r1d.t9launcher.ui.screen

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig
import com.h3110w0r1d.t9launcher.model.LocalGlobalViewModel
import com.h3110w0r1d.t9launcher.overlay.OverlayServiceState

@Composable
fun OverlaySettingsItems() {
    val config = LocalAppConfig.current.overlay
    val viewModel = LocalGlobalViewModel.current
    val context = LocalContext.current
    val connected by OverlayServiceState.connected.collectAsState()
    val systemShortcut = OverlayServiceState.usesSystemShortcut
    var showDisclosure by rememberSaveable { mutableStateOf(false) }

    fun openAccessibilitySettings() {
        val opened = runCatching {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }.isSuccess
        if (!opened) Toast.makeText(context, R.string.overlay_settings_failed, Toast.LENGTH_LONG).show()
    }

    SettingItemGroup(stringResource(R.string.overlay_title))
    SettingItem(
        imageVector = Icons.Outlined.AccessibilityNew,
        title = stringResource(if (systemShortcut) R.string.overlay_system_shortcut else R.string.overlay_enable),
        description = stringResource(if (systemShortcut) R.string.overlay_system_shortcut_summary else R.string.overlay_enable_summary),
        trailingContent = if (systemShortcut) null else { { Switch(checked = config.enabled, onCheckedChange = null) } },
        onClick = {
            if (!systemShortcut && config.enabled) viewModel.updateOverlayEnabled(false) else showDisclosure = true
        },
    )
    SettingItem(
        imageVector = Icons.Outlined.AccessibilityNew,
        title = stringResource(R.string.overlay_service_status),
        description = stringResource(
            if (systemShortcut) {
                if (connected) R.string.overlay_system_active else R.string.overlay_system_idle
            } else if (connected) R.string.overlay_connected else R.string.overlay_disconnected,
        ),
        onClick = { openAccessibilitySettings() },
    )
    if (!systemShortcut && config.enabled) {
        SettingItem(
            imageVector = Icons.Outlined.SettingsBackupRestore,
            title = stringResource(R.string.overlay_reset_position),
            onClick = {
                viewModel.resetOverlayPosition()
                OverlayServiceState.resetPosition()
            },
        )
    }
    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            title = { Text(stringResource(R.string.overlay_disclosure_title)) },
            text = { Text(stringResource(if (systemShortcut) R.string.overlay_system_disclosure else R.string.overlay_disclosure_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDisclosure = false
                    if (systemShortcut) {
                        openAccessibilitySettings()
                    } else {
                        viewModel.updateOverlayEnabled(true)
                        if (!connected) openAccessibilitySettings()
                    }
                }) { Text(stringResource(R.string.overlay_agree)) }
            },
            dismissButton = {
                TextButton(onClick = { showDisclosure = false }) { Text(stringResource(android.R.string.cancel)) }
            },
        )
    }
}
