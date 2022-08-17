package top.fumiama.moegoe.tools

import android.util.Log
import top.fumiama.moegoe.tools.ssl.AllTrustManager
import top.fumiama.moegoe.tools.ssl.IgnoreHostNameVerifier
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

object DownloadTools {

    init {
        HttpsURLConnection.setDefaultHostnameVerifier(IgnoreHostNameVerifier())
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getInstance("SSL").let {
            it.init(null, arrayOf(AllTrustManager()), SecureRandom())
            it
        }.socketFactory)
    }

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