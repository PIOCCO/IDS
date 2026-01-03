┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION START                        │
│                    (Main.java)                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
         ┌───────────────────────────────┐
         │  1. Initialize UI Framework   │
         │     - JavaFX initialization   │
         │     - Window setup            │
         └───────────┬───────────────────┘
                     │
                     ▼
         ┌───────────────────────────────┐
         │  2. Load Login Screen         │
         │     - Display login form      │
         │     - NO database needed      │
         └───────────┬───────────────────┘
                     │
                     ▼
         ┌───────────────────────────────┐
         │  3. User Enters Credentials   │
         │     Username: admin           │
         │     Password: admin123        │
         └───────────┬───────────────────┘
                     │
                     ▼
         ┌───────────────────────────────┐
         │  4. Authenticate User         │
         │     - In-memory validation    │
         │     - Fast, no DB needed      │
         └───────────┬───────────────────┘
                     │
           ┌─────────┴─────────┐
           │                   │
        [FAIL]              [SUCCESS]
           │                   │
           ▼                   ▼
    ┌──────────────┐   ┌──────────────────────┐
    │ Show Error   │   │  5. Show Loading     │
    │ Try Again    │   │     "Initializing    │
    └──────────────┘   │      database..."    │
                       └──────────┬───────────┘
                                  │
                                  ▼
                       ┌──────────────────────┐
                       │  6. Initialize DB    │
                       │     (Background)     │
                       │  - Connect to DB     │
                       │  - Create tables     │
                       │  - Init services     │
                       └──────────┬───────────┘
                                  │
                       ┌──────────┴──────────┐
                       │                     │
                    [FAIL]               [SUCCESS]
                       │                     │
                       ▼                     ▼
            ┌──────────────────┐   ┌────────────────────┐
            │ Show Warning:    │   │  7. Load Main App  │
            │ "Continue        │   │     - Dashboard    │
            │  without DB?"    │   │     - Full access  │
            └──────────────────┘   └────────────────────┘
                       │
            ┌──────────┴──────────┐
            │                     │
         [YES]                  [NO]
            │                     │
            ▼                     ▼
    ┌──────────────────┐  ┌──────────────┐
    │ Load Main App    │  │ Stay on      │
    │ Limited Mode:    │  │ Login Screen │
    │ - No DB logging  │  └──────────────┘
    │ - No history     │
    │ - Live only      │
    └──────────────────┘

# HOW TO USE THREAT SIMULATOR

🚨 Threat Simulator - Complete Usage Guide
Overview
The Threat Simulator is a testing tool built into your IDS Monitor that allows you to safely test and visualize your intrusion detection capabilities. It simulates real-world threats in a controlled environment.

⚠️ Safety Warning
CRITICAL: This tool is for TESTING ONLY on your LOCAL MACHINE!

✅ DO:

Use 127.0.0.1 (localhost) for testing
Run on your own development machine
Stop the simulator when done testing
❌ DON'T:

Use on production systems
Scan external networks
Run in public/corporate networks
Leave it running unattended
🎯 What Does It Do?
The Threat Simulator creates realistic threat scenarios that your IDS should detect:

1. Dangerous Port Listening 🎯
   Listens on known trojan/backdoor ports:

Port 1337 - Elite Trojan
Port 31337 - Back Orifice
Port 12345 - NetBus
Port 6667 - IRC Bot (potential botnet)
Port 9999 - Generic Backdoor
2. Port Scan Simulation 🔍
   Rapidly scans multiple ports to trigger port scan detection

3. SYN Flood Simulation 💥
   Sends rapid connection attempts to simulate DDoS attacks

4. Connection Testing 🔌
   Tests connections to dangerous ports

📋 Step-by-Step Usage
Method 1: Using the GUI (Recommended)
Step 1: Access Threat Simulator
Login to your IDS Monitor
Click "🚨 Threat Simulator" in the sidebar (under TESTING section)
Step 2: Start the Simulator
Click "▶️ Start Simulator" button
Watch the Activity Log for confirmation
You should see: "✅ Simulator started successfully!"
Status indicator will turn 🟢 ACTIVE
Step 3: Start IDS Packet Capture
Navigate to "🌐 Traffic Monitor" tab
Select your network interface from dropdown
Click "▶️ Start Capture"
Confirm capture is running
Step 4: Generate Test Traffic
Option A: Test Connection (Gentle Test)

