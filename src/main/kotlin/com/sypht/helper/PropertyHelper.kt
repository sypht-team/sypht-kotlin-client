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

    fun getEnvOrPropertyValueAsLong(key: String): Long {
        val value = getEnvOrProperty(key)
        value?.let {
            try {
                return it.toLong()
            } catch(exception: NumberFormatException) {
                return 0;
            }
        }
        return 0
    }
}
