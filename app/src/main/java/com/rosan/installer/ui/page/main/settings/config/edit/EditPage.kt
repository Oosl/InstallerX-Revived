// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2023-2026 iamr0s, InstallerX Revived contributors
package com.rosan.installer.ui.page.main.settings.config.edit

import android.os.Build
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rosan.installer.R
import com.rosan.installer.ui.icons.AppIcons
import com.rosan.installer.ui.page.main.widget.card.InfoTipCard
import com.rosan.installer.ui.page.main.widget.dialog.UnsavedChangesDialog
import com.rosan.installer.ui.page.main.widget.setting.AppBackButton
import com.rosan.installer.ui.page.main.widget.setting.DataAllowAllRequestedPermissionsWidget
import com.rosan.installer.ui.page.main.widget.setting.DataAllowDowngradeWidget
import com.rosan.installer.ui.page.main.widget.setting.DataAllowTestOnlyWidget
import com.rosan.installer.ui.page.main.widget.setting.DataApkChooseAllWidget
import com.rosan.installer.ui.page.main.widget.setting.DataAuthorizerWidget
import com.rosan.installer.ui.page.main.widget.setting.DataAutoDeleteWidget
import com.rosan.installer.ui.page.main.widget.setting.DataBypassLowTargetSdkWidget
import com.rosan.installer.ui.page.main.widget.setting.DataCustomizeAuthorizerWidget
import com.rosan.installer.ui.page.main.widget.setting.DataDeclareInstallerWidget
import com.rosan.installer.ui.page.main.widget.setting.DataDescriptionWidget
import com.rosan.installer.ui.page.main.widget.setting.DataForAllUserWidget
import com.rosan.installer.ui.page.main.widget.setting.DataInstallModeWidget
import com.rosan.installer.ui.page.main.widget.setting.DataInstallReasonWidget
import com.rosan.installer.ui.page.main.widget.setting.DataInstallRequesterWidget
import com.rosan.installer.ui.page.main.widget.setting.DataManualDexoptWidget
import com.rosan.installer.ui.page.main.widget.setting.DataNameWidget
import com.rosan.installer.ui.page.main.widget.setting.DataPackageSourceWidget
import com.rosan.installer.ui.page.main.widget.setting.DataSplitChooseAllWidget
import com.rosan.installer.ui.page.main.widget.setting.DataUserWidget
import com.rosan.installer.ui.page.main.widget.setting.DisplaySdkWidget
import com.rosan.installer.ui.page.main.widget.setting.DisplaySizeWidget
import com.rosan.installer.ui.page.main.widget.setting.LabelWidget
import com.rosan.installer.ui.theme.none
import com.rosan.installer.ui.util.isNoneActive
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditPage(
    navController: NavController,
    id: Long? = null,
    viewModel: EditViewModel = koinViewModel { parametersOf(id) }
) {
    val listState = rememberLazyListState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val stateAuthorizer = viewModel.state.data.authorizer
    val globalAuthorizer = viewModel.globalAuthorizer

    UnsavedChangesDialog(
        show = showUnsavedDialog,
        onDismiss = {
            showUnsavedDialog = false
        },
        onConfirm = {
            showUnsavedDialog = false
            navController.navigateUp()
        },
        // Pass the list of active error messages from the ViewModel.
        errorMessages = viewModel.activeErrorMessages
    )
    // The condition for interception is now expanded to include errors.
    // If there are unsaved changes OR if there are validation errors, we should intercept.
    val shouldInterceptBackPress = viewModel.hasUnsavedChanges || viewModel.hasErrors

    // Use this new combined condition for the BackHandler.
    BackHandler(enabled = shouldInterceptBackPress) {
        showUnsavedDialog = true
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is EditViewEvent.SnackBar -> {
                    snackBarHostState.showSnackbar(
                        message = event.message,
                        withDismissAction = true,
                    )
                }

                is EditViewEvent.Saved -> {
                    navController.navigateUp()
                }
            }
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    val horizontalSafeInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.none,
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(id = if (id == null) R.string.add else R.string.update)) },
                navigationIcon = { AppBackButton(onClick = { navController.navigateUp() }) },
            )
        },
        floatingActionButton = {
            SmallExtendedFloatingActionButton(
                modifier = Modifier.padding(
                    end = horizontalSafeInsets.calculateEndPadding(layoutDirection),
                    bottom = 16.dp
                ),
                icon = {
                    Icon(
                        imageVector = AppIcons.Save,
                        contentDescription = stringResource(R.string.save)
                    )
                },
                text = { Text(stringResource(R.string.save)) },
                onClick = { viewModel.dispatch(EditViewAction.SaveData) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                start = horizontalSafeInsets.calculateStartPadding(layoutDirection),
                top = paddingValues.calculateTopPadding(),
                end = horizontalSafeInsets.calculateEndPadding(layoutDirection),
                bottom = paddingValues.calculateBottomPadding() + 80.dp
            )
        ) {
            item { DataNameWidget(viewModel = viewModel) }
            item { DataDescriptionWidget(viewModel = viewModel) }
            item { DataAuthorizerWidget(viewModel = viewModel) }
            item { DataCustomizeAuthorizerWidget(viewModel = viewModel) }
            item { DataInstallModeWidget(viewModel = viewModel) }
            if (isNoneActive(stateAuthorizer, globalAuthorizer))
                item { InfoTipCard(text = stringResource(R.string.config_authorizer_none_tips)) }

            item { LabelWidget(label = stringResource(R.string.config_label_installer_settings)) }
            item { DataUserWidget(viewModel = viewModel, isM3E = false) }
            item { DataInstallReasonWidget(viewModel = viewModel, isM3E = false) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                item { DataPackageSourceWidget(viewModel, isM3E = false) }
            if (viewModel.state.isCustomInstallRequesterEnabled)
                item { DataInstallRequesterWidget(viewModel = viewModel, isM3E = false) }
            item { DataDeclareInstallerWidget(viewModel = viewModel, isM3E = false) }
            item { DataManualDexoptWidget(viewModel, isM3E = false) }
            item { DataAutoDeleteWidget(viewModel = viewModel, isM3E = false) }
            item { DisplaySdkWidget(viewModel = viewModel, isM3E = false) }
            item { DisplaySizeWidget(viewModel, isM3E = false) }

            item { LabelWidget(label = stringResource(R.string.config_label_install_options)) }
            item { DataForAllUserWidget(viewModel = viewModel, isM3E = false) }
            item { DataAllowTestOnlyWidget(viewModel = viewModel, isM3E = false) }
            item { DataAllowDowngradeWidget(viewModel = viewModel, isM3E = false) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                item { DataBypassLowTargetSdkWidget(viewModel = viewModel, isM3E = false) }
            item { DataAllowAllRequestedPermissionsWidget(viewModel = viewModel, isM3E = false) }

            item { LabelWidget(label = stringResource(R.string.config_label_preferences)) }
            item { DataSplitChooseAllWidget(viewModel = viewModel, isM3E = false) }
            item { DataApkChooseAllWidget(viewModel = viewModel, isM3E = false) }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}