Keep target IP as 127.0.0.1
Keep target port as 1337
Click "🔌 Test Connection"
This creates a single connection to a dangerous port
Option B: Port Scan (Medium Test)

Set target IP to 127.0.0.1
Click "🔍 Simulate Port Scan"
This scans 16 common ports rapidly
Should trigger PORT SCAN alerts
Option C: SYN Flood (Aggressive Test)

Set target IP to 127.0.0.1
Set target port to 1337
Click "💥 Simulate SYN Flood (10s)"
This sends many rapid connections
Should trigger DDoS/FLOOD alerts
Step 5: Check Alerts
Navigate to "🚨 Alerts" tab
You should see new alerts appearing:
Suspicious Port Access (from test connection)
Port Scan (from port scan simulation)
High Packet Rate (from SYN flood)
Check that Direction shows ⬇️ INBOUND
Severity should be MEDIUM to HIGH
Step 6: Stop Simulator
Go back to "🚨 Threat Simulator" tab
Click "⏹️ Stop Simulator"
Status should change to 🔴 STOPPED
Method 2: Using Command Line Test Program
For advanced testing without the full GUI:

Step 1: Run the Test Program
bash
# Navigate to your project directory
cd NLPM

# Compile (if needed)
mvn clean compile

# Run the test program
java -cp target/classes org.example.test.ThreatSimulatorTest
Step 2: Use the Menu
📋 MENU
1. ▶️  Start Threat Simulator
2. ⏹️  Stop Threat Simulator
3. 📊 Show Status
4. 🔍 Simulate Port Scan
5. 💥 Simulate SYN Flood
6. 🔌 Test Connection to Port
7. ❓ Help
0. 🚪 Exit
   Step 3: Start Simulator
   Enter 1 and press Enter
   Wait for confirmation
   Step 4: Generate Traffic
   Enter 6 to test a connection
   Enter 4 to simulate port scan
   Enter 5 to simulate SYN flood
   Step 5: Check IDS
   Open your IDS GUI
   Check Alerts tab
   Verify detections appear
   📊 Expected Results
   What Should You See in Alerts?
1. Test Connection Alert
   Severity: MEDIUM
   Type: Suspicious Port Access
   Direction: ⬇️ INBOUND
   Source: 127.0.0.1
   Destination: 127.0.0.1
   Port: 1337
   Description: Access to suspicious port 1337 (Elite Trojan)
2. Port Scan Alert
   Severity: HIGH
   Type: Port Scan
   Direction: ⬇️ INBOUND
   Source: 127.0.0.1
   Destination: 127.0.0.1
   Description: Port scan detected: 16 unique ports accessed
3. SYN Flood Alert
   Severity: HIGH
   Type: High Packet Rate / SYN Scan
   Direction: ⬇️ INBOUND
   Source: 127.0.0.1
   Destination: 127.0.0.1
   Description: High packet rate detected: 50+ packets/sec
   🔧 Configuration Options
   Target IP Options:
   127.0.0.1 (localhost) - Safest, recommended
   Your local IP (e.g., 192.168.1.100) - Test from your machine
   Another machine - Only on your private network!
   Port Options:
   1337 - Elite Trojan (default, good for testing)
   31337 - Back Orifice
   12345 - NetBus
   Any suspicious port from the list
   Duration Options (SYN Flood):
   5 seconds - Quick test
   10 seconds - Standard test (default)
   30 seconds - Stress test
   🐛 Troubleshooting
   Problem: Simulator won't start
   Symptoms:

"Failed to bind port" errors
No listeners active
Solutions:

Check if ports are already in use:
bash
# Windows
netstat -ano | findstr "1337"

# Linux/Mac
lsof -i :1337
Stop any other programs using those ports
Try restarting your computer
Run as Administrator (Windows) or with sudo (Linux/Mac)
Problem: No alerts appearing
Symptoms:

Traffic generated but no alerts in IDS
Packet capture shows 0 packets
Solutions:

Ensure IDS packet capture is RUNNING
Check you selected the correct network interface
Verify you're targeting 127.0.0.1 (localhost)
Check DetectionEngine is initialized:
java
DetectionEngine engine = DetectionEngine.getInstance();
System.out.println("Threats detected: " + engine.getTotalThreatsDetected());
Look at console logs for error messages
Problem: Connection test fails
Symptoms:

"Connection failed" message
Timeout errors
Solutions:

