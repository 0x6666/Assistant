package cc.ysong.assistant

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import android.util.Log

class AssistantService : Service() {
    var TAG = "AssistantService"

    //构造内部类
    private val stub: IAssistant.Stub = object : IAssistant.Stub() {
        @Throws(RemoteException::class)
        override fun basicTypes(
            anInt: Int,
            aLong: Long,
            aBoolean: Boolean,
            aFloat: Float,
            aDouble: Double,
            aString: String?
        ) {
            Log.d(TAG, "basicTypes")
        }

        override fun setTime(time: Int) {

        }
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.e("call", "onBind")
        return stub
    }

    override fun onCreate() {
        Log.e("call", "onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("call", "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.e("call", "onDestroy")
        super.onDestroy()
    }
}