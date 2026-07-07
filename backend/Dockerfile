# 1. Traemos un entorno de Node.js ya compilado y optimizado
FROM node:20-alpine

# 2. Creamos la carpeta de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiamos los archivos de dependencias
COPY package*.json ./

# 4. Instalamos lo que dice package.json, pero sin las dependencias de desarrollo
RUN npm ci --omit=dev

# 5. Copiamos el resto del código
COPY . .

# 6. Comando para arrancar la aplicación
CMD ["node", "src/index.js"]