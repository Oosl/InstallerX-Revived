// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2025-2026 InstallerX Revived contributors
package com.rosan.installer.data.privileged.repository.recycler

import com.rosan.installer.IPrivilegedService
import com.rosan.installer.data.privileged.model.DefaultPrivilegedService
import com.rosan.installer.data.privileged.repository.recyclable.Recycler
import com.rosan.installer.data.privileged.repository.recyclable.UserService
import com.rosan.installer.data.privileged.util.requireShizukuPermissionGranted
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import rikka.shizuku.Shizuku
import timber.log.Timber

/**
 * A Recycler that provides a UserService operating in "Shizuku Hook Mode".
 * It does NOT modify the global process state. Instead, it provides a PrivilegedService
 * that internally fetches hooked system services on-demand from the ShizukuHook factory.
 */
class ShizukuHookRecycler : Recycler<ShizukuHookRecycler.HookedUserService>(), KoinComponent {

    class HookedUserService : UserService {
        override val privileged: IPrivilegedService by lazy {
            DefaultPrivilegedService(isHookMode = true)
        }

        override fun close() {
            Timber.tag("ShizukuHookRecycler").d("close() called, no action needed in hook mode.")
        }
    }

    override fun onMake(): HookedUserService = runBlocking {
        requireShizukuPermissionGranted {
            ensureBinderReady()
            HookedUserService()
        }
    }

    private suspend fun ensureBinderReady() {
        repeat(5) {
            if (Shizuku.pingBinder()) return
            delay(100)
        }
    }
}
