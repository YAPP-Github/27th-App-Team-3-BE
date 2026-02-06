package com.yapp.love.web.photolog

import com.yapp.love.application.photolog.PhotologService
import com.yapp.love.application.storage.FileStoragePort
import com.yapp.love.web.auth.AuthUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Photolog", description = "인증샷 API")
@RestController
@RequestMapping("/api/v1/photologs")
class PhotologController(
    private val photologService: PhotologService,
    private val fileStoragePort: FileStoragePort,
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
