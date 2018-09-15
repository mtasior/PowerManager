package main.java.manager

import com.google.gson.GsonBuilder
import output.CarState

class DataHolder private constructor() {
    private object Holder {
        val INSTANCE = DataHolder()
    }

    @Transient
    private var gson = GsonBuilder().setPrettyPrinting().create()

    companion object {
        val shared: DataHolder by lazy { Holder.INSTANCE }
    }

    var boxState: BoxState = BoxState(CarState.UNKNOWN, 0f)

    override fun toString(): String {
        return gson.toJson(this)
    }
}

data class BoxState(val carState: CarState, val currentPowerKilowatt: Float)