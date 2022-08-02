package cc.ysong.assistant.nas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import cc.ysong.assistant.R


class NasAppListAdapter(context: Context) :
    ArrayAdapter<NasAppInfo>(context, 0) {

    companion object {
        private var installedPackage = mutableMapOf<String, NasInstalledAppInfo>()

        fun updateInstallApps(installedPkg: List<NasInstalledAppInfo>) {
            for (x in installedPkg) {
                installedPackage[x.pkgName] = x
            }
        }

        fun getInstalledApp(name: String) : NasInstalledAppInfo? {

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
                " Android-DSdownload" -> {
                    pkgName = "com.synology.DSdownload"
                }
                "Android-Photos" -> {
                    pkgName = "com.synology.projectkailash.cn"
                }
            }

            if (pkgName == "") {
                return null
            }

            return installedPackage[pkgName]
        }
    }

    override fun getView(position: Int, convertView_: View?, parent: ViewGroup): View {

        var convertView = convertView_
        val appInfo: NasAppInfo? = getItem(position)

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.app_list_item, parent, false)
        }

        val appName = convertView!!.findViewById<TextView>(R.id.nas_app_name)
//        val appUrl = convertView.findViewById<TextView>(R.id.nas_app_url)
        val appLastVer = convertView.findViewById<TextView>(R.id.nas_app_last_version)
        val appIcon = convertView.findViewById<ImageView>(R.id.nas_app_icon)
        val appInstallVer = convertView.findViewById<TextView>(R.id.nas_app_installed_version)

        if (appInfo != null) {
            val installedInfo = getInstalledApp(appInfo.name)
            if (installedInfo != null) {
                appIcon.setImageDrawable(installedInfo.appIcon)
                (installedInfo.verName + "-" + installedInfo.verCode.toString()).also { appInstallVer.text = it }
            } else {
                appIcon.setImageResource(R.mipmap.ic_launcher)
                appInstallVer.text = "not installed"
            }
            appName.text = appInfo.name
            appLastVer.text = appInfo.ver
//            appUrl.text = appInfo.url
        }

        return convertView
    }
}