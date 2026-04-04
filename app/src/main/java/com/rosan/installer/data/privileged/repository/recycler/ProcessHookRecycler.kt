// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2025-2026 InstallerX Revived contributors
package com.rosan.installer.data.privileged.repository.recycler

import android.content.Context
import android.os.IBinder
import com.rosan.app_process.AppProcess
import com.rosan.installer.IPrivilegedService
import com.rosan.installer.data.privileged.model.DefaultPrivilegedService
import com.rosan.installer.data.privileged.repository.recyclable.Recyclable
import com.rosan.installer.data.privileged.repository.recyclable.Recycler
import com.rosan.installer.data.privileged.repository.recyclable.RecyclerManager
import com.rosan.installer.data.privileged.repository.recyclable.UserService
import org.koin.core.component.KoinComponent

/**
 * A Recycler that provides a UserService operating in "Process Hook Mode" (Root Hook).
 *
 * It creates a local [DefaultPrivilegedService] but proxies underlying Binder calls
 * through a remote [AppProcess] (Root/Shell).
 */
class ProcessHookRecycler(
    private val shell: String,
    private val context: Context,
    private val appProcessRecyclerManager: RecyclerManager<String, AppProcessRecycler>
) : Recycler<ProcessHookRecycler.HookedUserService>(), KoinComponent {

    class HookedUserService(
        private val appProcessHandle: Recyclable<AppProcess>
    ) : UserService {

        // Provide access to the binder wrapper logic for external users (like InstallerRepo)
        fun binderWrapper(binder: IBinder): IBinder {
            return appProcessHandle.entity.binderWrapper(binder)
        }

        // Inject the binder wrapper logic into DefaultPrivilegedService for standard service calls
        override val privileged: IPrivilegedService by lazy {
            DefaultPrivilegedService(isHookMode = true) { binder ->
                binderWrapper(binder)
            }
        }

        override fun close() {
            // Recycle the underlying AppProcess (shell) when this service is closed
            appProcessHandle.recycle()
        }
    }

    override fun onMake(): HookedUserService {
        // Obtain a raw AppProcess shell from the injected manager
        val appProcessHandle = appProcessRecyclerManager.get(shell).make()

        // Critical Fix: Ensure the reused AppProcess is initialized.
        // If the process was previously closed/recycled, its context/manager might be null.
        // init() checks state internally and is safe to call multiple times.
        if (!appProcessHandle.entity.init(context)) {
            // If init fails, we might want to recycle it and try fresh or throw,
            // but usually init() will re-create the manager.
            // If it fails returning false, it usually means binder connection failed.
            throw IllegalStateException("Failed to initialize AppProcess for Hook Mode.")
        }

        return HookedUserService(appProcessHandle)
    }
}