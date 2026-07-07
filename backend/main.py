from fastapi import FastAPI

app = FastAPI(title="NetHome API")

@app.get("/")
def read_root():
    return {"status": "ok", "message": "API de NetHome funcionando correctamente"}