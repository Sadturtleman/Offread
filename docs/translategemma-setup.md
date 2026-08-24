# TranslateGemma 4B 모델 넣기 (F-020)

품질 최우선 경로다. 번역 전용으로 학습된 TranslateGemma 4B 를 LiteRT-LM 런타임으로 돌린다.

## 왜 파일을 직접 넣어야 하나

Gemma 계열 가중치는 **라이선스 동의가 필요한 gated 배포물**이라 앱이 대신 내려받지 않는다(P-02·P-03 와도 정합).
사용자가 받아 둔 파일을 설정에서 골라 앱 전용 저장소로 가져오는 방식만 제공한다.

## 절차

1. Hugging Face 에 로그인하고 [google/translategemma-4b-it](https://huggingface.co/google/translategemma-4b-it) 에서 Gemma 이용약관에 동의한다.
2. 안드로이드용 변환본을 받는다 — [barakplasma/translategemma-4b-it-android-task-quantized](https://huggingface.co/barakplasma/translategemma-4b-it-android-task-quantized)
   | 변환본 | 크기 | 요구 RAM | 비고 |
   |---|---|---|---|
   | INT4 generic | 약 2GB | 6GB+ | 기본 추천 |
   | Dynamic INT8 | 약 4GB | 8GB+ | 품질 우선 |
   | INT4 multimodal | 약 2.76GB | 6GB+ | 이미지 번역 포함(현재 앱은 미사용) |
3. `.litertlm` 파일을 기기(다운로드 폴더 등)에 둔다.
4. 앱에서 **설정 → 번역 엔진·모델 → TranslateGemma 4B** 를 고르고 `모델 파일 가져오기` 로 파일을 선택한다.

## 알아둘 제약

- **백엔드는 CPU 고정.** 이 번들의 GPU 초기화는 현재 실패한다(모델 카드 명시).
- **첫 번역이 느리다.** 엔진 초기화가 10초 안팎 걸린다. 이후에는 엔진을 재사용한다.
- **컨텍스트 1024 토큰.** 파이프라인의 문단 분할(기본 400자)이 이 범위에 들어오도록 잡혀 있다.
- **용어맵은 후처리로만 반영된다.** TranslateGemma 프롬프트는 `<src>ja</src><dst>ko</dst><text>…</text>` 형식이 고정이라
  용어 지시문을 넣을 자리가 없다. 고정(pin) 용어는 번역 후 치환으로 강제된다.
- 엔진을 바꾸면 캐시 키의 모델 버전이 달라져 **기존 번역은 재사용되지 않는다**(F-021). 의도된 동작이다.

## 직접 변환하려면

공식 `.task` 변환본은 없다. `litert-torch`(ai-edge-torch) generative API 로 safetensors → `.tflite` 변환 후
Task Bundle 로 묶는다. 커뮤니티 저장소의 `scripts/bundle_litertlm.py` 가 참고가 된다.
변환 결과는 `.litertlm` 이어야 이 앱의 TranslateGemma 엔진이 인식한다(`.task` 는 MediaPipe 엔진용).
