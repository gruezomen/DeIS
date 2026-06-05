package com.deis.backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream

@Configuration
class FirebaseConfig {

    @PostConstruct
    fun init() {
        if (FirebaseApp.getApps().isNotEmpty()) return

        val serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
            ?: throw IllegalStateException("FIREBASE_SERVICE_ACCOUNT_JSON no está configurado")

        val credentials = GoogleCredentials.fromStream(
            ByteArrayInputStream(serviceAccountJson.toByteArray(Charsets.UTF_8))
        )

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()

        FirebaseApp.initializeApp(options)
    }
}