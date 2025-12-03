#!/bin/bash
# Script de Ejemplo para Probar la Subida de Imágenes en Vehículos

# CONFIGURACIÓN
BASE_URL="http://localhost:8080"
TOKEN="YOUR_JWT_TOKEN_HERE"  # Reemplazar con tu token JWT de ARRENDADOR

echo "=== PRUEBA DE SUBIDA DE IMÁGENES - MÓDULO LISTINGS ==="
echo ""

# 1. CREAR UN VEHÍCULO CON IMAGEN
echo "1. Creando vehículo con imagen..."
echo "   Endpoint: POST ${BASE_URL}/api/v1/vehicles"
echo ""

# Ejemplo con cURL (descomenta para ejecutar)
# curl -X POST "${BASE_URL}/api/v1/vehicles" \
#   -H "Authorization: Bearer ${TOKEN}" \
#   -F "resource={\"brand\":\"Toyota\",\"model\":\"Corolla\",\"year\":2023,\"pricePerDay\":50.0};type=application/json" \
#   -F "image=@./test-image.jpg"

echo "   Comando cURL:"
echo "   curl -X POST \"${BASE_URL}/api/v1/vehicles\" \\"
echo "     -H \"Authorization: Bearer \${TOKEN}\" \\"
echo "     -F \"resource={\\\"brand\\\":\\\"Toyota\\\",\\\"model\\\":\\\"Corolla\\\",\\\"year\\\":2023,\\\"pricePerDay\\\":50.0};type=application/json\" \\"
echo "     -F \"image=@./ruta/a/imagen.jpg\""
echo ""

# 2. OBTENER TODOS LOS VEHÍCULOS
echo "2. Obteniendo lista de vehículos..."
echo "   Endpoint: GET ${BASE_URL}/api/v1/vehicles"
echo ""

# curl -X GET "${BASE_URL}/api/v1/vehicles"

echo "   Comando cURL:"
echo "   curl -X GET \"${BASE_URL}/api/v1/vehicles\""
echo ""

# 3. OBTENER IMAGEN DE UN VEHÍCULO
echo "3. Obteniendo imagen del vehículo con ID 1..."
echo "   Endpoint: GET ${BASE_URL}/api/v1/vehicles/1/image"
echo ""

echo "   Puedes acceder directamente desde el navegador:"
echo "   ${BASE_URL}/api/v1/vehicles/1/image"
echo ""

echo "   O descargarla con cURL:"
echo "   curl -X GET \"${BASE_URL}/api/v1/vehicles/1/image\" -o vehicle-image.jpg"
echo ""

# 4. ACTUALIZAR UN VEHÍCULO CON NUEVA IMAGEN
echo "4. Actualizando vehículo con nueva imagen..."
echo "   Endpoint: PUT ${BASE_URL}/api/v1/vehicles/1"
echo ""

echo "   Comando cURL:"
echo "   curl -X PUT \"${BASE_URL}/api/v1/vehicles/1\" \\"
echo "     -H \"Authorization: Bearer \${TOKEN}\" \\"
echo "     -F \"resource={\\\"brand\\\":\\\"Toyota\\\",\\\"model\\\":\\\"Camry\\\",\\\"year\\\":2024,\\\"pricePerDay\\\":60.0};type=application/json\" \\"
echo "     -F \"image=@./ruta/a/nueva-imagen.jpg\""
echo ""

echo "=== NOTAS IMPORTANTES ==="
echo "1. Reemplaza YOUR_JWT_TOKEN_HERE con tu token JWT real"
echo "2. Debes tener el rol ROLE_ARRENDADOR para crear/actualizar vehículos"
echo "3. Asegúrate de que la aplicación esté corriendo en el puerto 8080"
echo "4. Las imágenes se guardan en formato BLOB en MySQL"
echo "5. El tamaño máximo de archivo es 10MB (configurable en application.properties)"
echo ""

