package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.dto.NarrationResponse;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.repository.PackageDownloadRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Quản lý bài thuyết minh (Narration): nội dung văn bản, luồng phát trực tiếp âm thanh
 * và đóng gói ZIP ngoại tuyến (Offline Package).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NarrationService {

    private static final Path AUDIO_DIR = Paths.get("uploads", "audio");

    private final NarrationRepository narrationRepository;
    private final PackageDownloadRepository packageDownloadRepository;

    // Lấy bài thuyết minh của 1 địa điểm theo mã ngôn ngữ
    public NarrationResponse getNarration(Long locationId, String languageCode) {
        Narration narration = findNarration(locationId, languageCode);
        return NarrationResponse.builder()
                .locationId(narration.getLocation().getId())
                .locationName(narration.getLocation().getName())
                .languageCode(narration.getLanguage().getCode())
                .textContent(narration.getTextContent())
                .audioUrl(narration.getAudioUrl())
                .durationInSeconds(narration.getDurationInSeconds())
                .build();
    }

    // Lấy resource file âm thanh MP3 để phát trực tiếp (Streaming)
    public Resource getAudioResource(Long locationId, String languageCode) {
        Narration narration = findNarration(locationId, languageCode);
        Path audioPath = resolveAudioPath(narration);
        return new FileSystemResource(audioPath);
    }

    // Tạo file ZIP chứa textContent (content.txt) và file âm thanh MP3 thật phục vụ nghe Offline
    @Transactional
    public byte[] createOfflinePackage(Long locationId, String languageCode, String deviceId) {
        Narration narration = findNarration(locationId, languageCode);
        packageDownloadRepository.save(new vn.edu.sgu.audiotour.entity.PackageDownload(
                narration.getLocation(), languageCode, deviceId));
        Path audioPath = resolveAudioPath(narration);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Đóng gói nội dung văn bản thành content.txt
            ZipEntry textEntry = new ZipEntry("content.txt");
            zos.putNextEntry(textEntry);
            byte[] textBytes = narration.getTextContent() != null
                    ? narration.getTextContent().getBytes(StandardCharsets.UTF_8)
                    : new byte[0];
            zos.write(textBytes);
            zos.closeEntry();

            // Đóng gói file âm thanh thật từ uploads/audio
            String audioEntryName = audioPath.getFileName().toString();
            ZipEntry audioEntry = new ZipEntry(audioEntryName);
            zos.putNextEntry(audioEntry);
            Files.copy(audioPath, zos);
            zos.closeEntry();

            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi I/O khi đóng gói file ZIP: " + e.getMessage(), e);
        }
    }

    public byte[] createOfflinePackage(Long locationId, String languageCode) {
        return createOfflinePackage(locationId, languageCode, null);
    }

    private Narration findNarration(Long locationId, String languageCode) {
        return narrationRepository.findByLocationIdAndLanguageCode(locationId, languageCode)
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy bài thuyết minh cho địa điểm " + locationId + " và ngôn ngữ " + languageCode));
    }

    private Path resolveAudioPath(Narration narration) {
        String audioUrl = narration.getAudioUrl();
        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            throw new RuntimeException("Bài thuyết minh chưa được cấu hình tên file âm thanh (audioUrl rỗng)!");
        }

        Path audioPath = AUDIO_DIR.resolve(audioUrl);
        if (!Files.exists(audioPath) || !Files.isRegularFile(audioPath)) {
            throw new RuntimeException("File âm thanh không tồn tại tại: " + audioPath.toAbsolutePath());
        }
        return audioPath;
    }
}
