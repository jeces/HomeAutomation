// 창문
const uint8_t dirPin = 3;
const uint8_t stepPin = 4;
const uint8_t stepsPerRevolution2 = 16;

// 창문자석[닫힘]
const uint8_t doorPin = 12;
uint8_t doorState; //
char window_state = 'O';

// 창문자석2[열림]
const uint8_t doorPin2 = 13;

void setup() {
  // put your setup code here, to run once:
  // 창문
  Serial.begin(115200);
  pinMode(stepPin, OUTPUT);
  pinMode(dirPin, OUTPUT);

  pinMode(doorPin, INPUT);
  pinMode(doorPin2, INPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  while (Serial.available() > 0)
  {
    char data = Serial.read();
    if ( data == 'W') {
      uint8_t value = Serial.parseInt();
      switch (value)
      {
        case 11: // 정방향 -> [닫힘]
          digitalWrite(dirPin, HIGH); // 정회전
          for (int x = 0; x < stepsPerRevolution2; x++)
          {
            for (int y = 0; y < 1000; ++y) {
              digitalWrite(stepPin, HIGH);
              delayMicroseconds(2000);
              digitalWrite(stepPin, LOW);
              delayMicroseconds(2000);
              
              if(Serial.available() > 0) {
                char datas = Serial.read();
                if(datas == 'W') {
                  return;
                }
              }
              // 창문힘
              if (Magnetic() == 1) {
                Serial.println("12");
                return;
              }
              
            }
          }
          break;
        case 10: // 역방향 -> [열림]
          digitalWrite(dirPin, LOW);  // 역회전
          for (int x = 0; x < stepsPerRevolution2; x++)
          {
            for (int y = 0; y < 1000; ++y) {
              digitalWrite(stepPin, HIGH);
              delayMicroseconds(2000);
              digitalWrite(stepPin, LOW);
              delayMicroseconds(2000);
              
              if(Serial.available() > 0) {
                char datas = Serial.read();
                if(datas == 'W') {
                  return;
                }
              }
              //반대쪽 마그네틱 넣어서 창문 다 열렸으면 break;
              if (Magnetic2() == 1) {
                return;
              }
              
            }
          }
          break;
        case 00: // 정지
          break;
      }
    }
  }
}

//마그네틱
int Magnetic() {
  doorState = digitalRead(doorPin); //도어센서 값. 붙으면1 떨어지면0 ??
  //Serial.print("자석센서:");
  //Serial.println(doorState);
  return doorState;
}

//마그네틱
int Magnetic2() {
  doorState = digitalRead(doorPin2); //도어센서 값. 붙으면1 떨어지면0 ??
  //Serial.print("자석센서:");
  //Serial.println(doorState);
  return doorState;
}
