package cc.ysong.assistant

import android.app.Application
import cc.ysong.assistant.utils.Utils

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Utils.init(applicationContext)
    }
}