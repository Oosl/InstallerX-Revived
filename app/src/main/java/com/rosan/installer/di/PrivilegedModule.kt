// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2025-2026 InstallerX Revived contributors
package com.rosan.installer.di

import com.rosan.installer.data.privileged.provider.AppOpsProviderImpl
import com.rosan.installer.data.privileged.provider.ComponentOpsProviderImpl
import com.rosan.installer.data.privileged.provider.PermissionProviderImpl
import com.rosan.installer.data.privileged.provider.PostInstallTaskProviderImpl
import com.rosan.installer.data.privileged.provider.ShellExecutionProviderImpl
import com.rosan.installer.data.privileged.provider.SystemInfoProviderImpl
import com.rosan.installer.data.privileged.repository.recyclable.RecyclerManager
import com.rosan.installer.data.privileged.repository.recycler.AppProcessRecycler
import com.rosan.installer.data.privileged.repository.recycler.DhizukuUserServiceRecycler
import com.rosan.installer.data.privileged.repository.recycler.ProcessHookRecycler
import com.rosan.installer.data.privileged.repository.recycler.ProcessUserServiceRecycler
import com.rosan.installer.data.privileged.repository.recycler.ShizukuHookRecycler
import com.rosan.installer.data.privileged.repository.recycler.ShizukuUserServiceRecycler
import com.rosan.installer.data.privileged.service.AutoLockService
import com.rosan.installer.domain.privileged.provider.AppOpsProvider
import com.rosan.installer.domain.privileged.provider.ComponentOpsProvider
import com.rosan.installer.domain.privileged.provider.PermissionProvider
import com.rosan.installer.domain.privileged.provider.PostInstallTaskProvider
import com.rosan.installer.domain.privileged.provider.ShellExecutionProvider
import com.rosan.installer.domain.privileged.provider.SystemInfoProvider
import com.rosan.installer.domain.privileged.usecase.GetAvailableUsersUseCase
import com.rosan.installer.domain.privileged.usecase.OpenAppUseCase
import com.rosan.installer.domain.privileged.usecase.OpenLSPosedUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object RecyclerNames {
    val APP_PROCESS = named("AppProcessManager")
    val USER_SERVICE = named("ProcessUserServiceManager")
}

val privilegedModule = module {
    // Providers
    singleOf(::AppOpsProviderImpl) { bind<AppOpsProvider>() }
    singleOf(::ComponentOpsProviderImpl) { bind<ComponentOpsProvider>() }
    singleOf(::PermissionProviderImpl) { bind<PermissionProvider>() }
    singleOf(::ShellExecutionProviderImpl) { bind<ShellExecutionProvider>() }
    singleOf(::PostInstallTaskProviderImpl) { bind<PostInstallTaskProvider>() }
    singleOf(::SystemInfoProviderImpl) { bind<SystemInfoProvider>() }

    // Services
    singleOf(::AutoLockService)

    // UseCases
    factoryOf(::OpenAppUseCase)
    factoryOf(::OpenLSPosedUseCase)
    factoryOf(::GetAvailableUsersUseCase)

    // Recycler Infrastructure

    // 1. Recycler Managers (Singletons)
    // Add named qualifier to distinguish this manager
    single(RecyclerNames.APP_PROCESS) {
        RecyclerManager<String, AppProcessRecycler> { shell ->
            AppProcessRecycler(shell)
        }
    }

    // Add named qualifier to distinguish this manager
    single(RecyclerNames.USER_SERVICE) {
        RecyclerManager<String, ProcessUserServiceRecycler> { shell ->
            ProcessUserServiceRecycler(
                shell = shell,
                context = get(),
                appProcessRecyclerManager = get(RecyclerNames.APP_PROCESS)
            )
        }
    }

    // 2. Stateless / Permission-based Recyclers (Singletons)
    // Replaces the old 'object' declarations. Koin now manages their lifecycle.
    singleOf(::ShizukuUserServiceRecycler)
    singleOf(::DhizukuUserServiceRecycler)
    singleOf(::ShizukuHookRecycler)

    // 3. Shell-dependent Recyclers (Factories)
    // Created dynamically on demand based on the requested shell.
    factory { (shell: String) ->
        ProcessHookRecycler(
            shell = shell,
            context = get(),
            appProcessRecyclerManager = get(RecyclerNames.APP_PROCESS)
        )
    }
}
