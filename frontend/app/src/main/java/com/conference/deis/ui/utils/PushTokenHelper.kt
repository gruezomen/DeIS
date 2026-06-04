package com.conference.deis.ui.utils

import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.RegistrarPushTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun registrarTokenPushEnBackend() {
    val usuarioId = UserSession.user?.id?.toString() ?: return

    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitInstance.api.registrarPushToken(
                        RegistrarPushTokenRequest(
                            usuarioId = usuarioId,
                            tokenFcm = token
                        )
                    )
                } catch (_: Exception) {
                }
            }
        }
}