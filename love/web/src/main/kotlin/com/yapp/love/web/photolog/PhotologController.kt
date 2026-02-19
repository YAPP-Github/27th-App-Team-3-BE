package com.yapp.love.web.photolog

import com.yapp.love.application.couple.CoupleService
import com.yapp.love.application.goal.GoalService
import com.yapp.love.application.photolog.PhotologService
import com.yapp.love.application.storage.FileStoragePort
import com.yapp.love.domain.goal.model.GoalIcon
import com.yapp.love.domain.photolog.model.ReactionType
import com.yapp.love.domain.user.UserAdditionInfoRepository
import com.yapp.love.web.auth.AuthUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDate

@Tag(name = "Photolog", description = "인증샷 API")
@RestController
@RequestMapping("/api/v1/photologs")
class PhotologController(
    private val photologService: PhotologService,
    private val fileStoragePort: FileStoragePort,
    private val coupleService: CoupleService,
    private val userAdditionInfoRepository: UserAdditionInfoRepository,
    private val goalService: GoalService,
) {
    @Operation(summary = "인증샷 업로드 URL 발급")
    @GetMapping("/upload-url")
    fun getUploadUrl(
        @AuthUser userId: Long,
        @RequestParam goalId: Long,
    ): UploadUrlResponse {
        val result = photologService.generateUploadUrl(userId, goalId)
        return UploadUrlResponse(
            uploadUrl = result.uploadUrl,
            fileName = result.fileName,
        )
    }

    @Operation(summary = "인증샷 등록")
    @PostMapping
    fun createPhotolog(
        @AuthUser userId: Long,
        @Valid @RequestBody request: CreatePhotologRequest,
    ): PhotologResponse {
        val photolog =
            photologService.createPhotolog(
                goalId = request.goalId,
                userId = userId,
                fileName = request.fileName,
                comment = request.comment,
                verificationDate = request.verificationDate ?: LocalDate.now(),
            )
        return PhotologResponse(
            photologId = photolog.id!!,
            goalId = photolog.goalId,
            imageUrl = fileStoragePort.getPhotologUrl(photolog.goalId, photolog.fileName),
            comment = photolog.comment,
            verificationDate = photolog.verificationDate,
        )
    }

    @Operation(summary = "날짜별 인증샷 목록 조회")
    @GetMapping
    fun getPhotologsByDate(
        @AuthUser userId: Long,
        @RequestParam targetDate: LocalDate,
    ): PhotologListResponse {
        val coupleInfo = coupleService.getCoupleInfoByUserId(userId)
        val coupleId = coupleInfo.id!!
        val partnerId = coupleService.getPartnerUserIdByCoupleInfo(coupleInfo, userId)

        val myNickname = userAdditionInfoRepository.findByUserId(userId)?.nickname ?: "나"
        val partnerNickname = userAdditionInfoRepository.findByUserId(partnerId)?.nickname ?: "상대방"

        val goalsWithPhotologs = goalService.getGoalsWithPhotologs(
            coupleId = coupleId,
            myUserId = userId,
            partnerUserId = partnerId,
            targetDate = targetDate,
        )

        return PhotologListResponse(
            targetDate = targetDate,
            myNickname = myNickname,
            partnerNickname = partnerNickname,
            photologs = goalsWithPhotologs.map { goalWithPhotologs ->
                GoalPhotologPair(
                    goalId = goalWithPhotologs.goal.id!!,
                    goalName = goalWithPhotologs.goal.name,
                    goalIcon = goalWithPhotologs.goal.icon,
                    myPhotolog = goalWithPhotologs.myPhotolog?.let {
                        PhotologDetailResponse(
                            photologId = it.id!!,
                            goalId = it.goalId,
                            imageUrl = fileStoragePort.getPhotologUrl(it.goalId, it.fileName),
                            comment = it.comment,
                            verificationDate = it.verificationDate,
                            uploaderName = myNickname,
                            uploadedAt = it.uploadedAt,
                            reaction = it.reaction,
                        )
                    },
                    partnerPhotolog = goalWithPhotologs.partnerPhotolog?.let {
                        PhotologDetailResponse(
                            photologId = it.id!!,
                            goalId = it.goalId,
                            imageUrl = fileStoragePort.getPhotologUrl(it.goalId, it.fileName),
                            comment = it.comment,
                            verificationDate = it.verificationDate,
                            uploaderName = partnerNickname,
                            uploadedAt = it.uploadedAt,
                            reaction = it.reaction,
                        )
                    },
                )
            },
        )
    }

    @AddReactionApiSpec
    @PutMapping("/{photologId}/reaction")
    fun addReaction(
        @AuthUser userId: Long,
        @PathVariable photologId: Long,
        @Valid @RequestBody request: ReactionRequest,
    ): ReactionResponse {
        val result = photologService.addReaction(photologId, userId, request.reaction)
        return ReactionResponse(
            photologId = result.photologId,
            reaction = result.reaction,
        )
    }

    @Operation(summary = "인증샷 수정")
    @PutMapping("/{photologId}")
    fun updatePhotolog(
        @AuthUser userId: Long,
        @PathVariable photologId: Long,
        @Valid @RequestBody request: UpdatePhotologRequest,
    ) {
        photologService.updatePhotolog(
            photologId = photologId,
            userId = userId,
            fileName = request.fileName,
            comment = request.comment,
        )
    }

    @Operation(summary = "인증샷 삭제")
    @DeleteMapping("/{photologId}")
    fun deletePhotolog(
        @AuthUser userId: Long,
        @PathVariable photologId: Long,
    ) {
        photologService.deletePhotolog(photologId, userId)
    }
}

data class UploadUrlResponse(
    val uploadUrl: String,
    val fileName: String,
)

data class CreatePhotologRequest(
    val goalId: Long,
    @field:NotBlank(message = "파일명은 필수입니다.")
    val fileName: String,
    @field:Size(max = 5, message = "코멘트는 5자 이내여야 합니다.")
    val comment: String? = null,
    val verificationDate: LocalDate? = null,
)

data class UpdatePhotologRequest(
    @field:NotBlank(message = "파일명은 필수입니다.")
    val fileName: String,
    @field:Size(max = 5, message = "코멘트는 5자 이내여야 합니다.")
    val comment: String? = null,
)

data class PhotologResponse(
    val photologId: Long,
    val goalId: Long,
    val imageUrl: String,
    val comment: String?,
    val verificationDate: LocalDate,
)

data class PhotologDetailResponse(
    val photologId: Long,
    val goalId: Long,
    val imageUrl: String,
    val comment: String?,
    val verificationDate: LocalDate,
    val uploaderName: String,
    val uploadedAt: Instant,
    val reaction: ReactionType?,
)

data class PhotologListResponse(
    val targetDate: LocalDate,
    val myNickname: String,
    val partnerNickname: String,
    val photologs: List<GoalPhotologPair>,
)

data class GoalPhotologPair(
    val goalId: Long,
    val goalName: String,
    val goalIcon: GoalIcon,
    val myPhotolog: PhotologDetailResponse?,
    val partnerPhotolog: PhotologDetailResponse?,
)

data class ReactionRequest(
    @field:NotNull(message = "리액션은 필수입니다")
    val reaction: ReactionType,
)

data class ReactionResponse(
    val photologId: Long,
    val reaction: ReactionType,
)
