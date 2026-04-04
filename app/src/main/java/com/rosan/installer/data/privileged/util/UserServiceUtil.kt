// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2023-2026 iamr0s, InstallerX Revived contributors
package com.rosan.installer.data.privileged.util

import com.rosan.installer.IPrivilegedService
import com.rosan.installer.data.privileged.exception.ShizukuNotWorkException
import com.rosan.installer.data.privileged.model.DefaultPrivilegedService
import com.rosan.installer.data.privileged.repository.recyclable.Recyclable
import com.rosan.installer.data.privileged.repository.recyclable.RecyclerManager
import com.rosan.installer.data.privileged.repository.recyclable.UserService
import com.rosan.installer.data.privileged.repository.recycler.DhizukuUserServiceRecycler
import com.rosan.installer.data.privileged.repository.recycler.ProcessHookRecycler
import com.rosan.installer.data.privileged.repository.recycler.ProcessUserServiceRecycler
import com.rosan.installer.data.privileged.repository.recycler.ShizukuHookRecycler
import com.rosan.installer.data.privileged.repository.recycler.ShizukuUserServiceRecycler
import com.rosan.installer.di.RecyclerNames
import com.rosan.installer.domain.settings.model.Authorizer
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.lang.reflect.InvocationTargetException

private const val TAG = "PrivilegedService"

private object DefaultUserService : UserService {
    override val privileged: IPrivilegedService = DefaultPrivilegedService(isHookMode = false)
    override fun close() {}
}

fun useUserService(
    isSystemApp: Boolean,
    authorizer: Authorizer,
    customizeAuthorizer: String = "",
    useHookMode: Boolean = true,
    special: (() -> String?)? = null,
    action: (UserService) -> Unit
) {
    if (authorizer == Authorizer.None) {
        if (isSystemApp) {
            Timber.tag(TAG).d("Running as System App with None Authorizer. Executing direct calls.")
            action.invoke(DefaultUserService)
        } else {
            Timber.tag(TAG).w("Authorizer is None but not running as System App. Privileged action skipped.")
        }
        return
    }

    val recycler = getRecyclableInstance(authorizer, customizeAuthorizer, useHookMode, special)
    processRecycler(authorizer, recycler, action)
}

private fun processRecycler(
    authorizer: Authorizer,
    recycler: Recyclable<out UserService>?,
    action: (UserService) -> Unit
) {
    try {
        if (recycler != null) {
            Timber.tag(TAG).d("Processing $authorizer with recycler: ${recycler.entity::class.java.simpleName}")
            recycler.use { action.invoke(it.entity) }
        } else {
            Timber.tag(TAG).e("No recycler found for $authorizer. Falling back to DefaultUserService.")
            action.invoke(DefaultUserService)
        }
    } catch (e: Exception) {
        if (e is InvocationTargetException) {
            val target = e.targetException
            if (authorizer == Authorizer.Shizuku &&
                target is IllegalStateException &&
                target.message?.contains("binder haven't been received") == true
            ) {
                throw ShizukuNotWorkException("Shizuku service connection lost during privileged action (Reflected).", target)
            }
            throw e
        }

        if (e is IllegalStateException) {
            if (authorizer == Authorizer.Shizuku && e.message?.contains("binder haven't been received") == true) {
                throw ShizukuNotWorkException("Shizuku service connection lost during privileged action.", e)
            }
        }

        throw e
    }
}

private fun getRecyclableInstance(
    authorizer: Authorizer,
    customizeAuthorizer: String,
    useHookMode: Boolean,
    special: (() -> String?)?
): Recyclable<out UserService>? {
    val specialShell = special?.invoke()

    // Retrieve the active Koin instance
    val koin = GlobalContext.get()

    return when (authorizer) {
        Authorizer.None -> null

        Authorizer.Root -> {
            val targetShell = specialShell ?: SHELL_ROOT

            if (useHookMode) {
                Timber.tag(TAG).d("Using ProcessHookRecycler with shell: $targetShell")
                koin.get<ProcessHookRecycler> { parametersOf(targetShell) }.make()
            } else {
                Timber.tag(TAG).d("Using ProcessUserServiceRecycler with shell: $targetShell")
                koin.get<RecyclerManager<String, ProcessUserServiceRecycler>>(RecyclerNames.USER_SERVICE).get(targetShell).make()
            }
        }

        Authorizer.Shizuku -> {
            if (useHookMode) {
                Timber.tag(TAG).i("Using Shizuku Hook Mode.")
                koin.get<ShizukuHookRecycler>().make()
            } else {
                Timber.tag(TAG).i("Using Shizuku UserService Mode (Default).")
                koin.get<ShizukuUserServiceRecycler>().make()
            }
        }

        Authorizer.Dhizuku -> koin.get<DhizukuUserServiceRecycler>().make()

        Authorizer.Customize -> {
            val targetShell = customizeAuthorizer.ifBlank { SHELL_ROOT }
            Timber.tag(TAG).d("Using ProcessUserServiceRecycler with shell: $targetShell")
            koin.get<RecyclerManager<String, ProcessUserServiceRecycler>>(RecyclerNames.USER_SERVICE).get(targetShell).make()
        }

        else -> specialShell?.let {
            Timber.tag(TAG).d("Using ProcessUserServiceRecycler with shell: $it")
            koin.get<RecyclerManager<String, ProcessUserServiceRecycler>>(RecyclerNames.USER_SERVICE).get(it).make()
        }
    }
}
