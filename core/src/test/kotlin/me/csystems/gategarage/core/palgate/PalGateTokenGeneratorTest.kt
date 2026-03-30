package me.csystems.gategarage.core.palgate

import org.junit.Assert.*
import org.junit.Test

/**
 * Cross-validation against pylgate Python output.
 *
 * To get the reference value, run in your terminal:
 *   python3 -c "
 *   from pylgate.token_generator import generate_token
 *   print(generate_token(session_token=bytes(16), phone_number=972501234567, token_type=1, timestamp_ms=1700000000))
 *   "
 * Then paste the result into PYLGATE_EXPECTED and uncomment the test.
 */
class PalGateTokenGeneratorTest {

    @Test
    fun `output is 46 uppercase hex characters`() {
        val token = token()
        assertEquals(46, token.length)
        assertTrue("Must be uppercase hex", token.all { it.isDigit() || it in 'A'..'F' })
    }

    @Test fun `first byte is 01 for SMS`()       { assertEquals("01", token(type = 0).take(2)) }
    @Test fun `first byte is 11 for PRIMARY`()   { assertEquals("11", token(type = 1).take(2)) }
    @Test fun `first byte is 21 for SECONDARY`() { assertEquals("21", token(type = 2).take(2)) }

    @Test
    fun `bytes 1 to 6 encode phone number correctly`() {
        val phone = 972501234567L
        val tokenBytes = token(phone = phone).chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val phoneBeBytes = ByteArray(8) { i -> (phone ushr ((7 - i) * 8)).toByte() }
        for (i in 0..5) assertEquals("Phone byte $i", phoneBeBytes[i + 2], tokenBytes[i + 1])
    }

    @Test
    fun `different timestamps produce different tokens`() {
        assertNotEquals(token(ts = 1700000000L), token(ts = 1700000060L))
    }

    @Test
    fun `different session tokens produce different tokens`() {
        assertNotEquals(
            token(session = ByteArray(16) { 0x00 }),
            token(session = ByteArray(16) { 0xff.toByte() })
        )
    }

    // @Test
    // fun `matches pylgate output for known inputs`() {
    //     val PYLGATE_EXPECTED = "1100E26D97338757588A72386BA26C9B1CC2A3D372CFA6"
    //     assertEquals(PYLGATE_EXPECTED, token(session = ByteArray(16), phone = 972501234567L, type = 1, ts = 1700000000L))
    // }

    private fun token(
        session: ByteArray = ByteArray(16) { it.toByte() },
        phone: Long        = 972501234567L,
        type: Int          = 1,
        ts: Long           = 1700000000L
    ) = PalGateTokenGenerator.generate(session, phone, type, ts)
}
