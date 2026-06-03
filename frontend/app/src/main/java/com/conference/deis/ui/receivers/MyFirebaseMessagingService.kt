package com.conference.deis.ui.receivers

import com.conference.deis.network.RetrofitInstance
import com.conference.deis.network.UserSession
import com.conference.deis.network.model.RegistrarPushTokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val usuarioId = UserSession.user?.id?.toString() ?: return

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