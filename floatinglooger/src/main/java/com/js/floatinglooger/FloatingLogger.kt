package com.js.floatinglooger

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class FloatingLogger {

    private val tag: String = javaClass.simpleName

    fun init(context: AppCompatActivity) {
        overlayPermissionCheck(context)
    }

    private fun overlayPermissionCheck(context: AppCompatActivity) {
        if (Settings.canDrawOverlays(context)) {
            startFloatingLoggerService(context)
        } else {
            Toast.makeText(context, "Overlay Permission Needed", Toast.LENGTH_SHORT).show()
            Log.e(tag, "package=${context.packageName}")
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (Settings.canDrawOverlays(context)) {
                        startFloatingLoggerService(context)
                    } else {
                        Toast.makeText(
                            context,
                            "Overlay Permission Not Granted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.launch(this)
            }
        }
    }

    private fun startFloatingLoggerService(context: AppCompatActivity) {
        Log.e(tag, "startFloatingLoggerService() Called")

        Intent(context, FloatingLoggerService::class.java).also {
            context.startService(it)
        }
    }

    private fun stopFloatingLoggerService(context: AppCompatActivity) {
        Log.e(tag, "stopFloatingLoggerService() Called")

        if (!isServiceRunningOnBackground(context)) {
            Intent(context, FloatingLoggerService::class.java).also {
                context.stopService(it)
            }
        }
    }

    private fun isServiceRunningOnBackground(context: AppCompatActivity): Boolean {
        Log.e(tag, "isServiceORunningOnBackground()")

        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val runningProcesses: List<ActivityManager.RunningAppProcessInfo> =
            manager.runningAppProcesses

        for (processInfo in runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (activeProcess in processInfo.pkgList) {
                    Log.e(tag, "packageName : $activeProcess")
                    if (activeProcess == context.packageName) {
                        return false
                    }
                }
            }
        }

        return true
    }
}