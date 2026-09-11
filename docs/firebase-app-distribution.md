# App Distribution 으로 테스터에게 배포하기

Firebase 프로젝트는 `offread-50ac0`, 등록된 패키지는 `com.android.offread` 다.
배포 설정은 `:app` 의 `debug` 빌드 타입에 붙어 있다 — 디버그 키로 서명되므로 테스터가 바로 설치할 수 있다.
릴리스 서명 키가 생기면 `firebaseAppDistribution` 블록을 `release` 로 옮긴다.

## 1. 인증 (최초 1회)

업로드 태스크는 Firebase 자격증명을 요구한다. 셋 중 하나를 고른다.

| 방법 | 명령 | 쓰임새 |
|---|---|---|
| Firebase CLI 로그인 | `npm i -g firebase-tools && firebase login` | 로컬 개발 기본값 |
| 서비스 계정 키 | `export GOOGLE_APPLICATION_CREDENTIALS=/path/key.json` | CI |
| gcloud ADC | `gcloud auth application-default login` | gcloud 를 이미 쓰는 경우 |

서비스 계정을 쓸 경우 **Firebase App Distribution 관리자** 역할을 주고,
프로젝트에서 `firebaseappdistribution.googleapis.com` API 를 켜 둔다.

## 2. 테스터 그룹

빌드 설정이 `testers` 라는 그룹 별칭으로 배포한다.
Firebase 콘솔 → App Distribution → 테스터 및 그룹에서 같은 별칭의 그룹을 만들고 테스터를 넣는다.
그룹 이름을 바꾸려면 `app/build.gradle.kts` 의 `groups` 값을 고친다.

## 3. 업로드

```bash
./gradlew assembleDebug appDistributionUploadDebug -PdistNotes="무엇이 바뀌었는지"
```

`-PdistNotes` 를 생략하면 릴리스 노트는 `내부 테스트 빌드` 가 된다.
업로드가 끝나면 콘솔의 App Distribution 탭에 새 릴리스가 뜨고, 그룹의 테스터에게 메일이 간다.

## 알아둘 점

- `app/google-services.json` 이 저장소에 함께 커밋돼 있다. 이 파일의 API 키는 비밀값이 아니라
  패키지 이름과 서명 지문으로 제한되는 식별자다. 그래도 신경 쓰인다면 `.gitignore` 에 넣고
  CI 시크릿으로 주입한다.
- 테스터는 최초 1회 초대 메일에서 기기를 등록해야 설치 링크를 받는다.
