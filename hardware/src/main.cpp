#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include "configuration.h" // Aquí están WIFI_SSID, WIFI_PASS, MQTT_SERVER, MQTT_PORT

// --- PINES DE LOS SENSORES ---
#define PIN_LDR 34           // Pin analógico (ADC1) para la luz
#define PIN_MOV 23           // Pin digital para el
#define PIN_NTC 35           // Pin analógico (ADC1) para la temperatura
#define RES_FIJA 9830.0      // Resistencia fija de 10k ohmios para el divisor de tensión del NTC
#define COEFICIENTE_B 3950.0 // Coeficiente Beta del NTC

// --- OBJETOS DE RED ---
WiFiClient espClient;
PubSubClient client(espClient);

// Variables para no usar delay() y no bloquear el MQTT
unsigned long ultimoMuestreo = 0;
const long intervalo = 10000; // Enviar datos cada 10 segundos
int ultimoEstadoMov = -1;    // Empezamos en -1 para que obligatoriamente mande el primer dato al arrancar
unsigned long ultimoTiempoMovimiento = 0; // Guarda el milisegundo del último cambio real

float leerTemp();
int leerLuz();
int detectarMovimiento();

// Función para conectar al WiFi
void setup_wifi()
{
  delay(10);
  Serial.println();
  Serial.print("Conectando a WiFi: ");
  Serial.println(WIFI_SSID);

  WiFi.begin(WIFI_SSID, WIFI_PASSW);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi conectado. IP: ");
  Serial.println(WiFi.localIP());
}

// Función para reconectar al MQTT si se cae
void reconnect()
{
  while (!client.connected())
  {
    Serial.print("Intentando conexión MQTT a ");
    Serial.print(MQTT_SERVER);
    Serial.println("...");

    // Creamos un ID aleatorio para este ESP32
    String clientId = "ESP32Client-";
    clientId += String(random(0xffff), HEX);

    if (client.connect(clientId.c_str()))
    {
      Serial.println("¡Conectado al servidor MQTT");
    }
    else
    {
      Serial.print("Fallo, rc=");
      Serial.print(client.state());
      Serial.println(" reintentando en 5 segundos");
      delay(5000);
    }
  }
}

void setup()
{
  Serial.begin(115200);

  // Configuramos el pin del movimiento como entrada
  pinMode(PIN_MOV, INPUT);
  pinMode(PIN_LDR, INPUT);
  pinMode(PIN_NTC, INPUT);

  // Iniciamos WiFi y configuramos el servidor MQTT
  setup_wifi();
  client.setServer(MQTT_SERVER, MQTT_PORT);
}

void loop()
{
  // 1. Mantenemos la conexión MQTT viva
  if (!client.connected())
  {
    reconnect();
  }
  client.loop(); // Esto es vital para que PubSubClient procese los datos

  // 2. Leemos y enviamos datos cada 5 segundos (sin usar delay!)
  unsigned long tiempoActual = millis();
  int estadoMov = detectarMovimiento();

  if (estadoMov != ultimoEstadoMov && (millis() - ultimoTiempoMovimiento >= 2500))
  {
    ultimoTiempoMovimiento = millis();
    ultimoEstadoMov = estadoMov;

    String stringMOV = String(estadoMov);
    client.publish("nethome/habitacion/presencia", stringMOV.c_str());
    Serial.print("[MQTT] ¡Cambio de estado! Presencia actual: ");
    Serial.println(estadoMov);
  }

  if (tiempoActual - ultimoMuestreo >= intervalo)
  {
    ultimoMuestreo = tiempoActual;

    // --- LEER SENSORES ---
    float temperaturaC = leerTemp();
    int intensidadLuz = leerLuz();
    // --- IMPRIMIR EN CONSOLA LOCAL ---
    Serial.print("Temperatura: ");
    Serial.print(temperaturaC);
    Serial.println(" °C");
    Serial.print("intensidad de la luz: ");
    Serial.print(intensidadLuz);
    Serial.print(" %");
    // --- ENVIAR POR MQTT (CONVERTIR NÚMEROS A TEXTO) ---
    // PubSubClient solo envía texto (char array), así que convertimos los números
    // --- ENVIAR POR MQTT (MODO SEGURO Y AUTOMÁTICO) ---
    // Usamos String para que el ESP32 calcule el tamaño él
    String stringTemp = String(temperaturaC, 1);
    String stringIntensidad = String(intensidadLuz);

    // El comando .c_str() lo empaqueta perfectamente para el MQTT sin desbordar nada
    client.publish("nethome/habitacion/temperatura", stringTemp.c_str());
    client.publish("nethome/habitacion/intensidad", stringIntensidad.c_str());
  }
}

float leerTemp()
{
  int lecturaNtc = analogRead(PIN_NTC); // Rango de 0 a 4095

  float voltajeNtc = ((lecturaNtc * 3.3) / 4095.0);
  // 2. Calcular la resistencia actual de la NTC basándonos en el voltaje
  // Fórmula del divisor de tensión despejando la resistencia desconocida
  float resistenciaNTC = RES_FIJA * ((3.3 / voltajeNtc) - 1.0);

  // 3. Aplicar la ecuación Beta de Steinhart-Hart para obtener Kelvin
  float temperaturaK;
  temperaturaK = resistenciaNTC / 10000.0; // (R/Ro)
  temperaturaK = log(temperaturaK);        // ln(R/Ro)
  temperaturaK /= COEFICIENTE_B;           // 1/B * ln(R/Ro)
  temperaturaK += 1.0 / (25.0 + 273.15);   // + (1/To)
  temperaturaK = 1.0 / temperaturaK;       // Invertir para obtener Kelvin

  float temperaturaC = temperaturaK - 273.15;
  return temperaturaC;
}

int leerLuz()
{
  // 1. Tomar 10 muestras rápidas para limpiar el ruido
  long sumaLuz = 0;
  for (int i = 0; i < 10; i++)
  {
    sumaLuz += analogRead(PIN_LDR);
    delay(2);
  }
  int nivelLuzBruto = sumaLuz / 10; // Sacamos la media perfecta

  int intensidadLuz = map(nivelLuzBruto, 1000, 4050, 0, 100); // Convertimos a porcentaje

  intensidadLuz = constrain(intensidadLuz, 0, 100); // Aseguramos que esté entre 0 y 100
  return intensidadLuz;
}

int detectarMovimiento()
{
  int deteccionMOV = digitalRead(PIN_MOV); // 0 o 1
  return deteccionMOV;
}