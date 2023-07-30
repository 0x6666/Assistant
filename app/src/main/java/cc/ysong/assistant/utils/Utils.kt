package cc.ysong.assistant.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@SuppressLint("StaticFieldLeak")
object Utils {
    private var _context : Context? = null

    val context: Context
        get() = _context!!

    fun init(c: Context) {
        assert(_context == null)
        _context = c
    }

    val executor: ExecutorService by lazy {
        Executors.newFixedThreadPool(10);
    }

    val http: Http by lazy {
        Http()
    }

    fun getFilesDir() : String {
        return context.filesDir.absolutePath
    }

    fun md5(data: String) : String? {
        val md5 = MessageDigest.getInstance("MD5")
        md5.update(StandardCharsets.UTF_8.encode(data))
        return java.lang.String.format("%032x", BigInteger(1, md5.digest()))
    }

    fun installApk(apkPath: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val type = "application/vnd.android.package-archive"
        val uri: Uri = FileProvider.getUriForFile(context, context.packageName.toString() + ".fileProvider", File(apkPath))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, type)
        context.startActivity(intent)
    }
}
