from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from sqlalchemy import desc
from typing import List

from database.database import get_db, init_db
from database.models import MQTTReading

app = FastAPI(
    title="NetHome IoT API",
    description="API para la gestión de dispositivos IoT y lectura de datos de energía",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Poner las ips permitidas
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
def startup_event():
    init_db()  # Esto crea las tablas en Postgres si no existen la primera vez

@app.get("/api/temperatura/actual")
def obtener_temperatura_actual(db: Session = Depends(get_db)):
    """
    Devuelve la última lectura de temperatura registrada en la base de datos.
    Filtra los tópicos que contengan datos ambientales.
    """
    # Buscamos el último registro cuyo tópico sea el del sensor ambiental
    lectura = (
        db.query(MQTTReading)
        .filter(MQTTReading.topic.like("%temperatura%"))
        .order_by(desc(MQTTReading.received_at))
        .first()
    )
    
    if not lectura:
        raise HTTPException(status_code=404, detail="No hay lecturas ambientales disponibles todavía")
        
    return {
        "dispositivo": lectura.device_id or "Desconocido",
        "topico": lectura.topic,
        "valor": lectura.parsed_value,
        "unidad": lectura.unit or "°C",
        "fecha": lectura.received_at
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