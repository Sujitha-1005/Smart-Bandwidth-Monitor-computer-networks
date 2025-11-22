# Smart Bandwidth Monitor

A Java-based client-server application for real-time network bandwidth monitoring and visualization.

## Project Structure
Smart Bandwidth Monitor/
├── BandwidthMonitorClient.java
├── BandwidthMonitorServer.java
├── NetworkData.java
├── NetworkInfo.java
├── NetworkMonitor.java
└── README.md

text

## Features

- Real-time monitoring of network bandwidth
- Client-server architecture for network data collection
- Visualization of network usage through bandwidth graphs
- Scalable to multiple clients connecting to the server

## Prerequisites

- Java JDK 8 or higher
- Network connectivity for client-server communication

## Compilation

Compile all Java files using the following command:


javac BandwidthMonitorServer.java NetworkData.java NetworkMonitor.java BandwidthMonitorClient.java NetworkInfo.java
Running the Application
Start the Server
java BandwidthMonitorServer
Start the Client
bash
java BandwidthMonitorClient
Usage
First, start the BandwidthMonitorServer on the machine you want to use as the server

Then run BandwidthMonitorClient on the same or different machines

The client will connect to the server and begin monitoring network bandwidth

View real-time bandwidth graphs and statistics through the client interface

Architecture
BandwidthMonitorServer: Main server class that handles client connections

BandwidthMonitorClient: Client application with GUI for bandwidth visualization

NetworkMonitor: Core monitoring functionality for network data collection

NetworkData: Data structure for storing network statistics

NetworkInfo: Utility class for network information retrieval

Client-Server Communication
The application uses a client-server model where:

Server collects and processes network data

Clients connect to the server to receive real-time bandwidth information
