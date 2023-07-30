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
                _appListAdapter?.notifyDataSetChanged()
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
            NasAppMgr.getAppApkUrls(pos, fun(apks: String?) {
                if (apks != null) {
                    if (apks.isNotEmpty()) {
                        val info = NasAppMgr.getNasApp(pos)
                        if (info != null) {
                            activity?.runOnUiThread {
                                downApk(apks, info)
                            }
                        }
                    }
                }
            })
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

    private fun downApk(url: String, info: NasAppInfo) {
        val md5 = Utils.md5(url)
        val f = File(Utils.getFilesDir(), "$md5.apk")
        if (f.exists()) {
            info.progress = 100
            activity?.runOnUiThread {
                appListAdapter.notifyDataSetChanged()
                Utils.installApk(f.absolutePath)
            }
            return
        }

        Utils.http.download(url, Utils.getFilesDir(), "$md5.apk", object: Http.OnDownloadListener {
            override fun onDownloadSuccess(path: String) {
                Utils.installApk(path)
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