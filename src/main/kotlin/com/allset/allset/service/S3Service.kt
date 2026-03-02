package com.allset.allset.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

@Service
class S3Service(
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket}") private val bucketName: String,
    @Value("\${aws.s3.region}") private val region: String
) {

    private val logger = LoggerFactory.getLogger(S3Service::class.java)

    fun uploadFile(file: MultipartFile, folder: String = "uploads"): String {
        val originalName = file.originalFilename ?: "image"
        val key = "$folder/${UUID.randomUUID()}_$originalName"

        val putRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(file.contentType ?: "application/octet-stream")
            .build()

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.inputStream, file.size))
        logger.info("Uploaded file to S3: $key")

        return getPublicUrl(key)
    }

    fun deleteFile(fileUrl: String) {
        val key = extractKeyFromUrl(fileUrl) ?: return
        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()

        s3Client.deleteObject(deleteRequest)
        logger.info("Deleted file from S3: $key")
    }

    fun getTemplateUrl(imageName: String): String {
        return getPublicUrl("templates/$imageName")
    }

    private fun getPublicUrl(key: String): String {
        return "https://$bucketName.s3.$region.amazonaws.com/$key"
    }

    private fun extractKeyFromUrl(url: String): String? {
        val prefix = "https://$bucketName.s3.$region.amazonaws.com/"
        return if (url.startsWith(prefix)) url.removePrefix(prefix) else null
    }
}
