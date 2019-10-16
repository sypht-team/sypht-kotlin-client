package com.sypht

import com.sypht.helper.Constants
import com.sypht.helper.PropertyHelper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Log-in to the Sypht API
 */
class OAuthClient(val requestTimeout: Long = 30) {
    private var clientId: String? = null
    private var clientSecret: String? = null
    private var oauthAudience: String? = null
    private var log = Logger.getLogger("com.sypht.OAuthClient")

    /**
     * Create a default OAuthClient. Requires OAUTH_CLIENT_ID and OAUTH_CLIENT_SECRET
     * set as environment variables.
     */
    init {
        clientId = PropertyHelper.getEnvOrProperty("OAUTH_CLIENT_ID")
        clientSecret = PropertyHelper.getEnvOrProperty("OAUTH_CLIENT_SECRET")
        if (clientId == null && clientSecret == null) {
            val syphtApiKey = PropertyHelper.getEnvOrProperty("SYPHT_API_KEY")
            if (syphtApiKey != null) {
                clientId = syphtApiKey.split(":")[0]
                clientSecret = syphtApiKey.split(":")[1]
            }
        }
        if (clientId == null || clientSecret == null) {
            throw RuntimeException("SYPHT_API_KEY -OR- OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET environment" + " variables must be set before running this process, exiting")
        }
        oauthAudience = PropertyHelper.getEnvOrProperty("OAUTH_AUDIENCE")
        if (oauthAudience == null) {
            oauthAudience = "https://api.sypht.com"
        }
    }

    /**
     * Get a JWT bearer token for use with the Sypht API in exchange for your
     * client id and secret.
     *
     * @return a bearer token as a String.
     * @throws IOException when api execution fails.
     * @throws IllegalStateException when http response code is outside 200...299 or the response body is null.
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun login(): String {
        val client = OkHttpClient().newBuilder()
                .connectTimeout(requestTimeout, TimeUnit.SECONDS)
                .readTimeout(requestTimeout, TimeUnit.SECONDS)
                .writeTimeout(requestTimeout, TimeUnit.SECONDS)
                .build()
        val json = "{\"client_id\":\"$clientId\",\"client_secret\":\"$clientSecret\",\"audience\":\"$oauthAudience\",\"grant_type\":\"client_credentials\"}"
        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
        val request = Request.Builder()
                .url(Constants.SYPHT_AUTH_ENDPOINT)
                .post(requestBody)
                .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful || response.body() == null) {
            throw IllegalStateException("Client didn't respond with appropriate result")
        }
        val jsonObject = JSONObject(response.body()!!.string())
        log.info("successfully logged into Sypht for clientId " + clientId)
        return jsonObject.getString("access_token")
    }
}
