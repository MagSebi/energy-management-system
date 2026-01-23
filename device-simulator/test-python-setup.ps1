# Test script pentru verificarea configurării Python
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test configurare Python Interpreter" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Schimbă directorul la device-simulator
Set-Location "C:\Users\Sebi\OneDrive\Desktop\Facultate\An_4\SD\Tema1 (4)\Tema1\device-simulator"

Write-Host "1. Verificare Python global:" -ForegroundColor Yellow
python --version
Write-Host ""

Write-Host "2. Verificare Python în virtualenv:" -ForegroundColor Yellow
.\.venv\Scripts\python.exe --version
Write-Host ""

Write-Host "3. Verificare locație interpreter:" -ForegroundColor Yellow
.\.venv\Scripts\python.exe -c "import sys; print(f'Interpreter: {sys.executable}')"
Write-Host ""

Write-Host "4. Verificare pachete instalate:" -ForegroundColor Yellow
.\.venv\Scripts\python.exe -c "import aio_pika; print(f'aio-pika v{aio_pika.__version__} - OK')"
.\.venv\Scripts\python.exe -c "from dotenv import load_dotenv; print('python-dotenv - OK')"
Write-Host ""

Write-Host "5. Verificare sintaxă simulator.py:" -ForegroundColor Yellow
.\.venv\Scripts\python.exe -m py_compile simulator.py
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ simulator.py - fără erori de sintaxă" -ForegroundColor Green
} else {
    Write-Host "✗ simulator.py - erori de sintaxă găsite" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Configurare completă!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pentru a rula simulatorul:" -ForegroundColor Yellow
Write-Host "  .\.venv\Scripts\python.exe simulator.py" -ForegroundColor White
Write-Host ""
Write-Host "Sau activează virtualenv-ul:" -ForegroundColor Yellow
Write-Host "  .\.venv\Scripts\Activate.ps1" -ForegroundColor White
Write-Host "  python simulator.py" -ForegroundColor White

