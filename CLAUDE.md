# CLAUDE.md

이 파일은 Claude Code가 이 저장소에서 작업할 때 참조하는 가이드입니다.

## 프로젝트 개요

커플 앱 백엔드 서버 - Kotlin + Spring Boot 기반 멀티 모듈 프로젝트

## 기술 스택

- **언어**: Kotlin 1.9.25, Java 21
- **프레임워크**: Spring Boot 3.5.8
- **빌드**: Gradle (Groovy DSL)
- **데이터베이스**: PostgreSQL (Supabase + pgBouncer), Redis
- **인증**: Apple/Google/Kakao OAuth, JWT (JJWT 0.12.6)
- **모니터링**: Sentry 8.29.0
- **푸시알림**: Firebase Cloud Messaging (FCM) - 멀티캐스트, 100 토큰 청크 처리
- **파일 저장**: AWS S3 + CloudFront CDN (Presigned URL)
- **분산 스케줄링**: ShedLock

## 모듈 구조

```
bootstrap/          # 애플리케이션 진입점 (main class: com.yapp.ApplicationKt)
love/
├── domain/         # 도메인 모델, 레포지토리 인터페이스
├── application/    # 서비스 계층 (UseCase)
├── infrastructure/ # 인프라 구현 (JPA, Redis, OAuth, FCM 등)
├── web/            # 컨트롤러, DTO, 보안 설정
└── global-utils/   # 공통 예외, 에러 코드
```

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 포맷팅 검사
./gradlew spotlessCheck

# 애플리케이션 실행
./gradlew :bootstrap:bootRun
```

## 코딩 컨벤션

### 포맷팅
- Spotless + ktlint 1.2.1 사용
- 커밋 전 `./gradlew spotlessApply` 실행 권장
- pre-commit hook 설치: `./gradlew installGitHooks`

### Domain 레이어
- `BaseEntity` 상속하여 생성/수정 시간 자동 관리
- `init` 블록에서 불변 조건(invariant) 검증
- `require()`: 입력값 검증 (IllegalArgumentException)
- `check()`: 상태 검증 (IllegalStateException)
- 팩토리 메서드는 `companion object`에 `of()` 또는 `create()`로 정의
```kotlin
companion object {
    fun create(userId: Long) = UserOnboardingInfo(userId = userId)
}
```

### Application 레이어 (Service)
- 생성자 주입 사용 (trailing comma 권장)
- `@Transactional` 명시적으로 붙이기
- 조회 후 예외 처리 패턴:
```kotlin
val entity = repository.findByUserId(userId)
    ?: throw GlobalException(GlobalErrorCode.NOT_FOUND, "커스텀 메시지")
```
- 상수는 `companion object`에 정의

### Web 레이어 (Controller)
- Swagger 어노테이션 필수: `@Tag`, `@Operation`, `@ApiResponses`
- `@Valid` + Bean Validation으로 입력 검증
- Request/Response DTO는 컨트롤러 파일 하단에 `data class`로 정의
- 인증된 사용자 ID는 `@AuthUser userId: Long`으로 주입

### Swagger 명세 작성 (ApiSpec)
- API별로 `{FeatureName}ApiSpec.kt` 파일에 커스텀 어노테이션 정의
- 발생 가능한 **모든 예외 케이스**를 `ExampleObject`로 명시
- 참고: `love/web/src/main/kotlin/com/yapp/love/web/goal/GoalApiSpec.kt`

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
    summary = "API 요약",
    description = "상세 설명",
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(schema = Schema(implementation = SomeResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "올바르지 않은 입력값",
                            value = """{"status": 400, "code": "G4000", "message": "입력값이 올바르지 않습니다."}""",
                        ),
                        ExampleObject(
                            name = "JSON 형식 오류",
                            value = """{"status": 400, "code": "G4002", "message": "JSON 형식이 올바르지 않습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "인증되지 않은 사용자",
                            value = """{"status": 401, "code": "G4010", "message": "인증되지 않은 사용자입니다."}""",
                        ),
                        ExampleObject(
                            name = "토큰 만료",
                            value = """{"status": 401, "code": "G4011", "message": "토큰이 만료되었습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "접근 권한 없음",
                            value = """{"status": 403, "code": "G4030", "message": "해당 리소스에 대한 권한이 없습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "404",
            description = "리소스를 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "리소스 없음",
                            value = """{"status": 404, "code": "G4040", "message": "리소스를 찾을 수 없습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "서버 오류",
                            value = """{"status": 500, "code": "G5000", "message": "서버 내부 오류가 발생했습니다."}""",
                        ),
                    ],
                ),
            ],
        ),
    ],
)
annotation class SomeApiSpec
```

