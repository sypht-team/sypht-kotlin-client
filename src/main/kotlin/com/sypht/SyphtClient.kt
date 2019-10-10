package com.sypht

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import java.util.*

/**
 * Connect to the Sypht API at https://api.sypht.com
 */
open class SyphtClient() {
    private var bearerToken: String? = null
    private var oauthClient: OAuthClient
    private var OAUTH_GRACE_PERIOD = 1000 * 60 * 10

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
