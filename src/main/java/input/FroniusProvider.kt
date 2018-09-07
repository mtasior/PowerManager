package input

import networking.NetworkImpl
import settings.Settings

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