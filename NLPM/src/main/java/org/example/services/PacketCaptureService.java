package org.example.services;

import javafx.application.Platform;
import org.example.database.dao.TrafficDAO;
import org.example.models.TrafficData;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for capturing and analyzing network packets in real-time
 */
public class PacketCaptureService {
    private static PacketCaptureService instance;
    private PcapHandle handle;
    private ExecutorService executorService;
    private AtomicBoolean isCapturing;
    private TrafficDAO trafficDAO;
    private DetectionEngine detectionEngine;
    private PacketListener packetListener;
    private Future<?> captureTask; // Track the capture thread

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

        // Clean up any stale handle from previous capture
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
            int snapLen = 65536; // Capture all packets
            PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;

            int timeout = 10; // 10ms timeout

            handle = nif.openLive(snapLen, mode, timeout);

            // Set filter if needed (optional - capture all traffic)
            // String filter = "tcp or udp or icmp";
            // handle.setFilter(filter, BpfCompileMode.OPTIMIZE);

            isCapturing.set(true);

            // Start packet capture in separate thread and track it
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
                handle.breakLoop(); // Signal the loop to stop
            } catch (NotOpenException ignored) {
            }
        }

        // Wait for the capture thread to finish (max 3 seconds)
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

        // Final cleanup of handle (if not already done by capture thread)
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
            // Loop infini, stoppé par breakLoop()
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
            // IMPORTANT: Reset state when capture loop ends (for any reason)
            // This allows restarting the capture without admin privilege errors
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
            return; // Skip non-IPv4 packets
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
            if (dstPort == 80)
                protocol = "HTTP";
            else if (dstPort == 443)
                protocol = "HTTPS";
            else if (dstPort == 22)
                protocol = "SSH";
            else if (dstPort == 21)
                protocol = "FTP";
            else if (dstPort == 3389)
                protocol = "RDP";
        } else if (protocol.equals("UDP")) {
            if (dstPort == 53)
                protocol = "DNS";
            else if (dstPort == 67 || dstPort == 68)
                protocol = "DHCP";
        }

        // Create traffic data object
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String status = "Allowed"; // Default status

        TrafficData trafficData = new TrafficData(
                protocol,
                srcIp,
                String.valueOf(srcPort),
                dstIp,
                String.valueOf(dstPort),
                packet.length(),
                timestamp,
                status);

        // Store in database (async to avoid blocking)
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
     * Get list of available network interfaces
     */
    public static String[] getAvailableInterfaces() {
        try {
            return Pcaps.findAllDevs().stream()
                    .map(nif -> nif.getName() + " - " +
                            (nif.getAddresses().isEmpty() ? "No address"
                                    : nif.getAddresses().get(0).getAddress().getHostAddress()))
                    .toArray(String[]::new);
        } catch (PcapNativeException e) {
            System.err.println("Error getting network interfaces: " + e.getMessage());
            return new String[] { "Error: " + e.getMessage() };
        }
    }

    public boolean isCapturing() {
        return isCapturing.get();
    }

    public long getPacketsAnalyzed() {
        return packetsAnalyzed;
    }

    public long getBytesProcessed() {
        return bytesProcessed;
    }

    /**
     * Cleanup resources
     */

    public void stop() {
        PacketCaptureService.getInstance().shutdown();
        Platform.exit();
    }

    public void shutdown() {
        stopCapture();
        executorService.shutdown();
    }
}
