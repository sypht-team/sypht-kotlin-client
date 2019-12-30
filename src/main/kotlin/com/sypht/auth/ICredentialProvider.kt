package com.sypht.auth

/**
 * @author
 */
interface ICredentialProvider {
    val clientId: String
    val clientSecret: String
    var oauthAudience: String
}