package main.java.networking

import com.google.gson.Gson
import main.java.extension.LOG
import messaging.LogLevel
import networking.NetworkInterface
import java.net.URL

open class ApiStub(private val network: NetworkInterface, private val getIp: () -> String) {
    var url: URL = URL("http://0.0.0.0/")
        get() {
            return URL("http://${getIp()}/")
        }
    val gson = Gson()


    fun <T> requestData(url: URL, classToParse: Class<T>): T? {
        val (text, _, error) = network.doGetRequestSynchronous(url)

        if (error == null && text != null) {
            return try {
                val response = gson.fromJson(text, classToParse)
                response
            } catch (e: Exception) {
                LOG().log("API: Json not parseable", LogLevel.WARN)
                null
            }
        } else if (error?.errorText != null) {
            LOG().log("API: ${error.errorText} using $url", LogLevel.WARN)
        } else if (text == null) {
            LOG().log("API: Response without error but also without content. Please contact developer.", LogLevel.WARN)
        }
        return null
    }

}