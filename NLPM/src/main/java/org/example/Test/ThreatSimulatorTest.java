package org.example.Test;

import org.example.services.ThreatSimulatorService;

import java.util.Scanner;

/**
 * Standalone test program for Threat Simulator
 * Run this to test the simulator without the full IDS application
 */
public class ThreatSimulatorTest {

    public static void main(String[] args) {
        ThreatSimulatorService simulator = ThreatSimulatorService.getInstance();
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🚨 THREAT SIMULATOR TEST PROGRAM 🚨");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("This program will:");
        System.out.println("  1. Listen on dangerous ports (trojan/backdoor ports)");
        System.out.println("  2. Accept connections and simulate trojan behavior");
        System.out.println("  3. Trigger your IDS detection system");
        System.out.println();
        System.out.println("⚠️  WARNING: Only for TESTING on LOCAL machine!");
        System.out.println("=".repeat(70));
        System.out.println();

        // Display menu
        boolean running = true;

        while (running) {
            printMenu();

            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    startSimulator(simulator);
                    break;

                case "2":
                    stopSimulator(simulator);
                    break;

                case "3":
                    showStatus(simulator);
                    break;

                case "4":
                    simulatePortScan(simulator, scanner);
                    break;

                case "5":
                    simulateSynFlood(simulator, scanner);
                    break;

                case "6":
                    testConnection(scanner);
                    break;

                case "7":
                    showHelp();
                    break;

                case "0":
                    running = false;
                    System.out.println("\n👋 Exiting...");
                    if (simulator.isRunning()) {
                        simulator.stopSimulation();
                    }
                    break;

                default:
                    System.out.println("❌ Invalid choice. Try again.");
            }

            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📋 MENU");
        System.out.println("=".repeat(70));
        System.out.println("1. ▶️  Start Threat Simulator");
        System.out.println("2. ⏹️  Stop Threat Simulator");
        System.out.println("3. 📊 Show Status");
        System.out.println("4. 🔍 Simulate Port Scan");
        System.out.println("5. 💥 Simulate SYN Flood");
        System.out.println("6. 🔌 Test Connection to Port");
        System.out.println("7. ❓ Help");
        System.out.println("0. 🚪 Exit");
        System.out.println("=".repeat(70));
    }

    private static void startSimulator(ThreatSimulatorService simulator) {
        System.out.println("\n▶️  Starting Threat Simulator...");
        boolean success = simulator.startSimulation();

        if (success) {
            System.out.println("✅ Simulator started successfully!");
            System.out.println("\n📝 Now you can:");
            System.out.println("  - Start your IDS packet capture");
            System.out.println("  - Test connections using option 6");
            System.out.println("  - Simulate attacks using options 4 or 5");
            System.out.println("  - Check your IDS Alerts tab");
        } else {
            System.out.println("❌ Failed to start simulator");
        }
    }

    private static void stopSimulator(ThreatSimulatorService simulator) {
        System.out.println("\n⏹️  Stopping Threat Simulator...");
        simulator.stopSimulation();
        System.out.println("✅ Simulator stopped");
    }

    private static void showStatus(ThreatSimulatorService simulator) {
        simulator.printStatus();
    }

    private static void simulatePortScan(ThreatSimulatorService simulator, Scanner scanner) {
        System.out.print("\nEnter target IP (default: 127.0.0.1): ");
        String ip = scanner.nextLine().trim();

        if (ip.isEmpty()) {
            ip = "127.0.0.1";
        }

        System.out.println("\n🔍 Starting port scan simulation...");
        System.out.println("Target: " + ip);
        System.out.println("⚠️  This will trigger PORT SCAN alerts in your IDS!");

        simulator.simulatePortScan(ip);

        System.out.println("\n✅ Port scan simulation started");
        System.out.println("📊 Check your IDS Alerts tab for detections!");
    }

