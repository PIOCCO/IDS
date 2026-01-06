# IDS Monitor - Système de Détection d'Intrusions

[![Java](https://img.shields.io/badge/Java-23-orange.svg)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue.svg)](https://openjfx.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9-red.svg)](https://maven.apache.org/)

## 📋 Description

**IDS Monitor** est une application de monitoring réseau développée en Java/JavaFX permettant de :

- 🔍 Capturer et analyser le trafic réseau en temps réel
- ⚠️ Détecter les menaces (Port Scan, DDoS, Brute Force, SQL Injection, XSS)
- 🔔 Gérer les alertes de sécurité avec notifications
- 📊 Visualiser les métriques via un tableau de bord interactif
- 📈 Générer des rapports détaillés avec export CSV

---

## 🏗️ Architecture du Projet

```
src/main/java/org/example/
├── controllers/        # Contrôleurs JavaFX (MVC)
│   ├── DashboardController.java
│   ├── LoginController.java
│   ├── MainController.java
│   ├── TrafficController.java
│   └── ...
├── dao/                # Couche d'Accès aux Données
│   ├── base/           # BaseDAO générique
│   ├── impl/           # Implémentations DAO
│   ├── interfaces/     # Interfaces DAO
│   ├── DAOFactory.java
│   └── *.java          # DAOs concrets
├── models/             # Modèles de données (7 classes)
│   ├── User.java
│   ├── TrafficData.java
│   ├── SecurityAlert.java
│   ├── MonitoringSession.java
│   ├── SessionStatistics.java
│   ├── SessionSnapshot.java
│   └── ChartMetric.java
├── services/           # Services métier
│   ├── AuthenticationService.java
│   ├── PacketCaptureService.java
│   ├── DetectionEngine.java
│   └── ...
├── utils/              # Utilitaires
│   ├── DatabaseManager.java
│   └── ...
└── IDSMonitorApplication.java
```

---

## 🚀 Installation

### Prérequis

- **Java JDK 23+**
- **Maven 3.9+**
- **PostgreSQL 16+**
- **Npcap** (Windows) ou **libpcap** (Linux/macOS)

### Configuration de la Base de Données

1. Créer une base de données PostgreSQL :
```sql
CREATE DATABASE ids_monitor;
CREATE SCHEMA ids_schema;
```

2. Configurer `src/main/resources/database.properties` :
```properties
db.url=jdbc:postgresql://localhost:5432/ids_monitor
db.schema=ids_schema
db.username=your_username
db.password=your_password
```

### Compilation et Exécution

```bash
# Cloner le projet
git clone <repository-url>
cd NLPM

# Compiler
mvn clean compile

# Exécuter l'application
mvn javafx:run
```

---

## 👥 Rôles Utilisateur

| Rôle | Permissions |
|------|-------------|
| **Admin** | Accès complet : gestion utilisateurs, configuration, monitoring |
| **User** | Monitoring, alertes, export (sans administration) |

---

## 📊 Fonctionnalités

### Dashboard
- Vue panoramique des métriques réseau
- Graphiques temps réel avec sélection de catégories (Protocol, Alert, Session, Traffic)
- Tableaux d'alertes récentes et statistiques

### Traffic Monitor
- Capture de paquets en temps réel (interface sélectionnable)
- Filtrage par protocole (TCP, UDP, ICMP, etc.)
- Export CSV des données capturées

### Alertes
- Détection automatique : Port Scan, DDoS, Brute Force
- Classification par sévérité (CRITICAL, HIGH, MEDIUM, LOW)
- Actions : Acknowledge, Resolve, Delete

### Reports
- Historique des sessions de monitoring
- Statistiques agrégées
- Export des rapports

---

## 🛠️ Technologies

| Catégorie | Technologie |
|-----------|-------------|
| Langage | Java 23 |
| UI | JavaFX 21.0.6 |
| Base de données | PostgreSQL 16 |
| Pool connexions | HikariCP 5.1.0 |
| Capture réseau | Pcap4J 1.8.2 |
| Build | Maven 3.9 |

---

## 📖 Documentation

La documentation UML complète est disponible dans `docs/UML_Documentation.md`.

---

## 📝 Licence

Ce projet est développé dans le cadre académique.
