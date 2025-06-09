# PhotonStream Protocol (Part 3: Implementation)

**Author:** Cas Wang  
**Course:** CS544 - Computer Networks  
**Professor:** Brian Mitchell  
**Project:** QUIC-based Custom Protocol Implementation --PhotonStream
**Version:** 1.0-SNAPSHOT

---

## 📦 Overview

PhotonStream is a stateful, application-layer protocol implemented over the QUIC transport protocol using
the [Kwik](https://github.com/ptrd/kwik) Java library. It is designed for high-quality on-demand audiovisual streaming
and features multi-track synchronization, structured session negotiation, and future extensibility.

This implementation focuses on the `INIT_REQUEST`, `INIT_RESPONSE`, and `END_SESSION` messages and validates state
transitions via a defined DFA.

---

## 🖥️ How to Compile

Make sure you have **Java 21+** and **Maven 3+** installed.

```bash
mvn clean package
```

This will produce:

```
target/Photon-Quic-1.0-SNAPSHOT.jar
```

---

## 🚀 How to Run

### ✅ 1. Generate Self-Signed Certificates

If you haven't already:

```bash
openssl req -x509 -newkey rsa:2048 -nodes -keyout server-key.pem -out server-cert.pem -days 365
```

Place both `.pem` files in the root directory.

### ✅ 2. Start the Server

```bash
java -cp target/Photon-Quic-1.0-SNAPSHOT.jar server.PhotonStreamServer
```

Expected output:

```
PhotonStream QUIC Server started on port 9090
```

### ✅ 3. Start the Client

```bash
java -cp target/Photon-Quic-1.0-SNAPSHOT.jar client.PhotonStreamClient 127.0.0.1 9090
```

Expected output:

```
Session ID: 56789
Sent END_SESSION
```

---

## 📜 Protocol Description

PhotonStream uses QUIC bidirectional streams to deliver the following messages:

### Supported PDUs

| ID   | Message       | Direction       |
|------|---------------|-----------------|
| 0x01 | INIT_REQUEST  | Client → Server |
| 0x02 | INIT_RESPONSE | Server → Client |
| 0x08 | END_SESSION   | Client → Server |
| 0x09 | ERROR         | Server → Client |

Each message is defined in `protocol/` using structured binary formats with big-endian encoding.

---

## 🔁 DFA (Deterministic Finite Automaton)

| State        | Input         | Next State   |
|--------------|---------------|--------------|
| IDLE         | INIT_REQUEST  | SESSION_INIT |
| SESSION_INIT | INIT_RESPONSE | STREAMING    |
| STREAMING    | END_SESSION   | SESSION_END  |

Incorrect transitions are rejected, and the server logs "Unexpected message or state."

---

## 🔐 Security

- QUIC connections require TLS 1.3 (handled by Kwik).
- The server uses a self-signed certificate (`server-cert.pem`, `server-key.pem`).
- The client disables certificate validation (`noServerCertificateCheck()`) for development purposes.
- Authentication token (`authToken`) is included in INIT_REQUEST (e.g., "admin123").

---

## 🔧 Configuration

All configuration is runtime-based via command line or code constants. No files are hardcoded.

### Server

- Listens on **port 9090**
- Accepts **PhotonStream** ALPN
- Requires cert and key files

### Client

- Takes two arguments: host + port

---

## 📁 File Structure

```
src/
├── client/
│   └── PhotonStreamClient.java
├── server/
│   └── PhotonStreamServer.java
├── protocol/
│   ├── InitRequest.java
│   ├── InitResponse.java
│   └── PDUType.java
├── shared/
│   ├── DFAValidator.java
│   └── State.java
```

---

## ➕ Optional Extensions (Not implemented but designed for)

- MEDIA_SEGMENT message (video/audio/metadata)
- SEEK, PAUSE, RESUME support
- Feature negotiation (bitrate switching, live streaming)
- Session resumption
- OAuth2 or SSO authentication

---

## ⚠️ Limitations

- Only INIT, RESPONSE, and END are implemented.
- Certificate validation is bypassed for demo purposes.
- Media is not streamed — placeholder only.

---

## 🏁 Submission Notes

- ✅ DFA logic is implemented and validated
- ✅ QUIC transport is properly used (via Kwik)
- ✅ All source code is documented and modular
- ✅ Self-contained and runnable on Linux, macOS, or WSL
- ✅ No external configuration required
- ✅ All protocol messages are custom-built (no 3rd party parsing)

---

## 📎 License Note

Kwik is LGPL-licensed. See: https://github.com/ptrd/kwik
