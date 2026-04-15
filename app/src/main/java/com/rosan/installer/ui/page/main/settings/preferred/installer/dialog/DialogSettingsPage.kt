// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
package com.rosan.installer.ui.page.main.settings.preferred.installer.dialog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rosan.installer.R
import com.rosan.installer.ui.icons.AppIcons
import com.rosan.installer.ui.navigation.LocalNavigator
import com.rosan.installer.ui.page.main.widget.setting.AppBackButton
import com.rosan.installer.ui.page.main.widget.setting.SwitchWidget
import com.rosan.installer.ui.theme.none
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogSettingsPage(
    viewModel: DialogSettingsViewModel = koinViewModel()
) {
    val navigator = LocalNavigator.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val layoutDirection = LocalLayoutDirection.current
    val horizontalSafeInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.none,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dialog_settings)) },
                navigationIcon = {
                    AppBackButton(onClick = { navigator.pop() })
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = horizontalSafeInsets.calculateStartPadding(layoutDirection),
                top = paddingValues.calculateTopPadding(),
                end = horizontalSafeInsets.calculateEndPadding(layoutDirection),
                bottom = paddingValues.calculateBottomPadding()
            )
        ) {
            item {
                SwitchWidget(
                    icon = AppIcons.MultiLineSettingIcon,
                    title = stringResource(id = R.string.version_compare_in_single_line),
                    description = stringResource(id = R.string.version_compare_in_single_line_desc),
                    checked = uiState.versionCompareInSingleLine,
                    isM3E = false,
                    onCheckedChange = {
                        viewModel.dispatch(DialogSettingsAction.ChangeVersionCompareInSingleLine(it))
                    }
                )
            }

            item {
                SwitchWidget(
                    icon = AppIcons.SingleLineSettingIcon,
                    title = stringResource(id = R.string.sdk_compare_in_multi_line),
                    description = stringResource(id = R.string.sdk_compare_in_multi_line_desc),
                    checked = uiState.sdkCompareInMultiLine,
                    isM3E = false,
                    onCheckedChange = {
                        viewModel.dispatch(DialogSettingsAction.ChangeSdkCompareInMultiLine(it))
                    }
                )
            }

            item {
                SwitchWidget(
                    icon = AppIcons.MenuOpen,
                    title = stringResource(id = R.string.show_dialog_install_extended_menu),
                    description = stringResource(id = R.string.show_dialog_install_extended_menu_desc),
                    checked = uiState.showDialogInstallExtendedMenu,
                    isM3E = false,
                    onCheckedChange = {
                        viewModel.dispatch(DialogSettingsAction.ChangeShowDialogInstallExtendedMenu(it))
                    }
                )
            }

            item {
                SwitchWidget(
                    icon = AppIcons.Suggestion,
                    title = stringResource(id = R.string.show_intelligent_suggestion),
                    description = stringResource(id = R.string.show_intelligent_suggestion_desc),
                    checked = uiState.showSmartSuggestion,
                    isM3E = false,
                    onCheckedChange = {
                        viewModel.dispatch(DialogSettingsAction.ChangeShowSuggestion(it))
                    }
                )
            }

            item {
                SwitchWidget(
                    icon = AppIcons.Silent,
                    title = stringResource(id = R.string.auto_silent_install),
                    description = stringResource(id = R.string.auto_silent_install_desc),
                    checked = uiState.autoSilentInstall,
                    onCheckedChange = {
                        viewModel.dispatch(DialogSettingsAction.ChangeAutoSilentInstall(it))
                    }
                )
            }

            item {
                SwitchWidget(
                    icon = AppIcons.NotificationDisabled,
                    title = stringResource(id = R.string.disable_notification_on_dismiss),
                    description = stringResource(id = R.string.close_notification_immediately_on_dialog_dismiss),
                    checked = uiState.disableNotificationForDialogInstall,
                    isM3E = false,
                    onCheckedChange = {
                        viewModel.dispatch(DialogSettingsAction.ChangeShowDisableNotification(it))
                    }
                )
            }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}
