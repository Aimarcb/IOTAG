from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, timedelta
from database.database import get_db
from database.models import MQTTReading

from database.models import Configuration

router = APIRouter()

@router.get("/api/electricidad/historial")
def obtener_historial_energia(
    periodo: str = Query("semana", enum=["dia", "semana", "mes", "ano"]), 
    db: Session = Depends(get_db)
):
    ahora = datetime.utcnow()

    config = db.query(Configuration).filter(Configuration.key == "precio_kwh").first()
    precio_kwh = config.value if config else 0.15
    
    # 1. Ajustar el límite de tiempo y la agrupación
    if periodo == "dia":
        fecha_limite = ahora - timedelta(days=1)
        agrupacion = func.date_trunc('hour', MQTTReading.received_at)
    elif periodo == "semana":
        fecha_limite = ahora - timedelta(days=7)
        agrupacion = func.date_trunc('day', MQTTReading.received_at)
    elif periodo == "mes":
        fecha_limite = ahora - timedelta(days=30)
        agrupacion = func.date_trunc('day', MQTTReading.received_at)
    else: # "ano"
        fecha_limite = ahora - timedelta(days=365)
        agrupacion = func.date_trunc('month', MQTTReading.received_at)

    # 2. Consultar SOLO las filas donde el topic sea el de la potencia
    resultados = db.query(
        agrupacion.label("tiempo"),
        func.sum(MQTTReading.parsed_value).label("total_medida") # Sumamos tu columna real
    ).filter(
        MQTTReading.received_at >= fecha_limite,
        MQTTReading.topic == "nethome/sensores/potencia"
    ).group_by(agrupacion).order_by("tiempo").all()
    
    historial = []
    for r in resultados:
        if periodo == "dia":
            label_tiempo = r.tiempo.strftime("%H:00")
        elif periodo == "semana" or periodo == "mes":
            label_tiempo = r.tiempo.strftime("%d/%m")
        else: # ano
            label_tiempo = r.tiempo.strftime("%m/%Y")
            
        # Como guardas Potencia en W, simulamos pasarlo a kWh dividiendo entre 1000 (ajusta según necesites)
        valor_kwh = float(r.total_medida or 0.0) / 1000.0
        valor_coste = valor_kwh * precio_kwh
            
        historial.append({
            "etiqueta": label_tiempo,
            "kwh": round(valor_kwh, 2),
            "coste": round(valor_coste, 2)
        })

    # 4. Respuesta JSON
    return {
        "periodo": periodo,
        "resumen": {
            "total_kwh_periodo": round(sum(h["kwh"] for h in historial), 2),
            "total_dinero_periodo": round(sum(h["coste"] for h in historial), 2),
            "hora_pico": "21:00" # Simulado por ahora
        },
        "datos_grafica": historial
    }

@router.post("/api/config/precio")
def actualizar_precio(nuevo_precio: float, db: Session = Depends(get_db)):
    config = db.query(Configuration).filter(Configuration.key == "precio_kwh").first()
    if config:
        config.value = nuevo_precio
    else:
        config = Configuration(key="precio_kwh", value=nuevo_precio)
        db.add(config)
    db.commit()
    return {"message": "Precio actualizado correctamente"}