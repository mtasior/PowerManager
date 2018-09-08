package main.java.input

import input.Consumptions
import input.ConsumptionsProvider
import main.java.settings.Settings
import networking.NetworkImpl

class FroniusProvider : ConsumptionsProvider {
    val api = FroniusSolarApi(NetworkImpl.shared) { Settings.shared.froniusIp }


    override fun getConsumptions(): Consumptions {
        val flow = api.getCurrentPowerFlow()
        return if (flow != null) {
            Consumptions((flow.powerPv / 1000).toFloat(), ((flow.powerPv + flow.powerGrid) / 1000).toFloat())
        } else {
            Consumptions(0f, 0f)
        }
    }
}