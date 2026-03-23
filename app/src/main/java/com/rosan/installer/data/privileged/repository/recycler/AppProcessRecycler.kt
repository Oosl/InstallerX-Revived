// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2023-2026 iamr0s, InstallerX Revived contributors
package com.rosan.installer.data.privileged.repository.recycler

import com.rosan.app_process.AppProcess
import com.rosan.installer.data.privileged.exception.AppProcessNotWorkException
import com.rosan.installer.data.privileged.exception.RootNotWorkException
import com.rosan.installer.data.privileged.repository.recyclable.Recycler
import com.rosan.installer.data.privileged.util.SHELL_ROOT

class AppProcessRecycler(private val shell: String) : Recycler<AppProcess>() {

    override val delayDuration: Long = 100L

    private class CustomizeAppProcess(private val shell: String) : AppProcess.Terminal() {
        override fun newTerminal(): MutableList<String> {
            // Split the shell command and its arguments properly to build the command list
            return shell.trim().split("\\s+".toRegex()).toMutableList()
        }
    }

    override fun onMake(): AppProcess {
        return CustomizeAppProcess(shell).apply {
            if (init()) return@apply

            val command = shell.trim().split("\\s+".toRegex()).firstOrNull()
            val fullCommand = shell.trim()

            // Strictly check if the user intended to use standard root.
            // Avoid throwing RootNotWorkException if arguments like "su 2000" were passed.
            if (command == SHELL_ROOT && fullCommand == SHELL_ROOT) {
                throw RootNotWorkException("Cannot access su command")
            } else {
                // Throw the exact full command that failed initialization for accurate debugging
                throw AppProcessNotWorkException("AppProcess init failed for shell: $fullCommand")
            }
        }
    }
}