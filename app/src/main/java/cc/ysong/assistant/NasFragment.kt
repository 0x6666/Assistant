package cc.ysong.assistant

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
import cc.ysong.assistant.nas.NasAppMgr
import cc.ysong.assistant.utils.Http
import cc.ysong.assistant.utils.Utils
import java.io.File
import java.lang.Exception


class NasFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var _appListAdapter : NasAppListAdapter? = null
    private val appListAdapter get() = _appListAdapter!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        NasAppMgr.updateAllInstalledNasApp()
        NasAppMgr.loadNasAppList()

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val mNasAppUpdateListener = object: NasAppMgr.UpdateListener {
        override fun onUpdate() {
            activity?.runOnUiThread {
                NasAppMgr.sort()
                appListAdapter.notifyDataSetChanged()
            }
        }

        override fun onLoadingNasAppList(loading: Boolean) {
            showProgress(loading)
        }

        override fun onLoadingNasAppApkUrls(loading: Boolean) {
            showProgress(loading)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _appListAdapter = NasAppListAdapter()
        binding.appList.adapter = appListAdapter
        binding.appList.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>, _: View, pos: Int, _: Long ->
            Utils.executor.execute {
                downApk(NasAppMgr.getNasApp(pos))
            }
        }

        showProgress(NasAppMgr.isNasAppListLoading())
        NasAppMgr.setUpdateListener(mNasAppUpdateListener)

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
    }

    override fun onDestroyView() {
        NasAppMgr.setUpdateListener(null)
        super.onDestroyView()
        _binding = null
    }

    private fun downApk(info: NasAppInfo?) {
        if (info?.apkUrl == null) {
            Log.e("download", "info is null or url is null")
            return
        }
        val url = info.apkUrl!!
        val md5 = Utils.md5(url+info.ver)
        val apkPath = "$md5.apk"
        val f = File(Utils.getFilesDir(), apkPath)
        if (f.exists()) {
            info.progress = 100
            activity?.runOnUiThread {
                appListAdapter.notifyDataSetChanged()
                Utils.installApk(f.absolutePath)
            }
            return
        }

        val tmpApk = "$apkPath.tmp"

        Utils.http.download(url, Utils.getFilesDir(), tmpApk, object: Http.OnDownloadListener {
            override fun onDownloadSuccess(path: String) {
                var apkPathTmp = path;
                if (path.endsWith(".tmp")) {
                    apkPathTmp = path.removeSuffix(".tmp")
                    File(path).renameTo(File(apkPathTmp))
                }
                Utils.installApk(apkPathTmp)
                Log.i("download", "success")
            }

            override fun onDownloading(progress: Int) {
                info.progress = progress
                Log.i("download", "progress: $progress")
                activity?.runOnUiThread {
                    appListAdapter.notifyDataSetChanged()
                }
            }

            override fun onDownloadFailed(e: Exception) {
                Log.e("download", "fail", e)
            }
        })
    }

    private fun showProgress(show: Boolean) {
        activity?.runOnUiThread {
            if (show) {
                binding.progressBarCyclic.visibility = View.VISIBLE
            } else {
                binding.progressBarCyclic.visibility = View.GONE
            }
        }
    }
}