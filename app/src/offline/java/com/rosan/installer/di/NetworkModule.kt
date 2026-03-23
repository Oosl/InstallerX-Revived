// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2025-2026 InstallerX Revived contributors
package com.rosan.installer.di

import com.rosan.installer.data.session.resolver.OfflineNetworkResolver
import com.rosan.installer.data.updater.provider.OfflineInAppInstallProviderImpl
import com.rosan.installer.data.updater.repository.OfflineUpdateRepositoryImpl
import com.rosan.installer.domain.session.repository.NetworkResolver
import com.rosan.installer.domain.updater.provider.InAppInstallProvider
import com.rosan.installer.domain.updater.repository.UpdateRepository
import com.rosan.installer.domain.updater.usecase.PerformAppUpdateUseCase
import org.koin.dsl.module

val networkModule = module {
    single<NetworkResolver> { OfflineNetworkResolver() }
}

val updateModule = module {
    // 1. Data Layer dummy implementations for Offline
    single<UpdateRepository> { OfflineUpdateRepositoryImpl() }
    single<InAppInstallProvider> { OfflineInAppInstallProviderImpl() }

    // 2. Domain Layer UseCase (shared logic, but safely backed by offline dummy implementations)
    factory { PerformAppUpdateUseCase(get(), get()) }
}
