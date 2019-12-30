package com.sypht.auth

import com.sypht.helper.PropertyHelper

class EnvironmentVariableCredentialProvider : AbsCredentialProvider() {
    init {
        this.oauthAudience = PropertyHelper.getEnvOrProperty("OAUTH_AUDIENCE") ?: ""
    }
    override val clientId: String
        get() = PropertyHelper.getEnvOrProperty("OAUTH_CLIENT_ID")
                ?: PropertyHelper.getEnvOrProperty("SYPHT_API_KEY")?.split(':')?.get(0).toString() ?: ""
    override val clientSecret: String
        get() = PropertyHelper.getEnvOrProperty("OAUTH_CLIENT_SECRET")
                ?: PropertyHelper.getEnvOrProperty("SYPHT_API_KEY")?.split(':')?.get(1).toString() ?: ""
}