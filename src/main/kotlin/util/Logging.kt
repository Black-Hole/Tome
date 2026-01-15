package util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Initializes the logger for the application.
 * Note: SLF4J with Logback is configured via logback.xml in resources.
 */
fun initLogger() {
    // SLF4J/Logback initialization is automatic via logback.xml
    // This function exists for API compatibility with the Rust version
    val logger = LoggerFactory.getLogger("util.Logging")
    logger.debug("Logger initialized")
}

/**
 * Extension function to get a logger for any class.
 */
inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}
