# IDS Monitor - Documentation UML

## 📋 Résumé du Projet

**IDS Monitor** est un système de détection d'intrusions (Intrusion Detection System) développé en Java/JavaFX qui permet de:
- Capturer et analyser le trafic réseau en temps réel
- Détecter les menaces (Port Scan, DDoS, Brute Force, SQL Injection, XSS)
- Gérer les alertes de sécurité
- Authentifier les utilisateurs (Admin, User, Viewer)

---

## 1. Diagramme de Cas d'Utilisation (Use Case)

```mermaid
graph TB
    subgraph Acteurs
        Admin["👑 Administrateur"]
        User["👤 Utilisateur"]
        Viewer["👁️ Viewer"]
    end
    
    subgraph "Système IDS Monitor"
        UC1["Se connecter"]
        UC2["Voir le Dashboard"]
        UC3["Monitorer le Trafic"]
        UC4["Démarrer/Arrêter Capture"]
        UC5["Voir les Alertes"]
        UC6["Exporter CSV"]
        UC7["Effacer le Trafic"]
        UC8["Gérer les Utilisateurs"]
        UC9["Configurer Paramètres"]
        UC10["Se déconnecter"]
    end
    
    Admin --> UC1
    Admin --> UC2
    Admin --> UC3
    Admin --> UC4
    Admin --> UC5
    Admin --> UC6
    Admin --> UC7
    Admin --> UC8
    Admin --> UC9
    Admin --> UC10
    
    User --> UC1
    User --> UC2
    User --> UC3
    User --> UC4
    User --> UC5
    User --> UC6
    User --> UC7
    User --> UC10
    
    Viewer --> UC1
    Viewer --> UC2
    Viewer --> UC5
    Viewer --> UC10
```

### Tableau des Cas d'Utilisation

| ID | Cas d'Utilisation | Admin | User | Viewer |
|----|-------------------|-------|------|--------|
| UC1 | Se connecter | ✅ | ✅ | ✅ |
| UC2 | Voir le Dashboard | ✅ | ✅ | ✅ |
| UC3 | Monitorer le Trafic | ✅ | ✅ | ❌ |
| UC4 | Démarrer/Arrêter Capture | ✅ | ✅ | ❌ |
| UC5 | Voir les Alertes | ✅ | ✅ | ✅ |
| UC6 | Exporter CSV | ✅ | ✅ | ❌ |
| UC7 | Effacer le Trafic | ✅ | ✅ | ❌ |
| UC8 | Gérer les Utilisateurs | ✅ | ❌ | ❌ |
| UC9 | Configurer Paramètres | ✅ | ❌ | ❌ |
| UC10 | Se déconnecter | ✅ | ✅ | ✅ |

---

## 2. Diagramme de Classes

