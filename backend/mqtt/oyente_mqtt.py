import paho.mqtt.client as mqtt
import time

MQTT_BROKER = "mqtt_broker" 
MQTT_PORT = 1883
MQTT_TOPIC = "nethome/energia/#"

# En la versión 2.0+, la función on_connect recibe 'reason_code' y 'properties'
def on_connect(client, userdata, flags, reason_code, properties):
    if reason_code == 0:
        print("✅ [Worker] Conectado exitosamente a Mosquitto")
        client.subscribe(MQTT_TOPIC)
        print(f"📡 Suscrito al tópico: {MQTT_TOPIC}")
    else:
        print(f"❌ [Worker] Error de conexión, código: {reason_code}")

def on_message(client, userdata, msg):
    print(f"📥 [Worker] Mensaje recibido en {msg.topic}: {msg.payload.decode()}")

def iniciar_worker():
    print("🚀 Iniciando Worker MQTT en segundo plano...")
    
    # ⚠️ AQUÍ ESTÁ LA MAGIA QUE ARREGLA EL ERROR
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, "nethome_backend_worker")
    
    client.on_connect = on_connect
    client.on_message = on_message

    while True:
        try:
            client.connect(MQTT_BROKER, MQTT_PORT, 60)
            break
        except Exception as e:
            print(f"⏳ Esperando al broker MQTT... ({e})")
            time.sleep(3)

    client.loop_forever()

if __name__ == "__main__":
    iniciar_worker()