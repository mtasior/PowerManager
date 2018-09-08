import main.java.extension.LOG
import main.java.input.FroniusProvider
import main.java.manager.Manager
import main.java.output.GoePowerController
import main.java.settings.IpDetector
import main.java.settings.Settings
import main.java.webConfig.WebServer

fun main(args: Array<String>) {

    //Autodetect IPs if needed
    val detector = IpDetector()
    LOG().log("MANAGER: PowerManager V${Settings.shared.version}")

    if (Settings.shared.froniusIp == "0.0.0.0") {
        LOG().log("Auto-Detecting Fronius IP")
        LOG().suppressWarning(true)
        detector.searchForFronius()
        LOG().suppressWarning(false)
    }
    if (Settings.shared.goeIp == "0.0.0.0") {
        LOG().log("Auto-Detecting Go-e IP")
        LOG().suppressWarning(true)
        detector.searchForGoe()
        LOG().suppressWarning(false)
    }


    val chargingPowerController = GoePowerController()
    val consumptionsProvider = FroniusProvider()
    WebServer()

    val manager = Manager(consumptionsProvider, chargingPowerController)
    manager.start()
}
