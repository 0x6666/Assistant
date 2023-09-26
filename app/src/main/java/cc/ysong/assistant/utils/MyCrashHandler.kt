package cc.ysong.assistant.utils

import android.text.TextUtils
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer


class MyCrashHandler : Thread.UncaughtExceptionHandler {

    var TAG = "MyCrashHandler"

    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e(TAG, "Thread = ${t.name} Throwable = ${e.message}")
        val stackTraceInfo = getStackTraceInfo(e)
        Log.e(TAG, stackTraceInfo)
        saveThrowableMessage(stackTraceInfo)
    }

    /**
     * 获取错误的信息
     *
     * @param throwable
     * @return
     */
    private fun getStackTraceInfo(throwable: Throwable): String {
        var pw: PrintWriter? = null
        val writer: Writer = StringWriter()
        try {
            pw = PrintWriter(writer)
            throwable.printStackTrace(pw)
        } catch (e: Exception) {
            return ""
        } finally {
            pw?.close()
        }
        return writer.toString()
    }

    private val logFilePath = Utils.getFilesDir() + File.separator + "log"+ File.separator + "crash"

    private fun saveThrowableMessage(errorMessage: String) {
        if (TextUtils.isEmpty(errorMessage)) {
            return
        }
        val file = File(logFilePath)
        if (!file.exists()) {
            val mkdirs = file.mkdirs()
            if (mkdirs) {
                writeStringToFile(errorMessage, file)
            }
        } else {
            writeStringToFile(errorMessage, file)
        }
    }

    private fun writeStringToFile(errorMessage: String, file: File) {
        Thread {
            var outputStream: FileOutputStream? = null
            try {
                val inputStream = ByteArrayInputStream(errorMessage.toByteArray())
                outputStream = FileOutputStream(
                    File(
                        file,
                        System.currentTimeMillis().toString() + ".txt"
                    )
                )
                var len = 0
                val bytes = ByteArray(1024)
                while (inputStream.read(bytes).also { len = it } != -1) {
                    outputStream.write(bytes, 0, len)
                }
                outputStream.flush()
                Log.e(TAG, "写入本地文件成功：" + file.absolutePath)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }
}