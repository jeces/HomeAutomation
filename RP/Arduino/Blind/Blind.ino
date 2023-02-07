#include <Stepper.h>
#include "TimerOne.h"
// 블라인드
// 2048:한바퀴(360도), 1024:반바퀴(180도) 512: 90도
const int stepsPerRevolution = 64;

// 모터 드라이브에 연결된 핀 IN4, IN2, IN3, IN1
Stepper myStepper(stepsPerRevolution, 11, 9, 10, 8);
String blind_state = " ";

void setup() {
  Serial.begin(115200);
  Timer1.initialize(100000);  // 0.001sec
  Timer1.attachInterrupt(blindTimer);
  myStepper.setSpeed(512);
}

void loop() {
  while (Serial.available() > 0)  {
    char data = Serial.read();
    if ( data == 'B') {
      uint8_t value = Serial.parseInt();
      switch (value)
      {
        case 11: // 정방향 -> [닫힘]
          blind_state = "Bclose";
          // 시계 반대 방향으로 한바퀴 회전
          for (int i = 0; i < 100; ++i) {
            myStepper.step(stepsPerRevolution);
            if (Serial.available() > 0) {
              char datas = Serial.read();
              if ( datas == 'B') {
                break;
              }
            }
          }
          break;
        case 10: // 역방향 -> [열림]
          blind_state = "Bopen";
          for (int i = 0; i < 100; ++i) {
            myStepper.step(-stepsPerRevolution);
            if (Serial.available() > 0) {
              char datas = Serial.read();
              if ( datas == 'B') {
                break;
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

void blindTimer(){
  //Serial.println(blind_state);
}
