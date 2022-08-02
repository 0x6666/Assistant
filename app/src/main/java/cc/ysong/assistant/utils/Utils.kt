package cc.ysong.assistant.utils

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Utils {
    val executor: ExecutorService by lazy {
        Executors.newFixedThreadPool(10);
    }

    val downloader: Downloader by lazy {
        Downloader()
    }
}
