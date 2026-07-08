import os
from typing import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

from .models import Base, MQTTReading


def _build_database_url() -> str:
    if os.getenv("DATABASE_URL"):
        return os.getenv("DATABASE_URL")

    user = os.getenv("POSTGRES_USER", "nethome_user")
    password = os.getenv("POSTGRES_PASSWORD", "nethome_password")
    host = os.getenv("POSTGRES_HOST", "postgres_db")
    port = os.getenv("POSTGRES_PORT", "5432")
    dbname = os.getenv("POSTGRES_DB", "nethome_iot")
    return f"postgresql+psycopg2://{user}:{password}@{host}:{port}/{dbname}"


DATABASE_URL = _build_database_url()
engine = create_engine(DATABASE_URL, pool_pre_ping=True, future=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False, future=True)


def init_db() -> bool:
    try:
        Base.metadata.create_all(bind=engine)
        print("✅ Tablas de base de datos creadas o verificadas")
        return True
    except Exception as exc:
        print(f"⚠️ No se pudo inicializar la base de datos: {exc}")
        return False


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def _parse_payload(payload: str | bytes | bytearray) -> tuple[str | None, float | None, str | None]:
    if isinstance(payload, (bytes, bytearray)):
        payload = payload.decode("utf-8", errors="ignore")
    text = str(payload or "").strip()
    if not text:
        return None, None, None

    device_id = None
    parsed_value = None
    unit = None

    try:
        import json
        data = json.loads(text)
        if isinstance(data, dict):
            device_id = data.get("device_id")
            unit = data.get("unit")
            value = data.get("value")
            if isinstance(value, (int, float)):
                parsed_value = float(value)
    except (TypeError, ValueError):
        pass

    if parsed_value is None:
        try:
            parsed_value = float(text)
        except (TypeError, ValueError):
            parsed_value = None

    return device_id, parsed_value, unit


def save_mqtt_message(topic: str, payload: str | bytes | bytearray) -> MQTTReading:
    device_id, parsed_value, unit = _parse_payload(payload)
    db = SessionLocal()
    try:
        reading = MQTTReading(
            topic=topic,
            device_id=str(device_id) if device_id is not None else None,
            payload=str(payload or ""),
            parsed_value=parsed_value,
            unit=unit,
        )
        db.add(reading)
        db.commit()
        db.refresh(reading)
        return reading
    except Exception as exc:
        db.rollback()
        raise RuntimeError(f"Error al guardar mensaje MQTT: {exc}") from exc
    finally:
        db.close()
