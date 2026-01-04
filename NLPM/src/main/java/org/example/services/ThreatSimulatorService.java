package org.example.services;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Threat Simulator Service
 *
 * This service simulates suspicious network activity for testing the IDS system.
 * It listens on dangerous ports (trojan, backdoor ports) to trigger alerts.
 *
 * ⚠️ WARNING: Only use this for TESTING on your LOCAL machine!
 * ⚠️ Do NOT run this on production systems or public networks!
 */
public class ThreatSimulatorService {
    private static ThreatSimulatorService instance;
    private ExecutorService executorService;
    private List<ServerSocket> activeServers;
    private volatile boolean isRunning;

    // Dangerous ports to listen on (will trigger IDS alerts)
    private static final Map<Integer, String> DANGEROUS_PORTS = new LinkedHashMap<>();
    static {
        DANGEROUS_PORTS.put(1337, "Elite Trojan");
        DANGEROUS_PORTS.put(31337, "Back Orifice");
        DANGEROUS_PORTS.put(12345, "NetBus");
        DANGEROUS_PORTS.put(12346, "NetBus Pro");
        DANGEROUS_PORTS.put(20034, "NetBus 2.0");
        DANGEROUS_PORTS.put(6667, "IRC Bot (potential botnet)");
        DANGEROUS_PORTS.put(9999, "Generic Backdoor");
    }

    private ThreatSimulatorService() {
        this.executorService = Executors.newCachedThreadPool();
        this.activeServers = new CopyOnWriteArrayList<>();
        this.isRunning = false;
    }

    public static synchronized ThreatSimulatorService getInstance() {
        if (instance == null) {
            instance = new ThreatSimulatorService();
        }
        return instance;
    }

