package main.java.logging


data class test(
    val Body: Body,
    val Head: Head
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
    val P_Akku: Any,
    val P_Grid: Double,
    val P_Load: Double,
    val P_PV: Int,
    val rel_Autonomy: Int,
    val rel_SelfConsumption: Double
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
