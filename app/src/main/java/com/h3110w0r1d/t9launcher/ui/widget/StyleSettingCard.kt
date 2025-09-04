package com.h3110w0r1d.t9launcher.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StyleSettingCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(modifier = Modifier.padding(bottom = 10.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, modifier = Modifier.padding(bottom = 6.dp))
            content()
        }
    }
}
