package messaging

/**
 * in order to provide the possibility to capture logging output and deliver it to arbitrary receivers.
 * E.g. MQTT brokers
 */
interface UnifiedLogging {
    fun log(text: String, level: LogLevel = LogLevel.INFO)
}


enum class LogLevel {
    WARN, INFO, VERBOSE
}