package com.js.floatinglogger

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class FloatingLogger(private val activity: AppCompatActivity) {

    private val tag: String = javaClass.simpleName

    @RequiresApi(Build.VERSION_CODES.M)
    private val launcher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(activity)) {
                startFloatingLoggerService()
            } else {
                Toast.makeText(
                    activity,
                    "Overlay Permission Not Granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun init() {
        overlayPermissionCheck()
    }

    private fun overlayPermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(activity)) {
                startFloatingLoggerService()
            } else {
                Toast.makeText(activity, "Overlay Permission Needed", Toast.LENGTH_SHORT).show()
                Log.e(tag, "package=${activity.packageName}")
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                ).apply {
                    launcher.launch(this)
                }
            }
        } else {
            startFloatingLoggerService()
        }
    }

    private fun startFloatingLoggerService() {
        Log.e(tag, "startFloatingLoggerService() Called")

        Intent(activity, FloatingLoggerService::class.java).also {
            activity.startService(it)
        }
    }

    private fun stopFloatingLoggerService() {
        Log.e(tag, "stopFloatingLoggerService() Called")

        if (!isServiceRunningOnBackground()) {
            Intent(activity, FloatingLoggerService::class.java).also {
                activity.stopService(it)
            }
        }
    }

    private fun isServiceRunningOnBackground(): Boolean {
        Log.e(tag, "isServiceORunningOnBackground()")

        val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val runningProcesses: List<ActivityManager.RunningAppProcessInfo> =
            manager.runningAppProcesses

        for (processInfo in runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (activeProcess in processInfo.pkgList) {
                    Log.e(tag, "packageName : $activeProcess")
                    if (activeProcess == activity.packageName) {
                        return false
                    }
                }
            }
        }

        return true
    }
}