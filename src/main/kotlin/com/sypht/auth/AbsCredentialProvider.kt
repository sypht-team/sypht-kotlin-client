package com.sypht.auth

abstract class AbsCredentialProvider: ICredentialProvider {
    private var userSetAudience: String = ""
    override var oauthAudience: String
        get() = if (userSetAudience.isNullOrEmpty()) "https://api.sypht.com" else userSetAudience
        set(audience) {
            userSetAudience = audience
        }
}