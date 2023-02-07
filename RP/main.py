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
import threading

from socket import *


import cv2  # opencv 모듈

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






form_class = uic.loadUiType("fream.ui")[0]  # ui파일을 불러 온다.





###########################################################
######################  MAIN  #############################
###########################################################

class MyWindow(QMainWindow, form_class):
    def __init__(self):
        super().__init__()
        self.setupUi(self)

        t1 = threading.Thread(target=self.pm25)
        t2 = threading.Thread(target=self.data25)
        t1.daemon=True
        t2.daemon=True
        t1.start()
        t2.start()

        ###### Camera thread ######
        self.cam_thread = cam_thread()
        self.cam_thread.changePixmap.connect(self.set_Image)  # 영상데이터를 받아오는 함수
        self.cam_thread.start()

    ####################################################
    def set_Image(self, image):
        self.label_cctv.setPixmap(QPixmap.fromImage(image))



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

    def closeEvent(self, e):
        self.cam_thread.stop()



    


    def data25(self):
        
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
                print("if 12345")
                
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
                print("here?")
                # 클라이언트로 메시지가 도착하면 다음 줄로 넘어가고
                # 그렇지 않다면 대기(Blocking)
                data, addr = s.recvfrom(BUFSIZE)
                print("here   2???")
                # 받은 메시지와 클라이언트 주소 화면에 출력
                print('server received %r from %r' % (data, addr))
                
 
                

                

                # 받은 메시지를 클라이언트로 다시 전송
                #s.sendto(data, addr)
                # 다시 처음 루프로 돌아감
                #(String)api1.pm25_value
                api_pm25 = api1.pm25_value
                api_humi = api1.api_humidity
                api_temp = api1.weather_data
                
                strf = str(api_pm25) + "," + str(api_humi) + "," + str(api_temp)
                
                s.sendto(strf.encode(), addr)

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


    # 서버 함수
    def server():
        
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
        s.bind(('', port))

        s.sendto(api1.pm25_value, addr)
        # 무한 루프 돌림
        while 1:
            # 클라이언트로 메시지가 도착하면 다음 줄로 넘어가고
            # 그렇지 않다면 대기(Blocking)
            data, addr = s.recvfrom(BUFSIZE)

            # 받은 메시지와 클라이언트 주소 화면에 출력
            #print 'server received %r from %r' % (data, addr)

            # 받은 메시지를 클라이언트로 다시 전송
            #s.sendto(data, addr)
            # 다시 처음 루프로 돌아감
            s.sendto(api1.pm25_value, addr)


    # 클라이언트 함수
    def client():
        # 매개변수가 3개 미만 이라면
        if len(sys.argv) < 3:
            # 사용 방법 화면에 출력
            # usage함수에서 프로그램 종료
            usage()

        # 두번째 매개변수를 서버 IP로 설정
        host = sys.argv[2]

        # 매개변수가 3개를 초과하였다면(4개라면)
        # ex>$ python udp_echo.py -c 127.0.0.1 8001
        if len(sys.argv) > 3:
            # 3번째 매개변수를 포트로 설정
            port = eval(sys.argv[3])

        # 초과하지 않았다면(즉, 3개라면)
        # ex>$ python udp_echo.py -c 127.0.0.1
        else:
            # 기본 포트로 설정
            port = ECHO_PORT

        # IP 주소 변수에 서버 주소와 포트 설정
        addr = host, port

        # 소켓 생성
        s = socket(AF_INET, SOCK_DGRAM)

        # 클라이언트 포트 설정 : 자동
        s.bind(('', 0))

        # 준비 완료 화면에 출력
        #print 'udp echo client ready, reading stdin'

        # 무한 루프
        while 1:
            # 터미널 차(입력창)에서 타이핑을하고 ENTER키를 누를때 까지
            line = sys.stdin.readline()
            # 변수에 값이 없다면
            if not line:
                break

            # 입력받은 텍스트를 서버로 발송
            s.sendto(line, addr)

            # 리턴 대기
            data, fromaddr = s.recvfrom(BUFSIZE)
            # 서버로부터 받은 메시지 출력
            #print 'client received %r from %r' % (data, fromaddr)




######################################################
######################################################
if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = MyWindow()
    window.show()
    sys.exit(app.exec_())
