/*
 * PropertyHelper
 */
package com.sypht.helper

/**
 * @author Gbolahan Kuti
 */
object PropertyHelper {

    fun getEnvOrProperty(key: String): String? {
        return System.getenv(key) ?: System.getProperty(key)
    }
}
