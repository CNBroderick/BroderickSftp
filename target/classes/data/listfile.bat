@echo off & setlocal EnableDelayedExpansion
for /f "delims=" %%i in ('"dir /a/s/b/on *.*"') do (
set file=%%~fi
set ��Ҫ����file=!file:%cd%/=!
set file=!file:/=/!
echo !file! >> filepath.txt
)