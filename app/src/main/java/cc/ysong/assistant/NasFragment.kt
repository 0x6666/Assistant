package cc.ysong.assistant

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import cc.ysong.assistant.databinding.FragmentFirstBinding
import cc.ysong.assistant.nas.NasAppInfo
import cc.ysong.assistant.nas.NasAppListAdapter
import cc.ysong.assistant.nas.NasInstalledAppInfo
import cc.ysong.assistant.utils.Utils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class NasFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var _appListAdapter : NasAppListAdapter? = null
    private val appListAdapter get() = _appListAdapter!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        loadData()
        getAllInstalledPkg()

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _appListAdapter = activity?.let { NasAppListAdapter(it) }
        binding.appList.adapter = appListAdapter
        binding.appList.onItemClickListener = AdapterView.OnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->

        }

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
    }

    private fun updateData(info: NasAppInfo) {
        appListAdapter.add(info)
    }

    private fun updateInstalledAppInfo(installedPkg: List<NasInstalledAppInfo>) {
        NasAppListAdapter.updateInstallApps(installedPkg)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadData() {
        Utils.executor.execute {
            val topLevel = "https://archive.synology.com"
            val doc: Document = Jsoup.connect("$topLevel/download/Mobile").get()
            val appHtml = doc.select("table > tbody > tr > th > a")

            for (e in appHtml) {
                val name = e.text()
                if (NasAppListAdapter.getInstalledApp(name) == null) {
                    continue
                }

                addNasApp(e, topLevel, name)
            }

            for (e in appHtml) {
                val name = e.text()
                if (NasAppListAdapter.getInstalledApp(name) != null) {
                    continue
                }

                addNasApp(e, topLevel, name)
            }
        }
    }

    private fun addNasApp(e: Element, topLevelUrl: String, name: String) {
        val href = e.attr("href")

        Log.i("data", "top level href: ${topLevelUrl + href}  text: $name")

        val eDoc: Document = Jsoup.connect(topLevelUrl + href).get()
        val lastVer = eDoc.select("table > tbody > tr:nth-child(2) > th > a")
        val downUrl = lastVer.attr("href")
        val version = lastVer.text()
        val url = topLevelUrl + downUrl
        Log.i("data", "version: $version downUrl: $url")

        activity?.runOnUiThread {
            updateData(NasAppInfo(name, version, url))
        }
    }

    private fun getAllInstalledPkg() {
        Utils.executor.execute {
            val installedApps = mutableListOf<NasInstalledAppInfo>()
            val a = activity
            if (a != null) {
                val installedPkg = a.packageManager?.getInstalledPackages(0)
                if (installedPkg != null) {
                    for (packageInfo in installedPkg) {
                        if ((packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                            val packageName = packageInfo.packageName //获取应用包名，可用于卸载和启动应用
                            if (packageName.startsWith("com.synology")) {
                                val appName = packageInfo.applicationInfo.loadLabel(a.packageManager) // appname
                                val versionName = packageInfo.versionName //获取应用版本名
                                val versionCode = packageInfo.versionCode //获取应用版本号
                                val appIcon = packageInfo.applicationInfo.loadIcon(a.packageManager) //获取应用图标

                                installedApps.add(NasInstalledAppInfo(appName.toString(), packageName, versionName, versionCode, appIcon))

                                Log.i("xxxx", "appName: $appName, packageName: $packageName, versionName: $versionName, versionCode: $versionCode, appIcon: $appIcon")
                            }
                        }
                    }

                    activity?.runOnUiThread {
                        updateInstalledAppInfo(installedApps)
                    }
                }
            }
        }
    }
}