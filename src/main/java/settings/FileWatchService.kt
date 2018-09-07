package settings

import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchService
import kotlin.concurrent.thread

private fun Path.watch(): WatchService {
    //Create a watch service
    val watchService = this.fileSystem.newWatchService()

    //Register the service, specifying which events to watch
    register(watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.OVERFLOW,
            StandardWatchEventKinds.ENTRY_DELETE)

    //Return the watch service
    return watchService
}

fun watchFolderAsync(folder: Path, action: (WatchEvent<*>) -> Unit) {
    thread(start = true) {
        val path = folder
        val watcher = path.watch()

        while (true) {
            //The watcher blocks until an event is available
            val key = watcher.take()

            //Now go through each event on the folder
            key.pollEvents().forEach { it ->
                action(it)
            }
            //Call reset() on the key to watch for future events
            key.reset()
        }
    }
}

