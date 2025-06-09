# PhotonStream Protocol (Part 3: Implementation)

**Author:** Cas Wang  
**Course:** CS544 - Computer Networks  
**Professor:** Brian Mitchell  
**Project:** QUIC-based Custom Protocol Implementation  
**Version:** 1.0-SNAPSHOT

---

## 📦 Overview

PhotonStream is a stateful, application-layer protocol implemented over the QUIC transport protocol using
the [Kwik](https://github.com/ptrd/kwik) Java library. It is designed for high-quality on-demand audiovisual streaming
and features multi-track synchronization, structured session negotiation, and future extensibility.

This implementation focuses on the `INIT_REQUEST`, `INIT_RESPONSE`, and `END_SESSION` messages and validates state
transitions via a defined DFA.

> 🔀 **Branching Notice**: This repository has two branches:
> - `main`: always contains the **latest and most stable** implementation ✅
> - `dev`: used for ongoing development, may be unstable 🧪

---

## 🛠️ Project Build Details

When built using Maven, this project generates a **standalone UBER JAR** using the Maven Shade Plugin. This JAR contains
all dependencies (including Kwik), so it can be run without any additional setup.

```
target/Photon-Quic-1.0-SNAPSHOT-shaded.jar
```

---

## 🖥️ How to Compile

Make sure you have **Java 21+** and **Maven 3+** installed.

```bash
mvn clean package
```

This will generate the fat JAR:

```
target/Photon-Quic-1.0-SNAPSHOT-shaded.jar
```

---

## 🧰 How to Install Maven on a Linux-Based System

If Maven is not installed, you can install it using your system package manager:

### For Debian/Ubuntu:

```bash
sudo apt update
sudo apt install maven
```

### For Fedora/RHEL:

```bash
sudo dnf install maven
```

### Verify Installation:

```bash
mvn -version
```

You should see output confirming the Maven version installed.

---

## 🚀 How to Run

### ✅ 1. Generate Required TLS Certificates (**MANDATORY**)

Before starting the server, you **must** generate two self-signed TLS certificates. Without these, the QUIC server will
not start.

```bash
openssl req -x509 -newkey rsa:2048 -nodes -keyout server-key.pem -out server-cert.pem -days 365
```

Both `server-cert.pem` and `server-key.pem` must be placed in the **project root** directory.

---

### ✅ 2. Run Scripts

This project includes two helpful scripts to launch the server and client.

#### ▶️ Start the Server

```bash
./run-server.sh
```

Expected output:

```
PhotonStream QUIC Server started on port 9090
```

#### ▶️ Start the Client

```bash
./run-client.sh 127.0.0.1 9090
```

Expected output:

```
Session ID: 56789
Sent END_SESSION
```

> ℹ️ Make sure to `chmod +x run-server.sh run-client.sh` the first time to make the scripts executable.

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
- The server uses a **self-signed certificate**: `server-cert.pem` and `server-key.pem`.
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
run-server.sh
run-client.sh
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
- ✅ Self-contained UBER JAR built with Shade Plugin
- ✅ No external configuration required beyond the certs

---

## 📎 License Note

Kwik is LGPL-licensed. See: https://github.com/ptrd/kwik
