package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.repository.NarrationRepository;

import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class AudioPackageService {

    private final NarrationRepository narrationRepository;

    // Hàm tạo file ZIP chứa Audio và Text dựa trên ID địa điểm và Ngôn ngữ
    public byte[] createOfflinePackage(Long locationId, String languageCode) {
        Narration narration = narrationRepository.findByLocationIdAndLanguageCode(locationId, languageCode)
            .orElseThrow(() -> new RuntimeException("Chưa có bản thuyết minh cho ngôn ngữ này!"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Đóng gói nội dung văn bản (Text)
            ZipEntry textEntry = new ZipEntry("content.txt");
            zos.putNextEntry(textEntry);
            zos.write(narration.getTextContent().getBytes());
            zos.closeEntry();

            // Logic thêm file Audio (MP3) vào ZIP sẽ được viết ở đây khi có hệ thống lưu trữ file (ví dụ Amazon S3 hoặc thư mục cục bộ)
            // ZipEntry audioEntry = new ZipEntry("audio.mp3");
            // ...

            zos.finish();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đóng gói dữ liệu offline: " + e.getMessage());
        }
    }
}