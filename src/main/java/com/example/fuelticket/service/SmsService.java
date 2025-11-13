package com.example.fuelticket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service pour l'envoi de SMS
 * Supporte Orange SMS API avec OAuth2
 */
@Service
public class SmsService {
    
    @Value("${app.sms.enabled:true}")
    private boolean smsEnabled;
    
    @Value("${app.sms.provider:generic}")
    private String smsProvider;
    
    @Value("${app.sms.api.url:}")
    private String smsApiUrl;
    
    @Value("${app.sms.api.key:}")
    private String smsApiKey;
    
    @Value("${app.sms.api.secret:}")
    private String smsApiSecret;
    
    @Value("${app.sms.sender:Taadjii Ko Lafia}")
    private String smsSender;
    
    // Configuration Orange SMS API
    @Value("${app.sms.orange.oauth.url:https://api.orange.com/oauth/v3/token}")
    private String orangeOauthUrl;
    
    @Value("${app.sms.orange.api.url:https://api.orange.com/smsmessaging/v1/outbound}")
    private String orangeApiUrl;
    
    @Value("${app.sms.orange.client.id:}")
    private String orangeClientId;
    
    @Value("${app.sms.orange.client.secret:}")
    private String orangeClientSecret;
    
    @Value("${app.sms.orange.bearer.token:}")
    private String orangeBearerToken;
    
    @Value("${app.sms.orange.sender.address:}")
    private String orangeSenderAddress; // Numéro de téléphone de l'expéditeur (format: +223XXXXXXXX ou 223XXXXXXXX)
    
    @Value("${app.sms.orange.sender.name:}")
    private String orangeSenderName; // Nom d'expéditeur personnalisé (max 11 caractères alphanumériques, sans espaces)
    
    private String cachedBearerToken;
    private long tokenExpiryTime = 0;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
            String message = buildVerificationMessage(nom, verificationCode);
            
