import calendar
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, timedelta
from database.database import get_db
from database.models import MQTTReading, Configuration

router = APIRouter()

@router.get("/api/electricidad/historial")
def obtener_historial_energia(
    periodo: str = Query("semana", enum=["dia", "semana", "mes", "ano"]),
    mes: int = Query(None),
    ano: int = Query(None),
    db: Session = Depends(get_db)
):
    ahora = datetime.utcnow()
    config = db.query(Configuration).filter(Configuration.key == "precio_kwh").first()
    precio_kwh = config.value if config else 0.15
    
    if periodo == "mes" and mes is not None and ano is not None:
        fecha_limite = datetime(ano, mes, 1, 0, 0, 0)
        ultimo_dia_mes = calendar.monthrange(ano, mes)[1]
        ahora = datetime(ano, mes, ultimo_dia_mes, 23, 59, 59)
        agrupacion = func.date_trunc('day', MQTTReading.received_at)
        
    else:
        if periodo == "dia":
            # Empieza a las 00:00 de hoy
            fecha_limite = ahora.replace(hour=0, minute=0, second=0, microsecond=0)
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
        MQTTReading.received_at <= ahora,
        MQTTReading.topic == "nethome/sensores/potencia"
    ).group_by(agrupacion).order_by("tiempo").all()
    
    datos_reales = {}
    for r in resultados:
        if periodo == "dia":
            etiqueta = r.tiempo.strftime("%H:00")
        elif periodo in ["semana", "mes"]:
            etiqueta = r.tiempo.strftime("%d/%m")
        else: # ano
            etiqueta = r.tiempo.strftime("%m/%Y")
            
        valor_kwh = float(r.total_medida or 0.0) / 1000.0
        datos_reales[etiqueta] = {
            "kwh": valor_kwh,
            "coste": valor_kwh * precio_kwh
        }
    
    historial = []
    
    if periodo == "dia":
        # Creamos las 24 horas del día (de 00:00 a 23:00)
        for i in range(24):
            etiq = f"{i:02d}:00"
            # Buscamos si hay dato real. Si no hay, devolvemos 0.0
            dato = datos_reales.get(etiq, {"kwh": 0.0, "coste": 0.0})
            historial.append({"etiqueta": etiq, "kwh": round(dato["kwh"], 2), "coste": round(dato["coste"], 2)})
            
    elif periodo == "semana":
        # Creamos los últimos 7 días en orden
        for i in range(6, -1, -1):
            etiq = (ahora - timedelta(days=i)).strftime("%d/%m")
            dato = datos_reales.get(etiq, {"kwh": 0.0, "coste": 0.0})
            historial.append({"etiqueta": etiq, "kwh": round(dato["kwh"], 2), "coste": round(dato["coste"], 2)})
            
    elif periodo == "mes" and mes is not None and ano is not None:
        # Creamos todos los días exactos de ese mes calendario (ej. del 1 al 31 de Julio)
        dias_del_mes = calendar.monthrange(ano, mes)[1]
        for i in range(1, dias_del_mes + 1):
            etiq = datetime(ano, mes, i).strftime("%d/%m")
            dato = datos_reales.get(etiq, {"kwh": 0.0, "coste": 0.0})
            historial.append({"etiqueta": etiq, "kwh": round(dato["kwh"], 2), "coste": round(dato["coste"], 2)})
            
    else:
        # Fallback para mes móvil (30 días) o año
        # Mostramos los datos que hay en el diccionario tal cual
        for etiq, dato in datos_reales.items():
            historial.append({"etiqueta": etiq, "kwh": round(dato["kwh"], 2), "coste": round(dato["coste"], 2)})

    # C. Calcular el Pico (ignorando las horas muertas donde el consumo fue 0)
    if historial and any(h["kwh"] > 0 for h in historial):
        elemento_pico = max(historial, key=lambda x: x["kwh"])
        texto_pico = elemento_pico["etiqueta"]
    else:
        texto_pico = "N/A"

    return {
        "periodo": periodo,
        "resumen": {
            "total_kwh_periodo": round(sum(h["kwh"] for h in historial), 2),
            "total_dinero_periodo": round(sum(h["coste"] for h in historial), 2),
            "hora_pico": texto_pico
        },
        "datos_grafica": historial
    }

@router.get("/api/electricidad/fecha-inicio")
def obtener_fecha_inicio(db: Session = Depends(get_db)):
    primer_registro = db.query(func.min(MQTTReading.received_at)).filter(MQTTReading.topic == "nethome/sensores/potencia").scalar()
    fecha_minima = primer_registro if primer_registro else datetime.utcnow()

    return {"mes_inicio": fecha_minima.month,
            "ano_inicio": fecha_minima.year}

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