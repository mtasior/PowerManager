package main.java.logging

import main.java.settings.Settings
import main.java.settings.SettingsChangedListener
import messaging.LogLevel
import messaging.UnifiedLogging
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * to provide a global, verbose logging interface.
 * Can be used for explicitly implementing MQTT Log output
 *
 */
class Logger : UnifiedLogging, SettingsChangedListener {
    private var settings = Settings.shared
        get() {
            return Settings.shared
        }

    private val mqtt = MqttClient("tcp://tasior.info:1883", "PowerManager")
    private val options = MqttConnectOptions()
    private var suppressWarning: Boolean = false // for the IP Detector, to suppress the warnings

    init {
        settings.addListener(this)
        options.userName = "whatsapp"
        options.password = "client".toCharArray()
    }

    private object Holder {
        val INSTANCE = Logger()
    }

    /**
     * the singleton structure
     */
    companion object {
        val shared: Logger by lazy { Holder.INSTANCE }
    }

    override fun settingsChanged() {
    }

    override fun log(text: String, level: LogLevel) {
        val s = settings.logLevel
        when {
            s == LogLevel.VERBOSE -> doLog(text, level)
            s == LogLevel.INFO && level != LogLevel.VERBOSE -> doLog(text, level)
            s == LogLevel.WARN && level == LogLevel.WARN -> doLog(text, level)
        }

        val active = Settings.shared.publishLog
        if (active && !mqtt.isConnected) try {
            mqtt.connect(options)
        } catch (e: Exception){
            println("MQTT Error: ${e.message}")
        }
        if (active && mqtt.isConnected) {
            val m = MqttMessage(text.toByteArray())
            mqtt.publish("PowerManager/$level", m)
        }
        if (!active && mqtt.isConnected) {
            mqtt.disconnect()
        }
    }

    fun suppressWarning(on: Boolean) {
        suppressWarning = on
    }

    private fun doLog(text: String, level: LogLevel) {
        if (suppressWarning && level == LogLevel.WARN) return
        println("$level: $text")
    }
}