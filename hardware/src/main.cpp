#include <Arduino.h>
#include <DHT.h>
#include "configuration.h"

#define DHTPIN 4
#define DHTTYPE DHT11

DHT dht(DHTPIN, DHTTYPE);

void readTempHum();

void setup()
{
  Serial.begin(115200);

  // inicalizamos el sensor
  dht.begin();
  delay(2000);
}

void loop()
{

  readTempHum();
  delay(2000); 
}

// put function definitions here:
void readTempHum()
{
  float humedad = dht.readHumidity();
  float temperatura = dht.readTemperature();

  if (isnan(humedad) || isnan(temperatura))
  {
    Serial.println("Error en la lectura del sensor DHT");
  }

  Serial.print("Humedad: ");
  Serial.print(humedad);
  Serial.print("%  |  Temperatura: ");
  Serial.print(temperatura);
  Serial.println("°C");
}