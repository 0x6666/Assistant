package cc.ysong.assistant.nas

import android.graphics.drawable.Drawable

data class NasInstalledAppInfo(val appName: String,
                               val pkgName: String,
                               val verName: String,
                               val verCode: Int,
                               val appIcon: Drawable)
