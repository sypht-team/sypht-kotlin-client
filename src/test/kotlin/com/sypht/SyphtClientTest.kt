package com.sypht

import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * @author Gbolahan Kuti
 */
class SyphtClientTest {

    /**
     * Test upload using a file
     */
    @Test
    @Throws(IOException::class, IllegalStateException::class)
    fun uploadWithPNGFile() {
        val fileId = SyphtClient().upload(getTestFile(), arrayOf("sypht.invoice"))
        assertNotNull("fileId was null", fileId)
    }

    /**
     * Test upload using InputStream
     */
    @Test
    @Throws(IOException::class, IllegalStateException::class)
    fun uploadWithPNGInputStream() {
        val file = getTestFile()
        val fileId = SyphtClient().upload(file.name, FileInputStream(file), arrayOf("sypht.invoice"))
        assertNotNull("fileId was null", fileId)
    }

    /**
     * Test upload and final prediction
     */
    @Test
    @Throws(IOException::class, IllegalStateException::class)
    fun getResults() {
        val syphtClient = SyphtClient()
        val results = syphtClient.result(syphtClient.upload(getTestFile(), arrayOf("sypht.invoice")))
        assertNotNull("result was null", results)
        println(results)
        assert(results.contains("total"))
    }

    /**
     * Test upload and final prediction using Proxy Server
     */
    @Test
    @Throws(IOException::class, IllegalStateException::class)
    fun getResultsWithSocksProxy() {
        System.setProperty("socksHost", "50.62.59.61")
        System.setProperty("socksPort", "1431")
        val syphtClient = SyphtClient()
        val results = syphtClient.result(syphtClient.upload(getTestFile(), arrayOf("sypht.invoice")))
        assertNotNull("result was null", results)
        println(results)
        assert(results.contains("total"))
    }

    private fun getTestFile(): File {
        val classLoader = javaClass.classLoader
        return File(classLoader.getResource("receipt.pdf").file)
    }
}