package com.yapp.love.web.onboarding

import com.yapp.love.application.onboarding.OnboardingService
import com.yapp.love.domain.onboarding.model.OnboardingStatus
import com.yapp.love.web.auth.AuthUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.*
import java.time.LocalDate


@Tag(name = "Onboarding", description = "온보딩 API")
@RestController
@RequestMapping("/api/v1/onboarding")
class OnboardingController(
    private val onboardingService: OnboardingService,
) {

    @Operation(summary = "온보딩 상태 조회")
    @GetMapping("/status")
    fun getOnboardingStatus(
        @AuthUser userId: Long,
    ): OnboardingStatusResponse {
        return OnboardingStatusResponse(
            onboardingService.getOnboardingStatus(userId)
        )
    }

    @GetMapping("/invite-code")
    fun getInviteCode(
        @AuthUser userId: Long,
    ): InviteCodeResponse {
        return InviteCodeResponse(
            onboardingService.getOrCreateInviteCode(userId)
        )
    }

    data class InviteCodeResponse(
        val inviteCode: String,
    )

    @Operation(summary = "커플 연결")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "커플 연결 성공"),
        ApiResponse(responseCode = "404", description = "잘못된 코드"),
    )
    @PostMapping("/couple-connection")
    fun coupleConnection(
        @AuthUser userId: Long,
        @Valid @RequestBody request: CoupleConnectionRequest,
    ) {
        onboardingService.connectCouple(userId, request.inviteCode)
    }

    @Operation(summary = "프로필 등록")
    @PostMapping("/profile")
    fun profileSetup(
        @AuthUser userId: Long,
        @Valid @RequestBody request: ProfileSetupRequest,
    ) {
        onboardingService.setProfile(userId, request.nickname)
    }

    @Operation(summary = "기념일 설정")
    @PostMapping("/anniversary")
    fun anniversarySetup(
        @AuthUser userId: Long,
        @Valid @RequestBody request: AnniversarySetupRequest,
    ) {
        onboardingService.setAnniversary(userId, request.anniversaryDate)
    }
}

data class CoupleConnectionRequest(
    @field:NotBlank(message = "초대 코드는 필수입니다.")
    val inviteCode: String,
)

data class ProfileSetupRequest(
    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Size(min = 1, max = 10, message = "닉네임은 1~10자 이내여야 합니다.")
    val nickname: String,
)

data class AnniversarySetupRequest(
    @field:NotNull(message = "기념일은 필수입니다.")
    val anniversaryDate: LocalDate,
)

data class OnboardingStatusResponse(
    val status: OnboardingStatus,
)
