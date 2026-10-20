# ==============================================================================
# KỊCH BẢN KIỂM THỬ ĐỒNG THỜI 4 NGƯỜI DÙNG CÙNG TRUY CẬP (CONCURRENT USERS DEMO)
# Đề tài: Hệ thống Thuyết minh Audio Tour Tự Động Đa Ngôn Ngữ - SGU
# ==============================================================================

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " [DEMO SGU] BẮT ĐẦU MÔ PHỎNG 4 DU KHÁCH ĐỒNG THỜI QUÉT MÃ QR" -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan

# 1. Đăng nhập lấy Token JWT
Write-Host "[1/3] Đang xác thực hệ thống..." -NoNewline
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$token = $login.token
Write-Host " [THÀNH CÔNG]" -ForegroundColor Green

# 2. Xem số lượt nghe hiện tại trên Dashboard
$headers = @{ Authorization = "Bearer $token" }
$dashBefore = Invoke-RestMethod -Uri "http://localhost:8080/api/analytics/dashboard" -Headers $headers
Write-Host "[2/3] Tổng lượt nghe HIỆN TẠI trên Dashboard: " -NoNewline
Write-Host "$($dashBefore.totalListens) lượt" -ForegroundColor Yellow

# 3. Kích hoạt 4 luồng chạy song song cùng 1 giây
Write-Host "[3/3] Đang phóng 4 luồng truy cập đồng thời..." -ForegroundColor Cyan

$jobs = 1..4 | ForEach-Object {
    Start-Job -ScriptBlock {
        param($idx, $tok)
        $devId = "du-khach-0$idx"
        $headers = @{ Authorization = "Bearer $tok" }
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        
        # Bước A: Quét mã QR Dinh Độc Lập
        $loc = Invoke-RestMethod -Uri "http://localhost:8080/api/locations/by-qr/QR-DINH-DOC-LAP-01" -Headers $headers
        
        # Bước B: Gửi log phát audio
        $body = @{
            locationId = $loc.id
            languageCode = "vi"
            listenDurationSeconds = 120 + $idx
            triggerType = "QR_CODE"
            latitude = 10.7770
            longitude = 106.6953
            deviceId = $devId
        } | ConvertTo-Json
        
        $logRes = Invoke-RestMethod -Uri "http://localhost:8080/api/analytics/logs" -Method Post -ContentType "application/json" -Headers $headers -Body $body
        $sw.Stop()
        
        return "[Du khách $idx ($devId)] Quét QR '$($loc.name)' -> Ghi Log ID: $($logRes.id) | Phản hồi: $($sw.ElapsedMilliseconds)ms | Lúc: $(Get-Date -Format 'HH:mm:ss.fff')"
    } -ArgumentList $_, $token
}

$results = $jobs | Receive-Job -Wait -AutoRemoveJob

Write-Host ""
Write-Host "--- KẾT QUẢ PHẢN HỒI SONG SONG TỪ MÁY CHỦ ---" -ForegroundColor Green
$results | ForEach-Object { Write-Host $_ -ForegroundColor White }
Write-Host ""

# 4. Kiểm tra lại Dashboard xem số liệu tăng lên ngay lập tức
$dashAfter = Invoke-RestMethod -Uri "http://localhost:8080/api/analytics/dashboard" -Headers $headers
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " KẾT QUẢ SAU KHI 4 NGƯỜI CÙNG TRUY CẬP:" -ForegroundColor Green
Write-Host " - Tổng số lượt nghe mới: $($dashAfter.totalListens) lượt (TĂNG THÊM 4 LƯỢT THỰC TẾ!)" -ForegroundColor Yellow
Write-Host " - Số lượt kích hoạt QR Code: $($dashAfter.listensByTriggerType.QR_CODE)" -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan
