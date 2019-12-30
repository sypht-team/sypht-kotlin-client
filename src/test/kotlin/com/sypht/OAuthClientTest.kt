package com.sypht

import com.sypht.auth.BasicCredentialProvider
import com.sypht.auth.EnvironmentVariableCredentialProvider
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
    val environmentVariables = EnvironmentVariables()
    @Rule @JvmField
    val exceptionRule = ExpectedException.none();

    /**
     * Test the OAuth Login
     */
    @Test
    fun loginUsingEnvironmentVariables() {
        val client = OAuthClient(credentialProvider = EnvironmentVariableCredentialProvider())
        val token = client.login()

        TestCase.assertTrue("doesn't look like a JWT Token", token.startsWith("eyJ0"))
    }

    @Test
    fun testBasicCredentials() {
        val cp = BasicCredentialProvider("abc", "def")
        var client = OAuthClient(credentialProvider = cp)

        TestCase.assertEquals("Client ID does not match", "abc", getFieldByNameUsingReflection(client, "clientId"))
        TestCase.assertEquals("Client Secret does not match", "def", getFieldByNameUsingReflection(client, "clientSecret"))
        TestCase.assertNotNull("Audience is null", getFieldByNameUsingReflection(client, "oauthAudience"))
        TestCase.assertFalse("Audience is empty", getFieldByNameUsingReflection(client, "oauthAudience") == "")

        cp.oauthAudience = "ghi"
        client = OAuthClient(credentialProvider = cp)

        TestCase.assertEquals("Audience does not match", "ghi", getFieldByNameUsingReflection(client, "oauthAudience"))
    }

    @Test
    fun testEnvironmentVariableCredentialProvider() {
        environmentVariables.set("OAUTH_CLIENT_ID", "invalidClientId")
        environmentVariables.set("OAUTH_CLIENT_SECRET", "invalidClientSecret")
        environmentVariables.set("OAUTH_AUDIENCE", "invalidAudience")
        val client = OAuthClient(credentialProvider = EnvironmentVariableCredentialProvider())

        TestCase.assertEquals("Client ID does not match", "invalidClientId", getFieldByNameUsingReflection(client, "clientId"))
        TestCase.assertEquals("Client Secret does not match", "invalidClientSecret", getFieldByNameUsingReflection(client, "clientSecret"))
        TestCase.assertEquals("Client Secret does not match", "invalidAudience", getFieldByNameUsingReflection(client, "oauthAudience"))
    }

    private fun getFieldByNameUsingReflection(obj: Any, fieldName: String): Any? {
        val f = obj.javaClass.declaredFields.first { it.name == fieldName}
        f.isAccessible = true
        return f.get(obj)
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
        val client = OAuthClient(credentialProvider = EnvironmentVariableCredentialProvider())
        client.login()
    }
}