package networking

import java.net.URL

interface NetworkInterface{
    fun doGetRequestSynchronous(url: URL): ResponseData
}

data class ResponseData(val text: String?, val responseCode: Int, val error: ResponseError?)
data class ResponseError(val errorText: String?)