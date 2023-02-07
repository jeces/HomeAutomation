#include <Arduino_FreeRTOS.h> // RTOS 기법
#include <SoftwareSerial.h>
#include <dht.h>

// 미세먼지 # 커넥터 상단위치 기준 TX/RX/VCC/GND 순서
const uint8_t pm25Tx = 7;
const uint8_t pm25Rx = 6;
SoftwareSerial pm25_Serial(pm25Tx,pm25Rx);

//온습도
dht DHT;
const uint8_t dhtpin = 2;
String temphumi = "";

struct
{
    uint32_t total;
    uint32_t ok;
    uint32_t crc_error;
    uint32_t time_out;
    uint32_t connect;
    uint32_t ack_l;
    uint32_t ack_h;
    uint32_t unknown;
} stat = { 0,0,0,0,0,0,0,0}; 

// LED
const uint8_t ledPin = 12;
const uint8_t ledPin2 = 8;
const uint8_t ledPin3 = 9;

// 릴레이 FAN
const uint8_t fanSig = 3;

String fan_state = "Foff";
String led_state = "Loff";

void setup() {  
  xTaskCreate(taskPMS, (const char*)"taskPMS", 128, NULL, 2, NULL); // 미세먼지
  xTaskCreate(readTemperatureSensor, (const char*)"readTemperatureSensor", 128, NULL, 2, NULL); // 온습도
  xTaskCreate(taskFanLed, (const char*)"taskFanLed", 128, NULL, 2, NULL); // 팬, 전구
  
  Serial.begin(115200);

  // 미세먼지
  pm25_Serial.begin(9600);
  
  // 온습도
  //Serial.println("Type,\tstatus,\tHumidity (%),\tTemperature (C)\tTime (us)");

  // 팬, 전구
  pinMode(ledPin, OUTPUT);
  pinMode(ledPin2, OUTPUT);
  pinMode(ledPin3, OUTPUT);
  pinMode(fanSig, OUTPUT);
  digitalWrite(ledPin, LOW);
  digitalWrite(ledPin2, LOW);
  digitalWrite(ledPin3, LOW);
  digitalWrite(fanSig, HIGH);

}

void loop() {
  // put your main code here, to run repeatedly:
}

void readTemperatureSensor(void * pvParameter)
{
  int* parameter_value = (int*) pvParameter;
  for (;;)
  {
    // READ DATA
    //Serial.print("DHT22, \t");
 
    uint32_t start = micros();
    int chk = DHT.read22(dhtpin);
    uint32_t stop = micros();
 
    stat.total++;
    switch (chk)
    {
    case DHTLIB_OK:
        stat.ok++;
        //Serial.print("OK,\t");
        break;
    case DHTLIB_ERROR_CHECKSUM:
        stat.crc_error++;
        //Serial.print("Checksum error,\t");
        break;
    case DHTLIB_ERROR_TIMEOUT:
        stat.time_out++;
        //Serial.print("Time out error,\t");
        break;
    case DHTLIB_ERROR_CONNECT:
        stat.connect++;
        //Serial.print("Connect error,\t");
        break;
    case DHTLIB_ERROR_ACK_L:
        stat.ack_l++;
        //Serial.print("Ack Low error,\t");
        break;
    case DHTLIB_ERROR_ACK_H:
        stat.ack_h++;
        //Serial.print("Ack High error,\t");
        break;
    default:
        stat.unknown++;
        //Serial.print("Unknown error,\t");
        break;
    }
    // DISPLAY DATA
//    Serial.print(DHT.humidity, 1);
//    Serial.print(",\t");
//    Serial.print(DHT.temperature, 1);
//    Serial.print(",\t");
//    Serial.print(stop - start);
//    Serial.println();
 
    temphumi = String(DHT.temperature) + "," + String(DHT.humidity);
 
    vTaskDelay(2000 / portTICK_PERIOD_MS);
  }
}


// 미세먼지
void taskPMS(void * vp_Parameter) {
  int* param = (int*)vp_Parameter;
  for(;;) {  //LOOP    
    
    
    //float h = dht.readHumidity();
    //float t = dht.readTemperature();
    
    static int CheckFirst = 0;
    static int pm_add[3][5] = {0,};
    static int pm_old[3] = {0,};
    int chksum = 0, res = 0;
    unsigned char pms[32] = {0,};
  
    if(pm25_Serial.available() >= 32) {
      for(int j = 0; j < 32; j++) {
        pms[j] = pm25_Serial.read();
        if(j < 30)
          chksum += pms[j];
      }

      //String myString1 = String(pms[12]);
      String myString2 = String(pms[13]);
      String str =  myString2;
      Serial.print(temphumi);
      Serial.print(",");
      Serial.print(str);
      Serial.print(",");
      Serial.print(led_state);
      Serial.print(",");
      Serial.println(fan_state);

      if(pms[13] > 20) {
        digitalWrite(fanSig, LOW);
        digitalWrite(ledPin2, HIGH);
        digitalWrite(ledPin3, LOW);
      } else {
        digitalWrite(ledPin2, LOW);
        digitalWrite(ledPin3, HIGH);
      }
    }
  }
}

// Fan and Led
void taskFanLed(void * vp_Parameter) {
  for(;;) {
    while (Serial.available() > 0) {
      char data = Serial.read();
      if ( data == 'L' ) {
        uint8_t value = Serial.parseInt();
        switch (value)
        {
          case 11:
            led_state = "Lon";
            digitalWrite(ledPin, HIGH);
            break;
          case 00:
            led_state = "Loff";
            digitalWrite(ledPin, LOW);
            break;
        }
      }
      // FAN
      if ( data == 'F' ) {
        uint8_t value = Serial.parseInt();
        switch (value)
        {
          case 11:
            fan_state = "Fon";
            digitalWrite(fanSig, LOW);
            break;
          case 00:
            fan_state = "Foff";
            digitalWrite(fanSig, HIGH);
            break;
        }
      }
    }
  }
}
