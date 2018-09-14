package main.java.manager

import input.ConsumptionsProvider
import main.java.extension.LOG
import main.java.settings.Settings
import main.java.settings.SettingsChangedListener
import messaging.LogLevel
import output.ChargingPowerController
import settings.ManagementMode
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

/**
 * the actual manager
 */
class Manager(
        private val consumptionsProvider: ConsumptionsProvider,
        private var chargingPowerController: ChargingPowerController
) : SettingsChangedListener {

    private var localTimer: Timer? = null    //the localTimer that executes a task with the given interval
    private var threadRunning = false   //indicates if a thread is currently running. if yes, the current interval is skipped
    private var settings = Settings.shared
        get() {
            return Settings.shared
        }
    private var currentInterval = 5000L
    private var lastAvailablePower = 0f //we need to persist the last value to add up the remaining power.

    init {
        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            localTimer?.cancel()
        }))

        settings.addListener(this)
    }

    private fun doLoop() {
        if (threadRunning) return
        threadRunning = true
        try {
            loopFunction()
        } catch (e: Exception) {
            LOG().log("MANAGER: Loop interrupted: (${e.message}), continuing automatically", LogLevel.WARN)
        }
        threadRunning = false
    }

    private fun loopFunction() {
        // calculate the currently available production first call the box. As all calls are done synchronously,
        // this could debounce the load on the box
        lastAvailablePower = chargingPowerController.getcurrentConsumptionkiloWatt()
        val (production, consumption) = consumptionsProvider.getConsumptions()
        val excessPower = production - consumption
        var newPower = excessPower + lastAvailablePower
        LOG().log("PowerManager Version V${Settings.shared.version}", LogLevel.VERBOSE)
        LOG().log("MANAGER: Excess Power from PV: $excessPower kW," +
                " current consumption of Box: $lastAvailablePower kW", LogLevel.VERBOSE)
        LOG().log("MANAGER: New available Power for Box: $newPower kW", LogLevel.VERBOSE)

        // apply the security margin
        newPower -= settings.securityMarginKiloWatt
        LOG().log("MANAGER: After applying security margin: $newPower kW", LogLevel.VERBOSE)

        // reduce to maximum allowed power
        if (newPower > settings.maxPowerKiloWatt) {
            newPower = settings.maxPowerKiloWatt
            LOG().log("MANAGER: This exceeds the maximum Power, set to $newPower kW", LogLevel.VERBOSE)
        }

        newPower = when (settings.mode) {
            ManagementMode.PV_WITH_MIN -> {
                if (newPower < settings.minPowerKiloWatt) settings.minPowerKiloWatt
                else newPower
            }
            ManagementMode.MAX -> settings.maxPowerKiloWatt
            ManagementMode.PV -> newPower
            ManagementMode.OFF -> 0f
        }
        LOG().log("MANAGER: Mode is ${settings.mode}, so: $newPower kW", LogLevel.VERBOSE)

        // if the resulting value is negative, the value is set to 0
        newPower = if (newPower < 0) 0f.also {
            LOG().log("MANAGER: was smaller than zero: 0.0", LogLevel.VERBOSE)
        } else newPower

        chargingPowerController.setNewChargingPowerKiloWatt(newPower)

    }


    fun start() {
        // execute one last time if it is switched off
        if (settings.mode == ManagementMode.OFF) {
            loopFunction()
            LOG().log("MANAGER: Mode is OFF, timer stopped after executing sequence")
            return
        }
        currentInterval = TimeUnit.SECONDS.toMillis(settings.intervalSeconds.toLong())
        LOG().log("MANAGER: Started Timer with interval $currentInterval")
        localTimer = timer(period = currentInterval, action = {
            Thread(Runnable { doLoop() }).start()
        })
    }

    private fun restart() {
        localTimer?.cancel()
        start()
    }

    override fun settingsChanged() {
        if (settings.intervalSeconds.toLong() != currentInterval * 1000
                || settings.mode != ManagementMode.OFF) {
            restart()
        }
    }
}