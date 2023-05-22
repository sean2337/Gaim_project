# 📝 [Gaim] GPS만을 이용한 보정 위치정보 제공 서비스 앱 (이중차분 알고리즘)

---

# 👍 프로젝트 상세 설명

## 핵심 포인트

1. **현재 스마트폰의 GNSS정보를 통한 위치획득의 현저히 낮은 정확도 `Gaim` 을 통해 향상시키다.**
2. **여러 정보를 가져와 사용할 필요없이 GPS정보 하나만을 통해 기존보다 높은 정확도의 위치정보 획득**
3. **실시간 현재 위치 정보를 보정 전과 보정 후 정보를 비교하여 사용자들에게 얼마나 보정 되었는지 확인할 수 있게 하여 신뢰도를 높인다.** 
4. **한가지 좌표로 고정된 정보만을 제공하는 것이 아니라, XYZ 좌표, 위경도 좌표 두가지 모두 보정한 위치 정보를 제공하는 서비스 제공**

<br/>
<br/>

## 프로젝트 기획과 배경 및 목적

1. 현재 스마트폰의 위치정보를 얻는 과정은 GNSS 측위과정을 통해 얻고 있지만, 이는 현저히 정확도가 떨어진다. 단순히 우리 일상생활 속에서 길을 찾거나 할때는 물론 문제가 없겠지만, 높은 정확도를 요구하는 일을 할때는 사용할 수 없는 데이터이다.
2. 데이터 값을 받아올때, GPS 정보만을 이용하여 오차를 보정해주는 알고리즘을 개발하자.
3. 위와 같이 개발이 된다면 GPS정보만을 이용하기 때문에 시간적인 면에서도 빠르고 정확도 측면에서도 향상될 것이다. 

<br/><br/>

## 프로젝트의 활용 개념 설명

이 프로젝트의 핵심은 GPS 정보를 어떻게 보정할 것인지에 대한 것이다. 

이를 해결할 방법은 3가지로, DGPS, DD, SSR이 있는데 이번 프로젝트에선 DD(Double Difference) 을 사용

- 이중 차분 기법(Double Difference) 이란?

이중 차분 기법은 수신기 간 단일 차분 (Between-Receiver Single Difference)과 위성 간 단일 차분 (Between-Satellite Single Difference) 기법을 결합함으로써 수신기 시계오차, 위성 시계오차와 수신기 및 위성 하드웨어 오차를 제거할 수 있다.  또한 이중 차분 기법에서 수신기 간 기선거리가 짧을 때는 대류권과 이온층 오차까지 상쇄시킬 수 있다. 

따라서 이중 차분 기법을 측위에 적용한다면 대류권과 이온층 효과를 무시할 수 있고 또한, 수신기 시계오차에 대한 미지수를 설정하지 않아도 되므로 단독 측위 (Point Positioning)보다 비교적 단순한 측위 알고리즘으로도 높은 측위 정확도를 확보할 수 있다.

<br/><br/>

## 🤔 알고리즘 과정(어플리케이션 프로세스)

<img width="817" alt="image" src="https://github.com/sean2337/Gaim_project/assets/100525337/26017fba-2b48-47ac-bedc-2c2c857d3163">


1. **GNSS 원시 데이터 처리 과정**

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
