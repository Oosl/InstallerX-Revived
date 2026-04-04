// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2025-2026 InstallerX Revived contributors
package com.rosan.installer.data.device.provider

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.rosan.installer.core.env.DeviceConfig
import com.rosan.installer.core.reflection.ReflectionProvider
import com.rosan.installer.core.reflection.invokeStatic
import com.rosan.installer.domain.device.model.Manufacturer
import com.rosan.installer.domain.device.model.ShizukuMode
import com.rosan.installer.domain.device.provider.DeviceCapabilityProvider
import com.rosan.installer.util.hasFlag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku
import timber.log.Timber

class DeviceCapabilityProviderImpl(
    private val context: Context,
    private val reflect: ReflectionProvider
) : DeviceCapabilityProvider {
    companion object {
        private const val MIUI_PACKAGE_INSTALLER = "com.miui.packageinstaller"
        private const val MIN_SUPPORTED_MIUI_VERSION_CODE = 54100L

        private const val KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name"
        private const val KEY_MI_OS_VERSION_NAME = "ro.mi.os.version.name"
        private const val KEY_OPLUS_API = "ro.build.version.oplus.api"
        private const val KEY_OPLUS_SUB_API = "ro.build.version.oplus.sub_api"
    }

    private val systemPropertiesClass by lazy { @SuppressLint("PrivateApi") Class.forName("android.os.SystemProperties") }

    override val isSessionInstallSupported: Boolean by lazy {
        calculateSessionInstallSupport() || isSystemApp
    }

    override val hasMiPackageInstaller: Boolean by lazy {
        getMiuiPackageInstallerVersion() != null
    }

    override val isSystemApp: Boolean by lazy {
        try {
            context.applicationInfo.flags.hasFlag(ApplicationInfo.FLAG_SYSTEM)
        } catch (_: Exception) {
            false
        }
    }

    override val isHyperOS: Boolean by lazy {
        val osName = getSystemProperty(KEY_MI_OS_VERSION_NAME)
        !osName.isNullOrEmpty() && osName.startsWith("OS")
    }

    override val isMIUI: Boolean by lazy {
        val miuiName = getSystemProperty(KEY_MIUI_VERSION_NAME)
        !miuiName.isNullOrEmpty() && !isHyperOS
    }

    override val isSupportMiIsland: Boolean by lazy {
        try {
            val focusProtocolVersion = Settings.System.getInt(
                context.contentResolver,
                "notification_focus_protocol",
                0
            )
            focusProtocolVersion == 3
        } catch (_: Exception) {
            false
        }
    }

    override val oplusOSdkVersion: String? by lazy {
        val api = getSystemProperty(KEY_OPLUS_API)
        if (api.isNullOrEmpty()) return@lazy null

        val subApi = getSystemProperty(KEY_OPLUS_SUB_API)
        if (!subApi.isNullOrEmpty()) "$api.$subApi" else api
    }

    // --- Shizuku Dynamic Flow ---
    private val _shizukuModeFlow = MutableStateFlow(ShizukuMode.NONE)
    override val shizukuModeFlow: StateFlow<ShizukuMode> = _shizukuModeFlow.asStateFlow()

    // --- Root Static State ---
    // override var rootMode: RootMode = RootMode.None

    init {
        // Register Shizuku listeners to drive state automatically
        Shizuku.addBinderReceivedListenerSticky {
            updateShizukuModeInternal()
        }

        Shizuku.addBinderDeadListener {
            _shizukuModeFlow.value = ShizukuMode.NONE
            Timber.d("Shizuku binder is dead. Status updated to NONE.")
        }
    }

    private fun updateShizukuModeInternal() {
        try {
            if (Shizuku.pingBinder()) {
                val mode = ShizukuMode.fromUid(Shizuku.getUid())
                _shizukuModeFlow.value = mode
                Timber.d("Shizuku binder received. Status updated to: $mode")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse Shizuku UID.")
            _shizukuModeFlow.value = ShizukuMode.NONE
        }
    }

    /*    override suspend fun refreshPrivilegeStatus() {
            // 1. Manually trigger a fallback refresh for Shizuku
            // Already handled in init { }
            // updateShizukuModeInternal()

            // 2. Fetch the Root status once and assign it to the static variable
            rootMode = detectRootMode()
        }*/

    /* private suspend fun detectRootMode(): RootMode = withContext(Dispatchers.IO) {
         if (checkBinaryViaSu("ksud -V")) return@withContext RootMode.KernelSU
         if (checkBinaryViaSu("magisk -v")) return@withContext RootMode.Magisk
         if (checkBinaryViaSu("apd -v")) return@withContext RootMode.APatch

         RootMode.None
     }*/

    /*  private fun checkBinaryViaSu(command: String): Boolean {
          return try {
              // Execute the specific binary check within the su environment
              val process = ProcessBuilder("su", "-c", command)
                  .redirectErrorStream(true)
                  .start()

              val exitCode = process.waitFor()
              if (exitCode == 0) {
                  Timber.d("RootDetection: Successfully executed -> su -c '$command'")
                  true
              } else {
                  Timber.d("RootDetection: Command 'su -c \'$command\'' failed with exit code: $exitCode")
                  false
              }
          } catch (e: CancellationException) {
              // Rethrow to maintain structured concurrency
              throw e
          } catch (e: Exception) {
              // Catch IOExceptions or other runtime errors
              Timber.d("RootDetection: Execution failed for 'su -c '$command\': ${e.message}")
              false
          }
      }*/

    private fun calculateSessionInstallSupport(): Boolean {
        val isMi = DeviceConfig.currentManufacturer == Manufacturer.XIAOMI
        if (!isMi) return true

        val miPackageManagerVersion = getMiuiPackageInstallerVersion()
        return if (miPackageManagerVersion != null) {
            miPackageManagerVersion.second >= MIN_SUPPORTED_MIUI_VERSION_CODE
        } else true
    }

    private fun getMiuiPackageInstallerVersion(): Pair<String, Long>? =
        try {
            val info = context.packageManager.getPackageInfo(MIUI_PACKAGE_INSTALLER, 0)
            val versionName = info.versionName ?: ""
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION") info.versionCode.toLong()
            }
            versionName to versionCode
        } catch (_: PackageManager.NameNotFoundException) {
            null
        } catch (e: Throwable) {
            Timber.e(e, "Failed to retrieve MIUI package installer version")
            null
        }

    private fun getSystemProperty(key: String): String? =
        reflect.invokeStatic<String>(
            "get",
            systemPropertiesClass,
            arrayOf(String::class.java, String::class.java),
            key, ""
        )?.takeIf { it.isNotEmpty() }
}