            // Utiliser Orange SMS API si configuré
            if ("orange".equalsIgnoreCase(smsProvider) && orangeClientId != null && !orangeClientId.isEmpty()) {
                sendSmsViaOrange(telephone, message);
            } else if (smsApiUrl != null && !smsApiUrl.isEmpty()) {
                // Fallback: API générique
                sendSmsViaApi(telephone, message);
            } else {
                // Fallback: simulation en développement
                System.out.println("📱 [SMS] Code de vérification pour " + telephone + " (" + nom + ") : " + verificationCode);
                System.out.println("📱 [SMS] Message: " + message);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du SMS à " + telephone + " : " + e.getMessage());
            e.printStackTrace();
            // En cas d'erreur, afficher le code dans les logs pour le développement
            System.out.println("⚠️  Le code de vérification est : " + verificationCode + " (à utiliser manuellement si le SMS n'a pas été envoyé)");
            // Ne pas faire échouer l'inscription en cas d'erreur SMS
        }
    }
    
    /**
     * Envoie un SMS via Orange SMS API avec OAuth2
     */
    private void sendSmsViaOrange(String telephone, String message) throws Exception {
        if (orangeClientId == null || orangeClientId.isEmpty() || orangeClientSecret == null || orangeClientSecret.isEmpty()) {
            throw new IllegalStateException("Orange SMS API: Client ID et Client Secret requis");
        }
        
        // Obtenir le bearer token (avec cache)
        String bearerToken = getOrangeBearerToken();
        
        // Formater le numéro de téléphone du destinataire (Orange attend le format international sans +)
        String formattedPhone = formatPhoneForOrange(telephone);
        
        // Obtenir l'adresse de l'expéditeur (doit être un numéro de téléphone)
        // IMPORTANT: Orange SMS API exige que le senderAddress soit au format tel:+22376396922
        // dans l'URL (encodé) ET dans le body (non encodé)
        // Format URL: tel%3A%2B22376396922 (encodage URL de tel:+22376396922)
        // Format Body: tel:+22376396922 (avec tel: et +)
        String senderAddressForBody;
        
        if (orangeSenderAddress != null && !orangeSenderAddress.trim().isEmpty()) {
            // S'assurer que le numéro a le + (format international)
            senderAddressForBody = orangeSenderAddress.startsWith("+") ? orangeSenderAddress : "+" + orangeSenderAddress;
        } else if (smsSender != null && smsSender.matches("^\\+?223[0-9]{8}$")) {
            // Si smsSender est un numéro de téléphone, l'utiliser
            senderAddressForBody = smsSender.startsWith("+") ? smsSender : "+" + smsSender;
        } else {
            // Par défaut, utiliser un numéro générique (à configurer dans application.properties)
            // Orange nécessite un numéro de téléphone valide comme senderAddress
            throw new IllegalStateException("Orange SMS API: app.sms.orange.sender.address doit être configuré avec un numéro de téléphone valide (format: +223XXXXXXXX ou 223XXXXXXXX)");
        }
        
        // Construire l'URL de l'API Orange
        // Format: https://api.orange.com/smsmessaging/v1/outbound/{senderAddress}/requests
        // senderAddress dans l'URL doit être au format tel:+22376396922 ENCODÉ
        // tel: devient tel%3A et + devient %2B
        String senderAddressForUrl = "tel:" + senderAddressForBody; // tel:+22376396922
        String encodedSenderAddress = URLEncoder.encode(senderAddressForUrl, StandardCharsets.UTF_8); // tel%3A%2B22376396922
        String apiUrl = orangeApiUrl + "/" + encodedSenderAddress + "/requests";
        
        // Construire le JSON de la requête
        // IMPORTANT: Le senderAddress dans le body doit être tel:+22376396922 (même format que dans l'URL avant encodage)
        // L'API Orange compare le senderAddress décodé de l'URL avec celui du body, ils doivent être identiques
        // Le senderName est optionnel et permet d'afficher un nom personnalisé au lieu du numéro
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"outboundSMSMessageRequest\": {");
        jsonBuilder.append("\"address\": \"tel:").append(formattedPhone).append("\",");
        jsonBuilder.append("\"senderAddress\": \"tel:").append(senderAddressForBody).append("\"");
        
        // Ajouter le senderName si configuré (max 11 caractères alphanumériques, sans espaces)
        if (orangeSenderName != null && !orangeSenderName.trim().isEmpty()) {
            // Nettoyer le senderName : enlever les espaces et caractères non alphanumériques, limiter à 11 caractères
            String cleanSenderName = orangeSenderName.trim()
                    .replaceAll("\\s+", "") // Enlever les espaces
                    .replaceAll("[^a-zA-Z0-9]", ""); // Enlever les caractères non alphanumériques
            
            // Limiter à 11 caractères et vérifier qu'il n'est pas vide
            if (!cleanSenderName.isEmpty()) {
                cleanSenderName = cleanSenderName.substring(0, Math.min(11, cleanSenderName.length()));
                jsonBuilder.append(",\"senderName\": \"").append(cleanSenderName).append("\"");
            }
        }
        
        jsonBuilder.append(",\"outboundSMSTextMessage\": {");
        jsonBuilder.append("\"message\": \"").append(message.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")).append("\"");
        jsonBuilder.append("}");
        jsonBuilder.append("}}");
        
        String jsonBody = jsonBuilder.toString();
        
        // Log pour déboguer
        System.out.println("DEBUG Orange SMS - senderAddress (Body): tel:" + senderAddressForBody);
        System.out.println("DEBUG Orange SMS - senderAddress (URL, avant encodage): " + senderAddressForUrl);
        System.out.println("DEBUG Orange SMS - senderAddress (URL, après encodage): " + encodedSenderAddress);
        System.out.println("DEBUG Orange SMS - URL complète: " + apiUrl);
        System.out.println("DEBUG Orange SMS - JSON Body: " + jsonBody);
        
        // Envoyer la requête
        HttpURLConnection connection = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        
        // Écrire le body
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(jsonBody);
            writer.flush();
        }
        
        int responseCode = connection.getResponseCode();
        
        if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("✅ SMS Orange envoyé avec succès à : " + telephone);
        } else {
            // Lire le message d'erreur
            String errorMessage = "Erreur HTTP " + responseCode;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                errorMessage += " - " + response.toString();
            }
            throw new RuntimeException("Erreur lors de l'envoi du SMS Orange: " + errorMessage);
        }
    }
    
    /**
     * Obtient le bearer token Orange via OAuth2 (avec cache)
     */
    private String getOrangeBearerToken() throws Exception {
        // Si un token est défini manuellement, l'utiliser
        if (orangeBearerToken != null && !orangeBearerToken.isEmpty()) {
            return orangeBearerToken;
        }
        
        // Vérifier si le token en cache est encore valide (on suppose 1 heure de validité)
        if (cachedBearerToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return cachedBearerToken;
        }
        
        // Obtenir un nouveau token
        String credentials = orangeClientId + ":" + orangeClientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        
        HttpURLConnection connection = (HttpURLConnection) URI.create(orangeOauthUrl).toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        
        // Écrire le body
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write("grant_type=client_credentials");
            writer.flush();
        }
        
        int responseCode = connection.getResponseCode();
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                JsonNode jsonNode = objectMapper.readTree(response.toString());
                String accessToken = jsonNode.get("access_token").asText();
                int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 3600;
                
                // Mettre en cache le token (expire 5 minutes avant la date d'expiration réelle)
                cachedBearerToken = accessToken;
                tokenExpiryTime = System.currentTimeMillis() + ((expiresIn - 300) * 1000L);
                
                return accessToken;
            }
        } else {
            String errorMessage = "Erreur HTTP " + responseCode;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                errorMessage += " - " + response.toString();
            }
            throw new RuntimeException("Erreur lors de l'obtention du token Orange: " + errorMessage);
        }
    }
    
    /**
     * Formate un numéro de téléphone pour Orange (format international sans +)
     */
    private String formatPhoneForOrange(String telephone) {
        if (telephone == null) {
            return null;
        }
        // Orange attend le format international sans le +
        return telephone.startsWith("+") ? telephone.substring(1) : telephone;
    }
    
    /**
     * Envoie un SMS via une API générique (fallback)
     */
    private void sendSmsViaApi(String telephone, String message) throws Exception {
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

