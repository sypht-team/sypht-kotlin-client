package com.sypht

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import okhttp3.*
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
open class SyphtClient() {
    private var bearerToken: String? = null
    private var oauthClient: OAuthClient
    private val OAUTH_GRACE_PERIOD = 1000 * 60 * 10
    private val REQUEST_TIMEOUT: Long = 30
    protected var log = Logger.getLogger("com.sypht.OAuthClient")


    /**
     * Create a default Sypht client that manages bearer tokens automatically.
     */
    init {
        oauthClient = OAuthClient()
    }

    /**
     * Create a custom Sypht client with your own bearer token.
     * @param bearerToken the Jwt token
     */
    constructor(bearerToken: String) : this() {
        this.bearerToken = bearerToken
    }

    /**
     * Pass a file to Sypht for detection.
     * @param file the file in pdf, jpeg, gif or png format. Files may be up to
     *             20MB in size and pdf files may contain up to 16 individual pages.
     * @param fieldSetOptions pass in custom upload options here.
     * @param requestTimeout pass in custom http request timeout default is 30seconds.
     * @return a fileId as a String.
     * @throws IOException in the event the upload went wrong.
     * @throws IllegalStateException when http response code is outside 200...299 or the response body is null.
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun upload(file: File, fieldSetOptions: Map<String, String>? = null, requestTimeout: Long = REQUEST_TIMEOUT): String {
        val builder = buildMultipartBodyUploadWithFile(file)
        fieldSetOptions?.let {
            addFieldSets(it, builder)
        }
        return performUpload(builder, if (requestTimeout < 30) REQUEST_TIMEOUT else requestTimeout)
    }

    /**
     * Pass a file to Sypht for detection.

     * @param fileName the file name
     * @param inputStream binary input stream of pdf, jpeg, gif or png format. Files may be up to
     *                    20MB in size and pdf files may contain up to 16 individual pages.
     * @param fieldSetOptions pass in custom upload options here.
     * @param requestTimeout pass in custom http request timeout default is 30seconds.
     * @return a fileId as a String.
     * @throws IOException in the event the upload went wrong.
     * @throws IllegalStateException when http response code is outside 200...299 or the response body is null.
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun upload(fileName: String, inputStream: InputStream, fieldSetOptions: Map<String, String>? = null,
               requestTimeout: Long = REQUEST_TIMEOUT): String {
        val builder = buildMultipartBodyUploadWithInputStream(fileName,inputStream)
        fieldSetOptions?.let {
            addFieldSets(it, builder)
        }
        return performUpload(builder, if (requestTimeout < 30) REQUEST_TIMEOUT else requestTimeout)
    }

    protected fun buildMultipartBodyUploadWithFile(file: File): MultipartBody.Builder {
        val formBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileToUpload", file.name,
                        RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), file))
        return formBody
    }

    protected fun buildMultipartBodyUploadWithInputStream(fileName: String, inputStream: InputStream): MultipartBody.Builder {
        val formBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileToUpload", fileName,
                        InputStreamRequestBody(MediaType.parse("application/x-www-form-urlencoded"), inputStream))
        return formBody
    }

    @Throws(IOException::class, IllegalStateException::class)
    protected fun performUpload(builder: MultipartBody.Builder, timeout: Long): String {
        val client = OkHttpClient().newBuilder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build()

        val formBody = builder.build()
        val request = createAuthorizedHttpRequest(Constants.SYPHT_API_ENDPOINT + "/fileupload")
                .post(formBody)
                .build();

        val response = client.newCall(request).execute();
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("sypht upload failed")
        }
        val jsonObject = JSONObject(response.body()!!.string())
        val fileId = jsonObject.getString("fileId")
        log.info("sypht file upload successful, fileId " + fileId)
        return fileId
    }

    protected fun addFieldSets(fieldSetOptions: Map<String, String>, multipartBody: MultipartBody.Builder) {
        fieldSetOptions.forEach {
            multipartBody.addFormDataPart(it.key, it.value)
        }
    }

    private fun createAuthorizedHttpRequest(url: String): Request.Builder {
        return Request.Builder().url(url)
                .addHeader("Accepts", "application/json")
                .addHeader("Authorization", "Bearer " + getBearerToken())
    }

    protected @Synchronized fun getBearerToken(): String {
        bearerToken?.let {
            val cacheExpiry = cacheExpiry(decodeTokenClaims(it))
            if (cacheExpiry > Date().time) {
                return it
            }
        }
        try {
            bearerToken = oauthClient.login()
        } catch(e: RuntimeException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
        return bearerToken!!
    }

    protected fun cacheExpiry(claims: Claims): Long {
        return claims.expiration.time - OAUTH_GRACE_PERIOD
    }

    protected fun decodeTokenClaims(token: String): Claims {
        val splitToken = token.split("\\.")
        val unsignedToken = splitToken[0] + "." + splitToken[1] + "."

        val jwt = Jwts.parser().parse(unsignedToken)
        val claims = jwt.body as Claims
        return claims
    }
}
