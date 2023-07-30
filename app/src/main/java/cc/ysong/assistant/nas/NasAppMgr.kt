package cc.ysong.assistant.nas

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import cc.ysong.assistant.utils.Http
import cc.ysong.assistant.utils.Utils
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

data class NasInstalledAppInfo(val appName: String,
                               val pkgName: String,
                               val verName: String,
                               val verCode: Int,
                               val appIcon: Drawable
)

class NasAppInfo(var name: String, var ver: String, var progress: Int) {
    var pkgName: String? = null
    var apkUrl: String? = null

    fun iconPath() : String{
        return File(Utils.getFilesDir(), "$name.png").absolutePath
    }

    fun iconExist(): Boolean {
        return File(Utils.getFilesDir(), "$name.png").exists()
    }
}

object NasAppMgr {

    private var nasAppNameAll = mutableListOf<String>()
    private var nasAppInfoMap = mutableMapOf<String, NasAppInfo>()

    private var installedPackage = mutableMapOf<String, NasInstalledAppInfo>()
    private var svrRoot = "https://assistant.nas.ysong.cc:5001/syno/api"

    private var listener: UpdateListener? = null
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
            Utils.http.get("$svrRoot/apps", object: Http.HttpListener{
                override fun onSuccess(data: String) {

                    try {
                        val obj = JSONObject(data)
                        val datas = obj.getJSONArray("data")

                        for (i in 0 until datas.length()) {
                            val item = datas.getJSONObject(i)

                            val name = item.getString("Name")
                            val pkgName = item.getString("PkgName")
                            val version = item.getString("Version")

                            if (getInstalledApp(name, pkgName) == null) {
                                continue
                            }

                            addNasApp(name, pkgName, version)
                        }

                        for (i in 0 until datas.length()) {
                            val item = datas.getJSONObject(i)

                            val name = item.getString("Name")
                            val pkgName = item.getString("PkgName")
                            val version = item.getString("Version")
                            if (getInstalledApp(name, pkgName) != null) {
                                continue
                            }

                            addNasApp(name, pkgName, version)
                        }
                    } catch (e: Throwable) {
                        Utils.context.mainExecutor.execute {
                            Toast.makeText(Utils.context, "parse apps fail: $e", Toast.LENGTH_SHORT).show();
                        }
                    } finally {
                        nasAppLoading.set(false)
                        listener?.onLoadingNasAppList(nasAppLoading.get())
                    }
                }

                override fun onDownloadFailed() {
                    Toast.makeText(Utils.context, "get apps fail", Toast.LENGTH_SHORT).show();
                }
            })
        }
    }

    fun getAppApkUrls(pos: Int, onLoad: (String?) -> Unit) {
        val info = getNasApp(pos)
        if (info == null) {
            onLoad(null)
            return
        }

        if (info.apkUrl != null) {
            onLoad(info.apkUrl)
            return
        }
    }

    private fun addNasApp(name: String, pkgName: String, version: String) {
        val url = "$svrRoot/app/$name"
        Log.i("data", "version: $version downUrl: $url")

        val info: NasAppInfo?
        if (nasAppInfoMap.containsKey(name)) {
            info = nasAppInfoMap[name]
        } else {
            info = NasAppInfo(name, version, 0)
            nasAppInfoMap[name] = info
            nasAppNameAll.add(name)
        }

        info?.ver = version
        info?.apkUrl = url
        info?.pkgName = pkgName
        listener?.onUpdate()

        downIcon(name)
    }

    private fun updateInstallApps(installedPkg: List<NasInstalledAppInfo>) {
        for (x in installedPkg) {
            installedPackage[x.pkgName] = x
        }
    }

    fun getInstalledApp(name: String, pkgName_: String?): NasInstalledAppInfo? {

        var pkgName = pkgName_
        if (pkgName.isNullOrEmpty()) {
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
        }


        if (pkgName.isNullOrEmpty()) {
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

    fun downIcon(appName: String) {
        val f = File(Utils.getFilesDir(), "$appName.png")
        if (f.exists()) {
            return
        }

        val url = "$svrRoot/app/$appName?icon=true"

        Utils.http.download(url, Utils.getFilesDir(), "$appName.png", object: Http.OnDownloadListener {
            override fun onDownloadSuccess(path: String) {

            }

            override fun onDownloading(progress: Int) {
            }

            override fun onDownloadFailed(e: Exception) {
                Log.e("NasAppmgr", "down icon fail", e)

            }
        })
    }

    interface UpdateListener {
        fun onUpdate()
        fun onLoadingNasAppList(loading: Boolean)
        fun onLoadingNasAppApkUrls(loading: Boolean)
    }
}
