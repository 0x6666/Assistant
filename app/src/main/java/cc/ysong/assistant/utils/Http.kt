package cc.ysong.assistant.utils

import android.os.Environment
import android.util.Log
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class Http {
    private val okHttpClient: OkHttpClient = OkHttpClient()

    fun get(url: String, listener: HttpListener) {
        val request: Request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Downloader", "get fail", e)
                listener.onDownloadFailed()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { listener.onSuccess(it) }
                response.body?.close()
            }
        })
    }

    fun download(url: String, saveDir: String, name: String, listener: OnDownloadListener) {
        val request: Request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 下载失败
                listener.onDownloadFailed(e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                var `is`: InputStream? = null
                val buf = ByteArray(2048)
                var len = 0
                var fos: FileOutputStream? = null
                // 储存下载文件的目录
                val savePath = isExistDir(saveDir)
                try {
                    `is` = response.body?.byteStream()
                    val total: Long = response.body?.contentLength()!!
                    val file = File(savePath, name.ifEmpty { getNameFromUrl(url) })
                    fos = FileOutputStream(file)
                    var sum: Long = 0
                    if (`is` != null) {
                        while (`is`.read(buf).also { len = it } != -1) {
                            fos.write(buf, 0, len)
                            sum += len.toLong()
                            val progress = (sum * 1.0f / total * 100).toInt()
                            // 下载中
                            listener.onDownloading(progress)
                        }
                    }
                    fos.flush()
                    // 下载完成
                    listener.onDownloadSuccess(file.absolutePath)
                } catch (e: Exception) {
                    listener.onDownloadFailed(e)
                } finally {
                    try {
                        `is`?.close()
                    } catch (_: IOException) {
                    }
                    try {
                        fos?.close()
                    } catch (_: IOException) {
                    }
                }
            }
        })
    }


    @Throws(IOException::class)
    private fun isExistDir(saveDir: String): String {
        return if (File(saveDir).isAbsolute) {
            saveDir
        } else {
            val downloadFile = File(Environment.getExternalStorageDirectory(), saveDir)
            downloadFile.isAbsolute
            if (!downloadFile.mkdirs()) {
                downloadFile.createNewFile()
            }
            downloadFile.absolutePath
        }
    }

    private fun getNameFromUrl(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1)
    }

    interface OnDownloadListener {
        fun onDownloadSuccess(path: String)

        fun onDownloading(progress: Int)

        fun onDownloadFailed(e: Exception)
    }


    interface HttpListener {
        fun onSuccess(data: String)
        fun onDownloadFailed()
    }
}