package com.yapp.love.infrastructure.fcm.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

private val logger = KotlinLogging.logger {}

@Configuration
class FcmConfig(
    @Value("\${firebase.credentials.path}")
    private val firebaseSdkResource: Resource,
) {
    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        return firebaseSdkResource.inputStream.use { inputStream ->
            val firebaseApp = getOrInitializeFirebaseApp(inputStream)
            FirebaseMessaging.getInstance(firebaseApp)
        }
    }

    private fun getOrInitializeFirebaseApp(refreshToken: java.io.InputStream): FirebaseApp {
        val existingApp = FirebaseApp.getApps().find { it.name == FirebaseApp.DEFAULT_APP_NAME }
        if (existingApp != null) {
            return existingApp
        }

        val options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(refreshToken))
                .build()

        return FirebaseApp.initializeApp(options)
    }
}
