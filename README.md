# NFC Security & Privacy Guardian 🛡️

An advanced, offline-first Android security suite focused on NFC threat detection, privacy-preserving network filtering, and hardware-backed data encryption. Developed as a high-integrity Proof of Concept (PoC).

---

## 🌟 Key Modules

### 📡 NFC Sentinel
Real-time monitoring of the NFC field to detect unauthorized tag interactions or potential skimmers.
- **Tech:** Foreground Dispatch System, NDEF parsing, and tag technology identification.
- **Goal:** Protect against relay attacks and rogue NFC tags.

### 🔐 Secure Vault
A military-grade encrypted storage for sensitive data (passwords, notes, keys).
- **Security:** AES-256-GCM encryption with keys stored in the **Android Keystore**.
- **Authentication:** Per-use **Biometric Authentication** (Fingerprint/Face) integrated via `CryptoObject`.
- **Privacy:** Data is never sent to the cloud; everything stays on-device.

### 🛡️ Malware & Integrity Scanner
Heuristic and signature-based scanning of installed applications and local files.
- **Heuristics:** Detects dangerous permission combinations (e.g., SMS + GPS) typical of stalkerware.
- **Signatures:** SHA-256 hash matching against documented malware families.
- **System Health:** Monitors Root status, ADB debugging, and boot persistence.

### 🌐 DNS-Filtering Tunnel (VPN)
A local VPN service that intercepts network traffic at the packet level to block trackers and malicious domains.
- **Logic:** Custom IPv4/UDP/DNS packet parser (RFC 1035).
- **Features:** NXDOMAIN response for malicious hits, upstream forwarding to 1.1.1.1 via protected sockets.
- **Zero-Latency:** No external server involved; filtering happens entirely on the CPU.

---

## 🏗️ Architecture & Tech Stack

This project follows **Clean Architecture** principles and **SOLID** patterns, ensuring high testability and modularity.

- **UI:** 100% Jetpack Compose with a custom high-contrast "Cyber" theme.
- **Concurrency:** Kotlin Coroutines & Flow (StateFlow/SharedFlow) for reactive state management.
- **Dependency Injection:** Hilt (Dagger) for modular decoupling.
- **Persistence:** Room (Local DB) and EncryptedSharedPreferences/DataStore.
- **Background Tasks:** WorkManager for periodic 15-minute security audits.
- **Lifecycle:** Modern ViewModel-driven UI state.

---

## 📸 Screenshots
| Dashboard | NFC Sentinel | VPN Tunnel | Vault |
|---|---|---|---|
| *[Add Image]* | *[Add Image]* | *[Add Image]* | *[Add Image]* |

---

## 🛠️ Installation & Build

1. Clone the repository:
   ```bash
   git clone https://github.com/andreaats2024-ai/app_clone.git
