# coding : utf-8

import sys
from PyQt5 import uic, QtCore, QtGui, QtWidgets
from PyQt5.QtCore import *
from PyQt5.QtWidgets import *
from PyQt5.QtGui import QImage, QPixmap
from PyQt5.QtTest import QTest

import api1
from urllib import *
import time
import serial
import threading

from socket import *

port="/dev/ttyACM0"
port2="/dev/ttyACM1"
port3="/dev/ttyACM3"
ser = serial.Serial(port, 115200)
ser2 = serial.Serial(port2, 115200)
ser3 = serial.Serial(port3, 115200)
print(ser.portstr)
print(ser2.portstr)
print(ser3.portstr)

# import js2py  # 자바를 쓸 경우 주석 해제
# import RPi.GPIO as GPIO

# 웹 크롤링시 주석해제
# from urllib.request import urlopen
# from bs4 import BeautifulSoup

#! /usr/bin/env python

# Client and server for udp (datagram) echo.
#
# Usage: udpecho -s [port]            (to start a server)
# or:    udpecho -c host [port] <file (client)

# 통신
from socket import *

# ECHO_PORT 기본 포트
ECHO_PORT = 8011

# 버퍼 사이즈
BUFSIZE = 1024

#Singnal
Signal = b''

#Dust
Ardstate = ""

#Window_state
window_state = ""

#Blind_state
blind_state = ""

#Fan_state
fan_state = ""

#Led_state
led_state = ""

#아두이노에서 받은 상태 배열 [온도,습도,먼지,창문]
state = []



form_class = uic.loadUiType("fream.ui")[0]  # ui파일을 불러 온다.
###########################################################
######################  MAIN  #############################
###########################################################

class MyWindow(QMainWindow, form_class):
    def __init__(self):
        super().__init__()
        self.setupUi(self)


#################버튼

        self.pushButton_windows_on.clicked.connect(self.window_Right)
        self.pushButton_windows_off.clicked.connect(self.window_left)
        self.pushButton_windows_stop.clicked.connect(self.window_stop)

        self.pushButton_blind_right.clicked.connect(self.blind_Right)
        self.pushButton_blind_left.clicked.connect(self.blind_left)
        self.pushButton_blind_stop.clicked.connect(self.blind_stop)

        self.pushButton_light_on.clicked.connect(self.light_on)
        self.pushButton_light_off.clicked.connect(self.light_off)

        self.pushButton_fan_on.clicked.connect(self.fan_on)
        self.pushButton_fan_off.clicked.connect(self.fan_off)

####################스래드

        t1 = threading.Thread(target=self.pm25)  #API
        t2 = threading.Thread(target=self.data25)  #소켓
        t3 = threading.Thread(target=self.dataArdu)  #아두이노 시리얼
        t4 = threading.Thread(target=self.ser1) #시리얼1
        t5 = threading.Thread(target=self.ser2) #시리얼2
        t6 = threading.Thread(target=self.ser3) #시리얼2
        t1.daemon=True
        t2.daemon=True
        t3.daemon=True
        t4.daemon=True
        t5.daemon=True
        t6.daemon=True
        t1.start()
        t2.start()
        t3.start()
        t4.start()
        t5.start()
        t6.start()

