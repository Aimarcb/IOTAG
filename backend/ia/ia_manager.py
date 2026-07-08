import os 
import json
from prompt import generar_prompt
import psycopg2
import google.generativeai as genai

#1. Cargamos la api secreta de Google desde el .env
API_KEY = os.environ.get("GEMINI_API_KEY")
genai.configure(api_key= API_KEY)
modelo = genai.GenerativeModel("gemini-3.5-flash")

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
 
    
def procesar_ia(orden):

    sensores = obtener_datos_db()
        
    if not sensores:
        return "No se pudieron obtener datos de los sensores."
        
    instrucciones = generar_prompt(
        temperatura = sensores['temperatura'],
        luz = sensores['intensidad'],
        movimiento = sensores['presencia'],
        orden_usuario = orden
    ) 

    try:
        respuesta = modelo.generate_content(
            instrucciones,
            generation_config = { "response_mime_type": "application/json" }
        )

        return json.loads(respuesta.text)
        
    except Exception as e:
        print(f"Error al procesar la IA: {e}")
        return {"error": "No se pudo procesar la orden con la IA."}
        
        # --- BLOQUE DE PRUEBAS TEMPORAL ---
# Esto solo se ejecuta si lanzas este archivo directamente
if __name__ == "__main__":
    print("🚀 Iniciando prueba aislada del Cerebro IA...")
    try:
        # Simulamos que el usuario manda este texto desde el móvil
        resultado = procesar_ia("Hace un frío terrible aquí dentro y está oscureciendo.")
        
        print("\n✅ ¡CONEXIÓN EXITOSA CON GEMINI Y POSTGRES!")
        print("--------------------------------------------")
        print(f"🔊 Altavoz: {resultado['respuesta_altavoz']}")
        print(f"💡 Luz: {resultado['rele_luz']}")
        
    except Exception as e:
        print(f"❌ La prueba ha fallado: {e}")