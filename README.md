### Introduction

The Android Socket Project is a robust Android application that utilizes foreground services and the Java-WebSocket library to establish a WebSocket connection. It features both server-side and client-side implementations using the WebSocket protocol, enabling real-time, full-duplex communication between devices.  Additionally, the project includes a Wi-Fi and Ethernet connection observer that monitors network connectivity, ensuring stable and reliable communication over different network interfaces. Furthermore, whenever a message is received from the client by the server, the application displays it as a notification, keeping users informed of real-time interactions.


### Features
- Foreground Service:  Ensures the socket remains active even when the app is in the background.
- Java-WebSocket Integration: Utilizes org.java-websocket:Java-WebSocket for reliable socket communication.
- flavor (The server and client sides are separated using flavors in Android)
- jetpack compose
- kotlin
- notification
- broadcastReceiver

###  demo
<img src="/my_android_socket_demo.gif" width="500"  height="560"/>
