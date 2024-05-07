# Android-Floating-Logger
- 앱의 로그를 보여주는 플로팅 앱

# 이 앱을 만들게 된 이유
- 개발자를 제외한 기획자/현업/테스터 등 테스트 과정에서 발생하는 문제의 원인을 제대로 전달하기 위함
- 보안으로 인해 디버깅 할 수 있는 스마트폰이 제한적이다 보니 현상 재현 및 원인 파악이 어려운데 로그가 있으면 추적이 쉬워짐

# How To
- settings.gradle
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' } // add line
    }
}
```

- gradle app_level
```gradle
dependencies {
    implementation 'com.github.cheonjoosung:Android-Floating-Logger:v0.0.1' // need version tag 
}
```

- 앱
```kotlin
val logger = FloatingLogger()
logger.init(this) // AppCompatActivity need when init
```

## Stack & Skill (스택 기술)
- Kotlin & Android


## Sample
|<img src="https://github.com/cheonjoosung/Android-Floating-Logger/blob/master/image/mini.jpg">|<img src="https://github.com/cheonjoosung/Android-Floating-Logger/blob/master/image/expand.jpg">|
|-|-|


## 참조(Reference)
- [화해 Logger](https://blog.hwahae.co.kr/all/tech/8087)