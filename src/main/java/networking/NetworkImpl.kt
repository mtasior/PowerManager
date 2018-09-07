package networking

import org.asynchttpclient.Dsl.asyncHttpClient
import java.net.URL
import java.util.concurrent.ExecutionException


class NetworkImpl : NetworkInterface {
    var asyncHttpClient = asyncHttpClient()

    init {
        Runtime.getRuntime().addShutdownHook(Thread(Runnable { run { asyncHttpClient.close() } }))
    }

    override fun doGetRequestSynchronous(url: URL): ResponseData {
        try {
            val whenResponse = asyncHttpClient.prepareGet(url.toExternalForm())
                    .execute()
            val response = whenResponse.get()
            return ResponseData(response.responseBody, responseCode = response.statusCode, error = null)
        } catch (connect: ExecutionException) {
            return ResponseData(null, responseCode = 500, error = ResponseError(connect.localizedMessage))
        } catch (e: Exception) {
            return ResponseData(null, responseCode = 500, error = ResponseError(e.localizedMessage))
        }
    }

    companion object {
        val shared: NetworkImpl = NetworkImpl()
    }
}