package input

import extension.LOG
import messaging.LogLevel
import networking.ApiStub
import networking.NetworkInterface
import java.net.URL

class FroniusSolarApi(network: NetworkInterface, getIp: () -> String) : ApiStub(network, getIp) {

    /**
     * positive at the injection point into the grid: producing power for the grid
     * negative: consuming power from grid
     */
    fun getCurrentPowerFlow(): PowerFlow? {
        val requestUrl = URL(url, "/solar_api/v1/GetPowerFlowRealtimeData.fcgi")
        LOG().log("FROAPI: Requesting Status using ${requestUrl.toExternalForm()}", LogLevel.VERBOSE)
        val response = requestData(requestUrl, GetPowerFlowRealtimeDataResponse::class.java)
        return if (response != null) {
            PowerFlow(response.Body.Data.Site.P_Grid ?: 0.0, response.Body.Data.Site.P_PV ?: 0.0)
        } else {
            null
        }
    }

    data class PowerFlow(
            val powerGrid: Double,
            val powerPv: Double
    )

}


data class GetPowerFlowRealtimeDataResponse(
        val Body: Body,
        val Head: Head
)

data class Head(
        val Status: Status,
        val Timestamp: String
)

data class Status(
        val Code: Int,
        val Reason: String,
        val UserMessage: String
)

data class Body(
        val Data: Data
)

data class Data(
        val Site: Site,
        val Version: String
)

data class Site(
        val E_Day: Int,
        val E_Total: Int,
        val E_Year: Int,
        val Meter_Location: String,
        val Mode: String,
        val P_Akku: Double?,
        val P_Grid: Double?,
        val P_Load: Double?,
        val P_PV: Double?,
        val rel_Autonomy: Double,
        val rel_SelfConsumption: Double
)