package com.allset.allset.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class S3Config {

    private val logger = LoggerFactory.getLogger(S3Config::class.java)

    @Value("\${aws.s3.region}")
    private lateinit var region: String

    @Value("\${aws.s3.access-key:}")
    private var accessKey: String = ""

    @Value("\${aws.s3.secret-key:}")
    private var secretKey: String = ""

    @Bean
    fun s3Client(): S3Client {
        val builder = S3Client.builder()
            .region(Region.of(region))

        if (accessKey.isNotBlank() && secretKey.isNotBlank()) {
            logger.info("Using static AWS credentials")
            builder.credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
        } else {
            logger.info("Using default AWS credentials provider chain")
            builder.credentialsProvider(DefaultCredentialsProvider.create())
        }

        return builder.build()
    }
}
