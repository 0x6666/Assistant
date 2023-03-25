package cc.ysong.assistant.nas

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.util.Log
import cc.ysong.assistant.utils.Utils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.atomic.AtomicBoolean

data class NasInstalledAppInfo(val appName: String,
                               val pkgName: String,
                               val verName: String,
                               val verCode: Int,
                               val appIcon: Drawable
)

data class NasAppApkUrl(val name: String, val url: String)

class NasAppInfo(var name: String, var ver: String, var apkListUrl: String, var progress: Int) {
    var apkUrls: List<NasAppApkUrl>? = null
}

object NasAppMgr {

    private var nasAppNameAll = mutableListOf<String>()
    private var nasAppInfoMap = mutableMapOf<String, NasAppInfo>()

    private var installedPackage = mutableMapOf<String, NasInstalledAppInfo>()


    private var listener: UpdateListener? = null
    private const val topLevel = "https://archive.synology.com"
    private val nasAppLoading = AtomicBoolean(false)

    fun onlineAppCount(): Int {
        return nasAppNameAll.size;
    }

    fun getNasApp(idx: Int): NasAppInfo? {
        return if (idx >= nasAppNameAll.size) {
            null
        } else {
            nasAppInfoMap[nasAppNameAll[idx]]
        }
    }

    fun isNasAppListLoading(): Boolean {
        return nasAppLoading.get()
    }

    fun loadNasAppList() {
        nasAppLoading.set(true)
        listener?.onLoadingNasAppList(nasAppLoading.get())
        Utils.executor.execute {
            val doc: Document = Jsoup.connect("$topLevel/download/Mobile").get()
            val appHtml = doc.select("table > tbody > tr > th > a")

            for (e in appHtml) {
                val name = e.text()
                if (getInstalledApp(name) == null) {
                    continue
                }

                addNasApp(e, name)
            }

            for (e in appHtml) {
                val name = e.text()
                if (getInstalledApp(name) != null) {
                    continue
                }

                addNasApp(e, name)
            }

            nasAppLoading.set(false)
            listener?.onLoadingNasAppList(nasAppLoading.get())
        }
    }

    fun getAppApkUrls(pos: Int, onLoad: (List<NasAppApkUrl>?) -> Unit) {
        val info = getNasApp(pos)
        if (info == null) {
            onLoad(null)
            return
        }

        if (info.apkUrls != null) {
            onLoad(info.apkUrls)
            return
        }

        listener?.onLoadingNasAppApkUrls(true)

        Utils.executor.execute {
            val doc: Document = Jsoup.connect(info.apkListUrl).get()
            val appHtml = doc.select("table > tbody > tr > th > a")

            val tmpList = mutableListOf<NasAppApkUrl>()
            for (e in appHtml) {
                tmpList.add(NasAppApkUrl(e.text(), e.attr("href")))
            }
            info.apkUrls = tmpList
            onLoad(info.apkUrls)

            listener?.onLoadingNasAppApkUrls(false)
        }
    }


    private fun addNasApp(e: Element, name: String) {
        val href = e.attr("href")

        val eDoc: Document = Jsoup.connect(topLevel + href).get()
        val lastVer = eDoc.select("table > tbody > tr:nth-child(2) > th > a")
        val downUrl = lastVer.attr("href")
        val version = lastVer.text()
        val url = topLevel + downUrl
        Log.i("data", "version: $version downUrl: $url")

        if (nasAppInfoMap.containsKey(name)) {
            nasAppInfoMap[name]?.ver = version
            nasAppInfoMap[name]?.apkListUrl = url
        } else {
            nasAppInfoMap[name] = NasAppInfo(name, version, url, 0)
            nasAppNameAll.add(name)
        }
        listener?.onUpdate()
    }

    private fun updateInstallApps(installedPkg: List<NasInstalledAppInfo>) {
        for (x in installedPkg) {
            installedPackage[x.pkgName] = x
        }
    }

    fun getInstalledApp(name: String): NasInstalledAppInfo? {

        var pkgName = ""
        when (name) {
            "Android-Drive" -> {
                pkgName = "com.synology.dsdrive"
            }
            "Android-DSmail" -> {
                pkgName = "com.synology.dsmail.china"
            }
            "Android-ActiveInsight" -> {
                pkgName = "com.synology.activeinsight.china"
            }
            "Android-DSfinder" -> {
                pkgName = "com.synology.DSfinder"
            }
            "Android-DSdownload" -> {
                pkgName = "com.synology.DSdownload"
            }
            "Android-Photos" -> {
                pkgName = "com.synology.projectkailash.cn"
            }
            "Android-DSfile" -> {
                pkgName = "com.synology.DSfile"
            }
        }

        if (pkgName == "") {
            Log.w("NasAppMgr", "$name not found")
            return null
        }

        return installedPackage[pkgName]
    }

    fun updateAllInstalledNasApp() {
        Utils.executor.execute {
            val installedApps = mutableListOf<NasInstalledAppInfo>()
            val pm = Utils.context.packageManager
            val installedPkg = pm?.getInstalledPackages(0)
            if (installedPkg != null) {
                for (packageInfo in installedPkg) {
                    if ((packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                        val packageName = packageInfo.packageName //获取应用包名，可用于卸载和启动应用
                        if (packageName.startsWith("com.synology")) {
                            val appName = packageInfo.applicationInfo.loadLabel(pm) // appname
                            val versionName = packageInfo.versionName //获取应用版本名
                            val versionCode = packageInfo.versionCode //获取应用版本号
                            val appIcon = packageInfo.applicationInfo.loadIcon(pm) //获取应用图标

                            installedApps.add(
                                NasInstalledAppInfo(
                                    appName.toString(),
                                    packageName,
                                    versionName,
                                    versionCode,
                                    appIcon
                                )
                            )
                        }
                    }
                }
            }
            updateInstallApps(installedApps)
        }
    }

    fun setUpdateListener(ul: UpdateListener?) {
        listener = ul
    }

    interface UpdateListener {
        fun onUpdate()
        fun onLoadingNasAppList(loading: Boolean)
        fun onLoadingNasAppApkUrls(loading: Boolean)
    }
}
