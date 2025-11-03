package com.example.fuelticket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service pour l'envoi de SMS
 * Pour la production, intégrer un service SMS réel (Twilio, Vonage, Orange SMS API, etc.)
 */
@Service
public class SmsService {
    
    @Value("${app.sms.enabled:true}")
    private boolean smsEnabled;
    
    @Value("${app.sms.api.url:}")
    private String smsApiUrl;
    
    @Value("${app.sms.api.key:}")
    private String smsApiKey;
    
    @Value("${app.sms.api.secret:}")
    private String smsApiSecret;
    
    @Value("${app.sms.sender:Taadjii Ko Lafia}")
    private String smsSender;
    
    /**
     * Envoie un code de vérification par SMS
     * En mode développement (smsEnabled=false), affiche simplement le code dans les logs
     */
    public void sendVerificationSms(String telephone, String nom, String verificationCode) {
        if (!smsEnabled) {
            System.out.println("📱 [SMS SIMULATION] Code de vérification pour " + telephone + " (" + nom + ") : " + verificationCode);
            System.out.println("📱 [SMS SIMULATION] Message: Votre code de vérification Taadjii Ko Lafia est: " + verificationCode);
            return;
        }
        
        try {
            // Pour la production, utiliser un service SMS réel
            // Exemple avec une API SMS générique (à adapter selon le fournisseur)
            if (smsApiUrl != null && !smsApiUrl.isEmpty()) {
                sendSmsViaApi(telephone, buildVerificationMessage(nom, verificationCode));
            } else {
                // Fallback: simulation en développement
                System.out.println("📱 [SMS] Code de vérification pour " + telephone + " (" + nom + ") : " + verificationCode);
                System.out.println("📱 [SMS] Message: " + buildVerificationMessage(nom, verificationCode));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du SMS à " + telephone + " : " + e.getMessage());
            // En cas d'erreur, afficher le code dans les logs pour le développement
            System.out.println("⚠️  Le code de vérification est : " + verificationCode + " (à utiliser manuellement si le SMS n'a pas été envoyé)");
            // Ne pas faire échouer l'inscription en cas d'erreur SMS
        }
    }
    
    /**
     * Envoie un SMS via une API (à adapter selon le fournisseur)
     */
    private void sendSmsViaApi(String telephone, String message) throws Exception {
        // Exemple d'intégration avec une API SMS
        // À adapter selon le fournisseur choisi (Twilio, Vonage, Orange, etc.)
        
        if (smsApiUrl == null || smsApiUrl.isEmpty()) {
            throw new IllegalStateException("SMS API URL non configurée");
        }
        
        // Exemple avec une API REST générique
        String urlString = String.format("%s?to=%s&message=%s&key=%s&secret=%s",
                smsApiUrl,
                URLEncoder.encode(telephone, StandardCharsets.UTF_8),
                URLEncoder.encode(message, StandardCharsets.UTF_8),
                URLEncoder.encode(smsApiKey, StandardCharsets.UTF_8),
                URLEncoder.encode(smsApiSecret, StandardCharsets.UTF_8));
        
        URI uri = URI.create(urlString);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        int responseCode = connection.getResponseCode();
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("✅ SMS envoyé avec succès à : " + telephone);
        } else {
            throw new RuntimeException("Erreur HTTP " + responseCode + " lors de l'envoi du SMS");
        }
    }
    
    /**
     * Construit le message SMS de vérification
     */
    private String buildVerificationMessage(String nom, String code) {
        return String.format("Bonjour %s, votre code de vérification Taadjii Ko Lafia est: %s. Valide 15 min.", 
                nom != null ? nom : "Utilisateur", code);
    }
    
    /**
     * Vérifie la validité d'un numéro de téléphone malien
     * Format attendu: +223XXXXXXXX (8 chiffres après +223)
     */
    public boolean isValidMalienPhoneNumber(String telephone) {
        if (telephone == null || telephone.trim().isEmpty()) {
            return false;
        }
        
        // Format: +223XXXXXXXX
        return telephone.matches("^\\+223[0-9]{8}$");
    }
    
    /**
     * Formate un numéro de téléphone pour l'envoi SMS
     * Enlève le + si nécessaire selon l'API utilisée
     */
    public String formatPhoneForSms(String telephone) {
        if (telephone == null) {
            return null;
        }
        
        // Pour certaines APIs, il faut enlever le +
        // À adapter selon le fournisseur
        return telephone.startsWith("+") ? telephone.substring(1) : telephone;
    }
}

