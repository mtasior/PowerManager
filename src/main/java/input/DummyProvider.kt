package input

import main.java.TestProvider

class DummyProvider : ConsumptionsProvider {
    override fun getConsumptions(): Consumptions {
        return Consumptions(TestProvider.shared.production, TestProvider.shared.consumption)
    }
}