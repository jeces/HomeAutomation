//센서 지문인식 빨간선부터 vcc(3.3V), 2번(rx), 3번(tx), gnd

#include <SoftwareSerial.h>
#include <Adafruit_Fingerprint.h>

// pin #2 is IN from sensor 
// pin #3 is OUT from arduino  
// Set up the serial port to use softwareserial

const uint8_t fp_Rx = 2;
const uint8_t fp_Tx = 3;

SoftwareSerial fp_Serial(fp_Rx, fp_Tx);

Adafruit_Fingerprint finger = Adafruit_Fingerprint(&fp_Serial);

uint8_t id;

//릴레이
const uint8_t relaypin = 5;

void setup() {
  // put your setup code here, to run once:  
  
  Serial.begin(115200);
  while (!Serial);  // For Yun/Leo/Micro/Zero/...
  delay(100);

  //릴레이
  pinMode(relaypin, OUTPUT);
  digitalWrite(relaypin, HIGH);

  finger.begin(57600);

  if (finger.verifyPassword()) {
    Serial.println("Found fingerprint sensor!"); 
  } else {
    Serial.println("Did not find fingerprint sensor :(");
    while (1); { delay(1); }
  }  
  finger_menu(); 

//  pinMode(led_pin, OUTPUT);
  
}

/////////////// Serial 메뉴창 ///////////////////////
void finger_menu(){
  finger.getTemplateCount(); // 현재 등록된 계정
  Serial.println(F("\n|----------[ 지문등록 menu ]----------|"));
  Serial.println(F("| type 'E' : Enroll finger id & data |"));
  Serial.println(F("| type 'D' : Delete finger Id        |"));
  Serial.println(F("| type 'L' : Show account number     |"));
  Serial.println(F("|------------------------------------|"));
}

uint8_t readnumber(void) {
  uint8_t num = 0;

  while (num == 0) {
    while (! Serial.available());
    num = Serial.parseInt();
  }
  return num;
}
void loop() {
  getFingerprintIDez();  
  
    
  while(Serial.available()>0)
  {
    char finger_option = Serial.read();

    if(finger_option == 'E' || finger_option == 'e')
    {
      Serial.println("\nEnroll Finger");
      Serial.println("Ready to enroll a fingerprint!");
      Serial.println("등록할 ID # 숫자(1 ~ 127)를 입력하세요...");
      id = readnumber();
      if (id == 0) {// ID #0 not allowed, try again!
        return;
      }     
      
      Serial.print("Enrolling ID #");
      Serial.println(id);
      while (!  getFingerprintEnroll() );              
    }

    if(finger_option == 'D' || finger_option == 'd')
    {      
      Serial.println("\nDelete Finger");

      Serial.println("삭제할 ID # 숫자(1 ~ 127)를 입력하세요...");
      id = readnumber();
      if (id == 0) {// ID #0 not allowed, try again!
        return;
      }
      
      Serial.print("Deleting ID #");
      Serial.println(id);
      deleteFingerprint(id);      
    }

    if(finger_option == 'L' || finger_option == 'l')
    {
       if (finger.templateCount == 0) {
        Serial.println("등록된 지문 데이터가 없습니다. 먼저 등록하세요");
        finger_menu();
       }
       else {
        Serial.println("Waiting for valid finger...");
        Serial.print("등록된 계정 수 : "); Serial.println(finger.templateCount);
       }
    }    
  }
}




