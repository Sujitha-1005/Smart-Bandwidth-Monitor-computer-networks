# 🚀 Smart Bandwidth Monitor
A Java-based **client–server bandwidth monitoring system** providing real-time network speed tracking, latency measurement, traffic analysis, and dynamic visualization using **Java Swing**, **Multithreading**, and **Java Sockets**.

---

## 📁 Project Structure
```
Smart Bandwidth Monitor/
├── BandwidthMonitorClient.java
├── BandwidthMonitorServer.java
├── NetworkData.java
├── NetworkInfo.java
├── NetworkMonitor.java
└── README.md
```

---

## ✨ Features

### 🔍 Real-Time Monitoring
- Live upload & download speed tracking  
- Dual-axis dynamic graphs  
- 60-second rolling window  
- Cumulative data tracking  
- Network interface selection  

### 🔗 Client–Server Architecture
- Pure **Java Sockets** (TCP)  
- Server streams bandwidth data  
- Supports multiple concurrent clients  

### ⚡ Multithreaded Traffic Analysis
- Latency measurement (<100ms)  
- Packet loss detection  
- Public IP & ISP data  
- Concurrent data transfer metrics  

### 🎨 Java Swing Visualization
- Responsive UI dashboard  
- Auto-scaling charts  
- Color-coded performance overlays  

---

## 🛠 Tech Stack
- Java  
- Java Swing  
- Java Sockets  
- Multithreading  
- Java Networking API  

---

## 📦 Prerequisites
- Java **JDK 8 or higher**  
- Network connectivity between server & client machines  

---

## 🧪 Compilation
Compile all Java files:

```bash
javac BandwidthMonitorServer.java NetworkData.java NetworkMonitor.java BandwidthMonitorClient.java NetworkInfo.java
```

---

## 🚀 Running the Application

### 1️⃣ Start Server
```bash
java BandwidthMonitorServer
```

### 2️⃣ Start Client
```bash
java BandwidthMonitorClient
```

---

## 📌 Usage Guide
- Start server on host machine  
- Start client (same or different machine)  
- Client auto-connects  
- View:  
  - Live bandwidth graphs  
  - Upload/Download speeds  
  - Latency & packet loss  
  - Interface stats  
  - Cumulative data  

---

## 🏗 Architecture Overview

### 📡 BandwidthMonitorServer
Handles client connections & streams network data.

### 🖥 BandwidthMonitorClient
Java Swing UI that visualizes all real-time data.

### ⚙ NetworkMonitor
Measures speeds, latency, packet loss, etc.

### 📊 NetworkData
Model for transmitting structured network statistics.

### 🧩 NetworkInfo
Fetches IP address, ISP, and available interfaces.

---
## 🤝 Contributing

Contributions, enhancements, and suggestions are welcome.  
Feel free to open a pull request or an issue.

---

## 📜 License

This project is open-source under the **MIT License**.