    private static void simulateSynFlood(ThreatSimulatorService simulator, Scanner scanner) {
        System.out.print("\nEnter target IP (default: 127.0.0.1): ");
        String ip = scanner.nextLine().trim();
        if (ip.isEmpty()) ip = "127.0.0.1";

        System.out.print("Enter target port (default: 1337): ");
        String portStr = scanner.nextLine().trim();
        int port = portStr.isEmpty() ? 1337 : Integer.parseInt(portStr);

        System.out.print("Enter duration in seconds (default: 10): ");
        String durationStr = scanner.nextLine().trim();
        int duration = durationStr.isEmpty() ? 10 : Integer.parseInt(durationStr);

        System.out.println("\n💥 Starting SYN flood simulation...");
        System.out.println("Target: " + ip + ":" + port);
        System.out.println("Duration: " + duration + " seconds");
        System.out.println("⚠️  This will trigger DDoS/FLOOD alerts in your IDS!");

        simulator.simulateSynFlood(ip, port, duration);

        System.out.println("\n✅ SYN flood simulation started");
        System.out.println("📊 Check your IDS Alerts tab for detections!");
    }

    private static void testConnection(Scanner scanner) {
        System.out.print("\nEnter host (default: 127.0.0.1): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) host = "127.0.0.1";

        System.out.print("Enter port (default: 1337): ");
        String portStr = scanner.nextLine().trim();
        int port = portStr.isEmpty() ? 1337 : Integer.parseInt(portStr);

        ThreatSimulatorService.testConnection(host, port);
    }

    private static void showHelp() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("❓ HELP - How to Use Threat Simulator");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("🎯 Purpose:");
        System.out.println("   This simulator helps you test your IDS detection capabilities by");
        System.out.println("   simulating real threat scenarios in a controlled, safe environment.");
        System.out.println();
        System.out.println("📝 Step-by-Step Guide:");
        System.out.println();
        System.out.println("   1️⃣  START THE SIMULATOR (Option 1)");
        System.out.println("       - Listens on dangerous ports (1337, 31337, 12345, etc.)");
        System.out.println("       - These are known trojan/backdoor ports");
        System.out.println();
        System.out.println("   2️⃣  START YOUR IDS PACKET CAPTURE");
        System.out.println("       - Open your IDS application");
        System.out.println("       - Go to Traffic Monitor");
        System.out.println("       - Select your network interface");
        System.out.println("       - Click 'Start Capture'");
        System.out.println();
        System.out.println("   3️⃣  GENERATE TEST TRAFFIC");
        System.out.println("       - Option 4: Port Scan → scans multiple ports rapidly");
        System.out.println("       - Option 5: SYN Flood → sends many connection attempts");
        System.out.println("       - Option 6: Test Connection → connects to a dangerous port");
        System.out.println();
        System.out.println("   4️⃣  CHECK YOUR IDS ALERTS");
        System.out.println("       - Go to Alerts tab in your IDS");
        System.out.println("       - You should see:");
        System.out.println("         • Port Scan alerts");
        System.out.println("         • Suspicious Port Access alerts");
        System.out.println("         • High Packet Rate alerts (for SYN flood)");
        System.out.println("         • Direction should show ⬇️ INBOUND");
        System.out.println();
        System.out.println("   5️⃣  STOP SIMULATOR (Option 2)");
        System.out.println("       - When done testing, stop the simulator");
        System.out.println();
        System.out.println("🔐 Safety Notes:");
        System.out.println("   ✅ Use 127.0.0.1 (localhost) for safe testing");
        System.out.println("   ✅ Only use on your own machine");
        System.out.println("   ❌ Do NOT use on production systems");
        System.out.println("   ❌ Do NOT scan external networks");
        System.out.println();
        System.out.println("💡 What to Expect:");
        System.out.println("   - Your IDS should detect suspicious activity");
        System.out.println("   - Alerts should appear in real-time");
        System.out.println("   - Direction should be marked as INBOUND");
        System.out.println("   - Severity should be MEDIUM to HIGH");
        System.out.println();
        System.out.println("🐛 Troubleshooting:");
        System.out.println("   - If ports fail to bind: check if already in use");
        System.out.println("   - If no alerts appear: ensure IDS is capturing packets");
        System.out.println("   - If connection fails: ensure simulator is running");
        System.out.println();
        System.out.println("=".repeat(70));
    }
}