```mermaid
classDiagram
    class Main {
        +start(Stage stage)
        +main(String[] args)
        -handleApplicationClose()
    }
    
    class AuthenticationService {
        -instance: AuthenticationService
        -currentUser: User
        -users: HashMap
        +getInstance(): AuthenticationService
        +authenticate(username, password, rememberMe): boolean
        +logout()
        +getCurrentUser(): User
    }
    
    class PacketCaptureService {
        -instance: PacketCaptureService
        -handle: PcapHandle
        -isCapturing: AtomicBoolean
        -captureTask: Future
        +getInstance(): PacketCaptureService
        +startCapture(deviceName): boolean
        +stopCapture()
        +getAvailableInterfaces(): List
    }
    
    class DetectionEngine {
        -instance: DetectionEngine
        -portScanTracker: ConcurrentHashMap
        -connectionTracker: ConcurrentHashMap
        +getInstance(): DetectionEngine
        +analyzeTraffic(TrafficData, Packet)
        +detectPortScan(sourceIP, destPort)
        +detectDDoS(destIP)
        +detectBruteForce(sourceIP, service)
    }
    
    class DatabaseManager {
        -instance: DatabaseManager
        -dataSource: HikariDataSource
        +getInstance(): DatabaseManager
        +getConnection(): Connection
        +getSchema(): String
    }
    
    class TrafficDAO {
        -dbManager: DatabaseManager
        +getAllTraffic(): List
        +getRecentTraffic(limit): List
        +insertTraffic(TrafficData): boolean
        +deleteAllTraffic(): boolean
    }
    
    class AlertDAO {
        -dbManager: DatabaseManager
        +getAllAlerts(): List
        +insertAlert(SecurityAlert): boolean
        +updateAlertStatus(alertId, status): boolean
    }
    
    class TrafficController {
        -trafficTable: TableView
        -captureService: PacketCaptureService
        -trafficDAO: TrafficDAO
        +initialize()
        +startMonitoring()
        +stopMonitoring()
        +handleClearTraffic()
        +handleExportTraffic()
    }
    
    class LoginController {
        -usernameField: TextField
        -passwordField: PasswordField
        -authService: AuthenticationService
        +handleLogin()
        +handleQuickAdminLogin()
        +handleQuickUserLogin()
    }
    
    class TrafficData {
        -protocol: String
        -sourceIP: String
        -sourcePort: String
        -destinationIP: String
        -destinationPort: String
        -packetSize: long
        -timestamp: String
        -status: String
    }
    
    class SecurityAlert {
        -id: String
        -severity: String
        -type: String
        -sourceIP: String
        -destinationIP: String
        -description: String
        -timestamp: LocalDateTime
        -status: String
    }
    
    class User {
        -username: String
        -passwordHash: String
        -role: String
    }
    
    Main --> AuthenticationService
    Main --> DatabaseManager
    Main --> DetectionEngine
    
    TrafficController --> PacketCaptureService
    TrafficController --> TrafficDAO
    TrafficController --> DetectionEngine
    
    LoginController --> AuthenticationService
    
    PacketCaptureService --> TrafficDAO
    PacketCaptureService --> DetectionEngine
    
    DetectionEngine --> AlertDAO
    
    TrafficDAO --> DatabaseManager
    AlertDAO --> DatabaseManager
    
    TrafficDAO ..> TrafficData
    AlertDAO ..> SecurityAlert
    AuthenticationService ..> User
```

### Relations entre Classes

| Classe Source | Relation | Classe Cible | Description |
|---------------|----------|--------------|-------------|
| Main | utilise | AuthenticationService | Initialisation au démarrage |
| Main | utilise | DatabaseManager | Connexion BD au démarrage |
| TrafficController | utilise | PacketCaptureService | Capture des paquets |
| TrafficController | utilise | TrafficDAO | Accès aux données trafic |
| PacketCaptureService | utilise | DetectionEngine | Analyse des paquets |
| DetectionEngine | utilise | AlertDAO | Sauvegarde des alertes |
| TrafficDAO | utilise | DatabaseManager | Accès BD |

---

## 3. Diagrammes de Séquence

### 3.1 Séquence: Authentification

```mermaid
sequenceDiagram
    participant U as Utilisateur
    participant LC as LoginController
    participant AS as AuthenticationService
    participant MC as MainController
    
    U->>LC: Clic "Sign In" ou "Quick Admin Login"
    LC->>AS: authenticate(username, password)
    
    alt Authentification réussie
        AS-->>LC: true + User object
        LC->>MC: openMainApplication()
        MC-->>U: Affiche Dashboard
    else Échec authentification
        AS-->>LC: false
        LC-->>U: Affiche erreur "Invalid credentials"
    end
```

### 3.2 Séquence: Capture de Trafic

```mermaid
sequenceDiagram
    participant U as Utilisateur
    participant TC as TrafficController
    participant PCS as PacketCaptureService
    participant DE as DetectionEngine
    participant TD as TrafficDAO
    participant DB as Database
    
    U->>TC: Clic "Start Capture"
    TC->>PCS: startCapture(interfaceName)
    PCS->>PCS: openLive(interface)
    PCS-->>TC: true (capture démarrée)
    TC-->>U: Status: "Monitoring Active" (vert)
    
    loop Pour chaque paquet capturé
        PCS->>PCS: processPacket(packet)
        PCS->>DE: analyzeTraffic(trafficData, packet)
        DE->>DE: detectPortScan()
        DE->>DE: detectDDoS()
        DE->>DE: detectBruteForce()
        PCS->>TD: insertTraffic(trafficData)
        TD->>DB: INSERT INTO traffic_logs
    end
    
    U->>TC: Clic "Stop Capture"
    TC->>PCS: stopCapture()
    PCS->>PCS: breakLoop() + wait thread
    PCS-->>TC: Capture arrêtée
    TC-->>U: Status: "Stopped" (rouge)
```

