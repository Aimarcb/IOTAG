# Usa una imagen oficial de Python ligera
FROM python:3.11-slim

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Instala dependencias del sistema necesarias para PostgreSQL (Psycopg2)
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Copia e instala los requerimientos de librerías
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia el resto de las carpetas (api, mqtt, base_datos, ia...)
COPY . .

ENV PYTHONUNBUFFERED=1

# Por defecto, arranca tu API de FastAPI
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]