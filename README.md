### Introduction
The Android Socket Project is a robust Android application that enables real-time, full-duplex communication between devices using both WebSocket and TCP protocols. The project leverages Android flavors to separate server-side and client-side implementations, providing flexibility and modularity in code. Utilizing Android foreground services, it maintains persistent connections and supports file transfer, enabling the exchange of both text messages and files between devices. Whenever a message is received from a client by the server, the application displays it as a notification, ensuring users are informed of real-time interactions. Additionally, a Wi-Fi and Ethernet connection observer monitors network connectivity, ensuring stable communication across network interfaces.

### Features
- Foreground Service: Ensures persistent connections by running in the foreground, enhancing reliability for long-running communication tasks.
- Server and Client Implementations with Flavors: Separates server and client code using Android flavors, providing both functionalities for WebSocket and TCP protocols to enable real-time, full-duplex data exchange.
- Network Connectivity Monitoring: Actively monitors Wi-Fi and Ethernet connections, ensuring stable communication across various network interfaces.
- Dual Protocol Support: Establishes reliable WebSocket and TCP connections, allowing flexible communication options between devices.
- File Transfer Capability: Supports file exchange by allowing clients to send files to the server, which automatically saves them in the download folder for easy access.
- Real-Time Notifications: Displays incoming messages as notifications whenever the server receives messages from clients, keeping users informed of real-time interactions.
- Modern Android Stack: Utilizes Jetpack Compose for UI development and is fully implemented in Kotlin.
- BroadcastReceiver Integration: Utilizes BroadcastReceiver to respond to specific system or app events, enhancing responsiveness to connectivity and other real-time changes.

###  demo
<img src="/android_socket.gif" width="500"  height="560"/>
