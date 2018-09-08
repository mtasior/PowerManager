package main.java.extension

import main.java.logging.Logger
import java.lang.reflect.Field
import java.util.*



fun Boolean.toSuccessString() = if (this) "successful" else "failed"
fun Boolean.toInteger() = if (this) 1 else 0


/**
 * Extension to obtain a logger everywhere
 */
fun LOG(): Logger {
    return Logger.shared
}

/**
 * checks if a given field name is contained as field in an object
 */
fun Any.containsField(fieldName: String): Boolean {
    return Arrays.stream<Field>(this.javaClass.fields)
            .anyMatch { f -> f.getName() == fieldName }
}