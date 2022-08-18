package top.fumiama.moegoe.tools

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask

object DownloadTools {
    private fun getConnection(url: String, method: String = "GET"): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 30000
        connection.readTimeout = 30000
        return connection
    }

    fun getHttpContentTask(u: String, refer: String? = null, ua: String? = null): FutureTask<ByteArray?> {
        Log.d("Mydl", "getHttp: $u")
        var ret: ByteArray? = null
        val task = FutureTask(Callable {
            try {
                getConnection(u).apply {
                    refer?.let { setRequestProperty("referer", it) }
                    ua?.let { setRequestProperty("User-agent", it) }
                    ret = inputStream.readBytes()
                    disconnect()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return@Callable ret
        })
        Thread(task).start()
        return task
    }
}