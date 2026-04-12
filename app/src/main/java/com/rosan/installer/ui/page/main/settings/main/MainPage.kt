// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2023-2026 iamr0s, InstallerX Revived contributors
package com.rosan.installer.ui.page.main.settings.main

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailColors
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rosan.installer.R
import com.rosan.installer.domain.settings.model.ThemeState
import com.rosan.installer.domain.settings.repository.ConfigRepository
import com.rosan.installer.ui.icons.AppIcons
import com.rosan.installer.ui.page.main.settings.SettingsSharedViewModel
import com.rosan.installer.ui.page.main.settings.config.all.AllPage
import com.rosan.installer.ui.page.main.settings.config.all.NewAllPage
import com.rosan.installer.ui.page.main.settings.preferred.NewPreferredPage
import com.rosan.installer.ui.page.main.settings.preferred.PreferredPage
import com.rosan.installer.ui.theme.LocalWindowLayoutInfo
import com.rosan.installer.ui.theme.installerMaterial3BlurEffect
import com.rosan.installer.ui.theme.rememberMaterial3BlurBackdrop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import top.yukonga.miuix.kmp.blur.layerBackdrop

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainPage(
    uiState: ThemeState,
    sharedViewModel: SettingsSharedViewModel = koinViewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
) {
    val scope = rememberCoroutineScope()
    val sharedState by sharedViewModel.state.collectAsStateWithLifecycle()
    val showExpressiveUI = uiState.isExpressive
    val useBlur = showExpressiveUI && uiState.useBlur
    val backdrop = rememberMaterial3BlurBackdrop(useBlur)

    val configRepo = koinInject<ConfigRepository>()
    val configCountFlow = remember { configRepo.flowAll().map { it.size } }
    val configCount by configCountFlow.collectAsStateWithLifecycle(initialValue = 0)
    val configLabel = stringResource(R.string.config)
    val preferredLabel = stringResource(R.string.preferred)

    val data = remember(showExpressiveUI, configLabel, preferredLabel) {
        arrayOf(
            NavigationData(
                icon = AppIcons.RoomPreferences,
                label = configLabel
            ) { outerPadding ->
                if (showExpressiveUI) NewAllPage(
                    useBlur = useBlur,
                    outerPadding = outerPadding
                )
                else AllPage(outerPadding = outerPadding)
            },
            NavigationData(
                icon = AppIcons.SettingsSuggest,
                label = preferredLabel
            ) { outerPadding ->
                if (showExpressiveUI) NewPreferredPage(
                    useBlur = useBlur,
                    outerPadding = outerPadding
                )
                else PreferredPage(outerPadding = outerPadding)
            }
        )
    }

    val pagerState = rememberPagerState(
        initialPage = sharedState.lastMainPageIndex,
        pageCount = { data.size }
    )
    val currentPage = pagerState.currentPage
    LaunchedEffect(currentPage) {
        if (sharedState.lastMainPageIndex != currentPage) {
            sharedViewModel.updateLastMainPageIndex(currentPage)
        }
    }
    fun onPageChanged(page: Int) {
        scope.launch {
            pagerState.animateScrollToPage(page = page)
        }
    }

    val layoutInfo = LocalWindowLayoutInfo.current
    val showRail = layoutInfo.showNavigationRail
    val isMedium = layoutInfo.isMediumPortrait

    val navigationSide =
        if (showRail) WindowInsetsSides.Start
        else WindowInsetsSides.Bottom

    val navigationWindowInsets = WindowInsets.safeDrawing.only(
        (if (showRail) WindowInsetsSides.Vertical
        else WindowInsetsSides.Horizontal) + navigationSide
    )

    if (!showRail) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                RowNavigation(
                    modifier = Modifier.installerMaterial3BlurEffect(backdrop),
                    isM3e = showExpressiveUI,
                    windowInsets = navigationWindowInsets,
                    data = data,
                    currentPage = currentPage,
                    onPageChanged = { onPageChanged(it) },
                    configCount = configCount,
                    containerColor = if (useBlur) Color.Transparent else BottomAppBarDefaults.containerColor,
                    isMedium = isMedium
                )
            }
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true,
                modifier = Modifier
                    .fillMaxSize()
                    .then(backdrop?.let { Modifier.layerBackdrop(backdrop) } ?: Modifier)
            ) { page ->
                data[page].content(paddingValues)
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxSize()) {
            ColumnNavigation(
                isM3e = showExpressiveUI,
                windowInsets = navigationWindowInsets,
                data = data,
                currentPage = currentPage,
                onPageChanged = { onPageChanged(it) }
            )

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .then(backdrop?.let { Modifier.layerBackdrop(backdrop) } ?: Modifier)
            ) { page ->
                data[page].content(PaddingValues(0.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RowNavigation(
    modifier: Modifier = Modifier,
    isM3e: Boolean,
    windowInsets: WindowInsets,
    data: Array<NavigationData>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    configCount: Int,
    containerColor: Color = if (isM3e) MaterialTheme.colorScheme.surfaceContainer else BottomAppBarDefaults.containerColor,
    isMedium: Boolean = false
) {
    if (isM3e) {
        ShortNavigationBar(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentSize(),
            windowInsets = windowInsets,
            containerColor = containerColor,
            arrangement = if (isMedium) ShortNavigationBarArrangement.Centered else ShortNavigationBarArrangement.EqualWeight
        ) {
            data.forEachIndexed { index, navigationData ->
                ShortNavigationBarItem(
                    selected = currentPage == index,
                    onClick = { onPageChanged(index) },
                    iconPosition = if (isMedium) NavigationItemIconPosition.Start else NavigationItemIconPosition.Top,
                    icon = {
                        val showBadge = index == 0 && configCount > 1

                        BadgedBox(
                            badge = {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showBadge,
                                    enter = scaleIn() + fadeIn(),
                                    exit = scaleOut() + fadeOut(),
                                    label = "badge"
                                ) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ) { Text(configCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = navigationData.icon,
                                contentDescription = navigationData.label
                            )
                        }
                    },
                    label = {
                        Text(text = navigationData.label)
                    }
                )
            }
        }
    } else {
        // Fallback for non-expressive UI
        FlexibleBottomAppBar(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentSize(),
            windowInsets = windowInsets,
            expandedHeight = 72.dp,
            containerColor = containerColor,
            horizontalArrangement = BottomAppBarDefaults.FlexibleHorizontalArrangement,
            content = {
                data.forEachIndexed { index, navigationData ->
                    NavigationBarItem(
                        selected = currentPage == index,
                        onClick = { onPageChanged(index) },
                        icon = {
                            val showBadge = index == 0 && configCount > 1

                            BadgedBox(
                                badge = {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = showBadge,
                                        enter = scaleIn() + fadeIn(),
                                        exit = scaleOut() + fadeOut(),
                                        label = "badge"
                                    ) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.secondary,
                                            contentColor = MaterialTheme.colorScheme.onSecondary
                                        ) { Text(configCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = navigationData.icon,
                                    contentDescription = navigationData.label
                                )
                            }
                        },
                        label = {
                            Text(text = navigationData.label)
                        },
                        alwaysShowLabel = false
                    )
                }
            }
        )
    }
}

@Composable
fun ColumnNavigation(
    isM3e: Boolean,
    windowInsets: WindowInsets,
    data: Array<NavigationData>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit
) {
    val state = rememberWideNavigationRailState()
    val scope = rememberCoroutineScope()

    WideNavigationRail(
        state = state,
        windowInsets = windowInsets,
        colors = if (isM3e) WideNavigationRailColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modalContainerColor = WideNavigationRailDefaults.colors().modalContainerColor,
            modalScrimColor = WideNavigationRailDefaults.colors().modalScrimColor,
            modalContentColor = WideNavigationRailDefaults.colors().modalContentColor
        ) else WideNavigationRailDefaults.colors(),
        header = {
            IconButton(
                modifier =
                    Modifier
                        .padding(start = 24.dp)
                        .semantics {
                            stateDescription =
                                if (state.currentValue == WideNavigationRailValue.Expanded) {
                                    "Expanded"
                                } else {
                                    "Collapsed"
                                }
                        },
                onClick = {
                    scope.launch {
                        if (state.targetValue == WideNavigationRailValue.Expanded)
                            state.collapse()
                        else state.expand()
                    }
                },
            ) {
                if (state.targetValue == WideNavigationRailValue.Expanded) {
                    Icon(AppIcons.MenuOpen, "Collapse rail")
                } else {
                    Icon(AppIcons.Menu, "Expand rail")
                }
            }
        }
    ) {
        data.forEachIndexed { index, navigationData ->
            WideNavigationRailItem(
                railExpanded = state.targetValue == WideNavigationRailValue.Expanded,
                selected = currentPage == index,
                onClick = { onPageChanged(index) },
                icon = {
                    Icon(
                        imageVector = navigationData.icon,
                        contentDescription = navigationData.label
                    )
                },
                label = {
                    Text(text = navigationData.label)
                }
            )
        }
    }
}