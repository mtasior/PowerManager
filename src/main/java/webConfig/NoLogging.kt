package main.java.webConfig

import org.eclipse.jetty.util.log.Logger

/**
 * To disable unnecessary logging
 */
class NoLogging : Logger {
    override fun debug(p0: String?, p1: Long) {

    }

    override fun getName(): String {
        return "no"
    }

    override fun warn(msg: String, vararg args: Any) {}
    override fun warn(thrown: Throwable) {}
    override fun warn(msg: String, thrown: Throwable) {}
    override fun info(msg: String, vararg args: Any) {}
    override fun info(thrown: Throwable) {}
    override fun info(msg: String, thrown: Throwable) {}
    override fun isDebugEnabled(): Boolean {
        return false
    }

    override fun setDebugEnabled(enabled: Boolean) {}
    override fun debug(msg: String, vararg args: Any) {}
    override fun debug(thrown: Throwable) {}
    override fun debug(msg: String, thrown: Throwable) {}
    override fun getLogger(name: String): Logger {
        return this
    }

    override fun ignore(ignored: Throwable) {}
}