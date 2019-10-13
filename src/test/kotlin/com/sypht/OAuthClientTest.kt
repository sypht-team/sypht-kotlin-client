package com.sypht

import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.rules.ExpectedException

/**
 * @author Gbolahan Kuti
 */
class OAuthClientTest {
    @Rule @JvmField
    final val environmentVariables = EnvironmentVariables()
    @Rule @JvmField
    final val exceptionRule = ExpectedException.none();

    /**
     * Test the OAuth Login
     */
    @Test
    fun login() {
        val client = OAuthClient()
        val token = client.login()
        TestCase.assertTrue("doesn't look like a JWT Token", token.startsWith("eyJ0"))

    }

    /**
     * Test the OAuth Login failure with invalid OAUTH_CLIENT_ID and OAUTH_CLIENT_SECRET
     */
    @Test
    fun loginFail() {
        exceptionRule.expect(IllegalStateException::class.java)
        exceptionRule.expectMessage("Client didn't respond with appropriate result")
        environmentVariables.set("OAUTH_CLIENT_ID", "invalidClientId")
        environmentVariables.set("OAUTH_CLIENT_SECRET", "invalidClientSecret")
        val client = OAuthClient()
        client.login()
    }
}