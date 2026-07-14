import os 
import json
from ia.prompt import generar_prompt
import psycopg2
import google.generativeai as genai

#1. Cargamos la api secreta de Google desde el .env
API_KEY = os.environ.get("GEMINI_API_KEY")
genai.configure(api_key= API_KEY)
modelo = genai.GenerativeModel('gemini-3.1-flash-lite')

# 2. Datos de conexión a la Base de Datos (sacados del .env automáticamente)
DB_HOST = "postgres_db"  # Nombre del contenedor en el docker-compose
DB_USER = os.environ.get("POSTGRES_USER")
DB_PASSWORD = os.environ.get("POSTGRES_PASSWORD")
DB_NAME = os.environ.get("POSTGRES_DB")

def obtener_datos_db():

    try: 
        conexion = psycopg2.connect(
            host=DB_HOST,
            user=DB_USER,
            password=DB_PASSWORD,
            dbname=DB_NAME
        )
        cursor = conexion.cursor()

        
        query = """
            SELECT DISTINCT ON (topic) topic, parsed_value
            FROM mqtt_readings 
            ORDER BY topic, received_at DESC
        """
        cursor.execute(query)
        resultados = cursor.fetchall()   # Devuelve algo [('nethome/habitacion/temperatura', 24.5), ('nethome/habitacion/luz', 45.0)]
        
        # Lo convertimos a un diccionario limpio de Python
        datos = {topic.split('/')[-1]: valor for topic, valor in resultados}
        
        cursor.close()
        conexion.close()
        return datos
    
    except Exception as e:
        print(f"Error al obtener datos de la base de datos: {e}")
 
    
def procesar_ia(mensaje_usuario: str):

    sensores = obtener_datos_db()
        
    if not sensores:
        return "No se pudieron obtener datos de los sensores."
        
    instrucciones = generar_prompt(
        temperatura = sensores.get('temperatura', 20.0),
        luz = sensores.get('intensidad', 50.0),
        movimiento = sensores.get('presencia', 0),
        orden_usuario = mensaje_usuario
    ) 
    import os

    try:
        respuesta = modelo.generate_content(
            instrucciones,
            generation_config = { "response_mime_type": "application/json" }
        )

        return json.loads(respuesta.text)
        
    except Exception as e:
        print(f"Error al procesar la IA: {e}")
        return {"error": "No se pudo procesar la orden con la IA."}
 