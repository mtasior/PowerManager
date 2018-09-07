import extension.LOG
import input.FroniusProvider
import main.java.settings.IpDetector
import main.java.webConfig.WebServer
import manager.Manager
import output.GoePowerController
import settings.Settings

fun main(args: Array<String>) {

    //Autodetect IPs if needed
    val detector = IpDetector()
    detector.LOG().log("MANAGER: PowerManager V0.9")

    if (Settings.shared.froniusIp == "0.0.0.0") {
        detector.LOG().log("Auto-Detecting Fronius IP")
        detector.LOG().suppressWarning(true)
        detector.searchForFronius()
        detector.LOG().suppressWarning(false)
    }
    if (Settings.shared.goeIp == "0.0.0.0") {
        detector.LOG().log("Auto-Detecting Go-e IP")
        detector.LOG().suppressWarning(true)
        detector.searchForGoe()
        detector.LOG().suppressWarning(false)
    }


    val chargingPowerController = GoePowerController()
    val consumptionsProvider = FroniusProvider()
    WebServer()

    val manager = Manager(consumptionsProvider, chargingPowerController)
    manager.start()
}
