// 1. PINES Y VARIABLES
const int pinSeguridad = 2; 
const int pinLampara = 3;     
const int pinVentiladores = 4; 

// 2. REGISTRO DE INVENTARIO
String objetos[] = {"Ninguno", "Teclado", "Mouse", "Anteojos", "Kit Robotica"};
int objetoActualID = 0; // Se recibirá desde la App

// 3. CONTROL DE TIEMPO (Sin delay)
unsigned long tiempoReferencia = 0;
const long duracionUVC = 10000;
const long duracionEnfriamiento = 5000;

// ETAPAS DEL SISTEMA
int etapa = 0; // 0: Espera, 1: Desinfectando, 2: Enfriando (Gestion Termica)

void setup() {
  Serial.begin(9600); 
  pinMode(pinSeguridad, INPUT_PULLUP);
  pinMode(pinLampara, OUTPUT);
  pinMode(pinVentiladores, OUTPUT);
  digitalWrite(pinLampara, LOW);
  digitalWrite(pinVentiladores, LOW);
  Serial.println("PuraBox: Seleccione objeto en la App y presione 'S'");
}
void apagarTodo() {
  digitalWrite(pinLampara, LOW);
  digitalWrite(pinVentiladores, LOW);
}

void loop() {
  // --- CAPA DE SEGURIDAD PRIORITARIA ---
  if (digitalRead(pinSeguridad) == HIGH) 3{ // Tapa abierta
    if (etapa > 0) {
      Serial.println("ALERTA: Tapa abierta! Corte de emergencia.");
    }
    apagarTodo();
    etapa = 0;
    return; 
  }

  // --- CAPA DE CONTROL DE CICLO ---
  switch (etapa) {
    case 0: // ESPERA
      if (Serial.available() > 0) {
        char comando = Serial.read();
        if (comando == 'S') { // Iniciar ciclo
          iniciarCiclo();
        }
      }
      break;

    case 1: // ETAPA: DESINFECCIÓN UVC
      if (millis() - tiempoReferencia >= duracionUVC) {
        digitalWrite(pinLampara, LOW);
        Serial.println("UVC Finalizado. Iniciando Gestion Termica...");
        tiempoReferencia = millis(); // Reiniciamos cronómetro para enfriar
        etapa = 2;
      }
      break;

    case 2: // ETAPA: GESTIÓN TÉRMICA (ENFRIAMIENTO)
      if (millis() - tiempoReferencia >= duracionEnfriamiento) {
        digitalWrite(pinVentiladores, LOW);
        registrarExito();
        etapa = 0;
      }
      break;
  }
}

void iniciarCiclo() {
  // Simulación: La app nos dijo que pusimos un Teclado (ID 1)
  objetoActualID = 1; 
  
  Serial.print("Iniciando desinfeccion de: ");
  Serial.println(objetos[objetoActualID]);
  
  digitalWrite(pinVentiladores, HIGH);
  digitalWrite(pinLampara, HIGH);
  tiempoReferencia = millis(); // "Clava" el tiempo de inicio
  etapa = 1;
}

void registrarExito() {
  Serial.print("REGISTRO: ");
  Serial.print(objetos[objetoActualID]);
  Serial.println(" sanitizado correctamente.");
  Serial.println("--- Listo para el siguiente objeto ---");
}

