# PalAndGate

Android app and JVM CLI for opening a [PalGate](https://www.pal-es.com/) barrier via the PalGate Cloud API.

The token generation algorithm is ported from [pylgate](https://github.com/DonutByte/pylgate) by DonutByte.

---

## Modules

| Module | Description |
|--------|-------------|
| `core` | Pure JVM/Kotlin. Token generator and HTTP service. No Android dependencies. |
| `app`  | Android MVVM app. One-tap gate opener with encrypted credential storage. |
| `cli`  | JVM command-line tool. Reads credentials from a JSON file. |

---

## Requirements

- Java 21
- Kotlin 2.1.21
- Gradle 9.4.1 (via wrapper — no separate install needed)
- Android SDK 35, build-tools 35.0.0 *(app module only)*
- Android 8.0+ on device (min SDK 26)

---

## Build

```bash
# Android APK
./gradlew :app:assembleDebug        # debug
./gradlew :app:assembleRelease      # release (ProGuard minified)

# CLI fat-jar / run
./gradlew :cli:run -q               # run directly (reads gate_config.json)
./gradlew :cli:run --args="--debug" -q   # with HTTP debug logging

# Core unit tests
./gradlew :core:test
```

---

## Credentials

You need three values from the PalGate mobile app:

| Field | Description |
|-------|-------------|
| `sessionToken` | 32 hex characters obtained during device linking |
| `phoneNumber` | Full international number, digits only (e.g. `972501234567`) |
| `tokenType` | `0` = SMS, `1` = Primary (device linking), `2` = Secondary |
| `deviceId` | Device identifier from the PalGate app. Append `:N` to target output N (e.g. `abc123:2`); defaults to output 1. |

### Android app

Enter credentials in **Settings** (⚙ icon). All values are stored encrypted via Android Keystore (AES-256-GCM).

### CLI

Create `gate_config.json` in the project root (gitignored):

```json
{
  "sessionToken": "your32hexchartoken000000000000ab",
  "phoneNumber": "972501234567",
  "tokenType": 1,
  "deviceId": "your-device-id"
}
```

Then run:

```bash
./gradlew :cli:run -q
```

---

## Architecture

```
app/
  MainActivity       — single-screen UI, observes GateViewModel
  GateViewModel      — launches openGate() on IO dispatcher
  CredentialsStore   — AES-256-GCM encrypted SharedPreferences via Jetpack Security
  SettingsActivity   — credential entry form

core/
  PalGateService         — sends GET /v1/bt/device/{id}/open-gate with X-Bt-Token header
  PalGateTokenGenerator  — custom AES-like cipher producing a 46-hex-char token (5 s TTL)

cli/
  Main.kt            — reads gate_config.json, calls PalGateService, exits 0/1
```

### Token algorithm

`Token = [type_byte (1 B) | phone_bytes (6 B) | cipher_payload (16 B)]` → 46 uppercase hex chars.

Two-pass derivation using a hardcoded key: first pass embeds the phone number;
second pass enciphers a timestamp block. The cipher is a non-standard AES
variant from pylgate — **do not** replace it with `javax.crypto.Cipher`.

Tokens have a ~5 second TTL and must be regenerated per request.

---

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).

The token algorithm is derived from [pylgate](https://github.com/DonutByte/pylgate) © DonutByte, also GPLv3.
