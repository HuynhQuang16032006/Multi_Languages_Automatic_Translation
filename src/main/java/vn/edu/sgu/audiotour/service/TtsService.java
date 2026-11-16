package vn.edu.sgu.audiotour.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import vn.edu.sgu.audiotour.config.TtsProperties;
import vn.edu.sgu.audiotour.dto.NarrationResponse;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.exception.ServiceUnavailableException;
import vn.edu.sgu.audiotour.repository.NarrationRepository;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Sinh file MP3 cho một bài thuyết minh bằng ElevenLabs TTS, ghi vào uploads/audio
 * và lưu tên file vào narration.audioUrl. File đã có thì dùng lại, không gọi API lại.
 */
@Slf4j
@Service
public class TtsService {

    // Ước lượng thời lượng: ~15 ký tự/giây cho giọng đọc thuyết minh.
    // shortcut: ước lượng theo độ dài văn bản, sai số ±15%; đổi sang đọc metadata MP3 nếu cần chính xác.
    private static final double CHARS_PER_SECOND = 15.0;

    private final TtsProperties properties;
    private final NarrationRepository narrationRepository;
    private final RestClient restClient;
    private final Path audioDir = Paths.get("uploads", "audio");

    public TtsService(TtsProperties properties, NarrationRepository narrationRepository,
                      RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.narrationRepository = narrationRepository;
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    }

    @Transactional
    public NarrationResponse generateAudio(Long narrationId, boolean force) {
        Narration narration = narrationRepository.findById(narrationId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài thuyết minh id " + narrationId));

        String existingFileName = narration.getAudioUrl();
        Path existing = existingFileName == null || existingFileName.isBlank() ? null
                : audioDir.resolve(Paths.get(existingFileName).getFileName());

        // Có file rồi thì coi là cache: không gọi API, không tốn credit
        if (!force && existing != null && Files.isRegularFile(existing)) {
            return toResponse(narration, existingFileName);
        }

        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new ServiceUnavailableException(
                    "Chưa cấu hình ELEVENLABS_API_KEY, không sinh được audio cho bài thuyết minh id " + narrationId);
        }

        String text = narration.getTextContent();
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Bài thuyết minh id " + narrationId + " chưa có nội dung văn bản để đọc");
        }

        String languageCode = narration.getLanguage().getCode();
        String voiceId = properties.voiceId();
        if (voiceId == null || voiceId.isBlank()) {
            throw new IllegalArgumentException("Chưa cấu hình voice TTS (app.tts.voice-id / TTS_VOICE)"
                    + " cho ngôn ngữ '" + languageCode + "'");
        }

        byte[] mp3 = synthesize(text, voiceId);
        String fileName = "narration-" + narrationId + "-" + UUID.randomUUID() + ".mp3";
        Path target = audioDir.resolve(fileName);
        writeAtomically(target, mp3);

        narration.setAudioUrl(fileName);
        narration.setDurationInSeconds(Math.max(1, (int) Math.round(text.length() / CHARS_PER_SECOND)));
        narrationRepository.save(narration);
        log.info("Đã sinh audio TTS cho narration {} -> {} ({} byte)", narrationId, fileName, mp3.length);

        return toResponse(narration, fileName);
    }

    private byte[] synthesize(String text, String voiceId) {
        try {
            byte[] audio = restClient.post()
                    .uri(builder -> builder.path("/v1/text-to-speech/{voiceId}")
                            .queryParam("output_format", "mp3_44100_128")
                            .build(voiceId))
                    .header("xi-api-key", properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", text, "model_id", properties.modelId()))
                    .retrieve()
                    .body(byte[].class);

            if (audio == null || audio.length == 0) {
                throw new ServiceUnavailableException("ElevenLabs trả về audio rỗng");
            }
            return audio;
        } catch (RestClientResponseException ex) {
            throw new ServiceUnavailableException("ElevenLabs trả lỗi " + ex.getStatusCode().value() + ": "
                    + abbreviate(ex.getResponseBodyAsString()));
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Không gọi được ElevenLabs: " + ex.getMessage());
        }
    }

    // Ghi ra file tạm rồi move: không bao giờ để lại file MP3 dở dang cho endpoint stream/ZIP đọc
    private void writeAtomically(Path target, byte[] bytes) {
        Path tmp = null;
        try {
            Files.createDirectories(audioDir);
            tmp = Files.createTempFile(audioDir, "tts-", ".part");
            Files.write(tmp, bytes);
            try {
                Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target);
            }
        } catch (IOException e) {
            throw new ServiceUnavailableException("Không ghi được file âm thanh " + target + ": " + e.getMessage());
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException e) {
                    log.warn("Không xóa được file TTS tạm {}", tmp, e);
                }
            }
        }
    }


    private NarrationResponse toResponse(Narration narration, String fileName) {
        return NarrationResponse.builder()
                .locationId(narration.getLocation().getId())
                .locationName(narration.getLocation().getName())
                .languageCode(narration.getLanguage().getCode())
                .textContent(narration.getTextContent())
                .audioUrl(fileName)
                .durationInSeconds(narration.getDurationInSeconds())
                .build();
    }

    private String abbreviate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 200 ? body : body.substring(0, 200) + "...";
    }
}
