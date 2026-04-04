// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2025-2026 InstallerX Revived contributors
package com.rosan.installer.domain.device.provider

import com.rosan.installer.domain.device.model.ShizukuMode
import kotlinx.coroutines.flow.StateFlow

interface DeviceCapabilityProvider {
    val isSessionInstallSupported: Boolean
    val hasMiPackageInstaller: Boolean

    val isSystemApp: Boolean
    val isHyperOS: Boolean
    val isMIUI: Boolean
    val isSupportMiIsland: Boolean
    val oplusOSdkVersion: String?

    /**
     * Flow emitting the current Shizuku running mode.
     * Can change dynamically during runtime.
     */
    val shizukuModeFlow: StateFlow<ShizukuMode>

    /*    */
    /**
     * Detected local root implementation.
     * Static during the app's lifecycle.
     *//*
    var rootMode: RootMode*/

    /**
     * Refreshes both Shizuku and Root detection states.
     * This should be called early in the app lifecycle (e.g., Application.onCreate).
     */
    // suspend fun refreshPrivilegeStatus()
}
