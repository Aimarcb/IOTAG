import os
import time

import paho.mqtt.client as mqtt

from database.database import init_db, save_mqtt_message

MQTT_BROKER = os.getenv("MQTT_BROKER_HOST", "mqtt_broker")
MQTT_PORT = int(os.getenv("MQTT_BROKER_PORT", "1883"))
MQTT_TOPIC = os.getenv("MQTT_TOPIC", "nethome/energia/#")


def on_connect(client, userdata, flags, reason_code, properties):
    if reason_code == 0:
        print("✅ [Worker] Conectado exitosamente a Mosquitto")
        client.subscribe(MQTT_TOPIC)
        print(f"📡 Suscrito al tópico: {MQTT_TOPIC}")
    else:
        print(f"❌ [Worker] Error de conexión, código: {reason_code}")


def on_message(client, userdata, msg):
    try:
        payload_text = msg.payload.decode("utf-8", errors="ignore")
        save_mqtt_message(topic=msg.topic, payload=payload_text)
        print(f"📥 [Worker] Mensaje recibido en {msg.topic}: {payload_text}")
    except Exception as exc:
        print(f"❌ [Worker] No se pudo guardar el mensaje: {exc}")


def iniciar_worker():
    print("🚀 Iniciando Worker MQTT en segundo plano...")

    while not init_db():
        print("⏳ Esperando a que la base de datos esté disponible...")
        time.sleep(3)

    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, "nethome_backend_worker")
    client.on_connect = on_connect
    client.on_message = on_message

    while True:
        try:
            client.connect(MQTT_BROKER, MQTT_PORT, 60)
            break
        except Exception as exc:
            print(f"⏳ Esperando al broker MQTT... ({exc})")
            time.sleep(3)

    client.loop_forever()


if __name__ == "__main__":
    iniciar_worker()