# TranslateGemma 4B 모델 넣기

품질 최우선 경로다. 번역 전용으로 학습된 TranslateGemma 4B 를 LiteRT-LM 런타임으로 돌린다.
**기본 엔진이다.** 모델 파일을 넣기 전에는 번역이 실패하고 그 이유가 화면에 뜬다 —
모델을 넣을 수 없는 기기라면 설정에서 ML Kit 으로 바꾼다(가볍지만 품질이 낮다).

## 앱이 알아서 받는다

공식 Gemma 가중치는 라이선스 동의가 필요한 gated 배포물이지만, 커뮤니티 변환본은 게이트 없이
공개돼 있다. 앱은 그 파일을 직접 내려받는다.

- 파일: `translategemma-4b-it-int4-generic.litertlm` (약 1.9GB)
- 모델이 없으면 **Wi-Fi 일 때 실행하자마자** 받기 시작한다. 종량제 망에서는 크기를 보여 주고
  사용자가 누를 때만 받는다.
- 끊기면 받은 만큼 `.part` 로 남겨 두고 다음에 이어받는다. 다 받은 뒤에야 최종 파일명이 된다.

아래는 **직접 변환한 파일을 쓰거나 다른 변환본을 넣고 싶을 때**의 수동 절차다.

## 수동으로 넣는 절차

1. Hugging Face 에 로그인하고 [google/translategemma-4b-it](https://huggingface.co/google/translategemma-4b-it) 에서 Gemma 이용약관에 동의한다.
2. 안드로이드용 변환본을 받는다 — [barakplasma/translategemma-4b-it-android-task-quantized](https://huggingface.co/barakplasma/translategemma-4b-it-android-task-quantized)
   | 변환본 | 크기 | 요구 RAM | 비고 |
   |---|---|---|---|
   | INT4 generic | 약 2GB | 6GB+ | 기본 추천 |
   | Dynamic INT8 | 약 4GB | 8GB+ | 품질 우선 |
   | INT4 multimodal | 약 2.76GB | 6GB+ | 이미지 번역 포함(현재 앱은 미사용) |
3. `.litertlm` 파일을 기기(다운로드 폴더 등)에 둔다.
4. 앱 오른쪽 위 **설정** → `모델 파일 가져오기` 로 그 파일을 고른다. 엔진은 이미 TranslateGemma 4B 로 선택돼 있다.

## 알아둘 제약

- **백엔드는 CPU 고정.** 이 번들의 GPU 초기화는 현재 실패한다(모델 카드 명시).
- **첫 번역이 느리다.** 엔진 초기화가 10초 안팎 걸린다. 이후에는 엔진을 재사용한다.
- **컨텍스트 1024 토큰.** 파이프라인의 문단 분할(기본 400자)이 이 범위에 들어오도록 잡혀 있다.
- 프롬프트는 `<src>ja</src><dst>ko</dst><text>…</text>` 태그 형식으로 고정이다(모델이 그렇게 학습됐다).
- 엔진을 바꾸면 캐시 키의 모델 버전이 달라져 **기존 번역은 재사용되지 않는다**. 의도된 동작이다.

## 직접 변환하려면

공식 `.task` 변환본은 없다. `litert-torch`(ai-edge-torch) generative API 로 safetensors → `.tflite` 변환 후
Task Bundle 로 묶는다. 커뮤니티 저장소의 `scripts/bundle_litertlm.py` 가 참고가 된다.
변환 결과는 `.litertlm` 이어야 앱이 인식한다.
