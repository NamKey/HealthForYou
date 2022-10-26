# HealthForYou (2017.05 ~ 2017.09)
OPENCV를 통한 심박수 및 호흡수를 측정하고 채팅을 통해서 서로 주고 받을 수 있는 서비스입니다. 

- 최대한 많은 기술들을 체험해보고자 Android부터 Server까지 독학하여 진행했습니다.

## App 설명
> 스마트폰 카메라를 이용하여 손가락의 맥박을 실시간으로 측정하여 심박수/호흡수를 추정하고 이를 서로 공유하는 어플리케이션

- Platform: Android
- Languages: Java, C++, PHP

- Algorithm: 
  - 심박수 추정 / Local maxima detection 
  - 심박수 추정 / FFT(Fast Fourier Transform) 
  - 호흡신호 분리 / ICA(Independent Component Analysis) 
   
- Protocol: HTTP, TCP/IP(Socket 통신)

- Server OS: Centos

- DB: MariaDB/Sqlite/SharedPreference

- Library: Glide / MPAndroidChart / JTransforms / Weka-pugin

- NDK: OpenCV(C++)/Dlib(C++)

## 개발 부분
> Client : Android(기여도 : 100%)
- Fragment를 통하여 UI 구성 - MVC pattern
- 자체 회원가입 / 외부 로그인 API를 사용한 Login
- OAuth를 사용한 로그인 (Naver / Kakao / Facebook)

![image](https://user-images.githubusercontent.com/25685282/197930993-7584382b-cc9e-49bb-8260-66c723a0f31f.png)

- Open CV를 이용한 Image Processing
  - JNI(Java Native Interface)를 이용한 Open CV ndk 이식
  - C/C++를 이용하여 카메라의 RGB 값 처리

- 심박수 실시간 측정 알고리즘 자체 개발 후 안드로이드 적용
  - 주변 광원 때문에 생기는 Noise 제거를 위한 FIR 필터 설계 및 구현
  - Noise가 제거된 신호에서 Local Maxima를 구하기 위한 알고리즘 구현
  - 실시간 처리를 위한 Threading
    - Handler를 이용하여 실시간 그래프 업데이트
    - 진행상황 업데이트 - UI / 심박수
    
![image](https://user-images.githubusercontent.com/25685282/197931037-a8ba7cb9-ee07-4497-b4e1-d5acb85bc301.png)

- 측정 된 기록을 서버에 전송 및 DB 저장
  - 측정 완료된 기록을 Local DB 와 서버 DB에 저장

- 기록 관리메뉴 구성 - 페이징, Chart 구성
  - 기록을 그래프로 표시하여 사용자가 자신의 상태를 점검할 수 있도록 구성
  - 연도별/월별로 심박수 데이터를 확인할 수 있도록 구성
  - 동일 연령대/성별에 대한 데이터를 서버에서 계산하여 받아오도록 구성

![image](https://user-images.githubusercontent.com/25685282/197931072-b2da4f4d-a45c-4dca-9a9f-e78107738ac2.png)

- 1:1 / 1:N 채팅
  - 채팅상대에 따라 채팅방 구분하여 표시
  - 프로필 사진 변경 반영
  - 저장한 기록을 서로 주고 받을 수 있도록 구성

![image](https://user-images.githubusercontent.com/25685282/197931115-4c711a8a-8dbc-44bb-bef1-fc71549f55b9.png)
![image](https://user-images.githubusercontent.com/25685282/197931127-f526255d-90ce-4e80-8401-695ddc24c2aa.png)


> Server : Hosting Server (기여도 : 100%)

- HTTP Server : Apache + MariaDB + PHP
  - 회원가입/로그인 시 HTTP 통신을 통해 받아 회원가입/로그인 로직 수행
  - 측정한 Data JSON으로 받아 DB 에 저장
  - Local DB와 Server DB의 동기화 진행
  - 기록된 데이터 처리하여 Android로 전송

- TCP/IP Server : 실시간 Chatting 서버
  - Socket 통신을 이용하여 채팅 사용자끼리 채팅을 주고 받을 수 있도록 설계
  - 기록된 데이터를 주고 받을수 있도록 구현
