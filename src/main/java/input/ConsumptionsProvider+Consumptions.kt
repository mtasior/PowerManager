package input

/**
 * shall provide all the current consumption values. can be used for all input
 */
interface ConsumptionsProvider{
    fun getConsumptions() : Consumptions
}

/**
 * currentProductionKiloWatt: in kW
 * currentConsumptionKiloWatt: in kW. The full consumption at the injection point to the grid
 */
data class Consumptions(val currentProductionKiloWatt: Float, val currentConsumptionKiloWatt: Float)