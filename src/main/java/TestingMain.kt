package main.java

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import main.java.settings.IpDetector
import main.java.settings.SettingsChangedListener
import settings.watchFolderAsync
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.nio.file.Paths


fun main(args: Array<String>) {
//    IpDetector().searchForFronius()
    IpDetector().searchForGoe()
    System.exit(0)
}


class TestProvider
/**
 *  to suppress the explicit creation of this object
 */ private constructor() {


    var production = 11f
        @Synchronized
        set(value) {
            field = value
            saveConfigToDisk()
        }

    var consumption = 11f
        @Synchronized
        set(value) {
            field = value
            //saveConfigToDisk()
        }


    /**
     * removes the given listener if it is contained in the list
     */
    fun removeListener(listener: SettingsChangedListener) {
        if (listeners.contains(listener)) listeners.remove(listener)
    }

    private fun saveConfigToDisk() {
        val file = File(FILENAME)
        file.writeText(gson.toJson(this))
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

    /**
     * While creating the settings object, it must be made sure that, if a file is available, this is read.
     */
    companion object {
        private val FILENAME = "TestProvider.config"
        private val gson = GsonBuilder().setPrettyPrinting().create()
        private val listeners: MutableList<SettingsChangedListener> = mutableListOf()

        /**
         * implements the surveillance of the settings file. If a new one is found, the current shared is
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
                    }
                }
            }
        }

        var shared: TestProvider = build()
            @Synchronized
            get() {
                return field
            }

        private fun build(): TestProvider {
            val read = readFromFile()
            return if (read == null) {
                println("Creating new Settings file")
                val s = TestProvider()
                s.saveConfigToDisk()
                s
            } else {
                println("Settings file found")
                read
            }
        }

        private fun readFromFile(): TestProvider? {
            val settings: TestProvider?
            try {
                settings = gson.fromJson(FileReader(FILENAME), TestProvider::class.java)
            } catch (e: FileNotFoundException) {
                return null
            } catch (s: Exception) {
                println("Config File not readable!")
                return null
            }
            return settings
        }

    }
}