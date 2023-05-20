## 🤔 알고리즘 과정(어플리케이션 프로세스)

<img width="817" alt="image" src="https://github.com/sean2337/Gaim_project/assets/100525337/26017fba-2b48-47ac-bedc-2c2c857d3163">


1. **GNSS 윈시 데이터 처리 과정**

<img width="590" alt="image" src="https://github.com/sean2337/Gaim_project/assets/100525337/96c66950-804b-4ccf-b827-d2890574cc7a">

실시간으로 스마트폰으로 얻은 `raw data` 를 디코딩하는 작업을 했다. 왼쪽의 이 `raw data` 는 아직 관측값으로 변환되지 않은 상태이다. 오른쪽 영상은 매 초 수신되는 이 `raw Data` 를 실시간으로 관측값으로 변환하여 표시하는 화면이다. 이렇게 변환된 관측값은 `스마트폰의 좌표`를 구하는데 사용된다.

2. **기준국 데이터 불러오기**

<img width="706" alt="image" src="https://github.com/sean2337/Gaim_project/assets/100525337/14b3c998-2d35-464e-b7a4-b28ca45ed64b">

왼쪽의 웹페이지는 `PPHQ의 관측값 데이터 RTCM` 을 수신하여 매초 저장하고, 출력하는 화면이다. 매초 스마트폰에서 이 웹페이지에 출력된 텍스트를 읽어, PPHQ의 실시간 관측값을 가져온다.

3. **궤도력 데이터 불러오기**

<img width="697" alt="image" src="https://github.com/sean2337/Gaim_project/assets/100525337/fe8962d3-312c-48d6-a7cf-3495987f3d60">

이 웹페이지는 위성의 위치를 계산하는데 사용할 궤도력 데이터가 저장된 웹페이지이다.

PPHQ의 관측값을 불러오는 것과 마찬가지로, 웹페이지를 읽어 위성 별 궤도력 데이터를 가져와 위성의 위치를 계산할 수 있다.

4. **측위 코드 구현**

위의 가져온 정보들을 통해 `DD 알고리즘(측위코드)`을 구현했다. 기존 `Matlab`을 통해 구현 했고, 여기서 측위코드의 성능을 확인보았다.

<img width="271" alt="image" src="https://github.com/sean2337/Gaim_project/assets/100525337/1c1a156f-79bd-4db0-94c2-f483d2f4f242">

실제로 수평오차는 3.51 m → 2.00 m 로 43%정도 오차가 감소 하였고, 수직오차는 5.73 m 에서 3.68 m 만큼 감소한 것을 확인했다. 이후에 이를 Java코드로 수정 하였고, 이를 안드로이드 스튜디오에 적용시켰다. 적용하여 나온 값을 아래와 같은 어플리케이션 형태로 개발하였다.

5. **앱 시연 영상**
https://studio.youtube.com/video/wVYm54INt5s/edit
