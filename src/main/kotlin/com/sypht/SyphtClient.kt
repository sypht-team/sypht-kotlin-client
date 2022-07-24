package com.sypht

import com.sypht.auth.EnvironmentVariableCredentialProvider
import com.sypht.helper.Constants
import com.sypht.auth.ICredentialProvider
import com.sypht.helper.InputStreamRequestBody
import com.sypht.helper.PropertyHelper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Connect to the Sypht API at https://api.sypht.com
 */
open class SyphtClient(credentialProvider: ICredentialProvider = EnvironmentVariableCredentialProvider()) {
    private var bearerToken: String? = null
    private var okHttpClient: OkHttpClient? = null
    private var oauthClient: OAuthClient
    private var OAUTH_GRACE_PERIOD = 1000 * 60 * 10
    private var requestTimeout: Long = 30
    private val log = Logger.getLogger("com.sypht.OAuthClient")

    /**
     * Create a default Sypht client that manages bearer tokens automatically.
     */
    init {
        configureRequestTimeout()
        oauthClient = OAuthClient(requestTimeout, credentialProvider)
    }

    /**
     * Create a custom Sypht client with your own bearer token.
     *
     * @param bearerToken the Jwt token
     */
    constructor(bearerToken: String) : this() {
        this.bearerToken = bearerToken
    }

    /**
     * Pass a file to Sypht for detection.
     *
     * @param file the file in pdf, jpeg, gif or png format. Files may be up to
     *             20MB in size and pdf files may contain up to 16 individual pages.
     * @param fieldSetOptions pass in custom upload options here.
     * @param requestTimeout pass in custom http request timeout in seconds, default is 30seconds.
     * @return a fileId as a String.
     * @throws IOException in the event the upload went wrong.
     * @throws IllegalStateException when http response code is outside 200...299 or the response body is null.
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun upload(file: File, fieldSetOptions: Array<String>): String {
        val builder = buildMultipartBodyUploadWithFile(file)
        builder.addFormDataPart("fieldSets", JSONArray(fieldSetOptions).toString())
        return performUpload(builder)
    }

    /**
     * Pass a file to Sypht for detection.
     *
     * @param fileName the file name
     * @param inputStream binary input stream of pdf, jpeg, gif or png format. Files may be up to
     *                    20MB in size and pdf files may contain up to 16 individual pages.
     * @param fieldSetOptions pass in custom upload options here.
     * @param requestTimeout pass in custom http request timeout in seconds, default is 30seconds.
     * @return a fileId as a String.
     * @throws IOException in the event the upload went wrong.
     * @throws IllegalStateException when http response code is outside 200...299 or the response body is null.
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun upload(fileName: String, inputStream: InputStream, fieldSetOptions: Array<String>): String {
        val builder = buildMultipartBodyUploadWithInputStream(fileName, inputStream)
        builder.addFormDataPart("fieldSets", JSONArray(fieldSetOptions).toString())
        return performUpload(builder)
    }

    private fun buildMultipartBodyUploadWithFile(file: File): MultipartBody.Builder {
        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "fileToUpload", file.name,
                RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), file)
            )
    }

    private fun buildMultipartBodyUploadWithInputStream(
        fileName: String,
        inputStream: InputStream
    ): MultipartBody.Builder {
        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "fileToUpload", fileName,
                InputStreamRequestBody(MediaType.parse("application/x-www-form-urlencoded"), inputStream)
            )
    }

    @Throws(IOException::class, IllegalStateException::class)
    private fun performUpload(builder: MultipartBody.Builder): String {
        val client = getOkHttpClient()
        val formBody = builder.build()
        val request = createAuthorizedHttpRequest("${Constants.SYPHT_API_ENDPOINT}/fileupload")
            .post(formBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("sypht upload failed")
        }
        val jsonObject = JSONObject(response.body()!!.string())
        val fileId = jsonObject.getString("fileId")
        log.info("sypht file upload successful, fileId " + fileId)
        return fileId
    }

    /**
     * Fetch prediction results from Sypht.
     *
     * @param fileId the fileId.
     * @return prediction results in JSON format.
     * @throws IOException when api execution fails
     * @throws IllegalStateException when http response code is outside 200...299 or the response body is null.
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun result(fileId: String): String {
        val client = getOkHttpClient()
        val request = createAuthorizedHttpRequest("${Constants.SYPHT_API_ENDPOINT}/result/final/$fileId")
            .get()
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("sypht upload failed")
        }
        log.info("sypht results successfully fetched for fileId " + fileId)
        return response.body()!!.string()
    }

    private fun createAuthorizedHttpRequest(url: String): Request.Builder {
        return Request.Builder().url(url)
            .addHeader("Accepts", "application/json")
            .addHeader("Authorization", "Bearer " + getBearerToken())
    }

    @Synchronized
    private fun getBearerToken(): String {
        bearerToken?.let {
            val cacheExpiry = cacheExpiry(decodeTokenClaims(it))
            if (cacheExpiry > Date().time) {
                return it
            }
        }
        try {
            bearerToken = oauthClient.login()
        } catch (e: RuntimeException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
        return bearerToken!!
    }

    private fun getOkHttpClient(): OkHttpClient {
        okHttpClient?.let {
            return it
        }
        return OkHttpClient().newBuilder()
            .connectTimeout(requestTimeout, TimeUnit.SECONDS)
            .readTimeout(requestTimeout, TimeUnit.SECONDS)
            .writeTimeout(requestTimeout, TimeUnit.SECONDS)
            .build()
    }

    private fun cacheExpiry(claims: Claims): Long {
        return claims.expiration.time - OAUTH_GRACE_PERIOD
    }

    private fun decodeTokenClaims(token: String): Claims {
        val splitToken = token.split("\\.".toRegex())
        val unsignedToken = splitToken[0] + "." + splitToken[1] + "."
        val jwt = Jwts.parser().parse(unsignedToken)
        return jwt.body as Claims
    }

    private fun configureRequestTimeout() {
        val requestTimeout = PropertyHelper.getEnvOrPropertyValueAsLong("REQUEST_TIMEOUT")
        if (requestTimeout > 30) {
            this.requestTimeout = requestTimeout
        }
    }
}
