package com.example.fuelticket.service;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class TTSService {

    @Value("${google.cloud.tts.enabled:false}")
    private boolean ttsEnabled;

    @Value("${google.cloud.tts.project-id:}")
    private String projectId;

    /**
     * Synthesize speech using Google Cloud Text-to-Speech
     * 
     * @param text The text to synthesize
     * @param languageCode Language code (e.g., "fr-FR", "en-US")
     * @param voiceName Voice name (e.g., "fr-FR-Wavenet-C")
     * @param speakingRate Speaking rate (0.25 to 4.0)
     * @param pitch Pitch (-20.0 to 20.0 semitones)
     * @param volumeGainDb Volume gain in dB
     * @return Audio content as byte array, or empty if TTS is disabled or fails
     */
    public Optional<byte[]> synthesizeSpeech(
            String text,
            String languageCode,
            String voiceName,
            Double speakingRate,
            Double pitch,
            Double volumeGainDb) {
        
        if (!ttsEnabled) {
            log.warn("Google Cloud TTS is disabled. Enable it by setting google.cloud.tts.enabled=true");
            return Optional.empty();
        }

        if (text == null || text.trim().isEmpty()) {
            log.warn("Text to synthesize is empty");
            return Optional.empty();
        }

        try {
            // Initialize the client
            TextToSpeechClient client = TextToSpeechClient.create();

            // Set the text input
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            // Build the voice request
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setName(voiceName)
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();

            // Select the audio type
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .setSpeakingRate(speakingRate != null ? speakingRate : 0.9)
                    .setPitch(pitch != null ? pitch : 0.0)
                    .setVolumeGainDb(volumeGainDb != null ? volumeGainDb : 0.0)
                    .build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio content
            ByteString audioContents = response.getAudioContent();

            // Convert to byte array
            byte[] audioBytes = audioContents.toByteArray();

            client.close();

            log.info("Successfully synthesized speech for text: {}", text.substring(0, Math.min(50, text.length())));
            return Optional.of(audioBytes);

        } catch (IOException e) {
            log.error("Error synthesizing speech with Google Cloud TTS", e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error during TTS synthesis", e);
            return Optional.empty();
        }
    }

    /**
     * Synthesize speech for Bamanankan text
     * Uses French voice as fallback since Bamanankan is not directly supported
     */
    public Optional<byte[]> synthesizeBamanankan(String text) {
        // Try to use a French voice that might handle Bamanankan better
        // Note: This is a workaround - for true Bamanankan support, you'd need
        // custom voice models or specialized TTS services
        return synthesizeSpeech(
                text,
                "fr-FR", // Using French as language code
                "fr-FR-Wavenet-C", // French voice
                0.85, // Slightly slower for better comprehension
                0.0,
                0.0
        );
    }
}

