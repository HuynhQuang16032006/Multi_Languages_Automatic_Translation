@echo off
chcp 65001 >nul
echo =======================================================
echo     MO CONG 8080 VA CAI DAT WI-FI CHO PHEP DI DONG KET NOI
echo =======================================================
echo.
netsh advfirewall firewall add rule name="Spring Boot Port 8080" dir=in action=allow protocol=TCP localport=8080
powershell -Command "Set-NetConnectionProfile -InterfaceAlias 'Wi-Fi' -NetworkCategory Private"
echo.
echo =======================================================
echo     HOAN TAT! DI DONG DA CO THE TRUY CAP PORT 8080
echo =======================================================
pause
