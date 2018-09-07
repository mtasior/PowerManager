package output

import extension.LOG
import extension.toSuccessString
import messaging.LogLevel
import networking.NetworkImpl
import settings.Settings
import kotlin.math.roundToInt

class GoePowerController : ChargingPowerController {
    private val goeApi = GoeApi(NetworkImpl.shared) { Settings.shared.goeIp }
    private var currentStatus: GoeStatus? = null
    private val squareRootOf3 = 1.732f

    override fun setNewChargingPowerKiloWatt(desiredPowerKiloWatt: Float) {
        val powerWatt = desiredPowerKiloWatt * 1000

        val calculatedCurrent = when {
            Settings.shared.numberPhases == 1 -> powerWatt / 230
            Settings.shared.numberPhases == 2 -> powerWatt / 230 // Not sure yet what happens with 2 phases
            Settings.shared.numberPhases == 3 -> powerWatt / (400 * squareRootOf3)
            else -> 0f
        }
        var roundedCurrent = calculatedCurrent.roundToInt()

        //switch off when < 6
        roundedCurrent = if (roundedCurrent < 6) 0 else roundedCurrent

        LOG().log("GOECONTROLLER: Desired Power is $desiredPowerKiloWatt, calculated current per" +
                " phase is $calculatedCurrent A, mapping to $roundedCurrent", LogLevel.VERBOSE)

        // switch off the box if 0
        if (roundedCurrent == 0 && currentStatus?.alwBool == true) {
            val (succ, st) = goeApi.setAlw(false)
            currentStatus = st

            LOG().log("GOECONTROLLER: Desired current is $roundedCurrent, so wallbox is switched off temporarily:" +
                    " ${succ.toSuccessString()}")
            return
        }

        //switch the box on if an allowed current is present
        if (currentStatus?.alwBool == false && roundedCurrent > 0) {
            val (succ, st) = goeApi.setAlw(true)
            currentStatus = st
            LOG().log("GOECONTROLLER: Switching wallbox on: ${succ.toSuccessString()}")
        }

        //set the new current
        if (roundedCurrent > 0) {
            val (succ, st) = goeApi.setAmp(roundedCurrent)
            currentStatus = st
            LOG().log("GOECONTROLLER: Setting desired current $roundedCurrent to Go-e: ${succ.toSuccessString()}", LogLevel.VERBOSE)
            return
        } else {
            LOG().log("GOECONTROLLER: Desired current is 0 A, box stays off")
        }

    }

    override fun getcurrentConsumptionkiloWatt(): Float {
        currentStatus = goeApi.getStatus().status
        LOG().log("GOECONTROLLER: Current Box State:\n$currentStatus\n" +
                "Estimated Number of Phases: ${currentStatus?.estimatedNumberPhases}", LogLevel.VERBOSE)
        return currentStatus?.extractedPowerKiloWatt ?: 0f
    }
}
