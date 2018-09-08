package main.java.settings

import com.google.gson.GsonBuilder
import messaging.LogLevel
import settings.ManagementMode
import settings.watchFolderAsync
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.nio.file.Paths


/**
 * Defines all settings. Can then be written to and read from a file on disk.
 * Constructor is private to suppress explicit creation
 */
class Settings private constructor() {

    /**
     * in order to write the current version into the status object for the API but not the Settings file
     */
    @Transient
    val version = "0.11"

    /**
     * the management Mode
     */
    private val modeComment = "Available modes: OFF, PV, PV_WITH_MIN, MAX"
    var mode: ManagementMode = ManagementMode.OFF
        @Synchronized
        set(value) {
            field = value
            saveConfigToDisk()
        }

    /**
     * the control loop interval in seconds. minimum value of 5s
     */
    private val intervalSecondsComment = "The interval in which the controller calculates new values"
    var intervalSeconds = 30
        @Synchronized
        set(value) {
            var newValue = value
            if (newValue < 5) newValue = 5
            field = newValue
            saveConfigToDisk()
        }

    /**
     * the minimum power level that is set to the wallbox. Applicable in ManagementMode.PV_WITH_MIN
     */
    private val minPowerKiloWattComment = "The minimum power level that is set to the Wallbox. Applicable in PV_WITH_MIN. Ignored else."
    var minPowerKiloWatt = 0f
        @Synchronized
        set(value) {
            field = value
            saveConfigToDisk()
        }

    /**
     * the max power that is set to the wallbox
     */
    private val maxPowerKilowattComment = "The maximum power that is set to the wallbox on all three phases. " +
            "If the wallbox uses only one, it has to be three times the allowed amount."
    var maxPowerKiloWatt = 11f
        @Synchronized
        set(value) {
            field = value
            saveConfigToDisk()
        }

    /**
     * the security margin in order to not exceed the produced power. Set to 1.5 in order to stay 1.5 kW below the maximum available power
     */
    val securityMarginKiloWattComment = "The security margin in order to not exceed the produced power. Set to 1.5 in order to stay 1.5 kW below the maximum available power"
    var securityMarginKiloWatt = 0f
        @Synchronized
        set(value) {
            field = value
            saveConfigToDisk()
        }

    /**
     * the IP address of a potential goe charger
     */
    var froniusIp = "0.0.0.0"
        @Synchronized
        set(value) {
            field = value
            saveConfigToDisk()
        }

    /**
     * the IP address of a potential goe charger
     */
    var goeIp = "0.0.0.0"
        @Synchronized
        set(value) {
            field = value
            saveConfigToDisk()
        }

    /**
     * the global loglevel
     */
    private var logLevelComment = "Available Log levels: VERBOSE, INFO, WARN"
    var logLevel = LogLevel.INFO
        @Synchronized
        set(value) {
            field = value
            saveConfigToDisk()
        }

    /**
     * the amount of phases
     */
    private var numberPhasesComment = "How many phases are utilized in the wallbox"
    var numberPhases: Int = 3
        @Synchronized
        get() {
            return if (field in listOf(1, 2, 3)) field else 3
        }
        @Synchronized
        set(value) {
            if (value in listOf(1, 2, 3)) field = value
            else field = 3
            saveConfigToDisk()
        }

    /**
     * the port under which the API is reachable. -1 switches off
     */
    private var portComment = "If it is desired to have the configuartion API active, set this to a valid port. -1 switches off the server"
    var port = -1
        set(value) {
            field = value
            saveConfigToDisk()
        }

    /**
     * enables the logging uplink
     */
    var publishLog = false
        @Synchronized
        set(value) {
            field = value
            saveConfigToDisk()
        }

    /**
     * adds a listener to the list
     */
    fun addListener(listener: SettingsChangedListener) {
        listeners.add(listener)
    }

    /**
     * removes the given listener if it is contained in the list
     */
    fun removeListener(listener: SettingsChangedListener) {
        if (listeners.contains(listener)) listeners.remove(listener)
    }

    private fun saveConfigToDisk() {
        informListeners()
        val file = File(FILENAME)
        file.writeText(gson.toJson(this))
    }

    override fun toString(): String {
        return gson.toJson(this)
    }

    /**
     * While creating the settings object, it must be made sure that, if a file is available, this is read.
     */
    companion object {
        private val FILENAME = "PowerManagerSettings.config"
        private val gson = GsonBuilder().setPrettyPrinting().create()
        private val listeners: MutableList<SettingsChangedListener> = mutableListOf()

        /**
         * implements the surveillance of the settings file. If a new one is found, the current instance is
         * replaced and the listeners are informed
         */
        init {
            watchFolderAsync(Paths.get("")) {
                if (it.context().toString().equals(FILENAME)
                        && it.kind().name().equals("ENTRY_MODIFY")
                ) {
                    val s = readFromFile()
                    if (s != null) {
                        shared = s
                        informListeners()
                    }
                }
            }
        }

        var shared: Settings = build()
            @Synchronized
            get() {
                return field
            }

        private fun build(): Settings {
            val read = readFromFile()
            return if (read == null) {
                println("Creating new Settings file")
                val s = Settings()
                s.saveConfigToDisk()
                s
            } else {
                println("Settings file found")
                read
            }
        }

        private fun readFromFile(): Settings? {
            val settings: Settings?
            try {
                settings = gson.fromJson(FileReader(FILENAME), Settings::class.java)
            } catch (e: FileNotFoundException) {
                return null
            } catch (s: Exception) {
                println("Config File not readable!")
                return null
            }
            return settings
        }

        @Synchronized
        private fun informListeners() {
            for (listener in listeners) {
                listener.settingsChanged()
            }
        }

    }
}

interface SettingsChangedListener {
    fun settingsChanged()
}

