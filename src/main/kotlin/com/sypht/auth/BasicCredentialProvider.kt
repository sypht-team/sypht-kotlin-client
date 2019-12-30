package com.sypht.auth

class BasicCredentialProvider(override val clientId: String, override val clientSecret: String) : AbsCredentialProvider()
