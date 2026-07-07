const express = require('express');
const mqtt = require('mqtt');
const { Pool } = require('pg');
const env = require('./config/env');

const app = express();
app.use(express.json());

const pool = new Pool({
  host: env.DB_HOST,
  port: env.DB_PORT,
  user: env.DB_USER,
  password: env.DB_PASSWORD,
  database: env.DB_NAME
})

pool.query(`
  CREATE TABLE IF NOT EXISTS consumo_energia (
    id SERIAL PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    mensaje TEXT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  )
`).then(() => {
  console.log('🗄️ Tabla "consumo_energia" lista en PostgreSQL');
}).catch(err => {
  console.error('❌ Error configurando la base de datos:', err);
});

// 1. Iniciar servidor Express
app.listen(env.PORT, () => {
  console.log(`✅ Servidor API corriendo en el puerto ${env.PORT}`);
});

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', message: 'NetHome API funcionando' });
});

// 2. Conectar al Broker MQTT
const mqttClient = mqtt.connect(`mqtt://${env.MQTT_HOST}:${env.MQTT_PORT}`);

mqttClient.on('connect', () => {
  console.log('🔌 Conectado al Broker MQTT (Mosquitto)');
  mqttClient.subscribe('nethome/energia/#', (err) => {
    if (!err) {
      console.log('📡 Suscrito al tópico nethome/energia/#');
    }
  });
});

// 3. Escuchar los mensajes que lleguen por MQTT
mqttClient.on('message', (topic, message) => {
  console.log(`📩 Mensaje recibido en [${topic}]: ${message.toString()}`);
  
  try {
    // Insertamos el topic y el mensaje en la base de datos
    await pool.query(
      'INSERT INTO consumo_energia (topic, mensaje) VALUES ($1, $2)',
      [topic, mensajeTexto]
    );
    console.log('💾 Dato guardado en PostgreSQL correctamente');
  } catch (error) {
    console.error('❌ Error guardando en la base de datos:', error);
  }
});