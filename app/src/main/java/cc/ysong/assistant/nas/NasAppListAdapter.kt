package cc.ysong.assistant.nas

import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import cc.ysong.assistant.R
import cc.ysong.assistant.utils.Utils
import java.io.File


class NasAppViewHolder {
    var appName: TextView? = null
    var appLastVer: TextView? = null
    var appIcon: ImageView? = null
    var appInstallVer: TextView? = null
    var progressBar: ProgressBar? = null
}

class NasAppListAdapter : BaseAdapter() {

    override fun getCount(): Int {
        return NasAppMgr.onlineAppCount()
    }

    override fun getItem(idx: Int): NasAppInfo? {
        return NasAppMgr.getNasApp(idx)!!
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView_: View?, parent: ViewGroup): View {

        var convertView = convertView_
        val appInfo: NasAppInfo? = getItem(position)

        var holder_: NasAppViewHolder?
        if (convertView == null) {
            convertView = LayoutInflater.from(Utils.context).inflate(R.layout.app_list_item, parent, false)
            holder_ = NasAppViewHolder()
            holder_.appName = convertView!!.findViewById(R.id.nas_app_name)
            holder_.appLastVer = convertView.findViewById(R.id.nas_app_last_version)
            holder_.appIcon = convertView.findViewById(R.id.nas_app_icon)
            holder_.appInstallVer = convertView.findViewById(R.id.nas_app_installed_version)
            holder_.progressBar = convertView.findViewById(R.id.nas_app_down_progress)
            convertView.tag = holder_
        } else {
            holder_ = convertView.tag as NasAppViewHolder?
        }
        val holder = holder_!!

        if (appInfo != null) {
            val installedInfo = NasAppMgr.getInstalledApp(appInfo.name, appInfo.pkgName)
            if (installedInfo != null) {
                holder.appIcon?.setImageDrawable(installedInfo.appIcon)

                if (holder.appInstallVer != null) {
                    val installedVer = String.format("%s-%03d", installedInfo.verName, installedInfo.verCode)
                    holder.appInstallVer?.text = installedVer

                    if (installedVer < appInfo.ver) {
                        holder.appLastVer!!.setTextColor(Color.parseColor("#ff5e9cff"))
                    }
                }
            } else {
                if (appInfo.iconExist()) {
                    try {
                        val uri = Uri.fromFile(File(appInfo.iconPath()))
                        holder.appIcon?.setImageURI(uri)
                    } catch (t:Throwable) {
                        Log.e("NasAppListAdapter", "set icon fail", t)
                        holder.appIcon?.setImageResource(R.mipmap.ic_launcher)
                    }
                } else {
                    holder.appIcon?.setImageResource(R.mipmap.ic_launcher)
                }
                holder.appInstallVer?.text = "not installed"
            }
            holder.progressBar?.progress = appInfo.progress
            if (appInfo.progress > 0) {
                holder.progressBar?.visibility = View.VISIBLE
            } else {
                holder.progressBar?.visibility = View.GONE
            }
            holder.appName?.text = appInfo.name
            holder.appLastVer?.text = appInfo.ver
        }

        return convertView
    }
}