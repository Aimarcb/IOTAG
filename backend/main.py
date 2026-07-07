from fastapi import FastAPI
from database.database import init_db

app = FastAPI(title="NetHome API")

@app.on_event("startup")
def startup_event():
    init_db()  # Esto crea las tablas en Postgres si no existen la primera vez

@app.get("/")
def read_root():
    return {"message": "NetHome API activa"}