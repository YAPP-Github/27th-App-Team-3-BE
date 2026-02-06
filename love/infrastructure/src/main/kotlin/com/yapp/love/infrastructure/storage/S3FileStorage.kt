package com.yapp.love.infrastructure.storage

import com.yapp.love.application.storage.FileStoragePort
import com.yapp.love.application.storage.PresignedUrlResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Component
class S3FileStorage(
    private val s3Presigner: S3Presigner,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String,
    @Value("\${cloud.aws.cloudfront.domain}")
    private val cloudfrontDomain: String,
) : FileStoragePort {
    override fun generatePresignedUploadUrl(key: String): PresignedUrlResult {
        val putObjectRequest =
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()

        val presignRequest =
            PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES))
                .putObjectRequest(putObjectRequest)
                .build()

        val presignedRequest = s3Presigner.presignPutObject(presignRequest)
        val fileName = key.substringAfterLast("/")

        return PresignedUrlResult(
            uploadUrl = presignedRequest.url().toString(),
            fileName = fileName,
        )
    }

    override fun getPhotologUrl(
        goalId: Long,
        fileName: String,
    ): String {
        return "$cloudfrontDomain/photolog/$goalId/$fileName"
    }

    companion object {
        private const val PRESIGNED_URL_EXPIRATION_MINUTES = 10L
    }
}
