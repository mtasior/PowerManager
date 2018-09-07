package output

import extension.LOG
import extension.toInteger
import messaging.LogLevel
import networking.ApiStub
import networking.NetworkInterface
import java.net.URL

/**
 * A small implementation of the Go-e charger API. Intended for local use
 * https://go-e.co/app/api.pdf
 *
 * the getGoeIP function expects to receive an IP in the form of "192.168.100.23"
 *
 */
class GoeApi(network: NetworkInterface, getIp: () -> String) :
        ApiStub(network, getIp) {

    /**
     * returns the full status object or null
     */
    fun getStatus(): GoeApiResponse {
        val statusUrl = URL(url, "status")
        LOG().log("GOEAPI: Requesting Status using ${statusUrl.toExternalForm()}", LogLevel.VERBOSE)
        val response = requestData(statusUrl, GoeStatus::class.java)
        return GoeApiResponse(response != null, response)
    }

    /**
     * sets the current maximum current. Clamped between min and max of the interface
     * @return true if successful
     */
    fun setAmp(raw: Int): GoeApiResponse {
        val validated = when {
            raw < 6 -> 6
            raw > 32 -> 32
            else -> raw
        }
        var setAmpUrl = URL(url, "mqtt?payload=amp=$validated")
        LOG().log("GOEAPI: setting AMP using $setAmpUrl", LogLevel.VERBOSE)

        val status = requestData(setAmpUrl, GoeStatus::class.java)
        if (status != null && status.amp == raw.toString()) {
            return GoeApiResponse(true, status)
        }
        return GoeApiResponse(false, status)
    }

    /**
     * sets allow_charging.
     * @return true if successful
     */
    fun setAlw(alw: Boolean): GoeApiResponse {
        val transformedAlw = alw.toInteger()
        var alwUrl = URL(url, "mqtt?payload=alw=${transformedAlw}")
        LOG().log("GOEAPI: Setting Alw using $alwUrl", LogLevel.VERBOSE)

        val status = requestData(alwUrl, GoeStatus::class.java)
        if (status != null && status.alw == transformedAlw.toString()) {
            return GoeApiResponse(true, status)
        }
        return GoeApiResponse(false, status)
    }

}

data class GoeApiResponse(val successful: Boolean, val status: GoeStatus?)

data class GoeStatus(
        val version: String,
        val rbc: String,
        val rbt: String,
        val car: String,
        val amp: String,
        val err: String,
        val ast: String,
        val alw: String,
        val stp: String,
        val cbl: String,
        val pha: String,
        val tmp: String,
        val dws: String,
        val dwo: String,
        val adi: String,
        val uby: String,
        val eto: String,
        val wst: String,
        val nrg: List<Int>,
        val fwv: String,
        val sse: String,
        val wss: String,
        val wke: String,
        val wen: String,
        val tof: String,
        val tds: String,
        val lbr: String,
        val aho: String,
        val afi: String,
        val ama: String,
        val al1: String,
        val al2: String,
        val al3: String,
        val al4: String,
        val al5: String,
        val cid: String,
        val cch: String,
        val cfi: String,
        val lse: String,
        val ust: String,
        val wak: String,
        val r1x: String,
        val dto: String,
        val nmo: String,
        val eca: String,
        val ecr: String,
        val ecd: String,
        val ec4: String,
        val ec5: String,
        val ec6: String,
        val ec7: String,
        val ec8: String,
        val ec9: String,
        val ec1: String,
        val rca: String,
        val rcr: String,
        val rcd: String,
        val rc4: String,
        val rc5: String,
        val rc6: String,
        val rc7: String,
        val rc8: String,
        val rc9: String,
        val rc1: String,
        val rna: String,
        val rnm: String,
        val rne: String,
        val rn4: String,
        val rn5: String,
        val rn6: String,
        val rn7: String,
        val rn8: String,
        val rn9: String,
        val rn1: String
) {
    val extractedPowerKiloWatt: Float
        get() {
            return nrg[11].toFloat() / 100
        }

    val carState: CarState
        get() {
            return when (car) {
                "1" -> CarState.BOX_READY_NO_CAR
                "2" -> CarState.CAR_CHARGING
                "3" -> CarState.WAITING_FOR_CAR
                "4" -> CarState.CHARGING_ENDED_CAR_CONNECTED
                else -> CarState.UNKNOWN
            }
        }

    val alwBool: Boolean
        get() {
            return if (alw == "1") true else false
        }

    val estimatedNumberPhases: Int?
        get() {
            val overallPower = extractedPowerKiloWatt
            if (overallPower == 0f) return null // then there is no load on the system, phases cannot be estimated

            val powers = listOf(nrg[7].toFloat() / 10, nrg[8].toFloat() / 10, nrg[9].toFloat() / 10).sortedDescending()
            return when {
                powers[0] == 0f && powers[1] == 0f && powers[2] == 0f -> null // should not occur
                powers[0] > 0f && powers[1] == 0f && powers[2] == 0f -> 1
                powers[0] > 0f && powers[1] > 0f && powers[2] == 0f -> 2
                powers[0] > 0f && powers[1] > 0f && powers[2] > 0f -> 3
                else -> null
            }
        }
}

enum class CarState { BOX_READY_NO_CAR, CAR_CHARGING, WAITING_FOR_CAR, CHARGING_ENDED_CAR_CONNECTED, UNKNOWN }