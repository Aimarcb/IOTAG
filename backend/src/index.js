const express = require('express');
const mqtt = require('mqtt');
const env = require('./config/env');

const app = express();
app.use(express.json());

// 1. Iniciar servidor Express
app.listen(env.PORT, () => {
  console.log(`✅ Servidor API corriendo en el puerto ${env.PORT}`);
});

// Ruta de prueba
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', message: 'NetHome API funcionando' });
});

// 2. Conectar al Broker MQTT
const mqttClient = mqtt.connect(`mqtt://${env.MQTT_HOST}:${env.MQTT_PORT}`);

mqttClient.on('connect', () => {
  console.log('🔌 Conectado al Broker MQTT (Mosquitto)');
  
  // Suscribirse a un tópico de prueba (donde el hardware publicará datos)
  mqttClient.subscribe('nethome/energia/#', (err) => {
    if (!err) {
      console.log('📡 Suscrito al tópico nethome/energia/#');
    }
  });
});

// 3. Escuchar los mensajes que lleguen por MQTT
mqttClient.on('message', (topic, message) => {
  console.log(`📩 Mensaje recibido en [${topic}]: ${message.toString()}`);
  
  // TODO en el siguiente paso: Guardar esto en PostgreSQL
});