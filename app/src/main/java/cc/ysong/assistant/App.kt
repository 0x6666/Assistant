package cc.ysong.assistant

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import cc.ysong.assistant.utils.MyCrashHandler
import cc.ysong.assistant.utils.Utils


class App : Application() {

    override fun onCreate() {
        super.onCreate()

        bindService(Intent(baseContext, AssistantService::class.java), serviceConnection, BIND_AUTO_CREATE)

        Utils.init(applicationContext)

        Thread.setDefaultUncaughtExceptionHandler(MyCrashHandler())
    }

    var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val iMyServer: IAssistant = IAssistant.Stub.asInterface(service)
            try {
//                iMyServer.say("how are you?")
//                val result: Int = iMyServer.tell("how are you?", 18)
//                Log.d("IPC", "receive return content:$result in client")
            } catch (e: Exception) {
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            //Service被销毁时调用(内存不足等，正常解绑不会走这)
        }
    }
}