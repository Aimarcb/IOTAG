from fastapi import APIRouter, FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from sqlalchemy import desc
import paho.mqtt.client as mqtt
from typing import List
from pydantic import BaseModel
from ia.ia_manager import procesar_ia
from datetime import datetime, timedelta

from routers import electricidad

from database.database import get_db, init_db
from database.models import MQTTReading

""" 
añadir routers luego para poder hacer una modularizacion de los endponts mediante routers
from fastapi import APIRouter

router = APIRouter()

@router.get("api/...")
"""

nethome_mqtt_client = mqtt.Client()
nethome_mqtt_client.connect("nethome_mosquitto", 1883)
nethome_mqtt_client.loop_start()
#validacion de datos para transforma en string la peticion
class OrdenUsuario(BaseModel):
    mensaje: str
#envio la accion al broker mqtt
def enviar_orden(topic, accion):
    if accion in ["ON","OFF"]:
        nethome_mqtt_client.publish(topic, accion)
    else:
        print(f"Acción inválida: {accion}. Solo se permiten 'ON' o 'OFF'.")


app = FastAPI(
    title="NetHome IoT API",
    description="API para la gestión de dispositivos IoT y lectura de datos de energía",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Poner las ips permitidas
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
def startup_event():
    init_db()  # Esto crea las tablas en Postgres si no existen la primera vez

app.include_router(electricidad.router)

@app.get("/")
def read_root():
    return {"message": "NetHome API activa"}

@app.post("/api/ia/procesar_orden")
def consultar_ia(mensaje: OrdenUsuario):
    # Llama a tu cerebro pasándole la frase que llega del móvil
    decision = procesar_ia(mensaje)
 
    enviar_orden("nethome/accion/luz", decision.get("rele_luz"))

    return {"respuesta": decision.get("respuesta_texto")}

@app.get("/api/electricidad/actual")
def obtener_consumo_actual(db: Session = Depends(get_db)):
    # 1. Obtener la última lectura (la actual)
    lectura_actual = (
        db.query(MQTTReading)
        .filter(MQTTReading.topic == "nethome/energia/potencia")
        .order_by(desc(MQTTReading.received_at))
        .first()
    )

    if not lectura_actual:
        raise HTTPException(status_code=404, detail="No hay lecturas disponibles")

    # 2. Obtener la lectura de hace 1 hora aprox
    una_hora_antes = datetime.utcnow() - timedelta(hours=1)
    
    lectura_pasada = (
        db.query(MQTTReading)
        .filter(MQTTReading.topic == "nethome/energia/potencia")
        .filter(MQTTReading.received_at <= una_hora_antes) # Buscamos una lectura antigua
        .order_by(desc(MQTTReading.received_at))
        .first()
    )

    # 3. Cálculo de tendencia
    tendencia_str = "0% hoy"
    if lectura_pasada and lectura_pasada.parsed_value > 0:
        variacion = ((lectura_actual.parsed_value - lectura_pasada.parsed_value) / lectura_pasada.parsed_value) * 100
        signo = "+" if variacion > 0 else ""
        tendencia_str = f"{signo}{round(variacion, 1)}% hoy"

    return {
        "potencia_kw": round(lectura_actual.parsed_value, 2),
        "tendencia": tendencia_str,
        "unidad": lectura_actual.unit or "kW",
        "fecha": lectura_actual.received_at
    }

@app.get("/api/temperatura/actual")
def obtener_temperatura_actual(db: Session = Depends(get_db)):
    """
    Devuelve la última lectura de temperatura registrada y su diferencia con ayer.
    """
    # 1. Buscamos la temperatura actual
    lectura_actual = (
        db.query(MQTTReading)
        .filter(MQTTReading.topic.like("%temperatura%"))
        .order_by(desc(MQTTReading.received_at))
        .first()
    )
    
    if not lectura_actual:
        raise HTTPException(status_code=404, detail="No hay lecturas ambientales disponibles todavía")

    # 2. Buscamos la temperatura de hace exactamente 24 horas
    un_dia_antes = datetime.utcnow() - timedelta(days=1)
    
    lectura_ayer = (
        db.query(MQTTReading)
        .filter(MQTTReading.topic.like("%temperatura%"))
        .filter(MQTTReading.received_at <= un_dia_antes)
        .order_by(desc(MQTTReading.received_at))
        .first()
    )

    tendencia_str = "Igual que ayer"
    if lectura_ayer:
        diferencia = lectura_actual.parsed_value - lectura_ayer.parsed_value
        if abs(diferencia) >= 0.5: 
            signo = "+" if diferencia > 0 else ""
            tendencia_str = f"{signo}{round(diferencia, 1)}°C que ayer"
        
    return {
        "temperatura": round(lectura_actual.parsed_value, 1),
        "tendencia": tendencia_str,
        "unidad": lectura_actual.unit or "°C",
        "fecha": lectura_actual.received_at
    }

@app.get("/api/temperatura/historial")
def obtener_historial_ambiental(limite: int = 100, db: Session = Depends(get_db)):
    """
    Devuelve las últimas X lecturas ambientales para poder pintar las gráficas en la App.
    """
    lecturas = (
        db.query(MQTTReading)
        .filter(MQTTReading.topic.like("%temperatura%"))
        .order_by(desc(MQTTReading.received_at))
        .limit(limite)
        .all()
    )

    resultado = []
    for l in lecturas:
        resultado.append({
            "dispositivo": l.device_id or "Desconocido",
            "topico": l.topic,
            "valor": l.parsed_value,
            "unidad": l.unit or "°C",   
            "fecha": l.received_at
        })
    return resultado