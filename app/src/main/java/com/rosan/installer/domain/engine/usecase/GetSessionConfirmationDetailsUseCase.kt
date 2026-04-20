// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2025-2026 InstallerX Revived contributors
package com.rosan.installer.domain.engine.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.rosan.installer.data.privileged.util.useUserService
import com.rosan.installer.domain.device.provider.DeviceCapabilityProvider
import com.rosan.installer.domain.session.model.ConfirmationDetails
import com.rosan.installer.domain.settings.model.ConfigModel
import timber.log.Timber

/**
 * UseCase to retrieve details (label and icon) for a specific installation session.
 * Utilizes IPC services depending on the application's current authorization level.
 */
class GetSessionConfirmationDetailsUseCase(
    private val capabilityProvider: DeviceCapabilityProvider
) {
    /**
     * Retrieves session details based on the provided configuration.
     */
    operator fun invoke(
        sessionId: Int,
        config: ConfigModel,
        isSelfSession: Boolean = false,
        currentProgress: Int = 1,
        totalProgress: Int = 1
    ): ConfirmationDetails {
        var label: CharSequence? = null
        var icon: Bitmap? = null
        var packageName = ""
        var isUpdate = false
        var isOwnershipConflict = false
        var sourceAppLabel: CharSequence? = null

        Timber.d("Getting session details via service (Authorizer: ${config.authorizer})")

        var bundle: Bundle? = null
        try {
            useUserService(
                isSystemApp = capabilityProvider.isSystemApp,
                authorizer = config.authorizer,
                customizeAuthorizer = config.customizeAuthorizer,
                useHookMode = false
            ) { bundle = it.privileged.getSessionDetails(sessionId) }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get session details via ${config.authorizer}")
        }

        if (bundle != null) {
            label = bundle.getCharSequence("appLabel") ?: "N/A"
            packageName = bundle.getString("packageName", "")
            isUpdate = bundle.getBoolean("isUpdate", false)
            isOwnershipConflict = bundle.getBoolean("isOwnershipConflict", false)
            sourceAppLabel = bundle.getCharSequence("sourceAppLabel")

            val bytes = bundle.getByteArray("appIcon")
            if (bytes != null) {
                try {
                    icon = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to decode icon bitmap")
                }
            }
        } else {
            Timber.w("Service returned null bundle for session $sessionId")
        }

        return ConfirmationDetails(
            sessionId = sessionId,
            appLabel = label ?: "N/A",
            appIcon = icon,
            packageName = packageName,
            isUpdate = isUpdate,
            isOwnershipConflict = isOwnershipConflict,
            sourceAppLabel = sourceAppLabel,
            isSelfSession = isSelfSession,
            currentProgress = currentProgress,
            totalProgress = totalProgress
        )
    }
}
