package com.example.fuelticket.service;

import com.example.fuelticket.entity.User;
import com.example.fuelticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 15;
    private static final String CODE_CHARACTERS = "0123456789";
    
    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARACTERS.charAt(random.nextInt(CODE_CHARACTERS.length())));
        }
        
        return code.toString();
    }
    
    public void sendVerificationCode(User user) {
        String verificationCode = generateVerificationCode();
        LocalDateTime now = LocalDateTime.now();
        
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(now.plusMinutes(CODE_EXPIRY_MINUTES));
        user.setVerificationCodeSentAt(now);
        
        userRepository.save(user);
        
        // Envoyer l'email
        emailService.sendVerificationEmail(user.getEmail(), user.getNom(), verificationCode);
        
        // Si c'est un citoyen, envoyer aussi un SMS de vérification
        if (user.getRole() == User.Role.CITOYEN && user.getTelephone() != null && !user.getTelephone().trim().isEmpty()) {
            sendTelephoneVerificationCode(user);
        }
    }
    
    /**
     * Envoie un code de vérification par SMS pour le téléphone
     */
    public void sendTelephoneVerificationCode(User user) {
        String verificationCode = generateVerificationCode();
        LocalDateTime now = LocalDateTime.now();
        
        user.setTelephoneVerificationCode(verificationCode);
        user.setTelephoneVerificationCodeExpiry(now.plusMinutes(CODE_EXPIRY_MINUTES));
        user.setTelephoneVerificationCodeSentAt(now);
        
        userRepository.save(user);
        
        // Envoyer le SMS
        smsService.sendVerificationSms(user.getTelephone(), user.getNom(), verificationCode);
    }
    
    public boolean verifyCode(String email, String code) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Vérifier si le code correspond et n'est pas expiré
        if (user.getVerificationCode() == null || 
            !user.getVerificationCode().equals(code) ||
            user.getVerificationCodeExpiry() == null ||
            LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
            return false;
        }
        
        // Marquer l'email comme vérifié
        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        user.setVerificationCodeSentAt(null);
        
        userRepository.save(user);
        
        // Envoyer l'email de bienvenue
        emailService.sendWelcomeEmail(user.getEmail(), user.getNom(), user.getRole().toString());
        
        return true;
    }
    
    public boolean isCodeExpired(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return true;
        }
        
        User user = userOpt.get();
        
        return user.getVerificationCodeExpiry() == null || 
               LocalDateTime.now().isAfter(user.getVerificationCodeExpiry());
    }
    
    public void resendVerificationCode(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        
        User user = userOpt.get();
        
        if (user.getEmailVerified()) {
            throw new RuntimeException("L'email est déjà vérifié");
        }
        
        sendVerificationCode(user);
    }
    
    public void sendPasswordResetCode(User user) {
        String resetCode = generateVerificationCode();
        LocalDateTime now = LocalDateTime.now();
        
        user.setVerificationCode(resetCode);
        user.setVerificationCodeExpiry(now.plusMinutes(CODE_EXPIRY_MINUTES));
        user.setVerificationCodeSentAt(now);
        
        userRepository.save(user);
        
        // Envoyer l'email de réinitialisation
        emailService.sendPasswordResetEmail(user.getEmail(), user.getNom(), resetCode);
    }
    
    /**
     * Vérifie le code de vérification du téléphone
     */
    public boolean verifyTelephoneCode(String telephone, String code) {
        Optional<User> userOpt = userRepository.findByTelephone(telephone);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Vérifier si le code correspond et n'est pas expiré
        if (user.getTelephoneVerificationCode() == null || 
            !user.getTelephoneVerificationCode().equals(code) ||
            user.getTelephoneVerificationCodeExpiry() == null ||
            LocalDateTime.now().isAfter(user.getTelephoneVerificationCodeExpiry())) {
            return false;
        }
        
        // Marquer le téléphone comme vérifié
        user.setTelephoneVerified(true);
        user.setTelephoneVerificationCode(null);
        user.setTelephoneVerificationCodeExpiry(null);
        user.setTelephoneVerificationCodeSentAt(null);
        
        userRepository.save(user);
        
        return true;
    }
    
    /**
     * Renvoie le code de vérification du téléphone
     */
    public void resendTelephoneVerificationCode(String telephone) {
        Optional<User> userOpt = userRepository.findByTelephone(telephone);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé avec ce numéro de téléphone");
        }
        
        User user = userOpt.get();
        
        if (user.getTelephoneVerified()) {
            throw new RuntimeException("Le téléphone est déjà vérifié");
        }
        
        sendTelephoneVerificationCode(user);
    }
}