**필수 import:**
```kotlin
import com.yapp.love.globalutils.exception.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
```

**사용법 (Controller에서):**
```kotlin
@SomeApiSpec
@PostMapping
fun someEndpoint(@AuthUser userId: Long): ResponseEntity<SomeResponse> { ... }
```

### Infrastructure 레이어
- JPA Repository는 `JpaRepository<Entity, Long>` 상속
- 설정 클래스는 생성자 주입으로 `@Value` 프로퍼티 받기
```kotlin
@Configuration
class SomeConfig(
    @Value("\${some.property}")
    private val property: String
)
```
- pgBouncer 사용 시 `prepareThreshold=0` 필수, `maxLifetime`은 pgBouncer idle timeout보다 짧게 설정
- FCM 멀티캐스트: 100 토큰 단위 청크, 만료 토큰 자동 정리
- S3 Presigned URL 업로드 후 CloudFront URL로 응답

### 예외 처리
- 비즈니스 예외: `GlobalException(GlobalErrorCode.XXX, "메시지")`
- 도메인 검증: `require()`, `check()` 사용 → GlobalExceptionHandler에서 처리
- 로깅: `KotlinLogging.logger {}`

## 설정 파일

- 민감한 설정 파일은 `twix-submodule/`에 위치
- 빌드 시 `src/main/resources`로 자동 복사됨 (yml, p8 파일)

## 패키지 구조

- 기본 패키지: `com.yapp.love`
- domain: `com.yapp.love.domain.{도메인명}`
- application: `com.yapp.love.application.{도메인명}`
- infrastructure: `com.yapp.love.infrastructure.{기능명}`
- web: `com.yapp.love.web.{도메인명}`

## 주요 도메인

- **user**: 사용자 관리 (프로필, 닉네임 변경)
- **couple**: 커플 연결 및 관계 정보
- **onboarding**: 온보딩, 초대 코드
- **goal**: 목표 관리 (반복 주기, 상태 전환 스케줄러)
- **photolog**: 포토 로그(오늘 달성할 목표 완료후 찍는 인증샷), 리액션
- **notification**: 푸시 알림, FCM 토큰 관리, 알림 설정
  - 알림 타입: `PARTNER_CONNECTED`, `POKE`, `GOAL_COMPLETED`, `REACTION`, `DAILY_GOAL_ACHIEVED`, `GOAL_ENDED`
  - 딥링크: `twix://notification/{action}`
- **poke**: 파트너에게 목표 알림 찌르기 (3시간 쿨다운)
- **stamp**: 목표 달성 기념 스탬프 (색상 랜덤 배정)
- **stats**: 월별 통계, 캘린더 뷰, 달성 요약

## 이벤트 기반 아키텍처

도메인 이벤트를 `ApplicationEventPublisher`로 발행하고 `NotificationEventListener`에서 처리:

- `PartnerConnectedEvent`: 커플 연결 완료 시
- `PokedEvent`: 찌르기 발생 시
- `PhotologCreatedEvent`: 포토로그 등록 시
- `ReactionCreatedEvent`: 리액션 추가 시
- `GoalEndedEvent`: 목표 종료 시
- `DailyGoalAchievedEvent`: 오늘의 목표 달성 시

## 스케줄러 (ShedLock)

분산 환경에서의 중복 실행 방지를 위해 ShedLock 사용:
- 목표 상태 전환: `NOT_STARTED → IN_PROGRESS → COMPLETED`
- 알림 정리
- 스탬프 생성

## 브랜치 전략

- `main`: 프로덕션 브랜치
- `dev`: 개발 브랜치
- PR은 `main` 브랜치로 생성