Ensure simulator is RUNNING (status shows 🟢)
Check target IP is correct (use 127.0.0.1)
Check target port matches a listening port (e.g., 1337)
Check firewall isn't blocking connections
Try a different port from the list
Problem: Too many/few alerts
Symptoms:

Alert spam
Not enough alerts
Solutions:

Too many alerts:
Add 127.0.0.1 to whitelist
Increase alert thresholds in DetectionEngine
Enable rate limiting (already enabled by default)
Too few alerts:
Decrease thresholds in DetectionEngine:
java
private static final int PORT_SCAN_THRESHOLD = 5; // Lower from 10
private static final int PACKET_RATE_THRESHOLD = 20; // Lower from 50
Check direction detection is working
Verify you're not in whitelist
📈 Advanced Usage
Testing Specific Attack Scenarios
1. SSH Brute Force Simulation
   java
   // In ThreatSimulatorService, add:
   public void simulateSSHBruteForce(String targetIP) {
   for (int i = 0; i < 20; i++) {
   try {
   Socket socket = new Socket(targetIP, 22);
   socket.close();
   Thread.sleep(100);
   } catch (Exception e) {}
   }
   }
2. Multiple Source IPs
   java
   // Test from different local IPs if available
   simulator.simulatePortScan("192.168.1.100");
   simulator.simulatePortScan("192.168.1.101");
3. Custom Port List
   java
   // Modify DANGEROUS_PORTS in ThreatSimulatorService
   DANGEROUS_PORTS.put(3306, "MySQL Database");
   DANGEROUS_PORTS.put(5432, "PostgreSQL Database");
   🎓 Understanding the Results
   What Makes a Good IDS?
   Your IDS should:

✅ Detect suspicious port access (INBOUND)
✅ Ignore normal outbound traffic
✅ Rate-limit alerts (not spam)
✅ Identify direction (INBOUND threats)
✅ Show severity correctly
Expected Detection Rates:
Port Scan: 100% detection (should catch all)
SYN Flood: 95%+ detection
Single Connection: 80%+ detection (depends on thresholds)
📞 Integration with External Tools
Using nmap (Advanced)
bash
# Port scan from external tool
nmap -p 1337,31337,12345 127.0.0.1

# SYN scan
nmap -sS 127.0.0.1
Using netcat (Advanced)
bash
# Connect to dangerous port
nc 127.0.0.1 1337

# Send data
echo "TEST" | nc 127.0.0.1 1337
Using Python (Advanced)
python
import socket

# Test connection
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(('127.0.0.1', 1337))
s.send(b'TEST\n')
print(s.recv(1024))
s.close()
🔬 Testing Checklist
Use this checklist to verify your IDS:

Simulator starts successfully
Dangerous ports show as active (🟢)
IDS packet capture is running
Test connection generates alert
Alert shows correct direction (⬇️ INBOUND)
Alert shows correct severity (MEDIUM/HIGH)
Port scan triggers PORT SCAN alert
SYN flood triggers DDoS/FLOOD alert
Activity log shows traffic
Alerts appear in real-time
Can filter alerts by direction
Can filter alerts by severity
Simulator stops cleanly
💡 Tips & Best Practices
Always use localhost (127.0.0.1) for initial testing
Start with gentle tests (Test Connection) before aggressive ones
Monitor console logs for detailed information
Check both Activity Log and Alerts tab
Stop simulator when done to free ports
Test one scenario at a time for clarity
Document your results for comparison
Adjust thresholds based on your network
🎉 Success Indicators
Your IDS is working correctly if:

✅ Detects suspicious port access within 1-2 seconds
✅ Port scans trigger alerts after 10+ ports
✅ Direction is correctly identified as INBOUND
✅ No false positives from normal OUTBOUND traffic
✅ Alerts are rate-limited (no spam)
✅ Severity levels are appropriate
✅ Console logs show packet analysis
📚 Additional Resources
DetectionEngine.java - Core detection logic
ThreatSimulatorService.java - Simulator implementation
AlertsController.java - Alerts display
Traffic Monitor - Packet capture view
🆘 Getting Help
If you encounter issues:

Check this guide's Troubleshooting section
Review console logs for errors
Verify all services are running
Test with simplest scenario first (Test Connection)
Check firewall/antivirus settings
Happy Testing! 🛡️🔍

Remember: This tool helps you verify your IDS works correctly. Use it responsibly and only on your own systems!

