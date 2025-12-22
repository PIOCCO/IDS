package org.example.services;

import org.example.models.SecurityAlert;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for sending alert notifications via email
 */
public class AlertNotificationService {
    private static AlertNotificationService instance;
    private ExecutorService executorService;
    private boolean emailEnabled;
    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String recipientEmail;
    private boolean useSSL;

    private AlertNotificationService() {
        this.executorService = Executors.newSingleThreadExecutor();
        loadConfiguration();
    }

    public static synchronized AlertNotificationService getInstance() {
        if (instance == null) {
            instance = new AlertNotificationService();
        }
        return instance;
    }

    /**
     * Load email configuration
     */
    private void loadConfiguration() {
        // Load from properties file or use defaults
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader()
                    .getResourceAsStream("notification.properties"));

            this.emailEnabled = Boolean.parseBoolean(
                    props.getProperty("email.enabled", "false"));
            this.smtpHost = props.getProperty("email.smtp.host", "smtp.gmail.com");
            this.smtpPort = Integer.parseInt(
                    props.getProperty("email.smtp.port", "587"));
            this.smtpUsername = props.getProperty("email.username", "");
            this.smtpPassword = props.getProperty("email.password", "");
            this.recipientEmail = props.getProperty("email.recipient", "");
            this.useSSL = Boolean.parseBoolean(
                    props.getProperty("email.ssl", "true"));

        } catch (Exception e) {
            System.err.println("Failed to load email configuration: " + e.getMessage());
            this.emailEnabled = false;
        }
    }

    /**
     * Send alert notification
     */
    public void sendAlert(SecurityAlert alert) {
        // Only send for Critical and High severity alerts
        if (!alert.getSeverity().equals("Critical") &&
                !alert.getSeverity().equals("High")) {
            return;
        }

        // Send email notification asynchronously
        if (emailEnabled && !recipientEmail.isEmpty()) {
            executorService.submit(() -> sendEmailAlert(alert));
        }

        // Could add more notification channels here:
        // - Slack webhook
        // - SMS via Twilio
        // - Push notifications
        // - Syslog
    }

    /**
     * Send email alert
     */
    private void sendEmailAlert(SecurityAlert alert) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));
            props.put("mail.smtp.auth", "true");

            if (useSSL) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.ssl.trust", smtpHost);
            }

            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUsername));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail)
            );

            // Set subject with severity indicator
            String severityEmoji = getSeverityEmoji(alert.getSeverity());
            message.setSubject(String.format(
                    "%s [%s] IDS Alert: %s",
                    severityEmoji,
                    alert.getSeverity().toUpperCase(),
                    alert.getType()
            ));

            // Create email body
            String emailBody = buildEmailBody(alert);
            message.setContent(emailBody, "text/html; charset=utf-8");

            // Send email
            Transport.send(message);

            System.out.println("Email alert sent for: " + alert.getId());

        } catch (MessagingException e) {
            System.err.println("Failed to send email alert: " + e.getMessage());
        }
    }

    /**
     * Build HTML email body
     */
    private String buildEmailBody(SecurityAlert alert) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .header { background-color: %s; color: white; padding: 20px; border-radius: 8px 8px 0 0; margin: -30px -30px 20px -30px; }
                    .alert-title { font-size: 24px; font-weight: bold; margin: 0; }
                    .alert-subtitle { font-size: 14px; opacity: 0.9; margin: 5px 0 0 0; }
                    .details { margin: 20px 0; }
                    .detail-row { margin: 10px 0; padding: 10px; background-color: #f8f9fa; border-radius: 4px; }
                    .label { font-weight: bold; color: #555; display: inline-block; width: 150px; }
                    .value { color: #333; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; font-size: 12px; color: #777; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <p class="alert-title">%s Security Alert Detected</p>
                        <p class="alert-subtitle">IDS Monitor - Alert ID: %s</p>
                    </div>
                    
                    <div class="details">
                        <div class="detail-row">
                            <span class="label">Alert Type:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Severity:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Source IP:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Destination IP:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Timestamp:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Description:</span>
                            <span class="value">%s</span>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>This is an automated alert from your IDS Monitor system.</p>
                        <p>Please review the alert and take appropriate action if necessary.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                getSeverityColor(alert.getSeverity()),
                getSeverityEmoji(alert.getSeverity()),
                alert.getId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getSourceIP(),
                alert.getDestinationIP(),
                alert.getTimestamp().toString(),
                alert.getDescription()
        );
    }

    /**
     * Get color for severity level
     */
    private String getSeverityColor(String severity) {
        return switch (severity.toLowerCase()) {
            case "critical" -> "#dc3545";
            case "high" -> "#fd7e14";
            case "medium" -> "#ffc107";
            case "low" -> "#28a745";
            default -> "#6c757d";
        };
    }

    /**
     * Get emoji for severity level
     */
    private String getSeverityEmoji(String severity) {
        return switch (severity.toLowerCase()) {
            case "critical" -> "🚨";
            case "high" -> "⚠️";
            case "medium" -> "⚡";
            case "low" -> "ℹ️";
            default -> "🔔";
        };
    }

    /**
     * Configure email settings
     */
    public void configureEmail(String host, int port, String username,
                               String password, String recipient, boolean ssl) {
        this.smtpHost = host;
        this.smtpPort = port;
        this.smtpUsername = username;
        this.smtpPassword = password;
        this.recipientEmail = recipient;
        this.useSSL = ssl;
    }

    /**
     * Enable or disable email notifications
     */
    public void setEmailEnabled(boolean enabled) {
        this.emailEnabled = enabled;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    /**
     * Test email configuration
     */
    public boolean testEmailConfiguration() {
        try {
            SecurityAlert testAlert = new SecurityAlert(
                    "TEST-00000",
                    "Info",
                    "Test Alert",
                    "0.0.0.0",
                    "0.0.0.0",
                    "This is a test alert to verify email configuration",
                    java.time.LocalDateTime.now()
            );

            sendEmailAlert(testAlert);
            return true;
        } catch (Exception e) {
            System.err.println("Email test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Shutdown service
     */
    public void shutdown() {
        executorService.shutdown();
    }
}