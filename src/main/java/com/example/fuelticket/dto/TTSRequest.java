package com.example.fuelticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TTSRequest {
    private String text;
    private String languageCode = "fr-FR"; // Default to French, can be "bm" for Bambara if supported
    private String voiceName = "fr-FR-Wavenet-C";
    private AudioConfig audioConfig = new AudioConfig();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioConfig {
        private String audioEncoding = "MP3";
        private Double speakingRate = 0.9;
        private Double pitch = 0.0;
        private Double volumeGainDb = 0.0;
    }
}

