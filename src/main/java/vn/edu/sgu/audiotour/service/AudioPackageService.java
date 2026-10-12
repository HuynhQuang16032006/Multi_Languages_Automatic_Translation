package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.repository.NarrationRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class AudioPackageService {

    private final NarrationRepository narrationRepository;

    /**
     * Tạo file ZIP chứa textContent (content.txt) và file âm thanh MP3 thật từ ổ cứng.
     *
     * @param locationId   ID của địa điểm
     * @param languageCode Mã ngôn ngữ (ví dụ: "vi", "en")
     * @return Mảng byte của file ZIP
     */
    public byte[] createOfflinePackage(Long locationId, String languageCode) {
        // 1. Truy vấn bài thuyết minh từ Database
        Narration narration = narrationRepository.findByLocationIdAndLanguageCode(locationId, languageCode)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy bản thuyết minh cho địa điểm ID: " + locationId + " với ngôn ngữ: '" + languageCode + "'"));

        // 2. Kiểm tra tên file âm thanh
        String audioUrl = narration.getAudioUrl();
        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            throw new RuntimeException("Bài thuyết minh chưa được cấu hình tên file âm thanh (audioUrl rỗng)!");
        }

        // 3. Kiểm tra file âm thanh vật lý trên ổ cứng (uploads/audio/<audioUrl>)
        Path audioPath = Paths.get("uploads", "audio", audioUrl);
        if (!Files.exists(audioPath) || !Files.isRegularFile(audioPath)) {
            throw new RuntimeException("File âm thanh không tồn tại tại đường dẫn: " + audioPath.toAbsolutePath());
        }

        // 4. Đóng gói ZIP
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

            // Đóng gói file âm thanh thật từ ổ cứng
            String audioEntryName = audioPath.getFileName().toString();
            ZipEntry audioEntry = new ZipEntry(audioEntryName);
            zos.putNextEntry(audioEntry);
            Files.copy(audioPath, zos);
            zos.closeEntry();

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi I/O khi đóng gói file ZIP: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi tạo gói dữ liệu offline: " + e.getMessage(), e);
        }
    }
}