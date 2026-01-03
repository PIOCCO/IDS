package org.example.services;

import org.example.database.dao.TrafficDAO;
import org.example.models.TrafficData;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enhanced Packet Capture Service with Smart Interface Filtering
 * Only shows interfaces with valid real IP addresses (excludes APIPA, link-local, etc.)
 */
public class PacketCaptureService {
    private static PacketCaptureService instance;
    private PcapHandle handle;
    private ExecutorService executorService;
    private AtomicBoolean isCapturing;
    private TrafficDAO trafficDAO;
    private DetectionEngine detectionEngine;
    private Future<?> captureTask;

    private long packetsAnalyzed = 0;
    private long bytesProcessed = 0;

    private PacketCaptureService() {
        this.isCapturing = new AtomicBoolean(false);
        this.executorService = Executors.newFixedThreadPool(2);
        this.trafficDAO = new TrafficDAO();
        this.detectionEngine = DetectionEngine.getInstance();
    }

    public static synchronized PacketCaptureService getInstance() {
        if (instance == null) {
            instance = new PacketCaptureService();
        }
        return instance;
    }

    /**
     * Start capturing packets on specified network interface
     */
    public boolean startCapture(String deviceName) {
        if (isCapturing.get()) {
            System.out.println("Packet capture already running");
            return false;
        }

        // Clean up any stale handle
        if (handle != null) {
            try {
                if (handle.isOpen()) {
                    handle.close();
                }
            } catch (Exception e) {
                System.err.println("Error cleaning up old handle: " + e.getMessage());
            }
            handle = null;
        }

        try {
            // Get network interface
            PcapNetworkInterface nif = getNetworkInterface(deviceName);
            if (nif == null) {
                System.err.println("Network interface not found: " + deviceName);
                return false;
            }

            // Open interface for capturing
            int snapLen = 65536;
            PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;
            int timeout = 10; // 10ms timeout

            handle = nif.openLive(snapLen, mode, timeout);
            isCapturing.set(true);

            // Start packet capture in separate thread
            captureTask = executorService.submit(() -> {
                try {
                    capturePackets();
                } catch (Exception e) {
                    System.err.println("Error during packet capture: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            System.out.println("Packet capture started on interface: " + nif.getName());
            return true;

        } catch (PcapNativeException e) {
            System.err.println("Failed to start packet capture: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Stop packet capture
     */
    public void stopCapture() {
        if (!isCapturing.get()) {
            return;
        }

        isCapturing.set(false);

        if (handle != null && handle.isOpen()) {
            try {
                handle.breakLoop();
            } catch (NotOpenException ignored) {
            }
        }

        // Wait for capture thread to finish
        if (captureTask != null) {
            try {
                captureTask.get(3, TimeUnit.SECONDS);
                System.out.println("Capture thread finished cleanly");
            } catch (Exception e) {
                System.err.println("Error waiting for capture thread: " + e.getMessage());
                captureTask.cancel(true);
            }
            captureTask = null;
        }

        // Final cleanup
        if (handle != null) {
            try {
                if (handle.isOpen()) {
                    handle.close();
                }
            } catch (Exception e) {
                System.err.println("Error closing handle: " + e.getMessage());
            }
            handle = null;
        }

        System.out.println("Packet capture stopped");
        System.out.println("Total packets analyzed: " + packetsAnalyzed);
        System.out.println("Total bytes processed: " + bytesProcessed);
    }

    /**
     * Main packet capture loop
     */
    private void capturePackets() {
        PacketListener listener = packet -> {
            try {
                processPacket(packet);
            } catch (Exception e) {
                System.err.println("Error processing packet: " + e.getMessage());
            }
        };

        try {
            handle.loop(-1, listener);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Packet capture interrupted");
        } catch (NotOpenException e) {
            System.out.println("Pcap handle closed");
        } catch (PcapNativeException e) {
            System.err.println("Native pcap error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isCapturing.set(false);
            if (handle != null && handle.isOpen()) {
                handle.close();
                handle = null;
            }
            System.out.println("Capture loop ended - ready for restart");
        }
    }

    /**
     * Process captured packet
     */
    private void processPacket(Packet packet) {
        packetsAnalyzed++;
        bytesProcessed += packet.length();

        // Extract IP packet
        IpV4Packet ipPacket = packet.get(IpV4Packet.class);
        if (ipPacket == null) {
            return;
        }

        String srcIp = ipPacket.getHeader().getSrcAddr().getHostAddress();
        String dstIp = ipPacket.getHeader().getDstAddr().getHostAddress();

        // Extract transport layer info
        String protocol = "UNKNOWN";
        int srcPort = 0;
        int dstPort = 0;

        // Check TCP
        TcpPacket tcpPacket = packet.get(TcpPacket.class);
        if (tcpPacket != null) {
            protocol = "TCP";
            srcPort = tcpPacket.getHeader().getSrcPort().valueAsInt();
            dstPort = tcpPacket.getHeader().getDstPort().valueAsInt();
        } else {
            // Check UDP
            UdpPacket udpPacket = packet.get(UdpPacket.class);
            if (udpPacket != null) {
                protocol = "UDP";
                srcPort = udpPacket.getHeader().getSrcPort().valueAsInt();
                dstPort = udpPacket.getHeader().getDstPort().valueAsInt();
            } else {
                // Check ICMP
                IcmpV4CommonPacket icmpPacket = packet.get(IcmpV4CommonPacket.class);
                if (icmpPacket != null) {
                    protocol = "ICMP";
                }
            }
        }

        // Determine protocol for common ports
        if (protocol.equals("TCP")) {
            protocol = switch (dstPort) {
                case 80 -> "HTTP";
                case 443 -> "HTTPS";
                case 22 -> "SSH";
                case 21 -> "FTP";
                case 3389 -> "RDP";
                default -> "TCP";
            };
        } else if (protocol.equals("UDP")) {
            protocol = switch (dstPort) {
                case 53 -> "DNS";
                case 67, 68 -> "DHCP";
                default -> "UDP";
            };
        }

        // Create traffic data object
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String status = "Allowed";

        TrafficData trafficData = new TrafficData(
                protocol, srcIp, String.valueOf(srcPort), dstIp, String.valueOf(dstPort),
                packet.length(), timestamp, status);

        // Store in database (async)
        executorService.submit(() -> {
            try {
                trafficDAO.insertTraffic(trafficData);
            } catch (Exception e) {
                System.err.println("Error storing traffic data: " + e.getMessage());
            }
        });

        // Run through detection engine
        detectionEngine.analyzeTraffic(trafficData, packet);
    }

    /**
     * Get network interface by name
     */
    private PcapNetworkInterface getNetworkInterface(String name) throws PcapNativeException {
        for (PcapNetworkInterface nif : Pcaps.findAllDevs()) {
            if (nif.getName().equals(name)) {
                return nif;
            }
        }
        return null;
    }

    /**
     * Get list of available network interfaces with SMART FILTERING
     * Only shows interfaces with valid, real IP addresses
     * Excludes: APIPA (169.254.x.x), link-local, loopback, no IP
     */
    public static String[] getAvailableInterfaces() {
        try {
            List<String> validInterfaces = new ArrayList<>();

            for (PcapNetworkInterface nif : Pcaps.findAllDevs()) {
                // Skip if no addresses
                if (nif.getAddresses().isEmpty()) {
                    continue;
                }

                // Get the first IPv4 address
                String ipAddress = null;
                for (PcapAddress addr : nif.getAddresses()) {
                    if (addr.getAddress() instanceof java.net.Inet4Address) {
                        ipAddress = addr.getAddress().getHostAddress();
                        break;
                    }
                }

                // Skip if no valid IPv4 address found
                if (ipAddress == null) {
                    continue;
                }

                // Filter out unwanted IP addresses
                if (isValidRealIP(ipAddress)) {
                    String interfaceDisplay = String.format("%s - %s (%s)",
                            nif.getName(),
                            ipAddress,
                            nif.getDescription() != null ? nif.getDescription() : "Network Adapter");
                    validInterfaces.add(interfaceDisplay);
                }
            }

            if (validInterfaces.isEmpty()) {
                return new String[] { "No valid network interfaces found" };
            }

            return validInterfaces.toArray(new String[0]);

        } catch (PcapNativeException e) {
            System.err.println("Error getting network interfaces: " + e.getMessage());
            return new String[] { "Error: " + e.getMessage() };
        }
    }

    /**
     * Check if IP address is a valid "real" IP (not APIPA, link-local, etc.)
     */
    private static boolean isValidRealIP(String ip) {
        // Exclude loopback (127.x.x.x)
        if (ip.startsWith("127.")) {
            return false;
        }

        // Exclude APIPA / link-local (169.254.x.x)
        if (ip.startsWith("169.254.")) {
            return false;
        }

        // Exclude 0.0.0.0
        if (ip.equals("0.0.0.0")) {
            return false;
        }

        // Include private networks (these are real local IPs)
        // 192.168.x.x, 10.x.x.x, 172.16-31.x.x
        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return true;
        }

        if (ip.startsWith("172.")) {
            try {
                String[] parts = ip.split("\\.");
                int secondOctet = Integer.parseInt(parts[1]);
                if (secondOctet >= 16 && secondOctet <= 31) {
                    return true; // 172.16.0.0 - 172.31.255.255
                }
            } catch (Exception e) {
                return false;
            }
        }

        // Include public IPs (anything else that passed previous filters)
        return true;
    }

    /**
     * Get detailed interface information for debugging
     */
    public static void printInterfaceDetails() {
        try {
            System.out.println("\n=== Network Interface Analysis ===");
            for (PcapNetworkInterface nif : Pcaps.findAllDevs()) {
                System.out.println("\nInterface: " + nif.getName());
                System.out.println("  Description: " + nif.getDescription());
                System.out.println("  Addresses:");
                for (PcapAddress addr : nif.getAddresses()) {
                    String ip = addr.getAddress().getHostAddress();
                    boolean isValid = isValidRealIP(ip);
                    System.out.println("    - " + ip + " (Valid: " + isValid + ")");
                }
            }
            System.out.println("=================================\n");
        } catch (Exception e) {
            System.err.println("Error printing interface details: " + e.getMessage());
        }
    }

    public boolean isCapturing() { return isCapturing.get(); }
    public long getPacketsAnalyzed() { return packetsAnalyzed; }
    public long getBytesProcessed() { return bytesProcessed; }

    public void shutdown() {
        stopCapture();
        executorService.shutdown();
    }
}