######################스레드 끝

    def window_Right(self):
        global Signal
        global window_state
        if Signal == b'':
            Signal = "W11".encode()
            window_state = "Wopen"
            print("window right")

    def window_left(self):
        global Signal
        global window_state
        if Signal == b'':
            Signal = "W10".encode()
            window_state = "Wclose"
            print("window left")

    def window_stop(self):
        global Signal
        global window_state
        if Signal == b'':
            Signal = "W00".encode()
            window_state = "Wstop"
            print("window stop")

    def blind_Right(self):
        global Signal
        global blind_state
        if Signal == b'':
            Signal = "B11".encode()
            blind_state = "Bopen"
            print("blind right")

    def blind_left(self):
        global Signal
        global blind_state
        if Signal == b'':
            Signal = "B10".encode()
            blind_state = "Bclose"
            print("blind left")

    def blind_stop(self):
        global Signal
        global blind_state
        if Signal == b'':
            Signal = "B00".encode()
            blind_state = "Bstop"
            print("blind stop")

    def light_on(self):
        global Signal
        global led_state
        if Signal == b'':
            Signal = "L11".encode()
            led_state = "Lon"
            print("led on")

    def light_off(self):
        global Signal
        global led_state
        if Signal == b'':
            Signal = "L00".encode()
            led_state = "Loff"
            print("led on")

    def fan_on(self):
        global Signal
        global fan_state
        if Signal == b'':
            Signal = "F11".encode()
            fan_state = "Fon"
            print("fan on")

    def fan_off(self):
        global Signal
        global fan_state
        if Signal == b'':
            Signal = "F00".encode()
            fan_state = "Foff"
            print("fan on")

    def set_Image(self, image):
        self.label_cctv.setPixmap(QPixmap.fromImage())

    def pm25(self):
        while True:
            api_pm25 = api1.pm25_value
            api_humi = api1.api_humidity
            api_temp = api1.weather_data

            QTest.qWait(2000)
            self.lcdNumber_api_pm25.display(api_pm25)
            QTest.qWait(2000)
            self.lcdNumber_api_humidity.display(api_humi)
            QTest.qWait(2000)
            self.lcdNumber_api_temperature.display(api_temp)

    def dataArdu(self): #라즈베리에서 아두이노로 데이터를 보내는것이고 
        global Signal
        while True:
            if Signal != b'':
                print("Arduino Send")
                ser.write(str(Signal).encode())
                ser2.write(str(Signal).encode())
                ser3.write(str(Signal).encode())
                Signal = b''
                
    def ser1(self):   #첫번째 아두이노에서 데이터를 보내는것
        global Ardstate
        while True:
            if ser.readable():
                print("Arduino1 Receive")
                input_s = ser.readline()
                input = input_s.decode('utf-8').strip()
                #Ardstate = input
                print(Ardstate)
                
    def ser2(self): #두번재 아두이노에서 데이터를  보내는것
        while True:
            if ser2.readable():
                print("Arduino2 Receive")
                input_s2 = ser2.readline()
                input2 = input_s2.decode('utf-8').strip()
                print(input2)

    def ser3(self): #두번재 아두이노에서 데이터를  보내는것
        global Ardstate
        while True:
            if ser3.readable():
                print("Arduino3 Receive")
                input_s3 = ser3.readline()
                input3 = input_s3.decode('utf-8').strip()
                Ardstate = input3
                print(input3)

    def data25(self):
        global Signal
        global Ardstate
        global state
        global blind_state
        global fan_state
        global led_state
        # 매개변수가 2개보다 적다면
        if len(sys.argv) < 2:
            # 사용 방법 표시
            usage()

        # 첫 매개변수가 '-s' 라면
        if sys.argv[1] == '-s':
            # 서버 함수 호출
            # 매개 변수가 2개 초과라면
            
            # ex>$ python udp_echo.py -s 8001
            if len(sys.argv) > 2:
                # 두번째 매개변수를 포트로 지정
                port = eval(sys.argv[2])
                
                
            # 매개 변수가 2개 라면
            # ex>$ python udp_echo.py -s
            else:
                # 기본 포트로 설정
                port = ECHO_PORT

            # 소켓 생성 (UDP = SOCK_DGRAM, TCP = SOCK_STREAM)
            s = socket(AF_INET, SOCK_DGRAM)

            # 포트 설정
            s.bind(("192.168.0.4", port))
            
            # 무한 루프 돌림
            while 1:
                # 클라이언트로 메시지가 도착하면 다음 줄로 넘어가고
                # 그렇지 않다면 대기(Blocking)
                data, addr = s.recvfrom(BUFSIZE)
                # 받은 메시지와 클라이언트 주소 화면에 출력
                print('server received %r from %r' % (data, addr))
                Signal = data
                print(data)
                # 받은 메시지를 클라이언트로 다시 전송
                #s.sendto(data, addr)
                # 다시 처음 루프로 돌아감
                #(String)api1.pm25_value
                api_pm25 = api1.pm25_value
                api_humi = api1.api_humidity
                api_temp = api1.weather_data

                state = Ardstate.split(',')
                print(state)

                #api먼지,api습도,api온도,실내온도,실내습도,실내먼지,창문상태,블라인드상태,led상태,fan상태
                strf = str(api_pm25) + "," + str(api_humi) + "," + str(api_temp) + "," + state[0] + "," + state[1] + "," + state[2] + "," + state[3] + "," + blind_state + "," + fan_state + "," + led_state
                
                s.sendto(strf.encode(), addr)

                state[3] = ""
                blind_state = ""
                led_state = ""
                fan_state =""
                print(strf)

        # 첫 매개변수가 '-c' 라면
        elif sys.argv[1] == '-c':
            # 클라이언트 함수 호출
            client()

        # '-s' 또는 '-c' 가 아니라면
        else:
            # 사용 방법 표시
            usage()

    # 사용하는 방법 화면에 표시하는 함수
    def usage():
        sys.stdout = sys.stderr
        # 종료
        sys.exit(2)

######################################################
######################################################
if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = MyWindow()
    window.show()
    sys.exit(app.exec_())