package com.sypht.helper

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.Okio
import okio.Source
import java.io.IOException
import java.io.InputStream

/**
 * @author Gbolahan Kuti
 */
class InputStreamRequestBody(val contentType: MediaType?, private val inputStream: InputStream) : RequestBody() {
    override fun contentType(): MediaType? {
        return contentType
    }

    override fun contentLength(): Long {
        try {
            return inputStream.available().toLong()
        } catch (e: IOException) {
            return -1
        }

    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        Okio.source(inputStream).let {
            try {
                sink.writeAll(it)
            } finally {
                Util.closeQuietly(it)
            }
        }
    }
}