    /**
     * Start listening on all dangerous ports
     */
    public boolean startSimulation() {
        if (isRunning) {
            System.out.println("Threat simulator already running");
            return false;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚨 THREAT SIMULATOR STARTING 🚨");
        System.out.println("=".repeat(60));
        System.out.println("⚠️  This will trigger IDS alerts!");
        System.out.println("⚠️  Only for TESTING purposes!");
        System.out.println("=".repeat(60) + "\n");

        isRunning = true;
        int successCount = 0;

        for (Map.Entry<Integer, String> entry : DANGEROUS_PORTS.entrySet()) {
            int port = entry.getKey();
            String description = entry.getValue();

            if (startListeningOnPort(port, description)) {
                successCount++;
            }
        }

        System.out.println("\n✅ Threat Simulator Started!");
        System.out.println("📊 Listening on " + successCount + " dangerous ports");
        System.out.println("🔍 Your IDS should detect these as threats\n");

        return successCount > 0;
    }

    /**
     * Start listening on a specific dangerous port
     */
    private boolean startListeningOnPort(int port, String description) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            activeServers.add(serverSocket);

            System.out.println(String.format("🎯 Listening on port %d - %s", port, description));

            // Accept connections in background
            executorService.submit(() -> acceptConnections(serverSocket, port, description));

            return true;

        } catch (IOException e) {
            System.err.println(String.format("❌ Failed to bind port %d: %s", port, e.getMessage()));
            return false;
        }
    }

    /**
     * Accept and handle connections on a dangerous port
     */
    private void acceptConnections(ServerSocket serverSocket, int port, String description) {
        System.out.println(String.format("👂 Port %d (%s) ready to accept connections", port, description));

        while (isRunning && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();

                // Log the connection (this will trigger IDS alerts!)
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                System.out.println(String.format(
                        "⚠️  ALERT! Connection to port %d from %s - IDS should detect this!",
                        port, clientIP
                ));

                // Handle the connection
                executorService.submit(() -> handleClient(clientSocket, port, description));

            } catch (IOException e) {
                if (isRunning) {
                    System.err.println(String.format("Error accepting connection on port %d: %s",
                            port, e.getMessage()));
                }
            }
        }
    }

    /**
     * Handle a client connection (simulate trojan behavior)
     */
    private void handleClient(Socket clientSocket, int port, String description) {
        try {
            String clientIP = clientSocket.getInetAddress().getHostAddress();
            System.out.println(String.format(
                    "🔴 Handling suspicious connection on port %d from %s", port, clientIP
            ));

            // Send a suspicious message (simulating trojan)
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Trojan-like greeting
            out.println("220 " + description + " - Backdoor Service Ready");
            out.println("Command: ");

            // Read and echo commands (simulate trojan command server)
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(String.format(
                        "📥 Port %d received command from %s: %s", port, clientIP, inputLine
                ));

                if (inputLine.equalsIgnoreCase("QUIT") || inputLine.equalsIgnoreCase("EXIT")) {
                    out.println("221 Goodbye");
                    break;
                }

                // Echo back (trojan simulation)
                out.println("200 Command executed: " + inputLine);
                out.println("Command: ");
            }

            clientSocket.close();
            System.out.println(String.format("🔌 Connection closed from %s on port %d", clientIP, port));

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    /**
     * Stop all simulated threats
     */
    public void stopSimulation() {
        if (!isRunning) {
            System.out.println("Threat simulator not running");
            return;
        }

        System.out.println("\n🛑 Stopping Threat Simulator...");
        isRunning = false;

        // Close all server sockets
        for (ServerSocket server : activeServers) {
            try {
                if (!server.isClosed()) {
                    server.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }

        activeServers.clear();
        executorService.shutdown();

        System.out.println("✅ Threat Simulator Stopped\n");
    }

    /**
     * Simulate a port scan attack
     */
    public void simulatePortScan(String targetIP) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔍 SIMULATING PORT SCAN ATTACK");
        System.out.println("=".repeat(60));
        System.out.println("Target: " + targetIP);
        System.out.println("This will trigger PORT SCAN alerts in your IDS!");
        System.out.println("=".repeat(60) + "\n");

        executorService.submit(() -> {
            int[] portsToScan = {21, 22, 23, 25, 80, 443, 445, 1337, 3306, 3389,
                    5900, 6667, 8080, 8443, 12345, 31337};

            for (int port : portsToScan) {
                try {
                    System.out.println(String.format("🔎 Scanning %s:%d...", targetIP, port));

                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(targetIP, port), 1000);

                    System.out.println(String.format("✅ Port %d is OPEN", port));
                    socket.close();

                } catch (IOException e) {
                    System.out.println(String.format("❌ Port %d is CLOSED", port));
                }

                // Small delay between scans
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }

            System.out.println("\n✅ Port scan simulation completed!");
            System.out.println("📊 Your IDS should have detected this as a PORT SCAN\n");
        });
    }

    /**
     * Simulate SYN flood attack
     */
    public void simulateSynFlood(String targetIP, int targetPort, int duration) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("💥 SIMULATING SYN FLOOD ATTACK");
        System.out.println("=".repeat(60));
        System.out.println("Target: " + targetIP + ":" + targetPort);
        System.out.println("Duration: " + duration + " seconds");
        System.out.println("This will trigger DDoS/FLOOD alerts in your IDS!");
        System.out.println("=".repeat(60) + "\n");

        executorService.submit(() -> {
            long endTime = System.currentTimeMillis() + (duration * 1000);
            int packetCount = 0;

            while (System.currentTimeMillis() < endTime) {
                try {
                    // Attempt rapid connections (simulates SYN flood)
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(targetIP, targetPort), 100);
                    socket.close();

                    packetCount++;

                    if (packetCount % 50 == 0) {
                        System.out.println(String.format("📡 Sent %d packets...", packetCount));
                    }

                } catch (IOException e) {
                    // Expected - many will fail
                }

                // Very small delay
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }

            System.out.println("\n✅ SYN flood simulation completed!");
            System.out.println(String.format("📊 Sent %d packets in %d seconds", packetCount, duration));
            System.out.println("📊 Your IDS should have detected this as DDoS/FLOOD\n");
        });
    }

    /**
     * Get list of dangerous ports being monitored
     */
    public Map<Integer, String> getDangerousPorts() {
        return new LinkedHashMap<>(DANGEROUS_PORTS);
    }

    /**
     * Check if simulator is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get count of active listeners
     */
    public int getActiveListenerCount() {
        return (int) activeServers.stream().filter(s -> !s.isClosed()).count();
    }

    /**
     * Print status
     */
    public void printStatus() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚨 THREAT SIMULATOR STATUS");
        System.out.println("=".repeat(60));
        System.out.println("Running: " + (isRunning ? "✅ YES" : "❌ NO"));
        System.out.println("Active Listeners: " + getActiveListenerCount());

        if (isRunning) {
            System.out.println("\nDangerous Ports Being Monitored:");
            for (Map.Entry<Integer, String> entry : DANGEROUS_PORTS.entrySet()) {
                boolean isActive = activeServers.stream()
                        .anyMatch(s -> !s.isClosed() && s.getLocalPort() == entry.getKey());
                String status = isActive ? "🟢" : "🔴";
                System.out.println(String.format("  %s Port %d - %s",
                        status, entry.getKey(), entry.getValue()));
            }
        }
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Test connection to a dangerous port (from external tool)
     */
    public static void testConnection(String host, int port) {
        System.out.println(String.format("\n🔌 Testing connection to %s:%d...", host, port));

        try {
            Socket socket = new Socket(host, port);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("✅ Connected successfully!");

            // Read greeting
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("📥 Received: " + line);
                if (line.contains("Command:")) {
                    break;
                }
            }

            // Send test command
            System.out.println("📤 Sending: TEST");
            out.println("TEST");

            // Read response
            while ((line = in.readLine()) != null) {
                System.out.println("📥 Received: " + line);
                if (line.contains("Command:")) {
                    break;
                }
            }

            // Quit
            out.println("QUIT");
            socket.close();

            System.out.println("✅ Test completed successfully!\n");

        } catch (IOException e) {
            System.err.println("❌ Connection failed: " + e.getMessage() + "\n");
        }
    }
}