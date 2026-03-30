package me.csystems.gategarage.cli

import com.google.gson.Gson
import me.csystems.gategarage.core.palgate.PalGateService
import java.io.File

data class GateConfig(
    val sessionToken: String,   // hex string, 32 chars
    val phoneNumber: String,    // international format, e.g. "972501234567"
    val tokenType: Int,         // 0=SMS, 1=Primary, 2=Secondary
    val deviceId: String        // e.g. "abc123" or "abc123:2"
)

fun main(args: Array<String>) {
    val debug = "--debug" in args
    val configPath = args.firstOrNull { !it.startsWith("--") } ?: "gate_config.json"
    val configFile = File(configPath)

    if (!configFile.exists()) {
        System.err.println("Config file not found: ${configFile.absolutePath}")
        System.err.println("Create it based on gate_config.example.json")
        System.exit(1)
    }

    val config = runCatching {
        Gson().fromJson(configFile.readText(), GateConfig::class.java)
    }.getOrElse {
        System.err.println("Failed to parse config: ${it.message}")
        System.exit(1)
        return
    }

    val sessionToken = runCatching {
        require(config.sessionToken.length == 32) { "sessionToken must be 32 hex characters" }
        config.sessionToken.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }.getOrElse {
        System.err.println("Invalid sessionToken: ${it.message}")
        System.exit(1)
        return
    }

    val phoneNumber = config.phoneNumber.toLongOrNull() ?: run {
        System.err.println("Invalid phoneNumber: must be numeric")
        System.exit(1)
        return
    }

    println("Opening gate...")
    val service = PalGateService(
        sessionToken = sessionToken,
        phoneNumber  = phoneNumber,
        tokenType    = config.tokenType,
        deviceId     = config.deviceId,
        debugLogging = debug
    )

    service.openGate().fold(
        onSuccess = { println("Gate opened successfully.") },
        onFailure = {
            System.err.println("Failed: ${it.message}")
            System.exit(1)
        }
    )
}
