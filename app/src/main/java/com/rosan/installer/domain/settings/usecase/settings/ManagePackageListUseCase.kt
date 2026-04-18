// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2025-2026 InstallerX Revived contributors
package com.rosan.installer.domain.settings.usecase.settings

import com.rosan.installer.domain.settings.model.NamedPackage
import com.rosan.installer.domain.settings.repository.AppSettingsRepository
import com.rosan.installer.domain.settings.repository.NamedPackageListSetting
import kotlinx.coroutines.flow.first

class ManagePackageListUseCase(
    private val appSettingsRepo: AppSettingsRepository
) {
    suspend fun addPackage(setting: NamedPackageListSetting, pkg: NamedPackage) {
        val currentList = appSettingsRepo.getNamedPackageList(setting).first().toMutableList()
        if (!currentList.contains(pkg)) {
            currentList.add(pkg)
            appSettingsRepo.putNamedPackageList(setting, currentList)
        }
    }

    suspend fun removePackage(setting: NamedPackageListSetting, pkg: NamedPackage) {
        val currentList = appSettingsRepo.getNamedPackageList(setting).first().toMutableList()
        if (currentList.remove(pkg)) {
            appSettingsRepo.putNamedPackageList(setting, currentList)
        }
    }
}
