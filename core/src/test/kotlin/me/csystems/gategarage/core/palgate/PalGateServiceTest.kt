package me.csystems.gategarage.core.palgate

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PalGateServiceTest {

    private val server = MockWebServer()

    private fun service(deviceId: String = "abc123") = PalGateService(
        sessionToken = ByteArray(16) { it.toByte() },
        phoneNumber  = 972501234567L,
        tokenType    = 1,
        deviceId     = deviceId,
        baseUrl      = server.url("").toString().trimEnd('/')
    )

    @Before fun start() = server.start()
    @After  fun stop()  = server.shutdown()

    // ── success / failure ────────────────────────────────────────────────────

    @Test
    fun `openGate succeeds on 200 response`() {
        server.enqueue(MockResponse().setResponseCode(200))

        val result = service().openGate()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `openGate fails on non-200 response`() {
        server.enqueue(MockResponse().setResponseCode(403).setBody("Forbidden"))

        val result = service().openGate()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("403"))
    }

    @Test
    fun `openGate fails on 500 response`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        val result = service().openGate()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("500"))
    }

    // ── request shape ────────────────────────────────────────────────────────

    @Test
    fun `X-Bt-Token header is 46 uppercase hex characters`() {
        server.enqueue(MockResponse().setResponseCode(200))

        service().openGate()

        val req = server.takeRequest()
        val token = req.getHeader("X-Bt-Token")!!
        assertEquals(46, token.length)
        assertTrue("Must be uppercase hex", token.all { it.isDigit() || it in 'A'..'F' })
    }

    @Test
    fun `URL contains device ID`() {
        server.enqueue(MockResponse().setResponseCode(200))

        service(deviceId = "abc123").openGate()

        val req = server.takeRequest()
        assertTrue(req.path!!.contains("abc123"))
    }

    @Test
    fun `device ID without colon uses outputNum 1`() {
        server.enqueue(MockResponse().setResponseCode(200))

        service(deviceId = "abc123").openGate()

        val req = server.takeRequest()
        assertTrue(req.path!!.contains("outputNum=1"))
    }

    @Test
    fun `device ID with colon suffix uses specified output number`() {
        server.enqueue(MockResponse().setResponseCode(200))

        service(deviceId = "abc123:2").openGate()

        val req = server.takeRequest()
        assertTrue(req.path!!.contains("abc123"))
        assertTrue(req.path!!.contains("outputNum=2"))
        assertFalse(req.path!!.contains("abc123:2"))
    }

    @Test
    fun `URL contains open-gate path`() {
        server.enqueue(MockResponse().setResponseCode(200))

        service().openGate()

        val req = server.takeRequest()
        assertTrue(req.path!!.contains("/v1/bt/device/"))
        assertTrue(req.path!!.contains("/open-gate"))
    }
}
