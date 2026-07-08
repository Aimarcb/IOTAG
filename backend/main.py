from fastapi import FastAPI
from database.database import init_db

app = FastAPI(title="NetHome API")

@app.on_event("startup")
def startup_event():
    init_db()  # Esto crea las tablas en Postgres si no existen la primera vez

@app.get("/")
def read_root():
    return {"message": "NetHome API activa"}
"""""
## Código que añadirá tu hermano en su API:
from fastapi import APIRouter
from ia.ia_manager import procesar_orden_inteligente # <-- Importa tu función

router = APIRouter()

@router.get("/ia/orden")
def api_consultar_ia(texto: str):
    # Llama a tu cerebro pasándole la frase que llega del móvil
    decision = procesar_orden_inteligente(texto)
    
    # Aquí tu hermano añadirá el código para mandar el MQTT al ESP32
    # basado en lo que tú has decidido (decision['rele_luz'], etc.)
    
    return decision"""