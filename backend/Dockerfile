# 1. Traemos un entorno ligero de Python
FROM python:3.11-slim

# 2. Creamos la carpeta de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiamos el archivo de dependencias (¡Aquí sí usamos requirements.txt!)
COPY requirements.txt .

# 4. Instalamos las librerías de Python sin guardar caché para ahorrar espacio
RUN pip install --no-cache-dir -r requirements.txt

# 5. Copiamos el resto del código
COPY . .

# 6. Comando por defecto (aunque lo sobrescribiremos en desarrollo para el auto-recargado)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]