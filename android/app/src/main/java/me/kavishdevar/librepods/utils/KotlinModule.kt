package me.kavishdevar.librepods.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam

private const val TAG = "LibrePodsHook"

@SuppressLint("DiscouragedApi", "PrivateApi")
class KotlinModule(base: XposedInterface, param: ModuleLoadedParam): XposedModule(base, param) {
    init {
        log(Log.INFO, TAG, "module initialized at :: ${param.processName}")
        log(Log.INFO, TAG, "framework: $frameworkName($frameworkVersionCode) API ${XposedInterface.API}")
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    override fun onPackageLoaded(param: PackageLoadedParam) {
        log(Log.INFO, TAG, "onPackageLoaded :: ${param.packageName}")

        if (param.packageName == "com.google.android.bluetooth" || param.packageName == "com.android.bluetooth") {
            log(Log.INFO, TAG, "Bluetooth app detected, hooking l2c_fcr_chk_chan_modes")
            try {
                if (param.isFirstPackage) {
                    val abi = android.os.Build.SUPPORTED_ABIS.first()
                    val soName = "libl2c_fcr_hook.so"

                    val candidates = buildList {
                        add("${applicationInfo.sourceDir}!/lib/$abi/$soName")

                        applicationInfo.splitSourceDirs?.forEach { split ->
                            add("$split!/lib/$abi/$soName")
                        }
                    }

                    var loaded = false

                    for (path in candidates) {
                        try {
                            log(Log.INFO, TAG, "Trying to load native lib from $path")
                            System.load(path)
                            log(Log.INFO, TAG, "Loaded native lib from $path")
                            loaded = true
                            break
                        } catch (e: Throwable) {
                            log(Log.WARN, TAG, "Failed to load from $path: ${e.message}")
                        }
                    }

                    if (!loaded) {
                        log(Log.ERROR, TAG, "Could not load $soName from base or splits")
                        return
                    }

                    val remotePrefValue = getRemotePreferences("me.kavishdevar.librepods").getBoolean("vendor_id_hook", false)
                    log(Log.INFO, TAG, "sdp hook enabled (remote pref): $remotePrefValue")
                    NativeBridge.setSdpHook(remotePrefValue)
                    log(Log.INFO, TAG, "Native library loaded successfully")
                }
            } catch (e: Exception) {
                log(Log.ERROR, TAG, "Failed to load native library: ${e.message}")
            }
        }

        if (param.packageName == "com.google.android.settings") {
            hookSettingsController(param, "com.google.android.settings.bluetooth.AdvancedBluetoothDetailsHeaderController")
        }

        if (param.packageName == "com.android.settings") {
            hookSettingsController(param, "com.android.settings.bluetooth.AdvancedBluetoothDetailsHeaderController")
        }
    }

    private fun hookSettingsController(param: PackageLoadedParam, className: String) {
        log(Log.INFO, TAG, "Settings app detected, hooking Bluetooth icon handling")
        try {
            val headerControllerClass = Class.forName(className, false, param.defaultClassLoader)
            val updateIconMethod = headerControllerClass.getDeclaredMethod(
                "updateIcon",
                ImageView::class.java,
                String::class.java
            )

            hook(updateIconMethod, BluetoothIconHooker::class.java)

            log(Log.INFO, TAG, "Successfully hooked updateIcon method in Bluetooth settings")
        } catch (e: Exception) {
            log(Log.ERROR, TAG, "Failed to hook Bluetooth icon handler: ${e.message}")
        }
    }

    private fun log(priority: Int, tag: String, message: String) {
        super.log(priority, tag, message, null)
    }
}


/** API 100 calls a static before method for each invocation of updateIcon. */
class BluetoothIconHooker : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        fun before(callback: XposedInterface.BeforeHookCallback) {
            try {
                val imageView = callback.args[0] as? ImageView ?: return
                val iconUri = callback.args[1] as? String ?: return
                val uri = iconUri.toUri()
                if (!uri.toString().startsWith("android.resource://me.kavishdevar.librepods")) return

                Handler(Looper.getMainLooper()).post {
                    try {
                        val packageName = uri.authority ?: return@post
                        val packageContext = imageView.context.createPackageContext(
                            packageName, Context.CONTEXT_IGNORE_SECURITY
                        )
                        val resPath = uri.pathSegments
                        if (resPath.size >= 2 && resPath[0] == "drawable") {
                            val resourceName = resPath[1]
                            val resourceId = packageContext.resources.getIdentifier(
                                resourceName, "drawable", packageName
                            )
                            if (resourceId != 0) {
                                imageView.setImageDrawable(
                                    packageContext.resources.getDrawable(resourceId, packageContext.theme)
                                )
                                imageView.alpha = 1.0f
                                Log.i(TAG, "Loaded icon resource: $resourceName")
                            } else {
                                Log.e(TAG, "Resource not found: $resourceName")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading icon resource: ${e.message}", e)
                    }
                }
                callback.returnAndSkip(null)
            } catch (e: Exception) {
                Log.e(TAG, "Error in Bluetooth icon hook: ${e.message}", e)
            }
        }

        @JvmStatic
        @Suppress("UNUSED_PARAMETER")
        fun after(callback: XposedInterface.AfterHookCallback) = Unit
    }
}

object NativeBridge {
    external fun setSdpHook(enabled: Boolean)
}
