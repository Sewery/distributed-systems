Write-Host "Generuję stuby Python z logistics.proto..."
& $pythonCmd -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. logistics.proto
Write-Host " Uruchamiam klienta CLI..."
& $pythonCmd .\client.py
