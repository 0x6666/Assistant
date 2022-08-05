package cc.ysong.assistant.utils

import android.os.Environment
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class Downloader {
    private val okHttpClient: OkHttpClient = OkHttpClient()

    fun download(url: String, saveDir: String, listener: OnDownloadListener) {
        val request: Request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 下载失败
                listener.onDownloadFailed()
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
                    val file = File(savePath, getNameFromUrl(url))
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
                    listener.onDownloadSuccess()
                } catch (e: Exception) {
                    listener.onDownloadFailed()
                } finally {
                    try {
                        `is`?.close()
                    } catch (e: IOException) {
                    }
                    try {
                        fos?.close()
                    } catch (e: IOException) {
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
        fun onDownloadSuccess()

        fun onDownloading(progress: Int)

        fun onDownloadFailed()
    }
}