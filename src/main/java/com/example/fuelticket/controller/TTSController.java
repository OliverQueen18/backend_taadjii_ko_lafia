package com.example.fuelticket.controller;

import com.example.fuelticket.dto.TTSRequest;
import com.example.fuelticket.service.TTSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
@Tag(name = "Text-to-Speech", description = "Text-to-Speech API for audio synthesis")
@Slf4j
public class TTSController {

    private final TTSService ttsService;

    @PostMapping("/synthesize")
    @Operation(summary = "Synthesize speech", description = "Convert text to speech audio using Google Cloud TTS")
    public ResponseEntity<byte[]> synthesizeSpeech(@RequestBody TTSRequest request) {
        try {
            Optional<byte[]> audioBytes = ttsService.synthesizeSpeech(
                    request.getText(),
                    request.getLanguageCode(),
                    request.getVoiceName(),
                    request.getAudioConfig().getSpeakingRate(),
                    request.getAudioConfig().getPitch(),
                    request.getAudioConfig().getVolumeGainDb()
            );

            if (audioBytes.isPresent()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
                headers.setContentLength(audioBytes.get().length);
                headers.set("Content-Disposition", "inline; filename=\"speech.mp3\"");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(audioBytes.get());
            } else {
                log.warn("TTS synthesis failed or is disabled");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error in TTS synthesis endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @PostMapping("/synthesize/bamanankan")
    @Operation(summary = "Synthesize Bamanankan speech", description = "Convert Bamanankan text to speech audio")
    public ResponseEntity<byte[]> synthesizeBamanankan(@RequestBody String text) {
        try {
            Optional<byte[]> audioBytes = ttsService.synthesizeBamanankan(text);

            if (audioBytes.isPresent()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
                headers.setContentLength(audioBytes.get().length);
                headers.set("Content-Disposition", "inline; filename=\"bamanankan-speech.mp3\"");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(audioBytes.get());
            } else {
                log.warn("Bamanankan TTS synthesis failed or is disabled");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error in Bamanankan TTS synthesis endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}

