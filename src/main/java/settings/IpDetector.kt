package main.java.settings

import extension.LOG
import input.FroniusSolarApi
import networking.NetworkImpl
import output.GoeApi
import settings.Settings
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors


class IpDetector {
    val responses: ArrayList<String> = ArrayList()
    val ips: ArrayList<String> = ArrayList()

    private fun searchForIPs() {
        //search is already done
        if (ips.size != 0) return


        //execute ARP -A
        val builder = ProcessBuilder()
        builder.command("arp", "-a")
        builder.directory(File(System.getProperty("user.home")))

        val process = builder.start()
        val streamGobbler = StreamGobbler(process.inputStream) { data: String -> responses.add(data) }
        Executors.newSingleThreadExecutor().submit(streamGobbler)
        val exitCode = process.waitFor()
        assert(exitCode == 0)

        if (exitCode != 0) {
            LOG().log("IPDETECTOR: Automatic Detection of Fronius not successful")
        }

        extractIPs()
    }

    private fun extractIPs() {
        for (s in responses) {
            val ip = s.split(" ")[1].replace("(", "").replace(")", "")
            ips.add(ip)
        }
    }

    fun searchForFronius() {
        searchForIPs()
        for (s in ips) {
            val api = FroniusSolarApi(NetworkImpl.shared) { s }
            if (api.getCurrentPowerFlow() != null){
                Settings.shared.froniusIp = s
                LOG().log("IPDETECTOR: Fronius IP found and set: $s")
                return
            }
        }
        LOG().log("IPDETECTOR: Fronius IP not found. Please set manually in the config file")
    }

    fun searchForGoe() {
        searchForIPs()
        for (s in ips) {
            val api = GoeApi(NetworkImpl.shared) { s }
            if (api.getStatus().successful){
                Settings.shared.goeIp = s
                LOG().log("IPDETECTOR: Go-e IP found and set: $s")
                return
            }
        }
        LOG().log("IPDETECTOR: Go-e IP not found. Please set manually in the config file")
    }


    private class StreamGobbler(private val inputStream: InputStream, private val consumer: (data: String) -> Unit) : Runnable {

        override fun run() {
            BufferedReader(InputStreamReader(inputStream)).lines()
                    .forEach(consumer)
        }
    }
}