/////////////////////////////지문 등록//////////////////////////////////////
uint8_t getFingerprintEnroll() {

  int p = -1;
  Serial.print("Waiting for valid finger to enroll as #"); Serial.println(id);
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image taken");
      break;
    case FINGERPRINT_NOFINGER:
      Serial.println(".");
      break;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      break;
    case FINGERPRINT_IMAGEFAIL:
      Serial.println("Imaging error");
      break;
    default:
      Serial.println("Unknown error");
      break;
    }
  }

  // OK success!

  p = finger.image2Tz(1);
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image converted");
      break;
    case FINGERPRINT_IMAGEMESS:
      Serial.println("Image too messy");
      return p;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      return p;
    case FINGERPRINT_FEATUREFAIL:
      Serial.println("Could not find fingerprint features");
      finger_menu();
      return p;
    case FINGERPRINT_INVALIDIMAGE:
      Serial.println("Could not find fingerprint features");
      finger_menu();
      return p;
    default:
      Serial.println("Unknown error");
      return p;
  }

  Serial.println("Remove finger");
  delay(2000);
  p = 0;
  while (p != FINGERPRINT_NOFINGER) {
    p = finger.getImage();
  }
  Serial.print("ID "); Serial.println(id);
  p = -1;
  Serial.println("Place same finger again");
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image taken");
      break;
    case FINGERPRINT_NOFINGER:
      Serial.print(".");
      break;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      break;
    case FINGERPRINT_IMAGEFAIL:
      Serial.println("Imaging error");
      break;
    default:
      Serial.println("Unknown error");
      break;
    }
  }

  // OK success!

  p = finger.image2Tz(2);
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image converted");
      break;
    case FINGERPRINT_IMAGEMESS:
      Serial.println("Image too messy");
      return p;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      return p;
    case FINGERPRINT_FEATUREFAIL:
      Serial.println("Could not find fingerprint features");
      finger_menu();
      return p;
    case FINGERPRINT_INVALIDIMAGE:
      Serial.println("Could not find fingerprint features");
      finger_menu();
      return p;
    default:
      Serial.println("Unknown error");
      return p;
  }

  // OK converted!
  Serial.print("Creating model for #");  Serial.println(id);

  p = finger.createModel();
  if (p == FINGERPRINT_OK) {
    Serial.println("Prints matched!");
  } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
    Serial.println("Communication error");
    return p;
  } else if (p == FINGERPRINT_ENROLLMISMATCH) {
    Serial.println("Fingerprints did not match");
    finger_menu();
    return p;
  } else {
    Serial.println("Unknown error");
    return p;
  }

  Serial.print("ID "); Serial.println(id);
  p = finger.storeModel(id);
  if (p == FINGERPRINT_OK) {
    Serial.println("Stored!");
  } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
    Serial.println("Communication error");
    return p;
  } else if (p == FINGERPRINT_BADLOCATION) {
    Serial.println("Could not store in that location");
    return p;
  } else if (p == FINGERPRINT_FLASHERR) {
    Serial.println("Error writing to flash");
    return p;
  } else {
    Serial.println("Unknown error");
    return p;
  }

  finger_menu();
  return true;
}

//////////////////////////// 지문삭제  /////////////////////////
uint8_t deleteFingerprint(uint8_t id) {
  uint8_t p = -1;

  p = finger.deleteModel(id);

  if (p == FINGERPRINT_OK) {
    Serial.println("Deleted!");
  } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
    Serial.println("Communication error");
    return p;
  } else if (p == FINGERPRINT_BADLOCATION) {
    Serial.println("Could not delete in that location");
    return p;
  } else if (p == FINGERPRINT_FLASHERR) {
    Serial.println("Error writing to flash");
    return p;
  } else {
    Serial.print("Unknown error: 0x"); Serial.println(p, HEX);
    return p;
  }
  finger_menu();
}

////////////////////// 지문 인식체크 ////////////////////////////
uint8_t getFingerprintID() {
  uint8_t p = finger.getImage();
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image taken");
      break;
    case FINGERPRINT_NOFINGER:
      Serial.println("No finger detected");
      return p;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");      
      return p;
    case FINGERPRINT_IMAGEFAIL:
      Serial.println("Imaging error");
      return p;
    default:
      Serial.println("Unknown error");
      return p;
  }

  // OK success!

  p = finger.image2Tz();
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image converted");
      break;
    case FINGERPRINT_IMAGEMESS:
      Serial.println("Image too messy");
      return p;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      return p;
    case FINGERPRINT_FEATUREFAIL:
      Serial.println("Could not find fingerprint features");
      return p;
    case FINGERPRINT_INVALIDIMAGE:
      Serial.println("Could not find fingerprint features");
      return p;
    default:
      Serial.println("Unknown error");
      return p;
  }

  // OK converted!
  p = finger.fingerSearch();
  if (p == FINGERPRINT_OK) {
    Serial.println("Found a print match!");
  } else if (p == FINGERPRINT_PACKETRECIEVEERR) {
    Serial.println("Communication error");
    return p;
  } else if (p == FINGERPRINT_NOTFOUND) {
    Serial.println("Did not find a match");
    return p;
  } else {
    Serial.println("Unknown error");
    return p;
  }

  // found a match!
  Serial.print("Found ID #"); Serial.print(finger.fingerID);
  Serial.print(" with confidence of "); Serial.println(finger.confidence);

  return finger.fingerID;
}



int getFingerprintIDez() {
  uint8_t p = finger.getImage();
  if (p != FINGERPRINT_OK)  return -1;

  p = finger.image2Tz();
  if (p != FINGERPRINT_OK)  return -1;

  p = finger.fingerFastSearch();
  if (p != FINGERPRINT_OK){
    Serial.println("등록된 지문이 아닙니다");  
    return -1;
  }

  // found a match!
  Serial.print("접속자 ID #"); Serial.print(finger.fingerID);
  Serial.print(" with confidence of "); Serial.println(finger.confidence);
  if(finger.confidence >= 100){
    Serial.print("지문인식 성공, " );    
    digitalWrite(relaypin, LOW); // 도어락 열림
    Serial.println("도어락 열림");
    delay(3000); //3초동안 열림
    digitalWrite(relaypin, HIGH);
    Serial.println("도어락 닫힘");
  }
  else{
    Serial.println("retouch");      
  }
  
  return finger.fingerID;
}
