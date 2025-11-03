package com.example.fuelticket.service;

import com.example.fuelticket.entity.Ticket;
import com.example.fuelticket.entity.User;
import com.example.fuelticket.entity.Station;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnClass(JavaMailSender.class)
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.email.from:noreply@taadjiikolafia.ml}")
    private String fromEmail;
    
    @Value("${app.email.from.name:Taadjii Ko Lafia}")
    private String fromName;
    
    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${app.server.url:http://localhost:8000}")
    private String serverUrl;
    
    public void sendVerificationEmail(String to, String nom, String verificationCode) {
        if (!emailEnabled) {
            System.out.println("⚠️ L'envoi d'email est désactivé. Code de vérification pour " + to + " : " + verificationCode);
            return;
        }
        
        String subject = "Vérification de votre compte - Taadjii Ko Lafia";
        String htmlContent = buildVerificationEmailHtml(nom, verificationCode);
        
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom(fromEmail, fromName);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(htmlContent, true);
        };
        
        try {
            mailSender.send(messagePreparator);
            System.out.println("✅ Email de vérification envoyé avec succès à : " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de vérification à " + to + " : " + e.getMessage());
            System.err.println("   Détails de l'erreur : " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("   Cause : " + e.getCause().getMessage());
            }
            System.err.println("⚠️  Le code de vérification est : " + verificationCode + " (à utiliser manuellement si l'email n'a pas été envoyé)");
            // Ne pas faire échouer l'inscription en cas d'erreur d'email
            // throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification", e);
        }
    }
    
    public void sendPasswordResetEmail(String to, String nom, String resetCode) {
        if (!emailEnabled) {
            System.out.println("⚠️ L'envoi d'email est désactivé. Code de réinitialisation pour " + to + " : " + resetCode);
            return;
        }
        
        String subject = "Réinitialisation de votre mot de passe - Taadjii Ko Lafia";
        String htmlContent = buildPasswordResetEmailHtml(nom, resetCode);
        
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom(fromEmail, fromName);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(htmlContent, true);
        };
        
        try {
            mailSender.send(messagePreparator);
            System.out.println("✅ Email de réinitialisation envoyé avec succès à : " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de réinitialisation à " + to + " : " + e.getMessage());
            System.err.println("   Détails de l'erreur : " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("   Cause : " + e.getCause().getMessage());
            }
            System.err.println("⚠️  Le code de réinitialisation est : " + resetCode + " (à utiliser manuellement si l'email n'a pas été envoyé)");
        }
    }
    
    public void sendWelcomeEmail(String to, String nom, String role) {
        if (!emailEnabled) {
            System.out.println("⚠️ L'envoi d'email est désactivé. Email de bienvenue pour " + to + " ignoré.");
            return;
        }
        
        String subject = "Bienvenue sur Taadjii Ko Lafia !";
        String htmlContent = buildWelcomeEmailHtml(nom, role);
        
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom(fromEmail, fromName);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(htmlContent, true);
        };
        
        try {
            mailSender.send(messagePreparator);
            System.out.println("✅ Email de bienvenue envoyé avec succès à : " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de bienvenue à " + to + " : " + e.getMessage());
            System.err.println("   Détails de l'erreur : " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("   Cause : " + e.getCause().getMessage());
            }
            // Ne pas faire échouer la vérification en cas d'erreur d'email
            // throw new RuntimeException("Erreur lors de l'envoi de l'email de bienvenue", e);
        }
    }
    
    private String buildPasswordResetEmailHtml(String nom, String resetCode) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Réinitialisation de mot de passe</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .reset-code { background: #fff; border: 2px solid #ff5722; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
                    .code { font-size: 32px; font-weight: bold; color: #ff5722; letter-spacing: 5px; }
                    .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⛽ Taadjii Ko Lafia</h1>
                        <p>Réinitialisation de votre mot de passe</p>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Vous avez demandé à réinitialiser votre mot de passe sur Taadjii Ko Lafia.</p>
                        
                        <div class="reset-code">
                            <p>Votre code de réinitialisation :</p>
                            <div class="code">%s</div>
                            <p><small>Ce code expire dans 15 minutes</small></p>
                        </div>
                        
                        <div class="warning">
                            <p><strong>⚠️ Important :</strong></p>
                            <p>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email. Votre mot de passe ne sera pas modifié.</p>
                        </div>
                        
                        <p>Utilisez ce code pour réinitialiser votre mot de passe sur la plateforme.</p>
                        
                        <div class="footer">
                            <p>© 2025 Taadjii Ko Lafia - Gestion transparente du carburant au Mali</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, nom, resetCode);
    }
    
    private String buildVerificationEmailHtml(String nom, String verificationCode) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Vérification de compte</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .verification-code { background: #fff; border: 2px solid #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
                    .code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⛽ Taadjii Ko Lafia</h1>
                        <p>Vérification de votre compte</p>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Merci de vous être inscrit sur Taadjii Ko Lafia ! Pour activer votre compte, veuillez utiliser le code de vérification ci-dessous :</p>
                        
                        <div class="verification-code">
                            <p>Votre code de vérification :</p>
                            <div class="code">%s</div>
                            <p><small>Ce code expire dans 15 minutes</small></p>
                        </div>
                        
                        <p>Si vous n'avez pas créé de compte sur Taadjii Ko Lafia, veuillez ignorer cet email.</p>
                        
                        <div class="footer">
                            <p>© 2025 Taadjii Ko Lafia - Gestion transparente du carburant au Mali</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, nom, verificationCode);
    }
    
    private String buildWelcomeEmailHtml(String nom, String role) {
        String roleText = switch (role) {
            case "CITOYEN" -> "citoyen";
            case "STATION" -> "gérant de station";
            default -> "utilisateur";
        };
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Bienvenue sur Taadjii Ko Lafia</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .welcome-box { background: #fff; border-left: 4px solid #28a745; padding: 20px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⛽ Taadjii Ko Lafia</h1>
                        <p>Bienvenue !</p>
                    </div>
                    <div class="content">
                        <h2>Félicitations %s !</h2>
                        
                        <div class="welcome-box">
                            <h3>🎉 Votre compte a été activé avec succès</h3>
                            <p>Vous êtes maintenant %s sur la plateforme Taadjii Ko Lafia.</p>
                        </div>
                        
                        <p>Vous pouvez maintenant :</p>
                        <ul>
                            <li>Consulter les stations de carburant disponibles</li>
                            <li>Créer des tickets de carburant</li>
                            <li>Suivre l'état de vos demandes</li>
                        </ul>
                        
                        <p>Connectez-vous dès maintenant pour commencer à utiliser la plateforme !</p>
                        
                        <div class="footer">
                            <p>© 2025 Taadji Ko Lafia - Gestion transparente du carburant au Mali</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, nom, roleText);
    }
    
    public void sendNewTicketNotificationToManager(String to, String managerNom, String stationNom, 
                                                     Ticket ticket, 
                                                     User citoyen) {
        if (!emailEnabled) {
            System.out.println("⚠️ L'envoi d'email est désactivé. Notification de nouveau ticket pour " + to + " ignorée.");
            return;
        }
        
        String subject = "Nouveau ticket créé - Station " + stationNom + " - Taadjii Ko Lafia";
        String htmlContent = buildNewTicketNotificationHtml(managerNom, stationNom, ticket, citoyen);
        
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom(fromEmail, fromName);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(htmlContent, true);
        };
        
        try {
            mailSender.send(messagePreparator);
            System.out.println("✅ Email de notification de nouveau ticket envoyé avec succès à : " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de notification à " + to + " : " + e.getMessage());
            System.err.println("   Détails de l'erreur : " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("   Cause : " + e.getCause().getMessage());
            }
            // Ne pas faire échouer la création du ticket en cas d'erreur d'email
        }
    }
    
    private String buildNewTicketNotificationHtml(String managerNom, String stationNom, 
                                                   Ticket ticket,
                                                   User citoyen) {
        String statutText = switch (ticket.getStatut().toString()) {
            case "EN_ATTENTE" -> "En attente";
            case "VALIDE" -> "Validé";
            case "SERVI" -> "Servi";
            case "ANNULE" -> "Annulé";
            case "EXPIRE" -> "Expiré";
            default -> ticket.getStatut().toString();
        };
        
        String typeCarburantDisplay = switch (ticket.getTypeCarburant().toUpperCase()) {
            case "ESSENCE" -> "Essence";
            case "DIESEL" -> "Diesel";
            case "GPL" -> "GPL";
            case "KEROSENE" -> "Kérosène";
            default -> ticket.getTypeCarburant();
        };
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Nouveau ticket créé</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .ticket-box { background: #fff; border: 2px solid #667eea; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .ticket-header { background: #667eea; color: white; padding: 15px; margin: -20px -20px 20px -20px; border-radius: 6px 6px 0 0; }
                    .ticket-info { margin: 10px 0; }
                    .ticket-info strong { color: #667eea; }
                    .citoyen-info { background: #e8f4f8; padding: 15px; border-radius: 8px; margin: 15px 0; }
                    .status-badge { display: inline-block; padding: 5px 15px; border-radius: 20px; font-weight: bold; margin-left: 10px; }
                    .status-en-attente { background: #ffc107; color: #000; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⛽ Taadjii Ko Lafia</h1>
                        <p>Nouveau ticket créé</p>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Un nouveau ticket a été créé pour votre station <strong>%s</strong>.</p>
                        
                        <div class="ticket-box">
                            <div class="ticket-header">
                                <h3 style="margin: 0;">📋 Détails du ticket</h3>
                            </div>
                            <div class="ticket-info">
                                <strong>Numéro de ticket :</strong> %s<br>
                                <strong>Numéro d'ordre :</strong> %s<br>
                                <strong>Statut :</strong> %s <span class="status-badge status-en-attente">%s</span><br>
                                <strong>Type de carburant :</strong> %s<br>
                                <strong>Quantité :</strong> %.2f L<br>
                                <strong>Date d'approvisionnement :</strong> %s<br>
                                <strong>Date de création :</strong> %s<br>
                                <strong>Date d'expiration :</strong> %s
                            </div>
                            
                            <div class="citoyen-info">
                                <h4 style="margin-top: 0; color: #667eea;">👤 Informations du citoyen</h4>
                                <div class="ticket-info">
                                    <strong>Nom complet :</strong> %s %s<br>
                                    <strong>Email :</strong> %s<br>
                                    <strong>Téléphone :</strong> %s
                                </div>
                            </div>
                        </div>
                        
                        <p><strong>Action requise :</strong> Veuillez traiter ce ticket dans votre système et confirmer lorsque le carburant sera servi.</p>
                        
                        <div class="footer">
                            <p>© 2025 Taadjii Ko Lafia - Gestion transparente du carburant au Mali</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, 
            managerNom,
            stationNom,
            ticket.getNumeroTicket(),
            ticket.getNumeroOrdre(),
            statutText,
            statutText,
            typeCarburantDisplay,
            ticket.getQuantite(),
            ticket.getDateApprovisionnement() != null ? ticket.getDateApprovisionnement().toString() : "N/A",
            ticket.getDateCreation() != null ? ticket.getDateCreation().toString() : "N/A",
            ticket.getDateExpiration() != null ? ticket.getDateExpiration().toString() : "N/A",
            citoyen.getNom() != null ? citoyen.getNom() : ticket.getNomCitoyen() != null ? ticket.getNomCitoyen() : "N/A",
            citoyen.getPrenom() != null ? citoyen.getPrenom() : ticket.getPrenomCitoyen() != null ? ticket.getPrenomCitoyen() : "N/A",
            citoyen.getEmail() != null ? citoyen.getEmail() : ticket.getEmailCitoyen() != null ? ticket.getEmailCitoyen() : "N/A",
            citoyen.getTelephone() != null ? citoyen.getTelephone() : ticket.getTelephoneCitoyen() != null ? ticket.getTelephoneCitoyen() : "N/A"
        );
    }
    
    public void sendTicketReceiptToCitizen(String to, String nomComplet, Ticket ticket, String downloadUrl) {
        if (!emailEnabled) {
            System.out.println("⚠️ L'envoi d'email est désactivé. Reçu PDF pour " + to + " ignoré.");
            return;
        }
        
        String subject = "Reçu de votre ticket de carburant - " + ticket.getNumeroTicket() + " - Taadjii Ko Lafia";
        
        // Construire l'URL complète de téléchargement
        String fullDownloadUrl = serverUrl + (downloadUrl.startsWith("/") ? downloadUrl : "/" + downloadUrl);
        String htmlContent = buildTicketReceiptEmailHtml(nomComplet, ticket, fullDownloadUrl);
        
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            messageHelper.setFrom(fromEmail, fromName);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(htmlContent, true);
        };
        
        try {
            mailSender.send(messagePreparator);
            System.out.println("✅ Reçu PDF envoyé avec succès à : " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du reçu PDF à " + to + " : " + e.getMessage());
            System.err.println("   Détails de l'erreur : " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("   Cause : " + e.getCause().getMessage());
            }
            // Ne pas faire échouer la validation du ticket en cas d'erreur d'email
        }
    }
    
    private String buildTicketReceiptEmailHtml(String nomComplet, Ticket ticket, String downloadUrl) {
        String statutText = switch (ticket.getStatut().toString()) {
            case "EN_ATTENTE" -> "En attente";
            case "VALIDE" -> "Validé";
            case "SERVI" -> "Servi";
            case "ANNULE" -> "Annulé";
            case "EXPIRE" -> "Expiré";
            default -> ticket.getStatut().toString();
        };
        
        String typeCarburantDisplay = switch (ticket.getTypeCarburant().toUpperCase()) {
            case "ESSENCE" -> "Essence";
            case "DIESEL" -> "Diesel";
            case "GPL" -> "GPL";
            case "KEROSENE" -> "Kérosène";
            default -> ticket.getTypeCarburant();
        };
        
        Station station = ticket.getStation();
        String stationNom = station != null ? station.getNom() : "N/A";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reçu de ticket de carburant</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .receipt-box { background: #fff; border: 2px solid #28a745; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .receipt-header { background: #28a745; color: white; padding: 15px; margin: -20px -20px 20px -20px; border-radius: 6px 6px 0 0; }
                    .receipt-info { margin: 10px 0; }
                    .receipt-info strong { color: #28a745; }
                    .status-valid { background: #28a745; color: white; display: inline-block; padding: 5px 15px; border-radius: 20px; font-weight: bold; margin-left: 10px; }
                    .qr-code-note { background: #e8f5e9; border-left: 4px solid #28a745; padding: 15px; margin: 20px 0; }
                    .download-button { display: inline-block; background: #28a745; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; text-align: center; }
                    .download-button:hover { background: #218838; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⛽ Taadjii Ko Lafia</h1>
                        <p>Votre ticket de carburant a été validé</p>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Votre ticket de carburant a été validé avec succès. Vous pouvez télécharger votre reçu PDF contenant toutes les informations ainsi que le code QR en cliquant sur le bouton ci-dessous.</p>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="download-button">📥 Télécharger le reçu PDF</a>
                        </div>
                        
                        <div class="receipt-box">
                            <div class="receipt-header">
                                <h3 style="margin: 0;">📋 Détails du ticket</h3>
                            </div>
                            <div class="receipt-info">
                                <strong>Numéro de ticket :</strong> %s<br>
                                <strong>Numéro d'ordre :</strong> %s<br>
                                <strong>Statut :</strong> %s <span class="status-valid">%s</span><br>
                                <strong>Station :</strong> %s<br>
                                <strong>Type de carburant :</strong> %s<br>
                                <strong>Quantité :</strong> %.2f L<br>
                                <strong>Montant payé :</strong> %.0f FCFA<br>
                                <strong>Date d'approvisionnement :</strong> %s<br>
                                <strong>Date de création :</strong> %s
                            </div>
                            
                            <div class="qr-code-note">
                                <p><strong>📱 Code QR :</strong></p>
                                <p>Le PDF téléchargeable contient un code QR que vous pourrez présenter lors de l'approvisionnement pour faciliter la validation.</p>
                            </div>
                        </div>
                        
                        <p><strong>Lien de téléchargement :</strong></p>
                        <p>Si le bouton ne fonctionne pas, copiez et collez ce lien dans votre navigateur :</p>
                        <p style="word-break: break-all; color: #28a745;"><a href="%s">%s</a></p>
                        
                        <p><strong>Prochaines étapes :</strong></p>
                        <ul>
                            <li>Téléchargez et conservez le reçu PDF avec le code QR</li>
                            <li>Présentez-vous à la station <strong>%s</strong> à la date prévue</li>
                            <li>Montrez le code QR ou le numéro de ticket pour recevoir votre carburant</li>
                        </ul>
                        
                        <p>Merci d'avoir utilisé Taadjii Ko Lafia !</p>
                        
                        <div class="footer">
                            <p>© 2025 Taadjii Ko Lafia - Gestion transparente du carburant au Mali</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, 
            nomComplet,
            downloadUrl,
            ticket.getNumeroTicket(),
            ticket.getNumeroOrdre() != null ? ticket.getNumeroOrdre() : "N/A",
            statutText,
            statutText,
            stationNom,
            typeCarburantDisplay,
            ticket.getQuantite(),
            ticket.getMontantPaye() != null ? ticket.getMontantPaye() : 0.0,
            ticket.getDateApprovisionnement() != null ? ticket.getDateApprovisionnement().toString() : "N/A",
            ticket.getDateCreation() != null ? ticket.getDateCreation().toString() : "N/A",
            downloadUrl,
            downloadUrl,
            stationNom
        );
    }
}
