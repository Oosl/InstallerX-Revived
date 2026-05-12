// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.ui.page.main.widget.setting

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

// A dedicated widget for radio button settings items
@Composable
fun RadioButtonWidget(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    iconPlaceholder: Boolean = true,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    BaseWidget(
        modifier = modifier,
        icon = icon,
        iconPlaceholder = iconPlaceholder,
        title = title,
        description = description,
        selected = selected,
        enabled = enabled,
        onClick = onClick
    ) {
        RadioButton(
            selected = selected,
            // The click event is already handled by BaseWidget's ListItem,
            // passing null here avoids double-triggering ripples
            onClick = null,
            enabled = enabled,
            // Inherit the dynamic content color provided by BaseWidget
            colors = RadioButtonDefaults.colors(
                selectedColor = LocalContentColor.current
            )
        )
    }
}
