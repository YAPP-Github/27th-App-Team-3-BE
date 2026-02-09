package com.yapp.love.web.photolog

import com.yapp.love.application.couple.CoupleService
import com.yapp.love.application.goal.GoalService
import com.yapp.love.application.photolog.PhotologService
import com.yapp.love.application.storage.FileStoragePort
import com.yapp.love.domain.user.UserAdditionInfoRepository
import com.yapp.love.web.auth.AuthUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import com.yapp.love.domain.photolog.model.ReactionType
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

    @Operation(summary = "목표별 인증샷 목록 조회")
    @GetMapping("/goals/{goalId}")
    fun getPhotologsByGoal(
        @AuthUser userId: Long,
        @PathVariable goalId: Long,
    ): PhotologListResponse {
        val coupleInfo = coupleService.getCoupleInfoByUserId(userId)
        val partnerId = coupleService.getPartnerUserIdByCoupleInfo(coupleInfo, userId)
        val goal = goalService.getGoalById(userId, goalId)

        val myNickname = userAdditionInfoRepository.findByUserId(userId)?.nickname ?: "나"
        val partnerNickname = userAdditionInfoRepository.findByUserId(partnerId)?.nickname ?: "상대방"

        val photologs = photologService.getPhotologsByGoalId(goalId)

        return PhotologListResponse(
            goalId = goalId,
            myNickname = myNickname,
            partnerNickname = partnerNickname,
            goalTitle = goal.name,
            photologs = photologs.map { photolog ->
                PhotologDetailResponse(
                    photologId = photolog.id!!,
                    goalId = photolog.goalId,
                    imageUrl = fileStoragePort.getPhotologUrl(photolog.goalId, photolog.fileName),
                    comment = photolog.comment,
                    verificationDate = photolog.verificationDate,
                    isMine = photolog.userId == userId,
                    uploaderName = if (photolog.userId == userId) myNickname else partnerNickname,
                    uploadedAt = photolog.uploadedAt,
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
}

data class UploadUrlResponse(
    val uploadUrl: String,
    val fileName: String,
)

data class CreatePhotologRequest(
    val goalId: Long,
    @field:NotBlank(message = "파일명은 필수입니다.")
    val fileName: String,
    @field:Size(max = 30, message = "코멘트는 5자 이내여야 합니다.")
    val comment: String? = null,
    val verificationDate: LocalDate? = null,
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
    val isMine: Boolean,
    val uploaderName: String,
    val uploadedAt: Instant,
)

data class PhotologListResponse(
    val goalId: Long,
    val myNickname: String,
    val partnerNickname: String,
    val goalTitle: String,
    val photologs: List<PhotologDetailResponse>?,
)

data class ReactionRequest(
    @field:NotNull(message = "리액션은 필수입니다")
    val reaction: ReactionType,
)

data class ReactionResponse(
    val photologId: Long,
    val reaction: ReactionType,
)