### 3.3 Séquence: Détection de Menace

```mermaid
sequenceDiagram
    participant PCS as PacketCaptureService
    participant DE as DetectionEngine
    participant AD as AlertDAO
    participant ANS as AlertNotificationService
    participant DB as Database
    
    PCS->>DE: analyzeTraffic(trafficData, packet)
    
    alt Port Scan détecté (>10 ports en 60s)
        DE->>DE: detectPortScan() returns true
        DE->>DE: createAlert("HIGH", "Port Scan")
        DE->>AD: insertAlert(alert)
        AD->>DB: INSERT INTO alerts
        DE->>ANS: sendAlertNotification(alert)
    end
    
    alt DDoS détecté (>1000 paquets/s)
        DE->>DE: detectDDoS() returns true
        DE->>DE: createAlert("CRITICAL", "DDoS Attack")
        DE->>AD: insertAlert(alert)
        AD->>DB: INSERT INTO alerts
        DE->>ANS: sendAlertNotification(alert)
    end
```

---

## 4. Architecture du Système

```
┌─────────────────────────────────────────────────────────────┐
│                    PRÉSENTATION (JavaFX)                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐│
│  │  Login   │ │Dashboard │ │ Traffic  │ │     Alerts       ││
│  │Controller│ │Controller│ │Controller│ │   Controller     ││
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    SERVICES (Singleton)                      │
│  ┌────────────────┐ ┌────────────────┐ ┌──────────────────┐ │
│  │Authentication  │ │ PacketCapture  │ │   Detection      │ │
│  │   Service      │ │   Service      │ │    Engine        │ │
│  └────────────────┘ └────────────────┘ └──────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    ACCÈS DONNÉES (DAO)                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐│
│  │TrafficDAO│ │ AlertDAO │ │ UserDAO  │ │   AccountDAO     ││
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘│
│                    ┌──────────────────┐                      │
│                    │ DatabaseManager  │                      │
│                    │   (HikariCP)     │                      │
│                    └──────────────────┘                      │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    BASE DE DONNÉES                           │
│                      PostgreSQL                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐│
│  │  users   │ │  alerts  │ │ traffic  │ │  system_stats    ││
│  │          │ │          │ │  _logs   │ │                  ││
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Modèle de Données (Schéma BD)

```mermaid
erDiagram
    USERS {
        int user_id PK
        string username UK
        string password_hash
        string role
        timestamp created_at
    }
    
    ALERTS {
        int alert_id PK
        string severity
        string alert_type
        string source_ip
        string destination_ip
        string description
        string status
        timestamp created_at
    }
    
    TRAFFIC_LOGS {
        int log_id PK
        string protocol
        string source_ip
        int source_port
        string destination_ip
        int destination_port
        bigint packet_size
        string status
        timestamp captured_at
    }
    
    SYSTEM_STATS {
        int stat_id PK
        bigint total_packets
        bigint total_bytes
        int active_connections
        timestamp recorded_at
    }
```

---

## 6. Technologies Utilisées

| Catégorie | Technologie | Version |
|-----------|-------------|---------|
| Langage | Java | 23 |
| Framework UI | JavaFX | 21.0.6 |
| Base de données | PostgreSQL | 16 |
| Pool de connexions | HikariCP | 5.1.0 |
| Capture réseau | Pcap4J | 1.8.2 |
| Build | Maven | 3.9 |
| Email | JavaMail API | 1.6.2 |
| Logging | SLF4J | 2.0.9 |

---

## 7. Résumé Bref du Projet

> **IDS Monitor** est une application de monitoring réseau développée en Java/JavaFX qui utilise la bibliothèque **Pcap4J** pour capturer le trafic réseau en temps réel. L'application analyse chaque paquet pour détecter des menaces de sécurité comme les **scans de ports**, les **attaques DDoS**, et les **tentatives de brute force**.
>
> L'architecture suit le pattern **MVC** (Model-View-Controller) avec une couche de services utilisant le pattern **Singleton** pour garantir une instance unique des services critiques. Les données sont stockées dans **PostgreSQL** via le pattern **DAO** (Data Access Object).
>
> Le système supporte trois rôles d'utilisateurs: **Administrateur** (accès complet), **Utilisateur** (monitoring sans admin), et **Viewer** (lecture seule).
